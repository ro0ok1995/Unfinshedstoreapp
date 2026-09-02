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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.model.Customer
import com.example.core.model.CustomerStatus
import com.example.core.model.Money
import com.example.core.model.Product
import com.example.core.model.ProductStatus
import com.example.data.localization.AppLanguage
import com.example.data.localization.LocalAppLanguage
import com.example.data.localization.LocalStrings
import com.example.ui.components.AddEditCustomerDialog
import com.example.ui.components.AddEditProductDialog
import com.example.ui.components.AppHeader
import com.example.ui.components.ProductImage
import com.example.ui.components.RecordPaymentDialog
import com.example.ui.components.SearchBar
import com.example.ui.components.StatusBadge
import com.example.ui.theme.LocalAppThemeColors
import com.example.ui.theme.FinancialDebt
import com.example.ui.theme.FinancialDebtContainer
import com.example.ui.viewmodel.ShopViewModel

enum class DbMainTab {
    CUSTOMERS,
    PRODUCTS
}

/**
 * SCREEN 5: DATABASE MANAGEMENT
 * Independent tabs for Customers and Products with dedicated Status filter,
 * unified search, serial numbering, contextual more-actions menu,
 * and protected soft-delete / restore / permanent delete workflows.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatabaseScreen(
    viewModel: ShopViewModel,
    onOpenDrawer: () -> Unit = {},
    onShowNotification: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val strings = LocalStrings.current
    val themeColors = LocalAppThemeColors.current
    val currentLang = LocalAppLanguage.current
    val isArabic = currentLang == AppLanguage.ARABIC

    var activeTab by remember { mutableStateOf(DbMainTab.CUSTOMERS) }

    // Status filter options: Active (default), Archived, Deleted
    val statusOptions = listOf(
        CustomerStatus.ACTIVE to strings.statusActive,
        CustomerStatus.ARCHIVED to strings.statusArchived,
        CustomerStatus.DELETED to strings.statusDeleted
    )

    // Independent status state for Customers and Products
    var selectedCustomerStatus by remember { mutableStateOf(CustomerStatus.ACTIVE) }
    var selectedProductStatus by remember { mutableStateOf(ProductStatus.ACTIVE) }

    val customerSearchQuery by viewModel.dbCustomerSearchQuery.collectAsStateWithLifecycle()
    val productSearchQuery by viewModel.dbProductSearchQuery.collectAsStateWithLifecycle()

    val allCustomers by viewModel.allDatabaseCustomers.collectAsStateWithLifecycle()
    val allProducts by viewModel.allDatabaseProducts.collectAsStateWithLifecycle()
    val customerDebtsMap by viewModel.customerDebtsMap.collectAsStateWithLifecycle()
    val unreadNotificationCount by viewModel.unreadNotificationCount.collectAsStateWithLifecycle()

    // Filtered lists based on the selected status dropdown
    val filteredCustomers = remember(allCustomers, selectedCustomerStatus) {
        allCustomers.filter { it.status == selectedCustomerStatus }
    }

    val filteredProducts = remember(allProducts, selectedProductStatus) {
        allProducts.filter { it.status == selectedProductStatus }
    }

    // Dialog States
    var showAddCustomerDialog by remember { mutableStateOf(false) }
    var editingCustomer by remember { mutableStateOf<Customer?>(null) }
    var permanentDeleteCustomerId by remember { mutableStateOf<Long?>(null) }
    var paymentCustomer by remember { mutableStateOf<Customer?>(null) }

    var showAddProductDialog by remember { mutableStateOf(false) }
    var editingProduct by remember { mutableStateOf<Product?>(null) }
    var permanentDeleteProductId by remember { mutableStateOf<Long?>(null) }

    // Dropdown expand state
    var statusDropdownExpanded by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            AppHeader(
                title = strings.databaseTitle,
                subtitle = if (activeTab == DbMainTab.CUSTOMERS) strings.tabCustomers else strings.tabProducts,
                navigationIcon = {
                    IconButton(
                        onClick = onOpenDrawer,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.15f))
                            .testTag("db_drawer_btn")
                    ) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.MoreVert,
                            contentDescription = strings.drawerMore,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                },
                actions = {
                    Box {
                        IconButton(
                            onClick = onShowNotification,
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.15f))
                                .testTag("db_notifications_btn")
                        ) {
                            Icon(
                                imageVector = androidx.compose.material.icons.Icons.Default.Check,
                                contentDescription = strings.notifications,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        if (unreadNotificationCount > 0) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(FinancialDebt),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (unreadNotificationCount > 9) "9+" else "$unreadNotificationCount",
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            )

            // ==========================================
            // MAIN TABS: [ Customers ] [ Products ]
            // ==========================================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(14.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Customers Tab
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { activeTab = DbMainTab.CUSTOMERS }
                        .testTag("tab_customers_btn"),
                    color = if (activeTab == DbMainTab.CUSTOMERS) MaterialTheme.colorScheme.primary else Color.Transparent,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.People,
                            contentDescription = null,
                            tint = if (activeTab == DbMainTab.CUSTOMERS) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = strings.tabCustomers,
                            color = if (activeTab == DbMainTab.CUSTOMERS) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = if (activeTab == DbMainTab.CUSTOMERS) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 14.sp
                        )
                    }
                }

                // Products Tab
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { activeTab = DbMainTab.PRODUCTS }
                        .testTag("tab_products_btn"),
                    color = if (activeTab == DbMainTab.PRODUCTS) MaterialTheme.colorScheme.primary else Color.Transparent,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Inventory2,
                            contentDescription = null,
                            tint = if (activeTab == DbMainTab.PRODUCTS) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = strings.tabProducts,
                            color = if (activeTab == DbMainTab.PRODUCTS) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = if (activeTab == DbMainTab.PRODUCTS) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            // ==========================================
            // STATUS FILTER (Full-width Dropdown)
            // ==========================================
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                ExposedDropdownMenuBox(
                    expanded = statusDropdownExpanded,
                    onExpandedChange = { statusDropdownExpanded = !statusDropdownExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val currentStatusLabel = if (activeTab == DbMainTab.CUSTOMERS) {
                        when (selectedCustomerStatus) {
                            CustomerStatus.ACTIVE -> strings.statusActive
                            CustomerStatus.ARCHIVED -> strings.statusArchived
                            CustomerStatus.DELETED -> strings.statusDeleted
                            else -> strings.statusActive
                        }
                    } else {
                        when (selectedProductStatus) {
                            ProductStatus.ACTIVE -> strings.statusActive
                            ProductStatus.ARCHIVED -> strings.statusArchived
                            ProductStatus.DELETED -> strings.statusDeleted
                            else -> strings.statusActive
                        }
                    }

                    OutlinedTextField(
                        value = "${strings.statusLabel}: $currentStatusLabel",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusDropdownExpanded)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                            .testTag("status_filter_dropdown"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        ),
                        singleLine = true
                    )

                    ExposedDropdownMenu(
                        expanded = statusDropdownExpanded,
                        onDismissRequest = { statusDropdownExpanded = false }
                    ) {
                        statusOptions.forEach { (statusCode, label) ->
                            val isSelected = if (activeTab == DbMainTab.CUSTOMERS) {
                                selectedCustomerStatus == statusCode
                            } else {
                                selectedProductStatus == statusCode
                            }

                            DropdownMenuItem(
                                text = {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = label,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) themeColors.primary else Color.Unspecified
                                        )
                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = themeColors.primary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                },
                                onClick = {
                                    if (activeTab == DbMainTab.CUSTOMERS) {
                                        selectedCustomerStatus = statusCode
                                    } else {
                                        selectedProductStatus = statusCode
                                    }
                                    statusDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // ==========================================
            // SEARCH BAR
            // ==========================================
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                if (activeTab == DbMainTab.CUSTOMERS) {
                    SearchBar(
                        query = customerSearchQuery,
                        onQueryChange = { viewModel.setDbCustomerSearchQuery(it) },
                        placeholder = strings.searchCustomerHint,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    SearchBar(
                        query = productSearchQuery,
                        onQueryChange = { viewModel.setDbProductSearchQuery(it) },
                        placeholder = strings.searchProductHint,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // ==========================================
            // "ADD NEW +" ACTION HEADER
            // ==========================================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val countText = if (activeTab == DbMainTab.CUSTOMERS) {
                    "${strings.tabCustomers} (${filteredCustomers.size})"
                } else {
                    "${strings.tabProducts} (${filteredProducts.size})"
                }

                Text(
                    text = countText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = themeColors.primary
                )

                Button(
                    onClick = {
                        if (activeTab == DbMainTab.CUSTOMERS) {
                            showAddCustomerDialog = true
                        } else {
                            showAddProductDialog = true
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = themeColors.primary),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("add_new_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (activeTab == DbMainTab.CUSTOMERS) strings.addCustomer else strings.addProduct,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }

            // ==========================================
            // LIST CONTENT (CUSTOMERS / PRODUCTS)
            // ==========================================
            if (activeTab == DbMainTab.CUSTOMERS) {
                if (filteredCustomers.isEmpty()) {
                    EmptyDataView(
                        icon = Icons.Default.People,
                        title = strings.noCustomersFound,
                        message = if (customerSearchQuery.isNotBlank()) strings.noResultsMessage else strings.noCustomersFound
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 120.dp, top = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        itemsIndexed(
                            items = filteredCustomers,
                            key = { _, customer -> customer.id }
                        ) { index, customer ->
                            val debt = customerDebtsMap[customer.id] ?: Money.ZERO
                            CustomerDatabaseCard(
                                serialNumber = index + 1,
                                customer = customer,
                                outstandingDebt = debt,
                                isArabic = isArabic,
                                onClick = { viewModel.openCustomerDetails(customer.id) },
                                onSetAsHomeContext = { viewModel.setCustomerAsHomeContext(customer) },
                                onOpenPurchases = { viewModel.openPurchasesForCustomer(customer) },
                                onOpenPayment = { paymentCustomer = customer },
                                onOpenStatements = { viewModel.openStatementsForCustomer(customer) },
                                onEdit = { editingCustomer = customer },
                                onArchive = { viewModel.archiveCustomer(customer.id) },
                                onSoftDelete = { viewModel.softDeleteCustomer(customer.id) },
                                onRestore = { viewModel.restoreCustomer(customer.id) },
                                onPermanentDelete = { permanentDeleteCustomerId = customer.id }
                            )
                        }
                    }
                }
            } else {
                if (filteredProducts.isEmpty()) {
                    EmptyDataView(
                        icon = Icons.Default.Inventory2,
                        title = strings.noProductsFound,
                        message = if (productSearchQuery.isNotBlank()) strings.noResultsMessage else strings.noProductsFound
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 120.dp, top = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        itemsIndexed(
                            items = filteredProducts,
                            key = { _, product -> product.id }
                        ) { index, product ->
                            ProductDatabaseCard(
                                serialNumber = index + 1,
                                product = product,
                                isArabic = isArabic,
                                onEdit = { editingProduct = product },
                                onArchive = { viewModel.archiveProduct(product.id) },
                                onSoftDelete = { viewModel.softDeleteProduct(product.id) },
                                onRestore = { viewModel.restoreProduct(product.id) },
                                onPermanentDelete = { permanentDeleteProductId = product.id }
                            )
                        }
                    }
                }
            }
        }

        // ==========================================
        // DIALOGS
        // ==========================================

        // Add Customer Dialog
        if (showAddCustomerDialog) {
            AddEditCustomerDialog(
                customer = null,
                onDismiss = { showAddCustomerDialog = false },
                onSave = { newCust ->
                    viewModel.saveCustomer(newCust) {
                        showAddCustomerDialog = false
                    }
                }
            )
        }

        // Edit Customer Dialog
        editingCustomer?.let { cust ->
            AddEditCustomerDialog(
                customer = cust,
                onDismiss = { editingCustomer = null },
                onSave = { updated ->
                    viewModel.saveCustomer(updated) {
                        editingCustomer = null
                    }
                }
            )
        }

        // Permanent Delete Customer Confirmation Dialog
        permanentDeleteCustomerId?.let { id ->
            AlertDialog(
                onDismissRequest = { permanentDeleteCustomerId = null },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = FinancialDebt,
                        modifier = Modifier.size(36.dp)
                    )
                },
                title = {
                    Text(
                        text = strings.permanentDeleteWarning,
                        fontWeight = FontWeight.Bold,
                        color = FinancialDebt,
                        fontSize = 16.sp
                    )
                },
                text = {
                    Text(
                        text = strings.permanentDeleteConfirmPrompt,
                        fontSize = 13.sp,
                        color = Color(0xFF4B5563)
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.permanentDeleteCustomer(id)
                            permanentDeleteCustomerId = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = FinancialDebt),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(strings.delete, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = { permanentDeleteCustomerId = null },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(strings.cancel)
                    }
                },
                containerColor = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(18.dp)
            )
        }

        // Payment Dialog
        paymentCustomer?.let { cust ->
            val debt = customerDebtsMap[cust.id] ?: Money.ZERO
            RecordPaymentDialog(
                customer = cust,
                currentDebt = debt,
                onDismiss = { paymentCustomer = null },
                onConfirm = { amount, note ->
                    viewModel.submitPayment(cust.id, amount, note) {
                        paymentCustomer = null
                    }
                }
            )
        }

        // Add Product Dialog
        if (showAddProductDialog) {
            AddEditProductDialog(
                product = null,
                onDismiss = { showAddProductDialog = false },
                onSave = { newProd ->
                    viewModel.saveProduct(newProd) {
                        showAddProductDialog = false
                    }
                }
            )
        }

        // Edit Product Dialog
        editingProduct?.let { prod ->
            AddEditProductDialog(
                product = prod,
                onDismiss = { editingProduct = null },
                onSave = { updated ->
                    viewModel.saveProduct(updated) {
                        editingProduct = null
                    }
                }
            )
        }

        // Permanent Delete Product Confirmation Dialog
        permanentDeleteProductId?.let { id ->
            AlertDialog(
                onDismissRequest = { permanentDeleteProductId = null },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = FinancialDebt,
                        modifier = Modifier.size(36.dp)
                    )
                },
                title = {
                    Text(
                        text = strings.permanentDeleteWarning,
                        fontWeight = FontWeight.Bold,
                        color = FinancialDebt,
                        fontSize = 16.sp
                    )
                },
                text = {
                    Text(
                        text = strings.permanentDeleteConfirmPrompt,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.permanentDeleteProduct(id)
                            permanentDeleteProductId = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = FinancialDebt),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(strings.delete, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = { permanentDeleteProductId = null },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(strings.cancel)
                    }
                },
                containerColor = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(18.dp)
            )
        }
    }
}

/**
 * CUSTOMER CARD FOR DATABASE TAB
 * Contains: Serial number, customer name, phone, outstanding debt (if any),
 * quick action buttons, and a 3-dots More menu.
 */
