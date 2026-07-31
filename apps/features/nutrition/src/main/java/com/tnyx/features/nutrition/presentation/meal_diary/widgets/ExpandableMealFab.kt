package com.tnyx.features.nutrition.presentation.meal_diary.widgets

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.tnyx.core.theme.TnyxTheme
import androidx.compose.material.icons.rounded.KeyboardAlt
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

private const val EXPAND_DURATION_MS = 420
private val FAB_SIZE: Dp = 56.dp
private val SUB_FAB_SIZE: Dp = 48.dp
private val TRAVEL_DISTANCE: Dp = 76.dp

/**
 * Expandable Meal FAB — matches Flutter ExpandableMealFab UX exactly.
 *
 * Collapsed: circular button with restaurant/meal icon, bottom-right.
 * Expanded:  main icon switches to close (×) with scale+fade animation;
 *            3 sub-action buttons (Mic, Camera, Search) fan out in 420 ms
 *            with translate + fade + scale + rotate transitions.
 *            Tapping the backdrop collapses the FAB.
 *
 * Colors follow Flutter's inverseContainerColor pattern:
 *   dark theme  → white container, black icon
 *   light theme → black container, white icon
 */
@Composable
fun ExpandableMealFab(
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onCollapse: () -> Unit,
    onSearchClicked: () -> Unit,
    onMicClicked: () -> Unit,
    onCameraClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isDark = isSystemInDarkTheme()
    val containerColor = if (isDark) Color.White else Color(0xFF111111)
    val iconColor = if (isDark) Color(0xFF111111) else Color.White
    val subBorderColor = TnyxTheme.colors.textSecondary.copy(alpha = 0.18f)

    Box(modifier = modifier) {
        // ── Backdrop overlay ──────────────────────────────────────────────
        if (isExpanded) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        indication = null,
                        interactionSource = null,
                        onClick = onCollapse,
                    )
            )
        }

        // ── Sub-action: Mic (top-left diagonal) ───────────────────────────
        AnimatedSubFab(
            isExpanded = isExpanded,
            icon = Icons.Rounded.Mic,
            contentDescription = "Add meal by voice",
            targetOffsetX = -TRAVEL_DISTANCE,
            targetOffsetY = -TRAVEL_DISTANCE,
            containerColor = containerColor,
            iconColor = iconColor,
            borderColor = subBorderColor,
            size = SUB_FAB_SIZE,
            onClick = onMicClicked,
            modifier = Modifier.align(Alignment.BottomEnd),
        )

        // ── Sub-action: Camera (directly above) ───────────────────────────
        AnimatedSubFab(
            isExpanded = isExpanded,
            icon = Icons.Rounded.PhotoCamera,
            contentDescription = "Add meal by photo",
            targetOffsetX = 0.dp,
            targetOffsetY = -TRAVEL_DISTANCE,
            containerColor = containerColor,
            iconColor = iconColor,
            borderColor = subBorderColor,
            size = SUB_FAB_SIZE,
            onClick = onCameraClicked,
            modifier = Modifier.align(Alignment.BottomEnd),
        )

        // ── Sub-action: Search / Keyboard (left) ──────────────────────────
        AnimatedSubFab(
            isExpanded = isExpanded,
            icon = Icons.Rounded.KeyboardAlt,
            contentDescription = "Add meal by search",
            targetOffsetX = -TRAVEL_DISTANCE,
            targetOffsetY = 0.dp,
            containerColor = containerColor,
            iconColor = iconColor,
            borderColor = subBorderColor,
            size = SUB_FAB_SIZE,
            onClick = onSearchClicked,
            modifier = Modifier.align(Alignment.BottomEnd),
        )

        // ── Main FAB button ────────────────────────────────────────────────
        Surface(
            onClick = onToggle,
            shape = CircleShape,
            color = if (isExpanded) Color.Transparent else containerColor,
            shadowElevation = if (isExpanded) 0.dp else 6.dp,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(FAB_SIZE)
                .semantics { contentDescription = if (isExpanded) "Close meal options" else "Open meal options" },
        ) {
            Box(contentAlignment = Alignment.Center) {
                AnimatedContent(
                    targetState = isExpanded,
                    transitionSpec = {
                        (scaleIn(tween(200, easing = FastOutSlowInEasing)) +
                            fadeIn(tween(200))) togetherWith
                            (scaleOut(tween(150)) + fadeOut(tween(150)))
                    },
                    label = "fab_icon_switch",
                ) { expanded ->
                    if (expanded) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = null,
                            tint = if (isDark) Color.White else Color(0xFF111111),
                            modifier = Modifier.size(24.dp),
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Rounded.Restaurant,
                            contentDescription = null,
                            tint = iconColor,
                            modifier = Modifier.size(28.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AnimatedSubFab(
    isExpanded: Boolean,
    icon: ImageVector,
    contentDescription: String,
    targetOffsetX: Dp,
    targetOffsetY: Dp,
    containerColor: Color,
    iconColor: Color,
    borderColor: Color,
    size: Dp,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val progress by animateFloatAsState(
        targetValue = if (isExpanded) 1f else 0f,
        animationSpec = tween(durationMillis = EXPAND_DURATION_MS, easing = FastOutSlowInEasing),
        label = "sub_fab_progress",
    )

    val offsetXPx = (targetOffsetX.value * progress).dp
    val offsetYPx = (targetOffsetY.value * progress).dp
    val rotation = (PI / 2 * (1f - progress)).toFloat() * (180f / PI.toFloat())

    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = containerColor,
        shadowElevation = if (isExpanded) 4.dp else 0.dp,
        modifier = modifier
            .offset(x = offsetXPx, y = offsetYPx)
            .size(size)
            .alpha(progress)
            .scale(0.75f + 0.25f * progress)
            .rotate(rotation)
            .border(width = 1.dp, color = borderColor, shape = CircleShape)
            .semantics { this.contentDescription = contentDescription },
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(22.dp),
            )
        }
    }
}
