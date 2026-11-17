package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val KrishiLightColorScheme = lightColorScheme(
    primary = EmeraldGreen,
    onPrimary = PureWhite,
    primaryContainer = SoftMintCard,
    onPrimaryContainer = DeepForestGreen,
    secondary = VibrantGreen,
    onSecondary = PureWhite,
    secondaryContainer = Color(0xFFDCFCE7),
    onSecondaryContainer = ForestGreen,
    tertiary = AmberHighlight,
    onTertiary = PureWhite,
    tertiaryContainer = AmberContainer,
    onTertiaryContainer = Color(0xFF78350F),
    background = MintBackground,
    onBackground = DeepInk,
    surface = PureWhite,
    onSurface = DeepInk,
    surfaceVariant = SoftMintCard,
    onSurfaceVariant = MutedSage,
    outline = BorderGreenTint,
    error = DangerRed,
    onError = PureWhite,
    errorContainer = DangerContainer,
    onErrorContainer = Color(0xFF7F1D1D)
)

val KrishiShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun KrishiBazarTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = KrishiLightColorScheme,
        typography = KrishiTypography,
        shapes = KrishiShapes,
        content = content
    )
}
