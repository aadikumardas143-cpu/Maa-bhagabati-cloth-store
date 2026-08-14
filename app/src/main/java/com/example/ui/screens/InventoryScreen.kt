package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ClothItem
import com.example.ui.MainViewModel
import com.example.ui.theme.DarkRoyalMaroon
import com.example.ui.theme.LightZariGold
import com.example.ui.theme.RoyalMaroon
import com.example.ui.theme.ZariGold

@Composable
fun InventoryScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val items by viewModel.clothItems.collectAsState()
    val showAddDialog by viewModel.showAddProductDialog.collectAsState()

    val totalItemsCount = items.sumOf { it.stockCount }
    val totalStockValuation = items.sumOf { it.price * it.stockCount }
    val lowStockCount = items.count { it.stockCount <= 5 }

    var searchQuery by remember { mutableStateOf("") }

    val filteredItems = remember(items, searchQuery) {
        if (searchQuery.isBlank()) items
        else items.filter { it.title.contains(searchQuery, ignoreCase = true) || it.barcode.contains(searchQuery) }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Stock Header Summary
        Surface(
            color = RoyalMaroon,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Inventory & Stock Control", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text("Total Stock Valuation: ₹${totalStockValuation.toInt()}", color = ZariGold, fontSize = 12.sp)
                    }

                    Button(
                        onClick = { viewModel.showAddProductDialog.value = true },
                        colors = ButtonDefaults.buttonColors(containerColor = ZariGold, contentColor = Color.Black),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("add_product_button")
                    ) {
                        Icon(Icons.Default.AddBox, contentDescription = "Add Item")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("+ Stock", fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        color = Color.White.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Total Units", color = Color.White, fontSize = 10.sp)
                            Text("$totalItemsCount", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Surface(
                        color = if (lowStockCount > 0) Color(0xFFFFCDD2) else Color.White.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Low Stock Alert", color = if (lowStockCount > 0) Color.Red else Color.White, fontSize = 10.sp)
                            Text("$lowStockCount items", color = if (lowStockCount > 0) Color.Red else Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Search
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            placeholder = { Text("Search stock by title or barcode...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
            shape = RoundedCornerShape(24.dp),
            singleLine = true
        )

        // Stock List
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            items(filteredItems, key = { it.id }) { item ->
                InventoryItemRow(
                    item = item,
                    onUpdateStock = { newQty -> viewModel.updateStock(item, newQty) }
                )
            }
        }
    }

    // Add Product Dialog
    if (showAddDialog) {
        var title by remember { mutableStateOf("") }
        var category by remember { mutableStateOf("Sambalpuri") }
        var priceStr by remember { mutableStateOf("") }
        var mrpStr by remember { mutableStateOf("") }
        var stockStr by remember { mutableStateOf("") }
        var fabric by remember { mutableStateOf("Silk") }
        var color by remember { mutableStateOf("Red") }
        var description by remember { mutableStateOf("") }
        var barcode by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { viewModel.showAddProductDialog.value = false },
            title = { Text("Add New Product to Inventory", fontWeight = FontWeight.Bold) },
            text = {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Product Title") })
                    }
                    item {
                        OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Category (e.g. Sambalpuri, Banarasi)") })
                    }
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(value = priceStr, onValueChange = { priceStr = it }, label = { Text("Price (₹)") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                            OutlinedTextField(value = mrpStr, onValueChange = { mrpStr = it }, label = { Text("MRP (₹)") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                        }
                    }
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(value = stockStr, onValueChange = { stockStr = it }, label = { Text("Stock Quantity") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                            OutlinedTextField(value = fabric, onValueChange = { fabric = it }, label = { Text("Fabric Type") }, modifier = Modifier.weight(1f))
                        }
                    }
                    item {
                        OutlinedTextField(value = barcode, onValueChange = { barcode = it }, label = { Text("Barcode / SKU") })
                    }
                    item {
                        OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Short Description") })
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val p = priceStr.toDoubleOrNull() ?: 0.0
                        val mrp = mrpStr.toDoubleOrNull() ?: p
                        val st = stockStr.toIntOrNull() ?: 1
                        if (title.isNotBlank()) {
                            viewModel.addClothItem(title, category, p, mrp, st, fabric, color, description, barcode)
                            viewModel.showAddProductDialog.value = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalMaroon)
                ) {
                    Text("Save Product")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.showAddProductDialog.value = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun InventoryItemRow(
    item: ClothItem,
    onUpdateStock: (Int) -> Unit
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
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(item.title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color.Black)
                Text("${item.category} • ${item.fabricType} • ₹${item.price.toInt()}", fontSize = 12.sp, color = Color.Gray)
                Text("SKU: ${item.barcode}", fontSize = 11.sp, color = Color.LightGray)
            }

            Column(horizontalAlignment = Alignment.End) {
                Surface(
                    color = if (item.stockCount <= 5) Color(0xFFFFEBEE) else LightZariGold,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "Stock: ${item.stockCount}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (item.stockCount <= 5) Color.Red else DarkRoyalMaroon,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { if (item.stockCount > 0) onUpdateStock(item.stockCount - 1) }) {
                        Icon(Icons.Default.Remove, contentDescription = "Minus Stock", tint = Color.Gray)
                    }
                    IconButton(onClick = { onUpdateStock(item.stockCount + 1) }) {
                        Icon(Icons.Default.Add, contentDescription = "Plus Stock", tint = RoyalMaroon)
                    }
                }
            }
        }
    }
}
