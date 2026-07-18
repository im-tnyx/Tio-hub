package com.tnyx.core.ui.components.buttons

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import com.tnyx.core.theme.TnyxTheme

/**
 * Variants for Secondary button styles
 */
enum class TnyxSecondaryVariant {
    Standard, // Bold primary border
    Muted     // Subtle grey border (like outlined cards)
}

/**
 * Tnyx Primary Action Button - Strictly Monochrome / Solid
 */
@Composable
fun TnyxPrimaryButton(
    text: String,
    onPressed: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leading: @Composable (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null,
    expand: Boolean = false,
    textAlign: TextAlign = TextAlign.Center,
    maxLines: Int = 1,
    overflow: TextOverflow = TextOverflow.Ellipsis,
    height: Dp? = null
) {
    val tokens = TnyxTheme.components.button
    
    Button(
        onClick = onPressed,
        modifier = modifier
            .then(if (expand) Modifier.fillMaxWidth() else Modifier)
            .height(height ?: tokens.height),
        enabled = enabled,
        shape = TnyxTheme.shapes.Material.medium,
        colors = ButtonDefaults.buttonColors(
            containerColor = tokens.containerColor,
            contentColor = tokens.contentColor,
            disabledContainerColor = tokens.disabledContainerColor,
            disabledContentColor = tokens.disabledContentColor
        ),
        contentPadding = PaddingValues(horizontal = tokens.horizontalPadding)
    ) {
        ActionButtonContent(
            text = text,
            leading = leading,
            trailing = trailing,
            textAlign = textAlign,
            maxLines = maxLines,
            overflow = overflow,
            spacing = tokens.iconSpacing,
            textStyle = tokens.textStyle ?: TnyxTheme.typography.labelLarge
        )
    }
}

/**
 * Tnyx Secondary / Outlined Button
 * Supports Standard (Primary Border) and Muted (Subtle Border) variants.
 */
@Composable
fun TnyxSecondaryButton(
    text: String,
    onPressed: () -> Unit,
    modifier: Modifier = Modifier,
    variant: TnyxSecondaryVariant = TnyxSecondaryVariant.Standard,
    enabled: Boolean = true,
    leading: @Composable (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null,
    expand: Boolean = false,
    textAlign: TextAlign = TextAlign.Center,
    maxLines: Int = 1,
    overflow: TextOverflow = TextOverflow.Ellipsis,
    height: Dp? = null
) {
    val tokens = TnyxTheme.components.button
    
    val borderColor = when (variant) {
        TnyxSecondaryVariant.Standard -> if (enabled) tokens.secondaryBorderColor else tokens.disabledBorderColor
        TnyxSecondaryVariant.Muted -> if (enabled) tokens.mutedBorderColor else tokens.disabledBorderColor
    }

    OutlinedButton(
        onClick = onPressed,
        modifier = modifier
            .then(if (expand) Modifier.fillMaxWidth() else Modifier)
            .height(height ?: tokens.height),
        enabled = enabled,
        shape = TnyxTheme.shapes.Material.medium,
        border = BorderStroke(TnyxTheme.dimens.BorderThin, borderColor),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.Transparent,
            contentColor = tokens.secondaryContentColor,
            disabledContentColor = tokens.disabledContentColor
        ),
        contentPadding = PaddingValues(horizontal = tokens.horizontalPadding)
    ) {
        ActionButtonContent(
            text = text,
            leading = leading,
            trailing = trailing,
            textAlign = textAlign,
            maxLines = maxLines,
            overflow = overflow,
            spacing = tokens.iconSpacing,
            textStyle = tokens.textStyle ?: TnyxTheme.typography.labelLarge
        )
    }
}

/**
 * Tnyx Ghost Button - Only text, no border, no background
 */
@Composable
fun TnyxGhostButton(
    text: String,
    onPressed: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leading: @Composable (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null,
    expand: Boolean = false,
    textAlign: TextAlign = TextAlign.Center,
    maxLines: Int = 1,
    overflow: TextOverflow = TextOverflow.Ellipsis,
    height: Dp? = null
) {
    val tokens = TnyxTheme.components.button
    
    TextButton(
        onClick = onPressed,
        modifier = modifier
            .then(if (expand) Modifier.fillMaxWidth() else Modifier)
            .height(height ?: tokens.height),
        enabled = enabled,
        shape = TnyxTheme.shapes.Material.medium,
        colors = ButtonDefaults.textButtonColors(
            contentColor = tokens.secondaryContentColor,
            disabledContentColor = tokens.disabledContentColor
        ),
        contentPadding = PaddingValues(horizontal = tokens.horizontalPadding)
    ) {
        ActionButtonContent(
            text = text,
            leading = leading,
            trailing = trailing,
            textAlign = textAlign,
            maxLines = maxLines,
            overflow = overflow,
            spacing = tokens.iconSpacing,
            textStyle = tokens.textStyle ?: TnyxTheme.typography.labelLarge
        )
    }
}

@Composable
private fun ActionButtonContent(
    text: String,
    leading: @Composable (() -> Unit)?,
    trailing: @Composable (() -> Unit)?,
    textAlign: TextAlign,
    maxLines: Int,
    overflow: TextOverflow,
    spacing: androidx.compose.ui.unit.Dp,
    textStyle: androidx.compose.ui.text.TextStyle
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        if (leading != null) {
            leading()
            Spacer(modifier = Modifier.width(spacing))
        }
        
        Text(
            text = text,
            style = textStyle,
            textAlign = textAlign,
            maxLines = maxLines,
            overflow = overflow
        )

        if (trailing != null) {
            Spacer(modifier = Modifier.width(spacing))
            trailing()
        }
    }
}
