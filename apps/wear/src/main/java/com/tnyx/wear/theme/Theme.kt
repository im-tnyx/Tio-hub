package com.tnyx.wear.theme

import androidx.compose.runtime.Composable
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Colors

private val WearColorPalette = Colors(
    primary = ColorSteps,
    primaryVariant = ColorSteps,
    secondary = ColorWater,
    background = BackgroundBlack,
    surface = CardBackground,
    onPrimary = BackgroundBlack,
    onSecondary = BackgroundBlack,
    onBackground = TextWhite,
    onSurface = TextWhite
)

@Composable
fun SamsungHealthWearTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colors = WearColorPalette,
        typography = WearTypography,
        content = content
    )
}
