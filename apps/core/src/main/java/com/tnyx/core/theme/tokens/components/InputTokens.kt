package com.tnyx.core.theme.tokens.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import com.tnyx.core.theme.tokens.foundation.TnyxDimens
import com.tnyx.core.theme.tokens.foundation.TnyxPalette

/**
 * Tnyx Input Component Specs
 */
data class InputTokens(
    val height: Dp,
    val borderWidthFocused: Dp,
    val borderWidthUnfocused: Dp,
    val containerColor: Color,
    val focusedIndicatorColor: Color,
    val unfocusedIndicatorColor: Color,
    val errorIndicatorColor: Color,
    val textColor: Color,
    val placeholderColor: Color,
    val disabledContainerColor: Color,
    val disabledBorderColor: Color,
    val disabledTextColor: Color,
    val disabledPlaceholderColor: Color,
    val textStyle: TextStyle? = null
)

val DefaultInputTokens = InputTokens(
    height = TnyxDimens.InputHeight,
    borderWidthFocused = TnyxDimens.BorderThin,
    borderWidthUnfocused = TnyxDimens.BorderSubtle,
    containerColor = TnyxPalette.White,
    focusedIndicatorColor = TnyxPalette.Black,
    unfocusedIndicatorColor = TnyxPalette.Neutral200,
    errorIndicatorColor = TnyxPalette.Rose,
    textColor = TnyxPalette.Black,
    placeholderColor = TnyxPalette.Neutral400,
    disabledContainerColor = TnyxPalette.Neutral100,
    disabledBorderColor = TnyxPalette.Neutral200,
    disabledTextColor = TnyxPalette.Neutral400,
    disabledPlaceholderColor = TnyxPalette.Neutral400
)
