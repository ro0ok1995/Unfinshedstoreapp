package com.example.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.localization.AppLanguage
import com.example.data.localization.LocalAppLanguage
import com.example.data.localization.LocalStrings
import com.example.ui.components.AppHeader
import com.example.ui.theme.AppVisualTheme
import com.example.ui.theme.FinancialCancelled
import com.example.ui.theme.FinancialCancelledContainer
import com.example.ui.theme.LocalAppThemeColors
import com.example.ui.viewmodel.ShopViewModel

enum class MoreSubDestination {
    HUB,
    STORE_INFO,
    APP_SETTINGS,
    DATA_CENTER,
    ABOUT
}

/**
 * SCREEN 5: MORE SCREEN
 * Strict 4-destination hub as per Master Project Reference:
 * 1. Store Information (معلومات المحل)
 * 2. App Settings (إعدادات التطبيق)
 * 3. Data Center (مركز البيانات)
 * 4. About (حول التطبيق)
 */
@Composable
fun MoreScreen(
    viewModel: ShopViewModel,
    modifier: Modifier = Modifier,
    onOpenDrawer: () -> Unit = {}
) {
    val strings = LocalStrings.current
    var currentSubDestination by remember { mutableStateOf(MoreSubDestination.HUB) }

    // Intercept hardware back button when inside a sub-screen
    BackHandler(enabled = currentSubDestination != MoreSubDestination.HUB) {
        currentSubDestination = MoreSubDestination.HUB
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        AnimatedContent(
            targetState = currentSubDestination,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "more_sub_transition"
        ) { dest ->
            when (dest) {
                MoreSubDestination.HUB -> MoreHubContent(
                    onNavigateTo = { currentSubDestination = it },
                    onOpenDrawer = onOpenDrawer
                )
                MoreSubDestination.STORE_INFO -> StoreInfoSubScreen(
                    viewModel = viewModel,
                    onBack = { currentSubDestination = MoreSubDestination.HUB }
                )
                MoreSubDestination.APP_SETTINGS -> AppSettingsSubScreen(
                    viewModel = viewModel,
                    onBack = { currentSubDestination = MoreSubDestination.HUB }
                )
                MoreSubDestination.DATA_CENTER -> DataCenterSubScreen(
                    viewModel = viewModel,
                    onBack = { currentSubDestination = MoreSubDestination.HUB }
                )
                MoreSubDestination.ABOUT -> AboutSubScreen(
                    viewModel = viewModel,
                    onBack = { currentSubDestination = MoreSubDestination.HUB }
                )
            }
        }
    }
}

/**
 * 1. HUB: Displays EXACTLY the 4 destinations
 */
@Composable
private fun MoreHubContent(
    onNavigateTo: (MoreSubDestination) -> Unit,
    onOpenDrawer: () -> Unit
) {
    val strings = LocalStrings.current
    val themeColors = LocalAppThemeColors.current

    Column(modifier = Modifier.fillMaxSize()) {
        AppHeader(
            title = strings.navMore,
            subtitle = strings.appSettingsTitle,
            navigationIcon = {
                IconButton(
                    onClick = onOpenDrawer,
                    modifier = Modifier.testTag("more_drawer_btn").size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = strings.drawerMore,
                        tint = Color.White
                    )
                }
            }
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // Destination 1: Store Information
            item {
                MoreDestinationCard(
                    title = strings.moreStoreInfo,
                    description = strings.moreStoreInfoDesc,
                    icon = Icons.Default.Store,
                    accentColor = themeColors.primary,
                    testTag = "more_dest_store_info",
                    onClick = { onNavigateTo(MoreSubDestination.STORE_INFO) }
                )
            }

            // Destination 2: App Settings
            item {
                MoreDestinationCard(
                    title = strings.moreAppSettings,
                    description = strings.moreAppSettingsDesc,
                    icon = Icons.Default.Tune,
                    accentColor = Color(0xFF0284C7), // Sky blue
                    testTag = "more_dest_app_settings",
                    onClick = { onNavigateTo(MoreSubDestination.APP_SETTINGS) }
                )
            }

            // Destination 3: Data Center
            item {
                MoreDestinationCard(
                    title = strings.moreDataCenter,
                    description = strings.moreDataCenterDesc,
                    icon = Icons.Default.Storage,
                    accentColor = Color(0xFFD97706), // Amber
                    testTag = "more_dest_data_center",
                    onClick = { onNavigateTo(MoreSubDestination.DATA_CENTER) }
                )
            }

            // Destination 4: About
            item {
                MoreDestinationCard(
                    title = strings.moreAbout,
                    description = strings.moreAboutDesc,
                    icon = Icons.Default.Info,
                    accentColor = Color(0xFF7C3AED), // Purple
                    testTag = "more_dest_about",
                    onClick = { onNavigateTo(MoreSubDestination.ABOUT) }
                )
            }
        }
    }
}

