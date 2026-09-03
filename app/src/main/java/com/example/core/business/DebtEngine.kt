package com.example.core.business

import com.example.core.model.CustomerWithDebt
import com.example.core.model.FinancialSummary
import com.example.core.model.Money
import com.example.core.model.Transaction
import com.example.core.model.TransactionStatus
import com.example.core.model.TransactionType
import java.util.Calendar

/**
 * Pure, isolated Financial Business Logic Engine.
 * Implements strict calculation rules without side effects.
 */
object DebtEngine {

    /**
     * Calculates customer's outstanding debt from historical transaction records.
     * Formula:
     * Outstanding Debt = Valid Credit Purchases - Valid Payments
     *
     * Rules:
     * - Only COMPLETED transactions are counted.
     * - CANCELLED transactions are strictly excluded.
     * - CASH_PURCHASE transactions do NOT affect debt.
     * - Debt cannot be negative (floor at 0).
     */
    fun calculateOutstandingDebt(transactions: List<Transaction>): Money {
        var totalCreditMinorUnits = 0L
        var totalPaymentsMinorUnits = 0L

        for (tx in transactions) {
            if (tx.status != TransactionStatus.COMPLETED) continue

            when (tx.type) {
                TransactionType.CREDIT_PURCHASE -> {
                    totalCreditMinorUnits += tx.totalAmount.minorUnits
                }
                TransactionType.PAYMENT -> {
                    totalPaymentsMinorUnits += tx.totalAmount.minorUnits
                }
                TransactionType.CASH_PURCHASE -> {
                    // Cash purchases do NOT increase or decrease debt
                }
            }
        }

        val netMinorUnits = totalCreditMinorUnits - totalPaymentsMinorUnits
        return if (netMinorUnits > 0L) {
            Money.fromMinorUnits(netMinorUnits)
        } else {
            Money.ZERO
        }
    }

    /**
     * Validates if a proposed payment amount is permissible given the current outstanding debt.
     * Rules:
     * - Payment amount must be strictly positive.
     * - Payment amount cannot exceed the customer's outstanding debt.
     */
    fun validatePaymentAmount(paymentAmount: Money, currentDebt: Money): PaymentValidationResult {
        if (!paymentAmount.isPositive()) {
            return PaymentValidationResult.Invalid("Payment amount must be greater than zero.")
        }
        if (paymentAmount > currentDebt) {
            return PaymentValidationResult.ExceedsDebt(
                maxAllowed = currentDebt,
                message = "Payment amount (${paymentAmount.format()}) exceeds outstanding debt (${currentDebt.format()})."
            )
        }
        return PaymentValidationResult.Valid
    }

    /**
     * Aggregates daily dashboard summary figures from completed transactions.
     */
    fun calculateFinancialSummary(
        customersWithDebt: List<CustomerWithDebt>,
        allTransactions: List<Transaction>,
        targetDayTimestamp: Long = System.currentTimeMillis()
    ): FinancialSummary {
        val totalDebt = customersWithDebt.fold(Money.ZERO) { acc, c -> acc + c.outstandingDebt }
        val countDebtCustomers = customersWithDebt.count { it.outstandingDebt.isPositive() }

        val startOfDay = getStartOfDayTimestamp(targetDayTimestamp)
        val endOfDay = startOfDay + 86400000L

        var todayCreditMinor = 0L
        var todayPaymentMinor = 0L
        var todayCashMinor = 0L

        for (tx in allTransactions) {
            if (tx.status == TransactionStatus.COMPLETED && tx.createdAt in startOfDay until endOfDay) {
                when (tx.type) {
                    TransactionType.CREDIT_PURCHASE -> todayCreditMinor += tx.totalAmount.minorUnits
                    TransactionType.PAYMENT -> todayPaymentMinor += tx.totalAmount.minorUnits
                    TransactionType.CASH_PURCHASE -> todayCashMinor += tx.totalAmount.minorUnits
                }
            }
        }

        return FinancialSummary(
            totalOutstandingDebt = totalDebt,
            customersWithDebtCount = countDebtCustomers,
            todayCreditPurchases = Money.fromMinorUnits(todayCreditMinor),
            todayPayments = Money.fromMinorUnits(todayPaymentMinor),
            todayCashPurchases = Money.fromMinorUnits(todayCashMinor)
        )
    }

