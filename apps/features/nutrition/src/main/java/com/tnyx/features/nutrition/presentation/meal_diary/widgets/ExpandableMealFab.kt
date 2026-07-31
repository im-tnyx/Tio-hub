package com.tnyx.features.nutrition.presentation.meal_diary.widgets

import androidx.annotation.DrawableRes
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.tnyx.core.theme.TnyxTheme
import com.tnyx.features.nutrition.R
import kotlin.math.PI

private const val EXPAND_DURATION_MS = 420
private val FAB_SIZE: Dp = 36.dp
private val SUB_FAB_SIZE: Dp = 36.dp
private val ICON_SIZE: Dp = 26.dp
private val TRAVEL_DISTANCE: Dp = 56.dp

/**
 * Expandable Meal FAB — updated to shape size 36.dp and icon size 26.dp.
 * Uses custom vector drawables (ic_fab_main, ic_mic, ic_camera, ic_keyboard).
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
            drawableResId = R.drawable.ic_mic,
            contentDescription = "Add meal by voice",
            targetOffsetX = -TRAVEL_DISTANCE,
            targetOffsetY = -TRAVEL_DISTANCE,
            containerColor = containerColor,
            iconColor = iconColor,
            borderColor = subBorderColor,
            size = SUB_FAB_SIZE,
            iconSize = ICON_SIZE,
            onClick = onMicClicked,
            modifier = Modifier.align(Alignment.BottomEnd),
        )

        // ── Sub-action: Camera (directly above) ───────────────────────────
        AnimatedSubFab(
            isExpanded = isExpanded,
            drawableResId = R.drawable.ic_camera,
            contentDescription = "Add meal by photo",
            targetOffsetX = 0.dp,
            targetOffsetY = -TRAVEL_DISTANCE,
            containerColor = containerColor,
            iconColor = iconColor,
            borderColor = subBorderColor,
            size = SUB_FAB_SIZE,
            iconSize = ICON_SIZE,
            onClick = onCameraClicked,
            modifier = Modifier.align(Alignment.BottomEnd),
        )

        // ── Sub-action: Search / Keyboard (left) ──────────────────────────
        AnimatedSubFab(
            isExpanded = isExpanded,
            drawableResId = R.drawable.ic_keyboard,
            contentDescription = "Add meal by search",
            targetOffsetX = -TRAVEL_DISTANCE,
            targetOffsetY = 0.dp,
            containerColor = containerColor,
            iconColor = iconColor,
            borderColor = subBorderColor,
            size = SUB_FAB_SIZE,
            iconSize = ICON_SIZE,
            onClick = onSearchClicked,
            modifier = Modifier.align(Alignment.BottomEnd),
        )

        // ── Main FAB button ────────────────────────────────────────────────
        Surface(
            onClick = onToggle,
            shape = CircleShape,
            color = if (isExpanded) Color.Transparent else containerColor,
            shadowElevation = if (isExpanded) 0.dp else 4.dp,
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
                            modifier = Modifier.size(20.dp),
                        )
                    } else {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_fab_main),
                            contentDescription = null,
                            tint = iconColor,
                            modifier = Modifier.size(ICON_SIZE),
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
    @DrawableRes drawableResId: Int,
    contentDescription: String,
    targetOffsetX: Dp,
    targetOffsetY: Dp,
    containerColor: Color,
    iconColor: Color,
    borderColor: Color,
    size: Dp,
    iconSize: Dp,
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
                painter = painterResource(id = drawableResId),
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(iconSize),
            )
        }
    }
}
