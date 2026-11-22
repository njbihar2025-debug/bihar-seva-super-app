package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.data.ThemeMode

private val LightColors = lightColorScheme(
    primary = PeacockTeal,
    onPrimary = Color.White,
    primaryContainer = PeacockMist,
    onPrimaryContainer = PeacockTealDark,
    secondary = MarigoldSaffron,
    onSecondary = Color.White,
    secondaryContainer = MarigoldSoft,
    onSecondaryContainer = MarigoldDark,
    tertiary = HeritageGold,
    onTertiary = Color.White,
    tertiaryContainer = HeritageGoldSoft,
    onTertiaryContainer = Color(0xFF4B3000),
    background = IvoryBackground,
    onBackground = DeepInk,
    surface = IvorySurface,
    onSurface = DeepInk,
    surfaceVariant = IvorySurfaceVariant,
    onSurfaceVariant = MutedInk,
    outline = WarmOutline,
    outlineVariant = Color(0xFFE6DDD2),
    error = DebitCoral,
    onError = Color.White,
    errorContainer = DebitCoralContainer,
    onErrorContainer = Color(0xFF7F1D1D)
)

private val DarkColors = darkColorScheme(
    primary = DarkTealPrimary,
    onPrimary = PeacockTealDark,
    primaryContainer = PeacockTeal,
    onPrimaryContainer = PeacockMist,
    secondary = DarkMarigold,
    onSecondary = Color(0xFF471A00),
    secondaryContainer = MarigoldDark,
    onSecondaryContainer = MarigoldSoft,
    tertiary = DarkGold,
    onTertiary = Color(0xFF3E2800),
    tertiaryContainer = Color(0xFF5D3E00),
    onTertiaryContainer = HeritageGoldSoft,
    background = ObsidianBg,
    onBackground = DarkTextPrimary,
    surface = ObsidianSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = ObsidianCard,
    onSurfaceVariant = DarkTextSecondary,
    outline = Color(0xFF355052),
    outlineVariant = Color(0xFF23383A),
    error = Color(0xFFF87171),
    onError = Color(0xFF450A0A),
    errorContainer = Color(0xFF7F1D1D),
    onErrorContainer = Color(0xFFFEE2E2)
)

val AppShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun BiharSevaTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = Typography,
        shapes = AppShapes,
        content = content
    )
}
