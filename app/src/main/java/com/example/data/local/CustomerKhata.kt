package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "customer_khata")
data class CustomerKhata(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val phone: String,
    val totalBalance: Double = 0.0, // Positive = Customer owes store (Udhari), Negative = Advance paid
    val city: String = "Local",
    val notes: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "khata_transactions")
data class KhataTransaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val customerId: Long,
    val amount: Double,
    val type: String, // "GAVE" (Credit given to customer / + balance) or "GOT" (Payment received from customer / - balance)
    val note: String,
    val timestamp: Long = System.currentTimeMillis()
)