@Composable
private fun MoreDestinationCard(
    title: String,
    description: String,
    icon: ImageVector,
    accentColor: Color,
    testTag: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .testTag(testTag),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(accentColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/**
 * 2. SUB-SCREEN: Store Information
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StoreInfoSubScreen(
    viewModel: ShopViewModel,
    onBack: () -> Unit
) {
    val strings = LocalStrings.current
    val themeColors = LocalAppThemeColors.current
    val context = LocalContext.current
    val storeProfile by viewModel.storeProfile.collectAsStateWithLifecycle()

    var storeName by remember(storeProfile) { mutableStateOf(storeProfile.name) }
    var ownerName by remember(storeProfile) { mutableStateOf(storeProfile.ownerName) }
    var phone by remember(storeProfile) { mutableStateOf(storeProfile.phone) }
    var address by remember(storeProfile) { mutableStateOf(storeProfile.address) }
    var currency by remember(storeProfile) { mutableStateOf(storeProfile.currency) }

    Column(modifier = Modifier.fillMaxSize()) {
        AppHeader(
            title = strings.moreStoreInfo,
            subtitle = storeProfile.name.ifBlank { strings.storeInfoTitle },
            onBack = onBack
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .padding(bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = storeName,
                        onValueChange = { storeName = it },
                        label = { Text(strings.storeNameLabel) },
                        leadingIcon = { Icon(Icons.Default.Store, contentDescription = null, tint = themeColors.primary) },
                        modifier = Modifier.fillMaxWidth().testTag("input_store_name"),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = ownerName,
                        onValueChange = { ownerName = it },
                        label = { Text(strings.ownerNameLabel) },
                        leadingIcon = { Icon(Icons.Default.AccountCircle, contentDescription = null, tint = themeColors.primary) },
                        modifier = Modifier.fillMaxWidth().testTag("input_owner_name"),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text(strings.phoneLabel) },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = themeColors.primary) },
                        modifier = Modifier.fillMaxWidth().testTag("input_store_phone"),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text(strings.addressLabel) },
                        modifier = Modifier.fillMaxWidth().testTag("input_store_address"),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = currency,
                        onValueChange = { currency = it },
                        label = { Text(strings.currencyLabel) },
                        modifier = Modifier.fillMaxWidth().testTag("input_store_currency"),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            Button(
                onClick = {
                    viewModel.updateStoreProfile(
                        name = storeName,
                        owner = ownerName,
                        phone = phone,
                        address = address,
                        currency = currency
                    )
                    Toast.makeText(context, strings.savedSuccessfully, Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("save_store_profile_btn"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = themeColors.primary)
            ) {
                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(strings.save, fontWeight = FontWeight.Bold)
            }
        }
    }
}

/**
 * 3. SUB-SCREEN: App Settings (Theme, Language, Display)
 */
@Composable
private fun AppSettingsSubScreen(
    viewModel: ShopViewModel,
    onBack: () -> Unit
) {
    val strings = LocalStrings.current
    val currentLang = LocalAppLanguage.current
    val themeColors = LocalAppThemeColors.current
    val currentTheme by viewModel.appTheme.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        AppHeader(
            title = strings.moreAppSettings,
            subtitle = strings.appearanceSettingsTitle,
            onBack = onBack
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .padding(bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Language Selection Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Language, contentDescription = null, tint = themeColors.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = strings.languageSettingsTitle,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { viewModel.setLanguage(AppLanguage.ARABIC) }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = currentLang == AppLanguage.ARABIC,
                            onClick = { viewModel.setLanguage(AppLanguage.ARABIC) },
                            colors = RadioButtonDefaults.colors(selectedColor = themeColors.primary)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("العربية (Arabic)", fontWeight = FontWeight.SemiBold)
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { viewModel.setLanguage(AppLanguage.ENGLISH) }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = currentLang == AppLanguage.ENGLISH,
                            onClick = { viewModel.setLanguage(AppLanguage.ENGLISH) },
                            colors = RadioButtonDefaults.colors(selectedColor = themeColors.primary)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("English", fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            // Theme Selection Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Palette, contentDescription = null, tint = themeColors.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = strings.appearanceSettingsTitle,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    val themes = listOf(
                        AppVisualTheme.EMERALD to ("الزمردي (Emerald)"),
                        AppVisualTheme.CLASSIC_BLUE to ("الأزرق الكلاسيكي (Classic Blue)"),
                        AppVisualTheme.ROYAL_PURPLE to ("البنفسجي الملكي (Royal Purple)"),
                        AppVisualTheme.AMBER to ("العنبري الدافئ (Warm Amber)"),
                        AppVisualTheme.ROSE to ("الوردي الناعم (Rose)")
                    )

                    themes.forEach { (theme, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { viewModel.setTheme(theme) }
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = currentTheme == theme,
                                onClick = { viewModel.setTheme(theme) },
                                colors = RadioButtonDefaults.colors(selectedColor = themeColors.primary)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(label, fontSize = 14.sp)
                        }
                    }
                }
            }
        }
    }
}

