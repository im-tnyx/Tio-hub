package com.tnyx.core.theme.tokens.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import com.tnyx.core.theme.tokens.foundation.TnyxDimens
import com.tnyx.core.theme.tokens.foundation.TnyxPalette

/**
 * Tnyx Design System - Bottom Sheet Tokens
 */
data class SheetTokens(
    val containerColor: Color,
    val contentColor: Color,
    val scrimColor: Color,
    val shape: Shape,
    val horizontalPadding: Dp,
    val bottomPadding: Dp,
    val titleStyle: TextStyle?,
    val dividerColor: Color
)

val DefaultSheetTokens = SheetTokens(
    containerColor = TnyxPalette.Neutral900,
    contentColor = TnyxPalette.Neutral050,
    scrimColor = TnyxPalette.Black.copy(alpha = 0.72f),
    shape = RoundedCornerShape(
        topStart = TnyxDimens.RadiusXL,
        topEnd = TnyxDimens.RadiusXL
    ),
    horizontalPadding = TnyxDimens.SpaceM,
    bottomPadding = TnyxDimens.SpaceL,
    titleStyle = null, // Will be mapped to typography in Provider
    dividerColor = TnyxPalette.Neutral050.copy(alpha = 0.08f)
)
