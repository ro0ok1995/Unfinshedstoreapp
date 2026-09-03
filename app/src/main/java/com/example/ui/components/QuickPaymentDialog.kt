package com.example.ui.components

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
import com.example.data.localization.LocalStrings
import com.example.ui.theme.FinancialDebt
import com.example.ui.theme.FinancialDebtContainer
import com.example.ui.theme.FinancialPayment
import com.example.ui.theme.LocalAppThemeColors
import com.example.ui.viewmodel.QuickPaymentSuccessData
import com.example.ui.viewmodel.ShopViewModel

/**
 * Dedicated Quick Payment Dialog for settling customer debts.
 * Supports choosing customer with live debt search, real-time balance calculations,
 * "Pay All" quick action, double submission protection, and instant debt reduction.
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

    var selectedCustomer by remember(targetCustomer) { mutableStateOf(targetCustomer) }
    var amountText by remember { mutableStateOf("") }
    var notesText by remember { mutableStateOf("") }
    var searchQuery by remember { mutableStateOf("") }
    var isSelectingCustomer by remember { mutableStateOf(targetCustomer == null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val currentDebt = selectedCustomer?.let { debtsMap[it.id] } ?: Money.ZERO
    val paymentAmount = remember(amountText) { Money.fromShekels(amountText) }
    val remainingDebt = remember(currentDebt, paymentAmount) {
        if (currentDebt > paymentAmount) currentDebt - paymentAmount else Money.ZERO
    }

    val filteredCustomers = remember(customers, searchQuery, debtsMap) {
        val query = searchQuery.trim()
        val list = if (query.isBlank()) {
            customers.filter { (debtsMap[it.id] ?: Money.ZERO).isPositive() }
        } else {
            customers.filter {
                it.name.contains(query, ignoreCase = true) || it.phone.contains(query, ignoreCase = true)
            }
        }
        list.sortedByDescending { debtsMap[it.id] ?: Money.ZERO }
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
                            Column {
                                Text(
                                    text = strings.quickPayment,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 17.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                )
                                Text(
                                    text = strings.recordPaymentTitle,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 12.sp
                                    )
                                )
                            }
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
                        // Customer Selected Card
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = FinancialDebtContainer.copy(alpha = 0.4f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, FinancialDebt.copy(alpha = 0.4f)),
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
                        // Customer Search & Selection Dropdown
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
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E0E0)),
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

                // Payment Inputs & Real-time balance calculation
                if (selectedCustomer != null && !isSelectingCustomer) {
                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = amountText,
                                onValueChange = {
                                    amountText = it
                                    errorMessage = null
                                },
                                label = { Text(strings.paymentAmount + " (₪) *") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("quick_payment_amount_input"),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true,
                                enabled = !isSubmitting,
                                isError = errorMessage != null
                            )

                            // Quick "Pay All" Button
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(
                                    onClick = {
                                        amountText = currentDebt.formatWithoutSymbol()
                                        errorMessage = null
                                    },
                                    enabled = !isSubmitting && currentDebt.isPositive()
                                ) {
                                    Text(
                                        text = strings.payFullDebt,
                                        fontSize = 12.sp,
                                        color = themeColors.primary,
                                        fontWeight = FontWeight.Bold
                                    )
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
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(text = strings.cashPurchase, fontSize = 12.sp, color = FinancialPayment, fontWeight = FontWeight.SemiBold)
                                        Text(text = paymentAmount.format(), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = FinancialPayment)
                                    }
                                    Divider(color = Color(0xFFE5E7EB), thickness = 0.5.dp)
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(text = strings.newDebtLabel, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                        Text(text = remainingDebt.format(), fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = if (remainingDebt.isPositive()) FinancialDebt else FinancialPayment)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(2.dp))

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
                            paymentAmount.isPositive() &&
                            paymentAmount <= currentDebt &&
                            !isSubmitting

                        Button(
                            onClick = {
                                if (selectedCustomer == null) {
                                    errorMessage = strings.selectCustomerPrompt
                                    return@Button
                                }
                                if (!paymentAmount.isPositive()) {
                                    errorMessage = strings.amountMustBeGreaterThanZero
                                    return@Button
                                }
                                if (paymentAmount > currentDebt) {
                                    errorMessage = strings.paymentExceedsDebtError
                                    return@Button
                                }
                                viewModel.submitQuickPayment(selectedCustomer!!, paymentAmount, notesText)
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

/**
 * Success Dialog after quick payment is recorded.
 * Shows receipt summary: customer name, paid amount, previous debt, and new remaining balance.
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
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB)),
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
                            Text(text = strings.previousDebtLabel, fontSize = 12.sp, color = Color.Gray)
                            Text(text = data.previousDebt.format(), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = FinancialDebt)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = strings.paymentAmount, fontSize = 12.sp, color = FinancialPayment, fontWeight = FontWeight.Bold)
                            Text(text = data.paidAmount.format(), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = FinancialPayment)
                        }
                        Divider(color = Color(0xFFE5E7EB), thickness = 0.5.dp)
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
