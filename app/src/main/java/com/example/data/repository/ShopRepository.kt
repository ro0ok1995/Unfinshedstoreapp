package com.example.data.repository

import androidx.room.withTransaction
import com.example.core.business.DebtEngine
import com.example.core.model.AppLanguageCode
import com.example.core.model.AppNotification
import com.example.core.model.AppThemeMode
import com.example.core.model.CartItem
import com.example.core.model.Customer
import com.example.core.model.CustomerStatus
import com.example.core.model.CustomerWithDebt
import com.example.core.model.FinancialSummary
import com.example.core.model.Money
import com.example.core.model.Product
import com.example.core.model.ProductStatus
import com.example.core.model.Settings
import com.example.core.model.Transaction
import com.example.core.model.TransactionItem
import com.example.core.model.TransactionStatus
import com.example.core.model.TransactionType
import com.example.core.model.TransactionWithDetails
import com.example.core.validation.CustomerValidator
import com.example.core.validation.ProductValidator
import com.example.core.validation.TransactionValidator
import com.example.core.validation.ValidationResult
import com.example.data.db.AppDatabase
import com.example.data.db.NotificationEntity
import com.example.data.db.SettingsEntity
import com.example.data.db.TransactionItemEntity
import com.example.data.db.toDomain
import com.example.data.db.toEntity
import com.example.domain.repository.IShopRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class ShopRepository(private val database: AppDatabase) : IShopRepository {
    private val customerDao = database.customerDao()
    private val productDao = database.productDao()
    private val transactionDao = database.transactionDao()
    private val settingsDao = database.settingsDao()
    private val notificationDao = database.notificationDao()

    // ==========================================
    // CUSTOMERS
    // ==========================================

    override val allCustomers: Flow<List<Customer>> =
        customerDao.getAllCustomersFlow().map { list -> list.map { it.toDomain() } }

    override val activeCustomers: Flow<List<Customer>> =
        customerDao.getActiveCustomersFlow().map { list -> list.map { it.toDomain() } }

    override val archivedCustomers: Flow<List<Customer>> =
        customerDao.getCustomersByStatusFlow(CustomerStatus.ARCHIVED).map { list -> list.map { it.toDomain() } }

    override val deletedCustomers: Flow<List<Customer>> =
        customerDao.getCustomersByStatusFlow(CustomerStatus.DELETED).map { list -> list.map { it.toDomain() } }

    override fun getCustomerByIdFlow(id: Long): Flow<Customer?> =
        customerDao.getCustomerByIdFlow(id).map { it?.toDomain() }

    override suspend fun getCustomerById(id: Long): Customer? =
        customerDao.getCustomerById(id)?.toDomain()

    override fun searchActiveCustomers(query: String): Flow<List<Customer>> =
        customerDao.searchActiveCustomers(query.trim()).map { list -> list.map { it.toDomain() } }

    override fun searchCustomersByStatus(query: String, status: String): Flow<List<Customer>> =
        customerDao.searchCustomersByStatus(query.trim(), status).map { list -> list.map { it.toDomain() } }

    override suspend fun saveCustomer(customer: Customer): Result<Long> {
        val validation = CustomerValidator.validate(customer)
        if (!validation.isValid) {
            val errorMsg = (validation as ValidationResult.Error).defaultMessage
            return Result.failure(IllegalArgumentException(errorMsg))
        }

        val now = System.currentTimeMillis()
        val entity = if (customer.id == 0L) {
            customer.copy(createdAt = now, updatedAt = now).toEntity()
        } else {
            customer.copy(updatedAt = now).toEntity()
        }

        val id = customerDao.insertCustomer(entity)
        if (customer.id == 0L) {
            notificationDao.insertNotification(
                NotificationEntity(
                    title = "زبون جديد",
                    message = "تمت إضافة الزبون ${customer.name} إلى قائمة الحسابات",
                    type = "customer",
                    customerId = id,
                    isRead = false,
                    createdAt = now
                )
            )
        }
        return Result.success(if (customer.id == 0L) id else customer.id)
    }

    override suspend fun archiveCustomer(id: Long): Result<Unit> {
        customerDao.updateStatus(
            id = id,
            status = CustomerStatus.ARCHIVED,
            archivedAt = System.currentTimeMillis()
        )
        return Result.success(Unit)
    }

    override suspend fun restoreCustomer(id: Long): Result<Unit> {
        customerDao.updateStatus(
            id = id,
            status = CustomerStatus.ACTIVE,
            archivedAt = null,
            deletedAt = null
        )
        return Result.success(Unit)
    }

    override suspend fun softDeleteCustomer(id: Long): Result<Unit> {
        customerDao.updateStatus(
            id = id,
            status = CustomerStatus.DELETED,
            deletedAt = System.currentTimeMillis()
        )
        return Result.success(Unit)
    }

    override suspend fun permanentDeleteCustomer(id: Long): Result<Unit> {
        customerDao.deleteCustomerPermanently(id)
        return Result.success(Unit)
    }

    // ==========================================
    // PRODUCTS
    // ==========================================

    override val allProducts: Flow<List<Product>> =
        productDao.getAllProductsFlow().map { list -> list.map { it.toDomain() } }

    override val activeProducts: Flow<List<Product>> =
        productDao.getActiveProductsFlow().map { list -> list.map { it.toDomain() } }

    override val archivedProducts: Flow<List<Product>> =
        productDao.getProductsByStatusFlow(ProductStatus.ARCHIVED).map { list -> list.map { it.toDomain() } }

    override val deletedProducts: Flow<List<Product>> =
        productDao.getProductsByStatusFlow(ProductStatus.DELETED).map { list -> list.map { it.toDomain() } }

    override fun getProductByIdFlow(id: Long): Flow<Product?> =
        productDao.getProductByIdFlow(id).map { it?.toDomain() }

    override suspend fun getProductById(id: Long): Product? =
        productDao.getProductById(id)?.toDomain()

    override fun searchActiveProducts(query: String): Flow<List<Product>> =
        productDao.searchActiveProducts(query.trim()).map { list -> list.map { it.toDomain() } }

    override fun searchProductsByStatus(query: String, status: String): Flow<List<Product>> =
        productDao.searchProductsByStatus(query.trim(), status).map { list -> list.map { it.toDomain() } }

    override suspend fun saveProduct(product: Product): Result<Long> {
        val validation = ProductValidator.validate(product)
        if (!validation.isValid) {
            val errorMsg = (validation as ValidationResult.Error).defaultMessage
            return Result.failure(IllegalArgumentException(errorMsg))
        }

        val now = System.currentTimeMillis()
        val entity = if (product.id == 0L) {
            product.copy(createdAt = now, updatedAt = now).toEntity()
        } else {
            product.copy(updatedAt = now).toEntity()
        }

        val id = productDao.insertProduct(entity)
        return Result.success(if (product.id == 0L) id else product.id)
    }

    override suspend fun archiveProduct(id: Long): Result<Unit> {
        productDao.updateStatus(
            id = id,
            status = ProductStatus.ARCHIVED,
            archivedAt = System.currentTimeMillis()
        )
        return Result.success(Unit)
    }

    override suspend fun restoreProduct(id: Long): Result<Unit> {
        productDao.updateStatus(
            id = id,
            status = ProductStatus.ACTIVE,
            archivedAt = null,
            deletedAt = null
        )
        return Result.success(Unit)
    }

    override suspend fun softDeleteProduct(id: Long): Result<Unit> {
        productDao.updateStatus(
            id = id,
            status = ProductStatus.DELETED,
            deletedAt = System.currentTimeMillis()
        )
        return Result.success(Unit)
    }

    override suspend fun permanentDeleteProduct(id: Long): Result<Unit> {
        productDao.deleteProductPermanently(id)
        return Result.success(Unit)
    }

    // ==========================================
    // TRANSACTIONS
    // ==========================================

    override val allTransactions: Flow<List<Transaction>> =
        transactionDao.getAllTransactionsFlow().map { list -> list.map { it.toDomain() } }

    override val recentTransactions: Flow<List<Transaction>> =
        transactionDao.getRecentTransactionsFlow(30).map { list -> list.map { it.toDomain() } }

    override val allTransactionItems: Flow<List<TransactionItem>> =
        transactionDao.getAllTransactionItemsFlow().map { list -> list.map { it.toDomain() } }

    override fun getTransactionsByCustomer(customerId: Long): Flow<List<Transaction>> =
        transactionDao.getTransactionsByCustomerFlow(customerId).map { list -> list.map { it.toDomain() } }

    override fun getTransactionByIdFlow(id: Long): Flow<Transaction?> =
        transactionDao.getTransactionByIdFlow(id).map { it?.toDomain() }

    override fun getItemsForTransaction(transactionId: Long): Flow<List<TransactionItem>> =
        transactionDao.getItemsForTransactionFlow(transactionId).map { list -> list.map { it.toDomain() } }

    override val transactionsWithDetails: Flow<List<TransactionWithDetails>> = combine(
        allTransactions,
        allCustomers,
        allTransactionItems
    ) { transactions, customers, items ->
        val custMap = customers.associateBy { it.id }
        val itemMap = items.groupBy { it.transactionId }

        transactions.map { tx ->
            TransactionWithDetails(
                transaction = tx,
                customer = tx.customerId?.let { custMap[it] },
                items = itemMap[tx.id] ?: emptyList()
            )
        }
    }

    override suspend fun createPurchase(
        customerId: Long?,
        isCredit: Boolean,
        cartItems: List<CartItem>,
        note: String,
        paidAmount: Money
    ): Result<Long> = runCatching {
        val totalMinor = cartItems.fold(0L) { acc, item -> acc + item.subtotal.minorUnits }
        val totalMoney = Money.fromMinorUnits(totalMinor)

        // Determine if effectively credit or cash
        val isEffectiveCredit = isCredit && (paidAmount < totalMoney)
        var customerIsActive = true
        var customerName = ""

        if (isEffectiveCredit || (isCredit && customerId != null)) {
            if (customerId == null) {
                throw IllegalArgumentException("A customer must be selected for credit purchases.")
            }
            val customer = customerDao.getCustomerById(customerId)
                ?: throw IllegalStateException("Customer with ID $customerId does not exist.")
            if (customer.status != CustomerStatus.ACTIVE) {
                throw IllegalStateException("Cannot create credit purchase for archived or deleted customer.")
            }
            customerIsActive = customer.status == CustomerStatus.ACTIVE
            customerName = customer.name
        }

        val isPartial = isEffectiveCredit && paidAmount.isPositive()

        val validation = when {
            isPartial -> TransactionValidator.validatePartialPurchase(
                customerId = customerId,
                customerIsActive = customerIsActive,
                cartItems = cartItems,
                paidAmount = paidAmount,
                totalAmount = totalMoney
            )
            isEffectiveCredit -> TransactionValidator.validateCreditPurchase(
                customerId = customerId,
                customerIsActive = customerIsActive,
                cartItems = cartItems
            )
            else -> TransactionValidator.validateCashPurchase(cartItems)
        }

        if (!validation.isValid) {
            val errorMsg = (validation as ValidationResult.Error).defaultMessage
            throw IllegalArgumentException(errorMsg)
        }

        val now = System.currentTimeMillis()

        // Execute atomically in Room transaction
        database.withTransaction {
            if (!isEffectiveCredit) {
                // Full Cash Purchase
                val txEntity = com.example.data.db.TransactionEntity(
                    customerId = customerId,
                    type = TransactionType.CASH_PURCHASE,
                    totalAmount = totalMinor,
                    status = TransactionStatus.COMPLETED,
                    note = note.trim(),
                    createdAt = now,
                    updatedAt = now
                )

                val txId = transactionDao.insertTransaction(txEntity)

                val itemEntities = cartItems.map { item ->
                    TransactionItemEntity(
                        transactionId = txId,
                        productId = item.productId,
                        productNameSnapshot = item.name,
                        unitPrice = item.unitPrice.minorUnits,
                        quantity = item.quantity,
                        subtotal = item.subtotal.minorUnits
                    )
                }
                transactionDao.insertTransactionItems(itemEntities)

                val formattedTotal = totalMoney.format()
                notificationDao.insertNotification(
                    NotificationEntity(
                        title = "فاتورة شراء نقدي",
                        message = if (customerName.isNotBlank()) "تم تسجيل عملية شراء نقدية بقيمة $formattedTotal للزبون $customerName" else "تم تسجيل عملية شراء نقدية بقيمة $formattedTotal",
                        type = "cash_purchase",
                        customerId = customerId,
                        isRead = false,
                        createdAt = now
                    )
                )

                txId
            } else if (!isPartial) {
                // Full Debt Purchase
                val txEntity = com.example.data.db.TransactionEntity(
                    customerId = customerId,
                    type = TransactionType.CREDIT_PURCHASE,
                    totalAmount = totalMinor,
                    status = TransactionStatus.COMPLETED,
                    note = note.trim(),
                    createdAt = now,
                    updatedAt = now
                )

                val txId = transactionDao.insertTransaction(txEntity)

                val itemEntities = cartItems.map { item ->
                    TransactionItemEntity(
                        transactionId = txId,
                        productId = item.productId,
                        productNameSnapshot = item.name,
                        unitPrice = item.unitPrice.minorUnits,
                        quantity = item.quantity,
                        subtotal = item.subtotal.minorUnits
                    )
                }
                transactionDao.insertTransactionItems(itemEntities)

                val formattedTotal = totalMoney.format()
                notificationDao.insertNotification(
                    NotificationEntity(
                        title = "فاتورة شراء آجل",
                        message = "تم تسجيل مشتريات آجلة بقيمة $formattedTotal للزبون $customerName",
                        type = "purchase",
                        customerId = customerId,
                        isRead = false,
                        createdAt = now
                    )
                )

                txId
            } else {
                // Partial Purchase: Record CREDIT_PURCHASE and PAYMENT atomically
                val txEntity = com.example.data.db.TransactionEntity(
                    customerId = customerId,
                    type = TransactionType.CREDIT_PURCHASE,
                    totalAmount = totalMinor,
                    status = TransactionStatus.COMPLETED,
                    note = note.trim(),
                    createdAt = now,
                    updatedAt = now
                )

                val purchaseTxId = transactionDao.insertTransaction(txEntity)

                val itemEntities = cartItems.map { item ->
                    TransactionItemEntity(
                        transactionId = purchaseTxId,
                        productId = item.productId,
                        productNameSnapshot = item.name,
                        unitPrice = item.unitPrice.minorUnits,
                        quantity = item.quantity,
                        subtotal = item.subtotal.minorUnits
                    )
                }
                transactionDao.insertTransactionItems(itemEntities)

                // Record payment for the cash portion
                val paymentNote = if (note.trim().isNotBlank()) {
                    "دفعة نقدية مع الفاتورة #${purchaseTxId} (${note.trim()})"
                } else {
                    "دفعة نقدية مع الفاتورة #${purchaseTxId}"
                }

                val paymentEntity = com.example.data.db.TransactionEntity(
                    customerId = customerId,
                    type = TransactionType.PAYMENT,
                    totalAmount = paidAmount.minorUnits,
                    status = TransactionStatus.COMPLETED,
                    note = paymentNote,
                    createdAt = now + 1, // slight timestamp offset for ordering
                    updatedAt = now + 1
                )
                transactionDao.insertTransaction(paymentEntity)

                val remainingMinor = totalMinor - paidAmount.minorUnits
                val remainingMoney = Money.fromMinorUnits(remainingMinor)

                notificationDao.insertNotification(
                    NotificationEntity(
                        title = "فاتورة شراء مع دفعة نقدية",
                        message = "تم تسجيل شراء بقيمة ${totalMoney.format()}، استلام نقدي ${paidAmount.format()}، والمتبقي دين ${remainingMoney.format()} للزبون $customerName",
                        type = "purchase",
                        customerId = customerId,
                        isRead = false,
                        createdAt = now
                    )
                )

                purchaseTxId
            }
        }
    }

    override suspend fun recordPayment(
        customerId: Long,
        amount: Money,
        note: String
    ): Result<Long> = runCatching {
        val customer = customerDao.getCustomerById(customerId)
            ?: throw IllegalStateException("Customer with ID $customerId does not exist.")

        if (customer.status != CustomerStatus.ACTIVE) {
            throw IllegalStateException("Cannot record payment for archived or deleted customer.")
        }

        // Compute current outstanding debt to enforce the debt ceiling rule
        val customerTxs = transactionDao.getTransactionsByCustomerList(customerId).map { it.toDomain() }
        val currentDebt = DebtEngine.calculateOutstandingDebt(customerTxs)

        val validation = TransactionValidator.validatePayment(
            customerId = customerId,
            customerIsActive = customer.status == CustomerStatus.ACTIVE,
            amount = amount,
            currentDebt = currentDebt
        )

        if (!validation.isValid) {
            val errorMsg = (validation as ValidationResult.Error).defaultMessage
            throw IllegalArgumentException(errorMsg)
        }

        val now = System.currentTimeMillis()
        val txEntity = com.example.data.db.TransactionEntity(
            customerId = customerId,
            type = TransactionType.PAYMENT,
            totalAmount = amount.minorUnits,
            status = TransactionStatus.COMPLETED,
            note = note.trim(),
            createdAt = now,
            updatedAt = now
        )

        val txId = transactionDao.insertTransaction(txEntity)
        notificationDao.insertNotification(
            NotificationEntity(
                title = "سداد دفعة نقدية",
                message = "تم تسجيل دفعة بقيمة ${amount.format()} من الزبون ${customer.name}",
                type = "payment",
                customerId = customerId,
                isRead = false,
                createdAt = now
            )
        )
        txId
    }

    override suspend fun cancelTransaction(
        transactionId: Long,
        reason: String
    ): Result<Unit> = runCatching {
        val tx = transactionDao.getTransactionById(transactionId)
            ?: throw IllegalStateException("Transaction with ID $transactionId does not exist.")

        if (tx.status == TransactionStatus.CANCELLED) {
            throw IllegalStateException("Transaction is already cancelled.")
        }

        transactionDao.cancelTransaction(
            id = transactionId,
            cancelledAt = System.currentTimeMillis(),
            reason = reason.trim().ifBlank { null }
        )
    }

    override suspend fun restoreTransaction(
        transactionId: Long
    ): Result<Unit> = runCatching {
        val tx = transactionDao.getTransactionById(transactionId)
            ?: throw IllegalStateException("Transaction with ID $transactionId does not exist.")

        if (tx.status != TransactionStatus.CANCELLED) {
            throw IllegalStateException("Transaction is not cancelled.")
        }

        transactionDao.restoreTransaction(
            id = transactionId,
            updatedAt = System.currentTimeMillis()
        )
    }

    override suspend fun permanentDeleteTransaction(id: Long): Result<Unit> = runCatching {
        database.withTransaction {
            transactionDao.deleteItemsForTransaction(id)
            transactionDao.deleteTransactionPermanently(id)
        }
    }

    // ==========================================
    // SETTINGS
    // ==========================================

    override val settingsFlow: Flow<Settings> = settingsDao.getSettingsFlow().map {
        it?.toDomain() ?: Settings()
    }

    override suspend fun getSettings(): Settings {
        return settingsDao.getSettings()?.toDomain() ?: Settings()
    }

    override suspend fun updateSettings(settings: Settings): Result<Unit> = runCatching {
        settingsDao.insertOrUpdate(settings.toEntity())
    }

    override suspend fun setLanguage(language: String): Result<Unit> = runCatching {
        val current = getSettings()
        updateSettings(current.copy(language = language, updatedAt = System.currentTimeMillis()))
    }

    override suspend fun setTheme(theme: String): Result<Unit> = runCatching {
        val current = getSettings()
        updateSettings(current.copy(theme = theme, updatedAt = System.currentTimeMillis()))
    }

    override suspend fun updateShopInfo(
        storeName: String,
        ownerName: String,
        phone: String,
        address: String
    ): Result<Unit> = runCatching {
        val current = getSettings()
        updateSettings(
            current.copy(
                storeName = storeName.trim(),
                ownerName = ownerName.trim(),
                phone = phone.trim(),
                address = address.trim(),
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    // ==========================================
    // REACTIVE BUSINESS STREAMS
    // ==========================================

    override val activeCustomersWithDebt: Flow<List<CustomerWithDebt>> = combine(
        activeCustomers,
        allTransactions
    ) { customers, transactions ->
        val txsByCustomer = transactions.groupBy { it.customerId }

        customers.map { customer ->
            val custTxs = txsByCustomer[customer.id] ?: emptyList()
            val debt = DebtEngine.calculateOutstandingDebt(custTxs)
            val purchasesCount = custTxs.count {
                it.status == TransactionStatus.COMPLETED && it.type != TransactionType.PAYMENT
            }
            val lastTx = custTxs.maxByOrNull { it.createdAt }

            CustomerWithDebt(
                customer = customer,
                outstandingDebt = debt,
                totalPurchasesCount = purchasesCount,
                lastTransactionTime = lastTx?.createdAt
            )
        }.sortedWith(
            compareByDescending<CustomerWithDebt> { it.outstandingDebt.minorUnits }
                .thenBy { it.customer.name }
        )
    }

    override val financialSummary: Flow<FinancialSummary> = combine(
        activeCustomersWithDebt,
        allTransactions
    ) { customersWithDebt, transactions ->
        DebtEngine.calculateFinancialSummary(
            customersWithDebt = customersWithDebt,
            allTransactions = transactions
        )
    }

    // ==========================================
    // NOTIFICATIONS
    // ==========================================

    override val allNotifications: Flow<List<AppNotification>> =
        notificationDao.getAllNotificationsFlow().map { list -> list.map { it.toDomain() } }

    override val unreadNotificationCount: Flow<Int> =
        notificationDao.getUnreadCountFlow()

    override suspend fun addNotification(notification: AppNotification): Result<Long> = runCatching {
        notificationDao.insertNotification(notification.toEntity())
    }

    override suspend fun markNotificationAsRead(id: Long): Result<Unit> = runCatching {
        notificationDao.markAsRead(id)
    }

    override suspend fun markAllNotificationsAsRead(): Result<Unit> = runCatching {
        notificationDao.markAllAsRead()
    }

    override suspend fun deleteNotification(id: Long): Result<Unit> = runCatching {
        notificationDao.deleteNotification(id)
    }

    override suspend fun clearAllNotifications(): Result<Unit> = runCatching {
        notificationDao.deleteAllNotifications()
    }

    override suspend fun wipeAllData(): Result<Unit> = runCatching {
        database.withTransaction {
            notificationDao.deleteAllNotifications()
            transactionDao.deleteAllTransactionItems()
            transactionDao.deleteAllTransactions()
            customerDao.deleteAllCustomers()
            productDao.deleteAllProducts()
            settingsDao.insertOrUpdate(SettingsEntity())
        }
    }

    override suspend fun seedInitialDataIfEmpty() {
        val customers = customerDao.getAllCustomersList()
        if (customers.isEmpty()) {
            database.withTransaction {
                val c1 = customerDao.insertCustomer(
                    Customer(
                        name = "أحمد خليل",
                        phone = "0599123456",
                        countryCode = "+970",
                        address = "حي الرمال",
                        status = CustomerStatus.ACTIVE
                    ).toEntity()
                )
                val c2 = customerDao.insertCustomer(
                    Customer(
                        name = "محمد ناصر",
                        phone = "0598654321",
                        countryCode = "+970",
                        address = "شارع الوحدة",
                        status = CustomerStatus.ACTIVE
                    ).toEntity()
                )
                val c3 = customerDao.insertCustomer(
                    Customer(
                        name = "محمود عودة",
                        phone = "0597112233",
                        countryCode = "+970",
                        address = "البلدة القديمة",
                        status = CustomerStatus.ACTIVE
                    ).toEntity()
                )

                val p1 = productDao.insertProduct(
                    Product(
                        name = "سكر 1 كغم",
                        price = Money.fromShekels(5.0),
                        status = ProductStatus.ACTIVE
                    ).toEntity()
                )
                val p2 = productDao.insertProduct(
                    Product(
                        name = "زيت زيتون 1 لتر",
                        price = Money.fromShekels(35.0),
                        status = ProductStatus.ACTIVE
                    ).toEntity()
                )
                val p3 = productDao.insertProduct(
                    Product(
                        name = "أرز بسمتي 5 كغم",
                        price = Money.fromShekels(45.0),
                        status = ProductStatus.ACTIVE
                    ).toEntity()
                )
                val p4 = productDao.insertProduct(
                    Product(
                        name = "شاي فاخر 100 كيس",
                        price = Money.fromShekels(14.0),
                        status = ProductStatus.ACTIVE
                    ).toEntity()
                )

                // Seed sample purchases
                val tx1 = transactionDao.insertTransaction(
                    Transaction(
                        customerId = c1,
                        type = TransactionType.CREDIT_PURCHASE,
                        totalAmount = Money.fromShekels(85.0),
                        note = "فاتورة أول الشهر",
                        status = TransactionStatus.COMPLETED,
                        createdAt = System.currentTimeMillis() - 86400000L * 3
                    ).toEntity()
                )

                transactionDao.insertTransactionItems(
                    listOf(
                        TransactionItem(
                            transactionId = tx1,
                            productId = p2,
                            productNameSnapshot = "زيت زيتون 1 لتر",
                            unitPrice = Money.fromShekels(35.0),
                            quantity = 2.0,
                            subtotal = Money.fromShekels(70.0)
                        ).toEntity(),
                        TransactionItem(
                            transactionId = tx1,
                            productId = p1,
                            productNameSnapshot = "سكر 1 كغم",
                            unitPrice = Money.fromShekels(5.0),
                            quantity = 3.0,
                            subtotal = Money.fromShekels(15.0)
                        ).toEntity()
                    )
                )

                // Payment for c1
                transactionDao.insertTransaction(
                    Transaction(
                        customerId = c1,
                        type = TransactionType.PAYMENT,
                        totalAmount = Money.fromShekels(30.0),
                        note = "دفعة نقدية سداد",
                        status = TransactionStatus.COMPLETED,
                        createdAt = System.currentTimeMillis() - 86400000L
                    ).toEntity()
                )

                // Seed Notifications
                notificationDao.insertNotification(
                    NotificationEntity(
                        title = "فاتورة شراء آجل",
                        message = "تم تسجيل مشتريات آجلة بقيمة 85 ₪ للزبون أحمد خليل",
                        type = "purchase",
                        customerId = c1,
                        isRead = false,
                        createdAt = System.currentTimeMillis() - 86400000L * 3
                    )
                )
                notificationDao.insertNotification(
                    NotificationEntity(
                        title = "سداد دفعة نقدية",
                        message = "تم تسجيل دفعة بقيمة 30 ₪ من الزبون أحمد خليل",
                        type = "payment",
                        customerId = c1,
                        isRead = false,
                        createdAt = System.currentTimeMillis() - 86400000L
                    )
                )

                // Default Settings
                settingsDao.insertOrUpdate(
                    Settings(
                        storeName = "بقالة الخير والبركة",
                        ownerName = "أبو خليل",
                        phone = "0599000000",
                        address = "فلسطين - الشارع الرئيسي",
                        language = AppLanguageCode.ARABIC,
                        theme = AppThemeMode.SYSTEM
                    ).toEntity()
                )
            }
        }
    }
}
