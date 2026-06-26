package com.tnyx.core.legal.presentation.widgets

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import com.tnyx.core.theme.TnyxTheme
import com.tnyx.core.theme.tokens.effects.TnyxShadow
import com.tnyx.core.theme.tokens.effects.tnyxShadow

@Composable
fun LegalCloseButton(
    onTap: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String = "Close legal document"
) {
    val colors = TnyxTheme.colors
    val dimens = TnyxTheme.dimens
    val size = dimens.ButtonHeight - dimens.SpaceS

    Surface(
        modifier = modifier
            .size(size)
            .tnyxShadow(
                TnyxShadow(
                    color = colors.error.copy(alpha = 0.32f),
                    borderRadius = dimens.RadiusFull,
                    blurRadius = dimens.SpaceSM,
                    offsetY = dimens.SpaceXXS
                )
            )
            .clip(CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                role = Role.Button,
                onClick = onTap
            ),
        color = colors.error,
        contentColor = Color.White,
        shape = CircleShape,
        border = BorderStroke(
            width = dimens.BorderMedium,
            color = colors.textPrimary.copy(alpha = 0.35f)
        ),
        tonalElevation = TnyxTheme.elevation.None,
        shadowElevation = TnyxTheme.elevation.Level3
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.Rounded.Close,
                contentDescription = contentDescription,
                tint = Color.White,
                modifier = Modifier.size(dimens.IconS)
            )
        }
    }
}
