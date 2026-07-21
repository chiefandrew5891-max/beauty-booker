package com.beautyplanner.client.android.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.graphics.Color

enum class AppThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

private val PrimaryPink = Color(0xFFE83E8C)
private val PrimaryPinkLight = Color(0xFFFFC1DC)
private val PrimaryPinkDark = Color(0xFFB02665)
private val BackgroundLight = Color(0xFFFFF8FC)
private val SurfaceLight = Color(0xFFFFFFFF)
private val SurfaceVariantLight = Color(0xFFFCEAF4)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryPink,
    onPrimary = Color.White,
    primaryContainer = PrimaryPinkLight,
    onPrimaryContainer = Color(0xFF3A0020),
    secondary = Color(0xFFF06292),
    onSecondary = Color.White,
    background = BackgroundLight,
    surface = SurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onSurface = Color(0xFF1F1A1D),
    onSurfaceVariant = Color(0xFF6B5A63)
)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryPinkLight,
    onPrimary = Color(0xFF57152F),
    primaryContainer = PrimaryPinkDark,
    background = Color(0xFF161217),
    surface = Color(0xFF211A1E),
    surfaceVariant = Color(0xFF352A31)
)

@Composable
fun BeautyPlannerTheme(
    themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        AppThemeMode.SYSTEM -> isSystemInDarkTheme()
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK -> true
    }

    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        content = content
    )
}
