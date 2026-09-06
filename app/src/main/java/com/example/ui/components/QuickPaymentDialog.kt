package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.model.Customer
import com.example.core.model.Money
import com.example.core.model.SettlementMode
import com.example.data.localization.LocalStrings
import com.example.ui.theme.FinancialCash
import com.example.ui.theme.FinancialCashContainer
import com.example.ui.theme.FinancialDebt
import com.example.ui.theme.FinancialDebtContainer
import com.example.ui.theme.FinancialPayment
import com.example.ui.theme.LocalAppThemeColors
import com.example.ui.viewmodel.QuickPaymentSuccessData
import com.example.ui.viewmodel.ShopViewModel

/**
 * Quick Payment: the "customer + amount + settlement" entry point from the '+' menu
 * (and from Customer Details -> Payment). Uses the exact same unified settlement
 * model as the Purchases screen (Full Cash / Full Debt / Partial), just without a
 * cart, per the master reference sections 11-12 and 16.
 */
@Composable
fun QuickPaymentDialog(
    viewModel: ShopViewModel,
    onDismiss: () -> Unit
) {
    val strings = LocalStrings.current
    val themeColors = LocalAppThemeColors.current
    val customers by viewModel.allActiveCustomers.collectAsStateWithLifecycle()
    val debtsMap by viewModel.customerDebtsMap.collectAsStateWithLifecycle()
    val targetCustomer by viewModel.quickPaymentTargetCustomer.collectAsStateWithLifecycle()
    val isSubmitting by viewModel.isSubmitting.collectAsStateWithLifecycle()
    val settlementMode by viewModel.quickSettlementMode.collectAsStateWithLifecycle()
    val partialCashText by viewModel.quickPartialCashAmount.collectAsStateWithLifecycle()

    var selectedCustomer by remember(targetCustomer) { mutableStateOf(targetCustomer) }
    var amountText by remember { mutableStateOf("") }
    var notesText by remember { mutableStateOf("") }
    var searchQuery by remember { mutableStateOf("") }
    var isSelectingCustomer by remember { mutableStateOf(targetCustomer == null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val currentDebt = selectedCustomer?.let { debtsMap[it.id] } ?: Money.ZERO
    val totalAmount = remember(amountText) { Money.fromShekels(amountText) }
    val partialCashMoney = remember(partialCashText) { Money.fromShekels(partialCashText) }

    val isPartialValid = remember(settlementMode, partialCashMoney, totalAmount) {
        settlementMode != SettlementMode.PARTIAL || (partialCashMoney.isPositive() && partialCashMoney < totalAmount)
    }

    val debtAddedByThisEntry = remember(settlementMode, totalAmount, partialCashMoney) {
        when (settlementMode) {
            SettlementMode.FULL_CASH -> Money.ZERO
            SettlementMode.FULL_DEBT -> totalAmount
            SettlementMode.PARTIAL -> if (totalAmount > partialCashMoney) totalAmount - partialCashMoney else Money.ZERO
        }
    }
    val projectedDebt = remember(currentDebt, debtAddedByThisEntry) { currentDebt + debtAddedByThisEntry }

    val filteredCustomers = remember(customers, searchQuery) {
        val query = searchQuery.trim()
        if (query.isBlank()) {
            customers
        } else {
            customers.filter {
                it.name.contains(query, ignoreCase = true) || it.phone.contains(query, ignoreCase = true)
            }
        }
    }

    Dialog(onDismissRequest = { if (!isSubmitting) onDismiss() }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .testTag("quick_payment_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(FinancialPayment.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Payments,
                                    contentDescription = null,
                                    tint = FinancialPayment,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = strings.quickPayment,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }

                        IconButton(
                            onClick = { if (!isSubmitting) onDismiss() },
                            enabled = !isSubmitting,
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = strings.cancel,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // Customer Selection Section
                item {
                    if (selectedCustomer != null && !isSelectingCustomer) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = FinancialDebtContainer.copy(alpha = 0.4f),
                            border = BorderStroke(1.dp, FinancialDebt.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(themeColors.primary.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = null,
                                            tint = themeColors.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = selectedCustomer!!.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "${strings.currentDebt}: ",
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = currentDebt.format(),
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = FinancialDebt
                                            )
                                        }
                                    }
                                }

                                TextButton(
                                    onClick = { isSelectingCustomer = true },
                                    enabled = !isSubmitting,
                                    modifier = Modifier.testTag("quick_payment_change_customer_btn")
                                ) {
                                    Text(
                                        text = strings.changeCustomer,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = themeColors.primary
                                    )
                                }
                            }
                        }
                    } else {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = strings.selectCustomerPrompt,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = { Text(strings.searchCustomerHint, fontSize = 12.sp) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = null,
                                        tint = themeColors.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("quick_payment_search_input"),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFF9F9FA),
                                border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                if (filteredCustomers.isEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = strings.noCustomersFound,
                                            fontSize = 12.sp,
                                            color = Color.Gray
                                        )
                                    }
                                } else {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .heightIn(max = 180.dp)
                                    ) {
                                        filteredCustomers.forEach { cust ->
                                            val debt = debtsMap[cust.id] ?: Money.ZERO
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable {
                                                        selectedCustomer = cust
                                                        isSelectingCustomer = false
                                                        errorMessage = null
                                                    }
                                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Column {
                                                    Text(
                                                        text = cust.name,
                                                        fontWeight = FontWeight.Medium,
                                                        fontSize = 13.sp,
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                    if (cust.phone.isNotBlank()) {
                                                        Text(
                                                            text = cust.phone,
                                                            fontSize = 11.sp,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    }
                                                }

                                                Text(
                                                    text = debt.format(),
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp,
                                                    color = if (debt.isPositive()) FinancialDebt else Color.Gray
                                                )
                                            }
                                            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), thickness = 0.5.dp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Amount + Settlement + Notes
                if (selectedCustomer != null && !isSelectingCustomer) {
                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = amountText,
                                onValueChange = {
                                    amountText = it
                                    errorMessage = null
                                },
                                label = { Text(strings.totalAmount + " (₪) *") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("quick_payment_amount_input"),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true,
                                enabled = !isSubmitting,
                                isError = errorMessage != null
                            )

                            // Settlement Mode — identical model/terminology to Purchases
                            Text(
                                text = strings.purchaseType,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color.DarkGray
                            )
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color(0xFFF9F9FA))
                                    .padding(8.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                SettlementOptionRow(
                                    selected = settlementMode == SettlementMode.FULL_DEBT,
                                    accent = FinancialDebt,
                                    accentContainer = FinancialDebtContainer,
                                    title = strings.settlementFullDebt,
                                    desc = strings.settlementFullDebtDesc,
                                    enabled = !isSubmitting,
                                    onSelect = { viewModel.setQuickSettlementMode(SettlementMode.FULL_DEBT) }
                                )
                                SettlementOptionRow(
                                    selected = settlementMode == SettlementMode.FULL_CASH,
                                    accent = FinancialCash,
                                    accentContainer = FinancialCashContainer,
                                    title = strings.settlementFullCash,
                                    desc = strings.settlementFullCashDesc,
                                    enabled = !isSubmitting,
                                    onSelect = { viewModel.setQuickSettlementMode(SettlementMode.FULL_CASH) }
                                )
                                SettlementOptionRow(
                                    selected = settlementMode == SettlementMode.PARTIAL,
                                    accent = themeColors.primary,
                                    accentContainer = themeColors.primaryContainer,
                                    title = strings.settlementPartial,
                                    desc = strings.settlementPartialDesc,
                                    enabled = !isSubmitting,
                                    onSelect = { viewModel.setQuickSettlementMode(SettlementMode.PARTIAL) }
                                )

                                if (settlementMode == SettlementMode.PARTIAL) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    OutlinedTextField(
                                        value = partialCashText,
                                        onValueChange = {
                                            viewModel.setQuickPartialCashAmount(it)
                                            errorMessage = null
                                        },
                                        label = { Text(strings.partialPaymentAmountPrompt) },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("quick_payment_partial_cash_input"),
                                        shape = RoundedCornerShape(10.dp),
                                        singleLine = true,
                                        enabled = !isSubmitting,
                                        isError = partialCashText.isNotBlank() && !isPartialValid
                                    )
                                    if (partialCashText.isNotBlank() && !isPartialValid) {
                                        Text(
                                            text = strings.partialPaymentInvalid,
                                            color = Color.Red,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }

                            // Real-time Balance Breakdown
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFF3F4F6),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(text = strings.currentDebt, fontSize = 12.sp, color = Color.Gray)
                                        Text(text = currentDebt.format(), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = FinancialDebt)
                                    }
                                    Divider(color = Color(0xFFE5E7EB), thickness = 0.5.dp)
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(text = strings.newDebtLabel, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                        Text(text = projectedDebt.format(), fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = if (projectedDebt.isPositive()) FinancialDebt else FinancialPayment)
                                    }
                                }
                            }

                            OutlinedTextField(
                                value = notesText,
                                onValueChange = { notesText = it },
                                label = { Text(strings.notes + " (${strings.optional})") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("quick_payment_notes_input"),
                                shape = RoundedCornerShape(12.dp),
                                maxLines = 2,
                                enabled = !isSubmitting
                            )
                        }
                    }
                }

                // Error Message
                errorMessage?.let { err ->
                    item {
                        Text(
                            text = err,
                            color = Color.Red,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Actions: Cancel & Submit
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            enabled = !isSubmitting,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("quick_payment_cancel_btn"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(strings.cancel)
                        }

                        val canSubmit = selectedCustomer != null &&
                            !isSelectingCustomer &&
                            totalAmount.isPositive() &&
                            isPartialValid &&
                            !isSubmitting

                        Button(
                            onClick = {
                                if (selectedCustomer == null) {
                                    errorMessage = strings.selectCustomerPrompt
                                    return@Button
                                }
                                if (!totalAmount.isPositive()) {
                                    errorMessage = strings.amountMustBeGreaterThanZero
                                    return@Button
                                }
                                if (!isPartialValid) {
                                    errorMessage = strings.partialPaymentInvalid
                                    return@Button
                                }
                                viewModel.submitQuickPayment(
                                    selectedCustomer,
                                    totalAmount,
                                    settlementMode,
                                    partialCashMoney,
                                    notesText
                                )
                            },
                            enabled = canSubmit,
                            modifier = Modifier
                                .weight(1.5f)
                                .height(48.dp)
                                .testTag("quick_payment_confirm_btn"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = FinancialPayment,
                                disabledContainerColor = Color(0xFFBDBDBD)
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = strings.confirmAndSave,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettlementOptionRow(
    selected: Boolean,
    accent: Color,
    accentContainer: Color,
    title: String,
    desc: String,
    enabled: Boolean,
    onSelect: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) accentContainer.copy(alpha = 0.5f) else Color.Transparent)
            .clickable(enabled = enabled, onClick = onSelect)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = if (enabled) onSelect else null,
            colors = RadioButtonDefaults.colors(selectedColor = accent)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Column {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = if (selected) accent else Color.DarkGray
            )
            Text(text = desc, fontSize = 11.sp, color = Color.Gray)
        }
    }
}

/**
 * Success receipt after a Quick Payment entry is recorded.
 */
@Composable
fun QuickPaymentSuccessDialog(
    data: QuickPaymentSuccessData,
    onDismiss: () -> Unit
) {
    val strings = LocalStrings.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = FinancialPayment,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = strings.paymentSuccessTitle,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "${strings.customer}: ${data.customer.name}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFF9FAFB),
                    border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = strings.totalAmount, fontSize = 12.sp, color = Color.Gray)
                            Text(text = data.totalAmount.format(), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                        if (data.mode != SettlementMode.FULL_DEBT) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = strings.settlementFullCash, fontSize = 12.sp, color = FinancialPayment, fontWeight = FontWeight.Bold)
                                Text(text = data.cashPortion.format(), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = FinancialPayment)
                            }
                        }
                        Divider(color = Color(0xFFE5E7EB), thickness = 0.5.dp)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = strings.previousDebtLabel, fontSize = 12.sp, color = Color.Gray)
                            Text(text = data.previousDebt.format(), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = FinancialDebt)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = strings.newDebtLabel, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text(
                                text = data.newDebt.format(),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (data.newDebt.isPositive()) FinancialDebt else FinancialPayment
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = FinancialPayment),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("quick_payment_success_done_btn")
            ) {
                Text(strings.cancel, fontWeight = FontWeight.Bold)
            }
        },
        shape = RoundedCornerShape(18.dp),
        containerColor = MaterialTheme.colorScheme.surface
    )
}