/**
 * 4. SUB-SCREEN: Data Center (Backup, Restore, Clear DB)
 */
@Composable
private fun DataCenterSubScreen(
    viewModel: ShopViewModel,
    onBack: () -> Unit
) {
    val strings = LocalStrings.current
    val themeColors = LocalAppThemeColors.current
    val context = LocalContext.current
    var showClearConfirmDialog by remember { mutableStateOf(false) }

    val createBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.exportBackupToFile(uri)
            Toast.makeText(context, strings.backupExportSuccess, Toast.LENGTH_SHORT).show()
        }
    }

    val restoreBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.importBackupFromFile(uri)
            Toast.makeText(context, strings.backupImportSuccess, Toast.LENGTH_SHORT).show()
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        AppHeader(
            title = strings.moreDataCenter,
            subtitle = strings.databaseManagementTitle,
            onBack = onBack
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .padding(bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Backup Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CloudDownload, contentDescription = null, tint = themeColors.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = strings.exportBackupLabel,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = strings.backupDesc,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            createBackupLauncher.launch("SmallStore_Backup_${System.currentTimeMillis()}.json")
                        },
                        modifier = Modifier.fillMaxWidth().testTag("btn_export_backup"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = themeColors.primary)
                    ) {
                        Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(strings.exportBackupLabel, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Restore Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CloudUpload, contentDescription = null, tint = Color(0xFF0284C7))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = strings.importBackupLabel,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = strings.restoreDesc,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = {
                            restoreBackupLauncher.launch(arrayOf("application/json"))
                        },
                        modifier = Modifier.fillMaxWidth().testTag("btn_import_backup"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(strings.importBackupLabel, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Danger Zone: Clean Database
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = FinancialCancelledContainer.copy(alpha = 0.3f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, FinancialCancelled.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = FinancialCancelled)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = strings.cleanDatabaseTitle,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = FinancialCancelled
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = strings.cleanDatabaseDesc,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { showClearConfirmDialog = true },
                        modifier = Modifier.fillMaxWidth().testTag("btn_clean_database"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = FinancialCancelled)
                    ) {
                        Icon(Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(strings.cleanDatabaseTitle, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    if (showClearConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearConfirmDialog = false },
            title = { Text(strings.cleanDatabaseTitle, fontWeight = FontWeight.Bold) },
            text = { Text(strings.cleanDatabaseConfirm) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearDatabase()
                        showClearConfirmDialog = false
                        Toast.makeText(context, strings.databaseCleanedSuccess, Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FinancialCancelled)
                ) {
                    Text(strings.cleanDatabaseConfirmBtn, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmDialog = false }) {
                    Text(strings.cancel)
                }
            }
        )
    }
}

/**
 * 5. SUB-SCREEN: About (App Info, Version, Developer, Status)
 */
@Composable
private fun AboutSubScreen(
    viewModel: ShopViewModel,
    onBack: () -> Unit
) {
    val strings = LocalStrings.current
    val themeColors = LocalAppThemeColors.current
    val financialMetrics by viewModel.financialMetrics.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        AppHeader(
            title = strings.moreAbout,
            subtitle = "SmallStore v1.0.0",
            onBack = onBack
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .padding(bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
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
                            .clip(CircleShape)
                            .background(themeColors.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Store,
                            contentDescription = null,
                            tint = themeColors.primary,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "SmallStore",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = "v1.0.0 (Build 2026)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // System Status Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = strings.systemStatusTitle,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(strings.totalTransactions, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${financialMetrics.totalTransactionsCount}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(strings.totalCustomersLabel, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${financialMetrics.activeCustomersCount}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(strings.databaseStatusTitle, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(strings.databaseActiveStatus, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = themeColors.primary)
                    }
                }
            }
        }
    }
}
