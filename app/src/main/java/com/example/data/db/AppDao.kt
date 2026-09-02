package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.core.model.CustomerStatus
import com.example.core.model.ProductStatus
import com.example.core.model.TransactionStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomerDao {
    @Query("SELECT * FROM customers ORDER BY name COLLATE LOCALIZED ASC")
    fun getAllCustomersFlow(): Flow<List<CustomerEntity>>

    @Query("SELECT * FROM customers WHERE status = :status ORDER BY name COLLATE LOCALIZED ASC")
    fun getCustomersByStatusFlow(status: String): Flow<List<CustomerEntity>>

    @Query("SELECT * FROM customers WHERE status = 'active' ORDER BY name COLLATE LOCALIZED ASC")
    fun getActiveCustomersFlow(): Flow<List<CustomerEntity>>

    @Query("SELECT * FROM customers WHERE id = :id")
    suspend fun getCustomerById(id: Long): CustomerEntity?

    @Query("SELECT * FROM customers WHERE id = :id")
    fun getCustomerByIdFlow(id: Long): Flow<CustomerEntity?>

    @Query("SELECT * FROM customers WHERE status = 'active' AND (name LIKE '%' || :query || '%' OR phone LIKE '%' || :query || '%') ORDER BY name COLLATE LOCALIZED ASC")
    fun searchActiveCustomers(query: String): Flow<List<CustomerEntity>>

    @Query("SELECT * FROM customers WHERE status = :status AND (name LIKE '%' || :query || '%' OR phone LIKE '%' || :query || '%') ORDER BY name COLLATE LOCALIZED ASC")
    fun searchCustomersByStatus(query: String, status: String): Flow<List<CustomerEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: CustomerEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(customers: List<CustomerEntity>)

    @Update
    suspend fun updateCustomer(customer: CustomerEntity)

    @Query("UPDATE customers SET status = :status, updated_at = :updatedAt, archived_at = :archivedAt, deleted_at = :deletedAt WHERE id = :id")
    suspend fun updateStatus(
        id: Long,
        status: String,
        updatedAt: Long = System.currentTimeMillis(),
        archivedAt: Long? = null,
        deletedAt: Long? = null
    )

    @Query("DELETE FROM customers WHERE id = :id")
    suspend fun deleteCustomerPermanently(id: Long)

    @Query("DELETE FROM customers")
    suspend fun deleteAllCustomers()

    @Query("SELECT * FROM customers")
    suspend fun getAllCustomersList(): List<CustomerEntity>
}