@Composable
fun CustomerDatabaseCard(
    serialNumber: Int,
    customer: Customer,
    outstandingDebt: Money,
    isArabic: Boolean,
    onClick: () -> Unit,
    onSetAsHomeContext: () -> Unit,
    onOpenPurchases: () -> Unit,
    onOpenPayment: () -> Unit,
    onOpenStatements: () -> Unit,
    onEdit: () -> Unit,
    onArchive: () -> Unit,
    onSoftDelete: () -> Unit,
    onRestore: () -> Unit,
    onPermanentDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalStrings.current
    val themeColors = LocalAppThemeColors.current
    var menuExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("customer_db_card_${customer.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
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
                // Serial Number & Customer Info
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Serial Number badge
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(themeColors.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "#$serialNumber",
                            color = themeColors.primary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = customer.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            StatusBadge(status = customer.status)
                        }

                        Spacer(modifier = Modifier.height(3.dp))

                        if (customer.phone.isNotBlank()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Phone,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = customer.formattedPhoneWithCode,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Outstanding Debt display
                        Spacer(modifier = Modifier.height(3.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${strings.currentDebt}: ",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = outstandingDebt.format(isArabic),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (outstandingDebt.isPositive()) FinancialDebt else Color(0xFF16A34A)
                            )
                        }
                    }
                }

                // More Menu (⋮)
                Box {
                    IconButton(
                        onClick = { menuExpanded = true },
                        modifier = Modifier.testTag("customer_more_menu_${customer.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Actions Menu",
                            tint = themeColors.primary
                        )
                    }

                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(strings.viewCustomerProfile) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = themeColors.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            onClick = {
                                menuExpanded = false
                                onClick()
                            }
                        )

                        if (customer.status == CustomerStatus.ACTIVE) {
                            DropdownMenuItem(
                                text = { Text(strings.setAsHomeCustomer) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = themeColors.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                onClick = {
                                    menuExpanded = false
                                    onSetAsHomeContext()
                                }
                            )

                            DropdownMenuItem(
                                text = { Text(strings.recordPurchase) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null,
                                        tint = themeColors.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                onClick = {
                                    menuExpanded = false
                                    onOpenPurchases()
                                }
                            )

                            DropdownMenuItem(
                                text = { Text(strings.recordPayment) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Phone,
                                        contentDescription = null,
                                        tint = Color(0xFF16A34A),
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                onClick = {
                                    menuExpanded = false
                                    onOpenPayment()
                                }
                            )

                            DropdownMenuItem(
                                text = { Text(strings.viewAccountStatement) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Inventory2,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                onClick = {
                                    menuExpanded = false
                                    onOpenStatements()
                                }
                            )

                            DropdownMenuItem(
                                text = { Text(strings.editCustomer) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = null,
                                        tint = themeColors.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                onClick = {
                                    menuExpanded = false
                                    onEdit()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(strings.archiveCustomer) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Archive,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                onClick = {
                                    menuExpanded = false
                                    onArchive()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(strings.deleteCustomer, color = FinancialDebt) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = null,
                                        tint = FinancialDebt,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                onClick = {
                                    menuExpanded = false
                                    onSoftDelete()
                                }
                            )
                        } else if (customer.status == CustomerStatus.ARCHIVED) {
                            DropdownMenuItem(
                                text = { Text(strings.restore) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Restore,
                                        contentDescription = null,
                                        tint = Color(0xFF16A34A),
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                onClick = {
                                    menuExpanded = false
                                    onRestore()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(strings.editCustomer) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = null,
                                        tint = themeColors.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                onClick = {
                                    menuExpanded = false
                                    onEdit()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(strings.deleteCustomer, color = FinancialDebt) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = null,
                                        tint = FinancialDebt,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                onClick = {
                                    menuExpanded = false
                                    onSoftDelete()
                                }
                            )
                        } else if (customer.status == CustomerStatus.DELETED) {
                            DropdownMenuItem(
                                text = { Text(strings.restore) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Restore,
                                        contentDescription = null,
                                        tint = Color(0xFF16A34A),
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                onClick = {
                                    menuExpanded = false
                                    onRestore()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(strings.delete, color = FinancialDebt, fontWeight = FontWeight.Bold) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.DeleteForever,
                                        contentDescription = null,
                                        tint = FinancialDebt,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                onClick = {
                                    menuExpanded = false
                                    onPermanentDelete()
                                }
                            )
                        }
                    }
                }
            }

            // Quick action buttons for ACTIVE customers
            if (customer.status == CustomerStatus.ACTIVE) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onOpenPurchases,
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(32.dp)
                    ) {
                        Text(
                            text = "+ ${strings.recordPurchase}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }

                    OutlinedButton(
                        onClick = onOpenPayment,
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(32.dp)
                    ) {
                        Text(
                            text = strings.recordPayment,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }

                    OutlinedButton(
                        onClick = onSetAsHomeContext,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .height(32.dp)
                    ) {
                        Text(
                            text = strings.setAsHomeCustomer,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

/**
 * PRODUCT CARD FOR DATABASE TAB
 * Contains: Product placeholder icon / image, name, price in ₪, status, and 3-dots More menu.
 */
@Composable
fun ProductDatabaseCard(
    serialNumber: Int,
    product: Product,
    isArabic: Boolean,
    onEdit: () -> Unit,
    onArchive: () -> Unit,
    onSoftDelete: () -> Unit,
    onRestore: () -> Unit,
    onPermanentDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalStrings.current
    val themeColors = LocalAppThemeColors.current
    var menuExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("product_db_card_${product.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Product info & Image / Placeholder
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Product Icon / Avatar / Custom Image
                ProductImage(
                    imagePath = product.imagePath,
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(10.dp)),
                    contentDescription = product.name
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = product.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        StatusBadge(status = product.status)
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = product.price.format(isArabic),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = themeColors.primary
                    )
                }
            }

            // More Actions Menu
            Box {
                IconButton(
                    onClick = { menuExpanded = true },
                    modifier = Modifier.testTag("product_more_menu_${product.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Product Actions Menu",
                        tint = themeColors.primary
                    )
                }

                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    when (product.status) {
                        ProductStatus.ACTIVE -> {
                            DropdownMenuItem(
                                text = { Text(strings.editProduct) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = null,
                                        tint = themeColors.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                onClick = {
                                    menuExpanded = false
                                    onEdit()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(strings.archiveProduct) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Archive,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                onClick = {
                                    menuExpanded = false
                                    onArchive()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(strings.deleteProduct, color = FinancialDebt) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = null,
                                        tint = FinancialDebt,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                onClick = {
                                    menuExpanded = false
                                    onSoftDelete()
                                }
                            )
                        }
                        ProductStatus.ARCHIVED -> {
                            DropdownMenuItem(
                                text = { Text(strings.restore) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Restore,
                                        contentDescription = null,
                                        tint = Color(0xFF16A34A),
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                onClick = {
                                    menuExpanded = false
                                    onRestore()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(strings.editProduct) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = null,
                                        tint = themeColors.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                onClick = {
                                    menuExpanded = false
                                    onEdit()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(strings.deleteProduct, color = FinancialDebt) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = null,
                                        tint = FinancialDebt,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                onClick = {
                                    menuExpanded = false
                                    onSoftDelete()
                                }
                            )
                        }
                        ProductStatus.DELETED -> {
                            DropdownMenuItem(
                                text = { Text(strings.restore) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Restore,
                                        contentDescription = null,
                                        tint = Color(0xFF16A34A),
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                onClick = {
                                    menuExpanded = false
                                    onRestore()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(strings.delete, color = FinancialDebt, fontWeight = FontWeight.Bold) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.DeleteForever,
                                        contentDescription = null,
                                        tint = FinancialDebt,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                onClick = {
                                    menuExpanded = false
                                    onPermanentDelete()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyDataView(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    message: String,
    modifier: Modifier = Modifier
) {
    val themeColors = LocalAppThemeColors.current
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(themeColors.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = themeColors.primary,
                modifier = Modifier.size(32.dp)
            )
        }
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = title,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = message,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