    private fun getStartOfDayTimestamp(timestamp: Long): Long {
        val cal = Calendar.getInstance().apply {
            timeInMillis = timestamp
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }

    /**
     * Resolves start and end millisecond timestamps for the specified analysis period.
     */
    fun getPeriodBounds(
        period: AnalysisPeriod,
        customStart: Long? = null,
        customEnd: Long? = null,
        now: Long = System.currentTimeMillis()
    ): Pair<Long, Long> {
        val cal = Calendar.getInstance().apply { timeInMillis = now }
        return when (period) {
            AnalysisPeriod.TODAY -> {
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val start = cal.timeInMillis
                cal.set(Calendar.HOUR_OF_DAY, 23)
                cal.set(Calendar.MINUTE, 59)
                cal.set(Calendar.SECOND, 59)
                cal.set(Calendar.MILLISECOND, 999)
                Pair(start, cal.timeInMillis)
            }
            AnalysisPeriod.THIS_WEEK -> {
                cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val start = cal.timeInMillis
                cal.add(Calendar.DAY_OF_WEEK, 6)
                cal.set(Calendar.HOUR_OF_DAY, 23)
                cal.set(Calendar.MINUTE, 59)
                cal.set(Calendar.SECOND, 59)
                cal.set(Calendar.MILLISECOND, 999)
                Pair(start, cal.timeInMillis)
            }
            AnalysisPeriod.THIS_MONTH -> {
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val start = cal.timeInMillis
                cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
                cal.set(Calendar.HOUR_OF_DAY, 23)
                cal.set(Calendar.MINUTE, 59)
                cal.set(Calendar.SECOND, 59)
                cal.set(Calendar.MILLISECOND, 999)
                Pair(start, cal.timeInMillis)
            }
            AnalysisPeriod.CUSTOM -> {
                val start = customStart ?: 0L
                val end = customEnd ?: Long.MAX_VALUE
                Pair(start, end)
            }
            AnalysisPeriod.ALL_TIME -> {
                Pair(0L, Long.MAX_VALUE)
            }
        }
    }

    /**
     * Calculates comprehensive analytical statistics for a list of transactions in a time window.
     */
    fun calculateAnalysisMetrics(
        transactionsInPeriod: List<Transaction>,
        totalOutstandingDebt: Money,
        totalCustomersCount: Int,
        activeCustomersCount: Int
    ): AnalysisMetrics {
        var cashMinor = 0L
        var creditMinor = 0L
        var paymentsMinor = 0L
        var cashCount = 0
        var creditCount = 0
        var paymentsCount = 0
        var completedCount = 0

        for (tx in transactionsInPeriod) {
            if (tx.status != TransactionStatus.COMPLETED) continue
            completedCount++
            when (tx.type) {
                TransactionType.CASH_PURCHASE -> {
                    cashMinor += tx.totalAmount.minorUnits
                    cashCount++
                }
                TransactionType.CREDIT_PURCHASE -> {
                    creditMinor += tx.totalAmount.minorUnits
                    creditCount++
                }
                TransactionType.PAYMENT -> {
                    paymentsMinor += tx.totalAmount.minorUnits
                    paymentsCount++
                }
            }
        }

        val totalSalesMinor = cashMinor + creditMinor
        val totalVolumeMinor = totalSalesMinor + paymentsMinor

        val cashSales = Money.fromMinorUnits(cashMinor)
        val creditSales = Money.fromMinorUnits(creditMinor)
        val totalSales = Money.fromMinorUnits(totalSalesMinor)
        val paymentsCollected = Money.fromMinorUnits(paymentsMinor)

        val collectionRate = if (creditMinor > 0L) {
            ((paymentsMinor.toDouble() / creditMinor.toDouble()) * 100.0).toFloat().coerceIn(0f, 100f)
        } else if (totalSalesMinor > 0L && paymentsMinor > 0L) {
            100f
        } else {
            0f
        }

        val cashPercentage = if (totalVolumeMinor > 0L) {
            ((cashMinor.toDouble() / totalVolumeMinor.toDouble()) * 100.0).toFloat()
        } else 0f

        val creditPercentage = if (totalVolumeMinor > 0L) {
            ((creditMinor.toDouble() / totalVolumeMinor.toDouble()) * 100.0).toFloat()
        } else 0f

        val paymentsPercentage = if (totalVolumeMinor > 0L) {
            ((paymentsMinor.toDouble() / totalVolumeMinor.toDouble()) * 100.0).toFloat()
        } else 0f

        val averageDebt = if (activeCustomersCount > 0 && totalOutstandingDebt.isPositive()) {
            Money.fromMinorUnits(totalOutstandingDebt.minorUnits / activeCustomersCount)
        } else {
            Money.ZERO
        }

        return AnalysisMetrics(
            totalSales = totalSales,
            cashSales = cashSales,
            creditSales = creditSales,
            paymentsCollected = paymentsCollected,
            outstandingCustomerDebt = totalOutstandingDebt,
            totalTransactionsCount = completedCount,
            cashSalesCount = cashCount,
            creditSalesCount = creditCount,
            paymentsCount = paymentsCount,
            totalCustomersCount = totalCustomersCount,
            activeCustomersCount = activeCustomersCount,
            collectionRate = collectionRate,
            cashPercentage = cashPercentage,
            creditPercentage = creditPercentage,
            paymentsPercentage = paymentsPercentage,
            averageDebt = averageDebt,
            totalVolume = Money.fromMinorUnits(totalVolumeMinor)
        )
    }

    /**
     * Computes chronological running debt balance for each transaction of a customer.
     * Always evaluates in strict chronological order (oldest to newest) to preserve
     * the exact balance trajectory over time.
     */
    fun calculateRunningBalances(
        customerTransactions: List<Transaction>
    ): Map<Long, Money> {
        val chronological = customerTransactions.sortedBy { it.createdAt }
        val resultMap = mutableMapOf<Long, Money>()
        var runningDebtMinor = 0L

        for (tx in chronological) {
            if (tx.status == TransactionStatus.COMPLETED) {
                when (tx.type) {
                    TransactionType.CREDIT_PURCHASE -> {
                        runningDebtMinor += tx.totalAmount.minorUnits
                    }
                    TransactionType.PAYMENT -> {
                        runningDebtMinor = if (runningDebtMinor > tx.totalAmount.minorUnits) {
                            runningDebtMinor - tx.totalAmount.minorUnits
                        } else {
                            0L
                        }
                    }
                    TransactionType.CASH_PURCHASE -> {
                        // Cash purchase does not affect debt running balance
                    }
                }
            }
            resultMap[tx.id] = Money.fromMinorUnits(runningDebtMinor)
        }

        return resultMap
    }
}

enum class AnalysisPeriod {
    TODAY,
    THIS_WEEK,
    THIS_MONTH,
    CUSTOM,
    ALL_TIME
}

data class AnalysisMetrics(
    val totalSales: Money = Money.ZERO,
    val cashSales: Money = Money.ZERO,
    val creditSales: Money = Money.ZERO,
    val paymentsCollected: Money = Money.ZERO,
    val outstandingCustomerDebt: Money = Money.ZERO,
    val totalTransactionsCount: Int = 0,
    val cashSalesCount: Int = 0,
    val creditSalesCount: Int = 0,
    val paymentsCount: Int = 0,
    val totalCustomersCount: Int = 0,
    val activeCustomersCount: Int = 0,
    val collectionRate: Float = 0f,
    val cashPercentage: Float = 0f,
    val creditPercentage: Float = 0f,
    val paymentsPercentage: Float = 0f,
    val averageDebt: Money = Money.ZERO,
    val totalVolume: Money = Money.ZERO
)

sealed class PaymentValidationResult {
    object Valid : PaymentValidationResult()
    data class ExceedsDebt(val maxAllowed: Money, val message: String) : PaymentValidationResult()
    data class Invalid(val message: String) : PaymentValidationResult()

    val isValid: Boolean get() = this is Valid
}
