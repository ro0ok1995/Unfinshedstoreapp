package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.model.CustomerWithDebt
import com.example.core.model.Money
import com.example.core.model.TransactionWithDetails
import com.example.data.localization.LocalStrings
import com.example.ui.components.AddEditCustomerDialog
import com.example.ui.components.SearchBar
import com.example.ui.components.ThemedHeaderBox
import com.example.ui.components.ThemedPrimaryButton
import com.example.ui.theme.AppVisualTheme
import com.example.ui.theme.FinancialCash
import com.example.ui.theme.FinancialDebt
import com.example.ui.theme.FinancialPayment
import com.example.ui.theme.LocalAppThemeColors
import com.example.ui.viewmodel.HomeFinancialStats
import com.example.ui.viewmodel.HomePeriod
import com.example.ui.viewmodel.ShopViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * SCREEN 1: REDESIGNED HOME SCREEN (Phase 2)
 *
 * Requirements fulfilled:
 * 1. Header:
 *    - Drawer button on the start/right side
 *    - Centered Shop Name
 *    - Notifications button on the end/left side
 * 2. Customer Search:
 *    - Real query search across name and phone
 *    - Customer Context Banner showing active selection vs shop-wide context
 * 3. Compact Financial Statistics:
 *    - Donut/Circular chart driven by real database figures (Debts, Cash, Payments)
 *    - Real calculated percentages and balances
 * 4. Period Selector:
 *    - Today, This Week, This Month, All Time
 * 5. Customer List & Outstanding Balances:
 *    - Automatic serial numbering (①, ②, ③...)
 *    - Clean RTL card layout
 * 6. Latest Activities (أحدث الحركات):
 *    - Displaying newest transactions with types, amounts, and dates
 * 7. Actionable Empty States
 */
