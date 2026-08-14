package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ClothItemDao {
    @Query("SELECT * FROM cloth_items ORDER BY isFeatured DESC, id DESC")
    fun getAllClothItems(): Flow<List<ClothItem>>

    @Query("SELECT * FROM cloth_items WHERE category = :category ORDER BY id DESC")
    fun getItemsByCategory(category: String): Flow<List<ClothItem>>

    @Query("SELECT * FROM cloth_items WHERE id = :id")
    suspend fun getItemById(id: Long): ClothItem?

    @Query("SELECT * FROM cloth_items WHERE barcode = :barcode LIMIT 1")
    suspend fun getItemByBarcode(barcode: String): ClothItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: ClothItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<ClothItem>)

    @Update
    suspend fun updateItem(item: ClothItem)

    @Delete
    suspend fun deleteItem(item: ClothItem)

    @Query("SELECT COUNT(*) FROM cloth_items")
    suspend fun getItemCount(): Int
}

@Dao
interface KhataDao {
    @Query("SELECT * FROM customer_khata ORDER BY updatedAt DESC")
    fun getAllCustomers(): Flow<List<CustomerKhata>>

    @Query("SELECT * FROM customer_khata WHERE id = :id")
    suspend fun getCustomerById(id: Long): CustomerKhata?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: CustomerKhata): Long

    @Update
    suspend fun updateCustomer(customer: CustomerKhata)

    @Query("SELECT * FROM khata_transactions WHERE customerId = :customerId ORDER BY timestamp DESC")
    fun getTransactionsForCustomer(customerId: Long): Flow<List<KhataTransaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: KhataTransaction): Long

    @Query("SELECT SUM(totalBalance) FROM customer_khata WHERE totalBalance > 0")
    fun getTotalCollectableBalance(): Flow<Double?>
}

@Dao
interface BillDao {
    @Query("SELECT * FROM store_bills ORDER BY timestamp DESC")
    fun getAllBills(): Flow<List<StoreBill>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBill(bill: StoreBill): Long

    @Query("SELECT SUM(totalAmount) FROM store_bills")
    fun getTotalSales(): Flow<Double?>
}
