package com.tnyx.core.ui.shell.presentation.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tnyx.core.theme.TnyxTheme
import com.tnyx.core.ui.shell.presentation.action.ShellAction
import com.tnyx.core.ui.shell.presentation.state.ShellPlanTier

@Composable
fun MainTopBar(
    planTier: ShellPlanTier,
    scrollOpacity: Float,
    onAction: (ShellAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = TnyxTheme.colors.surface.copy(alpha = scrollOpacity * 0.95f)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .statusBarsPadding()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = TnyxTheme.dimens.SpaceM),
            contentAlignment = Alignment.Center
        ) {
            // --- Left: TNYX Logo (Refined Typography) ---
            Text(
                text = "TNYX",
                style = TnyxTheme.typography.titleLarge.copy(
                    letterSpacing = 2.sp,
                    lineHeight = 24.sp
                ),
                fontWeight = FontWeight.Black,
                color = TnyxTheme.colors.textPrimary,
                modifier = Modifier.align(Alignment.CenterStart)
            )

            // --- Center: Plan Badge (Sleeker & More Compact) ---
            PlanBadge(
                tier = planTier,
                onClick = { onAction(ShellAction.PremiumClicked) },
                modifier = Modifier.align(Alignment.Center)
            )

            // --- Right: Icons (Streak & Profile) ---
            Row(
                modifier = Modifier.align(Alignment.CenterEnd),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(TnyxTheme.dimens.SpaceS)
            ) {
                TopBarCircularItem(
                    icon = Icons.Default.LocalFireDepartment,
                    tint = TnyxTheme.colors.warning,
                    onClick = { onAction(ShellAction.StreakClicked) }
                )
                TopBarCircularItem(
                    icon = Icons.Default.PersonOutline,
                    tint = TnyxTheme.colors.textPrimary,
                    onClick = { onAction(ShellAction.ProfileClicked) }
                )
            }
        }
        
        // --- Bottom Edge Line (Subtle Fade) ---
        HorizontalDivider(
            thickness = 0.5.dp,
            color = TnyxTheme.colors.textPrimary.copy(alpha = 0.06f * scrollOpacity.coerceAtLeast(0.4f))
        )
    }
}

@Composable
private fun PlanBadge(
    tier: ShellPlanTier,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val visuals = when (tier) {
        ShellPlanTier.Free -> PremiumVisuals(
            label = "GET PREMIUM",
            icon = Icons.Default.Star,
            accentColor = TnyxTheme.colors.onPrimary,
            backgroundColor = TnyxTheme.colors.primary
        )
        ShellPlanTier.Plus -> PremiumVisuals(
            label = "PLUS",
            icon = Icons.Default.Bolt,
            accentColor = TnyxTheme.colors.success,
            backgroundColor = TnyxTheme.colors.success.copy(alpha = 0.15f)
        )
        ShellPlanTier.Premium -> PremiumVisuals(
            label = "PREMIUM",
            icon = Icons.Default.WorkspacePremium,
            accentColor = TnyxTheme.colors.info,
            backgroundColor = TnyxTheme.colors.info.copy(alpha = 0.15f)
        )
    }

    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(visuals.backgroundColor)
            .border(0.5.dp, visuals.accentColor.copy(alpha = 0.2f), CircleShape)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = visuals.icon,
            contentDescription = null,
            tint = visuals.accentColor,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = visuals.label,
            style = TnyxTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                letterSpacing = 0.5.sp
            ),
            fontWeight = FontWeight.ExtraBold,
            color = visuals.accentColor
        )
    }
}

@Composable
private fun TopBarCircularItem(
    icon: ImageVector,
    tint: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .background(TnyxTheme.colors.surfaceVariant.copy(alpha = 0.4f), CircleShape)
            .border(0.5.dp, TnyxTheme.colors.textPrimary.copy(alpha = 0.08f), CircleShape)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(18.dp)
        )
    }
}

private data class PremiumVisuals(
    val label: String,
    val icon: ImageVector,
    val accentColor: Color,
    val backgroundColor: Color
)
