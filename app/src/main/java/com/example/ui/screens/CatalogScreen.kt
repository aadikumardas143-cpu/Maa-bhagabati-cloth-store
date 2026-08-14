package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.ClothItem
import com.example.ui.MainViewModel
import com.example.ui.theme.DarkRoyalMaroon
import com.example.ui.theme.LightZariGold
import com.example.ui.theme.RoyalMaroon
import com.example.ui.theme.ZariGold

@Composable
fun CatalogScreen(
    viewModel: MainViewModel,
    onNavigateToPOS: () -> Unit,
    modifier: Modifier = Modifier
) {
    val items by viewModel.clothItems.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val cart by viewModel.cartItems.collectAsState()

    var detailItem by remember { mutableStateOf<ClothItem?>(null) }

    val categories = listOf("All", "Sambalpuri", "Banarasi", "Handloom", "Bridal", "Suits", "Men's Wear", "Fabric")

    val filteredItems = remember(items, searchQuery, selectedCategory) {
        items.filter { item ->
            val matchesCategory = (selectedCategory == "All" || item.category.equals(selectedCategory, ignoreCase = true))
            val matchesSearch = queryMatches(item, searchQuery)
            matchesCategory && matchesSearch
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.setSearch(it) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .testTag("search_text_field"),
            placeholder = { Text("Search sarees, suits, fabrics, barcode...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.setSearch("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                    }
                }
            },
            shape = RoundedCornerShape(24.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = RoyalMaroon,
                unfocusedBorderColor = Color.LightGray
            )
        )

        // Hero Banner Carousel / Single Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .height(130.dp)
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, ZariGold, RoundedCornerShape(16.dp))
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_hero_saree_banner_1786637275515),
                contentDescription = "Maa Bhagabati Festive Collection Banner",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.35f))
                    .padding(12.dp),
                contentAlignment = Alignment.BottomStart
            ) {
                Column {
                    Surface(
                        color = ZariGold,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "FESTIVE WEDDING OFFER",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Royal Handloom & Silk Sarees",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Category Filter Chips
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories) { cat ->
                val isSelected = cat == selectedCategory
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.setCategory(cat) },
                    label = {
                        Text(
                            text = cat,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = RoyalMaroon,
                        selectedLabelColor = Color.White,
                        containerColor = Color.White,
                        labelColor = DarkRoyalMaroon
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        borderColor = ZariGold,
                        selectedBorderColor = ZariGold
                    )
                )
            }
        }

        // Products Grid
        if (filteredItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Checkroom,
                        contentDescription = "Empty",
                        modifier = Modifier.size(64.dp),
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No matching products found",
                        fontSize = 16.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp),
                contentPadding = PaddingValues(bottom = 80.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredItems, key = { it.id }) { cloth ->
                    val qtyInCart = cart[cloth] ?: 0
                    ProductCard(
                        cloth = cloth,
                        qtyInCart = qtyInCart,
                        onAddToCart = { viewModel.addToCart(cloth) },
                        onClick = { detailItem = cloth }
                    )
                }
            }
        }
    }

    // Detail Modal
    detailItem?.let { cloth ->
        AlertDialog(
            onDismissRequest = { detailItem = null },
            title = {
                Text(
                    text = cloth.title,
                    fontWeight = FontWeight.Bold,
                    color = DarkRoyalMaroon
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        SuggestionChip(
                            onClick = { },
                            label = { Text(cloth.category) }
                        )
                        SuggestionChip(
                            onClick = { },
                            label = { Text(cloth.fabricType) }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = cloth.description,
                        fontSize = 14.sp,
                        color = Color.DarkGray
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Price: ₹${cloth.price.toInt()}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = RoyalMaroon
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "MRP: ₹${cloth.mrp.toInt()}",
                            fontSize = 14.sp,
                            color = Color.Gray,
                            textDecoration = TextDecoration.LineThrough
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Stock: ${cloth.stockCount} units available",
                        fontSize = 12.sp,
                        color = if (cloth.stockCount > 5) Color(0xFF2E7D32) else Color.Red,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Barcode: ${cloth.barcode}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.addToCart(cloth)
                        detailItem = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalMaroon)
                ) {
                    Text("+ Add to POS Bill", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { detailItem = null }) {
                    Text("Close")
                }
            }
        )
    }
}

private fun queryMatches(item: ClothItem, query: String): Boolean {
    if (query.isBlank()) return true
    val q = query.lowercase().trim()
    return item.title.lowercase().contains(q) ||
            item.category.lowercase().contains(q) ||
            item.fabricType.lowercase().contains(q) ||
            item.color.lowercase().contains(q) ||
            item.barcode.lowercase().contains(q)
}

@Composable
fun ProductCard(
    cloth: ClothItem,
    qtyInCart: Int,
    onAddToCart: () -> Unit,
    onClick: () -> Unit
) {
    val discountPercent = remember(cloth.price, cloth.mrp) {
        if (cloth.mrp > cloth.price) {
            (((cloth.mrp - cloth.price) / cloth.mrp) * 100).toInt()
        } else 0
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("product_card_${cloth.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            // Category Tag & Discount badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = LightZariGold,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = cloth.category,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkRoyalMaroon,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                if (discountPercent > 0) {
                    Surface(
                        color = RoyalMaroon,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "$discountPercent% OFF",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = cloth.title,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = cloth.fabricType,
                fontSize = 11.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "₹${cloth.price.toInt()}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = RoyalMaroon
                )
                if (cloth.mrp > cloth.price) {
                    Text(
                        text = "₹${cloth.mrp.toInt()}",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        textDecoration = TextDecoration.LineThrough
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onAddToCart,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(vertical = 6.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (qtyInCart > 0) ZariGold else RoyalMaroon,
                    contentColor = if (qtyInCart > 0) Color.Black else Color.White
                )
            ) {
                Text(
                    text = if (qtyInCart > 0) "Added ($qtyInCart) +" else "+ Add to Bill",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
