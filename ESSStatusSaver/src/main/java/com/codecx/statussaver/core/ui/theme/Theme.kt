package com.codecx.statussaver.core.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = primary,
    onPrimary = Color.White,
    secondary = primaryStart,
    onSecondary = Color.White,
    background = Color.Black,
    onBackground = Color.White,
    surface = Color.Black,
    onSurface = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = primary,
    onPrimary = Color.White,
    secondary = primaryStart,
    onSecondary = Color.White,
    background = background,
    onBackground = text_primary,
    surface = background,
    onSurface = text_primary
)

@Composable
fun StatusSaverTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = AppTypography,
        content = content
    )
}