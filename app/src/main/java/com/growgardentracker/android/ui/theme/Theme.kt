package com.growgardentracker.android.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = GardenGreen,
    onPrimary = Color.White,
    primaryContainer = FreshMint,
    onPrimaryContainer = GardenDark,
    secondary = SoilBrown,
    tertiary = SunYellow,
    background = SoftGreen,
    onBackground = TextDark,
    surface = CardCream,
    onSurface = TextDark,
    surfaceVariant = FreshMint,
    onSurfaceVariant = TextMuted,
    error = WarningRed
)

@Composable
fun GrowGardenTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        typography = AppTypography,
        content = content
    )
}
