package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.localization.LocalStrings
import com.example.ui.theme.AppVisualTheme
import com.example.ui.theme.LocalAppThemeColors
import com.example.ui.viewmodel.ScreenDestination
import com.example.ui.viewmodel.ShopViewModel

/**
 * Global Right-Opening Navigation Drawer with 3 structured sections:
 * 1. الرئيسية (Home, Accounts, Purchases, Analysis Center, More)
 * 2. التحليل (Statistics, Account Statement, Reports)
 * 3. الإدارة (Shop Info, App Settings, Data Center, About App)
 */
@Composable
fun ThemedGlobalDrawer(
    viewModel: ShopViewModel,
    onCloseDrawer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalStrings.current
    val currentDestination by viewModel.currentDestination.collectAsStateWithLifecycle()
    val shopSettings by viewModel.shopSettings.collectAsStateWithLifecycle()
    val themeColors = LocalAppThemeColors.current

    val headerBrush = when (themeColors.visualTheme) {
        AppVisualTheme.PURPLE -> Brush.verticalGradient(
            listOf(Color(0xFF3B0764), Color(0xFF2E1065))
        )
        AppVisualTheme.GOLD -> Brush.verticalGradient(
            listOf(Color(0xFF422006), Color(0xFF2E1E05))
        )
        AppVisualTheme.BLACK_AND_WHITE -> Brush.verticalGradient(
            listOf(Color(0xFF1E1E1E), Color(0xFF121212))
        )
    }

    ModalDrawerSheet(
        modifier = modifier
            .widthIn(max = 320.dp)
            .fillMaxHeight(),
        drawerContainerColor = MaterialTheme.colorScheme.surface,
        drawerContentColor = MaterialTheme.colorScheme.onSurface,
        drawerShape = RoundedCornerShape(topStart = 0.dp, bottomStart = 0.dp, topEnd = 24.dp, bottomEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .verticalScroll(rememberScrollState())
        ) {
            // ==========================================
            // DRAWER HEADER (Store Identity & Theme)
            // ==========================================
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(headerBrush)
                    .padding(horizontal = 20.dp, vertical = 22.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(
                                    when (themeColors.visualTheme) {
                                        AppVisualTheme.PURPLE -> Color(0xFFA855F7).copy(alpha = 0.35f)
                                        AppVisualTheme.GOLD -> Color(0xFFD4AF37).copy(alpha = 0.35f)
                                        AppVisualTheme.BLACK_AND_WHITE -> Color.White.copy(alpha = 0.2f)
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Storefront,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(26.dp)
                            )
                        }

                        IconButton(
                            onClick = onCloseDrawer,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.15f))
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = strings.close,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = shopSettings.storeName.ifBlank { strings.appName },
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 19.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = strings.homeTitle,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 12.sp
                        ),
                        maxLines = 1
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Theme badge
                    val themeName = when (themeColors.visualTheme) {
                        AppVisualTheme.PURPLE -> strings.themePurple
                        AppVisualTheme.GOLD -> strings.themeGold
                        AppVisualTheme.BLACK_AND_WHITE -> strings.themeBlackAndWhite
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.White.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = themeName,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ==========================================
            // SECTION 1: الرئيسية (General / Main)
            // ==========================================
            DrawerSectionHeader(title = strings.drawerSectionMain)

            DrawerItemRow(
                label = strings.drawerHome,
                icon = Icons.Filled.Home,
                isSelected = currentDestination == ScreenDestination.HOME,
                testTag = "drawer_item_home",
                onClick = {
                    viewModel.navigateTo(ScreenDestination.HOME)
                    onCloseDrawer()
                }
            )

            DrawerItemRow(
                label = strings.drawerAccounts,
                icon = Icons.Filled.People,
                isSelected = currentDestination == ScreenDestination.DATABASE,
                testTag = "drawer_item_accounts",
                onClick = {
                    viewModel.navigateTo(ScreenDestination.DATABASE)
                    onCloseDrawer()
                }
            )

            DrawerItemRow(
                label = strings.drawerPurchases,
                icon = Icons.Filled.ShoppingCart,
                isSelected = currentDestination == ScreenDestination.PURCHASES,
                testTag = "drawer_item_purchases",
                onClick = {
                    viewModel.openPurchasesDirectly()
                    onCloseDrawer()
                }
            )

            DrawerItemRow(
                label = strings.drawerAnalysis,
                icon = Icons.Filled.Assessment,
                isSelected = currentDestination == ScreenDestination.STATEMENTS,
                testTag = "drawer_item_analysis_center",
                onClick = {
                    viewModel.navigateTo(ScreenDestination.STATEMENTS)
                    onCloseDrawer()
                }
            )

            DrawerItemRow(
                label = strings.drawerMore,
                icon = Icons.Filled.MoreHoriz,
                isSelected = currentDestination == ScreenDestination.SETTINGS,
                testTag = "drawer_item_more",
                onClick = {
                    viewModel.navigateTo(ScreenDestination.SETTINGS)
                    onCloseDrawer()
                }
            )

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 20.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(8.dp))

            // ==========================================
            // SECTION 2: التحليل (Analysis & Reports)
            // ==========================================
            DrawerSectionHeader(title = strings.drawerSectionAnalysis)

            DrawerItemRow(
                label = strings.drawerStats,
                icon = Icons.Filled.BarChart,
                isSelected = false,
                testTag = "drawer_item_stats",
                onClick = {
                    viewModel.navigateTo(ScreenDestination.STATEMENTS)
                    onCloseDrawer()
                }
            )

            DrawerItemRow(
                label = strings.drawerStatements,
                icon = Icons.Filled.ReceiptLong,
                isSelected = false,
                testTag = "drawer_item_statements",
                onClick = {
                    viewModel.navigateTo(ScreenDestination.STATEMENTS)
                    onCloseDrawer()
                }
            )

            DrawerItemRow(
                label = strings.drawerReports,
                icon = Icons.Filled.PictureAsPdf,
                isSelected = false,
                testTag = "drawer_item_reports",
                onClick = {
                    viewModel.openSettingsWithTabName("REPORTS")
                    onCloseDrawer()
                }
            )

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 20.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(8.dp))

            // ==========================================
            // SECTION 3: الإدارة (Management & Administration)
            // ==========================================
            DrawerSectionHeader(title = strings.drawerSectionManagement)

            DrawerItemRow(
                label = strings.drawerShopInfo,
                icon = Icons.Filled.Storefront,
                isSelected = false,
                testTag = "drawer_item_shop_info",
                onClick = {
                    viewModel.openSettingsWithTabName("ACCOUNT")
                    onCloseDrawer()
                }
            )

            DrawerItemRow(
                label = strings.drawerAppSettings,
                icon = Icons.Filled.Palette,
                isSelected = false,
                testTag = "drawer_item_app_settings",
                onClick = {
                    viewModel.openSettingsWithTabName("APPEARANCE")
                    onCloseDrawer()
                }
            )

            DrawerItemRow(
                label = strings.drawerDataCenter,
                icon = Icons.Filled.Storage,
                isSelected = false,
                testTag = "drawer_item_data_center",
                onClick = {
                    viewModel.openSettingsWithTabName("DATA")
                    onCloseDrawer()
                }
            )

            DrawerItemRow(
                label = strings.drawerAboutApp,
                icon = Icons.Filled.Info,
                isSelected = false,
                testTag = "drawer_item_about_app",
                onClick = {
                    viewModel.openSettingsWithTabName("ABOUT")
                    onCloseDrawer()
                }
            )

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
private fun DrawerSectionHeader(
    title: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelSmall.copy(
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            letterSpacing = 0.5.sp
        ),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp, vertical = 6.dp)
    )
}

