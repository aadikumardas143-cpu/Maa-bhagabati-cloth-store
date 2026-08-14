package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "store_bills")
data class StoreBill(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val billNumber: String,
    val customerName: String,
    val customerPhone: String,
    val subtotal: Double,
    val discountPercent: Double,
    val discountAmount: Double,
    val gstAmount: Double,
    val totalAmount: Double,
    val paymentType: String, // "CASH", "UPI", "CARD", "KHATA"
    val itemsSummary: String, // Text listing item titles and qty
    val timestamp: Long = System.currentTimeMillis()
)
