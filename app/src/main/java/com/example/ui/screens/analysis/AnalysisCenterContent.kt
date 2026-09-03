package com.example.ui.screens.analysis

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.business.AnalysisMetrics
import com.example.core.business.AnalysisPeriod
import com.example.core.model.Customer
import com.example.data.localization.LocalStrings
import com.example.data.localization.formatDateOnly
import com.example.ui.theme.FinancialCash
import com.example.ui.theme.FinancialDebt
import com.example.ui.theme.FinancialPayment
import com.example.ui.theme.LocalAppThemeColors
import com.example.ui.viewmodel.ShopViewModel

@Composable
fun AnalysisCenterContent(
    viewModel: ShopViewModel,
    selectedCustomer: Customer?,
    modifier: Modifier = Modifier
) {
    val strings = LocalStrings.current
    val themeColors = LocalAppThemeColors.current

    val analysisMetrics by viewModel.analysisMetrics.collectAsStateWithLifecycle()
    val currentPeriod by viewModel.analysisPeriod.collectAsStateWithLifecycle()
    val customStart by viewModel.analysisCustomStartDate.collectAsStateWithLifecycle()
    val customEnd by viewModel.analysisCustomEndDate.collectAsStateWithLifecycle()

    var showDatePickerDialog by remember { mutableStateOf(false) }

    if (showDatePickerDialog) {
        DateRangePickerDialog(
            currentStart = customStart,
            currentEnd = customEnd,
            onDismiss = { showDatePickerDialog = false },
            onConfirm = { start, end ->
                viewModel.setAnalysisCustomDateRange(start, end)
                showDatePickerDialog = false
            }
        )
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 120.dp)
    ) {
        // ==========================================
        // 1. Period Filter Chips (Independent State)
        // ==========================================
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, bottom = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = strings.sectionPeriodAnalysis,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (currentPeriod == AnalysisPeriod.CUSTOM && customStart != null && customEnd != null) {
                        Text(
                            text = "${formatDateOnly(customStart!!)} - ${formatDateOnly(customEnd!!)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = themeColors.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                val scrollState = rememberScrollState()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(scrollState)
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PeriodFilterChip(
                        label = strings.periodToday,
                        isSelected = currentPeriod == AnalysisPeriod.TODAY,
                        testTag = "period_chip_today",
                        onClick = { viewModel.setAnalysisPeriod(AnalysisPeriod.TODAY) }
                    )
                    PeriodFilterChip(
                        label = strings.periodThisWeek,
                        isSelected = currentPeriod == AnalysisPeriod.THIS_WEEK,
                        testTag = "period_chip_week",
                        onClick = { viewModel.setAnalysisPeriod(AnalysisPeriod.THIS_WEEK) }
                    )
                    PeriodFilterChip(
                        label = strings.periodThisMonth,
                        isSelected = currentPeriod == AnalysisPeriod.THIS_MONTH,
                        testTag = "period_chip_month",
                        onClick = { viewModel.setAnalysisPeriod(AnalysisPeriod.THIS_MONTH) }
                    )
                    PeriodFilterChip(
                        label = strings.periodCustom,
                        isSelected = currentPeriod == AnalysisPeriod.CUSTOM,
                        testTag = "period_chip_custom",
                        onClick = {
                            viewModel.setAnalysisPeriod(AnalysisPeriod.CUSTOM)
                            showDatePickerDialog = true
                        }
                    )
                    PeriodFilterChip(
                        label = strings.periodAllTime,
                        isSelected = currentPeriod == AnalysisPeriod.ALL_TIME,
                        testTag = "period_chip_all",
                        onClick = { viewModel.setAnalysisPeriod(AnalysisPeriod.ALL_TIME) }
                    )
                }

                // If custom period is selected, provide easy button to change dates
                if (currentPeriod == AnalysisPeriod.CUSTOM) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { showDatePickerDialog = true }
                                .border(1.dp, themeColors.primary.copy(alpha = 0.5f), RoundedCornerShape(10.dp)),
                            color = themeColors.primaryContainer.copy(alpha = 0.3f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarMonth,
                                        contentDescription = null,
                                        tint = themeColors.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (customStart != null && customEnd != null) {
                                            "النطاق: ${formatDateOnly(customStart!!)} إلى ${formatDateOnly(customEnd!!)}"
                                        } else {
                                            strings.selectDateRange
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                Text(
                                    text = "تعديل",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = themeColors.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // ==========================================
        // 2. Section: Overview
        // ==========================================
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                AnalysisOverviewCard(
                    metrics = analysisMetrics,
                    selectedCustomer = selectedCustomer,
                    onExportReport = {
                        if (selectedCustomer != null) {
                            viewModel.generateCustomerStatementReport(selectedCustomer)
                        } else {
                            viewModel.generateDebtsSummaryReport()
                        }
                    }
                )
            }
        }

        // ==========================================
        // 3. Section: Financial Statistics Grid
        // ==========================================
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                SectionHeader(title = strings.sectionFinancialStats)
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricTile(
                        modifier = Modifier.weight(1f),
                        title = strings.totalSalesLabel,
                        value = analysisMetrics.totalSales.format(),
                        count = "${analysisMetrics.cashSalesCount + analysisMetrics.creditSalesCount} عملية",
                        icon = Icons.Default.Sell,
                        color = themeColors.primary,
                        testTag = "metric_total_sales"
                    )
                    MetricTile(
                        modifier = Modifier.weight(1f),
                        title = strings.paymentsCollectedLabel,
                        value = analysisMetrics.paymentsCollected.format(),
                        count = "${analysisMetrics.paymentsCount} دفعة",
                        icon = Icons.Default.Payments,
                        color = FinancialPayment,
                        testTag = "metric_payments_collected"
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricTile(
                        modifier = Modifier.weight(1f),
                        title = strings.cashSalesLabel,
                        value = analysisMetrics.cashSales.format(),
                        count = "${analysisMetrics.cashSalesCount} فواتير",
                        icon = Icons.Default.ShoppingBag,
                        color = FinancialCash,
                        testTag = "metric_cash_sales"
                    )
                    MetricTile(
                        modifier = Modifier.weight(1f),
                        title = strings.creditSalesLabel,
                        value = analysisMetrics.creditSales.format(),
                        count = "${analysisMetrics.creditSalesCount} فواتير",
                        icon = Icons.Default.ReceiptLong,
                        color = FinancialDebt,
                        testTag = "metric_credit_sales"
                    )
                }
            }
        }

        // ==========================================
        // 4. Section: Visual Analysis (Charts)
        // ==========================================
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SectionHeader(title = "التحليل البصري والمخططات")

                // Donut Chart
                DonutBreakdownChart(metrics = analysisMetrics)

                // Sales vs Payments Progress
                SalesVsPaymentsComparison(metrics = analysisMetrics)

                // Debt Health Overview
                DebtHealthOverview(metrics = analysisMetrics)
            }
        }

        // ==========================================
        // 5. Section: Customer Debt & Counts
        // ==========================================
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                SectionHeader(title = strings.sectionCustomerDebt)
                Spacer(modifier = Modifier.height(10.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("customer_debt_analysis_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        DetailRowItem(
                            icon = Icons.Default.AccountBalanceWallet,
                            iconTint = FinancialDebt,
                            title = strings.outstandingDebt,
                            value = analysisMetrics.outstandingCustomerDebt.format(),
                            isBold = true,
                            textColor = FinancialDebt
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                        DetailRowItem(
                            icon = Icons.Default.Group,
                            iconTint = themeColors.primary,
                            title = strings.totalCustomersLabel,
                            value = "${analysisMetrics.totalCustomersCount} زبون",
                            isBold = false
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                        DetailRowItem(
                            icon = Icons.Default.People,
                            iconTint = FinancialPayment,
                            title = strings.activeCustomersLabel,
                            value = "${analysisMetrics.activeCustomersCount} زبون مدين",
                            isBold = false
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                        DetailRowItem(
                            icon = Icons.Default.TrendingUp,
                            iconTint = themeColors.secondary,
                            title = strings.averageDebtLabel,
                            value = analysisMetrics.averageDebt.format(),
                            isBold = false
                        )
                    }
                }
            }
        }

        // ==========================================
        // 6. Section: Transactions Activity
        // ==========================================
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                SectionHeader(title = strings.sectionTransactions)
                Spacer(modifier = Modifier.height(10.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("transactions_activity_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        DetailRowItem(
                            icon = Icons.Default.Receipt,
                            iconTint = themeColors.primary,
                            title = strings.allTransactions,
                            value = "${analysisMetrics.totalTransactionsCount} معاملة منجزة",
                            isBold = true
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                        DetailRowItem(
                            icon = Icons.Default.ShoppingBag,
                            iconTint = FinancialCash,
                            title = "مبيعات نقدية مكتملة",
                            value = "${analysisMetrics.cashSalesCount} معاملة",
                            isBold = false
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                        DetailRowItem(
                            icon = Icons.Default.ReceiptLong,
                            iconTint = FinancialDebt,
                            title = "مبيعات ذمم وآجل مكتملة",
                            value = "${analysisMetrics.creditSalesCount} معاملة",
                            isBold = false
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                        DetailRowItem(
                            icon = Icons.Default.Payments,
                            iconTint = FinancialPayment,
                            title = "سدادات مقبوضة مكتملة",
                            value = "${analysisMetrics.paymentsCount} دفعة",
                            isBold = false
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = modifier
    )
}

@Composable
private fun PeriodFilterChip(
    label: String,
    isSelected: Boolean,
    testTag: String,
    onClick: () -> Unit
) {
    val themeColors = LocalAppThemeColors.current

    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = {
            Text(
                text = label,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        },
        modifier = Modifier.testTag(testTag),
        shape = RoundedCornerShape(10.dp),
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = themeColors.primary,
            selectedLabelColor = Color.White,
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
            labelColor = MaterialTheme.colorScheme.onSurface
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = isSelected,
            borderColor = if (isSelected) themeColors.primary else MaterialTheme.colorScheme.outlineVariant
        )
    )
}

@Composable
private fun AnalysisOverviewCard(
    metrics: AnalysisMetrics,
    selectedCustomer: Customer?,
    onExportReport: () -> Unit
) {
    val strings = LocalStrings.current
    val themeColors = LocalAppThemeColors.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("analysis_overview_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = themeColors.primary
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (selectedCustomer != null) {
                            "تحليل حساب: ${selectedCustomer.name}"
                        } else {
                            "ملخص النشاط المالي العام"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "إجمالي حجم التداول المالي للفترة",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }

                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { onExportReport() }
                        .testTag("overview_export_pdf_chip"),
                    color = Color.White.copy(alpha = 0.2f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PictureAsPdf,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "تقرير PDF",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Big Volume Display
            Text(
                text = metrics.totalVolume.format(),
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = Color.White.copy(alpha = 0.25f))
            Spacer(modifier = Modifier.height(12.dp))

            // Sub Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "المبيعات الإجمالية",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Text(
                        text = metrics.totalSales.format(),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Column {
                    Text(
                        text = "التحصيلات النقدية",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Text(
                        text = metrics.paymentsCollected.format(),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Column {
                    Text(
                        text = "الديون القائمة",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Text(
                        text = metrics.outstandingCustomerDebt.format(),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun MetricTile(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    count: String,
    icon: ImageVector,
    color: Color,
    testTag: String
) {
    Card(
        modifier = modifier.testTag(testTag),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(color.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Text(
                    text = count,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun DetailRowItem(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    value: String,
    isBold: Boolean = false,
    textColor: Color = Color.Unspecified
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Medium,
            color = if (textColor != Color.Unspecified) textColor else MaterialTheme.colorScheme.onSurface
        )
    }
}