@Composable
private fun DrawerItemRow(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    testTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val themeColors = LocalAppThemeColors.current

    val containerColor = if (isSelected) {
        when (themeColors.visualTheme) {
            AppVisualTheme.PURPLE -> Color(0xFFA855F7).copy(alpha = 0.15f)
            AppVisualTheme.GOLD -> Color(0xFFD4AF37).copy(alpha = 0.15f)
            AppVisualTheme.BLACK_AND_WHITE -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
        }
    } else {
        Color.Transparent
    }

    val contentColor = if (isSelected) {
        when (themeColors.visualTheme) {
            AppVisualTheme.PURPLE -> Color(0xFF9333EA)
            AppVisualTheme.GOLD -> Color(0xFFB45309)
            AppVisualTheme.BLACK_AND_WHITE -> MaterialTheme.colorScheme.onSurface
        }
    } else {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
    }

    val iconColor = if (isSelected) {
        when (themeColors.visualTheme) {
            AppVisualTheme.PURPLE -> Color(0xFF9333EA)
            AppVisualTheme.GOLD -> Color(0xFFB45309)
            AppVisualTheme.BLACK_AND_WHITE -> MaterialTheme.colorScheme.onSurface
        }
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 2.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(containerColor)
            .clickable { onClick() }
            .minimumInteractiveComponentSize()
            .padding(horizontal = 14.dp, vertical = 10.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(22.dp)
        )

        Spacer(modifier = Modifier.width(14.dp))

        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                fontSize = 14.sp,
                color = contentColor
            ),
            modifier = Modifier.weight(1f)
        )
    }
}
