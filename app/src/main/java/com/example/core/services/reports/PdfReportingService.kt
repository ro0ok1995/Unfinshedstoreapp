package com.example.core.services.reports

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import com.example.core.model.Customer
import com.example.core.model.CustomerWithDebt
import com.example.core.model.Money
import com.example.core.model.Product
import com.example.core.model.Settings
import com.example.core.model.Transaction
import com.example.core.model.TransactionItem
import com.example.core.model.TransactionType
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PdfReportingService(private val context: Context) {

    /**
     * 1. Customer Account Statement Report (كشف حساب زبون)
     */
    fun generateCustomerStatementReport(
        customer: Customer,
        transactions: List<Transaction>,
        transactionItems: Map<Long, List<TransactionItem>>,
        currentDebt: Money,
        settings: Settings,
        isArabic: Boolean = true
    ): Result<File> = runCatching {
        val pdfDoc = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 standard (595 x 842 pt)
        val page = pdfDoc.startPage(pageInfo)
        val canvas = page.canvas

        drawReportHeader(
            canvas = canvas,
            title = if (isArabic) "كشف حساب الزبون" else "Customer Account Statement",
            subtitle = customer.name + if (customer.phone.isNotBlank()) " (${customer.formattedPhoneWithCode})" else "",
            settings = settings,
            isArabic = isArabic
        )

        // Summary Card
        val cardPaint = Paint().apply {
            color = Color.parseColor("#F4F0F5")
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(40f, 130f, 555f, 175f, 8f, 8f, cardPaint)

        val textPaint = Paint().apply {
            color = Color.parseColor("#4A154B")
            textSize = 12f
            isFakeBoldText = true
            textAlign = if (isArabic) Paint.Align.RIGHT else Paint.Align.LEFT
        }

        val debtLabel = if (isArabic) "الرصيد المتبقي المستحق: " else "Outstanding Balance: "
        val debtVal = currentDebt.format(isArabic)
        if (isArabic) {
            canvas.drawText("$debtLabel $debtVal", 535f, 158f, textPaint)
        } else {
            canvas.drawText("$debtLabel $debtVal", 60f, 158f, textPaint)
        }

        // Table Header
        var currentY = 205f
        drawTableHeader(
            canvas = canvas,
            y = currentY,
            col1 = if (isArabic) "التاريخ" else "Date",
            col2 = if (isArabic) "نوع العملية" else "Type",
            col3 = if (isArabic) "البيان / الأصناف" else "Description / Items",
            col4 = if (isArabic) "المبلغ" else "Amount",
            isArabic = isArabic
        )

        currentY += 24f
        val rowPaint = Paint().apply {
            textSize = 10f
            color = Color.DKGRAY
            textAlign = if (isArabic) Paint.Align.RIGHT else Paint.Align.LEFT
        }

        val linePaint = Paint().apply {
            color = Color.LTGRAY
            strokeWidth = 0.75f
        }

        for (tx in transactions) {
            val dateStr = formatShortDate(tx.createdAt)
            val typeStr = when (tx.type) {
                TransactionType.CREDIT_PURCHASE -> if (isArabic) "شراء آجل" else "Credit Purchase"
                TransactionType.CASH_PURCHASE -> if (isArabic) "شراء نقدي" else "Cash Purchase"
                TransactionType.PAYMENT -> if (isArabic) "دفعة سداد" else "Payment"
                else -> tx.type
            }

            val items = transactionItems[tx.id] ?: emptyList()
            val desc = if (items.isNotEmpty()) {
                items.joinToString(", ") { "${it.productNameSnapshot} (${it.quantity})" }
            } else {
                tx.note.ifBlank { if (tx.isPayment) (if (isArabic) "سداد نقدي" else "Cash Payment") else "-" }
            }

            val amountStr = tx.totalAmount.format(isArabic)

            if (isArabic) {
                canvas.drawText(dateStr, 535f, currentY, rowPaint)
                canvas.drawText(typeStr, 440f, currentY, rowPaint)
                canvas.drawText(desc.take(30), 340f, currentY, rowPaint)
                canvas.drawText(amountStr, 120f, currentY, rowPaint)
            } else {
                canvas.drawText(dateStr, 60f, currentY, rowPaint)
                canvas.drawText(typeStr, 155f, currentY, rowPaint)
                canvas.drawText(desc.take(30), 255f, currentY, rowPaint)
                canvas.drawText(amountStr, 475f, currentY, rowPaint)
            }

            canvas.drawLine(40f, currentY + 6f, 555f, currentY + 6f, linePaint)
            currentY += 22f

            if (currentY > 780f) break
        }

        pdfDoc.finishPage(page)

        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val fileName = "customer_statement_${customer.id}_$timeStamp.pdf"
        val reportsDir = File(context.filesDir, "reports").apply { if (!exists()) mkdirs() }
        val file = File(reportsDir, fileName)

        FileOutputStream(file).use { out -> pdfDoc.writeTo(out) }
        pdfDoc.close()
        file
    }

    /**
     * 2. All Customers Debt Report (كشف ديون جميع الزبائن)
     */
    fun generateAllCustomersDebtReport(
        customersWithDebt: List<CustomerWithDebt>,
        settings: Settings,
        isArabic: Boolean = true
    ): Result<File> = runCatching {
        val pdfDoc = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDoc.startPage(pageInfo)
        val canvas = page.canvas

        drawReportHeader(
            canvas = canvas,
            title = if (isArabic) "كشف ديون جميع الزبائن" else "All Customers Outstanding Debt Report",
            subtitle = if (isArabic) "تقرير شامل لجميع الأرصدة والديون المستحقة" else "Summary of all outstanding accounts",
            settings = settings,
            isArabic = isArabic
        )

        var currentY = 140f
        drawTableHeader(
            canvas = canvas,
            y = currentY,
            col1 = if (isArabic) "#" else "#",
            col2 = if (isArabic) "اسم الزبون" else "Customer Name",
            col3 = if (isArabic) "رقم الهاتف" else "Phone",
            col4 = if (isArabic) "الرصيد القائم" else "Balance",
            isArabic = isArabic
        )

        currentY += 24f
        val rowPaint = Paint().apply {
            textSize = 10.5f
            color = Color.DKGRAY
            textAlign = if (isArabic) Paint.Align.RIGHT else Paint.Align.LEFT
        }

        val linePaint = Paint().apply {
            color = Color.LTGRAY
            strokeWidth = 0.75f
        }

        var index = 1
        var totalDebt = Money.ZERO

        for (item in customersWithDebt) {
            totalDebt += item.outstandingDebt
            val numStr = "$index"
            val nameStr = item.customer.name
            val phoneStr = item.customer.phone.ifBlank { "-" }
            val amountStr = item.outstandingDebt.format(isArabic)

            if (isArabic) {
                canvas.drawText(numStr, 535f, currentY, rowPaint)
                canvas.drawText(nameStr, 490f, currentY, rowPaint)
                canvas.drawText(phoneStr, 290f, currentY, rowPaint)
                canvas.drawText(amountStr, 120f, currentY, rowPaint)
            } else {
                canvas.drawText(numStr, 60f, currentY, rowPaint)
                canvas.drawText(nameStr, 105f, currentY, rowPaint)
                canvas.drawText(phoneStr, 305f, currentY, rowPaint)
                canvas.drawText(amountStr, 475f, currentY, rowPaint)
            }

            canvas.drawLine(40f, currentY + 6f, 555f, currentY + 6f, linePaint)
            currentY += 22f
            index++

            if (currentY > 750f) break
        }

        // Total Footer
        val totalPaint = Paint().apply {
            color = Color.parseColor("#4A154B")
            textSize = 12f
            isFakeBoldText = true
            textAlign = if (isArabic) Paint.Align.RIGHT else Paint.Align.LEFT
        }
        val totalStr = (if (isArabic) "إجمالي ديون المحل: " else "Total Store Debt: ") + totalDebt.format(isArabic)
        if (isArabic) {
            canvas.drawText(totalStr, 535f, currentY + 25f, totalPaint)
        } else {
            canvas.drawText(totalStr, 60f, currentY + 25f, totalPaint)
        }

        pdfDoc.finishPage(page)

        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val fileName = "all_debts_report_$timeStamp.pdf"
        val reportsDir = File(context.filesDir, "reports").apply { if (!exists()) mkdirs() }
        val file = File(reportsDir, fileName)

        FileOutputStream(file).use { out -> pdfDoc.writeTo(out) }
        pdfDoc.close()
        file
    }

    /**
     * 3. Product Sales Report (تقرير مبيعات وحركة صنف)
     */
    fun generateProductSalesReport(
        product: Product,
        items: List<TransactionItem>,
        settings: Settings,
        isArabic: Boolean = true
    ): Result<File> = runCatching {
        val pdfDoc = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDoc.startPage(pageInfo)
        val canvas = page.canvas

        drawReportHeader(
            canvas = canvas,
            title = if (isArabic) "تقرير حركة ومبيعات الصنف" else "Product Sales Report",
            subtitle = product.name + " (" + product.price.format(isArabic) + ")",
            settings = settings,
            isArabic = isArabic
        )

        var currentY = 140f
        drawTableHeader(
            canvas = canvas,
            y = currentY,
            col1 = if (isArabic) "#" else "#",
            col2 = if (isArabic) "سعر البيع التاريخي" else "Snapshot Price",
            col3 = if (isArabic) "الكمية المباعة" else "Quantity",
            col4 = if (isArabic) "المجموع الفرعي" else "Subtotal",
            isArabic = isArabic
        )

        currentY += 24f
        val rowPaint = Paint().apply {
            textSize = 10.5f
            color = Color.DKGRAY
            textAlign = if (isArabic) Paint.Align.RIGHT else Paint.Align.LEFT
        }

        val linePaint = Paint().apply {
            color = Color.LTGRAY
            strokeWidth = 0.75f
        }

        var totalQty = 0.0
        var totalRev = Money.ZERO
        var idx = 1

        for (item in items) {
            totalQty += item.quantity
            totalRev += item.subtotal

            val numStr = "$idx"
            val priceStr = item.unitPrice.format(isArabic)
            val qtyStr = "${item.quantity}"
            val subStr = item.subtotal.format(isArabic)

            if (isArabic) {
                canvas.drawText(numStr, 535f, currentY, rowPaint)
                canvas.drawText(priceStr, 440f, currentY, rowPaint)
                canvas.drawText(qtyStr, 300f, currentY, rowPaint)
                canvas.drawText(subStr, 120f, currentY, rowPaint)
            } else {
                canvas.drawText(numStr, 60f, currentY, rowPaint)
                canvas.drawText(priceStr, 155f, currentY, rowPaint)
                canvas.drawText(qtyStr, 295f, currentY, rowPaint)
                canvas.drawText(subStr, 475f, currentY, rowPaint)
            }

            canvas.drawLine(40f, currentY + 6f, 555f, currentY + 6f, linePaint)
            currentY += 22f
            idx++

            if (currentY > 750f) break
        }

        pdfDoc.finishPage(page)

        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val fileName = "product_sales_${product.id}_$timeStamp.pdf"
        val reportsDir = File(context.filesDir, "reports").apply { if (!exists()) mkdirs() }
        val file = File(reportsDir, fileName)

        FileOutputStream(file).use { out -> pdfDoc.writeTo(out) }
        pdfDoc.close()
        file
    }

    /**
     * 4. All Products Catalog Report (دليل أسعار الأصناف)
     */
    fun generateAllProductsCatalogReport(
        products: List<Product>,
        settings: Settings,
        isArabic: Boolean = true
    ): Result<File> = runCatching {
        val pdfDoc = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDoc.startPage(pageInfo)
        val canvas = page.canvas

        drawReportHeader(
            canvas = canvas,
            title = if (isArabic) "دليل أسعار أصناف المحل" else "Store Products Price Catalog",
            subtitle = if (isArabic) "قائمة الأصناف والأسعار الحالية" else "Complete Product Price Directory",
            settings = settings,
            isArabic = isArabic
        )

        var currentY = 140f
        drawTableHeader(
            canvas = canvas,
            y = currentY,
            col1 = if (isArabic) "#" else "#",
            col2 = if (isArabic) "اسم الصنف" else "Product Name",
            col3 = if (isArabic) "الحالة" else "Status",
            col4 = if (isArabic) "السعر الافتراضي" else "Default Price",
            isArabic = isArabic
        )

        currentY += 24f
        val rowPaint = Paint().apply {
            textSize = 10.5f
            color = Color.DKGRAY
            textAlign = if (isArabic) Paint.Align.RIGHT else Paint.Align.LEFT
        }

        val linePaint = Paint().apply {
            color = Color.LTGRAY
            strokeWidth = 0.75f
        }

        var idx = 1
        for (prod in products) {
            val numStr = "$idx"
            val nameStr = prod.name
            val statusStr = if (prod.isActive) (if (isArabic) "نشط" else "Active") else (if (isArabic) "مؤرشف" else "Archived")
            val priceStr = prod.price.format(isArabic)

            if (isArabic) {
                canvas.drawText(numStr, 535f, currentY, rowPaint)
                canvas.drawText(nameStr, 480f, currentY, rowPaint)
                canvas.drawText(statusStr, 280f, currentY, rowPaint)
                canvas.drawText(priceStr, 120f, currentY, rowPaint)
            } else {
                canvas.drawText(numStr, 60f, currentY, rowPaint)
                canvas.drawText(nameStr, 115f, currentY, rowPaint)
                canvas.drawText(statusStr, 315f, currentY, rowPaint)
                canvas.drawText(priceStr, 475f, currentY, rowPaint)
            }

            canvas.drawLine(40f, currentY + 6f, 555f, currentY + 6f, linePaint)
            currentY += 22f
            idx++

            if (currentY > 750f) break
        }

        pdfDoc.finishPage(page)

        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val fileName = "products_catalog_$timeStamp.pdf"
        val reportsDir = File(context.filesDir, "reports").apply { if (!exists()) mkdirs() }
        val file = File(reportsDir, fileName)

        FileOutputStream(file).use { out -> pdfDoc.writeTo(out) }
        pdfDoc.close()
        file
    }

    // ==========================================
    // Internal Drawing Helpers
    // ==========================================

    private fun drawReportHeader(
        canvas: Canvas,
        title: String,
        subtitle: String,
        settings: Settings,
        isArabic: Boolean
    ) {
        val bannerPaint = Paint().apply {
            color = Color.parseColor("#4A154B")
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, 595f, 100f, bannerPaint)

        val storeTitlePaint = Paint().apply {
            color = Color.WHITE
            textSize = 18f
            isFakeBoldText = true
            textAlign = if (isArabic) Paint.Align.RIGHT else Paint.Align.LEFT
        }

        val reportTitlePaint = Paint().apply {
            color = Color.parseColor("#F4F0F5")
            textSize = 13f
            textAlign = if (isArabic) Paint.Align.RIGHT else Paint.Align.LEFT
        }

        val subPaint = Paint().apply {
            color = Color.parseColor("#E0D8E4")
            textSize = 10f
            textAlign = if (isArabic) Paint.Align.RIGHT else Paint.Align.LEFT
        }

        if (isArabic) {
            canvas.drawText(settings.storeName, 555f, 40f, storeTitlePaint)
            canvas.drawText(title, 555f, 65f, reportTitlePaint)
            canvas.drawText(subtitle, 555f, 85f, subPaint)
        } else {
            canvas.drawText(settings.storeName, 40f, 40f, storeTitlePaint)
            canvas.drawText(title, 40f, 65f, reportTitlePaint)
            canvas.drawText(subtitle, 40f, 85f, subPaint)
        }
    }

    private fun drawTableHeader(
        canvas: Canvas,
        y: Float,
        col1: String,
        col2: String,
        col3: String,
        col4: String,
        isArabic: Boolean
    ) {
        val bgPaint = Paint().apply {
            color = Color.parseColor("#4A154B")
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(40f, y - 14f, 555f, y + 10f, 4f, 4f, bgPaint)

        val textPaint = Paint().apply {
            color = Color.WHITE
            textSize = 10.5f
            isFakeBoldText = true
            textAlign = if (isArabic) Paint.Align.RIGHT else Paint.Align.LEFT
        }

        if (isArabic) {
            canvas.drawText(col1, 535f, y, textPaint)
            canvas.drawText(col2, 450f, y, textPaint)
            canvas.drawText(col3, 300f, y, textPaint)
            canvas.drawText(col4, 120f, y, textPaint)
        } else {
            canvas.drawText(col1, 60f, y, textPaint)
            canvas.drawText(col2, 145f, y, textPaint)
            canvas.drawText(col3, 295f, y, textPaint)
            canvas.drawText(col4, 475f, y, textPaint)
        }
    }

    private fun formatShortDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("yyyy/MM/dd", Locale.US)
        return sdf.format(Date(timestamp))
    }
}
