package com.tnyx.core.ui.components.cards

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import com.tnyx.core.theme.TnyxTheme
import com.tnyx.core.theme.tokens.effects.tnyxShadow

/**
 * Standard card variants for reusable Tnyx surfaces.
 */
enum class TnyxCardVariant {
    Surface,
    Elevated,
    Glass,
    Outlined,
    Normal
}

/**
 * Tnyx Standard Card.
 * Support for onClick, onLongClick and custom padding added for high reusability.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TnyxCard(
    modifier: Modifier = Modifier,
    variant: TnyxCardVariant = TnyxCardVariant.Surface,
    shape: Shape? = null,
    padding: Dp? = null, // Custom padding support
    containerColor: Color? = null,
    borderColor: Color? = null,
    borderWidth: Dp? = null,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val tokens = TnyxTheme.components.card
    val finalShape = shape ?: RoundedCornerShape(tokens.cornerRadius)
    val finalPadding = padding ?: tokens.contentPadding
    val finalBorderWidth = borderWidth ?: tokens.borderWidth

    val backgroundColor = containerColor ?: when (variant) {
        TnyxCardVariant.Glass -> tokens.glassContainerColor
        TnyxCardVariant.Normal -> tokens.normalContainerColor
        TnyxCardVariant.Outlined -> Color.Transparent
        else -> tokens.containerColor
    }

    val finalBorderColor = borderColor ?: when (variant) {
        TnyxCardVariant.Glass -> tokens.glassBorderColor
        TnyxCardVariant.Outlined -> tokens.outlinedBorderColor
        TnyxCardVariant.Normal -> Color.Transparent
        else -> tokens.borderColor
    }

    val cardModifier = modifier
        .then(if (variant == TnyxCardVariant.Elevated) Modifier.tnyxShadow(TnyxTheme.shadows.Subtle) else Modifier)
        .then(
            if (onClick != null || onLongClick != null) {
                Modifier.combinedClickable(
                    onClick = { onClick?.invoke() },
                    onLongClick = { onLongClick?.invoke() }
                )
            } else Modifier
        )

    Surface(
        modifier = cardModifier,
        shape = finalShape,
        color = backgroundColor,
        contentColor = TnyxTheme.colors.textPrimary,
        border = BorderStroke(finalBorderWidth, finalBorderColor),
        shadowElevation = if (variant == TnyxCardVariant.Elevated) tokens.elevation else TnyxTheme.elevation.None,
        tonalElevation = TnyxTheme.elevation.None
    ) {
        Box(modifier = Modifier.padding(finalPadding)) {
            content()
        }
    }
}
