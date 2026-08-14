package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cloth_items")
data class ClothItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val category: String, // "Sambalpuri", "Banarasi", "Handloom", "Bridal", "Suits", "Men's Wear", "Fabric", "Kids"
    val price: Double,
    val mrp: Double,
    val stockCount: Int,
    val fabricType: String, // "Silk", "Cotton", "Katan", "Handloom", "Georgette"
    val color: String,
    val description: String,
    val isFeatured: Boolean = false,
    val imageResName: String = "",
    val barcode: String = ""
)
