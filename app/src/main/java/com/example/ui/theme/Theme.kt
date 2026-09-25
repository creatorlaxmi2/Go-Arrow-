package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = ElectricBlueLight,
    onPrimary = Color.White,
    primaryContainer = ElectricBlueDark,
    onPrimaryContainer = Color.White,
    secondary = StarGold,
    onSecondary = Color.Black,
    background = BoardBackgroundDark,
    onBackground = TextDarkPrimary,
    surface = BoardSurfaceDark,
    onSurface = TextDarkPrimary,
    surfaceVariant = Color(0xFF13224B),
    onSurfaceVariant = TextDarkSecondary,
    outline = BoardBorderDark,
    error = BlockedRed,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = ElectricBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0F2FE),
    onPrimaryContainer = DeepNavy,
    secondary = StarGold,
    onSecondary = Color.White,
    background = Color(0xFFFAFAFA),
    onBackground = DeepNavy,
    surface = Color.White,
    onSurface = DeepNavy,
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFF475569),
    outline = BoardBorderLight,
    error = BlockedRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
