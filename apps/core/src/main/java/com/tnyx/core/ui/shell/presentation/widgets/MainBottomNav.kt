package com.tnyx.core.ui.shell.presentation.widgets

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.tnyx.core.R
import com.tnyx.core.theme.TnyxTheme
import com.tnyx.core.ui.shell.presentation.state.ShellTab

// ─────────────────────────────────────────────────────────────────────────────
// TAB DEFINITIONS — यहाँ सारे tabs एक जगह define हैं। बाद में बदलने के लिए
// सिर्फ यहाँ edit करें।
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Bottom nav tab का data model।
 * [icon] = drawable resource id (Int) या null (AI tab जैसे special tabs के लिए)
 * [label] = bottom nav label
 * [tab] = corresponding ShellTab enum value
 * [isSpecial] = true होने पर custom rendering (जैसे AI tab का circular design)
 */
private data class BottomNavTab(
    val tab: ShellTab,
    val label: String,
    val iconRes: Int?,       // null = custom/special tab
    val isSpecial: Boolean = false
)

/**
 * सभी bottom nav tabs यहाँ define हैं।
 * क्रम: left से right जैसा bottom bar में दिखेगा।
 *
 * ✏️ Tab add/remove/rename करने के लिए ONLY यहाँ बदलें।
 */
private val NAV_TABS = listOf(
    BottomNavTab(
        tab = ShellTab.Home,
        label = "Home",
        iconRes = R.drawable.ic_nav_home_outlined
    ),
    BottomNavTab(
        tab = ShellTab.Nutrition,
        label = "Nutrition",
        iconRes = R.drawable.ic_nav_nutrition_outlined
    ),
    BottomNavTab(
        tab = ShellTab.Ai,
        label = "AI",
        iconRes = null,          // Special circular design — AiTabIcon use होगा
        isSpecial = true
    ),
    BottomNavTab(
        tab = ShellTab.Workout,
        label = "Workout",
        iconRes = R.drawable.ic_nav_workout_outlined
    ),
    BottomNavTab(
        tab = ShellTab.Progress,
        label = "Progress",
        iconRes = R.drawable.ic_nav_progress_outlined
    ),
)

// ─────────────────────────────────────────────────────────────────────────────
// LAYOUT CONSTANTS — Now derived from TnyxTheme.components.navigation
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun navigationTokens() = TnyxTheme.components.navigation

// ─────────────────────────────────────────────────────────────────────────────
// COMPOSABLES
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun MainBottomNav(
    selectedTab: ShellTab,
    onTabSelected: (ShellTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val tokens = navigationTokens()
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
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            NAV_TABS.forEach { navTab ->
                if (navTab.isSpecial) {
                    // Special tab — AI circular design
                    AiNavTabItem(
                        isSelected = selectedTab == navTab.tab,
                        onClick = { onTabSelected(navTab.tab) }
                    )
                } else {
                    // Regular tab — icon + label
                    NavIcon(
                        icon = painterResource(id = navTab.iconRes!!),
                        label = navTab.label,
                        isSelected = selectedTab == navTab.tab,
                        onClick = { onTabSelected(navTab.tab) }
                    )
                }
            }
        }
    }
}

/**
 * Regular tab item — icon (painter/vector) + label नीचे।
 */
@Composable
private fun RowScope.NavIcon(
    icon: Any,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    iconModifier: Modifier = Modifier
) {
    val tokens = navigationTokens()
    val color = if (isSelected) TnyxTheme.colors.textPrimary else TnyxTheme.colors.textSecondary

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
        val baseIconModifier = Modifier
            .size(tokens.bottomNavIconSize)
            .then(iconModifier)

        Box(
            modifier = Modifier
                .size(tokens.bottomNavRippleSize)
                .clip(CircleShape)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            when (icon) {
                is ImageVector -> Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = color,
                    modifier = baseIconModifier
                )
                is Painter -> Icon(
                    painter = icon,
                    contentDescription = label,
                    tint = color,
                    modifier = baseIconModifier
                )
            }
        }

        Text(
            text = label,
            style = TnyxTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Normal
        )
    }
}

/**
 * AI special tab item — circular branded button, no label।
 */
@Composable
private fun RowScope.AiNavTabItem(
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val tokens = navigationTokens()
    Box(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .clickable(
                onClick = onClick,
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(tokens.bottomNavAiIconSize)
                .clip(CircleShape)
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            AiTabIcon(isSelected = isSelected)
        }
    }
}
