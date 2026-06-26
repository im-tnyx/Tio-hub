package com.tnyx.features.welcome.presentation.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.tnyx.core.theme.TnyxTheme

@Composable
fun WelcomeBackdrop() {
    // [TAG: CANVAS BACKGROUND MAPPING]
    // Uses Scaffold background color which corresponds to CanvasBackground (White in light, 121212 in dark)
    val themeColor = TnyxTheme.colors.background

    Box(modifier = Modifier.fillMaxSize()) {
        // Gradient 1: Top subtle overlay (fill)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0.0f to themeColor.copy(alpha = 0.18f),
                        0.35f to themeColor.copy(alpha = 0.08f),
                        1.0f to Color.Transparent
                    )
                )
        )

        // Gradient 2: Bottom heavy transition (85% height)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        0.0f to Color.Transparent,
                        0.20f to themeColor.copy(alpha = 0.30f),
                        0.30f to themeColor,
                        0.65f to themeColor,
                        0.75f to themeColor,
                        1.0f to themeColor
                    )
                )
        )
    }
}
