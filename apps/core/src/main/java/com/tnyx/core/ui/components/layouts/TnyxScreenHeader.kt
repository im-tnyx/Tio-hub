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
import androidx.compose.ui.unit.Dp
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
    height: Dp? = null,
    navigationIcon: ImageVector? = null,
    onNavigationClick: (() -> Unit)? = null,
    actions: @Composable (RowScope.() -> Unit)? = null
) {
    val tokens = TnyxTheme.components.header
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(height ?: tokens.height)
            .graphicsLayer { this.alpha = alpha }
            .background(tokens.containerColor)
            .padding(horizontal = tokens.horizontalPadding),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            if (navigationIcon != null && onNavigationClick != null) {
                IconButton(onClick = onNavigationClick) {
                    Icon(
                        imageVector = navigationIcon,
                        contentDescription = "Back",
                        tint = tokens.contentColor
                    )
                }
            } else {
                Spacer(modifier = Modifier.width(tokens.leadingSpacing))
            }

            Text(
                text = title.uppercase(),
                style = tokens.titleStyle,
                color = tokens.contentColor
            )
        }

        if (actions != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                content = actions
            )
        } else {
            Spacer(modifier = Modifier.size(tokens.actionPlaceholderSize))
        }
    }
}
