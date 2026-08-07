package com.tnyx.core.theme.tokens.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import com.tnyx.core.theme.tokens.foundation.TnyxDimens
import com.tnyx.core.theme.tokens.foundation.TnyxPalette
import com.tnyx.core.theme.tokens.typography.TnyxTypography

/**
 * Size variants for TnyxScreenHeader.
 *
 * - [Compact] → 36dp  — used on bottom-nav tab screens (Workout, Library, Progress, Meal Diary)
 * - [Standard] → 56dp — used on nested/detail screens (Search Exercises, etc.)
 */
enum class TnyxHeaderSize {
    Compact,
    Standard,
}

data class HeaderTokens(
    val height: Dp,
    val compactHeight: Dp,
    val standardHeight: Dp,
    val horizontalPadding: Dp,
    val leadingSpacing: Dp,
    val actionPlaceholderSize: Dp,
    val containerColor: Color,
    val contentColor: Color,
    val titleStyle: TextStyle
)

val DefaultHeaderTokens = HeaderTokens(
    height = TnyxDimens.ScreenHeaderHeight,
    compactHeight = TnyxDimens.ScreenHeaderHeightCompact,
    standardHeight = TnyxDimens.ScreenHeaderHeightStandard,
    horizontalPadding = TnyxDimens.SpaceS,
    leadingSpacing = TnyxDimens.SpaceS,
    actionPlaceholderSize = TnyxDimens.ScreenHeaderActionSize,
    containerColor = TnyxPalette.White,
    contentColor = TnyxPalette.Black,
    titleStyle = TnyxTypography.titleLarge.copy(fontWeight = FontWeight.Bold)
)
