package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.example.core.model.AppVisualThemeType

enum class AppVisualTheme(val code: String) {
    BLACK_AND_WHITE(AppVisualThemeType.BLACK_AND_WHITE),
    PURPLE(AppVisualThemeType.PURPLE),
    GOLD(AppVisualThemeType.GOLD);

    companion object {
        fun fromCode(code: String?): AppVisualTheme = when (code?.lowercase()) {
            AppVisualThemeType.PURPLE, "purple" -> PURPLE
            AppVisualThemeType.GOLD, "gold" -> GOLD
            else -> BLACK_AND_WHITE // Default on first-ever launch is Black & White!
        }
    }
}

@Immutable
data class AppThemeColors(
    val visualTheme: AppVisualTheme,
    val isDark: Boolean,
    val primary: Color,
    val onPrimary: Color,
    val primaryContainer: Color,
    val onPrimaryContainer: Color,
    val secondary: Color,
    val onSecondary: Color,
    val tertiary: Color,
    val background: Color,
    val onBackground: Color,
    val surface: Color,
    val onSurface: Color,
    val surfaceVariant: Color,
    val onSurfaceVariant: Color,
    val outline: Color,
    val outlineVariant: Color,
    val cardBorder: Color,
    val glowColor: Color,
    val headerGradient: List<Color>,
    val bottomBarColor: Color,
    val bottomBarActivePill: Color,
    val bottomBarActiveIcon: Color,
    val financialDebt: Color,
    val financialDebtContainer: Color,
    val financialPayment: Color,
    val financialPaymentContainer: Color,
    val financialCash: Color,
    val financialCashContainer: Color,
    val financialCancelled: Color,
    val isMotionEnabled: Boolean
)

private val PurpleLightColors = AppThemeColors(
    visualTheme = AppVisualTheme.PURPLE,
    isDark = false,
    primary = Color(0xFF6B21A8),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFF3E8FF),
    onPrimaryContainer = Color(0xFF3B0764),
    secondary = Color(0xFF7E22CE),
    onSecondary = Color.White,
    tertiary = Color(0xFF9333EA),
    background = Color(0xFFF8F5FC),
    onBackground = Color(0xFF1E1926),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1E1926),
    surfaceVariant = Color(0xFFF1E9FA),
    onSurfaceVariant = Color(0xFF6B5E78),
    outline = Color(0xFFD8B4FE),
    outlineVariant = Color(0xFFE9D5FF),
    cardBorder = Color(0xFFE9D5FF),
    glowColor = Color(0xFF9333EA).copy(alpha = 0.2f),
    headerGradient = listOf(Color(0xFF6B21A8), Color(0xFF4C1D95)),
    bottomBarColor = Color(0xFF581C87),
    bottomBarActivePill = Color.White.copy(alpha = 0.22f),
    bottomBarActiveIcon = Color(0xFFFFD54F),
    financialDebt = Color(0xFFDC2626),
    financialDebtContainer = Color(0xFFFEE2E2),
    financialPayment = Color(0xFF16A34A),
    financialPaymentContainer = Color(0xFFDCFCE7),
    financialCash = Color(0xFF0284C7),
    financialCashContainer = Color(0xFFE0F2FE),
    financialCancelled = Color(0xFF71717A),
    isMotionEnabled = true
)

private val PurpleDarkColors = AppThemeColors(
    visualTheme = AppVisualTheme.PURPLE,
    isDark = true,
    primary = Color(0xFFC084FC),
    onPrimary = Color(0xFF2E0249),
    primaryContainer = Color(0xFF4C1D95),
    onPrimaryContainer = Color(0xFFF3E8FF),
    secondary = Color(0xFFA855F7),
    onSecondary = Color(0xFF1E0038),
    tertiary = Color(0xFFE879F9),
    background = Color(0xFF0D0714),
    onBackground = Color(0xFFF3EEF8),
    surface = Color(0xFF170F22),
    onSurface = Color(0xFFF3EEF8),
    surfaceVariant = Color(0xFF241735),
    onSurfaceVariant = Color(0xFFC4B5D4),
    outline = Color(0xFF6B3A8B),
    outlineVariant = Color(0xFF3B1E54),
    cardBorder = Color(0xFF381E52),
    glowColor = Color(0xFFA855F7).copy(alpha = 0.35f),
    headerGradient = listOf(Color(0xFF3B1254), Color(0xFF190726)),
    bottomBarColor = Color(0xFF1A0D28),
    bottomBarActivePill = Color(0xFFA855F7).copy(alpha = 0.35f),
    bottomBarActiveIcon = Color(0xFFF3E8FF),
    financialDebt = Color(0xFFF87171),
    financialDebtContainer = Color(0xFF3E1418),
    financialPayment = Color(0xFF34D399),
    financialPaymentContainer = Color(0xFF0F3624),
    financialCash = Color(0xFF38BDF8),
    financialCashContainer = Color(0xFF0E3048),
    financialCancelled = Color(0xFFA1A1AA),
    isMotionEnabled = true
)

