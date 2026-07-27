package com.karol.readingsapp.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF6650a4),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEADDFF),
    onPrimaryContainer = Color(0xFF21005D),
    secondary = Color(0xFF625b71),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE8DEF8),
    onSecondaryContainer = Color(0xFF1D192B),
    tertiary = Color(0xFF7D5260),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFD8E4),
    onTertiaryContainer = Color(0xFF31111D),
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    surfaceVariant = Color(0xFFE7E0EC),
    onSurfaceVariant = Color(0xFF49454F),
)

private val BlueColorScheme = lightColorScheme(
    primary = TextBlue,
    onPrimary = Color.White,
    primaryContainer = NTGold,
    onPrimaryContainer = Color(0xFF004B75), // Darker blue for better contrast
    secondary = Color(0xFF5C6BC0), // Indigo-like for secondary highlights
    onSecondary = Color.White,
    secondaryContainer = CardLavender,
    onSecondaryContainer = Color(0xFF004B75),
    tertiary = Color(0xFF2E7D32), // Darker green for visibility (e.g. sync lock)
    onTertiary = Color.White,
    tertiaryContainer = OTGreen,
    onTertiaryContainer = Color(0xFF004B75),
    background = BackgroundBlue,
    surface = Color.White,
    onBackground = Color.Black,
    onSurface = Color.Black,
    surfaceVariant = Color(0xFFF0F0F0),
    onSurfaceVariant = Color.DarkGray,
)

private val SepiaColorScheme = lightColorScheme(
    primary = Color(0xFF5B4636),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD4C4A8),
    onPrimaryContainer = Color(0xFF3E2719), // Darker brown for contrast
    secondary = Color(0xFF8B6B4F), // Medium brown for secondary highlights
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE3DAC1),
    onSecondaryContainer = Color(0xFF3E2719),
    tertiary = Color(0xFF705D49), // Dark brown for visibility
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFE6D2B5),
    onTertiaryContainer = Color(0xFF3E2719),
    background = Color(0xFFF4ECD8),
    surface = Color(0xFFFEF9ED),
    onBackground = Color(0xFF5B4636),
    onSurface = Color(0xFF5B4636),
    surfaceVariant = Color(0xFFEBE0C9),
    onSurfaceVariant = Color(0xFF7A6652),
)

@Composable
fun ReadingsAppTheme(
    appTheme: AppTheme = AppTheme.SKY_BLUE,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when (appTheme) {
        AppTheme.PURPLE -> {
            if (dynamicColor) {
                dynamicLightColorScheme(LocalContext.current)
            } else {
                LightColorScheme
            }
        }

        AppTheme.SKY_BLUE -> BlueColorScheme

        AppTheme.SEPIA -> SepiaColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}
