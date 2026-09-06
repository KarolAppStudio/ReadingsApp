@file:Suppress("unused")

package com.karol.readingsapp.core.theme

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.min

val LocalWindowSizeClass = compositionLocalOf<WindowSizeClass> {
    error("No WindowSizeClass provided")
}

val LocalResolutionScale = compositionLocalOf { 1.0f }

@Composable
fun ProvideWindowSizeClass(windowSizeClass: WindowSizeClass, content: @Composable () -> Unit) {
    val windowInfo = LocalWindowInfo.current
    val density = LocalDensity.current
    val containerSize = windowInfo.containerSize

    val screenWidthDp = with(density) { containerSize.width.toDp().value }
    val screenHeightDp = with(density) { containerSize.height.toDp().value }

    // Baseline resolution: 720 x 1600 px @ ~320 dpi (360dp x 800dp)
    // Target resolution range: up to 3120 x 1440 px @ ~560-640 dpi (~412dp-480dp x 890dp-1040dp)
    val widthScale = if (screenWidthDp > 0f) screenWidthDp / 360f else 1.0f
    val heightScale = if (screenHeightDp > 0f) screenHeightDp / 800f else 1.0f

    // Symmetrical scale factor bounded smoothly between 0.85f and 1.5f across screen resolutions
    val symmetricalScale = min(widthScale, heightScale).coerceIn(0.85f, 1.5f)

    CompositionLocalProvider(
        LocalWindowSizeClass provides windowSizeClass,
        LocalResolutionScale provides symmetricalScale,
    ) {
        content()
    }
}

object AdaptiveDimens {
    val windowSize: WindowSizeClass
        @Composable
        @ReadOnlyComposable
        get() = LocalWindowSizeClass.current

    val resolutionScale: Float
        @Composable
        @ReadOnlyComposable
        get() = LocalResolutionScale.current

    val fontScale: Float
        @Composable
        @ReadOnlyComposable
        get() {
            val baseScale = when (windowSize.widthSizeClass) {
                WindowWidthSizeClass.Compact -> 1.0f
                WindowWidthSizeClass.Medium -> 1.15f
                else -> 1.3f
            }
            return baseScale * resolutionScale
        }

    val paddingSmall: Dp
        @Composable
        @ReadOnlyComposable
        get() {
            val base = when (windowSize.widthSizeClass) {
                WindowWidthSizeClass.Compact -> 8.dp
                WindowWidthSizeClass.Medium -> 12.dp
                else -> 16.dp
            }
            return base * resolutionScale
        }

    val paddingMedium: Dp
        @Composable
        @ReadOnlyComposable
        get() {
            val base = when (windowSize.widthSizeClass) {
                WindowWidthSizeClass.Compact -> 16.dp
                WindowWidthSizeClass.Medium -> 24.dp
                else -> 32.dp
            }
            return base * resolutionScale
        }

    val paddingLarge: Dp
        @Composable
        @ReadOnlyComposable
        get() {
            val base = when (windowSize.widthSizeClass) {
                WindowWidthSizeClass.Compact -> 24.dp
                WindowWidthSizeClass.Medium -> 36.dp
                else -> 48.dp
            }
            return base * resolutionScale
        }

    val iconSizeSmall: Dp
        @Composable
        @ReadOnlyComposable
        get() = (24.dp * resolutionScale)

    val iconSizeMedium: Dp
        @Composable
        @ReadOnlyComposable
        get() = (30.dp * resolutionScale)

    val iconSizeLarge: Dp
        @Composable
        @ReadOnlyComposable
        get() = (40.dp * resolutionScale)

    val titleFontSize: TextUnit
        @Composable
        @ReadOnlyComposable
        get() = (20 * fontScale).sp

    val bodyFontSize: TextUnit
        @Composable
        @ReadOnlyComposable
        get() = (16 * fontScale).sp

    val smallFontSize: TextUnit
        @Composable
        @ReadOnlyComposable
        get() = (14 * fontScale).sp

    val contentMaxWidth: Dp
        @Composable
        @ReadOnlyComposable
        get() = when (windowSize.widthSizeClass) {
            WindowWidthSizeClass.Compact -> Dp.Unspecified
            WindowWidthSizeClass.Medium -> 720.dp * resolutionScale
            else -> 1080.dp * resolutionScale
        }
}
