package com.tnyx.core.ui.shell.presentation.widgets

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import com.tnyx.core.R
import com.tnyx.core.theme.TnyxTheme
import com.tnyx.core.ui.shell.presentation.state.ShellTab

private data class BottomNavTab(
    val tab: ShellTab,
    val label: String,
    val selectedIconRes: Int?,
    val unselectedIconRes: Int?,
    val isSpecial: Boolean = false
)

private val NAV_TABS = listOf(
    BottomNavTab(
        tab = ShellTab.Home, label = "Home",
        selectedIconRes = R.drawable.ic_nav_home_filled,
        unselectedIconRes = R.drawable.ic_nav_home_outlined
    ),
    BottomNavTab(
        tab = ShellTab.Nutrition, label = "Nutrition",
        selectedIconRes = R.drawable.ic_nav_nutrition_filled,
        unselectedIconRes = R.drawable.ic_nav_nutrition_outlined
    ),
    BottomNavTab(
        tab = ShellTab.Ai, label = "AI",
        selectedIconRes = null, unselectedIconRes = null,
        isSpecial = true
    ),
    BottomNavTab(
        tab = ShellTab.Workout, label = "Workout",
        selectedIconRes = R.drawable.ic_nav_workout_filled,
        unselectedIconRes = R.drawable.ic_nav_workout_outlined
    ),
    BottomNavTab(
        tab = ShellTab.Progress, label = "Progress",
        selectedIconRes = R.drawable.ic_nav_progress_filled,
        unselectedIconRes = R.drawable.ic_nav_progress_outlined
    ),
)

@Composable
fun MainBottomNav(
    selectedTab: ShellTab,
    onTabSelected: (ShellTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val tokens = TnyxTheme.components.navigation
    Column(
        modifier = modifier.fillMaxWidth()
            .background(TnyxTheme.colors.surface)
            .navigationBarsPadding()
    ) {
        HorizontalDivider(
            thickness = TnyxTheme.dimens.BorderThin,
            color = TnyxTheme.colors.textPrimary.copy(alpha = tokens.bottomNavDividerAlpha)
        )

        Row(
            modifier = Modifier.fillMaxWidth().height(tokens.bottomNavHeight),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            NAV_TABS.forEach { navTab ->
                val isSelected = selectedTab == navTab.tab
                if (navTab.isSpecial) {
                    AiNavTabItem(isSelected = isSelected, onClick = { onTabSelected(navTab.tab) })
                } else {
                    NavIcon(
                        selectedIconRes = navTab.selectedIconRes!!,
                        unselectedIconRes = navTab.unselectedIconRes!!,
                        label = navTab.label,
                        isSelected = isSelected,
                        onClick = { onTabSelected(navTab.tab) }
                    )
                }
            }
        }
    }
}

@Composable
private fun RowScope.NavIcon(
    selectedIconRes: Int,
    unselectedIconRes: Int,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val tokens = TnyxTheme.components.navigation
    
    // 1. Smooth Color Transition
    val animatedColor by animateColorAsState(
        targetValue = if (isSelected) TnyxTheme.colors.textPrimary else TnyxTheme.colors.textSecondary,
        animationSpec = tween(durationMillis = 300),
        label = "nav_color_anim"
    )

    // 2. Smooth Scale Effect
    val animatedScale by animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "nav_scale_anim"
    )

    Column(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .clickable(
                onClick = onClick,
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(tokens.bottomNavRippleSize)
                .graphicsLayer {
                    scaleX = animatedScale
                    scaleY = animatedScale
                },
            contentAlignment = Alignment.Center
        ) {
            // 3. Crossfade between icons to avoid "jump"
            Crossfade(
                targetState = if (isSelected) selectedIconRes else unselectedIconRes,
                animationSpec = tween(durationMillis = 250),
                label = "nav_icon_fade"
            ) { iconRes ->
                Icon(
                    painter = painterResource(id = iconRes),
                    contentDescription = label,
                    tint = animatedColor,
                    modifier = Modifier.size(tokens.bottomNavIconSize)
                )
            }
        }
        Text(
            text = label,
            style = TnyxTheme.typography.labelSmall,
            color = animatedColor,
            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
        )
    }
}

@Composable
private fun RowScope.AiNavTabItem(isSelected: Boolean, onClick: () -> Unit) {
    val tokens = TnyxTheme.components.navigation
    Box(
        modifier = Modifier.weight(1f).fillMaxHeight().clickable(
            onClick = onClick,
            interactionSource = remember { MutableInteractionSource() },
            indication = null
        ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier.size(tokens.bottomNavAiIconSize).clip(CircleShape).clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            AiTabIcon(isSelected = isSelected)
        }
    }
}
