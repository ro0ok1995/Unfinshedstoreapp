package com.example.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.model.Customer
import com.example.core.model.CustomerStatus
import com.example.core.model.Money
import com.example.data.localization.LocalStrings
import com.example.ui.components.AddEditCustomerDialog
import com.example.ui.components.AppHeader
import com.example.ui.theme.FinancialDebt
import com.example.ui.theme.FinancialDebtContainer
import com.example.ui.theme.FinancialPayment
import com.example.ui.theme.FinancialPaymentContainer
import com.example.ui.theme.LocalAppThemeColors
import com.example.ui.viewmodel.ShopViewModel

enum class CustomerFilterOption {
    ALL,
    WITH_DEBT,
    SETTLED
}

/**
 * SCREEN: ACCOUNTS (الحسابات)
 * Dedicated real customer accounts screen displaying customer list, debt balances,
 * search & filter controls, and seamless navigation into Customer Details.
 */
@Composable
fun AccountsScreen(
    viewModel: ShopViewModel,
    onOpenDrawer: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val strings = LocalStrings.current
    val themeColors = LocalAppThemeColors.current
    val context = LocalContext.current

    val allCustomers by viewModel.allDatabaseCustomers.collectAsStateWithLifecycle()
    val customerDebtsMap by viewModel.customerDebtsMap.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf(CustomerFilterOption.ALL) }
    var showAddCustomerDialog by remember { mutableStateOf(false) }

    // Active customers (not soft-deleted)
    val activeCustomers = remember(allCustomers) {
        allCustomers.filter { it.status != CustomerStatus.DELETED }
    }

    // Filtered by search and status
    val displayedCustomers = remember(activeCustomers, customerDebtsMap, searchQuery, selectedFilter) {
        activeCustomers.filter { customer ->
            val matchesSearch = if (searchQuery.isBlank()) {
                true
            } else {
                customer.name.contains(searchQuery, ignoreCase = true) ||
                        customer.phone.contains(searchQuery, ignoreCase = true)
            }

            val debt = customerDebtsMap[customer.id] ?: Money.ZERO
            val matchesFilter = when (selectedFilter) {
                CustomerFilterOption.ALL -> true
                CustomerFilterOption.WITH_DEBT -> debt.isPositive()
                CustomerFilterOption.SETTLED -> !debt.isPositive()
            }

            matchesSearch && matchesFilter
        }.sortedWith(
            compareByDescending<Customer> { (customerDebtsMap[it.id] ?: Money.ZERO).amountMinor }
                .thenBy { it.name }
        )
    }

    val totalDebtAmount = remember(activeCustomers, customerDebtsMap) {
        activeCustomers.fold(Money.ZERO) { acc, cust ->
            acc + (customerDebtsMap[cust.id] ?: Money.ZERO)
        }
    }
    val indebtedCount = remember(activeCustomers, customerDebtsMap) {
        activeCustomers.count { (customerDebtsMap[it.id] ?: Money.ZERO).isPositive() }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header with Global Drawer access and Add Customer action
            AppHeader(
                title = strings.navAccounts,
                subtitle = "${activeCustomers.size} ${strings.totalCustomersLabel} • $indebtedCount ${strings.sectionCustomerDebt}",
                navigationIcon = {
                    IconButton(
                        onClick = onOpenDrawer,
                        modifier = Modifier
                            .testTag("accounts_drawer_btn")
                            .size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = strings.drawerMore,
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showAddCustomerDialog = true },
                        modifier = Modifier
                            .testTag("accounts_add_customer_btn")
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.PersonAdd,
                            contentDescription = strings.addCustomer,
                            tint = Color.White
                        )
                    }
                }
            )

            // Search Bar & Filter Controls
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("accounts_search_field"),
                    placeholder = {
                        Text(
                            text = strings.searchCustomer,
                            fontSize = 14.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = themeColors.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = strings.cancel,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = themeColors.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Filter Chips Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChip(
                        selected = selectedFilter == CustomerFilterOption.ALL,
                        onClick = { selectedFilter = CustomerFilterOption.ALL },
                        label = { Text(text = strings.all, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = themeColors.primary,
                            selectedLabelColor = Color.White
                        ),
                        modifier = Modifier.testTag("accounts_filter_all")
                    )

                    FilterChip(
                        selected = selectedFilter == CustomerFilterOption.WITH_DEBT,
                        onClick = { selectedFilter = CustomerFilterOption.WITH_DEBT },
                        label = {
                            Text(
                                text = "${strings.sectionCustomerDebt} ($indebtedCount)",
                                fontSize = 12.sp
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = FinancialDebt,
                            selectedLabelColor = Color.White
                        ),
                        modifier = Modifier.testTag("accounts_filter_with_debt")
                    )

                    FilterChip(
                        selected = selectedFilter == CustomerFilterOption.SETTLED,
                        onClick = { selectedFilter = CustomerFilterOption.SETTLED },
                        label = {
                            Text(
                                text = "${strings.statusActive} (${activeCustomers.size - indebtedCount})",
                                fontSize = 12.sp
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = FinancialPayment,
                            selectedLabelColor = Color.White
                        ),
                        modifier = Modifier.testTag("accounts_filter_settled")
                    )
                }
            }

            // Customer List or Empty State
            if (displayedCustomers.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(0.9f),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.People,
                                contentDescription = null,
                                tint = themeColors.primary.copy(alpha = 0.6f),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = if (searchQuery.isNotBlank()) strings.emptyState else strings.noData,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = themeColors.primary,
                                modifier = Modifier
                                    .clickable { showAddCustomerDialog = true }
                                    .testTag("accounts_empty_add_btn")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = strings.addCustomer,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("accounts_customer_list"),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 90.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(displayedCustomers, key = { it.id }) { customer ->
                        val debt = customerDebtsMap[customer.id] ?: Money.ZERO
                        CustomerAccountCard(
                            customer = customer,
                            debt = debt,
                            onCardClick = {
                                viewModel.openCustomerDetails(customer.id)
                            },
                            onPhoneClick = {
                                if (customer.phone.isNotBlank()) {
                                    val intent = Intent(Intent.ACTION_DIAL).apply {
                                        data = Uri.parse("tel:${customer.phone}")
                                    }
                                    context.startActivity(intent)
                                }
                            }
                        )
                    }
                }
            }
        }

        // Floating Action Button to add Customer
        FloatingActionButton(
            onClick = { showAddCustomerDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 84.dp)
                .testTag("accounts_fab_add_customer"),
            containerColor = themeColors.primary,
            contentColor = Color.White,
            shape = CircleShape
        ) {
            Icon(
                imageVector = Icons.Default.PersonAdd,
                contentDescription = strings.addCustomer
            )
        }
    }

    // Modal Add Customer Dialog
    if (showAddCustomerDialog) {
        AddEditCustomerDialog(
            customer = null,
            onDismiss = { showAddCustomerDialog = false },
            onSave = { newCustomer ->
                viewModel.saveCustomer(newCustomer) {
                    showAddCustomerDialog = false
                }
            }
        )
    }
}

