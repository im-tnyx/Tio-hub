package com.tnyx.core.ui.components.cards

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import com.tnyx.core.theme.TnyxTheme
import com.tnyx.core.theme.tokens.effects.tnyxShadow

/**
 * Standard card variants for reusable Tnyx surfaces.
 */
enum class TnyxCardVariant {
    Surface,
    Elevated,
    Glass
}

/**
 * Tnyx Standard Card.
 * Keep feature-specific state outside this component and pass content through slots.
 */
@Composable
fun TnyxCard(
    modifier: Modifier = Modifier,
    variant: TnyxCardVariant = TnyxCardVariant.Surface,
    shape: Shape? = null,
    content: @Composable () -> Unit
) {
    val tokens = TnyxTheme.components.card
    val isDark = TnyxTheme.colors.isDark
    val finalShape = shape ?: RoundedCornerShape(tokens.cornerRadius)
    val backgroundColor = when {
        variant == TnyxCardVariant.Glass && isDark -> TnyxTheme.colors.surfaceVariant.copy(alpha = 0.8f)
        variant == TnyxCardVariant.Glass -> TnyxTheme.colors.surfaceRaised.copy(alpha = 0.72f)
        else -> tokens.containerColor
    }
    val borderColor = when (variant) {
        TnyxCardVariant.Glass -> tokens.borderColor.copy(alpha = 0.16f)
        else -> tokens.borderColor
    }
    val cardModifier = when (variant) {
        TnyxCardVariant.Elevated -> modifier.tnyxShadow(TnyxTheme.shadows.Subtle)
        else -> modifier
    }

    Surface(
        modifier = cardModifier,
        shape = finalShape,
        color = backgroundColor,
        contentColor = TnyxTheme.colors.textPrimary,
        border = BorderStroke(tokens.borderWidth, borderColor),
        shadowElevation = if (variant == TnyxCardVariant.Elevated) tokens.elevation else TnyxTheme.elevation.None,
        tonalElevation = TnyxTheme.elevation.None
    ) {
        Box(modifier = Modifier.padding(tokens.contentPadding)) {
            content()
        }
    }
}
