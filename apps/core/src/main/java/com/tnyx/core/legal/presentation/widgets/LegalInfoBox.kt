package com.tnyx.core.legal.presentation.widgets

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import com.tnyx.core.theme.TnyxTheme

private const val LegalInfoContainerAlpha = 0.5f
private const val LegalInfoBorderAlpha = 0.22f

@Composable
fun LegalInfoBox(
    text: String,
    modifier: Modifier = Modifier
) {
    val colors = TnyxTheme.colors
    val dimens = TnyxTheme.dimens

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = TnyxTheme.shapes.Material.large,
        color = colors.surfaceVariant.copy(alpha = LegalInfoContainerAlpha),
        contentColor = colors.textSecondary,
        border = BorderStroke(
            width = dimens.BorderThin,
            color = colors.textPrimary.copy(alpha = LegalInfoBorderAlpha)
        ),
        tonalElevation = TnyxTheme.elevation.None
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = dimens.SpaceSM,
                vertical = dimens.SpaceSM
            ),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Outlined.Info,
                contentDescription = null,
                tint = colors.textSecondary,
                modifier = Modifier.size(dimens.IconS)
            )
            Spacer(modifier = Modifier.width(dimens.SpaceM))
            Text(
                text = text,
                style = TnyxTheme.typography.bodyMedium.copy(
                    fontStyle = FontStyle.Italic,
                    fontWeight = FontWeight.SemiBold
                ),
                color = colors.textSecondary
            )
        }
    }
}
