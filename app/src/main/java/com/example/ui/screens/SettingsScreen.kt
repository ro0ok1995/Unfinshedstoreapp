package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.model.Customer
import com.example.core.model.CustomerStatus
import com.example.core.model.Product
import com.example.core.model.ProductStatus
import com.example.core.model.TransactionStatus
import com.example.core.model.TransactionWithDetails
import com.example.core.services.backup.BackupPayload
import com.example.data.localization.AppLanguage
import com.example.data.localization.LocalAppLanguage
import com.example.data.localization.LocalStrings
import com.example.ui.components.AppHeader
import com.example.ui.components.StatusBadge
import com.example.ui.theme.FinancialDebt
import com.example.ui.theme.FinancialDebtContainer
import com.example.ui.theme.FinancialPayment
import com.example.ui.theme.FinancialPaymentContainer
import com.example.ui.theme.AppVisualTheme
import com.example.ui.theme.LocalAppThemeColors
import com.example.ui.viewmodel.ShopViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class SettingsTab {
    ACCOUNT,
    DATA,
    ARCHIVE,
    APPEARANCE,
    REPORTS,
    ABOUT
}

enum class ArchiveSubSection {
    CUSTOMERS,
    PRODUCTS,
    TRANSACTIONS
}

/**
 * SCREEN 6: SETTINGS (Admin & Configuration Center)
 * Features 6 fixed tabs displayed in TWO ROWS (no horizontal scrolling),
 * comprehensive store account settings, JSON/file backup & restore validation,
 * archive management with soft-delete & audit inspection, appearance switching,
 * PDF report generation, and app information.
 */
@Composable
fun SettingsScreen(
    viewModel: ShopViewModel,
    modifier: Modifier = Modifier
) {
    val strings = LocalStrings.current
    val currentLang = LocalAppLanguage.current
    val isArabic = currentLang == AppLanguage.ARABIC

    val initialTabName by viewModel.selectedSettingsTab.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableStateOf(SettingsTab.ACCOUNT) }
    
    androidx.compose.runtime.LaunchedEffect(initialTabName) {
        val tab = try {
            SettingsTab.valueOf(initialTabName)
        } catch (_: Exception) {
            SettingsTab.ACCOUNT
        }
        selectedTab = tab
    }
    
    val settings by viewModel.shopSettings.collectAsStateWithLifecycle()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            AppHeader(
                title = strings.settingsTitle,
                subtitle = when (selectedTab) {
                    SettingsTab.ACCOUNT -> strings.settingsTabAccount
                    SettingsTab.DATA -> strings.settingsTabData
                    SettingsTab.ARCHIVE -> strings.settingsTabArchive
                    SettingsTab.APPEARANCE -> strings.settingsTabAppearance
                    SettingsTab.REPORTS -> strings.settingsTabReports
                    SettingsTab.ABOUT -> strings.settingsTabAbout
                }
            )

            // =========================================================================
            // TWO-ROW 6-TAB GRID (Fixed layout: Row 1 = 3 tabs, Row 2 = 3 tabs)
            // =========================================================================
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 6.dp)
                    .background(MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(16.dp))
                    .padding(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // ROW 1: [ Account ] [ Data ] [ Archive ]
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    SettingsGridTabButton(
                        label = strings.settingsTabAccount,
                        icon = Icons.Default.Store,
                        isSelected = selectedTab == SettingsTab.ACCOUNT,
                        onClick = { selectedTab = SettingsTab.ACCOUNT },
                        modifier = Modifier.weight(1f),
                        testTag = "settings_tab_account"
                    )
                    SettingsGridTabButton(
                        label = strings.settingsTabData,
                        icon = Icons.Default.Backup,
                        isSelected = selectedTab == SettingsTab.DATA,
                        onClick = { selectedTab = SettingsTab.DATA },
                        modifier = Modifier.weight(1f),
                        testTag = "settings_tab_data"
                    )
                    SettingsGridTabButton(
                        label = strings.settingsTabArchive,
                        icon = Icons.Default.Archive,
                        isSelected = selectedTab == SettingsTab.ARCHIVE,
                        onClick = { selectedTab = SettingsTab.ARCHIVE },
                        modifier = Modifier.weight(1f),
                        testTag = "settings_tab_archive"
                    )
                }

                // ROW 2: [ Appearance ] [ Reports ] [ About ]
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    SettingsGridTabButton(
                        label = strings.settingsTabAppearance,
                        icon = Icons.Default.Palette,
                        isSelected = selectedTab == SettingsTab.APPEARANCE,
                        onClick = { selectedTab = SettingsTab.APPEARANCE },
                        modifier = Modifier.weight(1f),
                        testTag = "settings_tab_appearance"
                    )
                    SettingsGridTabButton(
                        label = strings.settingsTabReports,
                        icon = Icons.Default.PictureAsPdf,
                        isSelected = selectedTab == SettingsTab.REPORTS,
                        onClick = { selectedTab = SettingsTab.REPORTS },
                        modifier = Modifier.weight(1f),
                        testTag = "settings_tab_reports"
                    )
                    SettingsGridTabButton(
                        label = strings.settingsTabAbout,
                        icon = Icons.Default.Info,
                        isSelected = selectedTab == SettingsTab.ABOUT,
                        onClick = { selectedTab = SettingsTab.ABOUT },
                        modifier = Modifier.weight(1f),
                        testTag = "settings_tab_about"
                    )
                }
            }

            // =========================================================================
            // TAB CONTENT CONTAINER
            // =========================================================================
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                when (selectedTab) {
                    SettingsTab.ACCOUNT -> AccountTabContent(viewModel = viewModel)
                    SettingsTab.DATA -> DataTabContent(viewModel = viewModel)
                    SettingsTab.ARCHIVE -> ArchiveTabContent(viewModel = viewModel)
                    SettingsTab.APPEARANCE -> AppearanceTabContent(viewModel = viewModel)
                    SettingsTab.REPORTS -> ReportsTabContent(viewModel = viewModel)
                    SettingsTab.ABOUT -> AboutTabContent()
                }
            }
        }
    }
}

