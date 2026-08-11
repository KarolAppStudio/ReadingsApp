package com.karol.readingsapp.core.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.IndicationNodeFactory
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.node.DelegatableNode
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.invalidateDraw
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

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
    onPrimaryContainer = Color(0xFF004B75),
    secondary = Color(0xFF5C6BC0),
    onSecondary = Color.White,
    secondaryContainer = CardLavender,
    onSecondaryContainer = Color(0xFF004B75),
    tertiary = Color(0xFF2E7D32),
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
    onPrimaryContainer = Color(0xFF3E2719),
    secondary = Color(0xFF8B6B4F),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE3DAC1),
    onSecondaryContainer = Color(0xFF3E2719),
    tertiary = Color(0xFF705D49),
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

private val EInkLightColorScheme = lightColorScheme(
    primary = EInkBlack,
    onPrimary = EInkWhite,
    primaryContainer = EInkLightGray,
    onPrimaryContainer = EInkBlack,
    secondary = EInkDarkGray,
    onSecondary = EInkWhite,
    secondaryContainer = EInkLightGray,
    onSecondaryContainer = EInkBlack,
    tertiary = EInkDarkGray,
    onTertiary = EInkWhite,
    tertiaryContainer = EInkLightGray,
    onTertiaryContainer = EInkBlack,
    background = EInkWhite,
    surface = EInkWhite,
    onBackground = EInkBlack,
    onSurface = EInkBlack,
    outline = EInkBlack,
    surfaceVariant = EInkLightGray,
    onSurfaceVariant = EInkBlack,
)

private val EInkDarkColorScheme = darkColorScheme(
    primary = EInkWhite,
    onPrimary = EInkBlack,
    primaryContainer = EInkDarkGray,
    onPrimaryContainer = EInkWhite,
    secondary = EInkLightGray,
    onSecondary = EInkBlack,
    secondaryContainer = EInkDarkGray,
    onSecondaryContainer = EInkWhite,
    tertiary = EInkLightGray,
    onTertiary = Color.Black,
    tertiaryContainer = EInkDarkGray,
    onTertiaryContainer = EInkWhite,
    background = EInkBlack,
    surface = EInkBlack,
    onBackground = EInkWhite,
    onSurface = EInkWhite,
    outline = EInkWhite,
    surfaceVariant = EInkDarkGray,
    onSurfaceVariant = EInkWhite,
)

val LocalEInkBorder = staticCompositionLocalOf { BorderStroke(1.dp, Color.Black) }

/**
 * E-Ink optimized indication that provides instantaneous high-contrast feedback.
 * Uses IndicationNodeFactory and Modifier.Node for modern Compose performance.
 */
private object EInkIndication : IndicationNodeFactory {
    override fun create(interactionSource: InteractionSource): DelegatableNode = EInkIndicationNode(interactionSource)

    override fun equals(other: Any?): Boolean = other === this
    override fun hashCode(): Int = System.identityHashCode(this)
}

private class EInkIndicationNode(private val interactionSource: InteractionSource) :
    Modifier.Node(),
    DrawModifierNode {

    private var isPressed = false

    override fun onAttach() {
        coroutineScope.launch {
            interactionSource.interactions.collectLatest { interaction ->
                when (interaction) {
                    is PressInteraction.Press -> {
                        isPressed = true
                        invalidateDraw()
                    }

                    is PressInteraction.Release, is PressInteraction.Cancel -> {
                        isPressed = false
                        invalidateDraw()
                    }
                }
            }
        }
    }

    override fun ContentDrawScope.draw() {
        drawContent()
        if (isPressed) {
            // Instant feedback without animations.
            // Inverting or dimming slightly for E-ink.
            drawRect(color = Color.Black.copy(alpha = 0.1f))
        }
    }
}

@Composable
fun ReadingsAppTheme(
    appTheme: AppTheme = AppTheme.SKY_BLUE,
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val isEInk = appTheme == AppTheme.E_INK

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

        AppTheme.E_INK -> if (darkTheme) EInkDarkColorScheme else EInkLightColorScheme
    }

    val typography = if (isEInk) EInkTypography else Typography
    val shapes = if (isEInk) EInkShapes else Shapes
    val eInkBorder = BorderStroke(EInkBorderWidth, colorScheme.outline)

    CompositionLocalProvider(
        LocalEInkBorder provides eInkBorder,
        LocalIndication provides if (isEInk) EInkIndication else LocalIndication.current,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = typography,
            shapes = shapes,
            content = content,
        )
    }
}
