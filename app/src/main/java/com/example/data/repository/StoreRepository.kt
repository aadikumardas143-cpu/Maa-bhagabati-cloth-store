package com.example.data.repository

import com.example.data.local.*
import kotlinx.coroutines.flow.Flow

class StoreRepository(private val db: AppDatabase) {

    val allClothItems: Flow<List<ClothItem>> = db.clothItemDao().getAllClothItems()
    val allCustomers: Flow<List<CustomerKhata>> = db.khataDao().getAllCustomers()
    val allBills: Flow<List<StoreBill>> = db.billDao().getAllBills()
    val totalCollectableBalance: Flow<Double?> = db.khataDao().getTotalCollectableBalance()
    val totalSales: Flow<Double?> = db.billDao().getTotalSales()

    suspend fun checkAndSeedInitialData() {
        if (db.clothItemDao().getItemCount() == 0) {
            val initialItems = listOf(
                ClothItem(
                    title = "Sambalpuri Ikat Silk Saree (Red & Gold Zari)",
                    category = "Sambalpuri",
                    price = 6800.0,
                    mrp = 8500.0,
                    stockCount = 12,
                    fabricType = "Pure Silk",
                    color = "Crimson Red",
                    description = "Authentic handwoven Sambalpuri double ikat silk saree with traditional Pasapalli border and grand pallu.",
                    isFeatured = true,
                    barcode = "MBCS-1001"
                ),
                ClothItem(
                    title = "Bomkai Handloom Cotton Saree",
                    category = "Handloom",
                    price = 3200.0,
                    mrp = 4000.0,
                    stockCount = 18,
                    fabricType = "Pure Cotton",
                    color = "Royal Blue",
                    description = "Traditional Sonepuri Bomkai weave with intricate fish and flower motifs along the border.",
                    isFeatured = true,
                    barcode = "MBCS-1002"
                ),
                ClothItem(
                    title = "Banarasi Katan Silk Bridal Saree",
                    category = "Banarasi",
                    price = 12500.0,
                    mrp = 15900.0,
                    stockCount = 6,
                    fabricType = "Katan Silk",
                    color = "Maroon & Gold",
                    description = "Heavy bridal Banarasi silk with royal kadwa weave, kadwa zari work, and rich pallu.",
                    isFeatured = true,
                    barcode = "MBCS-1003"
                ),
                ClothItem(
                    title = "Kanjeevaram Silk Saree (Peacock Green)",
                    category = "Silk Saree",
                    price = 9800.0,
                    mrp = 12000.0,
                    stockCount = 8,
                    fabricType = "Kanchipuram Silk",
                    color = "Peacock Green & Magenta",
                    description = "Lustrous mulberry silk with contrast golden zari temple border.",
                    isFeatured = false,
                    barcode = "MBCS-1004"
                ),
                ClothItem(
                    title = "Designer Heavy Bridal Lehenga Set",
                    category = "Bridal",
                    price = 18500.0,
                    mrp = 23000.0,
                    stockCount = 4,
                    fabricType = "Velvet & Net",
                    color = "Ruby Red",
                    description = "Full zardozi and gota patti embroidery lehenga with matching dupatta and blouse piece.",
                    isFeatured = true,
                    barcode = "MBCS-1005"
                ),
                ClothItem(
                    title = "Chanderi Silk Unstitched Suit Material",
                    category = "Suits",
                    price = 2600.0,
                    mrp = 3200.0,
                    stockCount = 15,
                    fabricType = "Chanderi Silk",
                    color = "Mustard Yellow",
                    description = "Top Chanderi silk with thread work, santoon bottom, and digital print organza dupatta.",
                    isFeatured = false,
                    barcode = "MBCS-1006"
                ),
                ClothItem(
                    title = "Men's Dupion Silk Kurta Pyjama Set",
                    category = "Men's Wear",
                    price = 3400.0,
                    mrp = 4200.0,
                    stockCount = 10,
                    fabricType = "Dupion Silk",
                    color = "Off-White & Gold",
                    description = "Festive ethnic menswear kurta set with mandarin collar and churidar.",
                    isFeatured = false,
                    barcode = "MBCS-1007"
                ),
                ClothItem(
                    title = "Pasapalli Handloom Cotton Dress Material",
                    category = "Fabric",
                    price = 1450.0,
                    mrp = 1800.0,
                    stockCount = 25,
                    fabricType = "Handloom Cotton",
                    color = "Black & Red",
                    description = "Pure cotton 3-piece unstitched dress material with traditional checkerboard Pasapalli print.",
                    isFeatured = false,
                    barcode = "MBCS-1008"
                )
            )
            db.clothItemDao().insertAll(initialItems)

            // Seed sample Khata accounts
            val cust1Id = db.khataDao().insertCustomer(
                CustomerKhata(name = "Rajesh Mishra", phone = "9861000001", totalBalance = 4500.0, city = "Bhubaneswar", notes = "Bought 1 Banarasi Saree on credit")
            )
            db.khataDao().insertTransaction(
                KhataTransaction(customerId = cust1Id, amount = 4500.0, type = "GAVE", note = "Banarasi Saree Udhari")
            )

            val cust2Id = db.khataDao().insertCustomer(
                CustomerKhata(name = "Priya Dash", phone = "9437000002", totalBalance = 2200.0, city = "Cuttack", notes = "Regular wedding customer")
            )
            db.khataDao().insertTransaction(
                KhataTransaction(customerId = cust2Id, amount = 3200.0, type = "GAVE", note = "Bomkai Saree")
            )
            db.khataDao().insertTransaction(
                KhataTransaction(customerId = cust2Id, amount = 1000.0, type = "GOT", note = "UPI Payment")
            )
        }
    }

    suspend fun insertClothItem(item: ClothItem) = db.clothItemDao().insertItem(item)
    suspend fun updateClothItem(item: ClothItem) = db.clothItemDao().updateItem(item)
    suspend fun deleteClothItem(item: ClothItem) = db.clothItemDao().deleteItem(item)
    suspend fun getItemByBarcode(barcode: String) = db.clothItemDao().getItemByBarcode(barcode)

    suspend fun addCustomer(customer: CustomerKhata) = db.khataDao().insertCustomer(customer)
    suspend fun addKhataTransaction(customerId: Long, amount: Double, type: String, note: String) {
        val cust = db.khataDao().getCustomerById(customerId) ?: return
        val newBalance = if (type == "GAVE") cust.totalBalance + amount else cust.totalBalance - amount
        db.khataDao().updateCustomer(cust.copy(totalBalance = newBalance, updatedAt = System.currentTimeMillis()))
        db.khataDao().insertTransaction(
            KhataTransaction(customerId = customerId, amount = amount, type = type, note = note)
        )
    }

    fun getTransactionsForCustomer(customerId: Long) = db.khataDao().getTransactionsForCustomer(customerId)

    suspend fun saveBill(bill: StoreBill) = db.billDao().insertBill(bill)
}