/**
 * Customer Account Card with rich visual hierarchy, debt badge in NIS, phone action,
 * and tap-to-navigate into Customer Details.
 */
@Composable
private fun CustomerAccountCard(
    customer: Customer,
    debt: Money,
    onCardClick: () -> Unit,
    onPhoneClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val themeColors = LocalAppThemeColors.current
    val strings = LocalStrings.current

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onCardClick() }
            .testTag("customer_account_card_${customer.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Customer Avatar
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(
                        if (debt.isPositive()) FinancialDebtContainer else themeColors.primaryContainer
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = customer.name.take(1).uppercase(),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (debt.isPositive()) FinancialDebt else themeColors.primary,
                        fontSize = 18.sp
                    )
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Info Column
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = customer.name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(2.dp))

                if (customer.phone.isNotBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { onPhoneClick() }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = null,
                            tint = themeColors.primary,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = customer.phone,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = themeColors.primary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                } else if (customer.address.isNotBlank()) {
                    Text(
                        text = customer.address,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp
                        ),
                        maxLines = 1
                    )
                } else {
                    Text(
                        text = strings.statusActive,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            fontSize = 11.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Debt / Balance Badge
            Column(horizontalAlignment = Alignment.End) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (debt.isPositive()) FinancialDebtContainer else FinancialPaymentContainer
                ) {
                    Text(
                        text = debt.format(),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = if (debt.isPositive()) FinancialDebt else FinancialPayment,
                            fontSize = 13.sp
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = if (debt.isPositive()) strings.sectionCustomerDebt else strings.allCustomersSettled,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = if (debt.isPositive()) FinancialDebt.copy(alpha = 0.8f) else FinancialPayment.copy(alpha = 0.8f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = strings.details,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