@Composable
fun HomeScreen(
    viewModel: ShopViewModel,
    onOpenDrawer: () -> Unit = {},
    onShowNotification: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val strings = LocalStrings.current
    val shopSettings by viewModel.shopSettings.collectAsStateWithLifecycle()
    val customersWithDebt by viewModel.activeCustomersWithDebt.collectAsStateWithLifecycle()
    val searchQuery by viewModel.customerSearchQuery.collectAsStateWithLifecycle()
    val allActiveCustomers by viewModel.allActiveCustomers.collectAsStateWithLifecycle()
    val selectedCustomer by viewModel.selectedHomeCustomer.collectAsStateWithLifecycle()
    val activePeriod by viewModel.homePeriod.collectAsStateWithLifecycle()
    val homeStats by viewModel.homeFinancialStats.collectAsStateWithLifecycle()
    val latestActivities by viewModel.homeLatestActivities.collectAsStateWithLifecycle()
    val unreadNotificationCount by viewModel.unreadNotificationCount.collectAsStateWithLifecycle()
    val themeColors = LocalAppThemeColors.current

    var showAddCustomerDialog by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 110.dp)
        ) {
            // ==========================================
            // 1. TOP HEADER (Drawer + Shop Name + Notifications)
            // ==========================================
            item {
                ThemedHeaderBox(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 14.dp, end = 14.dp, top = 14.dp, bottom = 18.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Right in RTL / Start: Global Drawer Trigger Button
                            IconButton(
                                onClick = onOpenDrawer,
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.15f))
                                    .testTag("home_drawer_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Menu,
                                    contentDescription = strings.drawerMore,
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            // Center: Shop Name & Subtitle
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 8.dp)
                            ) {
                                Text(
                                    text = shopSettings.storeName.ifBlank { strings.appName },
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 19.sp
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = strings.homeTitle,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Color.White.copy(alpha = 0.85f),
                                        fontSize = 11.5.sp
                                    ),
                                    maxLines = 1,
                                    textAlign = TextAlign.Center
                                )
                            }

                            // Left in RTL / End: Notifications Button
                            Box {
                                IconButton(
                                    onClick = onShowNotification,
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.15f))
                                        .testTag("home_notifications_btn")
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Notifications,
                                        contentDescription = strings.notifications,
                                        tint = Color.White,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                if (unreadNotificationCount > 0) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .size(18.dp)
                                            .clip(CircleShape)
                                            .background(FinancialDebt),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = if (unreadNotificationCount > 9) "9+" else "$unreadNotificationCount",
                                            color = Color.White,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Customer Search Bar
                        SearchBar(
                            query = searchQuery,
                            onQueryChange = { viewModel.setCustomerSearchQuery(it) },
                            placeholder = strings.searchCustomerHint,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("home_search_bar")
                        )
                    }
                }
            }

            // ==========================================
            // 2. ACTIVE CONTEXT BANNER (Shop-wide vs Customer)
            // ==========================================
            item {
                AnimatedVisibility(
                    visible = selectedCustomer != null,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    if (selectedCustomer != null) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp),
                            shape = RoundedCornerShape(14.dp),
                            color = when (themeColors.visualTheme) {
                                AppVisualTheme.PURPLE -> Color(0xFFF3E8FF)
                                AppVisualTheme.GOLD -> Color(0xFFFEF3C7)
                                AppVisualTheme.BLACK_AND_WHITE -> MaterialTheme.colorScheme.surfaceVariant
                            },
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                when (themeColors.visualTheme) {
                                    AppVisualTheme.PURPLE -> Color(0xFFA855F7).copy(alpha = 0.4f)
                                    AppVisualTheme.GOLD -> Color(0xFFD97706).copy(alpha = 0.4f)
                                    AppVisualTheme.BLACK_AND_WHITE -> MaterialTheme.colorScheme.outlineVariant
                                }
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Person,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = strings.homeCustomerMode,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                fontSize = 11.sp
                                            )
                                        )
                                        Text(
                                            text = selectedCustomer!!.name,
                                            style = MaterialTheme.typography.titleSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = MaterialTheme.colorScheme.onSurface
                                            ),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = { viewModel.openCustomerDetails(selectedCustomer!!.id) },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier
                                            .height(32.dp)
                                            .testTag("home_view_customer_profile_btn")
                                    ) {
                                        Text(
                                            text = strings.viewCustomerProfile,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    IconButton(
                                        onClick = { viewModel.clearHomeCustomer() },
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f))
                                            .testTag("home_clear_customer_btn")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Close,
                                            contentDescription = strings.clearCustomerSelection,
                                            tint = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ==========================================
            // 3. PERIOD SELECTOR CHIPS
            // ==========================================
            item {
                Column(modifier = Modifier.padding(top = 10.dp, bottom = 4.dp)) {
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val periods = listOf(
                            HomePeriod.TODAY to strings.periodToday,
                            HomePeriod.THIS_WEEK to strings.periodThisWeek,
                            HomePeriod.THIS_MONTH to strings.periodThisMonth,
                            HomePeriod.ALL_TIME to strings.periodAllTime
                        )

                        items(periods) { (period, label) ->
                            val isSelected = activePeriod == period
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.setHomePeriod(period) },
                                label = {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            fontSize = 12.sp
                                        )
                                    )
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                    containerColor = MaterialTheme.colorScheme.surface,
                                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isSelected,
                                    borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                    selectedBorderColor = MaterialTheme.colorScheme.primary
                                )
                            )
                        }
                    }
                }
            }

            // ==========================================
            // 4. REAL STATS & DONUT CHART SECTION
            // ==========================================
            item {
                HomeStatsCard(
                    stats = homeStats,
                    isCustomerContext = selectedCustomer != null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            // ==========================================
            // 5. CUSTOMERS SECTION / SEARCH RESULTS
            // ==========================================
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (searchQuery.isNotBlank()) strings.search else strings.customersList,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 14.sp
                        )
                    )

                    Text(
                        text = "${customersWithDebt.size} ${strings.tabCustomers}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }

            if (allActiveCustomers.isEmpty() && searchQuery.isBlank()) {
                item {
                    NoCustomersEmptyState(
                        onAddCustomer = { showAddCustomerDialog = true }
                    )
                }
            } else if (customersWithDebt.isEmpty() && searchQuery.isNotBlank()) {
                item {
                    NoSearchResultsEmptyState(
                        searchQuery = searchQuery,
                        onClearSearch = { viewModel.setCustomerSearchQuery("") }
                    )
                }
            } else {
                // If searching, show all matches; otherwise show top 6 customers with fast expand to Accounts
                val displayList = if (searchQuery.isNotBlank()) customersWithDebt else customersWithDebt.take(6)

                itemsIndexed(
                    items = displayList,
                    key = { _, item -> item.customer.id }
                ) { index, item ->
                    CustomerCard(
                        serialNumber = index + 1,
                        customerWithDebt = item,
                        onClick = {
                            viewModel.selectHomeCustomer(item.customer)
                        }
                    )
                }

                if (searchQuery.isBlank() && customersWithDebt.size > 6) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            OutlinedButton(
                                onClick = { viewModel.navigateTo(com.example.ui.viewmodel.ScreenDestination.DATABASE) },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = "${strings.all} (${customersWithDebt.size})",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.Filled.ArrowForward,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            // ==========================================
            // 6. LATEST FINANCIAL ACTIVITIES
            // ==========================================
            item {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = strings.latestActivities,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 14.sp
                        )
                    )

                    if (latestActivities.isNotEmpty()) {
                        Text(
                            text = "${latestActivities.size} ${strings.actions}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp
                            )
                        )
                    }
                }
            }

            if (latestActivities.isEmpty()) {
                item {
                    NoActivitiesInPeriodEmptyState()
                }
            } else {
                items(
                    items = latestActivities,
                    key = { it.transaction.id }
                ) { txWithDetails ->
                    ActivityRowCard(
                        item = txWithDetails,
                        onClick = {
                            if (txWithDetails.customer != null) {
                                viewModel.openCustomerDetails(txWithDetails.customer.id)
                            }
                        }
                    )
                }
            }
        }
    }

    // Customer creation dialog
    if (showAddCustomerDialog) {
        AddEditCustomerDialog(
            customer = null,
            onDismiss = { showAddCustomerDialog = false },
            onSave = { customer ->
                viewModel.saveCustomer(customer) {
                    showAddCustomerDialog = false
                }
            }
        )
    }
}

