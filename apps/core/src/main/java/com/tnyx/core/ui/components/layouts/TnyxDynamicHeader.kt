package com.tnyx.core.ui.components.layouts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import com.tnyx.core.theme.TnyxTheme

/**
 * Tnyx Dynamic Header with Gradient.
 * Used as a background overlay for top content sections.
 */
@Composable
fun TnyxDynamicHeader(
    modifier: Modifier = Modifier,
    height: Dp = TnyxTheme.dimens.HeaderGradientHeight,
    color: Color = TnyxTheme.colors.background
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .background(
                brush = Brush.verticalGradient(
                    0.0f to color,
                    0.6f to color.copy(alpha = 0.9f),
                    1.0f to Color.Transparent
                )
            )
    )
}