private val GoldLightColors = AppThemeColors(
    visualTheme = AppVisualTheme.GOLD,
    isDark = false,
    primary = Color(0xFF8C6D1F),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFDF6E2),
    onPrimaryContainer = Color(0xFF3E2E07),
    secondary = Color(0xFFA27B1E),
    onSecondary = Color.White,
    tertiary = Color(0xFFB8860B),
    background = Color(0xFFFAF7F2),
    onBackground = Color(0xFF242018),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF242018),
    surfaceVariant = Color(0xFFF4EFE6),
    onSurfaceVariant = Color(0xFF756C5C),
    outline = Color(0xFFDECBA5),
    outlineVariant = Color(0xFFEBE0CA),
    cardBorder = Color(0xFFEBE0CA),
    glowColor = Color(0xFF8C6D1F).copy(alpha = 0.2f),
    headerGradient = listOf(Color(0xFF8C6D1F), Color(0xFF6B5112)),
    bottomBarColor = Color(0xFF8C6D1F),
    bottomBarActivePill = Color.White.copy(alpha = 0.22f),
    bottomBarActiveIcon = Color(0xFFFFF176),
    financialDebt = Color(0xFFB71C1C),
    financialDebtContainer = Color(0xFFFFEBEE),
    financialPayment = Color(0xFF1B5E20),
    financialPaymentContainer = Color(0xFFE8F5E9),
    financialCash = Color(0xFF01579B),
    financialCashContainer = Color(0xFFE1F5FE),
    financialCancelled = Color(0xFF757575),
    isMotionEnabled = true
)

private val GoldDarkColors = AppThemeColors(
    visualTheme = AppVisualTheme.GOLD,
    isDark = true,
    primary = Color(0xFFE5C158),
    onPrimary = Color(0xFF2B2004),
    primaryContainer = Color(0xFF4A3A16),
    onPrimaryContainer = Color(0xFFFDF4DC),
    secondary = Color(0xFFD4AF37),
    onSecondary = Color(0xFF2B2004),
    tertiary = Color(0xFFF3D079),
    background = Color(0xFF110F0C),
    onBackground = Color(0xFFF6EFE5),
    surface = Color(0xFF1B1713),
    onSurface = Color(0xFFF6EFE5),
    surfaceVariant = Color(0xFF28221B),
    onSurfaceVariant = Color(0xFFC7BCAB),
    outline = Color(0xFF615132),
    outlineVariant = Color(0xFF382E1C),
    cardBorder = Color(0xFF382E1C),
    glowColor = Color(0xFFD4AF37).copy(alpha = 0.35f),
    headerGradient = listOf(Color(0xFF332711), Color(0xFF1A1308)),
    bottomBarColor = Color(0xFF1E1914),
    bottomBarActivePill = Color(0xFFD4AF37).copy(alpha = 0.3f),
    bottomBarActiveIcon = Color(0xFFFFE082),
    financialDebt = Color(0xFFF87171),
    financialDebtContainer = Color(0xFF3B1818),
    financialPayment = Color(0xFF34D399),
    financialPaymentContainer = Color(0xFF14382A),
    financialCash = Color(0xFF38BDF8),
    financialCashContainer = Color(0xFF0E3048),
    financialCancelled = Color(0xFFA8A29E),
    isMotionEnabled = true
)

private val BlackAndWhiteLightColors = AppThemeColors(
    visualTheme = AppVisualTheme.BLACK_AND_WHITE,
    isDark = false,
    primary = Color(0xFF111111),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE5E5E5),
    onPrimaryContainer = Color(0xFF111111),
    secondary = Color(0xFF2B2B2B),
    onSecondary = Color.White,
    tertiary = Color(0xFF444444),
    background = Color(0xFFF6F6F6),
    onBackground = Color(0xFF111111),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF111111),
    surfaceVariant = Color(0xFFEEEEEE),
    onSurfaceVariant = Color(0xFF555555),
    outline = Color(0xFFCCCCCC),
    outlineVariant = Color(0xFFE0E0E0),
    cardBorder = Color(0xFFE0E0E0),
    glowColor = Color.Transparent,
    headerGradient = listOf(Color(0xFF222222), Color(0xFF111111)),
    bottomBarColor = Color(0xFF1A1A1A),
    bottomBarActivePill = Color.White.copy(alpha = 0.2f),
    bottomBarActiveIcon = Color.White,
    financialDebt = Color(0xFFC62828),
    financialDebtContainer = Color(0xFFFFEBEE),
    financialPayment = Color(0xFF2E7D32),
    financialPaymentContainer = Color(0xFFE8F5E9),
    financialCash = Color(0xFF0277BD),
    financialCashContainer = Color(0xFFE1F5FE),
    financialCancelled = Color(0xFF757575),
    isMotionEnabled = false // STRICTLY NO MOTION
)

