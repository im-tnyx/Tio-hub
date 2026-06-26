package com.tnyx.core.ui.components.layouts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.tnyx.core.theme.TnyxTheme

/**
 * Reusable Screen Header for Tnyx App.
 * Supports alpha fading for scroll synchronization.
 */
@Composable
fun TnyxScreenHeader(
    title: String,
    modifier: Modifier = Modifier,
    alpha: Float = 1f,
    height: Dp = 44.dp,
    actionIcon: ImageVector? = null,
    onActionClick: (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .graphicsLayer { this.alpha = alpha }
            .background(TnyxTheme.colors.background)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title.uppercase(),
            style = TnyxTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = TnyxTheme.colors.textPrimary
        )

        if (actionIcon != null && onActionClick != null) {
            IconButton(
                onClick = onActionClick,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = actionIcon,
                    contentDescription = null,
                    tint = TnyxTheme.colors.textPrimary
                )
            }
        } else {
            // Spacer to maintain layout if no icon
            Spacer(modifier = Modifier.size(24.dp))
        }
    }
}
