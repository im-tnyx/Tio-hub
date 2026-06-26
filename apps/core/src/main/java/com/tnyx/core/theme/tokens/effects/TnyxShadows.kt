package com.tnyx.core.theme.tokens.effects

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Tnyx Design System Shadow Tokens
 */
data class TnyxShadow(
    val color: Color = Color.Black.copy(alpha = 0.1f),
    val borderRadius: Dp = 0.dp,
    val blurRadius: Dp = 0.dp,
    val offsetY: Dp = 0.dp,
    val offsetX: Dp = 0.dp,
    val spread: Dp = 0.dp
)

object TnyxShadows {
    val None = TnyxShadow(Color.Transparent)
    
    val Subtle = TnyxShadow(
        color = Color.Black.copy(alpha = 0.05f),
        blurRadius = 4.dp,
        offsetY = 2.dp
    )
    
    val Medium = TnyxShadow(
        color = Color.Black.copy(alpha = 0.1f),
        blurRadius = 12.dp,
        offsetY = 6.dp
    )
    
    val Strong = TnyxShadow(
        color = Color.Black.copy(alpha = 0.15f),
        blurRadius = 24.dp,
        offsetY = 12.dp
    )
}

/**
 * Custom Shadow Modifier for fine control.
 */
fun Modifier.tnyxShadow(shadow: TnyxShadow): Modifier = if (shadow == TnyxShadows.None) this else this.drawBehind {
    drawIntoCanvas { canvas ->
        val paint = Paint()
        val frameworkPaint = paint.asFrameworkPaint()
        if (shadow.blurRadius > 0.dp) {
            frameworkPaint.maskFilter = android.graphics.BlurMaskFilter(
                shadow.blurRadius.toPx(),
                android.graphics.BlurMaskFilter.Blur.NORMAL
            )
        }
        frameworkPaint.color = shadow.color.toArgb()

        val left = shadow.offsetX.toPx() - shadow.spread.toPx()
        val top = shadow.offsetY.toPx() - shadow.spread.toPx()
        val right = size.width + shadow.offsetX.toPx() + shadow.spread.toPx()
        val bottom = size.height + shadow.offsetY.toPx() + shadow.spread.toPx()

        canvas.drawRoundRect(
            left = left,
            top = top,
            right = right,
            bottom = bottom,
            radiusX = shadow.borderRadius.toPx(),
            radiusY = shadow.borderRadius.toPx(),
            paint = paint
        )
    }
}
