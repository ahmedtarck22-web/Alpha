package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val AlphaGoColorScheme = darkColorScheme(
    primary = GoldAccent,
    onPrimary = CosmicBg,
    primaryContainer = RoyalGoldDark,
    onPrimaryContainer = TextPrimary,
    secondary = EgyptianCyan,
    onSecondary = CosmicBg,
    secondaryContainer = CosmicLight,
    onSecondaryContainer = EgyptianTurquoise,
    tertiary = GoldGlow,
    onTertiary = CosmicBg,
    background = CosmicBg,
    onBackground = TextPrimary,
    surface = CosmicCard,
    onSurface = TextPrimary,
    surfaceVariant = CosmicLight,
    onSurfaceVariant = TextSecondary,
    outline = GoldBorder,
    outlineVariant = GoldBorderSubtle
)

@Composable
fun RamsesTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = AlphaGoColorScheme,
        typography = Typography,
        content = content
    )
}
