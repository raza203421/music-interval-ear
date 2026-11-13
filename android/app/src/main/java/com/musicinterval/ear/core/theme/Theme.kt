package com.musicinterval.ear.core.theme

import androidx.compose.material3.*
import androidx.compose.runtime.*

val LocalReduceMotion = staticCompositionLocalOf { false }
private val LightScheme = lightColorScheme(primary = PrimaryIndigo, onPrimary = SurfaceWhite, secondary = AccentRed, onSecondary = SurfaceWhite, background = Background, onBackground = TextPrimary, surface = SurfaceWhite, onSurface = TextPrimary, surfaceVariant = BackgroundMid, onSurfaceVariant = TextSecondary, outline = Border, error = Error)
private val DarkScheme = darkColorScheme(primary = Lavender, onPrimary = TextPrimary, secondary = GoldLight, onSecondary = TextPrimary, background = DeepPlum, onBackground = SurfaceWhite, surface = TextPrimary, onSurface = Background, surfaceVariant = DeepPlum, onSurfaceVariant = Lavender, outline = Lavender, error = GoldLight)

@Composable
fun AppTheme(dark: Boolean = false, content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = if (dark) DarkScheme else LightScheme, typography = StudioTypography, content = content)
}
