package com.example.core.model

/**
 * Standard Status and Type definitions adhering to the Master Prompt.
 */
object CustomerStatus {
    const val ACTIVE = "active"
    const val ARCHIVED = "archived"
    const val DELETED = "deleted"

    fun isValid(status: String): Boolean = status in listOf(ACTIVE, ARCHIVED, DELETED)
}

object ProductStatus {
    const val ACTIVE = "active"
    const val ARCHIVED = "archived"
    const val DELETED = "deleted"

    fun isValid(status: String): Boolean = status in listOf(ACTIVE, ARCHIVED, DELETED)
}

object TransactionType {
    const val CREDIT_PURCHASE = "credit_purchase"
    const val CASH_PURCHASE = "cash_purchase"
    const val PAYMENT = "payment"

    fun isValid(type: String): Boolean = type in listOf(CREDIT_PURCHASE, CASH_PURCHASE, PAYMENT)
}

object TransactionStatus {
    const val COMPLETED = "completed"
    const val CANCELLED = "cancelled"

    fun isValid(status: String): Boolean = status in listOf(COMPLETED, CANCELLED)
}

object AppLanguageCode {
    const val ARABIC = "ar"
    const val ENGLISH = "en"
}

object AppThemeMode {
    const val LIGHT = "light"
    const val DARK = "dark"
    const val SYSTEM = "system"
}

object AppVisualThemeType {
    const val BLACK_AND_WHITE = "black_and_white"
    const val PURPLE = "purple"
    const val GOLD = "gold"
}

object CountryConstants {
    const val DEFAULT_COUNTRY_CODE = "+970"
    const val DEFAULT_COUNTRY_NAME_AR = "فلسطين"
    const val DEFAULT_COUNTRY_NAME_EN = "Palestine"
}
