package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
  primary = CyanNeon,
  onPrimary = NavyDarkest,
  primaryContainer = NavySurfaceVariant,
  onPrimaryContainer = CyanGlow,
  secondary = GoldAccent,
  onSecondary = NavyDarkest,
  secondaryContainer = NavySurfaceVariant,
  onSecondaryContainer = GoldAccent,
  tertiary = BlueElectric,
  onTertiary = TextPrimary,
  background = NavyDarkest,
  onBackground = TextPrimary,
  surface = NavyDark,
  onSurface = TextPrimary,
  surfaceVariant = NavySurface,
  onSurfaceVariant = TextSecondary,
  outline = NavyBorder,
  error = LossRed,
  onError = TextPrimary
)

private val LightColorScheme = lightColorScheme(
  primary = BlueElectric,
  onPrimary = TextPrimary,
  primaryContainer = Color(0xFFE0EFFF),
  onPrimaryContainer = NavyDarkest,
  secondary = GoldAccent,
  onSecondary = NavyDarkest,
  background = Color(0xFFF8FAFC),
  onBackground = Color(0xFF0F172A),
  surface = Color(0xFFFFFFFF),
  onSurface = Color(0xFF0F172A),
  surfaceVariant = Color(0xFFF1F5F9),
  onSurfaceVariant = Color(0xFF475569),
  outline = Color(0xFFCBD5E1),
  error = LossRed,
  onError = TextPrimary
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Default to sleek financial terminal dark mode
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

  MaterialTheme(
    colorScheme = colorScheme,
    typography = AppTypography,
    content = content
  )
}
