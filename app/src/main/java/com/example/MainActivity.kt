package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.localization.AppLanguage
import com.example.data.localization.ArabicStrings
import com.example.data.localization.EnglishStrings
import com.example.data.localization.LocalAppLanguage
import com.example.data.localization.LocalStrings
import com.example.ui.components.FloatingCurvedBottomBar
import com.example.ui.components.LoadingDialog
import com.example.ui.components.NotificationsSheet
import com.example.ui.components.QuickActionBottomSheet
import com.example.ui.components.ThemedGlobalDrawer
import com.example.ui.screens.CustomerDetailsScreen
import com.example.ui.screens.DatabaseScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.PurchasesScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.StatementsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.ScreenDestination
import com.example.ui.viewmodel.ShopViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val viewModel: ShopViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val currentLanguage by viewModel.currentLanguage.collectAsStateWithLifecycle()
            val currentVisualTheme by viewModel.currentVisualTheme.collectAsStateWithLifecycle()
            val shopSettings by viewModel.shopSettings.collectAsStateWithLifecycle()
            val strings = if (currentLanguage == AppLanguage.ARABIC) ArabicStrings else EnglishStrings
            val layoutDirection = if (currentLanguage == AppLanguage.ARABIC) LayoutDirection.Rtl else LayoutDirection.Ltr

            val isSystemDark = isSystemInDarkTheme()
            val isDark = when {
                shopSettings.theme.equals(com.example.core.model.AppThemeMode.DARK, ignoreCase = true) -> true
                shopSettings.theme.equals(com.example.core.model.AppThemeMode.LIGHT, ignoreCase = true) -> false
                else -> isSystemDark
            }

            CompositionLocalProvider(
                LocalAppLanguage provides currentLanguage,
                LocalStrings provides strings,
                LocalLayoutDirection provides layoutDirection
            ) {
                MyApplicationTheme(
                    visualTheme = currentVisualTheme,
                    darkTheme = isDark
                ) {
                    MainAppContent(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun MainAppContent(viewModel: ShopViewModel) {
    val currentDestination by viewModel.currentDestination.collectAsStateWithLifecycle()
    val selectedCustomerId by viewModel.selectedCustomerIdForDetails.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val strings = LocalStrings.current

    val snackbarHostState = remember { SnackbarHostState() }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var showQuickActionSheet by remember { mutableStateOf(false) }
    var showNotificationsSheet by remember { mutableStateOf(false) }

    // Handle UI Events (Snackbars / Toasts)
    LaunchedEffect(Unit) {
        viewModel.uiEvents.collectLatest { message ->
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = message,
                    duration = SnackbarDuration.Short
                )
            }
        }
    }

    // Hardware/Gesture Back navigation handler
    BackHandler(enabled = drawerState.isOpen || selectedCustomerId != null || currentDestination != ScreenDestination.HOME) {
        if (drawerState.isOpen) {
            scope.launch { drawerState.close() }
        } else if (selectedCustomerId != null) {
            viewModel.closeCustomerDetails()
        } else if (currentDestination != ScreenDestination.HOME) {
            viewModel.navigateTo(ScreenDestination.HOME)
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = selectedCustomerId == null,
        drawerContent = {
            ThemedGlobalDrawer(
                viewModel = viewModel,
                onCloseDrawer = {
                    scope.launch { drawerState.close() }
                }
            )
        }
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            bottomBar = {
                if (selectedCustomerId == null) {
                    FloatingCurvedBottomBar(
                        currentDestination = currentDestination,
                        onNavigate = { dest -> viewModel.navigateTo(dest) },
                        onQuickActionClick = { showQuickActionSheet = true }
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (selectedCustomerId != null) {
                    CustomerDetailsScreen(
                        customerId = selectedCustomerId!!,
                        viewModel = viewModel
                    )
                } else {
                    AnimatedContent(
                        targetState = currentDestination,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "screen_transition"
                    ) { target ->
                        when (target) {
                            ScreenDestination.HOME -> HomeScreen(
                                viewModel = viewModel,
                                onOpenDrawer = { scope.launch { drawerState.open() } },
                                onShowNotification = { showNotificationsSheet = true }
                            )
                            ScreenDestination.PURCHASES -> PurchasesScreen(viewModel = viewModel)
                            ScreenDestination.STATEMENTS -> StatementsScreen(viewModel = viewModel)
                            ScreenDestination.DATABASE -> DatabaseScreen(
                                viewModel = viewModel,
                                onOpenDrawer = { scope.launch { drawerState.open() } },
                                onShowNotification = { showNotificationsSheet = true }
                            )
                            ScreenDestination.SETTINGS -> SettingsScreen(viewModel = viewModel)
                        }
                    }
                }

                if (isLoading) {
                    LoadingDialog()
                }

                if (showNotificationsSheet) {
                    NotificationsSheet(
                        viewModel = viewModel,
                        onDismiss = { showNotificationsSheet = false }
                    )
                }

                if (showQuickActionSheet) {
                    QuickActionBottomSheet(
                        onDismiss = { showQuickActionSheet = false },
                        onRecordTransaction = {
                            viewModel.openPurchasesDirectly()
                        },
                        onQuickPayment = {
                            viewModel.navigateTo(ScreenDestination.DATABASE)
                        }
                    )
                }
            }
        }
    }
}

