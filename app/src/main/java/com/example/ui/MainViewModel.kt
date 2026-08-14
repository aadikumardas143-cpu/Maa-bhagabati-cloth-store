package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.*
import com.example.data.repository.StoreRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class StoreTab {
    CATALOG, POS_BILLING, KHATA_LEDGER, INVENTORY, WEB_STORE
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: StoreRepository

    val currentTab = MutableStateFlow(StoreTab.CATALOG)
    val searchQuery = MutableStateFlow("")
    val selectedCategory = MutableStateFlow("All")

    val clothItems: StateFlow<List<ClothItem>>
    val khataCustomers: StateFlow<List<CustomerKhata>>
    val totalCollectableBalance: StateFlow<Double>
    val storeBills: StateFlow<List<StoreBill>>
    val totalSales: StateFlow<Double>

    // POS Cart State: Item -> Quantity
    val cartItems = MutableStateFlow<Map<ClothItem, Int>>(emptyMap())

    // Selected customer for Khata detail view
    val selectedKhataCustomer = MutableStateFlow<CustomerKhata?>(null)
    val selectedCustomerTransactions = MutableStateFlow<List<KhataTransaction>>(emptyList())

    // Dialog states
    val showAddProductDialog = MutableStateFlow(false)
    val showAddCustomerDialog = MutableStateFlow(false)
    val showAddKhataTxDialog = MutableStateFlow(false)
    val showBillReceiptDialog = MutableStateFlow<StoreBill?>(null)

    init {
        val db = AppDatabase.getDatabase(application)
        repository = StoreRepository(db)

        viewModelScope.launch {
            repository.checkAndSeedInitialData()
        }

        clothItems = repository.allClothItems.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        khataCustomers = repository.allCustomers.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        totalCollectableBalance = repository.totalCollectableBalance.map { it ?: 0.0 }.stateIn(viewModelScope, SharingStarted.Lazily, 0.0)
        storeBills = repository.allBills.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        totalSales = repository.totalSales.map { it ?: 0.0 }.stateIn(viewModelScope, SharingStarted.Lazily, 0.0)
    }

    fun selectTab(tab: StoreTab) {
        currentTab.value = tab
    }

    fun setSearch(query: String) {
        searchQuery.value = query
    }

    fun setCategory(category: String) {
        selectedCategory.value = category
    }

    // Cart Operations
    fun addToCart(item: ClothItem) {
        val current = cartItems.value.toMutableMap()
        val count = current[item] ?: 0
        current[item] = count + 1
        cartItems.value = current
    }

    fun removeFromCart(item: ClothItem) {
        val current = cartItems.value.toMutableMap()
        val count = current[item] ?: 0
        if (count > 1) {
            current[item] = count - 1
        } else {
            current.remove(item)
        }
        cartItems.value = current
    }

    fun clearCart() {
        cartItems.value = emptyMap()
    }

    fun checkoutBill(
        customerName: String,
        customerPhone: String,
        discountPercent: Double,
        includeGst: Boolean,
        paymentType: String
    ) {
        viewModelScope.launch {
            val itemsMap = cartItems.value
            if (itemsMap.isEmpty()) return@launch

            var subtotal = 0.0
            val summaryLines = mutableListOf<String>()

            itemsMap.forEach { (item, qty) ->
                val lineTotal = item.price * qty
                subtotal += lineTotal
                summaryLines.add("${item.title} x$qty (₹${lineTotal.toInt()})")

                // Deduct stock count
                val newStock = (item.stockCount - qty).coerceAtLeast(0)
                repository.updateClothItem(item.copy(stockCount = newStock))
            }

            val discountAmt = subtotal * (discountPercent / 100.0)
            val afterDiscount = subtotal - discountAmt
            val gstAmt = if (includeGst) afterDiscount * 0.05 else 0.0
            val totalPayable = afterDiscount + gstAmt

            val billNumber = "MBCS-${System.currentTimeMillis().toString().takeLast(6)}"

            val newBill = StoreBill(
                billNumber = billNumber,
                customerName = customerName.ifBlank { "Walk-in Customer" },
                customerPhone = customerPhone.ifBlank { "N/A" },
                subtotal = subtotal,
                discountPercent = discountPercent,
                discountAmount = discountAmt,
                gstAmount = gstAmt,
                totalAmount = totalPayable,
                paymentType = paymentType,
                itemsSummary = summaryLines.joinToString("\n")
            )

            repository.saveBill(newBill)

            // If paymentType is KHATA, add to Khata credit ledger
            if (paymentType == "KHATA" && customerName.isNotBlank()) {
                val existingCust = khataCustomers.value.find { it.phone == customerPhone || it.name.equals(customerName, ignoreCase = true) }
                val custId = if (existingCust != null) {
                    existingCust.id
                } else {
                    repository.addCustomer(CustomerKhata(name = customerName, phone = customerPhone, totalBalance = 0.0))
                }
                repository.addKhataTransaction(custId, totalPayable, "GAVE", "Bill #$billNumber")
            }

            clearCart()
            showBillReceiptDialog.value = newBill
        }
    }

    // Khata Operations
    fun selectCustomerForDetail(customer: CustomerKhata?) {
        selectedKhataCustomer.value = customer
        if (customer != null) {
            viewModelScope.launch {
                repository.getTransactionsForCustomer(customer.id).collect {
                    selectedCustomerTransactions.value = it
                }
            }
        } else {
            selectedCustomerTransactions.value = emptyList()
        }
    }

    fun addNewKhataCustomer(name: String, phone: String, city: String, initialAmount: Double) {
        viewModelScope.launch {
            val custId = repository.addCustomer(CustomerKhata(name = name, phone = phone, city = city, totalBalance = initialAmount))
            if (initialAmount > 0) {
                repository.addKhataTransaction(custId, initialAmount, "GAVE", "Initial Credit Opening Balance")
            }
        }
    }

    fun addKhataTransaction(customerId: Long, amount: Double, type: String, note: String) {
        viewModelScope.launch {
            repository.addKhataTransaction(customerId, amount, type, note)
            // Refresh customer
            val updated = repository.allCustomers.first().find { it.id == customerId }
            if (updated != null) {
                selectedKhataCustomer.value = updated
            }
        }
    }

    // Inventory operations
    fun addClothItem(
        title: String,
        category: String,
        price: Double,
        mrp: Double,
        stock: Int,
        fabric: String,
        color: String,
        description: String,
        barcode: String
    ) {
        viewModelScope.launch {
            val item = ClothItem(
                title = title,
                category = category,
                price = price,
                mrp = mrp,
                stockCount = stock,
                fabricType = fabric,
                color = color,
                description = description,
                barcode = barcode.ifBlank { "MBCS-${(1000..9999).random()}" }
            )
            repository.insertClothItem(item)
        }
    }

    fun updateStock(item: ClothItem, newStock: Int) {
        viewModelScope.launch {
            repository.updateClothItem(item.copy(stockCount = newStock))
        }
    }
}