private val BlackAndWhiteDarkColors = AppThemeColors(
    visualTheme = AppVisualTheme.BLACK_AND_WHITE,
    isDark = true,
    primary = Color(0xFFFFFFFF),
    onPrimary = Color(0xFF000000),
    primaryContainer = Color(0xFF2C2C2C),
    onPrimaryContainer = Color(0xFFFFFFFF),
    secondary = Color(0xFFE0E0E0),
    onSecondary = Color(0xFF000000),
    tertiary = Color(0xFFCCCCCC),
    background = Color(0xFF000000),
    onBackground = Color(0xFFEEEEEE),
    surface = Color(0xFF121212),
    onSurface = Color(0xFFEEEEEE),
    surfaceVariant = Color(0xFF1E1E1E),
    onSurfaceVariant = Color(0xFFAAAAAA),
    outline = Color(0xFF404040),
    outlineVariant = Color(0xFF282828),
    cardBorder = Color(0xFF282828),
    glowColor = Color.Transparent,
    headerGradient = listOf(Color(0xFF1A1A1A), Color(0xFF0D0D0D)),
    bottomBarColor = Color(0xFF141414),
    bottomBarActivePill = Color.White.copy(alpha = 0.18f),
    bottomBarActiveIcon = Color.White,
    financialDebt = Color(0xFFE57373),
    financialDebtContainer = Color(0xFF281414),
    financialPayment = Color(0xFF81C784),
    financialPaymentContainer = Color(0xFF152A18),
    financialCash = Color(0xFF64B5F6),
    financialCashContainer = Color(0xFF10283C),
    financialCancelled = Color(0xFF888888),
    isMotionEnabled = false // STRICTLY NO MOTION
)

val LocalAppThemeColors = staticCompositionLocalOf { BlackAndWhiteLightColors }
val LocalAppVisualTheme = staticCompositionLocalOf { AppVisualTheme.BLACK_AND_WHITE }

private fun getAppThemeColors(visualTheme: AppVisualTheme, darkTheme: Boolean): AppThemeColors {
    return when (visualTheme) {
        AppVisualTheme.PURPLE -> if (darkTheme) PurpleDarkColors else PurpleLightColors
        AppVisualTheme.GOLD -> if (darkTheme) GoldDarkColors else GoldLightColors
        AppVisualTheme.BLACK_AND_WHITE -> if (darkTheme) BlackAndWhiteDarkColors else BlackAndWhiteLightColors
    }
}

private fun AppThemeColors.toM3ColorScheme(): ColorScheme {
    return if (isDark) {
        darkColorScheme(
            primary = primary,
            onPrimary = onPrimary,
            primaryContainer = primaryContainer,
            onPrimaryContainer = onPrimaryContainer,
            secondary = secondary,
            onSecondary = onSecondary,
            tertiary = tertiary,
            background = background,
            onBackground = onBackground,
            surface = surface,
            onSurface = onSurface,
            surfaceVariant = surfaceVariant,
            onSurfaceVariant = onSurfaceVariant,
            outline = outline,
            outlineVariant = outlineVariant
        )
    } else {
        lightColorScheme(
            primary = primary,
            onPrimary = onPrimary,
            primaryContainer = primaryContainer,
            onPrimaryContainer = onPrimaryContainer,
            secondary = secondary,
            onSecondary = onSecondary,
            tertiary = tertiary,
            background = background,
            onBackground = onBackground,
            surface = surface,
            onSurface = onSurface,
            surfaceVariant = surfaceVariant,
            onSurfaceVariant = onSurfaceVariant,
            outline = outline,
            outlineVariant = outlineVariant
        )
    }
}

@Composable
fun MyApplicationTheme(
    visualTheme: AppVisualTheme = AppVisualTheme.BLACK_AND_WHITE,
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val themeColors = getAppThemeColors(visualTheme, darkTheme)
    val colorScheme = themeColors.toM3ColorScheme()

    CompositionLocalProvider(
        LocalAppThemeColors provides themeColors,
        LocalAppVisualTheme provides visualTheme
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
