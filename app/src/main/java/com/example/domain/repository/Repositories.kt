package com.example.domain.repository

import com.example.core.model.AppNotification
import com.example.core.model.CartItem
import com.example.core.model.Customer
import com.example.core.model.CustomerWithDebt
import com.example.core.model.FinancialSummary
import com.example.core.model.Money
import com.example.core.model.Product
import com.example.core.model.Settings
import com.example.core.model.Transaction
import com.example.core.model.TransactionItem
import com.example.core.model.TransactionWithDetails
import kotlinx.coroutines.flow.Flow

interface CustomerRepository {
    val allCustomers: Flow<List<Customer>>
    val activeCustomers: Flow<List<Customer>>
    val archivedCustomers: Flow<List<Customer>>
    val deletedCustomers: Flow<List<Customer>>

    fun getCustomerByIdFlow(id: Long): Flow<Customer?>
    suspend fun getCustomerById(id: Long): Customer?
    fun searchActiveCustomers(query: String): Flow<List<Customer>>
    fun searchCustomersByStatus(query: String, status: String): Flow<List<Customer>>

    suspend fun saveCustomer(customer: Customer): Result<Long>
    suspend fun archiveCustomer(id: Long): Result<Unit>
    suspend fun restoreCustomer(id: Long): Result<Unit>
    suspend fun softDeleteCustomer(id: Long): Result<Unit>
    suspend fun permanentDeleteCustomer(id: Long): Result<Unit>
}

interface ProductRepository {
    val allProducts: Flow<List<Product>>
    val activeProducts: Flow<List<Product>>
    val archivedProducts: Flow<List<Product>>
    val deletedProducts: Flow<List<Product>>

    fun getProductByIdFlow(id: Long): Flow<Product?>
    suspend fun getProductById(id: Long): Product?
    fun searchActiveProducts(query: String): Flow<List<Product>>
    fun searchProductsByStatus(query: String, status: String): Flow<List<Product>>

    suspend fun saveProduct(product: Product): Result<Long>
    suspend fun archiveProduct(id: Long): Result<Unit>
    suspend fun restoreProduct(id: Long): Result<Unit>
    suspend fun softDeleteProduct(id: Long): Result<Unit>
    suspend fun permanentDeleteProduct(id: Long): Result<Unit>
}

interface TransactionRepository {
    val allTransactions: Flow<List<Transaction>>
    val recentTransactions: Flow<List<Transaction>>
    val transactionsWithDetails: Flow<List<TransactionWithDetails>>
    val allTransactionItems: Flow<List<TransactionItem>>

    fun getTransactionsByCustomer(customerId: Long): Flow<List<Transaction>>
    fun getTransactionByIdFlow(id: Long): Flow<Transaction?>
    fun getItemsForTransaction(transactionId: Long): Flow<List<TransactionItem>>

    /**
     * Atomically creates a purchase (Credit or Cash) with snapshots of items.
     * Rollback automatically if any step fails.
     */
    suspend fun createPurchase(
        customerId: Long?,
        isCredit: Boolean,
        cartItems: List<CartItem>,
        note: String
    ): Result<Long>

    /**
     * Atomically records a debt payment with overpayment safeguard.
     */
    suspend fun recordPayment(
        customerId: Long,
        amount: Money,
        note: String
    ): Result<Long>

    /**
     * Reverses the financial effect of a transaction by setting status to CANCELLED.
     */
    suspend fun cancelTransaction(
        transactionId: Long,
        reason: String
    ): Result<Unit>

    /**
     * Restores a previously cancelled transaction back to COMPLETED status.
     */
    suspend fun restoreTransaction(
        transactionId: Long
    ): Result<Unit>

    suspend fun permanentDeleteTransaction(id: Long): Result<Unit>
}

interface SettingsRepository {
    val settingsFlow: Flow<Settings>
    suspend fun getSettings(): Settings
    suspend fun updateSettings(settings: Settings): Result<Unit>
    suspend fun setLanguage(language: String): Result<Unit>
    suspend fun setTheme(theme: String): Result<Unit>
    suspend fun updateShopInfo(storeName: String, ownerName: String, phone: String, address: String): Result<Unit>
}

interface NotificationRepository {
    val allNotifications: Flow<List<AppNotification>>
    val unreadNotificationCount: Flow<Int>
    suspend fun addNotification(notification: AppNotification): Result<Long>
    suspend fun markNotificationAsRead(id: Long): Result<Unit>
    suspend fun markAllNotificationsAsRead(): Result<Unit>
    suspend fun deleteNotification(id: Long): Result<Unit>
    suspend fun clearAllNotifications(): Result<Unit>
}

interface IShopRepository : CustomerRepository, ProductRepository, TransactionRepository, SettingsRepository, NotificationRepository {
    val activeCustomersWithDebt: Flow<List<CustomerWithDebt>>
    val financialSummary: Flow<FinancialSummary>

    suspend fun seedInitialDataIfEmpty()
    suspend fun wipeAllData(): Result<Unit>
}
