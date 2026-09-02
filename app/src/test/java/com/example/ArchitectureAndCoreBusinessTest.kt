package com.example

import com.example.core.business.DebtEngine
import com.example.core.business.PaymentValidationResult
import com.example.core.model.CartItem
import com.example.core.model.Customer
import com.example.core.model.CustomerStatus
import com.example.core.model.Money
import com.example.core.model.Product
import com.example.core.model.ProductStatus
import com.example.core.model.Transaction
import com.example.core.model.TransactionStatus
import com.example.core.model.TransactionType
import com.example.core.validation.CustomerValidator
import com.example.core.validation.PhoneValidator
import com.example.core.validation.ProductValidator
import com.example.core.validation.TransactionValidator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ArchitectureAndCoreBusinessTest {

    @Test
    fun testMoneyMinorUnitArithmeticAndFormatting() {
        // 3.50 ₪ = 350 minor units
        val price = Money.fromShekels(3.50)
        assertEquals(350L, price.minorUnits)
        assertEquals(3.50, price.shekels, 0.0001)

        // Quantity multiplication: 3 units of 3.50 = 10.50 ₪ (1050 minor units)
        val total = price * 3.0
        assertEquals(1050L, total.minorUnits)
        assertEquals(10.50, total.shekels, 0.0001)

        // Fractional weight multiplication: 1.5 kg of 3.50 ₪/kg = 5.25 ₪ (525 minor units)
        val fractionalTotal = price * 1.5
        assertEquals(525L, fractionalTotal.minorUnits)
        assertEquals(5.25, fractionalTotal.shekels, 0.0001)

        // Addition and Subtraction
        val m1 = Money.fromMinorUnits(500)
        val m2 = Money.fromMinorUnits(250)
        assertEquals(750L, (m1 + m2).minorUnits)
        assertEquals(250L, (m1 - m2).minorUnits)

        // Formatting
        assertEquals("3.50 ₪", price.format(isArabic = true))
        assertEquals("₪ 3.50", price.format(isArabic = false))
    }

    @Test
    fun testDebtEngineCalculationRules() {
        // Scenario:
        // 1. Credit purchase: 100 ₪
        // 2. Credit purchase: 50 ₪
        // 3. Cash purchase: 80 ₪ (MUST NOT affect debt)
        // 4. Payment: 40 ₪
        // 5. Cancelled credit purchase: 70 ₪ (MUST NOT affect debt)
        // Expected Outstanding Debt = 100 + 50 - 40 = 110 ₪

        val transactions = listOf(
            Transaction(
                id = 1,
                customerId = 10,
                type = TransactionType.CREDIT_PURCHASE,
                totalAmount = Money.fromShekels(100.0),
                status = TransactionStatus.COMPLETED
            ),
            Transaction(
                id = 2,
                customerId = 10,
                type = TransactionType.CREDIT_PURCHASE,
                totalAmount = Money.fromShekels(50.0),
                status = TransactionStatus.COMPLETED
            ),
            Transaction(
                id = 3,
                customerId = 10,
                type = TransactionType.CASH_PURCHASE,
                totalAmount = Money.fromShekels(80.0),
                status = TransactionStatus.COMPLETED
            ),
            Transaction(
                id = 4,
                customerId = 10,
                type = TransactionType.PAYMENT,
                totalAmount = Money.fromShekels(40.0),
                status = TransactionStatus.COMPLETED
            ),
            Transaction(
                id = 5,
                customerId = 10,
                type = TransactionType.CREDIT_PURCHASE,
                totalAmount = Money.fromShekels(70.0),
                status = TransactionStatus.CANCELLED
            )
        )

        val debt = DebtEngine.calculateOutstandingDebt(transactions)
        assertEquals(11000L, debt.minorUnits)
        assertEquals(110.0, debt.shekels, 0.0001)
    }

    @Test
    fun testDebtNeverBecomesNegative() {
        // Overpaid or anomalous transactions must floor at 0
        val transactions = listOf(
            Transaction(
                id = 1,
                customerId = 10,
                type = TransactionType.CREDIT_PURCHASE,
                totalAmount = Money.fromShekels(50.0),
                status = TransactionStatus.COMPLETED
            ),
            Transaction(
                id = 2,
                customerId = 10,
                type = TransactionType.PAYMENT,
                totalAmount = Money.fromShekels(70.0),
                status = TransactionStatus.COMPLETED
            )
        )

        val debt = DebtEngine.calculateOutstandingDebt(transactions)
        assertEquals(Money.ZERO, debt)
    }

    @Test
    fun testPaymentCeilingValidation() {
        val currentDebt = Money.fromShekels(150.0)

        // Valid payment of 100 ₪
        val res1 = DebtEngine.validatePaymentAmount(Money.fromShekels(100.0), currentDebt)
        assertTrue(res1.isValid)

        // Valid exact payment of 150 ₪
        val res2 = DebtEngine.validatePaymentAmount(Money.fromShekels(150.0), currentDebt)
        assertTrue(res2.isValid)

        // Invalid excess payment of 150.50 ₪
        val res3 = DebtEngine.validatePaymentAmount(Money.fromShekels(150.50), currentDebt)
        assertFalse(res3.isValid)
        assertTrue(res3 is PaymentValidationResult.ExceedsDebt)

        // Invalid zero payment
        val res4 = DebtEngine.validatePaymentAmount(Money.ZERO, currentDebt)
        assertFalse(res4.isValid)
    }

    @Test
    fun testPalestinianPhoneValidation() {
        // Valid Jawwal & Ooredoo local numbers
        assertTrue(PhoneValidator.validatePhone("0599123456", "+970").isValid)
        assertTrue(PhoneValidator.validatePhone("0569123456", "+970").isValid)
        assertTrue(PhoneValidator.validatePhone("+970599123456", "+970").isValid)
        assertTrue(PhoneValidator.validatePhone("+972569123456", "+972").isValid)

        // Optional blank is valid
        assertTrue(PhoneValidator.validatePhone("", "+970").isValid)
        assertTrue(PhoneValidator.validatePhone("  ", "+970").isValid)

        // Invalid short / letters
        assertFalse(PhoneValidator.validatePhone("12345", "+970").isValid)
        assertFalse(PhoneValidator.validatePhone("0599abc123", "+970").isValid)
    }

    @Test
    fun testCustomerAndProductValidation() {
        // Customer Validator
        val validCustomer = Customer(name = "أحمد خليل", phone = "0599123456")
        assertTrue(CustomerValidator.validate(validCustomer).isValid)

        val invalidCustomerName = Customer(name = "أ")
        assertFalse(CustomerValidator.validate(invalidCustomerName).isValid)

        // Product Validator
        val validProduct = Product(name = "سكر 1 كغم", price = Money.fromShekels(5.0))
        assertTrue(ProductValidator.validate(validProduct).isValid)

        val invalidProductPrice = Product(name = "سكر 1 كغم", price = Money.ZERO)
        assertFalse(ProductValidator.validate(invalidProductPrice).isValid)
    }

    @Test
    fun testTransactionValidationRules() {
        val items = listOf(
            CartItem(productId = 1L, name = "سكر", unitPrice = Money.fromShekels(5.0), quantity = 2.0)
        )

        // Active customer credit purchase is valid
        assertTrue(TransactionValidator.validateCreditPurchase(1L, true, items).isValid)

        // Credit purchase with null customer is invalid
        assertFalse(TransactionValidator.validateCreditPurchase(null, true, items).isValid)

        // Credit purchase with archived/deleted customer is invalid
        assertFalse(TransactionValidator.validateCreditPurchase(1L, false, items).isValid)

        // Cash purchase does not require customer
        assertTrue(TransactionValidator.validateCashPurchase(items).isValid)

        // Empty cart is invalid
        assertFalse(TransactionValidator.validateCashPurchase(emptyList()).isValid)
    }

    @Test
    fun testTransactionRestorationReappliesDebt() {
        val txCancelled = Transaction(
            id = 101,
            customerId = 5,
            type = TransactionType.CREDIT_PURCHASE,
            totalAmount = Money.fromShekels(150.0),
            status = TransactionStatus.CANCELLED,
            cancelReason = "Entered by mistake"
        )
        // When cancelled, debt is 0
        assertEquals(0L, DebtEngine.calculateOutstandingDebt(listOf(txCancelled)).minorUnits)

        // When restored back to COMPLETED, debt is restored to 150.0
        val txRestored = txCancelled.copy(
            status = TransactionStatus.COMPLETED,
            cancelReason = null,
            cancelledAt = null
        )
        assertEquals(15000L, DebtEngine.calculateOutstandingDebt(listOf(txRestored)).minorUnits)
    }

    @Test
    fun testProductWithImageModelIntegrity() {
        val productWithImage = Product(
            id = 1,
            name = "حليب رغيد",
            price = Money.fromShekels(4.5),
            imagePath = "/data/user/0/com.example/files/product_images/product_123.jpg"
        )
        assertTrue(ProductValidator.validate(productWithImage).isValid)
        assertEquals("/data/user/0/com.example/files/product_images/product_123.jpg", productWithImage.imagePath)
    }
}
