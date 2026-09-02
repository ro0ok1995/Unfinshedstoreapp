package com.example.core.model

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

/**
 * Monetary representation using integer minor-units (Agorot / Cents).
 * 100 minor units = 1.00 ₪ (Israeli Shekel).
 * Avoids any floating-point arithmetic errors.
 * Example: 350 minor units = 3.50 ₪
 */
@ConsistentCopyVisibility
data class Money private constructor(val minorUnits: Long) : Comparable<Money> {

    init {
        // Enforce integer minor-unit constraints
    }

    val shekels: Double
        get() = minorUnits / 100.0

    operator fun plus(other: Money): Money = Money(this.minorUnits + other.minorUnits)

    operator fun minus(other: Money): Money = Money(this.minorUnits - other.minorUnits)

    operator fun times(quantity: Double): Money {
        val calculated = Math.round(this.minorUnits * quantity)
        return Money(calculated)
    }

    operator fun times(quantity: Long): Money = Money(this.minorUnits * quantity)
    operator fun times(quantity: Int): Money = Money(this.minorUnits * quantity.toLong())

    override fun compareTo(other: Money): Int = this.minorUnits.compareTo(other.minorUnits)

    fun isZero(): Boolean = minorUnits == 0L
    fun isPositive(): Boolean = minorUnits > 0L
    fun isNegative(): Boolean = minorUnits < 0L

    /**
     * Formats monetary amount according to language.
     * In Arabic: "3.50 ₪"
     * In English: "₪ 3.50"
     */
    fun format(isArabic: Boolean = true): String {
        val df = DecimalFormat("#,##0.00", DecimalFormatSymbols(Locale.US))
        val formattedNumber = df.format(shekels)
        return if (isArabic) {
            "$formattedNumber ₪"
        } else {
            "₪ $formattedNumber"
        }
    }

    fun formatWithoutSymbol(): String {
        val df = DecimalFormat("#,##0.00", DecimalFormatSymbols(Locale.US))
        return df.format(shekels)
    }

    companion object {
        val ZERO = Money(0L)

        fun fromMinorUnits(minorUnits: Long): Money = Money(minorUnits)

        fun fromShekels(shekels: Double): Money {
            val minor = Math.round(shekels * 100.0)
            return Money(minor)
        }

        fun fromShekels(shekelsStr: String): Money {
            val cleaned = shekelsStr.trim().replace(",", "")
            val parsed = cleaned.toDoubleOrNull() ?: 0.0
            return fromShekels(parsed)
        }

        fun max(a: Money, b: Money): Money = if (a >= b) a else b
        fun min(a: Money, b: Money): Money = if (a <= b) a else b
    }
}
