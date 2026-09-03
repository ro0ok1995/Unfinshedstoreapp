package com.example.ui.screens

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.model.CartItem
import com.example.core.model.Customer
import com.example.core.model.Money
import com.example.core.model.Product
import com.example.core.model.SettlementMode
import com.example.data.localization.LocalStrings
import com.example.ui.components.AddEditCustomerDialog
import com.example.ui.components.EditQuantityDialog
import com.example.ui.components.ProductImage
import com.example.ui.components.ThemedHeaderBox
import com.example.ui.components.ThemedPrimaryButton
import com.example.ui.theme.LocalAppThemeColors
import com.example.ui.theme.FinancialCash
import com.example.ui.theme.FinancialCashContainer
import com.example.ui.theme.FinancialDebt
import com.example.ui.theme.FinancialDebtContainer
import com.example.ui.theme.FinancialPayment
import com.example.ui.viewmodel.ScreenDestination
import com.example.ui.viewmodel.ShopViewModel

/**
 * SCREEN 2: RECORD PURCHASES AND POS CART
 * Allows browsing active products, managing live quantity in cart,
 * choosing credit/cash purchase, and atomically confirming transactions.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PurchasesScreen(
    viewModel: ShopViewModel,
    modifier: Modifier = Modifier
) {
    val strings = LocalStrings.current
    val themeColors = LocalAppThemeColors.current
    val customers by viewModel.allActiveCustomers.collectAsStateWithLifecycle()
    val products by viewModel.activeProducts.collectAsStateWithLifecycle()
    val selectedCustomer by viewModel.selectedPurchaseCustomer.collectAsStateWithLifecycle()
    val isCredit by viewModel.isCreditPurchase.collectAsStateWithLifecycle()
    val settlementMode by viewModel.selectedSettlementMode.collectAsStateWithLifecycle()
    val partialCashAmount by viewModel.partialCashAmount.collectAsStateWithLifecycle()
    val isSubmitting by viewModel.isSubmitting.collectAsStateWithLifecycle()
    val cartItems by viewModel.cartItems.collectAsStateWithLifecycle()
    val notes by viewModel.purchaseNotes.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var showReviewModal by remember { mutableStateOf(false) }
    var showCustomItemDialog by remember { mutableStateOf(false) }
    var showAddCustomerDialog by remember { mutableStateOf(false) }
    var emptyCartWarningMessage by remember { mutableStateOf<String?>(null) }

    val totalAmount = viewModel.cartTotal
    val totalItemCount = remember(cartItems) {
        cartItems.sumOf { it.quantity }.toInt()
    }

    val filteredProducts = remember(products, searchQuery) {
        if (searchQuery.isBlank()) {
            products
        } else {
            products.filter { it.name.contains(searchQuery, ignoreCase = true) }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // TOP BAR (RTL AWARE, CUSTOMER IDENTITY, CART & CLEAR ACTIONS)
            ThemedHeaderBox(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 8.dp, end = 12.dp, top = 12.dp, bottom = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Back navigation & Header
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        IconButton(
                            onClick = {
                                if (selectedCustomer != null) {
                                    viewModel.openCustomerDetails(selectedCustomer!!.id)
                                } else {
                                    viewModel.navigateTo(ScreenDestination.HOME)
                                }
                            },
                            modifier = Modifier.testTag("purchases_back_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = strings.cancel,
                                tint = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        Column {
                            Text(
                                text = if (selectedCustomer != null) {
                                    "${strings.recordTransactionFor}: ${selectedCustomer!!.name}"
                                } else {
                                    strings.purchasesTitle
                                },
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Text(
                                text = if (selectedCustomer != null) {
                                    if (isCredit) strings.creditPurchase else strings.cashPurchase
                                } else {
                                    strings.cashCustomerWalkIn
                                },
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color.White.copy(alpha = 0.85f),
                                    fontSize = 12.sp
                                )
                            )
                        }
                    }

                    // Actions: Clear cart & Cart Badge
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (cartItems.isNotEmpty()) {
                            IconButton(
                                onClick = { viewModel.clearCart() },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.18f))
                                    .testTag("clear_cart_top_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.DeleteSweep,
                                    contentDescription = strings.clearCart,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                        }

                        BadgedBox(
                            badge = {
                                if (cartItems.isNotEmpty()) {
                                    Badge(
                                        containerColor = FinancialDebt,
                                        contentColor = Color.White,
                                        modifier = Modifier.testTag("cart_badge_count")
                                    ) {
                                        Text(text = "$totalItemCount", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.18f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ShoppingCart,
                                    contentDescription = strings.orderItems,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }

            // PRODUCT SEARCH & QUICK CUSTOM ITEM BAR
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            text = strings.searchProductsPlaceholder,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = themeColors.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear search",
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("product_search_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedBorderColor = themeColors.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    ),
                    singleLine = true
                )

                Surface(
                    onClick = { showCustomItemDialog = true },
                    shape = RoundedCornerShape(12.dp),
                    color = themeColors.primaryContainer,
                    modifier = Modifier
                        .height(48.dp)
                        .testTag("add_custom_item_top_btn")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = themeColors.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = strings.customItem,
                            color = themeColors.primary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // RESPONSIVE PRODUCT GRID
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 150.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .padding(horizontal = 14.dp),
                contentPadding = PaddingValues(top = 4.dp, bottom = 100.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (filteredProducts.isEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Inventory2,
                                    contentDescription = null,
                                    tint = Color.LightGray,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = strings.noProductsFound,
                                    color = Color.Gray,
                                    fontSize = 14.sp,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                OutlinedButton(
                                    onClick = { showCustomItemDialog = true },
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text(strings.customItem)
                                }
                            }
                        }
                    }
                } else {
                    items(filteredProducts, key = { it.id }) { product ->
                        val cartItem = cartItems.find { it.productId == product.id }
                        val currentQuantity = cartItem?.quantity ?: 0.0

                        ProductGridCard(
                            product = product,
                            currentQuantity = currentQuantity,
                            onQuantityChanged = { newQty ->
                                val index = cartItems.indexOfFirst { it.productId == product.id }
                                if (index >= 0) {
                                    viewModel.updateCartItemQuantity(index, newQty)
                                } else if (newQty > 0.0) {
                                    viewModel.addProductToCart(product, newQty)
                                }
                            }
                        )
                    }
                }
            }
        }

        // STICKY BOTTOM ACTION BAR ("Complete Transaction")
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .shadow(16.dp),
            color = Color.White
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Cart Summary Amount
                Column {
                    Text(
                        text = "${strings.totalAmount} (${cartItems.size})",
                        fontSize = 12.sp,
                        color = Color(0xFF757575),
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = totalAmount.format(),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isCredit) FinancialDebt else FinancialCash,
                        modifier = Modifier.testTag("purchases_total_amount_text")
                    )
                }

                // Complete Transaction Button
                ThemedPrimaryButton(
                    onClick = {
                        if (cartItems.isEmpty()) {
                            emptyCartWarningMessage = strings.cartEmptyWarning
                        } else {
                            showReviewModal = true
                        }
                    },
                    enabled = cartItems.isNotEmpty(),
                    modifier = Modifier.testTag("complete_transaction_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = strings.completeTransaction,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }

    // REVIEW TRANSACTION MODAL
    if (showReviewModal) {
        ReviewTransactionModal(
            cartItems = cartItems,
            totalAmount = totalAmount,
            customers = customers,
            initialCustomer = selectedCustomer,
            settlementMode = settlementMode,
            initialPartialPaid = partialCashAmount,
            notes = notes,
            isSubmitting = isSubmitting,
            onCustomerChanged = { viewModel.setSelectedPurchaseCustomer(it) },
            onSettlementModeChanged = { viewModel.setSelectedSettlementMode(it) },
            onPartialPaidChanged = { viewModel.setPartialCashAmount(it) },
            onNotesChanged = { viewModel.setPurchaseNotes(it) },
            onAddNewCustomer = { showAddCustomerDialog = true },
            onDismiss = { showReviewModal = false },
            onConfirm = { confirmedCustomer, mode, partialPaid, confirmedNotes ->
                viewModel.submitPurchase(
                    customerId = confirmedCustomer.id,
                    mode = mode,
                    partialPaid = partialPaid,
                    note = confirmedNotes
                ) {
                    showReviewModal = false
                    viewModel.openCustomerDetails(confirmedCustomer.id)
                }
            }
        )
    }

    // Empty Cart Warning Dialog
    emptyCartWarningMessage?.let { warning ->
        AlertDialog(
            onDismissRequest = { emptyCartWarningMessage = null },
            title = {
                Text(
                    text = strings.orderItems,
                    fontWeight = FontWeight.Bold,
                    color = FinancialDebt
                )
            },
            text = {
                Text(text = warning, fontSize = 14.sp)
            },
            confirmButton = {
                Button(
                    onClick = { emptyCartWarningMessage = null },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(strings.cancel)
                }
            },
            shape = RoundedCornerShape(16.dp),
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    // Custom Ad-hoc Item Dialog
    if (showCustomItemDialog) {
        AddCustomItemDialog(
            onDismiss = { showCustomItemDialog = false },
            onAdd = { name, price, qty ->
                viewModel.addCustomItemToCart(name, price, qty)
                showCustomItemDialog = false
            }
        )
    }

    // Add Customer Dialog
    if (showAddCustomerDialog) {
        AddEditCustomerDialog(
            customer = null,
            onDismiss = { showAddCustomerDialog = false },
            onSave = { customer ->
                viewModel.saveCustomer(customer) { newId ->
                    val savedCustomer = customer.copy(id = newId)
                    viewModel.setSelectedPurchaseCustomer(savedCustomer)
                    showAddCustomerDialog = false
                }
            }
        )
    }
}

/**
 * Modern product card for the responsive grid with immediate quantity control [-] qty [+].
 */
