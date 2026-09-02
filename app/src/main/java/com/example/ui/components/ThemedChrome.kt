package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
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
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AppVisualTheme
import com.example.ui.theme.LocalAppThemeColors

/**
 * Top Header Container that renders the theme's distinct personality:
 * - Purple: Futuristic violet gradient + subtle abstract geometric pattern + soft glow depth.
 * - Gold: Elegant gold/bronze gradient + subtle luxury art-deco lines + soft golden highlight.
 * - Black & White: Flat, clean monochrome + static utilitarian styling without glow or animations.
 */
@Composable
fun ThemedHeaderBox(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val themeColors = LocalAppThemeColors.current

    val backgroundBrush = remember(themeColors.visualTheme, themeColors.isDark) {
        when (themeColors.visualTheme) {
            AppVisualTheme.PURPLE -> {
                if (themeColors.isDark) {
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF38104E),
                            Color(0xFF200531)
                        )
                    )
                } else {
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF6B21A8),
                            Color(0xFF4C1D95)
                        )
                    )
                }
            }
            AppVisualTheme.GOLD -> {
                if (themeColors.isDark) {
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF3D2D0B),
                            Color(0xFF221804)
                        )
                    )
                } else {
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF8C6D1F),
                            Color(0xFF634D12)
                        )
                    )
                }
            }
            AppVisualTheme.BLACK_AND_WHITE -> {
                if (themeColors.isDark) {
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF181818),
                            Color(0xFF0D0D0D)
                        )
                    )
                } else {
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF222222),
                            Color(0xFF141414)
                        )
                    )
                }
            }
        }
    }

    val shadowElevation = when (themeColors.visualTheme) {
        AppVisualTheme.BLACK_AND_WHITE -> 3.dp
        else -> 6.dp
    }

    val shadowSpotColor = when (themeColors.visualTheme) {
        AppVisualTheme.PURPLE -> Color(0xFFA855F7).copy(alpha = 0.35f)
        AppVisualTheme.GOLD -> Color(0xFFD4AF37).copy(alpha = 0.35f)
        AppVisualTheme.BLACK_AND_WHITE -> Color.Black.copy(alpha = 0.25f)
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color.Transparent,
        shadowElevation = shadowElevation,
        tonalElevation = 0.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(backgroundBrush)
        ) {
            // Subtle theme pattern overlay (Canvas) - Strictly restricted to Header
            if (themeColors.visualTheme != AppVisualTheme.BLACK_AND_WHITE) {
                Canvas(
                    modifier = Modifier
                        .matchParentSize()
                ) {
                    val width = size.width
                    val height = size.height

                    when (themeColors.visualTheme) {
                        AppVisualTheme.PURPLE -> {
                            // Futuristic subtle geometric lines & circuit nodes with low alpha
                            val lineColor = Color(0xFFD8B4FE).copy(alpha = 0.09f)
                            val dotColor = Color(0xFFE9D5FF).copy(alpha = 0.14f)

                            // Diagonal geometric grid lines
                            val step = 45f
                            var x = -height
                            while (x < width + height) {
                                drawLine(
                                    color = lineColor,
                                    start = Offset(x, 0f),
                                    end = Offset(x + height * 0.8f, height),
                                    strokeWidth = 1.2f
                                )
                                x += step
                            }

                            // Abstract geometric nodes
                            drawCircle(dotColor, radius = 3.5f, center = Offset(width * 0.85f, height * 0.3f))
                            drawCircle(dotColor, radius = 2.5f, center = Offset(width * 0.92f, height * 0.7f))
                            drawCircle(dotColor, radius = 3f, center = Offset(width * 0.15f, height * 0.8f))
                            drawCircle(dotColor, radius = 2f, center = Offset(width * 0.08f, height * 0.25f))

                            // Subtle bottom highlight line
                            drawLine(
                                color = Color(0xFFC084FC).copy(alpha = 0.25f),
                                start = Offset(0f, height),
                                end = Offset(width, height),
                                strokeWidth = 1.5f
                            )
                        }

                        AppVisualTheme.GOLD -> {
                            // Elegant Luxury Art-Deco Chevron & Diamond Pattern
                            val goldLineColor = Color(0xFFFFE082).copy(alpha = 0.09f)
                            val accentLineColor = Color(0xFFFFF9C4).copy(alpha = 0.14f)

                            // Diamond chevron lattice
                            val latticeSpacing = 50f
                            var y = 0f
                            while (y < height + 50f) {
                                val path = Path().apply {
                                    var currX = 0f
                                    moveTo(currX, y)
                                    while (currX < width + 60f) {
                                        lineTo(currX + 25f, y - 18f)
                                        lineTo(currX + 50f, y)
                                        currX += 50f
                                    }
                                }
                                drawPath(
                                    path = path,
                                    color = goldLineColor,
                                    style = Stroke(width = 1f)
                                )
                                y += latticeSpacing
                            }

                            // Luxury subtle bottom highlight line
                            drawLine(
                                color = Color(0xFFE5C158).copy(alpha = 0.3f),
                                start = Offset(0f, height),
                                end = Offset(width, height),
                                strokeWidth = 1.5f
                            )
                        }

                        AppVisualTheme.BLACK_AND_WHITE -> {
                            // Pure static minimalism - no decorative lines
                        }
                    }
                }
            }

            // Foreground content with ensured high-contrast text color
            CompositionLocalProvider(LocalContentColor provides Color.White) {
                content()
            }
        }
    }
}

