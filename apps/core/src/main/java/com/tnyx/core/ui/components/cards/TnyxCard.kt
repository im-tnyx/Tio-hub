package com.tnyx.core.ui.components.cards

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
 * Support for onClick and custom padding added for high reusability.
 */
@Composable
fun TnyxCard(
    modifier: Modifier = Modifier,
    variant: TnyxCardVariant = TnyxCardVariant.Surface,
    shape: Shape? = null,
    padding: Dp? = null, // Custom padding support
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val tokens = TnyxTheme.components.card
    val isDark = TnyxTheme.colors.isDark
    val finalShape = shape ?: RoundedCornerShape(tokens.cornerRadius)
    val finalPadding = padding ?: tokens.contentPadding

    val backgroundColor = when {
        variant == TnyxCardVariant.Glass && isDark -> TnyxTheme.colors.surfaceVariant.copy(alpha = 0.8f)
        variant == TnyxCardVariant.Glass -> TnyxTheme.colors.surfaceRaised.copy(alpha = 0.72f)
        variant == TnyxCardVariant.Normal -> TnyxTheme.colors.surfaceRaised
        variant == TnyxCardVariant.Outlined -> Color.Transparent
        else -> tokens.containerColor
    }

    val borderColor = when (variant) {
        TnyxCardVariant.Glass -> tokens.borderColor.copy(alpha = 0.16f)
        TnyxCardVariant.Outlined -> TnyxTheme.colors.textPrimary.copy(alpha = 0.16f)
        TnyxCardVariant.Normal -> Color.Transparent
        else -> tokens.borderColor
    }

    val cardModifier = modifier
        .then(if (variant == TnyxCardVariant.Elevated) Modifier.tnyxShadow(TnyxTheme.shadows.Subtle) else Modifier)
        .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)

    Surface(
        modifier = cardModifier,
        shape = finalShape,
        color = backgroundColor,
        contentColor = TnyxTheme.colors.textPrimary,
        border = BorderStroke(tokens.borderWidth, borderColor),
        shadowElevation = if (variant == TnyxCardVariant.Elevated) tokens.elevation else TnyxTheme.elevation.None,
        tonalElevation = TnyxTheme.elevation.None
    ) {
        Box(modifier = Modifier.padding(finalPadding)) {
            content()
        }
    }
}
