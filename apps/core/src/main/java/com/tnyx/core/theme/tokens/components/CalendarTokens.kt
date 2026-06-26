package com.tnyx.core.theme.tokens.components

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.tnyx.core.theme.tokens.foundation.TnyxDimens

/**
 * Tnyx Design System - Calendar Component Tokens
 */
data class CalendarTokens(
    val height: Dp = 56.dp,
    val sideSectionWidth: Dp = 60.dp,
    val dayWidth: Dp = TnyxDimens.CalendarDayWidth,
    val indicatorSize: Dp = TnyxDimens.CalendarIndicatorSize,
    val iconSize: Dp = TnyxDimens.IconS,
    val dividerAlpha: Float = 0.08f,
    val contentAlpha: Float = 0.70f,
    val labelAlpha: Float = 0.38f,
    val futureAlpha: Float = 0.30f
)