@Composable
fun ProductGridCard(
    product: Product,
    currentQuantity: Double,
    onQuantityChanged: (Double) -> Unit,
    modifier: Modifier = Modifier
) {
    val themeColors = LocalAppThemeColors.current
    val isInCart = currentQuantity > 0.0
    var showEditQtyDialog by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("product_grid_card_${product.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = if (isInCart) androidx.compose.foundation.BorderStroke(1.5.dp, themeColors.primary) else null,
        elevation = CardDefaults.cardElevation(defaultElevation = if (isInCart) 3.dp else 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(84.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (isInCart) themeColors.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                    ),
                contentAlignment = Alignment.Center
            ) {
                ProductImage(
                    imagePath = product.imagePath,
                    modifier = Modifier.fillMaxSize(),
                    placeholderIcon = Icons.Default.Inventory2,
                    placeholderTint = if (isInCart) themeColors.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    placeholderBackground = if (isInCart) themeColors.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = product.name,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = product.price.format(),
                fontWeight = FontWeight.ExtraBold,
                fontSize = 14.sp,
                color = themeColors.primary
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Quantity Control: [-] [qty text (tap to edit via keyboard)] [+]
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        if (isInCart) themeColors.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                    ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = {
                        if (currentQuantity > 0.0) {
                            onQuantityChanged(currentQuantity - 1.0)
                        }
                    },
                    enabled = currentQuantity > 0.0,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("product_decrease_btn_${product.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = "Decrease",
                        tint = if (currentQuantity > 0.0) themeColors.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.size(16.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { showEditQtyDialog = true }
                        .padding(horizontal = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (currentQuantity % 1.0 == 0.0) {
                            currentQuantity.toInt().toString()
                        } else {
                            currentQuantity.toString()
                        },
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp,
                        color = if (isInCart) themeColors.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.testTag("product_qty_text_${product.id}")
                    )
                }

                IconButton(
                    onClick = { onQuantityChanged(currentQuantity + 1.0) },
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("product_increase_btn_${product.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Increase",
                        tint = themeColors.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }

    if (showEditQtyDialog) {
        EditQuantityDialog(
            initialQuantity = currentQuantity,
            productName = product.name,
            onDismiss = { showEditQtyDialog = false },
            onConfirm = { newQty ->
                onQuantityChanged(newQty)
                showEditQtyDialog = false
            }
        )
    }
}

/**
 * Centered Review Transaction Modal displaying clear purchase summary,
 * product list with unit prices and subtotals, 3-way settlement mode selection
 * (Full Debt, Full Cash, Partial Cash+Debt), customer selection, and atomic confirmation.
 */
@Composable
fun ReviewTransactionModal(
    cartItems: List<CartItem>,
    totalAmount: Money,
    customers: List<Customer>,
    initialCustomer: Customer?,
    settlementMode: SettlementMode,
    initialPartialPaid: String,
    notes: String,
    isSubmitting: Boolean,
    onCustomerChanged: (Customer?) -> Unit,
    onSettlementModeChanged: (SettlementMode) -> Unit,
    onPartialPaidChanged: (String) -> Unit,
    onNotesChanged: (String) -> Unit,
    onAddNewCustomer: () -> Unit,
    onDismiss: () -> Unit,
    onConfirm: (Customer, SettlementMode, Money, String) -> Unit
) {
    val strings = LocalStrings.current
    val themeColors = LocalAppThemeColors.current
    var currentlySelectedCustomer by remember(initialCustomer) { mutableStateOf(initialCustomer) }
    var currentSettlementMode by remember(settlementMode) { mutableStateOf(settlementMode) }
    var currentPartialPaid by remember(initialPartialPaid) { mutableStateOf(initialPartialPaid) }
    var currentNotes by remember(notes) { mutableStateOf(notes) }
    var customerPickerExpanded by remember { mutableStateOf(false) }
    var customerSearchText by remember { mutableStateOf("") }
    var validationError by remember { mutableStateOf<String?>(null) }

    val partialPaidMoney = remember(currentPartialPaid) {
        Money.fromShekels(currentPartialPaid)
    }
    val remainingDebt = remember(totalAmount, partialPaidMoney) {
        if (totalAmount > partialPaidMoney) totalAmount - partialPaidMoney else Money.ZERO
    }
    val isPartialValid = remember(currentSettlementMode, partialPaidMoney, totalAmount) {
        if (currentSettlementMode != SettlementMode.PARTIAL) true
        else partialPaidMoney.isPositive() && partialPaidMoney < totalAmount
    }

    val filteredCustomers = remember(customers, customerSearchText) {
        if (customerSearchText.isBlank()) {
            customers
        } else {
            customers.filter {
                it.name.contains(customerSearchText, ignoreCase = true) ||
                it.phone.contains(customerSearchText, ignoreCase = true)
            }
        }
    }

    Dialog(onDismissRequest = { if (!isSubmitting) onDismiss() }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .testTag("review_transaction_modal"),
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
                // Modal Header
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = strings.reviewTransactionTitle,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = themeColors.primary
                                )
                            )
                            Text(
                                text = "${cartItems.size} ${strings.orderItems}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 12.sp
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

                // Customer Selection Field (Always present in dialog)
                item {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${strings.customer}:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            TextButton(onClick = onAddNewCustomer, enabled = !isSubmitting) {
                                Text(
                                    text = "+ " + strings.addCustomer,
                                    fontSize = 12.sp,
                                    color = themeColors.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        if (currentlySelectedCustomer != null) {
                            // STATE A: Customer is selected -> Display: "العميل: محمد أحمد ✓" with change option
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = themeColors.primaryContainer.copy(alpha = 0.35f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, themeColors.primary.copy(alpha = 0.5f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable(enabled = !isSubmitting) { customerPickerExpanded = !customerPickerExpanded }
                                    .testTag("review_selected_customer_card")
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
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = currentlySelectedCustomer!!.name,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 15.sp,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "✓",
                                                    fontWeight = FontWeight.ExtraBold,
                                                    fontSize = 15.sp,
                                                    color = FinancialPayment
                                                )
                                            }
                                            if (currentlySelectedCustomer!!.phone.isNotBlank()) {
                                                Text(
                                                    text = currentlySelectedCustomer!!.phone,
                                                    fontSize = 12.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }

                                    TextButton(
                                        onClick = { customerPickerExpanded = !customerPickerExpanded },
                                        enabled = !isSubmitting,
                                        modifier = Modifier.testTag("change_customer_btn")
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
                            // STATE B: No customer selected -> Display: "[ اختر العميل ▼ ]"
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFFFF4E5),
                                border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFED6C02)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable(enabled = !isSubmitting) { customerPickerExpanded = !customerPickerExpanded }
                                    .testTag("review_empty_customer_card")
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = null,
                                            tint = Color(0xFFED6C02),
                                            modifier = Modifier.size(22.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = strings.chooseCustomerDropdown,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = Color(0xFFD95300)
                                        )
                                    }
                                }
                            }
                        }

                        // Searchable Customer Selector Dropdown List
                        if (customerPickerExpanded) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFF9F9FA),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E0E0)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp)
                                 ) {
                                     OutlinedTextField(
                                        value = customerSearchText,
                                        onValueChange = { customerSearchText = it },
                                        placeholder = { Text(strings.searchCustomerHint, fontSize = 12.sp) },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.Search,
                                                contentDescription = null,
                                                tint = themeColors.primary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        },
                                        trailingIcon = {
                                            if (customerSearchText.isNotEmpty()) {
                                                IconButton(onClick = { customerSearchText = "" }) {
                                                    Icon(
                                                        imageVector = Icons.Default.Clear,
                                                        contentDescription = null,
                                                        tint = Color.Gray,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("customer_picker_search_input"),
                                        singleLine = true,
                                        shape = RoundedCornerShape(10.dp)
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    if (filteredCustomers.isEmpty()) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 12.dp),
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
                                                .heightIn(max = 200.dp)
                                        ) {
                                            filteredCustomers.forEach { cust ->
                                                val isCurrent = currentlySelectedCustomer?.id == cust.id
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .background(
                                                            if (isCurrent) themeColors.primaryContainer.copy(alpha = 0.6f) else Color.Transparent
                                                        )
                                                        .clickable {
                                                            currentlySelectedCustomer = cust
                                                            onCustomerChanged(cust)
                                                            customerPickerExpanded = false
                                                            validationError = null
                                                        }
                                                        .padding(horizontal = 10.dp, vertical = 8.dp),
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
                                                    if (isCurrent) {
                                                        Text(
                                                            text = "✓",
                                                            fontWeight = FontWeight.Bold,
                                                            color = FinancialPayment,
                                                            fontSize = 14.sp
                                                        )
                                                    }
                                                }
                                                Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), thickness = 0.5.dp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Payment Method Selector (3 Settlement Modes)
                item {
                    Text(
                        text = strings.purchaseType,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color.DarkGray
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFFF9F9FA))
                            .padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // 1. Full Debt Option
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (currentSettlementMode == SettlementMode.FULL_DEBT) FinancialDebtContainer.copy(alpha = 0.5f) else Color.Transparent
                                )
                                .clickable(enabled = !isSubmitting) {
                                    currentSettlementMode = SettlementMode.FULL_DEBT
                                    onSettlementModeChanged(SettlementMode.FULL_DEBT)
                                    validationError = null
                                }
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = currentSettlementMode == SettlementMode.FULL_DEBT,
                                onClick = {
                                    if (!isSubmitting) {
                                        currentSettlementMode = SettlementMode.FULL_DEBT
                                        onSettlementModeChanged(SettlementMode.FULL_DEBT)
                                        validationError = null
                                    }
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = FinancialDebt)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = strings.settlementFullDebt,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = if (currentSettlementMode == SettlementMode.FULL_DEBT) FinancialDebt else Color.DarkGray
                                )
                                Text(
                                    text = strings.settlementFullDebtDesc,
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                            }
                        }

                        // 2. Full Cash Option
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (currentSettlementMode == SettlementMode.FULL_CASH) FinancialCashContainer.copy(alpha = 0.5f) else Color.Transparent
                                )
                                .clickable(enabled = !isSubmitting) {
                                    currentSettlementMode = SettlementMode.FULL_CASH
                                    onSettlementModeChanged(SettlementMode.FULL_CASH)
                                    validationError = null
                                }
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = currentSettlementMode == SettlementMode.FULL_CASH,
                                onClick = {
                                    if (!isSubmitting) {
                                        currentSettlementMode = SettlementMode.FULL_CASH
                                        onSettlementModeChanged(SettlementMode.FULL_CASH)
                                        validationError = null
                                    }
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = FinancialCash)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = strings.settlementFullCash,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = if (currentSettlementMode == SettlementMode.FULL_CASH) FinancialCash else Color.DarkGray
                                )
                                Text(
                                    text = strings.settlementFullCashDesc,
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                            }
                        }

                        // 3. Partial (Cash + Debt) Option
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (currentSettlementMode == SettlementMode.PARTIAL) themeColors.primaryContainer.copy(alpha = 0.5f) else Color.Transparent
                                )
                                .clickable(enabled = !isSubmitting) {
                                    currentSettlementMode = SettlementMode.PARTIAL
                                    onSettlementModeChanged(SettlementMode.PARTIAL)
                                    validationError = null
                                }
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = currentSettlementMode == SettlementMode.PARTIAL,
                                onClick = {
                                    if (!isSubmitting) {
                                        currentSettlementMode = SettlementMode.PARTIAL
                                        onSettlementModeChanged(SettlementMode.PARTIAL)
                                        validationError = null
                                    }
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = themeColors.primary)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = strings.settlementPartial,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = if (currentSettlementMode == SettlementMode.PARTIAL) themeColors.primary else Color.DarkGray
                                )
                                Text(
                                    text = strings.settlementPartialDesc,
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                            }
                        }

                        // Detailed Partial Breakdown & Input
                        if (currentSettlementMode == SettlementMode.PARTIAL) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = androidx.compose.foundation.BorderStroke(1.dp, themeColors.primary.copy(alpha = 0.3f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 4.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = currentPartialPaid,
                                        onValueChange = {
                                            currentPartialPaid = it
                                            onPartialPaidChanged(it)
                                            validationError = null
                                        },
                                        label = { Text(strings.partialPaymentAmountPrompt) },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("partial_cash_amount_input"),
                                        shape = RoundedCornerShape(10.dp),
                                        singleLine = true,
                                        isError = currentPartialPaid.isNotBlank() && !isPartialValid
                                    )

                                    // Real-time breakdown
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFFF3F4F6))
                                            .padding(8.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(text = strings.totalAmount, fontSize = 12.sp, color = Color.Gray)
                                            Text(text = totalAmount.format(), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(text = strings.cashPurchase, fontSize = 12.sp, color = FinancialPayment, fontWeight = FontWeight.SemiBold)
                                            Text(text = partialPaidMoney.format(), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = FinancialPayment)
                                        }
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(text = strings.settlementFullDebt, fontSize = 12.sp, color = FinancialDebt, fontWeight = FontWeight.SemiBold)
                                            Text(text = remainingDebt.format(), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = FinancialDebt)
                                        }
                                    }

                                    if (currentPartialPaid.isNotBlank() && !isPartialValid) {
                                        Text(
                                            text = strings.partialPaymentInvalid,
                                            color = Color.Red,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Purchase Items Summary List
                item {
                    Text(
                        text = strings.orderItems,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color.DarkGray
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF9F9FA),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            cartItems.forEachIndexed { index, item ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = item.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = Color(0xFF222222)
                                        )
                                        Text(
                                            text = "${item.unitPrice.format()} × ${item.quantity}",
                                            fontSize = 11.sp,
                                            color = Color.Gray
                                        )
                                    }

                                    Text(
                                        text = item.subtotal.format(),
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 13.sp,
                                        color = themeColors.primary
                                    )
                                }

                                if (index < cartItems.size - 1) {
                                    Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), thickness = 0.5.dp)
                                }
                            }
                        }
                    }
                }

                // Total Summary Card
                item {
                    val summaryBgColor = when (currentSettlementMode) {
                        SettlementMode.FULL_DEBT -> FinancialDebtContainer
                        SettlementMode.FULL_CASH -> FinancialCashContainer
                        SettlementMode.PARTIAL -> themeColors.primaryContainer.copy(alpha = 0.5f)
                    }
                    val summaryTextColor = when (currentSettlementMode) {
                        SettlementMode.FULL_DEBT -> FinancialDebt
                        SettlementMode.FULL_CASH -> FinancialCash
                        SettlementMode.PARTIAL -> themeColors.primary
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = summaryBgColor,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = strings.totalAmount,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = summaryTextColor
                            )

                            Text(
                                text = totalAmount.format(),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 22.sp,
                                color = summaryTextColor,
                                modifier = Modifier.testTag("review_total_amount_text")
                            )
                        }
                    }
                }

                // Optional Notes
                item {
                    OutlinedTextField(
                        value = currentNotes,
                        onValueChange = {
                            currentNotes = it
                            onNotesChanged(it)
                        },
                        label = { Text(strings.notes + " (${strings.optional})") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("review_notes_input"),
                        shape = RoundedCornerShape(12.dp),
                        maxLines = 2,
                        enabled = !isSubmitting
                    )
                }

                // Validation Warning Message if Customer is Not Selected
                if (currentlySelectedCustomer == null) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFFFEBEE),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = strings.selectCustomerPrompt,
                                color = Color(0xFFC62828),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                    }
                }

                // Validation Error Message
                validationError?.let { err ->
                    item {
                        Text(
                            text = err,
                            color = Color.Red,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Actions: Cancel & Confirm Transaction
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
                                .testTag("cancel_transaction_btn"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(strings.cancel)
                        }

                        val canConfirm = currentlySelectedCustomer != null &&
                            cartItems.isNotEmpty() &&
                            !isSubmitting &&
                            (currentSettlementMode != SettlementMode.PARTIAL || isPartialValid)

                        Button(
                            onClick = {
                                if (cartItems.isEmpty()) {
                                    validationError = strings.cartEmptyWarning
                                    return@Button
                                }
                                if (currentlySelectedCustomer == null) {
                                    validationError = strings.selectCustomerPrompt
                                    return@Button
                                }
                                if (currentSettlementMode == SettlementMode.PARTIAL && !isPartialValid) {
                                    validationError = strings.partialPaymentInvalid
                                    return@Button
                                }
                                onConfirm(
                                    currentlySelectedCustomer!!,
                                    currentSettlementMode,
                                    if (currentSettlementMode == SettlementMode.PARTIAL) partialPaidMoney else Money.ZERO,
                                    currentNotes
                                )
                            },
                            enabled = canConfirm,
                            modifier = Modifier
                                .weight(1.5f)
                                .height(48.dp)
                                .testTag("confirm_transaction_btn"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = when (currentSettlementMode) {
                                    SettlementMode.FULL_DEBT -> FinancialDebt
                                    SettlementMode.FULL_CASH -> FinancialCash
                                    SettlementMode.PARTIAL -> themeColors.primary
                                },
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
                                text = strings.confirmTransaction,
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
 * Dialog to add custom ad-hoc item directly into the cart.
 */
@Composable
fun AddCustomItemDialog(
    onDismiss: () -> Unit,
    onAdd: (name: String, price: Money, quantity: Double) -> Unit
) {
    val strings = LocalStrings.current
    val themeColors = LocalAppThemeColors.current
    var name by remember { mutableStateOf("") }
    var priceText by remember { mutableStateOf("") }
    var quantityText by remember { mutableStateOf("1") }
    var nameError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = strings.customItem,
                fontWeight = FontWeight.Bold,
                color = themeColors.primary,
                fontSize = 16.sp
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        if (it.isNotBlank()) nameError = false
                    },
                    label = { Text(strings.itemName + " *") },
                    isError = nameError,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("custom_item_name_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = priceText,
                        onValueChange = { priceText = it },
                        label = { Text(strings.unitPrice + " (₪) *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("custom_item_price_input"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = quantityText,
                        onValueChange = { quantityText = it },
                        label = { Text(strings.quantity) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("custom_item_qty_input"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank()) {
                        nameError = true
                        return@Button
                    }
                    val money = Money.fromShekels(priceText)
                    val qty = quantityText.toDoubleOrNull() ?: 1.0
                    onAdd(name.trim(), money, qty)
                },
                colors = ButtonDefaults.buttonColors(containerColor = themeColors.primary),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("confirm_add_custom_item_btn")
            ) {
                Text(strings.addItem, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(strings.cancel)
            }
        },
        shape = RoundedCornerShape(20.dp),
        containerColor = MaterialTheme.colorScheme.surface
    )
}
