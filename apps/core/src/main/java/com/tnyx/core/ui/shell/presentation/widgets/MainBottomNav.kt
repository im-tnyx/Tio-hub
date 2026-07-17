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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.weight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import com.tnyx.core.R
import com.tnyx.core.theme.TnyxTheme
import com.tnyx.core.ui.shell.domain.model.ShellTab

private data class BottomNavTab(
    val tab: ShellTab,
    val label: String,
    val selectedIconRes: Int? = null,
    val unselectedIconRes: Int? = null,
    val imageVector: ImageVector? = null,
)

private val NAV_TABS = listOf(
    BottomNavTab(
        tab = ShellTab.Home,
        label = "Home",
        selectedIconRes = R.drawable.ic_nav_home_filled,
        unselectedIconRes = R.drawable.ic_nav_home_outlined,
    ),
    BottomNavTab(
        tab = ShellTab.Nutrition,
        label = "Nutrition",
        selectedIconRes = R.drawable.ic_nav_nutrition_filled,
        unselectedIconRes = R.drawable.ic_nav_nutrition_outlined,
    ),
    BottomNavTab(
        tab = ShellTab.MealPlan,
        label = "Meal Plan",
        imageVector = Icons.Rounded.DateRange,
    ),
    BottomNavTab(
        tab = ShellTab.Ai,
        label = "Tio",
        imageVector = Icons.Outlined.AutoAwesome,
    ),
    BottomNavTab(
        tab = ShellTab.Workout,
        label = "Workout",
        selectedIconRes = R.drawable.ic_nav_workout_filled,
        unselectedIconRes = R.drawable.ic_nav_workout_outlined,
    ),
    BottomNavTab(
        tab = ShellTab.WorkoutLibrary,
        label = "Library",
        imageVector = Icons.Rounded.MenuBook,
    ),
    BottomNavTab(
        tab = ShellTab.Progress,
        label = "Progress",
        selectedIconRes = R.drawable.ic_nav_progress_filled,
        unselectedIconRes = R.drawable.ic_nav_progress_outlined,
    ),
    BottomNavTab(
        tab = ShellTab.You,
        label = "You",
        imageVector = Icons.Rounded.Person,
    ),
)

private val NAV_TABS_BY_ID = NAV_TABS.associateBy(BottomNavTab::tab)

@Composable
fun MainBottomNav(
    tabs: List<ShellTab>,
    selectedTab: ShellTab,
    onTabSelected: (ShellTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    val tokens = TnyxTheme.components.navigation

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(TnyxTheme.colors.surface)
            .navigationBarsPadding()
    ) {
        HorizontalDivider(
            thickness = TnyxTheme.dimens.BorderThin,
            color = TnyxTheme.colors.textPrimary.copy(alpha = tokens.bottomNavDividerAlpha)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(tokens.bottomNavHeight),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround,
        ) {
            tabs.mapNotNull(NAV_TABS_BY_ID::get).forEach { navTab ->
                NavIcon(
                    selectedIconRes = navTab.selectedIconRes,
                    unselectedIconRes = navTab.unselectedIconRes,
                    imageVector = navTab.imageVector,
                    label = navTab.label,
                    isSelected = selectedTab == navTab.tab,
                    onClick = { onTabSelected(navTab.tab) },
                )
            }
        }
    }
}

@Composable
private fun RowScope.NavIcon(
    selectedIconRes: Int?,
    unselectedIconRes: Int?,
    imageVector: ImageVector?,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val tokens = TnyxTheme.components.navigation
    val animatedColor by animateColorAsState(
        targetValue = if (isSelected) {
            TnyxTheme.colors.textPrimary
        } else {
            TnyxTheme.colors.textSecondary
        },
        animationSpec = tween(durationMillis = 300),
        label = "nav_color_anim",
    )
    val animatedScale by animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow,
        ),
        label = "nav_scale_anim",
    )

    Column(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .clickable(
                onClick = onClick,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(tokens.bottomNavRippleSize)
                .graphicsLayer {
                    scaleX = animatedScale
                    scaleY = animatedScale
                },
            contentAlignment = Alignment.Center,
        ) {
            if (imageVector != null) {
                Icon(
                    imageVector = imageVector,
                    contentDescription = label,
                    tint = animatedColor,
                    modifier = Modifier.size(tokens.bottomNavIconSize),
                )
            } else {
                Crossfade(
                    targetState = if (isSelected) {
                        requireNotNull(selectedIconRes)
                    } else {
                        requireNotNull(unselectedIconRes)
                    },
                    animationSpec = tween(durationMillis = 250),
                    label = "nav_icon_fade",
                ) { iconRes ->
                    Icon(
                        painter = painterResource(id = iconRes),
                        contentDescription = label,
                        tint = animatedColor,
                        modifier = Modifier.size(tokens.bottomNavIconSize),
                    )
                }
            }
        }

        Text(
            text = label,
            style = TnyxTheme.typography.labelSmall,
            color = animatedColor,
            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
            maxLines = 1,
        )
    }
}
