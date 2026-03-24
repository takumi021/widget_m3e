package com.ompatel.expressivewidgetlab.widget

import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceTheme
import androidx.glance.color.ColorProviders
import androidx.glance.color.colorProviders
import androidx.glance.text.FontWeight
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider

object ExpressiveWidgetTheme {
    enum class ClockSizeClass {
        Compact,
        Regular,
        Large,
        Wide,
    }

    val OuterCornerRadius = 30.dp
    val InnerCornerRadius = 18.dp
    val ContentPadding = 18.dp
    val ComfortableSpacing = 12.dp
    val GridSpacing = 8.dp

    fun fallbackColors(): ColorProviders = colorProviders(
        primary = androidx.glance.color.ColorProvider(day = Color(0xFF3A57C4), night = Color(0xFFBAC3FF)),
        onPrimary = androidx.glance.color.ColorProvider(day = Color.White, night = Color(0xFF11205A)),
        primaryContainer = androidx.glance.color.ColorProvider(day = Color(0xFFDEE1FF), night = Color(0xFF25316C)),
        onPrimaryContainer = androidx.glance.color.ColorProvider(day = Color(0xFF11205A), night = Color(0xFFDEE1FF)),
        secondary = androidx.glance.color.ColorProvider(day = Color(0xFF0B6B64), night = Color(0xFFA5F1E7)),
        onSecondary = androidx.glance.color.ColorProvider(day = Color.White, night = Color(0xFF00201D)),
        secondaryContainer = androidx.glance.color.ColorProvider(day = Color(0xFFA5F1E7), night = Color(0xFF005049)),
        onSecondaryContainer = androidx.glance.color.ColorProvider(day = Color(0xFF00201D), night = Color(0xFFA5F1E7)),
        tertiary = androidx.glance.color.ColorProvider(day = Color(0xFF8A4B1F), night = Color(0xFFFFDCC5)),
        onTertiary = androidx.glance.color.ColorProvider(day = Color.White, night = Color(0xFF341100)),
        tertiaryContainer = androidx.glance.color.ColorProvider(day = Color(0xFFFFDCC5), night = Color(0xFF6D350B)),
        onTertiaryContainer = androidx.glance.color.ColorProvider(day = Color(0xFF341100), night = Color(0xFFFFDCC5)),
        error = androidx.glance.color.ColorProvider(day = Color(0xFFBA1A1A), night = Color(0xFFFFB4AB)),
        errorContainer = androidx.glance.color.ColorProvider(day = Color(0xFFFFDAD6), night = Color(0xFF93000A)),
        onError = androidx.glance.color.ColorProvider(day = Color.White, night = Color(0xFF690005)),
        onErrorContainer = androidx.glance.color.ColorProvider(day = Color(0xFF410002), night = Color(0xFFFFDAD6)),
        background = androidx.glance.color.ColorProvider(day = Color(0xFFFBF8FF), night = Color(0xFF11131B)),
        onBackground = androidx.glance.color.ColorProvider(day = Color(0xFF11131B), night = Color(0xFFE1E2EC)),
        surface = androidx.glance.color.ColorProvider(day = Color(0xFFF0F1FA), night = Color(0xFF1B1F2A)),
        onSurface = androidx.glance.color.ColorProvider(day = Color(0xFF11131B), night = Color(0xFFE1E2EC)),
        surfaceVariant = androidx.glance.color.ColorProvider(day = Color(0xFFE1E2EC), night = Color(0xFF2B303B)),
        onSurfaceVariant = androidx.glance.color.ColorProvider(day = Color(0xFF434852), night = Color(0xFFC3C7D2)),
        outline = androidx.glance.color.ColorProvider(day = Color(0xFF747986), night = Color(0xFF8E93A0)),
        inverseOnSurface = androidx.glance.color.ColorProvider(day = Color(0xFFEEEFFF), night = Color(0xFF11131B)),
        inverseSurface = androidx.glance.color.ColorProvider(day = Color(0xFF2B303B), night = Color(0xFFE1E2EC)),
        inversePrimary = androidx.glance.color.ColorProvider(day = Color(0xFFBAC3FF), night = Color(0xFF3A57C4)),
        widgetBackground = androidx.glance.color.ColorProvider(day = Color(0xFFF0F1FA), night = Color(0xFF1B1F2A)),
    )

    fun labelStyle(color: ColorProvider) = TextStyle(
        color = color,
        fontSize = 11.sp,
        fontWeight = FontWeight.Medium,
    )

    fun timeStyle(sizeClass: ClockSizeClass, color: ColorProvider) = TextStyle(
        color = color,
        fontSize = when (sizeClass) {
            ClockSizeClass.Compact -> 26.sp
            ClockSizeClass.Regular -> 30.sp
            ClockSizeClass.Large -> 38.sp
            ClockSizeClass.Wide -> 46.sp
        },
        fontWeight = FontWeight.Bold,
    )

    fun meridiemStyle(compact: Boolean, roomy: Boolean, color: ColorProvider) = TextStyle(
        color = color,
        fontSize = when {
            compact -> 12.sp
            roomy -> 18.sp
            else -> 14.sp
        },
        fontWeight = FontWeight.Medium,
    )

    fun dateStyle(compact: Boolean, roomy: Boolean, color: ColorProvider) = TextStyle(
        color = color,
        fontSize = when {
            compact -> 12.sp
            roomy -> 16.sp
            else -> 14.sp
        },
        fontWeight = FontWeight.Medium,
    )

    fun chipStyle(color: ColorProvider) = TextStyle(
        color = color,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
    )

    fun compactStatusStyle(color: ColorProvider) = TextStyle(
        color = color,
        fontSize = 10.sp,
        fontWeight = FontWeight.Medium,
    )

    fun boardCellStyle(large: Boolean, color: ColorProvider) = TextStyle(
        color = color,
        fontSize = if (large) 28.sp else 22.sp,
        fontWeight = FontWeight.Bold,
    )

    fun healthMetricValueStyle(sizeClass: HealthMetricSizeClass, color: ColorProvider) = TextStyle(
        color = color,
        fontSize = when (sizeClass) {
            HealthMetricSizeClass.Compact -> 16.sp
            HealthMetricSizeClass.Regular -> 20.sp
            HealthMetricSizeClass.Roomy -> 24.sp
        },
        fontWeight = FontWeight.Bold,
    )

    enum class HealthMetricSizeClass {
        Compact,
        Regular,
        Roomy,
    }

    @Composable
    fun GlanceSurface(content: @Composable () -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            GlanceTheme(content = content)
        } else {
            GlanceTheme(colors = fallbackColors(), content = content)
        }
    }
}
