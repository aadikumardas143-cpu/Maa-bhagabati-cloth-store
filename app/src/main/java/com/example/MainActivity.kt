package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.ui.MainViewModel
import com.example.ui.StoreTab
import com.example.ui.components.StoreHeader
import com.example.ui.screens.CatalogScreen
import com.example.ui.screens.InventoryScreen
import com.example.ui.screens.KhataScreen
import com.example.ui.screens.POSBillingScreen
import com.example.ui.screens.WebStoreScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                StoreApp(viewModel)
            }
        }
    }
}

@Composable
fun StoreApp(viewModel: MainViewModel) {
    val currentTab by viewModel.currentTab.collectAsState()
    val cart by viewModel.cartItems.collectAsState()
    val cartCount = cart.values.sum()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = currentTab == StoreTab.CATALOG,
                    onClick = { viewModel.selectTab(StoreTab.CATALOG) },
                    icon = { Icon(Icons.Default.Checkroom, contentDescription = "Catalog") },
                    label = { Text("Catalog") }
                )
                NavigationBarItem(
                    selected = currentTab == StoreTab.POS_BILLING,
                    onClick = { viewModel.selectTab(StoreTab.POS_BILLING) },
                    icon = { Icon(Icons.Default.ReceiptLong, contentDescription = "POS") },
                    label = { Text("POS") }
                )
                NavigationBarItem(
                    selected = currentTab == StoreTab.KHATA_LEDGER,
                    onClick = { viewModel.selectTab(StoreTab.KHATA_LEDGER) },
                    icon = { Icon(Icons.Default.MenuBook, contentDescription = "Khata") },
                    label = { Text("Khata") }
                )
                NavigationBarItem(
                    selected = currentTab == StoreTab.INVENTORY,
                    onClick = { viewModel.selectTab(StoreTab.INVENTORY) },
                    icon = { Icon(Icons.Default.Inventory, contentDescription = "Inventory") },
                    label = { Text("Inventory") }
                )
                NavigationBarItem(
                    selected = currentTab == StoreTab.WEB_STORE,
                    onClick = { viewModel.selectTab(StoreTab.WEB_STORE) },
                    icon = { Icon(Icons.Default.Language, contentDescription = "Web") },
                    label = { Text("Web Store") }
                )
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            if (currentTab != StoreTab.WEB_STORE) {
                StoreHeader(
                    cartCount = cartCount,
                    onCartClick = { viewModel.selectTab(StoreTab.POS_BILLING) }
                )
            }
            
            when (currentTab) {
                StoreTab.CATALOG -> CatalogScreen(
                    viewModel = viewModel,
                    onNavigateToPOS = { viewModel.selectTab(StoreTab.POS_BILLING) }
                )
                StoreTab.POS_BILLING -> POSBillingScreen(viewModel = viewModel)
                StoreTab.KHATA_LEDGER -> KhataScreen(viewModel = viewModel)
                StoreTab.INVENTORY -> InventoryScreen(viewModel = viewModel)
                StoreTab.WEB_STORE -> WebStoreScreen()
            }
        }
    }
}
