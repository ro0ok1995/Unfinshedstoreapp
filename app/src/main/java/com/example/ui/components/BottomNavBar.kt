package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.Assessment
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.People
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.localization.LocalStrings
import com.example.ui.theme.AppVisualTheme
import com.example.ui.theme.LocalAppThemeColors
import com.example.ui.viewmodel.ScreenDestination

data class NavItemData(
    val destination: ScreenDestination,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
)

/**
 * Modern, Floating, Rounded, 5-Element Bottom Navigation Bar with Prominent '+' Central Action Button.
 * 1. Home
 * 2. Accounts
 * 3. '+' (Quick Action Modal Trigger)
 * 4. Analysis Center
 * 5. More
 */
@Composable
fun FloatingCurvedBottomBar(
    currentDestination: ScreenDestination,
    onNavigate: (ScreenDestination) -> Unit,
    onQuickActionClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalStrings.current
    val themeColors = LocalAppThemeColors.current

    val barBrush = remember(themeColors.visualTheme, themeColors.isDark) {
        when (themeColors.visualTheme) {
            AppVisualTheme.PURPLE -> {
                if (themeColors.isDark) {
                    Brush.horizontalGradient(
                        listOf(Color(0xFF220835), Color(0xFF140321), Color(0xFF220835))
                    )
                } else {
                    Brush.horizontalGradient(
                        listOf(Color(0xFF4C1D95), Color(0xFF3B0764), Color(0xFF4C1D95))
                    )
                }
            }
            AppVisualTheme.GOLD -> {
                if (themeColors.isDark) {
                    Brush.horizontalGradient(
                        listOf(Color(0xFF2E2108), Color(0xFF191204), Color(0xFF2E2108))
                    )
                } else {
                    Brush.horizontalGradient(
                        listOf(Color(0xFF5C4712), Color(0xFF3E2F0B), Color(0xFF5C4712))
                    )
                }
            }
            AppVisualTheme.BLACK_AND_WHITE -> {
                val c = if (themeColors.isDark) Color(0xFF141414) else Color(0xFF1F1F1F)
                Brush.linearGradient(listOf(c, c))
            }
        }
    }

    val barBorderColor = remember(themeColors.visualTheme) {
        when (themeColors.visualTheme) {
            AppVisualTheme.PURPLE -> Color(0xFFA855F7).copy(alpha = 0.25f)
            AppVisualTheme.GOLD -> Color(0xFFD4AF37).copy(alpha = 0.25f)
            AppVisualTheme.BLACK_AND_WHITE -> Color.White.copy(alpha = 0.12f)
        }
    }

    val barElevation = if (themeColors.visualTheme == AppVisualTheme.BLACK_AND_WHITE) 4.dp else 16.dp
    val barSpotColor = when (themeColors.visualTheme) {
        AppVisualTheme.PURPLE -> Color(0xFFA855F7).copy(alpha = 0.4f)
        AppVisualTheme.GOLD -> Color(0xFFD4AF37).copy(alpha = 0.4f)
        AppVisualTheme.BLACK_AND_WHITE -> Color.Black.copy(alpha = 0.25f)
    }

    val plusButtonBg = when (themeColors.visualTheme) {
        AppVisualTheme.PURPLE -> Brush.linearGradient(listOf(Color(0xFFA855F7), Color(0xFF7C3AED)))
        AppVisualTheme.GOLD -> Brush.linearGradient(listOf(Color(0xFFF59E0B), Color(0xFFD97706)))
        AppVisualTheme.BLACK_AND_WHITE -> Brush.linearGradient(listOf(Color.White, Color(0xFFEEEEEE)))
    }

    val plusIconColor = when (themeColors.visualTheme) {
        AppVisualTheme.BLACK_AND_WHITE -> Color.Black
        else -> Color.White
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = barElevation,
                    shape = RoundedCornerShape(26.dp),
                    spotColor = barSpotColor
                ),
            shape = RoundedCornerShape(26.dp),
            color = Color.Transparent
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(26.dp))
                    .background(barBrush)
                    .border(1.dp, barBorderColor, RoundedCornerShape(26.dp))
            ) {
                // Subtle Canvas Pattern inside Floating Bottom Bar for Purple and Gold
                if (themeColors.visualTheme != AppVisualTheme.BLACK_AND_WHITE) {
                    Canvas(modifier = Modifier.matchParentSize()) {
                        val w = size.width
                        val h = size.height

                        when (themeColors.visualTheme) {
                            AppVisualTheme.PURPLE -> {
                                val strokeColor = Color(0xFFD8B4FE).copy(alpha = 0.06f)
                                var x = 0f
                                while (x < w + h) {
                                    drawLine(
                                        color = strokeColor,
                                        start = Offset(x, 0f),
                                        end = Offset(x - h, h),
                                        strokeWidth = 1f
                                    )
                                    x += 40f
                                }
                            }
                            AppVisualTheme.GOLD -> {
                                val strokeColor = Color(0xFFFFE082).copy(alpha = 0.06f)
                                var x = 0f
                                while (x < w + 50f) {
                                    val path = Path().apply {
                                        moveTo(x, 0f)
                                        lineTo(x + 20f, h / 2)
                                        lineTo(x, h)
                                    }
                                    drawPath(path, strokeColor, style = Stroke(1f))
                                    x += 50f
                                }
                            }
                            AppVisualTheme.BLACK_AND_WHITE -> {}
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(68.dp)
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 1. HOME
                    NavDestinationItem(
                        item = NavItemData(
                            destination = ScreenDestination.HOME,
                            label = strings.navHome,
                            selectedIcon = Icons.Filled.Home,
                            unselectedIcon = Icons.Outlined.Home,
                            testTag = "nav_item_home"
                        ),
                        isSelected = currentDestination == ScreenDestination.HOME,
                        onNavigate = onNavigate,
                        modifier = Modifier.weight(1f)
                    )

                    // 2. ACCOUNTS
                    NavDestinationItem(
                        item = NavItemData(
                            destination = ScreenDestination.DATABASE,
                            label = strings.navAccounts,
                            selectedIcon = Icons.Filled.People,
                            unselectedIcon = Icons.Outlined.People,
                            testTag = "nav_item_accounts"
                        ),
                        isSelected = currentDestination == ScreenDestination.DATABASE,
                        onNavigate = onNavigate,
                        modifier = Modifier.weight(1f)
                    )

                    // 3. '+' CENTRAL PRIMARY ACTION BUTTON
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            modifier = Modifier
                                .size(48.dp)
                                .shadow(
                                    elevation = if (themeColors.visualTheme == AppVisualTheme.BLACK_AND_WHITE) 2.dp else 8.dp,
                                    shape = CircleShape,
                                    spotColor = when (themeColors.visualTheme) {
                                        AppVisualTheme.PURPLE -> Color(0xFFA855F7)
                                        AppVisualTheme.GOLD -> Color(0xFFF59E0B)
                                        AppVisualTheme.BLACK_AND_WHITE -> Color.Black
                                    }
                                )
                                .clip(CircleShape)
                                .clickable { onQuickActionClick() }
                                .testTag("nav_item_quick_action_plus"),
                            shape = CircleShape,
                            color = Color.Transparent
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(plusButtonBg)
                                    .border(
                                        width = 1.5.dp,
                                        color = Color.White.copy(alpha = 0.35f),
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Add,
                                    contentDescription = strings.quickActionTitle,
                                    tint = plusIconColor,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                    }

                    // 4. ANALYSIS CENTER
                    NavDestinationItem(
                        item = NavItemData(
                            destination = ScreenDestination.STATEMENTS,
                            label = strings.navAnalysisCenter,
                            selectedIcon = Icons.Filled.Assessment,
                            unselectedIcon = Icons.Outlined.Assessment,
                            testTag = "nav_item_analysis_center"
                        ),
                        isSelected = currentDestination == ScreenDestination.STATEMENTS,
                        onNavigate = onNavigate,
                        modifier = Modifier.weight(1f)
                    )

                    // 5. MORE
                    NavDestinationItem(
                        item = NavItemData(
                            destination = ScreenDestination.SETTINGS,
                            label = strings.navMore,
                            selectedIcon = Icons.Filled.MoreHoriz,
                            unselectedIcon = Icons.Outlined.MoreHoriz,
                            testTag = "nav_item_more"
                        ),
                        isSelected = currentDestination == ScreenDestination.SETTINGS,
                        onNavigate = onNavigate,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun NavDestinationItem(
    item: NavItemData,
    isSelected: Boolean,
    onNavigate: (ScreenDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    val themeColors = LocalAppThemeColors.current

    val targetPillColor = if (isSelected) {
        when (themeColors.visualTheme) {
            AppVisualTheme.PURPLE -> Color(0xFFA855F7).copy(alpha = 0.28f)
            AppVisualTheme.GOLD -> Color(0xFFD4AF37).copy(alpha = 0.28f)
            AppVisualTheme.BLACK_AND_WHITE -> Color.White.copy(alpha = 0.18f)
        }
    } else {
        Color.Transparent
    }

    val activePillColor by animateColorAsState(
        targetValue = targetPillColor,
        animationSpec = if (themeColors.isMotionEnabled) spring(stiffness = Spring.StiffnessMediumLow) else snap(),
        label = "pill_color"
    )

    val targetContentColor = if (isSelected) {
        when (themeColors.visualTheme) {
            AppVisualTheme.PURPLE -> Color(0xFFF3E8FF)
            AppVisualTheme.GOLD -> Color(0xFFFFE082)
            AppVisualTheme.BLACK_AND_WHITE -> Color.White
        }
    } else {
        Color.White.copy(alpha = 0.65f)
    }

    val contentColor by animateColorAsState(
        targetValue = targetContentColor,
        animationSpec = if (themeColors.isMotionEnabled) spring(stiffness = Spring.StiffnessMediumLow) else snap(),
        label = "content_color"
    )

    val iconScale by animateFloatAsState(
        targetValue = if (isSelected && themeColors.isMotionEnabled) 1.08f else 1f,
        animationSpec = if (themeColors.isMotionEnabled) spring(stiffness = Spring.StiffnessMediumLow) else snap(),
        label = "icon_scale"
    )

    val interactionSource = remember { MutableInteractionSource() }

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                onNavigate(item.destination)
            }
            .minimumInteractiveComponentSize()
            .padding(vertical = 4.dp)
            .testTag(item.testTag),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val pillBorderColor = if (isSelected && themeColors.visualTheme != AppVisualTheme.BLACK_AND_WHITE) {
            when (themeColors.visualTheme) {
                AppVisualTheme.PURPLE -> Color(0xFFC084FC).copy(alpha = 0.4f)
                AppVisualTheme.GOLD -> Color(0xFFE5C158).copy(alpha = 0.4f)
                else -> Color.Transparent
            }
        } else Color.Transparent

        Box(
            modifier = Modifier
                .scale(iconScale)
                .clip(CircleShape)
                .background(activePillColor)
                .border(if (isSelected && themeColors.visualTheme != AppVisualTheme.BLACK_AND_WHITE) 1.dp else 0.dp, pillBorderColor, CircleShape)
                .padding(horizontal = 12.dp, vertical = 3.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                contentDescription = item.label,
                tint = contentColor,
                modifier = Modifier.size(20.dp)
            )
        }

        Text(
            text = item.label,
            color = contentColor,
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            textAlign = TextAlign.Center,
            maxLines = 1,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

/**
 * Action Sheet modal shown when tapping the central '+' navigation button.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickActionBottomSheet(
    onDismiss: () -> Unit,
    onRecordTransaction: () -> Unit,
    onQuickPayment: () -> Unit
) {
    val strings = LocalStrings.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val themeColors = LocalAppThemeColors.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .padding(bottom = 24.dp)
        ) {
            Text(
                text = strings.quickActionTitle,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Option 1: Record Transaction / Purchase
            QuickActionOptionCard(
                title = strings.quickActionNewPurchase,
                description = strings.quickActionNewPurchaseDesc,
                icon = Icons.Filled.ShoppingCart,
                iconBgColor = when (themeColors.visualTheme) {
                    AppVisualTheme.PURPLE -> Color(0xFFA855F7).copy(alpha = 0.15f)
                    AppVisualTheme.GOLD -> Color(0xFFF59E0B).copy(alpha = 0.15f)
                    AppVisualTheme.BLACK_AND_WHITE -> MaterialTheme.colorScheme.primaryContainer
                },
                iconTint = when (themeColors.visualTheme) {
                    AppVisualTheme.PURPLE -> Color(0xFF9333EA)
                    AppVisualTheme.GOLD -> Color(0xFFD97706)
                    AppVisualTheme.BLACK_AND_WHITE -> MaterialTheme.colorScheme.primary
                },
                testTag = "quick_action_new_purchase",
                onClick = {
                    onDismiss()
                    onRecordTransaction()
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Option 2: Quick Payment
            QuickActionOptionCard(
                title = strings.quickActionQuickPayment,
                description = strings.quickActionQuickPaymentDesc,
                icon = Icons.Filled.Payments,
                iconBgColor = Color(0xFF16A34A).copy(alpha = 0.15f),
                iconTint = Color(0xFF16A34A),
                testTag = "quick_action_quick_payment",
                onClick = {
                    onDismiss()
                    onQuickPayment()
                }
            )
        }
    }
}

@Composable
private fun QuickActionOptionCard(
    title: String,
    description: String,
    icon: ImageVector,
    iconBgColor: Color,
    iconTint: Color,
    testTag: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .testTag(testTag),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(iconBgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }
    }
}

