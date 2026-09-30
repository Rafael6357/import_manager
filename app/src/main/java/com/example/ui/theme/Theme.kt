package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = AccentPrimary,
    onPrimary = Color(0xFF003824),
    primaryContainer = AccentPrimaryContainer,
    onPrimaryContainer = Color(0xFF00422B),
    secondary = AccentPrimary,
    onSecondary = Color(0xFF003824),
    tertiary = AccentWarning,
    onTertiary = DarkBg,
    background = DarkBg,
    onBackground = TextMain,
    surface = DarkSurface,
    onSurface = TextMain,
    surfaceVariant = DarkSurfaceHover,
    onSurfaceVariant = TextSecondary,
    outline = BorderColor,
    error = AccentDanger,
    onError = TextMain
  )

@Composable
fun MyApplicationTheme(
  content: @Composable () -> Unit,
) {
  MaterialTheme(
    colorScheme = DarkColorScheme,
    typography = Typography,
    content = content
  )
}
