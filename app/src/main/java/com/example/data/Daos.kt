package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PurchaseDao {
    @Query("SELECT * FROM purchases ORDER BY id DESC")
    fun getAllPurchasesFlow(): Flow<List<Purchase>>

    @Query("SELECT * FROM purchases")
    suspend fun getAllPurchases(): List<Purchase>

    @Query("SELECT * FROM purchases WHERE id = :id LIMIT 1")
    suspend fun getPurchaseById(id: Int): Purchase?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPurchase(purchase: Purchase): Long

    @Update
    suspend fun updatePurchase(purchase: Purchase)

    @Delete
    suspend fun deletePurchase(purchase: Purchase)

    @Query("DELETE FROM purchases WHERE id = :id")
    suspend fun deletePurchaseById(id: Int)

    @Query("DELETE FROM purchases")
    suspend fun deleteAllPurchases()
}

@Dao
interface SaleDao {
    @Query("SELECT * FROM sales ORDER BY id DESC")
    fun getAllSalesFlow(): Flow<List<Sale>>

    @Query("SELECT * FROM sales")
    suspend fun getAllSales(): List<Sale>

    @Query("SELECT * FROM sales WHERE purchaseId = :purchaseId ORDER BY id DESC")
    fun getSalesForPurchaseFlow(purchaseId: Int): Flow<List<Sale>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSale(sale: Sale): Long

    @Update
    suspend fun updateSale(sale: Sale)

    @Delete
    suspend fun deleteSale(sale: Sale)

    @Query("DELETE FROM sales WHERE id = :id")
    suspend fun deleteSaleById(id: Int)

    @Query("DELETE FROM sales")
    suspend fun deleteAllSales()
}

@Dao
interface CustomerDao {
    @Query("SELECT * FROM customers ORDER BY name ASC")
    fun getAllCustomersFlow(): Flow<List<Customer>>

    @Query("SELECT * FROM customers WHERE id = :id LIMIT 1")
    suspend fun getCustomerById(id: Int): Customer?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: Customer): Long

    @Update
    suspend fun updateCustomer(customer: Customer)

    @Delete
    suspend fun deleteCustomer(customer: Customer)

    @Query("DELETE FROM customers")
    suspend fun deleteAllCustomers()
}
