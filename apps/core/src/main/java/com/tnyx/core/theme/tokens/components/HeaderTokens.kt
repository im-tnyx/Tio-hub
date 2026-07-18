package com.tnyx.core.theme.tokens.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import com.tnyx.core.theme.tokens.foundation.TnyxDimens
import com.tnyx.core.theme.tokens.foundation.TnyxPalette
import com.tnyx.core.theme.tokens.typography.TnyxTypography

data class HeaderTokens(
    val height: Dp,
    val horizontalPadding: Dp,
    val leadingSpacing: Dp,
    val actionPlaceholderSize: Dp,
    val containerColor: Color,
    val contentColor: Color,
    val titleStyle: TextStyle
)

val DefaultHeaderTokens = HeaderTokens(
    height = TnyxDimens.ScreenHeaderHeight,
    horizontalPadding = TnyxDimens.SpaceS,
    leadingSpacing = TnyxDimens.SpaceS,
    actionPlaceholderSize = TnyxDimens.ScreenHeaderActionSize,
    containerColor = TnyxPalette.White,
    contentColor = TnyxPalette.Black,
    titleStyle = TnyxTypography.titleMedium.copy(fontWeight = FontWeight.Bold)
)
