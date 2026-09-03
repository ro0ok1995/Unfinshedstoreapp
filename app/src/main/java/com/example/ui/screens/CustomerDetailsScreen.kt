package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.business.DebtEngine
import com.example.core.model.Customer
import com.example.data.localization.LocalStrings
import com.example.ui.components.AddEditCustomerDialog
import com.example.ui.components.RecordPaymentDialog
import com.example.ui.components.StatusBadge
import com.example.ui.components.ThemedHeaderBox
import com.example.ui.theme.LocalAppThemeColors
import com.example.ui.theme.FinancialDebt
import com.example.ui.theme.FinancialPayment
import com.example.ui.theme.FinancialPaymentContainer
import com.example.ui.viewmodel.ScreenDestination
import com.example.ui.viewmodel.ShopViewModel

/**
 * SCREEN 2: CUSTOMER DETAILS
 * Contextual screen for one selected customer providing the 3 primary actions:
 * 1. Record Purchase (CARD 1) -> Opens SCREEN 3 with customer preselected
 * 2. View Account Statement (CARD 2) -> Opens SCREEN 4 with customer preselected
 * 3. Record Payment (CARD 3) -> Opens Centered Modal Payment Dialog
 */
@Composable
fun CustomerDetailsScreen(
    customerId: Long,
    viewModel: ShopViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val strings = LocalStrings.current
    val themeColors = LocalAppThemeColors.current

    val allCustomers by viewModel.allActiveCustomers.collectAsStateWithLifecycle()
    val customer = allCustomers.find { it.id == customerId }
    val transactionsWithDetails by viewModel.filteredTransactionsWithDetails.collectAsStateWithLifecycle()

    val customerTransactions = remember(transactionsWithDetails, customerId) {
        transactionsWithDetails.filter { it.transaction.customerId == customerId }
    }

    // Current outstanding debt (Strictly non-negative)
    val customerDebt = remember(customerTransactions) {
        DebtEngine.calculateOutstandingDebt(customerTransactions.map { it.transaction })
    }

    var showEditCustomerDialog by remember { mutableStateOf(false) }
    var showPaymentDialog by remember { mutableStateOf(false) }

    if (customer == null) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(strings.customerNotFound, color = Color.Gray, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedButton(onClick = { viewModel.closeCustomerDetails() }) {
                    Text(strings.cancel)
                }
            }
        }
        return
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 40.dp)
        ) {
            // ==========================================
            // HEADER (STORE IDENTITY & BACK NAVIGATION)
            // ==========================================
            item {
                ThemedHeaderBox(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 8.dp, end = 16.dp, top = 14.dp, bottom = 18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { viewModel.closeCustomerDetails() },
                            modifier = Modifier.testTag("customer_details_back_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = strings.cancel,
                                tint = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = customer.name,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 19.sp
                                ),
                                maxLines = 1
                            )
                            Text(
                                text = strings.customerDetailsTitle,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color.White.copy(alpha = 0.85f),
                                    fontSize = 12.sp
                                )
                            )
                        }

                        IconButton(
                            onClick = { showEditCustomerDialog = true },
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.18f))
                                .testTag("edit_customer_header_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = strings.editCustomer,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // ==========================================
            // CUSTOMER INFO & DEBT HEADER CARD
            // ==========================================
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                        .testTag("customer_profile_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        // Customer Identity Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(CircleShape)
                                        .background(themeColors.primaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = customer.name.take(1),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 24.sp,
                                        color = themeColors.primary
                                    )
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                Column {
                                    Text(
                                        text = customer.name,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 18.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        ),
                                        maxLines = 1
                                    )

                                    if (customer.phone.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.clickable {
                                                val intent = Intent(Intent.ACTION_DIAL).apply {
                                                    data = Uri.parse("tel:${customer.phone}")
                                                }
                                                context.startActivity(intent)
                                            }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Phone,
                                                contentDescription = null,
                                                tint = themeColors.primary,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = customer.phone,
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    color = themeColors.primary,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            )
                                        }
                                    }
                                }
                            }

                            StatusBadge(status = customer.status)
                        }

                        if (customer.address.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "${strings.customerAddress}: ${customer.address}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 12.sp
                                )
                            )
                        }

                        Divider(
                            color = MaterialTheme.colorScheme.outlineVariant,
                            thickness = 1.dp,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )

                        // Outstanding Debt Section (Visually Prominent, Red indicator, ₪ Currency, Non-negative)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = strings.outstandingDebt,
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        color = if (customerDebt.isPositive()) FinancialDebt.copy(alpha = 0.9f) else Color.Gray,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp
                                    )
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (customerDebt.isPositive()) customerDebt.format() else strings.allCustomersSettled,
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 28.sp,
                                        color = if (customerDebt.isPositive()) FinancialDebt else FinancialPayment
                                    ),
                                    modifier = Modifier.testTag("customer_detail_debt_text")
                                )
                            }
                        }
                    }
                }
            }

            // ==========================================
            // PRIMARY ACTIONS SECTION (3 LARGE CLEAR CARDS)
            // ==========================================
            item {
                Text(
                    text = strings.primaryActionsTitle,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = themeColors.primary,
                        fontSize = 14.sp
                    ),
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                )
            }

            // CARD 1: Record Purchase -> Open SCREEN 3 (Purchases) with Customer preselected
            item {
                PrimaryActionCard(
                    icon = Icons.Default.ShoppingCart,
                    iconTint = themeColors.primary,
                    iconContainerColor = themeColors.primaryContainer,
                    title = strings.recordPurchase,
                    subtitle = strings.recordPurchaseDesc,
                    onClick = {
                        viewModel.openPurchasesForCustomer(customer)
                    },
                    testTag = "card_action_record_purchase"
                )
            }

            // CARD 2: View Account Statement -> Open SCREEN 4 (Statements) with Customer preselected
            item {
                PrimaryActionCard(
                    icon = Icons.Default.Assessment,
                    iconTint = themeColors.secondary,
                    iconContainerColor = themeColors.primaryContainer.copy(alpha = 0.5f),
                    title = strings.viewAccountStatement,
                    subtitle = strings.viewAccountStatementDesc,
                    onClick = {
                        viewModel.openStatementsForCustomer(customer)
                    },
                    testTag = "card_action_view_statement"
                )
            }

            // CARD 2.5: Analysis Center for this customer
            item {
                PrimaryActionCard(
                    icon = Icons.Default.BarChart,
                    iconTint = themeColors.primary,
                    iconContainerColor = themeColors.primaryContainer,
                    title = strings.tabAnalysisCenter,
                    subtitle = "عرض المؤشرات والرسوم البيانية لحساب ${customer.name}",
                    onClick = {
                        viewModel.openAnalysisCenter(customer)
                    },
                    testTag = "card_action_view_customer_analysis"
                )
            }

            // CARD 3: Record Payment -> Open Modal Payment Dialog with calm green accent
            item {
                PrimaryActionCard(
                    icon = Icons.Default.Payments,
                    iconTint = FinancialPayment,
                    iconContainerColor = FinancialPaymentContainer,
                    title = strings.recordPayment,
                    subtitle = if (customerDebt.isPositive()) strings.recordPaymentDesc else strings.accountFullyPaid,
                    onClick = { viewModel.openQuickPayment(customer) },
                    testTag = "card_action_record_payment"
                )
            }

            // CARD 4: Set as Home Context -> Focus home dashboard on this customer
            item {
                PrimaryActionCard(
                    icon = Icons.Default.Assessment,
                    iconTint = themeColors.primary,
                    iconContainerColor = themeColors.primaryContainer,
                    title = strings.setAsHomeCustomer,
                    subtitle = strings.homeCustomerMode,
                    onClick = {
                        viewModel.setCustomerAsHomeContext(customer)
                    },
                    testTag = "card_action_set_home_context"
                )
            }
        }
    }

    // ==========================================
    // PAYMENT DIALOG (MODAL)
    // ==========================================
    if (showPaymentDialog) {
        RecordPaymentDialog(
            customer = customer,
            currentDebt = customerDebt,
            onDismiss = { showPaymentDialog = false },
            onConfirm = { amount, note ->
                viewModel.submitPayment(customer.id, amount, note) {
                    showPaymentDialog = false
                }
            }
        )
    }

    // Edit Customer Modal Dialog
    if (showEditCustomerDialog) {
        AddEditCustomerDialog(
            customer = customer,
            onDismiss = { showEditCustomerDialog = false },
            onSave = { updated ->
                viewModel.saveCustomer(updated) {
                    showEditCustomerDialog = false
                }
            }
        )
    }
}

/**
 * Reusable large, clear primary action card with rounded corners, soft elevation, and distinct styling.
 */
@Composable
private fun PrimaryActionCard(
    icon: ImageVector,
    iconTint: Color,
    iconContainerColor: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable { onClick() }
            .testTag(testTag),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(iconContainerColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(22.dp)
            )
        }
    }
}
