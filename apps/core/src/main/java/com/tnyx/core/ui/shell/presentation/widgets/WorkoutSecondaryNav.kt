package com.tnyx.core.ui.shell.presentation.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import com.tnyx.core.theme.TnyxTheme
import com.tnyx.core.ui.shell.presentation.state.WorkoutSubTab

/**
 * Secondary tab bar shown above the bottom nav bar when Workout tab is active.
 * Provides sub-navigation: History | Explore | Routines.
 *
 * Scroll-hide behavior (translationY animation) is applied externally via [modifier].
 */
@Composable
fun WorkoutSecondaryNav(
    selectedTab: WorkoutSubTab,
    onTabSelected: (WorkoutSubTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val tokens = TnyxTheme.components.navigation
    val navShape = RoundedCornerShape(topStart = tokens.workoutSecondaryNavCornerRadius, topEnd = tokens.workoutSecondaryNavCornerRadius)
    val borderColor = TnyxTheme.colors.textPrimary.copy(alpha = tokens.workoutSecondaryNavBorderAlpha)
    val borderWidth = TnyxTheme.dimens.BorderThin

    androidx.compose.foundation.layout.Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = TnyxTheme.dimens.SpaceSM) // Padding moved here for side space
            .clip(navShape)
            .background(TnyxTheme.colors.surface.copy(alpha = tokens.workoutSecondaryNavBackgroundAlpha))
            .drawBehind {
                val strokePx = borderWidth.toPx()
                val radiusPx = tokens.workoutSecondaryNavCornerRadius.toPx()
                
                // Draw Left Vertical Line (starts after top radius)
                drawLine(
                    color = borderColor,
                    start = Offset(0f, radiusPx),
                    end = Offset(0f, size.height),
                    strokeWidth = strokePx
                )
                // Draw Right Vertical Line (starts after top radius)
                drawLine(
                    color = borderColor,
                    start = Offset(size.width, radiusPx),
                    end = Offset(size.width, size.height),
                    strokeWidth = strokePx
                )
                // Draw Top Curved Border / Horizontal Line
                // For simplicity and clean look with RoundedCornerShape, 
                // we draw the top line between the radii.
                drawLine(
                    color = borderColor,
                    start = Offset(radiusPx, 0f),
                    end = Offset(size.width - radiusPx, 0f),
                    strokeWidth = strokePx
                )
                // Draw Top-Left Arc
                drawArc(
                    color = borderColor,
                    startAngle = 180f,
                    sweepAngle = 90f,
                    useCenter = false,
                    topLeft = Offset(0f, 0f),
                    size = androidx.compose.ui.geometry.Size(radiusPx * 2, radiusPx * 2),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokePx)
                )
                // Draw Top-Right Arc
                drawArc(
                    color = borderColor,
                    startAngle = 270f,
                    sweepAngle = 90f,
                    useCenter = false,
                    topLeft = Offset(size.width - radiusPx * 2, 0f),
                    size = androidx.compose.ui.geometry.Size(radiusPx * 2, radiusPx * 2),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokePx)
                )
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(tokens.workoutSecondaryNavHeight),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            WorkoutSubTabItem(
                icon = Icons.Outlined.History,
                label = "History",
                isSelected = selectedTab == WorkoutSubTab.History,
                onClick = { onTabSelected(WorkoutSubTab.History) }
            )
            WorkoutSubTabItem(
                icon = Icons.Outlined.Explore,
                label = "Explore",
                isSelected = selectedTab == WorkoutSubTab.Explore,
                onClick = { onTabSelected(WorkoutSubTab.Explore) }
            )
            WorkoutSubTabItem(
                icon = Icons.Outlined.FolderOpen,
                label = "Routines",
                isSelected = selectedTab == WorkoutSubTab.Routines,
                onClick = { onTabSelected(WorkoutSubTab.Routines) }
            )
        }
    }
}

@Composable
private fun RowScope.WorkoutSubTabItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val tokens = TnyxTheme.components.navigation
    val color = if (isSelected) TnyxTheme.colors.textPrimary else TnyxTheme.colors.textSecondary

    Row(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .clickable(
                onClick = onClick,
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = color,
            modifier = Modifier.size(tokens.workoutSecondaryNavIconSize)
        )
        Spacer(modifier = Modifier.width(TnyxTheme.dimens.SpaceS))
        Text(
            text = label,
            style = TnyxTheme.typography.labelSmall,
            color = color,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}
