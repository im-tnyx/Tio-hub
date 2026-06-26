package com.tnyx.core.ui.components.navigation

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.tnyx.core.theme.TnyxTheme

data class TnyxTabItem<T>(
    val label: String,
    val icon: ImageVector,
    val value: T
)

/**
 * Tnyx Sliding Tab Switcher.
 * Safe for empty data and invalid selected values during loading states.
 */
@Composable
fun <T> TnyxTabSwitcher(
    tabs: List<TnyxTabItem<T>>,
    selectedValue: T,
    onTabSelected: (T) -> Unit,
    modifier: Modifier = Modifier
) {
    if (tabs.isEmpty()) return

    val selectedIndex = tabs.indexOfFirst { it.value == selectedValue }.let { index ->
        if (index >= 0) index else 0
    }
    val resolvedSelectedValue = tabs[selectedIndex].value

    BoxWithConstraints(
        modifier = modifier
            .height(TnyxTheme.dimens.TabSwitcherHeight)
            .fillMaxWidth()
            .clip(TnyxTheme.shapes.Material.medium)
            .background(TnyxTheme.colors.surfaceVariant.copy(alpha = 0.3f))
    ) {
        val tabWidth = maxWidth / tabs.size
        val indicatorOffset by animateDpAsState(
            targetValue = tabWidth * selectedIndex,
            animationSpec = tween(durationMillis = TnyxTheme.motion.DurationMedium1),
            label = "tabIndicatorOffset"
        )

        Box(
            modifier = Modifier
                .offset(x = indicatorOffset)
                .width(tabWidth)
                .fillMaxHeight()
                .padding(TnyxTheme.dimens.TabIndicatorPadding)
                .clip(TnyxTheme.shapes.Material.small)
                .background(TnyxTheme.colors.surfaceVariant)
        )

        Row(
            modifier = Modifier
                .fillMaxSize()
                .selectableGroup()
        ) {
            tabs.forEach { tab ->
                val isSelected = tab.value == resolvedSelectedValue
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .selectable(
                            selected = isSelected,
                            role = Role.Tab,
                            onClick = { onTabSelected(tab.value) }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = null,
                            tint = if (isSelected) TnyxTheme.colors.textPrimary else TnyxTheme.colors.textSecondary,
                            modifier = Modifier.size(TnyxTheme.dimens.TabIconSize)
                        )
                        Spacer(modifier = Modifier.width(TnyxTheme.dimens.SpaceS))
                        Text(
                            text = tab.label,
                            style = TnyxTheme.typography.labelMedium,
                            color = if (isSelected) TnyxTheme.colors.textPrimary else TnyxTheme.colors.textSecondary,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}
