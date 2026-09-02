package com.example.core.validation

import com.example.core.model.CartItem
import com.example.core.model.CountryConstants
import com.example.core.model.Customer
import com.example.core.model.Money
import com.example.core.model.Product

sealed class ValidationResult {
    object Valid : ValidationResult()
    data class Error(val errorKey: String, val defaultMessage: String) : ValidationResult()

    val isValid: Boolean get() = this is Valid
}

object PhoneValidator {
    /**
     * Validates optional phone number according to country code.
     * For Palestine (+970 / +972):
     * Valid mobile prefixes: 059 (Jawwal), 056 (Ooredoo), 050, 052, 054, 058, 053, 055, 02 (Jerusalem/WestBank), 08 (Gaza), 09 (Nablus/Tulkarm/Jenin/Qalqilya)
     * Total digits: 9 to 10 digits without country code.
     */
    fun validatePhone(phone: String, countryCode: String = CountryConstants.DEFAULT_COUNTRY_CODE): ValidationResult {
        val trimmed = phone.trim()
        if (trimmed.isEmpty()) {
            return ValidationResult.Valid // Phone is explicitly optional
        }

        // Clean any spaces, hyphens, or brackets
        val digitsOnly = trimmed.filter { it.isDigit() || it == '+' }

        return when (countryCode) {
            "+970", "+972" -> {
                val localDigits = digitsOnly
                    .removePrefix("+970")
                    .removePrefix("+972")
                    .removePrefix("00970")
                    .removePrefix("00972")

                val normalized = if (localDigits.startsWith("0")) localDigits else "0$localDigits"
                val regex = Regex("^0(5[0-9]|2|4|8|9)[0-9]{7}$")

                if (regex.matches(normalized)) {
                    ValidationResult.Valid
                } else {
                    ValidationResult.Error("invalid_phone_palestine", "Invalid Palestinian/local phone format (e.g., 0599123456 or 0569123456).")
                }
            }
            else -> {
                if (digitsOnly.length in 7..15) {
                    ValidationResult.Valid
                } else {
                    ValidationResult.Error("invalid_phone_international", "Phone number must be between 7 and 15 digits.")
                }
            }
        }
    }
}

object CustomerValidator {
    fun validate(customer: Customer): ValidationResult {
        if (customer.name.trim().length < 2) {
            return ValidationResult.Error("name_too_short", "Customer name must be at least 2 characters long.")
        }

        val phoneValidation = PhoneValidator.validatePhone(customer.phone, customer.countryCode)
        if (!phoneValidation.isValid) {
            return phoneValidation
        }

        return ValidationResult.Valid
    }
}

object ProductValidator {
    fun validate(product: Product): ValidationResult {
        if (product.name.trim().isEmpty()) {
            return ValidationResult.Error("product_name_empty", "Product name cannot be empty.")
        }
        if (!product.price.isPositive()) {
            return ValidationResult.Error("product_price_non_positive", "Product price must be greater than zero.")
        }
        return ValidationResult.Valid
    }
}

object TransactionValidator {
    fun validateCreditPurchase(
        customerId: Long?,
        customerIsActive: Boolean,
        cartItems: List<CartItem>
    ): ValidationResult {
        if (customerId == null) {
            return ValidationResult.Error("credit_customer_required", "A registered customer must be selected for credit purchases.")
        }
        if (!customerIsActive) {
            return ValidationResult.Error("customer_not_active", "Cannot record transactions for archived or deleted customers.")
        }
        if (cartItems.isEmpty()) {
            return ValidationResult.Error("cart_empty", "Purchase must contain at least one item.")
        }
        for (item in cartItems) {
            if (item.name.trim().isEmpty()) {
                return ValidationResult.Error("item_name_empty", "Item name cannot be empty.")
            }
            if (item.quantity <= 0.0) {
                return ValidationResult.Error("invalid_quantity", "Quantity must be greater than zero.")
            }
            if (item.unitPrice.minorUnits < 0L) {
                return ValidationResult.Error("invalid_price", "Unit price cannot be negative.")
            }
        }
        return ValidationResult.Valid
    }

    fun validateCashPurchase(cartItems: List<CartItem>): ValidationResult {
        if (cartItems.isEmpty()) {
            return ValidationResult.Error("cart_empty", "Purchase must contain at least one item.")
        }
        for (item in cartItems) {
            if (item.name.trim().isEmpty()) {
                return ValidationResult.Error("item_name_empty", "Item name cannot be empty.")
            }
            if (item.quantity <= 0.0) {
                return ValidationResult.Error("invalid_quantity", "Quantity must be greater than zero.")
            }
            if (item.unitPrice.minorUnits < 0L) {
                return ValidationResult.Error("invalid_price", "Unit price cannot be negative.")
            }
        }
        return ValidationResult.Valid
    }

    fun validatePayment(
        customerId: Long?,
        customerIsActive: Boolean,
        amount: Money,
        currentDebt: Money
    ): ValidationResult {
        if (customerId == null) {
            return ValidationResult.Error("payment_customer_required", "Customer selection is required for recording a payment.")
        }
        if (!customerIsActive) {
            return ValidationResult.Error("customer_not_active", "Cannot record payments for archived or deleted customers.")
        }
        if (!amount.isPositive()) {
            return ValidationResult.Error("payment_zero", "Payment amount must be greater than zero.")
        }
        if (amount > currentDebt) {
            return ValidationResult.Error(
                "payment_exceeds_debt",
                "Payment amount (${amount.format()}) cannot exceed customer's outstanding debt (${currentDebt.format()})."
            )
        }
        return ValidationResult.Valid
    }
}
