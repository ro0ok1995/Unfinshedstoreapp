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
}

sealed class PaymentValidationResult {
    object Valid : PaymentValidationResult()
    data class ExceedsDebt(val maxAllowed: Money, val message: String) : PaymentValidationResult()
    data class Invalid(val message: String) : PaymentValidationResult()

    val isValid: Boolean get() = this is Valid
}