/**
 * Compact, Real-Data Donut Chart & Financial Statistics Card.
 */
@Composable
private fun HomeStatsCard(
    stats: HomeFinancialStats,
    isCustomerContext: Boolean,
    modifier: Modifier = Modifier
) {
    val strings = LocalStrings.current

    Card(
        modifier = modifier.testTag("home_stats_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Outstanding balance banner
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (isCustomerContext) strings.outstandingDebt else strings.totalDebtSummary,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = stats.outstandingBalance.format(),
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = if (stats.outstandingBalance.isPositive()) FinancialDebt else FinancialPayment,
                            fontSize = 20.sp
                        )
                    )
                }

                if (stats.hasTransactions) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                    ) {
                        Text(
                            text = "${strings.homeStatsTotal}: ${stats.totalVolume.format()}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Donut Chart & Legend Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Donut Chart
                Box(
                    modifier = Modifier
                        .size(105.dp)
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    DonutChartCanvas(
                        creditPct = stats.creditPercentage,
                        cashPct = stats.cashPercentage,
                        paymentsPct = stats.paymentsPercentage,
                        hasData = stats.hasTransactions
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (stats.hasTransactions) "${(stats.creditPercentage * 100).toInt()}%" else "0%",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        Text(
                            text = strings.homeStatsCredit,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 9.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Legend and breakdown
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatBreakdownRow(
                        label = strings.homeStatsCredit,
                        amount = stats.totalCredit,
                        pct = stats.creditPercentage,
                        color = FinancialDebt
                    )

                    StatBreakdownRow(
                        label = strings.homeStatsCash,
                        amount = stats.totalCash,
                        pct = stats.cashPercentage,
                        color = FinancialCash
                    )

                    StatBreakdownRow(
                        label = strings.homeStatsPayments,
                        amount = stats.totalPayments,
                        pct = stats.paymentsPercentage,
                        color = FinancialPayment
                    )
                }
            }
        }
    }
}

@Composable
private fun DonutChartCanvas(
    creditPct: Float,
    cashPct: Float,
    paymentsPct: Float,
    hasData: Boolean,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val strokeWidth = 14.dp.toPx()

        if (!hasData) {
            drawCircle(
                color = Color.LightGray.copy(alpha = 0.35f),
                style = Stroke(width = strokeWidth)
            )
            return@Canvas
        }

        var startAngle = -90f

        // Credit slice
        if (creditPct > 0f) {
            val sweep = creditPct * 360f
            drawArc(
                color = FinancialDebt,
                startAngle = startAngle,
                sweepAngle = sweep,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
            startAngle += sweep
        }

        // Cash slice
        if (cashPct > 0f) {
            val sweep = cashPct * 360f
            drawArc(
                color = FinancialCash,
                startAngle = startAngle,
                sweepAngle = sweep,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
            startAngle += sweep
        }

        // Payments slice
        if (paymentsPct > 0f) {
            val sweep = paymentsPct * 360f
            drawArc(
                color = FinancialPayment,
                startAngle = startAngle,
                sweepAngle = sweep,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }
    }
}

@Composable
private fun StatBreakdownRow(
    label: String,
    amount: Money,
    pct: Float,
    color: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
        }

        Text(
            text = "${amount.format()} (${(pct * 100).toInt()}%)",
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = color
            )
        )
    }
}

