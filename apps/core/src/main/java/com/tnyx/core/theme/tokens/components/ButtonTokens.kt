package com.tnyx.core.theme.tokens.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import com.tnyx.core.theme.tokens.foundation.TnyxDimens
import com.tnyx.core.theme.tokens.foundation.TnyxPalette

/**
 * Tnyx Button Component Specs
 */
data class ButtonTokens(
    val height: Dp,
    val heightLarge: Dp,
    val horizontalPadding: Dp,
    val iconSpacing: Dp,
    val containerColor: Color,
    val contentColor: Color,
    val disabledContainerColor: Color,
    val disabledContentColor: Color,
    val textStyle: TextStyle? = null // Handled by Theme typography if null
)

val DefaultButtonTokens = ButtonTokens(
    height = TnyxDimens.ButtonHeight,
    heightLarge = TnyxDimens.ButtonHeightLarge,
    horizontalPadding = TnyxDimens.SpaceL,
    iconSpacing = TnyxDimens.SpaceS,
    containerColor = TnyxPalette.Black,
    contentColor = TnyxPalette.White,
    disabledContainerColor = TnyxPalette.Neutral100,
    disabledContentColor = TnyxPalette.Neutral400
)
