package com.tnyx.core.theme.tokens.semantic

import com.tnyx.core.theme.tokens.foundation.TnyxPalette

/**
 * Tnyx OLED Mode Theme Palette (Pure Black)
 */
val TnyxOledColors = TnyxDarkColors.copy(
    background = TnyxPalette.PureBlack,
    surface = TnyxPalette.Neutral950,
    surfaceRaised = TnyxPalette.Neutral900,
    isOled = true
)