/**
 * Individual Tab Button inside the 2-row Grid.
 */
@Composable
fun SettingsGridTabButton(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = ""
) {
    Surface(
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .testTag(testTag),
        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                fontSize = 12.sp,
                maxLines = 1
            )
        }
    }
}

// =============================================================================
// TAB 1: ACCOUNT (Store Profile & Identity)
// =============================================================================
@Composable
fun AccountTabContent(viewModel: ShopViewModel) {
    val strings = LocalStrings.current
    val settings by viewModel.shopSettings.collectAsStateWithLifecycle()
    val themeColors = LocalAppThemeColors.current

    var storeName by remember(settings.storeName) { mutableStateOf(settings.storeName) }
    var ownerName by remember(settings.ownerName) { mutableStateOf(settings.ownerName) }
    var storePhone by remember(settings.phone) { mutableStateOf(settings.phone) }
    var storeAddress by remember(settings.address) { mutableStateOf(settings.address) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .padding(bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Info Banner
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = themeColors.primaryContainer.copy(alpha = 0.45f))
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = themeColors.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = strings.storeDetailsInfoNotice,
                    fontSize = 12.sp,
                    color = themeColors.primary,
                    lineHeight = 17.sp
                )
            }
        }

        // Form Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = strings.storeInfoTitle,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color(0xFF1F2937)
                )

                // Store Name (Required)
                OutlinedTextField(
                    value = storeName,
                    onValueChange = { storeName = it },
                    label = { Text("${strings.storeName} *") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("store_name_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                // Owner Name (Optional)
                OutlinedTextField(
                    value = ownerName,
                    onValueChange = { ownerName = it },
                    label = { Text(strings.ownerName) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("owner_name_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                // Store Phone (Optional)
                OutlinedTextField(
                    value = storePhone,
                    onValueChange = { storePhone = it },
                    label = { Text(strings.storePhone) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("store_phone_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                // Store Address (Optional)
                OutlinedTextField(
                    value = storeAddress,
                    onValueChange = { storeAddress = it },
                    label = { Text(strings.storeAddress) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("store_address_input"),
                    shape = RoundedCornerShape(12.dp),
                    minLines = 2,
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(4.dp))

                Button(
                    onClick = {
                        if (storeName.isNotBlank()) {
                            viewModel.saveSettings(
                                storeName = storeName.trim(),
                                ownerName = ownerName.trim(),
                                storePhone = storePhone.trim(),
                                storeAddress = storeAddress.trim()
                            )
                        }
                    },
                    enabled = storeName.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("save_store_details_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = themeColors.primary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Save,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = strings.saveStoreDetails,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

// =============================================================================
// TAB 2: DATA (Backup, Restore Validation, Reset Danger Zone)
// =============================================================================
@Composable
fun DataTabContent(viewModel: ShopViewModel) {
    val strings = LocalStrings.current
    val themeColors = LocalAppThemeColors.current

    var pendingRestorePayload by remember { mutableStateOf<BackupPayload?>(null) }
    var showResetConfirmationDialog by remember { mutableStateOf(false) }

    // File picker launcher for .json / .backup files
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { selectedUri ->
            viewModel.validateAndInspectBackup(
                uri = selectedUri,
                onSuccess = { payload ->
                    pendingRestorePayload = payload
                },
                onError = { /* handled by viewModel uiEvents */ }
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .padding(bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // BACKUP CARD
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(themeColors.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Backup,
                            contentDescription = null,
                            tint = themeColors.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = strings.backupTitle,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color(0xFF1F2937)
                        )
                        Text(
                            text = strings.backupDescription,
                            fontSize = 12.sp,
                            color = Color(0xFF6B7280)
                        )
                    }
                }

                Button(
                    onClick = { viewModel.createAndShareBackup() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .testTag("create_backup_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = themeColors.primary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = strings.createBackupButton,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }

        // RESTORE CARD
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(FinancialPaymentContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Restore,
                            contentDescription = null,
                            tint = FinancialPayment,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = strings.restoreBackup,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color(0xFF1F2937)
                        )
                        Text(
                            text = strings.restoreDescription,
                            fontSize = 12.sp,
                            color = Color(0xFF6B7280)
                        )
                    }
                }

                OutlinedButton(
                    onClick = {
                        // Accept all files / json
                        filePickerLauncher.launch("*/*")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .testTag("restore_backup_picker_btn"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.UploadFile,
                        contentDescription = null,
                        tint = themeColors.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = strings.restoreBackupButton,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.primary,
                        fontSize = 14.sp
                    )
                }
            }
        }

        // DANGER ZONE / RESET CARD
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, FinancialDebt.copy(alpha = 0.35f), RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(FinancialDebtContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteForever,
                            contentDescription = null,
                            tint = FinancialDebt,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = strings.dangerZoneTitle,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = FinancialDebt
                        )
                        Text(
                            text = strings.resetAllDataDesc,
                            fontSize = 12.sp,
                            color = Color(0xFF6B7280)
                        )
                    }
                }

                Button(
                    onClick = { showResetConfirmationDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .testTag("reset_all_data_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = FinancialDebt),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = strings.resetAllData,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }

    // ==========================================
    // RESTORE VALIDATION & CONFIRMATION MODAL
    // ==========================================
    pendingRestorePayload?.let { payload ->
        val dateFormatted = remember(payload.metadata.exportedAt) {
            try {
                SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(payload.metadata.exportedAt))
            } catch (e: Exception) {
                "Unknown Date"
            }
        }

        AlertDialog(
            onDismissRequest = { pendingRestorePayload = null },
            icon = {
                Icon(
                    imageVector = Icons.Default.Restore,
                    contentDescription = null,
                    tint = themeColors.primary,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = strings.confirmRestoreTitle,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = themeColors.primary
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = strings.restoreInspectNotice,
                        fontSize = 13.sp,
                        color = Color(0xFF374151)
                    )

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = themeColors.primaryContainer.copy(alpha = 0.35f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "📅 ${strings.backupDateLabel}: $dateFormatted",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "👥 ${strings.backupInspectCustomers} ${payload.customers.size}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "📦 ${strings.backupInspectProducts} ${payload.products.size}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "🧾 ${strings.backupInspectTransactions} ${payload.transactions.size}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Text(
                        text = "🔒 ${strings.restoreSafetyNotice}",
                        fontSize = 11.sp,
                        color = Color(0xFF059669),
                        lineHeight = 15.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.restoreFromValidatedPayload(
                            payload = payload,
                            createSafetyBackupFirst = true,
                            onSuccess = { pendingRestorePayload = null }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = themeColors.primary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(strings.confirmAndRestore, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { pendingRestorePayload = null },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(strings.cancel)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(18.dp)
        )
    }

    // ==========================================
    // DANGER ZONE / WIPE ALL DATA CONFIRMATION
    // ==========================================
    if (showResetConfirmationDialog) {
        AlertDialog(
            onDismissRequest = { showResetConfirmationDialog = false },
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
                    text = strings.resetAllData,
                    fontWeight = FontWeight.Bold,
                    color = FinancialDebt,
                    fontSize = 16.sp
                )
            },
            text = {
                Text(
                    text = strings.resetAllDataWarning,
                    fontSize = 13.sp,
                    color = Color(0xFF4B5563),
                    lineHeight = 18.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.wipeAllData()
                        showResetConfirmationDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FinancialDebt),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(strings.delete, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showResetConfirmationDialog = false },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(strings.cancel)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(18.dp)
        )
    }
}

// =============================================================================
// TAB 3: ARCHIVE (Customers, Products, Cancelled Transactions)
// =============================================================================
@Composable
fun ArchiveTabContent(viewModel: ShopViewModel) {
    val strings = LocalStrings.current
    val currentLang = LocalAppLanguage.current
    val isArabic = currentLang == AppLanguage.ARABIC
    val themeColors = LocalAppThemeColors.current

    var activeSubSection by remember { mutableStateOf(ArchiveSubSection.CUSTOMERS) }

    val allCustomers by viewModel.allDatabaseCustomers.collectAsStateWithLifecycle()
    val allProducts by viewModel.allDatabaseProducts.collectAsStateWithLifecycle()
    val cancelledTransactions by viewModel.cancelledTransactionsWithDetails.collectAsStateWithLifecycle()

    val archivedOrDeletedCustomers = remember(allCustomers) {
        allCustomers.filter { it.status == CustomerStatus.ARCHIVED || it.status == CustomerStatus.DELETED }
    }

    val archivedOrDeletedProducts = remember(allProducts) {
        allProducts.filter { it.status == ProductStatus.ARCHIVED || it.status == ProductStatus.DELETED }
    }

    var permanentDeleteCustomerId by remember { mutableStateOf<Long?>(null) }
    var permanentDeleteProductId by remember { mutableStateOf<Long?>(null) }
    var permanentDeleteTxId by remember { mutableStateOf<Long?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Sub-sections Tab Selector: [ Customers ] [ Products ] [ Transactions ]
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, shape = RoundedCornerShape(12.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .height(38.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { activeSubSection = ArchiveSubSection.CUSTOMERS },
                color = if (activeSubSection == ArchiveSubSection.CUSTOMERS) themeColors.primary else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "${strings.tabCustomers} (${archivedOrDeletedCustomers.size})",
                        color = if (activeSubSection == ArchiveSubSection.CUSTOMERS) Color.White else Color(0xFF374151),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }

            Surface(
                modifier = Modifier
                    .weight(1f)
                    .height(38.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { activeSubSection = ArchiveSubSection.PRODUCTS },
                color = if (activeSubSection == ArchiveSubSection.PRODUCTS) themeColors.primary else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "${strings.tabProducts} (${archivedOrDeletedProducts.size})",
                        color = if (activeSubSection == ArchiveSubSection.PRODUCTS) Color.White else Color(0xFF374151),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }

            Surface(
                modifier = Modifier
                    .weight(1f)
                    .height(38.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { activeSubSection = ArchiveSubSection.TRANSACTIONS },
                color = if (activeSubSection == ArchiveSubSection.TRANSACTIONS) themeColors.primary else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "${strings.cancelledTransactions} (${cancelledTransactions.size})",
                        color = if (activeSubSection == ArchiveSubSection.TRANSACTIONS) Color.White else Color(0xFF374151),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Content
        when (activeSubSection) {
            ArchiveSubSection.CUSTOMERS -> {
                if (archivedOrDeletedCustomers.isEmpty()) {
                    EmptyDataView(
                        icon = Icons.Default.Archive,
                        title = strings.noArchiveRecords,
                        message = strings.noArchiveCustomersDesc
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 100.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(archivedOrDeletedCustomers, key = { it.id }) { customer ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = customer.name,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            StatusBadge(status = customer.status)
                                        }
                                        if (customer.phone.isNotBlank()) {
                                            Text(
                                                text = customer.formattedPhoneWithCode,
                                                fontSize = 12.sp,
                                                color = Color(0xFF6B7280)
                                            )
                                        }
                                    }

                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        // Restore Button
                                        Button(
                                            onClick = { viewModel.restoreCustomer(customer.id) },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Restore,
                                                contentDescription = null,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(strings.restore, fontSize = 12.sp)
                                        }

                                        // Permanent Delete Button
                                        IconButton(
                                            onClick = { permanentDeleteCustomerId = customer.id }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.DeleteForever,
                                                contentDescription = strings.delete,
                                                tint = FinancialDebt,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            ArchiveSubSection.PRODUCTS -> {
                if (archivedOrDeletedProducts.isEmpty()) {
                    EmptyDataView(
                        icon = Icons.Default.Archive,
                        title = strings.noArchiveRecords,
                        message = strings.noArchiveProductsDesc
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 100.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(archivedOrDeletedProducts, key = { it.id }) { product ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = product.name,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            StatusBadge(status = product.status)
                                        }
                                        Text(
                                            text = product.price.format(isArabic),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = themeColors.primary
                                        )
                                    }

                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Button(
                                            onClick = { viewModel.restoreProduct(product.id) },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Restore,
                                                contentDescription = null,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(strings.restore, fontSize = 12.sp)
                                        }

                                        IconButton(
                                            onClick = { permanentDeleteProductId = product.id }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.DeleteForever,
                                                contentDescription = strings.delete,
                                                tint = FinancialDebt,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            ArchiveSubSection.TRANSACTIONS -> {
                if (cancelledTransactions.isEmpty()) {
                    EmptyDataView(
                        icon = Icons.Default.ReceiptLong,
                        title = strings.noTransactionsFound,
                        message = strings.noCancelledTransactionsNotice
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 100.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(cancelledTransactions, key = { it.transaction.id }) { item ->
                            val tx = item.transaction
                            val dateStr = remember(tx.createdAt) {
                                SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(tx.createdAt))
                            }

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "#TX-${tx.id} • ${item.customer?.name ?: strings.unknownCustomer}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                        StatusBadge(status = TransactionStatus.CANCELLED)
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "$dateStr • ${tx.totalAmount.format(isArabic)}",
                                            fontSize = 12.sp,
                                            color = Color(0xFF6B7280)
                                        )

                                        IconButton(
                                            onClick = { permanentDeleteTxId = tx.id },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.DeleteForever,
                                                contentDescription = null,
                                                tint = FinancialDebt,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }

                                    val reason = tx.cancelReason
                                    if (!reason.isNullOrBlank()) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "⚠️ ${strings.cancellationReason}: $reason",
                                            fontSize = 11.sp,
                                            color = FinancialDebt,
                                            lineHeight = 14.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Permanent Delete Confirmation Dialogs
    permanentDeleteCustomerId?.let { id ->
        AlertDialog(
            onDismissRequest = { permanentDeleteCustomerId = null },
            icon = { Icon(Icons.Default.Warning, null, tint = FinancialDebt) },
            title = { Text(strings.permanentDeleteWarning, color = FinancialDebt, fontWeight = FontWeight.Bold) },
            text = { Text(strings.permanentDeleteConfirmPrompt) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.permanentDeleteCustomer(id)
                        permanentDeleteCustomerId = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FinancialDebt)
                ) { Text(strings.delete) }
            },
            dismissButton = {
                OutlinedButton(onClick = { permanentDeleteCustomerId = null }) { Text(strings.cancel) }
            }
        )
    }

    permanentDeleteProductId?.let { id ->
        AlertDialog(
            onDismissRequest = { permanentDeleteProductId = null },
            icon = { Icon(Icons.Default.Warning, null, tint = FinancialDebt) },
            title = { Text(strings.permanentDeleteWarning, color = FinancialDebt, fontWeight = FontWeight.Bold) },
            text = { Text(strings.permanentDeleteConfirmPrompt) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.permanentDeleteProduct(id)
                        permanentDeleteProductId = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FinancialDebt)
                ) { Text(strings.delete) }
            },
            dismissButton = {
                OutlinedButton(onClick = { permanentDeleteProductId = null }) { Text(strings.cancel) }
            }
        )
    }

    permanentDeleteTxId?.let { id ->
        AlertDialog(
            onDismissRequest = { permanentDeleteTxId = null },
            icon = { Icon(Icons.Default.Warning, null, tint = FinancialDebt) },
            title = { Text(strings.permanentDeleteWarning, color = FinancialDebt, fontWeight = FontWeight.Bold) },
            text = { Text(strings.permanentDeleteConfirmPrompt) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.permanentDeleteTransaction(id)
                        permanentDeleteTxId = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FinancialDebt)
                ) { Text(strings.delete) }
            },
            dismissButton = {
                OutlinedButton(onClick = { permanentDeleteTxId = null }) { Text(strings.cancel) }
            }
        )
    }
}

// =============================================================================
// TAB 4: APPEARANCE (Theme Mode & Language RTL/LTR)
// =============================================================================
@Composable
fun AppearanceTabContent(viewModel: ShopViewModel) {
    val strings = LocalStrings.current
    val currentLang = LocalAppLanguage.current
    val settings by viewModel.shopSettings.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .padding(bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // THEME MODE (Display Mode: Light / Dark / System)
        Card(
            modifier = Modifier.fillMaxWidth().testTag("display_mode_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = strings.displayModeTitle,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                // Light Mode
                ThemeOptionRow(
                    title = strings.themeLight,
                    icon = Icons.Default.LightMode,
                    isSelected = settings.theme.equals(com.example.core.model.AppThemeMode.LIGHT, ignoreCase = true),
                    onClick = { viewModel.setThemeMode(com.example.core.model.AppThemeMode.LIGHT) }
                )

                // Dark Mode
                ThemeOptionRow(
                    title = strings.themeDark,
                    icon = Icons.Default.DarkMode,
                    isSelected = settings.theme.equals(com.example.core.model.AppThemeMode.DARK, ignoreCase = true),
                    onClick = { viewModel.setThemeMode(com.example.core.model.AppThemeMode.DARK) }
                )

                // System Default
                ThemeOptionRow(
                    title = strings.themeSystem,
                    icon = Icons.Default.BrightnessAuto,
                    isSelected = settings.theme.equals(com.example.core.model.AppThemeMode.SYSTEM, ignoreCase = true),
                    onClick = { viewModel.setThemeMode(com.example.core.model.AppThemeMode.SYSTEM) }
                )
            }
        }

        // THEMES SECTION (Visual Identity: Purple, Gold, Black & White)
        val currentVisualTheme by viewModel.currentVisualTheme.collectAsStateWithLifecycle()
        val themeColors = LocalAppThemeColors.current

        Card(
            modifier = Modifier.fillMaxWidth().testTag("themes_selector_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ColorLens,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = strings.themesTitle,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = strings.themesSubtitle,
                            fontSize = 11.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                // Three selectable theme buttons side-by-side: [ Purple ] [ Gold ] [ Black & White ]
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // PURPLE
                    ThemeVisualOptionCard(
                        title = strings.themePurple,
                        primaryColor = Color(0xFF9333EA),
                        accentColor = Color(0xFFC084FC),
                        backgroundColor = if (themeColors.isDark) Color(0xFF2D1845) else Color(0xFFF3E8FF),
                        isSelected = currentVisualTheme == AppVisualTheme.PURPLE,
                        testTag = "theme_button_purple",
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.setVisualTheme(AppVisualTheme.PURPLE) }
                    )

                    // GOLD
                    ThemeVisualOptionCard(
                        title = strings.themeGold,
                        primaryColor = Color(0xFFD4AF37),
                        accentColor = Color(0xFFE5C158),
                        backgroundColor = if (themeColors.isDark) Color(0xFF3B2E15) else Color(0xFFFDF6E2),
                        isSelected = currentVisualTheme == AppVisualTheme.GOLD,
                        testTag = "theme_button_gold",
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.setVisualTheme(AppVisualTheme.GOLD) }
                    )

                    // BLACK & WHITE
                    ThemeVisualOptionCard(
                        title = strings.themeBlackAndWhite,
                        primaryColor = if (themeColors.isDark) Color(0xFFFFFFFF) else Color(0xFF111111),
                        accentColor = Color(0xFF888888),
                        backgroundColor = if (themeColors.isDark) Color(0xFF2A2A2A) else Color(0xFFE5E5E5),
                        isSelected = currentVisualTheme == AppVisualTheme.BLACK_AND_WHITE,
                        testTag = "theme_button_black_and_white",
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.setVisualTheme(AppVisualTheme.BLACK_AND_WHITE) }
                    )
                }
            }
        }

        // LANGUAGE SELECTION
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = strings.languageTitle,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                // Arabic (RTL)
                LanguageOptionRow(
                    title = "العربية (Arabic)",
                    subtitle = "اتجاه من اليمين لليسار (RTL)",
                    isSelected = currentLang == AppLanguage.ARABIC,
                    onClick = { viewModel.setLanguage(AppLanguage.ARABIC) }
                )

                // English (LTR)
                LanguageOptionRow(
                    title = "English",
                    subtitle = "Left-to-Right layout (LTR)",
                    isSelected = currentLang == AppLanguage.ENGLISH,
                    onClick = { viewModel.setLanguage(AppLanguage.ENGLISH) }
                )
            }
        }
    }
}

@Composable
fun ThemeVisualOptionCard(
    title: String,
    primaryColor: Color,
    accentColor: Color,
    backgroundColor: Color,
    isSelected: Boolean,
    testTag: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
    val cardContainerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f) else MaterialTheme.colorScheme.surface

    Card(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(14.dp)
            )
            .testTag(testTag),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = cardContainerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 2.dp else 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Visual Preview Chip / Swatch
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(primaryColor)
                    .border(2.dp, if (isSelected) Color.White else Color.Transparent, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(accentColor)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}

@Composable
fun ThemeOptionRow(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = title,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                fontSize = 14.sp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
        }

        RadioButton(
            selected = isSelected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(
                selectedColor = MaterialTheme.colorScheme.primary,
                unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
    }
}

@Composable
fun LanguageOptionRow(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = title,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                fontSize = 14.sp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        RadioButton(
            selected = isSelected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(
                selectedColor = MaterialTheme.colorScheme.primary,
                unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
    }
}

// =============================================================================
// TAB 5: REPORTS (PDF ONLY)
// =============================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsTabContent(viewModel: ShopViewModel) {
    val strings = LocalStrings.current
    val themeColors = LocalAppThemeColors.current
    val activeCustomers by viewModel.allActiveCustomers.collectAsStateWithLifecycle()
    val activeProducts by viewModel.activeProducts.collectAsStateWithLifecycle()

    var showCustomerPickerModal by remember { mutableStateOf(false) }
    var showProductPickerModal by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .padding(bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // PDF Reports Banner Notice
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = themeColors.primaryContainer.copy(alpha = 0.45f))
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.PictureAsPdf,
                    contentDescription = null,
                    tint = themeColors.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = strings.pdfReportsCenterNotice,
                    fontSize = 12.sp,
                    color = themeColors.primary,
                    lineHeight = 17.sp
                )
            }
        }

        // 1. CUSTOMER STATEMENT REPORT
        PdfReportActionCard(
            title = strings.reportCustomerStatement,
            description = strings.reportCustomerStatementDesc,
            icon = Icons.Default.People,
            buttonText = strings.generatePdf,
            onClick = { showCustomerPickerModal = true },
            testTag = "report_customer_btn"
        )

        // 2. ALL CUSTOMERS DEBT REPORT
        PdfReportActionCard(
            title = strings.reportAllCustomersDebts,
            description = strings.reportAllDebtsDesc,
            icon = Icons.Default.Assessment,
            buttonText = strings.generatePdf,
            onClick = { viewModel.generateDebtsSummaryReport() },
            testTag = "report_all_debts_btn"
        )

        // 3. PRODUCT SALES REPORT
        PdfReportActionCard(
            title = strings.reportProductSales,
            description = strings.reportProductSalesDesc,
            icon = Icons.Default.Inventory2,
            buttonText = strings.generatePdf,
            onClick = { showProductPickerModal = true },
            testTag = "report_product_sales_btn"
        )

        // 4. ALL PRODUCTS PRICE CATALOG REPORT
        PdfReportActionCard(
            title = strings.reportAllProductsCatalog,
            description = strings.reportProductCatalogDesc,
            icon = Icons.Default.Description,
            buttonText = strings.generatePdf,
            onClick = { viewModel.generateProductsCatalogReport() },
            testTag = "report_catalog_btn"
        )
    }

    // Modal to pick Customer for Statement PDF
    if (showCustomerPickerModal) {
        AlertDialog(
            onDismissRequest = { showCustomerPickerModal = false },
            title = {
                Text(
                    text = strings.selectCustomerForStatement,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                if (activeCustomers.isEmpty()) {
                    Text(strings.noCustomersFound)
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(activeCustomers, key = { it.id }) { customer ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable {
                                        showCustomerPickerModal = false
                                        viewModel.generateCustomerStatementReport(customer)
                                    },
                                color = themeColors.primaryContainer.copy(alpha = 0.35f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.People,
                                        contentDescription = null,
                                        tint = themeColors.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = customer.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                        if (customer.phone.isNotBlank()) {
                                            Text(
                                                text = customer.formattedPhoneWithCode,
                                                fontSize = 12.sp,
                                                color = Color(0xFF6B7280)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                OutlinedButton(onClick = { showCustomerPickerModal = false }) {
                    Text(strings.cancel)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(18.dp)
        )
    }

    // Modal to pick Product for Sales History PDF
    if (showProductPickerModal) {
        AlertDialog(
            onDismissRequest = { showProductPickerModal = false },
            title = {
                Text(
                    text = strings.selectProduct,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                if (activeProducts.isEmpty()) {
                    Text(strings.noProductsFound)
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(activeProducts, key = { it.id }) { product ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable {
                                        showProductPickerModal = false
                                        viewModel.generateProductSalesReport(product)
                                    },
                                color = themeColors.primaryContainer.copy(alpha = 0.35f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Inventory2,
                                        contentDescription = null,
                                        tint = themeColors.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = product.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            text = product.price.format(true),
                                            fontSize = 12.sp,
                                            color = themeColors.primary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                OutlinedButton(onClick = { showProductPickerModal = false }) {
                    Text(strings.cancel)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(18.dp)
        )
    }
}

@Composable
fun PdfReportActionCard(
    title: String,
    description: String,
    icon: ImageVector,
    buttonText: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = ""
) {
    val themeColors = LocalAppThemeColors.current

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(themeColors.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = themeColors.primary,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color(0xFF1F2937)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = description,
                        fontSize = 11.sp,
                        color = Color(0xFF6B7280),
                        lineHeight = 15.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Button(
                onClick = onClick,
                colors = ButtonDefaults.buttonColors(containerColor = themeColors.primary),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.testTag(testTag)
            ) {
                Icon(
                    imageVector = Icons.Default.PictureAsPdf,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = buttonText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }
    }
}

// =============================================================================
// TAB 6: ABOUT (App Info, Version, Policies)
// =============================================================================
@Composable
fun AboutTabContent() {
    val strings = LocalStrings.current
    val themeColors = LocalAppThemeColors.current
    var showPrivacyPolicy by remember { mutableStateOf(false) }
    var showTermsOfUse by remember { mutableStateOf(false) }
    var showContactDev by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .padding(bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // App Branding Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(themeColors.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Store,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = strings.appNameFull,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = themeColors.primary
                )

                Text(
                    text = "v1.0.0",
                    fontSize = 13.sp,
                    color = Color(0xFF6B7280),
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = strings.developerInfo,
                    fontSize = 12.sp,
                    color = Color(0xFF9CA3AF)
                )
            }
        }

        // Information & Policy Cards
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Privacy Policy
                AboutActionRow(
                    icon = Icons.Default.PrivacyTip,
                    title = strings.privacyPolicyTitle,
                    onClick = { showPrivacyPolicy = true }
                )

                Divider(color = Color(0xFFF3F4F6))

                // Terms of Use
                AboutActionRow(
                    icon = Icons.Default.Description,
                    title = strings.termsOfUseTitle,
                    onClick = { showTermsOfUse = true }
                )

                Divider(color = Color(0xFFF3F4F6))

                // Contact Developer
                AboutActionRow(
                    icon = Icons.Default.Email,
                    title = strings.contactDeveloperTitle,
                    onClick = { showContactDev = true }
                )
            }
        }
    }

    // Privacy Policy Modal
    if (showPrivacyPolicy) {
        AlertDialog(
            onDismissRequest = { showPrivacyPolicy = false },
            icon = { Icon(Icons.Default.PrivacyTip, null, tint = themeColors.primary) },
            title = { Text(strings.privacyPolicyTitle, fontWeight = FontWeight.Bold) },
            text = { Text(strings.privacyPolicyContent, fontSize = 13.sp, lineHeight = 18.sp) },
            confirmButton = {
                Button(
                    onClick = { showPrivacyPolicy = false },
                    colors = ButtonDefaults.buttonColors(containerColor = themeColors.primary)
                ) { Text(strings.close) }
            },
            shape = RoundedCornerShape(18.dp),
            containerColor = Color.White
        )
    }

    // Terms of Use Modal
    if (showTermsOfUse) {
        AlertDialog(
            onDismissRequest = { showTermsOfUse = false },
            icon = { Icon(Icons.Default.Description, null, tint = themeColors.primary) },
            title = { Text(strings.termsOfUseTitle, fontWeight = FontWeight.Bold) },
            text = { Text(strings.termsOfUseContent, fontSize = 13.sp, lineHeight = 18.sp) },
            confirmButton = {
                Button(
                    onClick = { showTermsOfUse = false },
                    colors = ButtonDefaults.buttonColors(containerColor = themeColors.primary)
                ) { Text(strings.close) }
            },
            shape = RoundedCornerShape(18.dp),
            containerColor = Color.White
        )
    }

    // Contact Developer Modal
    if (showContactDev) {
        AlertDialog(
            onDismissRequest = { showContactDev = false },
            icon = { Icon(Icons.Default.Email, null, tint = themeColors.primary) },
            title = { Text(strings.contactDeveloperTitle, fontWeight = FontWeight.Bold) },
            text = { Text(strings.contactDeveloperContent, fontSize = 13.sp, lineHeight = 18.sp) },
            confirmButton = {
                Button(
                    onClick = { showContactDev = false },
                    colors = ButtonDefaults.buttonColors(containerColor = themeColors.primary)
                ) { Text(strings.close) }
            },
            shape = RoundedCornerShape(18.dp),
            containerColor = Color.White
        )
    }
}

@Composable
fun AboutActionRow(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit
) {
    val themeColors = LocalAppThemeColors.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = themeColors.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = title,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = Color(0xFF1F2937)
            )
        }
        Icon(
            imageVector = Icons.Default.Info,
            contentDescription = null,
            tint = Color(0xFF9CA3AF),
            modifier = Modifier.size(16.dp)
        )
    }
}
