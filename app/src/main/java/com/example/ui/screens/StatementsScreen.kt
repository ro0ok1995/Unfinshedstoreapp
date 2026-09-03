package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.model.Money
import com.example.core.model.Customer
import com.example.core.model.TransactionStatus
import com.example.core.model.TransactionType
import com.example.core.model.TransactionWithDetails
import com.example.data.localization.LocalStrings
import com.example.data.localization.formatDateOnly
import com.example.data.localization.formatDateTime
import com.example.data.localization.formatTimeOnly
import com.example.ui.components.AppHeader
import com.example.ui.components.CancelTransactionDialog
import com.example.ui.components.RestoreTransactionDialog
import com.example.ui.components.StatusBadge
import com.example.ui.screens.analysis.AnalysisCenterContent
import com.example.ui.theme.LocalAppThemeColors
import com.example.ui.theme.FinancialCancelled
import com.example.ui.theme.FinancialCancelledContainer
import com.example.ui.theme.FinancialCash
import com.example.ui.theme.FinancialCashContainer
import com.example.ui.theme.FinancialDebt
import com.example.ui.theme.FinancialDebtContainer
import com.example.ui.theme.FinancialPayment
import com.example.ui.theme.FinancialPaymentContainer
import com.example.ui.viewmodel.ShopViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatementsScreen(
    viewModel: ShopViewModel,
    modifier: Modifier = Modifier
) {
    val strings = LocalStrings.current
    val themeColors = LocalAppThemeColors.current
    val transactionsWithDetails by viewModel.filteredTransactionsWithDetails.collectAsStateWithLifecycle()
    val searchQuery by viewModel.statementSearchQuery.collectAsStateWithLifecycle()
    val selectedTypeFilter by viewModel.selectedTxTypeFilter.collectAsStateWithLifecycle()
    val selectedCustomer by viewModel.selectedStatementCustomer.collectAsStateWithLifecycle()
    val financialMetrics by viewModel.statementFinancialMetrics.collectAsStateWithLifecycle()
    val allActiveCustomersWithDebt by viewModel.activeCustomersWithDebt.collectAsStateWithLifecycle()
    val selectedTab by viewModel.selectedAnalysisTab.collectAsStateWithLifecycle()
    val isNewestFirst by viewModel.isStatementNewestFirst.collectAsStateWithLifecycle()
    val runningBalances by viewModel.statementRunningBalances.collectAsStateWithLifecycle()

    var cancelTargetTxId by remember { mutableStateOf<Long?>(null) }
    var restoreTargetTxId by remember { mutableStateOf<Long?>(null) }
    var selectedDetailsTx by remember { mutableStateOf<TransactionWithDetails?>(null) }
    var isCustomerDropdownExpanded by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            AppHeader(
                title = if (selectedCustomer != null) {
                    "${strings.customerAccountStatement}: ${selectedCustomer?.name}"
                } else {
                    strings.statementsTitle
                },
                subtitle = if (selectedCustomer != null) {
                    selectedCustomer?.formattedPhoneWithCode?.ifBlank { strings.viewAccountStatementDesc } ?: strings.viewAccountStatementDesc
                } else {
                    "${financialMetrics.totalTransactionsCount} ${strings.allTransactions}"
                },
                onBack = if (selectedCustomer != null) {
                    {
                        viewModel.setSelectedStatementCustomer(null)
                        viewModel.setStatementSearchQuery("")
                    }
                } else null,
                actions = {
                    IconButton(
                        onClick = {
                            if (selectedCustomer != null) {
                                viewModel.generateCustomerStatementReport(selectedCustomer!!)
                            } else {
                                viewModel.generateDebtsSummaryReport()
                            }
                        },
                        modifier = Modifier.testTag("export_statement_pdf_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PictureAsPdf,
                            contentDescription = strings.exportPdf,
                            tint = Color.White
                        )
                    }
                }
            )

            // Customer Context Banner (Whole Shop vs Specific Customer)
            CustomerContextBanner(
                selectedCustomer = selectedCustomer,
                onClearCustomer = {
                    viewModel.setSelectedStatementCustomer(null)
                    viewModel.setStatementSearchQuery("")
                },
                onSelectCustomerClick = {
                    isCustomerDropdownExpanded = true
                }
            )

            // Segmented Tab Switcher (Analysis Center vs Account Statement)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        RoundedCornerShape(12.dp)
                    )
                    .padding(4.dp)
            ) {
                // Tab 1: Analysis Center
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { viewModel.setSelectedAnalysisTab(ShopViewModel.AnalysisScreenTab.ANALYSIS_CENTER) }
                        .testTag("tab_analysis_center"),
                    color = if (selectedTab == ShopViewModel.AnalysisScreenTab.ANALYSIS_CENTER) themeColors.primary else Color.Transparent,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Assessment,
                            contentDescription = null,
                            tint = if (selectedTab == ShopViewModel.AnalysisScreenTab.ANALYSIS_CENTER) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = strings.tabAnalysisCenter,
                            fontSize = 13.sp,
                            fontWeight = if (selectedTab == ShopViewModel.AnalysisScreenTab.ANALYSIS_CENTER) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == ShopViewModel.AnalysisScreenTab.ANALYSIS_CENTER) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Tab 2: Account Statement
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { viewModel.setSelectedAnalysisTab(ShopViewModel.AnalysisScreenTab.ACCOUNT_STATEMENT) }
                        .testTag("tab_account_statement"),
                    color = if (selectedTab == ShopViewModel.AnalysisScreenTab.ACCOUNT_STATEMENT) themeColors.primary else Color.Transparent,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ReceiptLong,
                            contentDescription = null,
                            tint = if (selectedTab == ShopViewModel.AnalysisScreenTab.ACCOUNT_STATEMENT) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = strings.tabAccountStatement,
                            fontSize = 13.sp,
                            fontWeight = if (selectedTab == ShopViewModel.AnalysisScreenTab.ACCOUNT_STATEMENT) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == ShopViewModel.AnalysisScreenTab.ACCOUNT_STATEMENT) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (selectedTab == ShopViewModel.AnalysisScreenTab.ANALYSIS_CENTER) {
                AnalysisCenterContent(
                    viewModel = viewModel,
                    selectedCustomer = selectedCustomer
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 100.dp)
                ) {
                // Section 1: Customer Search & Selector
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        // Customer Search & Dropdown Box
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = {
                                    viewModel.setStatementSearchQuery(it)
                                    isCustomerDropdownExpanded = true
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("statement_customer_search_input"),
                                placeholder = {
                                    Text(
                                        text = if (selectedCustomer != null) {
                                            selectedCustomer!!.name
                                        } else {
                                            strings.searchCustomerStatementPlaceholder
                                        },
                                        fontSize = 14.sp,
                                        color = if (selectedCustomer != null) themeColors.primary else Color.Gray,
                                        fontWeight = if (selectedCustomer != null) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = if (selectedCustomer != null) Icons.Default.Person else Icons.Default.Search,
                                        contentDescription = null,
                                        tint = if (selectedCustomer != null) themeColors.primary else Color.Gray
                                    )
                                },
                                trailingIcon = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (selectedCustomer != null || searchQuery.isNotBlank()) {
                                            IconButton(
                                                onClick = {
                                                    viewModel.setSelectedStatementCustomer(null)
                                                    viewModel.setStatementSearchQuery("")
                                                    isCustomerDropdownExpanded = false
                                                },
                                                modifier = Modifier.testTag("statement_clear_search_btn")
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Clear,
                                                    contentDescription = "Clear",
                                                    tint = Color.Gray,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
                                        IconButton(
                                            onClick = { isCustomerDropdownExpanded = !isCustomerDropdownExpanded },
                                            modifier = Modifier.testTag("statement_customer_dropdown_toggle")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.ArrowDropDown,
                                                contentDescription = "Select customer",
                                                tint = themeColors.primary
                                            )
                                        }
                                    }
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(16.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                                )
                            )

                            // Dropdown Menu for Customers matching search or all
                            DropdownMenu(
                                expanded = isCustomerDropdownExpanded,
                                onDismissRequest = { isCustomerDropdownExpanded = false },
                                modifier = Modifier
                                    .fillMaxWidth(0.92f)
                                    .background(MaterialTheme.colorScheme.surface)
                            ) {
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = "👥 ${strings.allCustomersMode}",
                                            fontWeight = FontWeight.Bold,
                                            color = themeColors.primary,
                                            fontSize = 14.sp
                                        )
                                    },
                                    onClick = {
                                        viewModel.setSelectedStatementCustomer(null)
                                        viewModel.setStatementSearchQuery("")
                                        isCustomerDropdownExpanded = false
                                    }
                                )

                                HorizontalDivider(color = Color(0xFFEEEEEE))

                                val filteredDropdownCustomers = remember(allActiveCustomersWithDebt, searchQuery) {
                                    if (searchQuery.isBlank()) {
                                        allActiveCustomersWithDebt
                                    } else {
                                        val q = searchQuery.trim()
                                        val queryDigits = q.filter { it.isDigit() }
                                        allActiveCustomersWithDebt.filter { item ->
                                            val nameMatch = item.customer.name.contains(q, ignoreCase = true)
                                            val rawPhoneMatch = item.customer.phone.contains(q, ignoreCase = true)
                                            val phoneDigits = item.customer.phone.filter { it.isDigit() }
                                            val digitsMatch = queryDigits.isNotEmpty() && (
                                                phoneDigits.contains(queryDigits) ||
                                                phoneDigits.removePrefix("970").removePrefix("972").contains(queryDigits.removePrefix("0")) ||
                                                phoneDigits.contains(queryDigits.removePrefix("0"))
                                            )
                                            nameMatch || rawPhoneMatch || digitsMatch
                                        }
                                    }
                                }

                                if (filteredDropdownCustomers.isEmpty()) {
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = strings.noCustomersFoundSearch,
                                                color = Color.Gray,
                                                fontSize = 13.sp
                                            )
                                        },
                                        onClick = { isCustomerDropdownExpanded = false }
                                    )
                                } else {
                                    filteredDropdownCustomers.forEach { customerWithDebt ->
                                        DropdownMenuItem(
                                            text = {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Column {
                                                        Text(
                                                            text = customerWithDebt.customer.name,
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 14.sp,
                                                            color = Color.Black
                                                        )
                                                        if (customerWithDebt.customer.phone.isNotBlank()) {
                                                            Text(
                                                                text = customerWithDebt.customer.formattedPhoneWithCode,
                                                                fontSize = 12.sp,
                                                                color = Color.Gray
                                                            )
                                                        }
                                                    }
                                                    if (customerWithDebt.outstandingDebt.isPositive()) {
                                                        Text(
                                                            text = customerWithDebt.outstandingDebt.format(),
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 13.sp,
                                                            color = FinancialDebt
                                                        )
                                                    }
                                                }
                                            },
                                            onClick = {
                                                viewModel.setSelectedStatementCustomer(customerWithDebt.customer)
                                                viewModel.setStatementSearchQuery("")
                                                isCustomerDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        // Selected Customer Badge / Card
                        if (selectedCustomer != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = themeColors.primaryContainer,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(32.dp)
                                                .clip(CircleShape)
                                                .background(themeColors.primary),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Person,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = selectedCustomer!!.name,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = themeColors.primary
                                            )
                                            if (selectedCustomer!!.phone.isNotBlank()) {
                                                Text(
                                                    text = selectedCustomer!!.formattedPhoneWithCode,
                                                    fontSize = 12.sp,
                                                    color = Color.DarkGray
                                                )
                                            }
                                        }
                                    }

                                    TextButton(
                                        onClick = {
                                            viewModel.setSelectedStatementCustomer(null)
                                            viewModel.setStatementSearchQuery("")
                                        },
                                        modifier = Modifier.testTag("btn_clear_customer_mode")
                                    ) {
                                        Text(
                                            text = strings.clearCustomerFilter,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = themeColors.primary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Section 2: Financial Summary Cards (3 key metrics)
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Card 1: Remaining Debt (Red)
                            FinancialMetricCard(
                                title = strings.currentDebt,
                                amount = financialMetrics.outstandingDebt.format(),
                                countText = if (selectedCustomer != null) null else "${allActiveCustomersWithDebt.count { it.outstandingDebt.isPositive() }} ${strings.active}",
                                color = FinancialDebt,
                                containerColor = FinancialDebtContainer,
                                icon = Icons.Default.AccountBalanceWallet,
                                modifier = Modifier.weight(1f),
                                testTag = "summary_card_debt"
                            )

                            // Card 2: Total Purchases
                            FinancialMetricCard(
                                title = strings.totalPurchases,
                                amount = financialMetrics.totalPurchases.format(),
                                countText = "${financialMetrics.completedCreditPurchasesCount} ${strings.creditPurchase}",
                                color = themeColors.primary,
                                containerColor = themeColors.primaryContainer,
                                icon = Icons.Default.CreditCard,
                                modifier = Modifier.weight(1f),
                                testTag = "summary_card_purchases"
                            )

                            // Card 3: Total Payments (Green)
                            FinancialMetricCard(
                                title = strings.totalPayments,
                                amount = financialMetrics.totalPayments.format(),
                                countText = "${financialMetrics.completedPaymentsCount} ${strings.payment}",
                                color = FinancialPayment,
                                containerColor = FinancialPaymentContainer,
                                icon = Icons.Default.AttachMoney,
                                modifier = Modifier.weight(1f),
                                testTag = "summary_card_payments"
                            )
                        }
                    }
                }

                // Section 3: ONE Transaction Type Filter Row ONLY
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        // Section Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = strings.transactionHistory,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = themeColors.primary
                            )
                            Text(
                                text = "${transactionsWithDetails.size} ${strings.details}",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Single Type Filter Chips Row
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            item {
                                FilterChip(
                                    selected = selectedTypeFilter == null,
                                    onClick = { viewModel.setSelectedTxTypeFilter(null) },
                                    label = { Text(strings.all, fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = themeColors.primary,
                                        selectedLabelColor = Color.White
                                    ),
                                    modifier = Modifier.testTag("filter_chip_all")
                                )
                            }
                            item {
                                FilterChip(
                                    selected = selectedTypeFilter == TransactionType.CREDIT_PURCHASE,
                                    onClick = {
                                        viewModel.setSelectedTxTypeFilter(
                                            if (selectedTypeFilter == TransactionType.CREDIT_PURCHASE) null else TransactionType.CREDIT_PURCHASE
                                        )
                                    },
                                    label = { Text(strings.transactionTypeCredit, fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = FinancialDebt,
                                        selectedLabelColor = Color.White
                                    ),
                                    modifier = Modifier.testTag("filter_chip_credit")
                                )
                            }
                            item {
                                FilterChip(
                                    selected = selectedTypeFilter == TransactionType.CASH_PURCHASE,
                                    onClick = {
                                        viewModel.setSelectedTxTypeFilter(
                                            if (selectedTypeFilter == TransactionType.CASH_PURCHASE) null else TransactionType.CASH_PURCHASE
                                        )
                                    },
                                    label = { Text(strings.transactionTypeCash, fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = FinancialCash,
                                        selectedLabelColor = Color.White
                                    ),
                                    modifier = Modifier.testTag("filter_chip_cash")
                                )
                            }
                            item {
                                FilterChip(
                                    selected = selectedTypeFilter == TransactionType.PAYMENT,
                                    onClick = {
                                        viewModel.setSelectedTxTypeFilter(
                                            if (selectedTypeFilter == TransactionType.PAYMENT) null else TransactionType.PAYMENT
                                        )
                                    },
                                    label = { Text(strings.transactionTypePayment, fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = FinancialPayment,
                                        selectedLabelColor = Color.White
                                    ),
                                    modifier = Modifier.testTag("filter_chip_payment")
                                )
                            }
                        }
                    }
                }

                // Section 3.5: Sort Order Switcher
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${transactionsWithDetails.size} ${strings.allTransactions}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { viewModel.setStatementSortOrder(!isNewestFirst) }
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                                .testTag("sort_order_toggle"),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SwapVert,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = themeColors.primary
                                )
                                Text(
                                    text = if (isNewestFirst) strings.orderNewestFirst else strings.orderOldestFirst,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = themeColors.primary
                                )
                            }
                        }
                    }
                }

                // Section 4: Transaction History List
                if (transactionsWithDetails.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = Color.LightGray,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = if (selectedTypeFilter != null) strings.noTransactionsOfType else strings.noTransactionsFound,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                } else {
                    items(transactionsWithDetails, key = { it.transaction.id }) { item ->
                        val txRunningBalance = runningBalances[item.transaction.id]
                        StatementTransactionCard(
                            item = item,
                            runningBalance = txRunningBalance,
                            onMoreDetailsClick = { selectedDetailsTx = item },
                            onCustomerClick = {
                                item.customer?.let { c ->
                                    viewModel.openCustomerDetails(c.id)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

    // Modal Sheet: Transaction "More Details"
    selectedDetailsTx?.let { txDetails ->
        TransactionDetailsBottomSheet(
            txDetails = txDetails,
            onDismiss = { selectedDetailsTx = null },
            onCancelTransaction = {
                val txId = txDetails.transaction.id
                selectedDetailsTx = null
                cancelTargetTxId = txId
            },
            onRestoreTransaction = {
                val txId = txDetails.transaction.id
                selectedDetailsTx = null
                restoreTargetTxId = txId
            },
            onCustomerClick = { customerId ->
                selectedDetailsTx = null
                viewModel.openCustomerDetails(customerId)
            }
        )
    }

    // Cancel Transaction Dialog
    cancelTargetTxId?.let { txId ->
        CancelTransactionDialog(
            transactionId = txId,
            onDismiss = { cancelTargetTxId = null },
            onConfirm = { reason ->
                viewModel.cancelTransaction(txId, reason) {
                    cancelTargetTxId = null
                }
            }
        )
    }

    // Restore Transaction Dialog
    restoreTargetTxId?.let { txId ->
        RestoreTransactionDialog(
            transactionId = txId,
            onDismiss = { restoreTargetTxId = null },
            onConfirm = {
                viewModel.restoreTransaction(txId) {
                    restoreTargetTxId = null
                }
            }
        )
    }
}

/**
 * Top 3 Financial Metric Cards (Debt, Purchases, Payments)
 */
@Composable
private fun FinancialMetricCard(
    title: String,
    amount: String,
    countText: String?,
    color: Color,
    containerColor: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    testTag: String
) {
    Card(
        modifier = modifier.testTag(testTag),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(containerColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = title,
                fontSize = 11.sp,
                color = Color.Gray,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = amount,
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold,
                color = color,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (countText != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = countText,
                    fontSize = 10.sp,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * Clean Transaction Item in Statement History
 */
@Composable
fun StatementTransactionCard(
    item: TransactionWithDetails,
    runningBalance: Money? = null,
    onMoreDetailsClick: () -> Unit,
    onCustomerClick: () -> Unit
) {
    val strings = LocalStrings.current
    val isCancelled = item.transaction.status == TransactionStatus.CANCELLED

    val (badgeText, typeColor, containerColor, icon) = when (item.transaction.type) {
        TransactionType.CREDIT_PURCHASE -> StatementCardStyle(
            strings.transactionTypeCredit,
            FinancialDebt,
            FinancialDebtContainer,
            Icons.Default.CreditCard
        )
        TransactionType.PAYMENT -> StatementCardStyle(
            strings.transactionTypePayment,
            FinancialPayment,
            FinancialPaymentContainer,
            Icons.Default.AttachMoney
        )
        TransactionType.CASH_PURCHASE -> StatementCardStyle(
            strings.transactionTypeCash,
            FinancialCash,
            FinancialCashContainer,
            Icons.Default.ShoppingCart
        )
        else -> StatementCardStyle(
            strings.transactionTypeCredit,
            FinancialDebt,
            FinancialDebtContainer,
            Icons.Default.CreditCard
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp)
            .testTag("statement_tx_card_${item.transaction.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCancelled) FinancialCancelledContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isCancelled) 0.dp else 1.5.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left Icon + Type & Customer Info
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(if (isCancelled) FinancialCancelledContainer else containerColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isCancelled) Icons.Default.Cancel else icon,
                            contentDescription = null,
                            tint = if (isCancelled) FinancialCancelled else typeColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = badgeText,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = if (isCancelled) FinancialCancelled else typeColor
                            )
                            if (isCancelled) {
                                Spacer(modifier = Modifier.width(6.dp))
                                StatusBadge(status = TransactionStatus.CANCELLED)
                            }
                        }

                        if (item.customer != null) {
                            Text(
                                text = item.customer.name,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                color = LocalAppThemeColors.current.primary,
                                modifier = Modifier
                                    .clickable { onCustomerClick() }
                                    .padding(top = 2.dp)
                            )
                        } else if (item.transaction.type == TransactionType.CASH_PURCHASE) {
                            Text(
                                text = strings.cashCustomer,
                                fontSize = 12.sp,
                                color = Color.Gray,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }

                        Text(
                            text = formatDateTime(item.transaction.createdAt),
                            fontSize = 11.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }

                // Right Amount + Running Balance + More Details Action
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = item.transaction.totalAmount.format(),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        color = if (isCancelled) Color.Gray else typeColor,
                        style = if (isCancelled) MaterialTheme.typography.bodyLarge.copy(textDecoration = TextDecoration.LineThrough) else MaterialTheme.typography.bodyLarge
                    )

                    if (runningBalance != null && !isCancelled) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (runningBalance.isPositive()) FinancialDebt.copy(alpha = 0.1f) else FinancialPayment.copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = "${strings.runningBalanceLabel}: ${runningBalance.format()}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (runningBalance.isPositive()) FinancialDebt else FinancialPayment,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    OutlinedButton(
                        onClick = onMoreDetailsClick,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier
                            .height(28.dp)
                            .testTag("btn_more_details_${item.transaction.id}")
                    ) {
                        Text(
                            text = strings.moreDetails,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = LocalAppThemeColors.current.primary
                        )
                    }
                }
            }
        }
    }
}

/**
 * Modal Bottom Sheet for Transaction Full Details
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDetailsBottomSheet(
    txDetails: TransactionWithDetails,
    onDismiss: () -> Unit,
    onCancelTransaction: () -> Unit,
    onRestoreTransaction: () -> Unit,
    onCustomerClick: (Long) -> Unit
) {
    val strings = LocalStrings.current
    val themeColors = LocalAppThemeColors.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val isCancelled = txDetails.transaction.status == TransactionStatus.CANCELLED

    val (badgeText, typeColor, _, icon) = when (txDetails.transaction.type) {
        TransactionType.CREDIT_PURCHASE -> StatementCardStyle(
            strings.transactionTypeCredit,
            FinancialDebt,
            FinancialDebtContainer,
            Icons.Default.CreditCard
        )
        TransactionType.PAYMENT -> StatementCardStyle(
            strings.transactionTypePayment,
            FinancialPayment,
            FinancialPaymentContainer,
            Icons.Default.AttachMoney
        )
        TransactionType.CASH_PURCHASE -> StatementCardStyle(
            strings.transactionTypeCash,
            FinancialCash,
            FinancialCashContainer,
            Icons.Default.ShoppingCart
        )
        else -> StatementCardStyle(
            strings.transactionTypeCredit,
            FinancialDebt,
            FinancialDebtContainer,
            Icons.Default.CreditCard
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            // Sheet Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(if (isCancelled) FinancialCancelledContainer else themeColors.primaryContainer.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (isCancelled) FinancialCancelled else themeColors.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = strings.transactionDetailsTitle,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = themeColors.primary
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("btn_close_details_sheet")
                ) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

            // Primary Total Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = if (isCancelled) FinancialCancelledContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = badgeText,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = if (isCancelled) FinancialCancelled else typeColor
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = txDetails.transaction.totalAmount.format(),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 28.sp,
                        color = if (isCancelled) Color.Gray else typeColor,
                        style = if (isCancelled) MaterialTheme.typography.headlineMedium.copy(textDecoration = TextDecoration.LineThrough) else MaterialTheme.typography.headlineMedium
                    )

                    if (isCancelled) {
                        Spacer(modifier = Modifier.height(6.dp))
                        StatusBadge(status = TransactionStatus.CANCELLED)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Metadata Rows
            if (txDetails.customer != null) {
                DetailInfoRow(
                    label = strings.customerLabel,
                    value = txDetails.customer.name,
                    valueColor = themeColors.primary,
                    isClickable = true,
                    onClick = { onCustomerClick(txDetails.customer.id) }
                )
                if (txDetails.customer.phone.isNotBlank()) {
                    DetailInfoRow(
                        label = strings.customerPhone,
                        value = txDetails.customer.formattedPhoneWithCode
                    )
                }
            } else if (txDetails.transaction.type == TransactionType.CASH_PURCHASE) {
                DetailInfoRow(
                    label = strings.customerLabel,
                    value = strings.cashCustomer
                )
            }

            DetailInfoRow(
                label = strings.paymentMethod,
                value = badgeText,
                valueColor = typeColor
            )

            DetailInfoRow(
                label = strings.date,
                value = formatDateOnly(txDetails.transaction.createdAt)
            )

            DetailInfoRow(
                label = strings.timeLabel,
                value = formatTimeOnly(txDetails.transaction.createdAt)
            )

            DetailInfoRow(
                label = strings.statusLabel,
                value = if (isCancelled) strings.cancelledTransactions else strings.completedTransactions,
                valueColor = if (isCancelled) FinancialCancelled else FinancialPayment
            )

            if (txDetails.transaction.note.isNotBlank()) {
                DetailInfoRow(
                    label = strings.notes,
                    value = txDetails.transaction.note
                )
            }

            if (txDetails.transaction.cancelReason?.isNotBlank() == true) {
                DetailInfoRow(
                    label = strings.cancellationReason,
                    value = txDetails.transaction.cancelReason,
                    valueColor = FinancialDebt
                )
            }

            // Purchased Items Table if applicable
            if (txDetails.items.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = strings.orderItems,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = themeColors.primary
                )
                Spacer(modifier = Modifier.height(8.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        txDetails.items.forEachIndexed { index, item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${item.productNameSnapshot} × ${item.quantity}",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = item.subtotal.format(),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            if (index < txDetails.items.size - 1) {
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), modifier = Modifier.padding(vertical = 2.dp))
                            }
                        }
                    }
                }
            }

            // Actions at bottom
            Spacer(modifier = Modifier.height(20.dp))

            if (!isCancelled) {
                Button(
                    onClick = onCancelTransaction,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("btn_sheet_cancel_tx"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = FinancialDebtContainer)
                ) {
                    Icon(
                        imageVector = Icons.Default.Cancel,
                        contentDescription = null,
                        tint = FinancialDebt,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = strings.cancelTransaction,
                        color = FinancialDebt,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            } else {
                Button(
                    onClick = onRestoreTransaction,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("btn_sheet_restore_tx"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = themeColors.primaryContainer)
                ) {
                    Icon(
                        imageVector = Icons.Default.Restore,
                        contentDescription = null,
                        tint = themeColors.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = strings.restoreTransaction,
                        color = themeColors.primary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = strings.cancel,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun DetailInfoRow(
    label: String,
    value: String,
    valueColor: Color = Color.Black,
    isClickable: Boolean = false,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = Color.Gray
        )
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = valueColor,
            modifier = if (isClickable) Modifier.clickable { onClick() } else Modifier
        )
    }
}

private data class StatementCardStyle(
    val badgeText: String,
    val typeColor: Color,
    val containerColor: Color,
    val icon: ImageVector
)

@Composable
fun CustomerContextBanner(
    selectedCustomer: Customer?,
    onClearCustomer: () -> Unit,
    onSelectCustomerClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalStrings.current
    val themeColors = LocalAppThemeColors.current

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        color = if (selectedCustomer != null) themeColors.primaryContainer.copy(alpha = 0.45f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (selectedCustomer != null) themeColors.primary.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(if (selectedCustomer != null) themeColors.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (selectedCustomer != null) Icons.Default.Person else Icons.Default.Storefront,
                        contentDescription = null,
                        tint = if (selectedCustomer != null) Color.White else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Column {
                    Text(
                        text = if (selectedCustomer != null) {
                            "وضع العرض: زبون محدد"
                        } else {
                            "وضع العرض: المحل بالكامل"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (selectedCustomer != null) {
                            selectedCustomer.name
                        } else {
                            "كافة الزبائن والعمليات"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (selectedCustomer != null) themeColors.primary else MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            if (selectedCustomer != null) {
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onClearCustomer() }
                        .testTag("clear_customer_context_btn"),
                    color = themeColors.primary.copy(alpha = 0.12f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                            tint = themeColors.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = strings.switchToWholeShop,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = themeColors.primary
                        )
                    }
                }
            } else {
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onSelectCustomerClick() }
                        .testTag("select_customer_context_btn"),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = themeColors.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "تحديد زبون",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = themeColors.primary
                        )
                    }
                }
            }
        }
    }
}
