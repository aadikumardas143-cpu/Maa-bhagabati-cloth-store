package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.CustomerKhata
import com.example.ui.MainViewModel
import com.example.ui.theme.DarkRoyalMaroon
import com.example.ui.theme.LightZariGold
import com.example.ui.theme.RoyalMaroon
import com.example.ui.theme.ZariGold
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun KhataScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val customers by viewModel.khataCustomers.collectAsState()
    val totalCollectable by viewModel.totalCollectableBalance.collectAsState()

    val showAddCustomer by viewModel.showAddCustomerDialog.collectAsState()
    val selectedCustomer by viewModel.selectedKhataCustomer.collectAsState()
    val customerTransactions by viewModel.selectedCustomerTransactions.collectAsState()

    var searchQuery by remember { mutableStateOf("") }

    val filteredCustomers = remember(customers, searchQuery) {
        if (searchQuery.isBlank()) customers
        else customers.filter {
            it.name.contains(searchQuery, ignoreCase = true) || it.phone.contains(searchQuery)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Collectable Banner
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = RoyalMaroon),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Total Collectable Balance (Aapko Milenge)",
                        color = LightZariGold,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "₹${totalCollectable.toInt()}",
                        color = Color.White,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = { viewModel.showAddCustomerDialog.value = true },
                    colors = ButtonDefaults.buttonColors(containerColor = ZariGold, contentColor = Color.Black),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("add_customer_button")
                ) {
                    Icon(Icons.Default.PersonAdd, contentDescription = "Add Customer")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("+ Customer", fontWeight = FontWeight.Bold)
                }
            }
        }

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            placeholder = { Text("Search customer by name or phone...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
            shape = RoundedCornerShape(24.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Customers List
        if (filteredCustomers.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.MenuBook, contentDescription = "No Khata", modifier = Modifier.size(64.dp), tint = Color.LightGray)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("No customer accounts in Khata ledger", color = Color.Gray, fontSize = 14.sp)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(filteredCustomers, key = { it.id }) { customer ->
                    CustomerKhataRow(
                        customer = customer,
                        onClick = { viewModel.selectCustomerForDetail(customer) }
                    )
                }
            }
        }
    }

    // Add Customer Dialog
    if (showAddCustomer) {
        var name by remember { mutableStateOf("") }
        var phone by remember { mutableStateOf("") }
        var city by remember { mutableStateOf("Local") }
        var initialAmountStr by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { viewModel.showAddCustomerDialog.value = false },
            title = { Text("Add Customer to Khata", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Customer Full Name") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Mobile Phone Number") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                    )
                    OutlinedTextField(
                        value = city,
                        onValueChange = { city = it },
                        label = { Text("City / Town") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = initialAmountStr,
                        onValueChange = { initialAmountStr = it },
                        label = { Text("Initial Credit Balance (₹)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val initAmt = initialAmountStr.toDoubleOrNull() ?: 0.0
                        if (name.isNotBlank()) {
                            viewModel.addNewKhataCustomer(name, phone, city, initAmt)
                            viewModel.showAddCustomerDialog.value = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalMaroon)
                ) {
                    Text("Save Customer")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.showAddCustomerDialog.value = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Customer Detail & Transaction Dialog
    selectedCustomer?.let { customer ->
        var showAddTxType by remember { mutableStateOf<String?>(null) } // "GAVE" or "GOT"
        var txAmountStr by remember { mutableStateOf("") }
        var txNote by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { viewModel.selectCustomerForDetail(null) },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(customer.name, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = DarkRoyalMaroon)
                        Text(customer.phone, fontSize = 12.sp, color = Color.Gray)
                    }
                    Surface(
                        color = if (customer.totalBalance > 0) Color(0xFFFFEBEE) else Color(0xFFE8F5E9),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = if (customer.totalBalance > 0) "Pending: ₹${customer.totalBalance.toInt()}" else "Clear",
                            color = if (customer.totalBalance > 0) Color.Red else Color(0xFF2E7D32),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Transaction History:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = DarkRoyalMaroon)
                    Spacer(modifier = Modifier.height(6.dp))

                    if (customerTransactions.isEmpty()) {
                        Text("No transactions recorded yet", fontSize = 12.sp, color = Color.Gray)
                    } else {
                        LazyColumn(
                            modifier = Modifier.heightIn(max = 200.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(customerTransactions) { tx ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFFFAF8F5), RoundedCornerShape(6.dp))
                                        .padding(8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(tx.note.ifBlank { if (tx.type == "GAVE") "Credit Given" else "Payment Received" }, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                        Text(formatTimestamp(tx.timestamp), fontSize = 10.sp, color = Color.Gray)
                                    }
                                    Text(
                                        text = "${if (tx.type == "GAVE") "+" else "-"}₹${tx.amount.toInt()}",
                                        color = if (tx.type == "GAVE") Color.Red else Color(0xFF2E7D32),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (showAddTxType != null) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF0C2)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text(
                                    text = if (showAddTxType == "GAVE") "Record Credit Given (Udhari +)" else "Record Payment Received (Jama -)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                                OutlinedTextField(
                                    value = txAmountStr,
                                    onValueChange = { txAmountStr = it },
                                    label = { Text("Amount (₹)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    value = txNote,
                                    onValueChange = { txNote = it },
                                    label = { Text("Note (e.g. Silk Saree bill)") },
                                    singleLine = true
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    TextButton(onClick = { showAddTxType = null }) { Text("Cancel") }
                                    Button(
                                        onClick = {
                                            val amt = txAmountStr.toDoubleOrNull() ?: 0.0
                                            if (amt > 0 && showAddTxType != null) {
                                                viewModel.addKhataTransaction(customer.id, amt, showAddTxType!!, txNote)
                                                showAddTxType = null
                                                txAmountStr = ""
                                                txNote = ""
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = RoyalMaroon)
                                    ) { Text("Save") }
                                }
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { showAddTxType = "GAVE" },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("+ Give Credit", fontSize = 11.sp)
                            }
                            Button(
                                onClick = { showAddTxType = "GOT" },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("- Payment", fontSize = 11.sp)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                if (customer.totalBalance > 0 && customer.phone.isNotBlank()) {
                    Button(
                        onClick = {
                            val msg = "Namaskar ${customer.name} ji, Maa Bhagabati Cloth Store se aapka ₹${customer.totalBalance.toInt()} pending balance baki hai. Kripaya Google Pay/UPI ya dukaan pe aakar clear karein. Dhanyawad!"
                            val uri = Uri.parse("https://api.whatsapp.com/send?phone=91${customer.phone}&text=${Uri.encode(msg)}")
                            context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))
                    ) {
                        Text("📲 WhatsApp Reminder", color = Color.White)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.selectCustomerForDetail(null) }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
fun CustomerKhataRow(
    customer: CustomerKhata,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = LightZariGold,
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = customer.name.take(1).uppercase(),
                            fontWeight = FontWeight.Bold,
                            color = DarkRoyalMaroon
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(customer.name, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.Black)
                    Text("${customer.phone} • ${customer.city}", fontSize = 12.sp, color = Color.Gray)
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "₹${customer.totalBalance.toInt()}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = if (customer.totalBalance > 0) Color.Red else Color(0xFF2E7D32)
                )
                Text(
                    text = if (customer.totalBalance > 0) "Collectable" else "Settled",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

private fun formatTimestamp(ts: Long): String {
    val sdf = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
    return sdf.format(Date(ts))
}