/**
 * Reusable Theme-Aware Primary Button:
 * - Purple: Vibrant purple/violet gradient, soft purple glow, subtle press spring scale.
 * - Gold: Luxurious gold/bronze gradient, soft golden glow, subtle press spring scale.
 * - Black & White: Flat crisp monochrome button, zero glow, zero motion animations.
 */
@Composable
fun ThemedPrimaryButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = RoundedCornerShape(12.dp),
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
    content: @Composable RowScope.() -> Unit
) {
    val themeColors = LocalAppThemeColors.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed && themeColors.isMotionEnabled && enabled) 0.96f else 1f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 400f),
        label = "btn_scale"
    )

    val backgroundBrush = remember(themeColors.visualTheme, themeColors.isDark, enabled) {
        if (!enabled) {
            Brush.linearGradient(listOf(Color.Gray.copy(alpha = 0.4f), Color.Gray.copy(alpha = 0.4f)))
        } else {
            when (themeColors.visualTheme) {
                AppVisualTheme.PURPLE -> {
                    Brush.horizontalGradient(
                        listOf(
                            Color(0xFF7E22CE),
                            Color(0xFF9333EA)
                        )
                    )
                }
                AppVisualTheme.GOLD -> {
                    Brush.horizontalGradient(
                        listOf(
                            Color(0xFF9E7D23),
                            Color(0xFFC59B27)
                        )
                    )
                }
                AppVisualTheme.BLACK_AND_WHITE -> {
                    val c = if (themeColors.isDark) Color(0xFFFFFFFF) else Color(0xFF181818)
                    Brush.linearGradient(listOf(c, c))
                }
            }
        }
    }

    val glowColor = remember(themeColors.visualTheme, enabled) {
        if (!enabled) Color.Transparent
        else when (themeColors.visualTheme) {
            AppVisualTheme.PURPLE -> Color(0xFF9333EA).copy(alpha = 0.35f)
            AppVisualTheme.GOLD -> Color(0xFFD4AF37).copy(alpha = 0.35f)
            AppVisualTheme.BLACK_AND_WHITE -> Color.Transparent
        }
    }

    val contentColor = remember(themeColors.visualTheme, themeColors.isDark, enabled) {
        if (!enabled) Color.White.copy(alpha = 0.6f)
        else when (themeColors.visualTheme) {
            AppVisualTheme.BLACK_AND_WHITE -> if (themeColors.isDark) Color(0xFF000000) else Color.White
            else -> Color.White
        }
    }

    val shadowElevation = if (enabled && themeColors.visualTheme != AppVisualTheme.BLACK_AND_WHITE) 4.dp else 0.dp

    Box(
        modifier = modifier
            .scale(scale)
            .shadow(
                elevation = shadowElevation,
                shape = shape,
                spotColor = glowColor
            )
            .clip(shape)
            .background(backgroundBrush)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick
            )
            .padding(contentPadding),
        contentAlignment = Alignment.Center
    ) {
        CompositionLocalProvider(LocalContentColor provides contentColor) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                content()
            }
        }
    }
}
