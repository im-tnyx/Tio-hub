package com.tnyx.core.theme.tokens.components

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.tnyx.core.theme.tokens.foundation.TnyxDimens

data class ImageCropperTokens(
    val cropAreaFraction: Float = 0.82f,
    val canvasTopPadding: Dp = TnyxDimens.SpaceXS,
    val touchRadius: Dp = TnyxDimens.SpaceXXL,
    val minimumCropSize: Dp = 60.dp,
    val frameStrokeWidth: Dp = 1.5.dp,
    val gridStrokeWidth: Dp = TnyxDimens.BorderThin,
    val handleLength: Dp = 22.dp,
    val handleStrokeWidth: Dp = 3.5.dp,
    val toolbarHorizontalPadding: Dp = TnyxDimens.SpaceM,
    val toolbarVerticalPadding: Dp = TnyxDimens.SpaceXS,
    val toolbarSectionHorizontalPadding: Dp = TnyxDimens.SpaceL,
    val toolbarSectionVerticalPadding: Dp = 6.dp,
    val toolbarIconSize: Dp = TnyxDimens.IconM,
    val actionIconSize: Dp = 26.dp,
    val rulerHorizontalPadding: Dp = TnyxDimens.SpaceS,
    val rulerHeight: Dp = 36.dp,
    val rulerTickStep: Dp = 14.dp,
    val rulerMajorTickHalfHeight: Dp = 5.dp,
    val rulerStrokeWidth: Dp = 1.5.dp,
    val rulerDotRadius: Dp = 1.5.dp,
    val aspectSelectorWidth: Dp = 44.dp,
    val aspectSelectorHeight: Dp = TnyxDimens.ButtonHeight,
    val aspectPreviewSize: Dp = 22.dp,
    val aspectSelectorRadius: Dp = 6.dp,
    val aspectContainerRadius: Dp = TnyxDimens.RadiusXS,
    val aspectPreviewRadius: Dp = TnyxDimens.SpaceXXS,
    val aspectBorderWidth: Dp = TnyxDimens.BorderMedium,
    val footerHorizontalPadding: Dp = TnyxDimens.SpaceL,
    val footerVerticalPadding: Dp = 14.dp,
    val overlayAlpha: Float = 0.82f,
    val gridAlpha: Float = 0.45f,
    val rulerOverlayAlpha: Float = 0.85f,
    val rulerMajorTickAlpha: Float = 0.85f,
    val rulerMinorTickAlpha: Float = 0.5f,
    val rulerMinimumTickAlpha: Float = 0.08f,
)