@Dao
interface ProductDao {
    @Query("SELECT * FROM products ORDER BY name COLLATE LOCALIZED ASC")
    fun getAllProductsFlow(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE status = :status ORDER BY name COLLATE LOCALIZED ASC")
    fun getProductsByStatusFlow(status: String): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE status = 'active' ORDER BY name COLLATE LOCALIZED ASC")
    fun getActiveProductsFlow(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getProductById(id: Long): ProductEntity?

    @Query("SELECT * FROM products WHERE id = :id")
    fun getProductByIdFlow(id: Long): Flow<ProductEntity?>

    @Query("SELECT * FROM products WHERE status = 'active' AND name LIKE '%' || :query || '%' ORDER BY name COLLATE LOCALIZED ASC")
    fun searchActiveProducts(query: String): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE status = :status AND name LIKE '%' || :query || '%' ORDER BY name COLLATE LOCALIZED ASC")
    fun searchProductsByStatus(query: String, status: String): Flow<List<ProductEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(products: List<ProductEntity>)

    @Update
    suspend fun updateProduct(product: ProductEntity)

    @Query("UPDATE products SET status = :status, updated_at = :updatedAt, archived_at = :archivedAt, deleted_at = :deletedAt WHERE id = :id")
    suspend fun updateStatus(
        id: Long,
        status: String,
        updatedAt: Long = System.currentTimeMillis(),
        archivedAt: Long? = null,
        deletedAt: Long? = null
    )

    @Query("DELETE FROM products WHERE id = :id")
    suspend fun deleteProductPermanently(id: Long)

    @Query("DELETE FROM products")
    suspend fun deleteAllProducts()

    @Query("SELECT * FROM products")
    suspend fun getAllProductsList(): List<ProductEntity>
}

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY created_at DESC")
    fun getAllTransactionsFlow(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE customer_id = :customerId ORDER BY created_at DESC")
    fun getTransactionsByCustomerFlow(customerId: Long): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE customer_id = :customerId ORDER BY created_at DESC")
    suspend fun getTransactionsByCustomerList(customerId: Long): List<TransactionEntity>

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getTransactionById(id: Long): TransactionEntity?

    @Query("SELECT * FROM transactions WHERE id = :id")
    fun getTransactionByIdFlow(id: Long): Flow<TransactionEntity?>

    @Query("SELECT * FROM transactions ORDER BY created_at DESC LIMIT :limit")
    fun getRecentTransactionsFlow(limit: Int = 30): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE created_at >= :startTime AND created_at < :endTime ORDER BY created_at DESC")
    fun getTransactionsBetweenFlow(startTime: Long, endTime: Long): Flow<List<TransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllTransactions(transactions: List<TransactionEntity>)

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    @Query("UPDATE transactions SET status = 'cancelled', cancelled_at = :cancelledAt, cancel_reason = :reason, updated_at = :updatedAt WHERE id = :id")
    suspend fun cancelTransaction(
        id: Long,
        cancelledAt: Long = System.currentTimeMillis(),
        reason: String?,
        updatedAt: Long = System.currentTimeMillis()
    )

    @Query("UPDATE transactions SET status = 'completed', cancelled_at = NULL, cancel_reason = NULL, updated_at = :updatedAt WHERE id = :id")
    suspend fun restoreTransaction(
        id: Long,
        updatedAt: Long = System.currentTimeMillis()
    )

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteTransactionPermanently(id: Long)

    @Query("DELETE FROM transactions")
    suspend fun deleteAllTransactions()

    @Query("SELECT * FROM transactions")
    suspend fun getAllTransactionsList(): List<TransactionEntity>

    // ----------------------------------------------------
    // Transaction Items
    // ----------------------------------------------------

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactionItem(item: TransactionItemEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactionItems(items: List<TransactionItemEntity>)

    @Query("SELECT * FROM transaction_items WHERE transaction_id = :txId")
    suspend fun getItemsForTransaction(txId: Long): List<TransactionItemEntity>

    @Query("SELECT * FROM transaction_items WHERE transaction_id = :txId")
    fun getItemsForTransactionFlow(txId: Long): Flow<List<TransactionItemEntity>>

    @Query("SELECT * FROM transaction_items")
    fun getAllTransactionItemsFlow(): Flow<List<TransactionItemEntity>>

    @Query("SELECT * FROM transaction_items")
    suspend fun getAllTransactionItemsList(): List<TransactionItemEntity>

    @Query("DELETE FROM transaction_items WHERE transaction_id = :txId")
    suspend fun deleteItemsForTransaction(txId: Long)

    @Query("DELETE FROM transaction_items")
    suspend fun deleteAllTransactionItems()
}

@Dao
interface SettingsDao {
    @Query("SELECT * FROM settings WHERE id = 1 LIMIT 1")
    fun getSettingsFlow(): Flow<SettingsEntity?>

    @Query("SELECT * FROM settings WHERE id = 1 LIMIT 1")
    suspend fun getSettings(): SettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(settings: SettingsEntity)
}

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications ORDER BY created_at DESC")
    fun getAllNotificationsFlow(): Flow<List<NotificationEntity>>

    @Query("SELECT COUNT(*) FROM notifications WHERE is_read = 0")
    fun getUnreadCountFlow(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(notifications: List<NotificationEntity>)

    @Query("UPDATE notifications SET is_read = 1 WHERE id = :id")
    suspend fun markAsRead(id: Long)

    @Query("UPDATE notifications SET is_read = 1")
    suspend fun markAllAsRead()

    @Query("DELETE FROM notifications WHERE id = :id")
    suspend fun deleteNotification(id: Long)

    @Query("DELETE FROM notifications")
    suspend fun deleteAllNotifications()
}
