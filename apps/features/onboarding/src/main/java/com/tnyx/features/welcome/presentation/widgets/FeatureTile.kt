package com.tnyx.features.welcome.presentation.widgets

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.tnyx.core.theme.TnyxTheme
import com.tnyx.core.ui.components.cards.TnyxCard

@Composable
fun FeatureItem(
    text: String,
    icon: ImageVector
) {
    TnyxCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = TnyxTheme.dimens.SpaceXS),
        shape = RoundedCornerShape(TnyxTheme.dimens.RadiusXL) // यहाँ आप Radius बदल सकते हैं
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.padding(end = TnyxTheme.dimens.SpaceSM),
                tint = TnyxTheme.colors.primary
            )
            Text(
                text = text,
                style = TnyxTheme.typography.bodyMedium,
                color = TnyxTheme.colors.textPrimary
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = null,
                modifier = Modifier.size(TnyxTheme.dimens.IconS),
                tint = TnyxTheme.colors.textMuted
            )
        }
    }
}
