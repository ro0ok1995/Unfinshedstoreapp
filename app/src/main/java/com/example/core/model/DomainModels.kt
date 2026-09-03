package com.example.core.model

data class Customer(
    val id: Long = 0,
    val name: String,
    val phone: String = "",
    val countryCode: String = CountryConstants.DEFAULT_COUNTRY_CODE,
    val address: String = "",
    val status: String = CustomerStatus.ACTIVE,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val archivedAt: Long? = null,
    val deletedAt: Long? = null
) {
    val isActive: Boolean get() = status == CustomerStatus.ACTIVE
    val isArchived: Boolean get() = status == CustomerStatus.ARCHIVED
    val isDeleted: Boolean get() = status == CustomerStatus.DELETED

    val formattedPhoneWithCode: String
        get() = if (phone.isNotBlank()) {
            if (phone.startsWith("+")) phone else "$countryCode $phone"
        } else ""
}

data class Product(
    val id: Long = 0,
    val name: String,
    val price: Money,
    val imagePath: String? = null,
    val status: String = ProductStatus.ACTIVE,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val archivedAt: Long? = null,
    val deletedAt: Long? = null
) {
    val isActive: Boolean get() = status == ProductStatus.ACTIVE
    val isArchived: Boolean get() = status == ProductStatus.ARCHIVED
    val isDeleted: Boolean get() = status == ProductStatus.DELETED
}

data class Transaction(
    val id: Long = 0,
    val customerId: Long? = null,
    val type: String, // credit_purchase, cash_purchase, payment
    val totalAmount: Money,
    val status: String = TransactionStatus.COMPLETED,
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val cancelledAt: Long? = null,
    val cancelReason: String? = null
) {
    val isCompleted: Boolean get() = status == TransactionStatus.COMPLETED
    val isCancelled: Boolean get() = status == TransactionStatus.CANCELLED
    val isCreditPurchase: Boolean get() = type == TransactionType.CREDIT_PURCHASE
    val isCashPurchase: Boolean get() = type == TransactionType.CASH_PURCHASE
    val isPayment: Boolean get() = type == TransactionType.PAYMENT
}

data class TransactionItem(
    val id: Long = 0,
    val transactionId: Long,
    val productId: Long? = null,
    val productNameSnapshot: String,
    val unitPrice: Money,
    val quantity: Double,
    val subtotal: Money
)

data class Settings(
    val id: Int = 1,
    val storeName: String = "حسابات المحل",
    val ownerName: String = "",
    val phone: String = "",
    val address: String = "",
    val language: String = AppLanguageCode.ARABIC,
    val theme: String = AppThemeMode.SYSTEM,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class CartItem(
    val productId: Long? = null,
    val name: String,
    val unitPrice: Money,
    val quantity: Double = 1.0
) {
    val subtotal: Money get() = unitPrice * quantity
}

enum class SettlementMode {
    FULL_DEBT,   // على الحساب / آجل كامل
    FULL_CASH,   // دفع كاش كامل
    PARTIAL      // دفع جزئي (نقد + آجل)
}

data class CustomerWithDebt(
    val customer: Customer,
    val outstandingDebt: Money,
    val totalPurchasesCount: Int = 0,
    val lastTransactionTime: Long? = null
)

data class TransactionWithDetails(
    val transaction: Transaction,
    val customer: Customer? = null,
    val items: List<TransactionItem> = emptyList()
)

data class FinancialSummary(
    val totalOutstandingDebt: Money = Money.ZERO,
    val customersWithDebtCount: Int = 0,
    val todayCreditPurchases: Money = Money.ZERO,
    val todayPayments: Money = Money.ZERO,
    val todayCashPurchases: Money = Money.ZERO
)

data class AppNotification(
    val id: Long = 0,
    val title: String,
    val message: String,
    val type: String, // purchase, payment, customer, system
    val customerId: Long? = null,
    val isRead: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