/**
 * Activity Item Row for Latest Financial Activities.
 */
@Composable
private fun ActivityRowCard(
    item: TransactionWithDetails,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tx = item.transaction
    val customerName = item.customer?.name ?: "—"
    val isCredit = tx.isCreditPurchase
    val isCash = tx.isCashPurchase
    val isPayment = tx.isPayment

    val (icon, badgeBg, badgeColor, typeLabel) = when {
        isCredit -> Quad(
            Icons.Filled.ReceiptLong,
            FinancialDebt.copy(alpha = 0.12f),
            FinancialDebt,
            "شراء آجل"
        )
        isCash -> Quad(
            Icons.Filled.ShoppingCart,
            FinancialCash.copy(alpha = 0.12f),
            FinancialCash,
            "شراء نقدي"
        )
        else -> Quad(
            Icons.Filled.Payments,
            FinancialPayment.copy(alpha = 0.12f),
            FinancialPayment,
            "سداد دفعة"
        )
    }

    val dateFormat = remember { SimpleDateFormat("HH:mm - yyyy/MM/dd", Locale.getDefault()) }
    val formattedDate = remember(tx.createdAt) { dateFormat.format(Date(tx.createdAt)) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable { onClick() }
            .testTag("activity_card_${tx.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(badgeBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = badgeColor,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = customerName,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = typeLabel,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = badgeColor,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp
                            )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "•",
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            fontSize = 10.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = formattedDate,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = "${if (isPayment) "-" else "+"}${tx.totalAmount.format()}",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp,
                    color = badgeColor
                )
            )
        }
    }
}

private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

/**
 * Customer Card in Home Screen List.
 */
@Composable
fun CustomerCard(
    serialNumber: Int,
    customerWithDebt: CustomerWithDebt,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalStrings.current
    val customer = customerWithDebt.customer
    val debt = customerWithDebt.outstandingDebt
    val hasDebt = debt.isPositive()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable { onClick() }
            .testTag("customer_card_${customer.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f, fill = false)
            ) {
                CustomerSerialBadge(serialNumber = serialNumber)

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = customer.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (customer.phone.isNotBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Phone,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = customer.phone,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 12.sp
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = strings.outstandingDebt,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = if (hasDebt) FinancialDebt.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = debt.format(),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        color = if (hasDebt) FinancialDebt else FinancialPayment
                    ),
                    modifier = Modifier.testTag("customer_debt_${customer.id}")
                )
            }
        }
    }
}

@Composable
fun CustomerSerialBadge(
    serialNumber: Int,
    modifier: Modifier = Modifier
) {
    val symbol = when (serialNumber) {
        1 -> "①"
        2 -> "②"
        3 -> "③"
        4 -> "④"
        5 -> "⑤"
        6 -> "⑥"
        7 -> "⑦"
        8 -> "⑧"
        9 -> "⑨"
        10 -> "⑩"
        11 -> "⑪"
        12 -> "⑫"
        13 -> "⑬"
        14 -> "⑭"
        15 -> "⑮"
        16 -> "⑯"
        17 -> "⑰"
        18 -> "⑱"
        19 -> "⑲"
        20 -> "⑳"
        else -> "$serialNumber"
    }

    Box(
        modifier = modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer)
            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = symbol,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontSize = if (serialNumber <= 20) 16.sp else 12.sp
            )
        )
    }
}

@Composable
private fun NoActivitiesInPeriodEmptyState(
    modifier: Modifier = Modifier
) {
    val strings = LocalStrings.current

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = strings.noActivitiesInPeriod,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp
                ),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun NoCustomersEmptyState(
    onAddCustomer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalStrings.current

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = strings.noCustomersYet,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = strings.noCustomersYetDesc,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            ThemedPrimaryButton(
                onClick = onAddCustomer,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .height(46.dp)
                    .testTag("empty_state_add_customer_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.PersonAdd,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = strings.addCustomer,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
private fun NoSearchResultsEmptyState(
    searchQuery: String,
    onClearSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalStrings.current

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.SearchOff,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = strings.noMatchingCustomers,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "\"$searchQuery\"",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = onClearSearch,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("clear_search_empty_btn")
            ) {
                Text(
                    text = strings.cancel,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
