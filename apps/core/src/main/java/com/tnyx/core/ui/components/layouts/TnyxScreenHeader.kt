package com.tnyx.core.ui.components.layouts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.tnyx.core.theme.TnyxTheme
import com.tnyx.core.theme.tokens.components.TnyxHeaderSize

/**
 * Reusable Screen Header for Tnyx App.
 * Supports alpha fading for scroll synchronization.
 *
 * Use [size] to pick a token-driven height:
 * - [TnyxHeaderSize.Compact]  → 36dp (bottom-nav tab screens)
 * - [TnyxHeaderSize.Standard] → 56dp (detail/nested screens)
 *
 * Uses LocalMinimumInteractiveComponentSize = Dp.Unspecified to prevent
 * M3 IconButton's 48dp minimum touch target from overriding the compact
 * header height and shifting title text off-center.
 */
@Composable
fun TnyxScreenHeader(
    title: String,
    modifier: Modifier = Modifier,
    alpha: Float = 1f,
    size: TnyxHeaderSize = TnyxHeaderSize.Compact,
    navigationIcon: ImageVector? = null,
    onNavigationClick: (() -> Unit)? = null,
    uppercaseTitle: Boolean = true,
    reserveNavigationSpace: Boolean = true,
    actions: @Composable (RowScope.() -> Unit)? = null
) {
    val tokens = TnyxTheme.components.header
    val resolvedHeight: Dp = when (size) {
        TnyxHeaderSize.Compact  -> tokens.compactHeight
        TnyxHeaderSize.Standard -> tokens.standardHeight
    }

    CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides Dp.Unspecified) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .height(resolvedHeight)
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
                } else if (reserveNavigationSpace) {
                    Spacer(modifier = Modifier.width(tokens.leadingSpacing))
                }

                Text(
                    text = if (uppercaseTitle) title.uppercase() else title,
                    style = tokens.titleStyle,
                    color = tokens.contentColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
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
}
