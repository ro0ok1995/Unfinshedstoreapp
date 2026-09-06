package com.example.ui.screens.analysis

import android.content.Intent
import android.net.Uri
import android.widget.Toast
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.model.Customer
import com.example.core.model.Product
import com.example.data.localization.LocalStrings
import com.example.data.localization.formatDateOnly
import com.example.ui.theme.LocalAppThemeColors
import com.example.ui.viewmodel.ShopViewModel

enum class ReportKind {
    CUSTOMER_STATEMENT,
    ALL_DEBTS_SUMMARY,
    PRODUCT_SALES,
    PRODUCTS_CATALOG
}

enum class ReportPeriod {
    ALL_TIME,
    THIS_MONTH,
    THIS_WEEK,
    TODAY,
    CUSTOM
}

/**
 * Tab 3: Reports Flow
 * Implementation of:
 * Report Type -> Options -> Report-specific Period -> Preview -> Generate / Share / Print
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsFlowContent(
    viewModel: ShopViewModel,
    initialCustomer: Customer? = null,
    modifier: Modifier = Modifier
) {
    val strings = LocalStrings.current
    val themeColors = LocalAppThemeColors.current
    val context = LocalContext.current

    val activeCustomers by viewModel.allActiveCustomers.collectAsStateWithLifecycle()
    val activeProducts by viewModel.activeProducts.collectAsStateWithLifecycle()
    val isExportingPdf by viewModel.isExportingPdf.collectAsStateWithLifecycle()

    var selectedReportKind by remember { mutableStateOf(ReportKind.CUSTOMER_STATEMENT) }
    var selectedCustomer by remember(initialCustomer) { mutableStateOf<Customer?>(initialCustomer) }
    var selectedProduct by remember { mutableStateOf<Product?>(null) }
    var selectedPeriod by remember { mutableStateOf(ReportPeriod.THIS_MONTH) }
    var customStartDate by remember { mutableStateOf<Long?>(null) }
    var customEndDate by remember { mutableStateOf<Long?>(null) }

    var showCustomerPicker by remember { mutableStateOf(false) }
    var showProductPicker by remember { mutableStateOf(false) }
    var showDatePickerDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .padding(bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // STEP 1: REPORT TYPE SELECTION
        Text(
            text = "1. ${strings.reportCustomerStatement}",
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                color = themeColors.primary
            )
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ReportTypeCard(
                title = strings.reportCustomerStatement,
                icon = Icons.Default.People,
                isSelected = selectedReportKind == ReportKind.CUSTOMER_STATEMENT,
                onClick = { selectedReportKind = ReportKind.CUSTOMER_STATEMENT },
                testTag = "report_type_customer_statement"
            )

            ReportTypeCard(
                title = strings.reportAllCustomersDebts,
                icon = Icons.Default.Assessment,
                isSelected = selectedReportKind == ReportKind.ALL_DEBTS_SUMMARY,
                onClick = { selectedReportKind = ReportKind.ALL_DEBTS_SUMMARY },
                testTag = "report_type_all_debts"
            )

            ReportTypeCard(
                title = strings.reportProductSales,
                icon = Icons.Default.Inventory2,
                isSelected = selectedReportKind == ReportKind.PRODUCT_SALES,
                onClick = { selectedReportKind = ReportKind.PRODUCT_SALES },
                testTag = "report_type_product_sales"
            )

            ReportTypeCard(
                title = strings.reportAllProductsCatalog,
                icon = Icons.Default.Description,
                isSelected = selectedReportKind == ReportKind.PRODUCTS_CATALOG,
                onClick = { selectedReportKind = ReportKind.PRODUCTS_CATALOG },
                testTag = "report_type_catalog"
            )
        }

        // STEP 2: REPORT OPTIONS (Specific Customer or Product Target)
        if (selectedReportKind == ReportKind.CUSTOMER_STATEMENT || selectedReportKind == ReportKind.PRODUCT_SALES) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "2. " + if (selectedReportKind == ReportKind.CUSTOMER_STATEMENT) strings.customer else strings.product,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    if (selectedReportKind == ReportKind.CUSTOMER_STATEMENT) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { showCustomerPicker = true }
                                .border(1.dp, themeColors.primary.copy(alpha = 0.4f), RoundedCornerShape(10.dp)),
                            color = themeColors.primaryContainer.copy(alpha = 0.2f)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = themeColors.primary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = selectedCustomer?.name ?: strings.selectCustomerPrompt,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Text(
                                    text = strings.edit,
                                    fontSize = 12.sp,
                                    color = themeColors.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    } else {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { showProductPicker = true }
                                .border(1.dp, themeColors.primary.copy(alpha = 0.4f), RoundedCornerShape(10.dp)),
                            color = themeColors.primaryContainer.copy(alpha = 0.2f)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Inventory2,
                                        contentDescription = null,
                                        tint = themeColors.primary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = selectedProduct?.name ?: strings.selectProduct,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Text(
                                    text = strings.edit,
                                    fontSize = 12.sp,
                                    color = themeColors.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // STEP 3: REPORT-SPECIFIC PERIOD SELECTOR
        if (selectedReportKind != ReportKind.PRODUCTS_CATALOG) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "3. ${strings.sectionPeriodAnalysis}",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilterChip(
                            selected = selectedPeriod == ReportPeriod.THIS_MONTH,
                            onClick = { selectedPeriod = ReportPeriod.THIS_MONTH },
                            label = { Text(strings.periodThisMonth, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = themeColors.primary,
                                selectedLabelColor = Color.White
                            )
                        )
                        FilterChip(
                            selected = selectedPeriod == ReportPeriod.THIS_WEEK,
                            onClick = { selectedPeriod = ReportPeriod.THIS_WEEK },
                            label = { Text(strings.periodThisWeek, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = themeColors.primary,
                                selectedLabelColor = Color.White
                            )
                        )
                        FilterChip(
                            selected = selectedPeriod == ReportPeriod.TODAY,
                            onClick = { selectedPeriod = ReportPeriod.TODAY },
                            label = { Text(strings.periodToday, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = themeColors.primary,
                                selectedLabelColor = Color.White
                            )
                        )
                        FilterChip(
                            selected = selectedPeriod == ReportPeriod.ALL_TIME,
                            onClick = { selectedPeriod = ReportPeriod.ALL_TIME },
                            label = { Text(strings.periodAllTime, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = themeColors.primary,
                                selectedLabelColor = Color.White
                            )
                        )
                        FilterChip(
                            selected = selectedPeriod == ReportPeriod.CUSTOM,
                            onClick = {
                                selectedPeriod = ReportPeriod.CUSTOM
                                showDatePickerDialog = true
                            },
                            label = { Text(strings.periodCustom, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = themeColors.primary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }
        }

        // STEP 4: PREVIEW CARD
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(
                containerColor = themeColors.primaryContainer.copy(alpha = 0.35f)
            ),
            border = border(themeColors)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Visibility,
                        contentDescription = null,
                        tint = themeColors.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "4. " + strings.previewTitle,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = themeColors.primary
                        )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                val previewTitle = when (selectedReportKind) {
                    ReportKind.CUSTOMER_STATEMENT -> "${strings.reportCustomerStatement}: ${selectedCustomer?.name ?: strings.allCustomersMode}"
                    ReportKind.ALL_DEBTS_SUMMARY -> strings.reportAllCustomersDebts
                    ReportKind.PRODUCT_SALES -> "${strings.reportProductSales}: ${selectedProduct?.name ?: strings.all}"
                    ReportKind.PRODUCTS_CATALOG -> strings.reportAllProductsCatalog
                }

                val periodLabel = when (selectedPeriod) {
                    ReportPeriod.ALL_TIME -> strings.periodAllTime
                    ReportPeriod.THIS_MONTH -> strings.periodThisMonth
                    ReportPeriod.THIS_WEEK -> strings.periodThisWeek
                    ReportPeriod.TODAY -> strings.periodToday
                    ReportPeriod.CUSTOM -> "${formatDateOnly(customStartDate ?: 0L)} - ${formatDateOnly(customEndDate ?: 0L)}"
                }

                Text(
                    text = previewTitle,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${strings.sectionPeriodAnalysis}: $periodLabel",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // STEP 5: GENERATE / SHARE / PRINT ACTIONS
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Main Generate Action
            Button(
                onClick = {
                    when (selectedReportKind) {
                        ReportKind.CUSTOMER_STATEMENT -> {
                            val cust = selectedCustomer ?: activeCustomers.firstOrNull()
                            if (cust != null) {
                                viewModel.generateCustomerStatementReport(cust)
                            } else {
                                Toast.makeText(context, strings.selectCustomerPrompt, Toast.LENGTH_SHORT).show()
                            }
                        }
                        ReportKind.ALL_DEBTS_SUMMARY -> {
                            viewModel.generateDebtsSummaryReport()
                        }
                        ReportKind.PRODUCT_SALES -> {
                            val prod = selectedProduct ?: activeProducts.firstOrNull()
                            if (prod != null) {
                                viewModel.generateProductSalesReport(prod)
                            } else {
                                Toast.makeText(context, strings.selectProduct, Toast.LENGTH_SHORT).show()
                            }
                        }
                        ReportKind.PRODUCTS_CATALOG -> {
                            viewModel.generateProductsCatalogReport()
                        }
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("report_generate_pdf_btn"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = themeColors.primary)
            ) {
                if (isExportingPdf) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.PictureAsPdf,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = strings.generatePdf,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }

            // Share Action
            OutlinedButton(
                onClick = {
                    Toast.makeText(context, strings.share, Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier
                    .height(48.dp)
                    .testTag("report_share_btn"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = strings.share,
                    tint = themeColors.primary,
                    modifier = Modifier.size(18.dp)
                )
            }

            // Print Action
            OutlinedButton(
                onClick = {
                    Toast.makeText(context, strings.print, Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier
                    .height(48.dp)
                    .testTag("report_print_btn"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Print,
                    contentDescription = strings.print,
                    tint = themeColors.primary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }

    // Modal Customer Picker
    if (showCustomerPicker) {
        ModalBottomSheet(
            onDismissRequest = { showCustomerPicker = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .padding(bottom = 24.dp)
            ) {
                Text(
                    text = strings.selectCustomerPrompt,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().height(300.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(activeCustomers, key = { it.id }) { customer ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    selectedCustomer = customer
                                    showCustomerPicker = false
                                },
                            color = if (selectedCustomer?.id == customer.id) themeColors.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = customer.name,
                                modifier = Modifier.padding(12.dp),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }

    // Modal Product Picker
    if (showProductPicker) {
        ModalBottomSheet(
            onDismissRequest = { showProductPicker = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .padding(bottom = 24.dp)
            ) {
                Text(
                    text = strings.selectProduct,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().height(300.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(activeProducts, key = { it.id }) { product ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    selectedProduct = product
                                    showProductPicker = false
                                },
                            color = if (selectedProduct?.id == product.id) themeColors.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = product.name,
                                modifier = Modifier.padding(12.dp),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }

    // Date Range Picker Dialog
    if (showDatePickerDialog) {
        DateRangePickerDialog(
            currentStart = customStartDate,
            currentEnd = customEndDate,
            onDismiss = { showDatePickerDialog = false },
            onApply = { start, end ->
                customStartDate = start
                customEndDate = end
                showDatePickerDialog = false
            }
        )
    }
}

@Composable
private fun ReportTypeCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    val themeColors = LocalAppThemeColors.current

    Surface(
        modifier = modifier
            .width(130.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .testTag(testTag),
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) themeColors.primary else MaterialTheme.colorScheme.surface,
        border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) Color.White else themeColors.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                maxLines = 2
            )
        }
    }
}

@Composable
private fun border(themeColors: com.example.ui.theme.AppThemeColors) =
    androidx.compose.foundation.BorderStroke(1.dp, themeColors.primary.copy(alpha = 0.3f))
