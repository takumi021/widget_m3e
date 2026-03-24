package com.ompatel.expressivewidgetlab.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val LightColors = lightColorScheme(
    primary = Iris40,
    onPrimary = Slate99,
    primaryContainer = Iris90,
    onPrimaryContainer = Cobalt20,
    secondary = Mint40,
    onSecondary = Slate99,
    secondaryContainer = Mint90,
    onSecondaryContainer = Slate10,
    tertiary = Peach40,
    onTertiary = Slate99,
    tertiaryContainer = Peach90,
    onTertiaryContainer = Slate10,
    background = Slate99,
    onBackground = Slate10,
    surface = Slate99,
    onSurface = Slate10,
    surfaceContainer = Slate95,
    surfaceContainerHigh = Slate90,
    onSurfaceVariant = Slate20,
)

private val DarkColors = darkColorScheme(
    primary = Iris80,
    onPrimary = Cobalt20,
    primaryContainer = Cobalt20,
    onPrimaryContainer = Iris90,
    secondary = Mint90,
    onSecondary = Slate10,
    secondaryContainer = Mint40,
    onSecondaryContainer = Slate99,
    tertiary = Peach90,
    onTertiary = Slate10,
    tertiaryContainer = Peach40,
    onTertiaryContainer = Slate99,
    background = Slate10,
    onBackground = Slate90,
    surface = Slate10,
    onSurface = Slate90,
    surfaceContainer = Slate20,
    surfaceContainerHigh = Slate20,
    onSurfaceVariant = Slate90,
)

@Composable
fun ExpressiveWidgetLabTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColors
        else -> LightColors
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = ExpressiveTypography,
        content = content,
    )
}
