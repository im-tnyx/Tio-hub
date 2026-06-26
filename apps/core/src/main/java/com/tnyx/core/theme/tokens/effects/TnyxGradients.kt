package com.tnyx.core.theme.tokens.effects

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.tnyx.core.theme.tokens.foundation.TnyxPalette

/**
 * Tnyx Design System Gradients
 */
object TnyxGradients {
    val Primary = Brush.verticalGradient(
        colors = listOf(TnyxPalette.Black, TnyxPalette.Neutral900)
    )
    
    val Surface = Brush.verticalGradient(
        colors = listOf(TnyxPalette.Neutral900, TnyxPalette.PureBlack)
    )

    val DarkOverlay = Brush.verticalGradient(
        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))
    )

    val Glass = Brush.verticalGradient(
        colors = listOf(
            TnyxPalette.White.copy(alpha = 0.12f),
            TnyxPalette.White.copy(alpha = 0.04f)
        )
    )
}
