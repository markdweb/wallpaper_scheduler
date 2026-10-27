package com.cheese.wallpaperscheduler.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = GoldAccent,
    onPrimary = GoldAccentOnLight,
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnBackground,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
)

private val DarkColors = darkColorScheme(
    primary = GoldAccentDark,
    onPrimary = GoldAccentOnDark,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnBackground,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
)

/**
 * @param darkTheme null = follow system (ThemeMode.SYSTEM); true/false = user's explicit override.
 */
@Composable
fun WallpaperSchedulerTheme(
    darkTheme: Boolean? = null,
    content: @Composable () -> Unit,
) {
    val useDark = darkTheme ?: isSystemInDarkTheme()
    MaterialTheme(
        colorScheme = if (useDark) DarkColors else LightColors,
        typography = Typography,
        content = content,
    )
}
