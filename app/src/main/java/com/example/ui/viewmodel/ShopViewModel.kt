package com.example.ui.viewmodel

import android.app.Application
import android.content.Intent
import android.net.Uri
import android.content.Context
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.business.DebtEngine
import com.example.core.model.AppLanguageCode
import com.example.core.model.AppNotification
import com.example.core.model.AppVisualThemeType
import com.example.ui.theme.AppVisualTheme
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
import com.example.core.model.TransactionStatus
import com.example.core.model.TransactionType
import com.example.core.model.TransactionWithDetails
import com.example.core.services.backup.BackupPayload
import com.example.core.services.backup.BackupService
import com.example.core.services.reports.PdfReportingService
import com.example.data.db.AppDatabase
import com.example.data.db.toDomain
import com.example.data.localization.AppLanguage
import com.example.data.repository.ShopRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.io.InputStreamReader

enum class ScreenDestination {
    HOME,
    PURCHASES,
    STATEMENTS,
    DATABASE,
    SETTINGS
}

enum class HomePeriod {
    TODAY,
    THIS_WEEK,
    THIS_MONTH,
    ALL_TIME
}

data class HomeFinancialStats(
    val totalCredit: Money = Money.ZERO,
    val totalCash: Money = Money.ZERO,
    val totalPayments: Money = Money.ZERO,
    val outstandingBalance: Money = Money.ZERO,
    val totalVolume: Money = Money.ZERO,
    val creditPercentage: Float = 0f,
    val cashPercentage: Float = 0f,
    val paymentsPercentage: Float = 0f,
    val hasTransactions: Boolean = false
)

class ShopViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getInstance(application)
    val repository = ShopRepository(database)
    val backupService = BackupService(database)
    val pdfReportingService = PdfReportingService(application)

    // Navigation State
    private val _currentDestination = MutableStateFlow(ScreenDestination.HOME)
    val currentDestination: StateFlow<ScreenDestination> = _currentDestination.asStateFlow()

    private val _selectedCustomerIdForDetails = MutableStateFlow<Long?>(null)
    val selectedCustomerIdForDetails: StateFlow<Long?> = _selectedCustomerIdForDetails.asStateFlow()

    // Language State
    private val _currentLanguage = MutableStateFlow(AppLanguage.ARABIC)
    val currentLanguage: StateFlow<AppLanguage> = _currentLanguage.asStateFlow()

    // Visual Theme State (Defaults to Black & White on first launch)
    private val themePrefs = application.getSharedPreferences("smallstore_appearance_prefs", Context.MODE_PRIVATE)
    private val _currentVisualTheme = MutableStateFlow(
        AppVisualTheme.fromCode(themePrefs.getString("app_visual_theme", AppVisualThemeType.BLACK_AND_WHITE))
    )
    val currentVisualTheme: StateFlow<AppVisualTheme> = _currentVisualTheme.asStateFlow()

    // UI Message Events (One-shot)
    private val _uiEvents = MutableSharedFlow<String>()
    val uiEvents = _uiEvents.asSharedFlow()

    // Loading State
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Search queries
    private val _customerSearchQuery = MutableStateFlow("")
    val customerSearchQuery: StateFlow<String> = _customerSearchQuery.asStateFlow()

    private val _statementSearchQuery = MutableStateFlow("")
    val statementSearchQuery: StateFlow<String> = _statementSearchQuery.asStateFlow()

    private val _productSearchQuery = MutableStateFlow("")
    val productSearchQuery: StateFlow<String> = _productSearchQuery.asStateFlow()

    // Database tab search & filters
    private val _dbCustomerSearchQuery = MutableStateFlow("")
    val dbCustomerSearchQuery: StateFlow<String> = _dbCustomerSearchQuery.asStateFlow()

    private val _dbCustomerStatusFilter = MutableStateFlow<String?>(null)
    val dbCustomerStatusFilter: StateFlow<String?> = _dbCustomerStatusFilter.asStateFlow()

    private val _dbProductSearchQuery = MutableStateFlow("")
    val dbProductSearchQuery: StateFlow<String> = _dbProductSearchQuery.asStateFlow()

    private val _dbProductStatusFilter = MutableStateFlow<String?>(null)
    val dbProductStatusFilter: StateFlow<String?> = _dbProductStatusFilter.asStateFlow()

    // Data Streams
    val notifications: StateFlow<List<AppNotification>> = repository.allNotifications.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val unreadNotificationCount: StateFlow<Int> = repository.unreadNotificationCount.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    val financialSummary: StateFlow<FinancialSummary> = repository.financialSummary.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = FinancialSummary()
    )

    val activeCustomersWithDebt: StateFlow<List<CustomerWithDebt>> = combine(
        repository.activeCustomersWithDebt,
        _customerSearchQuery
    ) { list, query ->
        if (query.isBlank()) {
            list
        } else {
            val q = query.trim()
            val queryDigits = q.filter { it.isDigit() }
            list.filter { item ->
                val nameMatch = item.customer.name.contains(q, ignoreCase = true)
                val rawPhoneMatch = item.customer.phone.contains(q, ignoreCase = true)
                val phoneDigits = item.customer.phone.filter { it.isDigit() }
                val digitsMatch = queryDigits.isNotEmpty() && (
                    phoneDigits.contains(queryDigits) ||
                    phoneDigits.removePrefix("970").removePrefix("972").contains(queryDigits.removePrefix("0")) ||
                    phoneDigits.contains(queryDigits.removePrefix("0"))
                )
                nameMatch || rawPhoneMatch || digitsMatch
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allActiveCustomers: StateFlow<List<Customer>> = repository.activeCustomers.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allDatabaseCustomers: StateFlow<List<Customer>> = combine(
        repository.allCustomers,
        _dbCustomerSearchQuery,
        _dbCustomerStatusFilter
    ) { list, query, statusFilter ->
        list.filter { customer ->
            val matchStatus = statusFilter == null || customer.status == statusFilter
            val matchSearch = query.isBlank() ||
                    customer.name.contains(query, ignoreCase = true) ||
                    customer.phone.contains(query, ignoreCase = true)
            matchStatus && matchSearch
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val activeProducts: StateFlow<List<Product>> = repository.activeProducts.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allDatabaseProducts: StateFlow<List<Product>> = combine(
        repository.allProducts,
        _dbProductSearchQuery,
        _dbProductStatusFilter
    ) { list, query, statusFilter ->
        list.filter { product ->
            val matchStatus = statusFilter == null || product.status == statusFilter
            val matchSearch = query.isBlank() || product.name.contains(query, ignoreCase = true)
            matchStatus && matchSearch
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val cancelledTransactionsWithDetails: StateFlow<List<TransactionWithDetails>> = repository.transactionsWithDetails.map { list ->
        list.filter { it.transaction.status == TransactionStatus.CANCELLED }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val customerDebtsMap: StateFlow<Map<Long, Money>> = repository.transactionsWithDetails.map { list ->
        val map = mutableMapOf<Long, Long>()
        list.forEach { item ->
            val tx = item.transaction
            val cId = tx.customerId
            if (cId != null && tx.isCompleted) {
                val current = map.getOrDefault(cId, 0L)
                if (tx.isCreditPurchase) {
                    map[cId] = current + tx.totalAmount.minorUnits
                } else if (tx.isPayment) {
                    map[cId] = current - tx.totalAmount.minorUnits
                }
            }
        }
        map.mapValues { (_, minor) ->
            if (minor > 0L) Money.fromMinorUnits(minor) else Money.ZERO
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyMap()
    )

    // ==========================================
    // HOME SCREEN STATE & FLOWS (Phase 2)
    // ==========================================
    private val _selectedHomeCustomer = MutableStateFlow<Customer?>(null)
    val selectedHomeCustomer: StateFlow<Customer?> = _selectedHomeCustomer.asStateFlow()

    private val _homePeriod = MutableStateFlow(HomePeriod.TODAY)
    val homePeriod: StateFlow<HomePeriod> = _homePeriod.asStateFlow()

    private val _selectedSettingsTab = MutableStateFlow("ACCOUNT")
    val selectedSettingsTab: StateFlow<String> = _selectedSettingsTab.asStateFlow()

    private fun getPeriodStartTime(period: HomePeriod): Long {
        val cal = java.util.Calendar.getInstance()
        return when (period) {
            HomePeriod.TODAY -> {
                cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
                cal.set(java.util.Calendar.MINUTE, 0)
                cal.set(java.util.Calendar.SECOND, 0)
                cal.set(java.util.Calendar.MILLISECOND, 0)
                cal.timeInMillis
            }
            HomePeriod.THIS_WEEK -> {
                cal.set(java.util.Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
                cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
                cal.set(java.util.Calendar.MINUTE, 0)
                cal.set(java.util.Calendar.SECOND, 0)
                cal.set(java.util.Calendar.MILLISECOND, 0)
                cal.timeInMillis
            }
            HomePeriod.THIS_MONTH -> {
                cal.set(java.util.Calendar.DAY_OF_MONTH, 1)
                cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
                cal.set(java.util.Calendar.MINUTE, 0)
                cal.set(java.util.Calendar.SECOND, 0)
                cal.set(java.util.Calendar.MILLISECOND, 0)
                cal.timeInMillis
            }
            HomePeriod.ALL_TIME -> 0L
        }
    }

    val homeFilteredTransactions: StateFlow<List<TransactionWithDetails>> = combine(
        repository.transactionsWithDetails,
        _selectedHomeCustomer,
        _homePeriod
    ) { transactions, selectedCustomer, period ->
        val startTime = getPeriodStartTime(period)
        transactions.filter { item ->
            val matchesCustomer = if (selectedCustomer != null) {
                item.transaction.customerId == selectedCustomer.id || item.customer?.id == selectedCustomer.id
            } else {
                true
            }
            val matchesPeriod = item.transaction.createdAt >= startTime
            matchesCustomer && matchesPeriod
        }.sortedByDescending { it.transaction.createdAt }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val homeFinancialStats: StateFlow<HomeFinancialStats> = combine(
        homeFilteredTransactions,
        _selectedHomeCustomer,
        customerDebtsMap,
        financialSummary
    ) { txList, selectedCustomer, debtsMap, summary ->
        var creditMinor = 0L
        var cashMinor = 0L
        var paymentsMinor = 0L
        var completedCount = 0

        txList.forEach { item ->
            val tx = item.transaction
            if (tx.isCompleted) {
                completedCount++
                if (tx.isCreditPurchase) {
                    creditMinor += tx.totalAmount.minorUnits
                } else if (tx.isCashPurchase) {
                    cashMinor += tx.totalAmount.minorUnits
                } else if (tx.isPayment) {
                    paymentsMinor += tx.totalAmount.minorUnits
                }
            }
        }

        val totalVolumeMinor = creditMinor + cashMinor + paymentsMinor
        val outstanding = if (selectedCustomer != null) {
            debtsMap[selectedCustomer.id] ?: Money.ZERO
        } else {
            summary.totalOutstandingDebt
        }

        val creditPct = if (totalVolumeMinor > 0L) creditMinor.toFloat() / totalVolumeMinor.toFloat() else 0f
        val cashPct = if (totalVolumeMinor > 0L) cashMinor.toFloat() / totalVolumeMinor.toFloat() else 0f
        val paymentsPct = if (totalVolumeMinor > 0L) paymentsMinor.toFloat() / totalVolumeMinor.toFloat() else 0f

        HomeFinancialStats(
            totalCredit = Money.fromMinorUnits(creditMinor),
            totalCash = Money.fromMinorUnits(cashMinor),
            totalPayments = Money.fromMinorUnits(paymentsMinor),
            outstandingBalance = outstanding,
            totalVolume = Money.fromMinorUnits(totalVolumeMinor),
            creditPercentage = creditPct,
            cashPercentage = cashPct,
            paymentsPercentage = paymentsPct,
            hasTransactions = completedCount > 0
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeFinancialStats()
    )

    val homeLatestActivities: StateFlow<List<TransactionWithDetails>> = homeFilteredTransactions.map { list ->
        list.take(15)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun selectHomeCustomer(customer: Customer) {
        _selectedHomeCustomer.value = customer
    }

    fun clearHomeCustomer() {
        _selectedHomeCustomer.value = null
    }

    fun setHomePeriod(period: HomePeriod) {
        _homePeriod.value = period
    }

    fun openSettingsWithTabName(tabName: String) {
        _selectedSettingsTab.value = tabName
        _selectedCustomerIdForDetails.value = null
        _currentDestination.value = ScreenDestination.SETTINGS
    }

    // Statements Filters & Selection
    private val _selectedStatementCustomer = MutableStateFlow<Customer?>(null)
    val selectedStatementCustomer: StateFlow<Customer?> = _selectedStatementCustomer.asStateFlow()

    val selectedTxTypeFilter = MutableStateFlow<String?>(null)
    val selectedTxStatusFilter = MutableStateFlow<String?>(null)

    val filteredTransactionsWithDetails: StateFlow<List<TransactionWithDetails>> = combine(
        repository.transactionsWithDetails,
        _selectedStatementCustomer,
        selectedTxTypeFilter
    ) { transactions, selectedCustomer, typeFilter ->
        val list = transactions.filter { item ->
            val matchCustomer = if (selectedCustomer != null) {
                item.transaction.customerId == selectedCustomer.id || item.customer?.id == selectedCustomer.id
            } else {
                true
            }
            val matchType = typeFilter == null || item.transaction.type == typeFilter
            matchCustomer && matchType
        }
        list.sortedByDescending { it.transaction.createdAt }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Financial calculations for Statements Screen (Mode A vs Mode B)
    data class StatementFinancialMetrics(
        val outstandingDebt: Money = Money.ZERO,
        val totalPurchases: Money = Money.ZERO,
        val totalPayments: Money = Money.ZERO,
        val completedCreditPurchasesCount: Int = 0,
        val completedPaymentsCount: Int = 0,
        val totalTransactionsCount: Int = 0
    )

    val statementFinancialMetrics: StateFlow<StatementFinancialMetrics> = combine(
        repository.transactionsWithDetails,
        _selectedStatementCustomer
    ) { transactions, selectedCustomer ->
        val relevantTransactions = if (selectedCustomer != null) {
            transactions.filter { it.transaction.customerId == selectedCustomer.id || it.customer?.id == selectedCustomer.id }
        } else {
            transactions
        }

        var totalCreditMinor = 0L
        var totalPaymentsMinor = 0L
        var creditCount = 0
        var paymentCount = 0

        relevantTransactions.forEach { item ->
            val tx = item.transaction
            if (tx.isCompleted) {
                if (tx.isCreditPurchase) {
                    totalCreditMinor += tx.totalAmount.minorUnits
                    creditCount++
                } else if (tx.isPayment) {
                    totalPaymentsMinor += tx.totalAmount.minorUnits
                    paymentCount++
                }
            }
        }

        val rawDebtMinor = totalCreditMinor - totalPaymentsMinor
        val netDebtMinor = if (rawDebtMinor > 0L) rawDebtMinor else 0L

        StatementFinancialMetrics(
            outstandingDebt = Money.fromMinorUnits(netDebtMinor),
            totalPurchases = Money.fromMinorUnits(totalCreditMinor),
            totalPayments = Money.fromMinorUnits(totalPaymentsMinor),
            completedCreditPurchasesCount = creditCount,
            completedPaymentsCount = paymentCount,
            totalTransactionsCount = relevantTransactions.size
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = StatementFinancialMetrics()
    )

    // Cart / New Purchase State
    private val _selectedPurchaseCustomer = MutableStateFlow<Customer?>(null)
    val selectedPurchaseCustomer: StateFlow<Customer?> = _selectedPurchaseCustomer.asStateFlow()

    private val _isCreditPurchase = MutableStateFlow(true)
    val isCreditPurchase: StateFlow<Boolean> = _isCreditPurchase.asStateFlow()

    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    private val _purchaseNotes = MutableStateFlow("")
    val purchaseNotes: StateFlow<String> = _purchaseNotes.asStateFlow()

    // Settings
    val shopSettings: StateFlow<Settings> = repository.settingsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = Settings()
    )

    init {
        viewModelScope.launch {
            repository.seedInitialDataIfEmpty()
            val settings = repository.getSettings()
            _currentLanguage.value = AppLanguage.fromCode(settings.language)
        }
    }

    // Navigation actions
    fun navigateTo(dest: ScreenDestination) {
        _selectedCustomerIdForDetails.value = null
        if (dest == ScreenDestination.PURCHASES && _currentDestination.value != ScreenDestination.PURCHASES) {
            _selectedPurchaseCustomer.value = null
        }
        if (dest == ScreenDestination.STATEMENTS && _currentDestination.value != ScreenDestination.STATEMENTS) {
            _selectedStatementCustomer.value = null
            _statementSearchQuery.value = ""
            selectedTxTypeFilter.value = null
        }
        _currentDestination.value = dest
    }

    fun openPurchasesForCustomer(customer: Customer) {
        _selectedCustomerIdForDetails.value = null
        _selectedPurchaseCustomer.value = customer
        _isCreditPurchase.value = true
        _currentDestination.value = ScreenDestination.PURCHASES
    }

    fun openPurchasesDirectly() {
        _selectedCustomerIdForDetails.value = null
        _selectedPurchaseCustomer.value = null
        _isCreditPurchase.value = true
        _currentDestination.value = ScreenDestination.PURCHASES
    }

    fun openCustomerDetails(customerId: Long) {
        _selectedCustomerIdForDetails.value = customerId
    }

    fun closeCustomerDetails() {
        _selectedCustomerIdForDetails.value = null
    }

    fun setCustomerAsHomeContext(customer: Customer) {
        _selectedHomeCustomer.value = customer
        _selectedCustomerIdForDetails.value = null
        _currentDestination.value = ScreenDestination.HOME
    }

    // Notification actions
    fun markNotificationAsRead(id: Long) {
        viewModelScope.launch {
            repository.markNotificationAsRead(id)
        }
    }

    fun markAllNotificationsAsRead() {
        viewModelScope.launch {
            repository.markAllNotificationsAsRead()
        }
    }

    fun deleteNotification(id: Long) {
        viewModelScope.launch {
            repository.deleteNotification(id)
        }
    }

    fun clearAllNotifications() {
        viewModelScope.launch {
            repository.clearAllNotifications()
        }
    }

    fun handleNotificationClick(notification: AppNotification) {
        viewModelScope.launch {
            repository.markNotificationAsRead(notification.id)
            if (notification.customerId != null) {
                openCustomerDetails(notification.customerId)
            }
        }
    }

    fun setCustomerSearchQuery(query: String) {
        _customerSearchQuery.value = query
    }

    fun setStatementSearchQuery(query: String) {
        _statementSearchQuery.value = query
    }

    fun setSelectedStatementCustomer(customer: Customer?) {
        _selectedStatementCustomer.value = customer
    }

    fun setSelectedTxTypeFilter(type: String?) {
        selectedTxTypeFilter.value = type
    }

    fun openStatementsForCustomer(customer: Customer) {
        _selectedCustomerIdForDetails.value = null
        _selectedStatementCustomer.value = customer
        _statementSearchQuery.value = ""
        selectedTxTypeFilter.value = null
        _currentDestination.value = ScreenDestination.STATEMENTS
    }

    fun setProductSearchQuery(query: String) {
        _productSearchQuery.value = query
    }

    fun setDbCustomerSearchQuery(query: String) {
        _dbCustomerSearchQuery.value = query
    }

    fun setDbCustomerStatusFilter(status: String?) {
        _dbCustomerStatusFilter.value = status
    }

    fun setDbProductSearchQuery(query: String) {
        _dbProductSearchQuery.value = query
    }

    fun setDbProductStatusFilter(status: String?) {
        _dbProductStatusFilter.value = status
    }

    fun setSelectedTxStatusFilter(status: String?) {
        selectedTxStatusFilter.value = status
    }

    fun setLanguage(language: AppLanguage) {
        _currentLanguage.value = language
        viewModelScope.launch {
            repository.setLanguage(language.code)
        }
    }

    fun setVisualTheme(theme: AppVisualTheme) {
        _currentVisualTheme.value = theme
        themePrefs.edit().putString("app_visual_theme", theme.code).apply()
        viewModelScope.launch {
            val name = when (theme) {
                AppVisualTheme.PURPLE -> if (_currentLanguage.value == AppLanguage.ARABIC) "السمة البنفسجية" else "Purple Theme"
                AppVisualTheme.GOLD -> if (_currentLanguage.value == AppLanguage.ARABIC) "السمة الذهبية" else "Gold Theme"
                AppVisualTheme.BLACK_AND_WHITE -> if (_currentLanguage.value == AppLanguage.ARABIC) "سمة الأبيض والأسود" else "Black & White Theme"
            }
            _uiEvents.emit(if (_currentLanguage.value == AppLanguage.ARABIC) "تم تفعيل $name بنجاح." else "$name activated.")
        }
    }

    // Cart / Purchases actions
    fun setSelectedPurchaseCustomer(customer: Customer?) {
        _selectedPurchaseCustomer.value = customer
    }

    fun setIsCreditPurchase(isCredit: Boolean) {
        _isCreditPurchase.value = isCredit
    }

    fun setPurchaseNotes(notes: String) {
        _purchaseNotes.value = notes
    }

    fun addProductToCart(product: Product, quantity: Double = 1.0) {
        val current = _cartItems.value.toMutableList()
        val index = current.indexOfFirst { it.productId == product.id }
        if (index >= 0) {
            val existing = current[index]
            current[index] = existing.copy(quantity = existing.quantity + quantity)
        } else {
            current.add(
                CartItem(
                    productId = product.id,
                    name = product.name,
                    unitPrice = product.price,
                    quantity = quantity
                )
            )
        }
        _cartItems.value = current
    }

    fun addCustomItemToCart(name: String, price: Money, quantity: Double = 1.0) {
        if (name.isBlank() || !price.isPositive()) return
        val current = _cartItems.value.toMutableList()
        current.add(
            CartItem(
                productId = null,
                name = name.trim(),
                unitPrice = price,
                quantity = quantity
            )
        )
        _cartItems.value = current
    }

    fun updateCartItemQuantity(index: Int, newQuantity: Double) {
        val current = _cartItems.value.toMutableList()
        if (index in current.indices) {
            if (newQuantity <= 0.0) {
                current.removeAt(index)
            } else {
                current[index] = current[index].copy(quantity = newQuantity)
            }
            _cartItems.value = current
        }
    }

    fun removeCartItem(index: Int) {
        val current = _cartItems.value.toMutableList()
        if (index in current.indices) {
            current.removeAt(index)
            _cartItems.value = current
        }
    }

    fun clearCart() {
        _cartItems.value = emptyList()
        _purchaseNotes.value = ""
    }

    val cartTotal: Money
        get() = _cartItems.value.fold(Money.ZERO) { acc, item -> acc + item.subtotal }

    fun submitPurchase(
        customerId: Long?,
        isCredit: Boolean,
        note: String,
        onSuccess: (Long) -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val items = _cartItems.value
                val result = repository.createPurchase(
                    customerId = customerId,
                    isCredit = isCredit,
                    cartItems = items,
                    note = note
                )

                result.onSuccess { txId ->
                    clearCart()
                    _selectedPurchaseCustomer.value = null
                    _uiEvents.emit(if (_currentLanguage.value == AppLanguage.ARABIC) "تم تسجيل عملية الشراء بنجاح!" else "Purchase recorded successfully!")
                    onSuccess(txId)
                }.onFailure { err ->
                    _uiEvents.emit(err.message ?: "Error recording purchase")
                }
            } catch (e: Exception) {
                _uiEvents.emit(e.message ?: "Error recording purchase")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun submitPurchase(onSuccess: () -> Unit) {
        val customer = _selectedPurchaseCustomer.value
        val isCredit = _isCreditPurchase.value
        val note = _purchaseNotes.value
        submitPurchase(
            customerId = customer?.id,
            isCredit = isCredit,
            note = note,
            onSuccess = { onSuccess() }
        )
    }

    // Payment actions
    fun submitPayment(customerId: Long, amount: Money, note: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = repository.recordPayment(customerId, amount, note)
                result.onSuccess {
                    _uiEvents.emit(if (_currentLanguage.value == AppLanguage.ARABIC) "تم تسجيل الدفعة النقدية بنجاح!" else "Payment recorded successfully!")
                    onSuccess()
                }.onFailure { err ->
                    _uiEvents.emit(err.message ?: "Payment error")
                }
            } catch (e: Exception) {
                _uiEvents.emit(e.message ?: "Payment error")
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Transaction cancellation & restoration
    fun cancelTransaction(transactionId: Long, reason: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = repository.cancelTransaction(transactionId, reason)
                result.onSuccess {
                    _uiEvents.emit(if (_currentLanguage.value == AppLanguage.ARABIC) "تم إلغاء المعاملة بنجاح وتحديث الرصيد." else "Transaction cancelled successfully.")
                    onSuccess()
                }.onFailure { err ->
                    _uiEvents.emit(err.message ?: "Cancellation error")
                }
            } catch (e: Exception) {
                _uiEvents.emit(e.message ?: "Cancellation error")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun restoreTransaction(transactionId: Long, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = repository.restoreTransaction(transactionId)
                result.onSuccess {
                    _uiEvents.emit(if (_currentLanguage.value == AppLanguage.ARABIC) "تمت استعادة المعاملة بنجاح وتحديث الرصيد." else "Transaction restored successfully.")
                    onSuccess()
                }.onFailure { err ->
                    _uiEvents.emit(err.message ?: "Restoration error")
                }
            } catch (e: Exception) {
                _uiEvents.emit(e.message ?: "Restoration error")
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Customer CRUD
    fun saveCustomer(customer: Customer, onSuccess: ((Long) -> Unit)? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = repository.saveCustomer(customer)
                result.onSuccess { newId ->
                    _uiEvents.emit(if (_currentLanguage.value == AppLanguage.ARABIC) "تم حفظ بيانات الزبون بنجاح!" else "Customer saved successfully!")
                    onSuccess?.invoke(newId)
                }.onFailure { err ->
                    _uiEvents.emit(err.message ?: "Error saving customer")
                }
            } catch (e: Exception) {
                _uiEvents.emit(e.message ?: "Error saving customer")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun archiveCustomer(id: Long) {
        viewModelScope.launch {
            repository.archiveCustomer(id)
            _uiEvents.emit(if (_currentLanguage.value == AppLanguage.ARABIC) "تم أرشفة الزبون." else "Customer archived.")
        }
    }

    fun restoreCustomer(id: Long) {
        viewModelScope.launch {
            repository.restoreCustomer(id)
            _uiEvents.emit(if (_currentLanguage.value == AppLanguage.ARABIC) "تمت استعادة الزبون للنشاط." else "Customer restored.")
        }
    }

    fun softDeleteCustomer(id: Long) {
        viewModelScope.launch {
            repository.softDeleteCustomer(id)
            _uiEvents.emit(if (_currentLanguage.value == AppLanguage.ARABIC) "تم نقل الزبون للمحذوفات." else "Customer moved to trash.")
        }
    }

    fun permanentDeleteCustomer(id: Long) {
        viewModelScope.launch {
            repository.permanentDeleteCustomer(id)
            _uiEvents.emit(if (_currentLanguage.value == AppLanguage.ARABIC) "تم حذف الزبون نهائياً من النظام." else "Customer permanently deleted.")
        }
    }

    // Product CRUD
    fun saveProduct(product: Product, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = repository.saveProduct(product)
                result.onSuccess {
                    _uiEvents.emit(if (_currentLanguage.value == AppLanguage.ARABIC) "تم حفظ الصنف بنجاح!" else "Product saved successfully!")
                    onSuccess()
                }.onFailure { err ->
                    _uiEvents.emit(err.message ?: "Error saving product")
                }
            } catch (e: Exception) {
                _uiEvents.emit(e.message ?: "Error saving product")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun archiveProduct(id: Long) {
        viewModelScope.launch {
            repository.archiveProduct(id)
            _uiEvents.emit(if (_currentLanguage.value == AppLanguage.ARABIC) "تم أرشفة الصنف." else "Product archived.")
        }
    }

    fun restoreProduct(id: Long) {
        viewModelScope.launch {
            repository.restoreProduct(id)
            _uiEvents.emit(if (_currentLanguage.value == AppLanguage.ARABIC) "تمت استعادة الصنف للنشاط." else "Product restored.")
        }
    }

    fun softDeleteProduct(id: Long) {
        viewModelScope.launch {
            repository.softDeleteProduct(id)
            _uiEvents.emit(if (_currentLanguage.value == AppLanguage.ARABIC) "تم نقل الصنف للمحذوفات." else "Product moved to trash.")
        }
    }

    fun permanentDeleteProduct(id: Long) {
        viewModelScope.launch {
            repository.permanentDeleteProduct(id)
            _uiEvents.emit(if (_currentLanguage.value == AppLanguage.ARABIC) "تم حذف الصنف نهائياً من النظام." else "Product permanently deleted.")
        }
    }

    fun permanentDeleteTransaction(id: Long) {
        viewModelScope.launch {
            repository.permanentDeleteTransaction(id)
            _uiEvents.emit(if (_currentLanguage.value == AppLanguage.ARABIC) "تم حذف المعاملة الملغاة نهائياً." else "Transaction record permanently deleted.")
        }
    }

    // Settings update
    fun saveSettings(storeName: String, ownerName: String = "", storePhone: String, storeAddress: String) {
        viewModelScope.launch {
            repository.updateShopInfo(storeName, ownerName, storePhone, storeAddress)
            _uiEvents.emit(if (_currentLanguage.value == AppLanguage.ARABIC) "تم حفظ بيانات المتجر بنجاح!" else "Store details saved successfully!")
        }
    }

    fun setThemeMode(theme: String) {
        viewModelScope.launch {
            repository.setTheme(theme)
            _uiEvents.emit(if (_currentLanguage.value == AppLanguage.ARABIC) "تم تغيير المظهر بنجاح." else "Theme updated successfully.")
        }
    }

    fun wipeAllData() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                database.clearAllTables()
                repository.seedInitialDataIfEmpty()
                _uiEvents.emit(if (_currentLanguage.value == AppLanguage.ARABIC) "تمت إعادة ضبط كافة بيانات المحل بنجاح." else "All shop data reset.")
            } catch (e: Exception) {
                _uiEvents.emit(e.message ?: "Reset error")
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Backup & Restore
    fun createAndShareBackup(onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = backupService.createBackupFile(getApplication())
                result.onSuccess { file ->
                    shareFile(file, "application/json", "Shop Accounts Backup")
                    _uiEvents.emit(if (_currentLanguage.value == AppLanguage.ARABIC) "تم إنشاء وتصدير ملف النسخة الاحتياطية بنجاح." else "Backup created and ready to share.")
                    onSuccess()
                }.onFailure { err ->
                    _uiEvents.emit(err.message ?: "Backup failed")
                }
            } catch (e: Exception) {
                _uiEvents.emit(e.message ?: "Backup failed")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun validateAndInspectBackup(
        uri: Uri,
        onSuccess: (BackupPayload) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val context = getApplication<Application>()
                val inputStream = context.contentResolver.openInputStream(uri)
                val jsonString = inputStream?.bufferedReader()?.use { it.readText() } ?: ""
                val validateResult = backupService.validateBackupJson(jsonString)
                validateResult.onSuccess { payload ->
                    onSuccess(payload)
                }.onFailure { err ->
                    val msg = err.message ?: if (_currentLanguage.value == AppLanguage.ARABIC) "ملف النسخة الاحتياطية غير صالح أو تالف." else "Invalid or corrupted backup file."
                    _uiEvents.emit(msg)
                    onError(msg)
                }
            } catch (e: Exception) {
                val msg = e.message ?: "Error reading backup file"
                _uiEvents.emit(msg)
                onError(msg)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun restoreFromValidatedPayload(
        payload: BackupPayload,
        createSafetyBackupFirst: Boolean = true,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                if (createSafetyBackupFirst) {
                    // Create an internal safety backup before restore
                    backupService.createBackupFile(getApplication())
                }
                val restoreResult = backupService.restoreFromPayload(payload)
                restoreResult.onSuccess {
                    _uiEvents.emit(if (_currentLanguage.value == AppLanguage.ARABIC) "تمت استعادة البيانات بالكامل بنجاح!" else "Data restored successfully!")
                    onSuccess()
                }.onFailure { err ->
                    _uiEvents.emit(err.message ?: "Restore failed")
                }
            } catch (e: Exception) {
                _uiEvents.emit(e.message ?: "Restore failed")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun restoreBackupFromUri(uri: Uri) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val context = getApplication<Application>()
                val inputStream = context.contentResolver.openInputStream(uri)
                val jsonString = inputStream?.bufferedReader()?.use { it.readText() } ?: ""
                val validateResult = backupService.validateBackupJson(jsonString)
                val payload = validateResult.getOrThrow()
                val restoreResult = backupService.restoreFromPayload(payload)
                restoreResult.onSuccess {
                    _uiEvents.emit(if (_currentLanguage.value == AppLanguage.ARABIC) "تمت استعادة البيانات بالكامل بنجاح!" else "Data restored successfully!")
                }.onFailure { err ->
                    _uiEvents.emit(err.message ?: "Restore failed")
                }
            } catch (e: Exception) {
                _uiEvents.emit(e.message ?: "Restore failed")
            } finally {
                _isLoading.value = false
            }
        }
    }

    // PDF Reports
    fun generateDebtsSummaryReport(onSuccess: (File) -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val customersWithDebt = activeCustomersWithDebt.value
                val settings = repository.getSettings()
                val isArabic = _currentLanguage.value == AppLanguage.ARABIC
                val result = pdfReportingService.generateAllCustomersDebtReport(customersWithDebt, settings, isArabic)
                result.onSuccess { file ->
                    shareFile(file, "application/pdf", "Debts Summary Report")
                    _uiEvents.emit(if (isArabic) "تم إنشاء تقرير ديون الزبائن PDF بنجاح!" else "Debt Report PDF generated successfully!")
                    onSuccess(file)
                }.onFailure { err ->
                    _uiEvents.emit(err.message ?: "PDF generation failed")
                }
            } catch (e: Exception) {
                _uiEvents.emit(e.message ?: "PDF generation failed")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun generateCustomerStatementReport(customer: Customer, onSuccess: (File) -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val transactions = database.transactionDao().getTransactionsByCustomerList(customer.id).map { it.toDomain() }
                val allItems = database.transactionDao().getAllTransactionItemsList().map { it.toDomain() }
                val itemsMap = allItems.groupBy { it.transactionId }
                val currentDebt = DebtEngine.calculateOutstandingDebt(transactions)
                val settings = repository.getSettings()
                val isArabic = _currentLanguage.value == AppLanguage.ARABIC

                val result = pdfReportingService.generateCustomerStatementReport(
                    customer = customer,
                    transactions = transactions,
                    transactionItems = itemsMap,
                    currentDebt = currentDebt,
                    settings = settings,
                    isArabic = isArabic
                )

                result.onSuccess { file ->
                    shareFile(file, "application/pdf", "Customer Statement - ${customer.name}")
                    _uiEvents.emit(if (isArabic) "تم إنشاء كشف حساب الزبون PDF بنجاح!" else "Customer Statement PDF generated successfully!")
                    onSuccess(file)
                }.onFailure { err ->
                    _uiEvents.emit(err.message ?: "PDF generation failed")
                }
            } catch (e: Exception) {
                _uiEvents.emit(e.message ?: "PDF generation failed")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun generateProductsCatalogReport(onSuccess: (File) -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val products = database.productDao().getAllProductsList().map { it.toDomain() }
                val settings = repository.getSettings()
                val isArabic = _currentLanguage.value == AppLanguage.ARABIC
                val result = pdfReportingService.generateAllProductsCatalogReport(products, settings, isArabic)
                result.onSuccess { file ->
                    shareFile(file, "application/pdf", "Products Catalog Price List")
                    _uiEvents.emit(if (isArabic) "تم إنشاء دليل أسعار الأصناف PDF بنجاح!" else "Products Catalog PDF generated successfully!")
                    onSuccess(file)
                }.onFailure { err ->
                    _uiEvents.emit(err.message ?: "PDF generation failed")
                }
            } catch (e: Exception) {
                _uiEvents.emit(e.message ?: "PDF generation failed")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun generateProductSalesReport(product: Product, onSuccess: (File) -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val allTxItems = database.transactionDao().getAllTransactionItemsList().map { it.toDomain() }
                val productItems = allTxItems.filter { it.productId == product.id }
                val allTransactions = database.transactionDao().getAllTransactionsList().map { it.toDomain() }
                val txMap = allTransactions.associateBy { it.id }
                val settings = repository.getSettings()
                val isArabic = _currentLanguage.value == AppLanguage.ARABIC

                val result = pdfReportingService.generateProductSalesReport(
                    product = product,
                    items = productItems,
                    settings = settings,
                    isArabic = isArabic
                )

                result.onSuccess { file ->
                    shareFile(file, "application/pdf", "Product Sales Report - ${product.name}")
                    _uiEvents.emit(if (isArabic) "تم إنشاء تقرير مبيعات الصنف PDF بنجاح!" else "Product Sales Report PDF generated successfully!")
                    onSuccess(file)
                }.onFailure { err ->
                    _uiEvents.emit(err.message ?: "PDF generation failed")
                }
            } catch (e: Exception) {
                _uiEvents.emit(e.message ?: "PDF generation failed")
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun shareFile(file: File, mimeType: String, title: String) {
        try {
            val context = getApplication<Application>()
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val chooser = Intent.createChooser(intent, title).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        } catch (e: Exception) {
            // fallback
        }
    }
}
