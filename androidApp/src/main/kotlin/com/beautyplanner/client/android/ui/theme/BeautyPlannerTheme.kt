package com.beautyplanner.client.android.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val PrimaryPink = Color(0xFFE91E8C)
private val PrimaryPinkLight = Color(0xFFFF6EB5)
private val PrimaryPinkDark = Color(0xFFB0006A)
private val BackgroundLight = Color(0xFFFFFBFE)
private val SurfaceLight = Color(0xFFF5F5F5)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryPink,
    onPrimary = Color.White,
    primaryContainer = PrimaryPinkLight,
    secondary = PrimaryPinkDark,
    background = BackgroundLight,
    surface = SurfaceLight
)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryPinkLight,
    onPrimary = Color.Black,
    primaryContainer = PrimaryPinkDark
)

@Composable
fun BeautyPlannerTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        content = content
    )
}
