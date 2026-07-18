package com.tnyx.core.theme.tokens.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import com.tnyx.core.theme.tokens.foundation.TnyxDimens
import com.tnyx.core.theme.tokens.foundation.TnyxPalette

/**
 * Tnyx Card Component Specs
 */
data class CardTokens(
    val containerColor: Color,
    val contentPadding: Dp,
    val cornerRadius: Dp,
    val elevation: Dp,
    val borderColor: Color,
    val borderWidth: Dp,
    val normalContainerColor: Color,
    val glassContainerColor: Color,
    val glassBorderColor: Color,
    val outlinedBorderColor: Color
)

val DefaultCardTokens = CardTokens(
    containerColor = TnyxPalette.White,
    contentPadding = TnyxDimens.SpaceM,
    cornerRadius = TnyxDimens.RadiusL,
    elevation = TnyxDimens.SpaceXXS, // Level 1 equivalent
    borderColor = TnyxPalette.Neutral200,
    borderWidth = TnyxDimens.BorderThin,
    normalContainerColor = TnyxPalette.Neutral100,
    glassContainerColor = TnyxPalette.White.copy(alpha = 0.72f),
    glassBorderColor = TnyxPalette.Neutral200.copy(alpha = 0.16f),
    outlinedBorderColor = TnyxPalette.Neutral200.copy(alpha = 0.16f)
)
