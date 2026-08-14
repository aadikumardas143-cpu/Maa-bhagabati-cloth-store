package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ClothItem
import com.example.data.local.StoreBill
import com.example.ui.MainViewModel
import com.example.ui.theme.DarkRoyalMaroon
import com.example.ui.theme.LightZariGold
import com.example.ui.theme.RoyalMaroon
import com.example.ui.theme.ZariGold

@Composable
fun POSBillingScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val cart by viewModel.cartItems.collectAsState()
    val showBillReceipt by viewModel.showBillReceiptDialog.collectAsState()

    var customerName by remember { mutableStateOf("") }
    var customerPhone by remember { mutableStateOf("") }
    var discountPercent by remember { mutableStateOf(0.0) }
    var includeGst by remember { mutableStateOf(true) }
    var selectedPaymentMode by remember { mutableStateOf("CASH") } // "CASH", "UPI", "CARD", "KHATA"

    val subtotal = remember(cart) {
        cart.entries.sumOf { (item, qty) -> item.price * qty }
    }
    val discountAmt = remember(subtotal, discountPercent) {
        subtotal * (discountPercent / 100.0)
    }
    val afterDiscount = subtotal - discountAmt
    val gstAmt = remember(afterDiscount, includeGst) {
        if (includeGst) afterDiscount * 0.05 else 0.0
    }
    val totalPayable = afterDiscount + gstAmt

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top POS Bar
        Surface(
            color = RoyalMaroon,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Store Billing Counter",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Items in Bill: ${cart.values.sum()}",
                        color = ZariGold,
                        fontSize = 12.sp
                    )
                }

                if (cart.isNotEmpty()) {
                    TextButton(
                        onClick = { viewModel.clearCart() },
                        colors = ButtonDefaults.textButtonColors(contentColor = Color.White)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Clear Bill")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Clear")
                    }
                }
            }
        }

        if (cart.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = "Empty Bill",
                        modifier = Modifier.size(64.dp),
                        tint = Color.LightGray
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No items added to bill yet",
                        fontSize = 16.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Go to Store Catalog & tap '+ Add to Bill'",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(cart.entries.toList(), key = { it.key.id }) { entry ->
                    CartItemRow(
                        cloth = entry.key,
                        quantity = entry.value,
                        onAdd = { viewModel.addToCart(entry.key) },
                        onRemove = { viewModel.removeFromCart(entry.key) }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(12.dp))

                    // Customer details
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Customer Details (Optional for Khata / Receipt)",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkRoyalMaroon
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = customerName,
                                    onValueChange = { customerName = it },
                                    label = { Text("Customer Name") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    value = customerPhone,
                                    onValueChange = { customerPhone = it },
                                    label = { Text("Mobile No.") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Discounts & GST
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Bill Adjustments & Payment",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkRoyalMaroon
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Special Discount: ${discountPercent.toInt()}%",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Slider(
                                    value = discountPercent.toFloat(),
                                    onValueChange = { discountPercent = it.toDouble() },
                                    valueRange = 0f..30f,
                                    steps = 29,
                                    modifier = Modifier.width(180.dp),
                                    colors = SliderDefaults.colors(thumbColor = RoyalMaroon, activeTrackColor = RoyalMaroon)
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(
                                        checked = includeGst,
                                        onCheckedChange = { includeGst = it },
                                        colors = CheckboxDefaults.colors(checkedColor = RoyalMaroon)
                                    )
                                    Text("Apply 5% Apparel GST", fontSize = 13.sp)
                                }
                                Text("₹${gstAmt.toInt()}", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text("Payment Mode:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf("CASH", "UPI", "CARD", "KHATA").forEach { mode ->
                                    val isSel = selectedPaymentMode == mode
                                    FilterChip(
                                        selected = isSel,
                                        onClick = { selectedPaymentMode = mode },
                                        label = { Text(mode, fontSize = 11.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = RoyalMaroon,
                                            selectedLabelColor = Color.White
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Bottom Payable Bar
            Surface(
                color = Color.White,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Total Payable",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                        Text(
                            text = "₹${totalPayable.toInt()}",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = RoyalMaroon
                        )
                    }

                    Button(
                        onClick = {
                            viewModel.checkoutBill(
                                customerName = customerName,
                                customerPhone = customerPhone,
                                discountPercent = discountPercent,
                                includeGst = includeGst,
                                paymentType = selectedPaymentMode
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = RoyalMaroon),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("checkout_bill_button")
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Checkout")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Generate Bill", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // Bill Receipt Dialog
    showBillReceipt?.let { bill ->
        AlertDialog(
            onDismissRequest = { viewModel.showBillReceiptDialog.value = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, contentDescription = "Success", tint = Color(0xFF2E7D32))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Bill Generated #${bill.billNumber}", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Surface(
                        color = Color(0xFFFAF8F5),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Column {
                            Text("MAA BHAGABATI CLOTH STORE", fontWeight = FontWeight.Bold, color = RoyalMaroon, fontSize = 14.sp)
                            Text("Customer: ${bill.customerName} (${bill.customerPhone})", fontSize = 12.sp)
                            Text("Payment: ${bill.paymentType}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            Divider(modifier = Modifier.padding(vertical = 6.dp))
                            Text(bill.itemsSummary, fontSize = 12.sp, color = Color.DarkGray)
                            Divider(modifier = Modifier.padding(vertical = 6.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Subtotal:", fontSize = 12.sp)
                                Text("₹${bill.subtotal.toInt()}", fontSize = 12.sp)
                            }
                            if (bill.discountAmount > 0) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Discount (${bill.discountPercent.toInt()}%):", fontSize = 12.sp, color = Color.Red)
                                    Text("-₹${bill.discountAmount.toInt()}", fontSize = 12.sp, color = Color.Red)
                                }
                            }
                            if (bill.gstAmount > 0) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("GST 5%:", fontSize = 12.sp)
                                    Text("₹${bill.gstAmount.toInt()}", fontSize = 12.sp)
                                }
                            }
                            Divider(modifier = Modifier.padding(vertical = 6.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Total Paid:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("₹${bill.totalAmount.toInt()}", fontWeight = FontWeight.Bold, color = RoyalMaroon, fontSize = 16.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (bill.paymentType == "UPI") {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = LightZariGold),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("📲 UPI Payment QR Code", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DarkRoyalMaroon)
                                Text("Scan via Google Pay / PhonePe / Paytm", fontSize = 10.sp, color = Color.DarkGray)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("UPI ID: maabhagabati@upi", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val shareText = "Maa Bhagabati Cloth Store\nBill #${bill.billNumber}\nCustomer: ${bill.customerName}\nTotal Amount: ₹${bill.totalAmount.toInt()}\nItems:\n${bill.itemsSummary}\nThank you for shopping with us!"
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, shareText)
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Share Bill Receipt"))
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalMaroon)
                ) {
                    Icon(Icons.Default.Share, contentDescription = "Share")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Share Bill")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.showBillReceiptDialog.value = null }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
fun CartItemRow(
    cloth: ClothItem,
    quantity: Int,
    onAdd: () -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = cloth.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )
                Text(
                    text = "₹${cloth.price.toInt()} / unit (${cloth.fabricType})",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onRemove) {
                    Icon(Icons.Default.RemoveCircleOutline, contentDescription = "Decrease", tint = RoyalMaroon)
                }
                Text(
                    text = quantity.toString(),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                IconButton(onClick = onAdd) {
                    Icon(Icons.Default.AddCircleOutline, contentDescription = "Increase", tint = RoyalMaroon)
                }
            }
        }
    }
}
