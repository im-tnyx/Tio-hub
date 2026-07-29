package com.tnyx.features.profile.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.MonitorWeight
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Straighten
import androidx.compose.material.icons.rounded.Whatshot
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tnyx.core.theme.TnyxTheme
import com.tnyx.core.ui.components.avatar.TnyxAvatarSize
import com.tnyx.core.ui.components.avatar.TnyxUserAvatar
import com.tnyx.core.ui.components.cards.TnyxCard
import com.tnyx.core.ui.components.cards.TnyxCardVariant

@Composable
fun ProfileHomeScreen(
    uiState: ProfileHomeUiState,
    onAction: (ProfileHomeAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberLazyListState()
    val headerHeight = 56.dp
    val showUsernameInHeader by remember {
        derivedStateOf {
            scrollState.firstVisibleItemIndex > 1 ||
                (scrollState.firstVisibleItemIndex == 1 && scrollState.firstVisibleItemScrollOffset > 100)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(TnyxTheme.colors.background),
    ) {
        LazyColumn(
            state = scrollState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = 16.dp,
                bottom = 48.dp,
            ),
        ) {
            item {
                Spacer(modifier = Modifier.statusBarsPadding())
                Spacer(modifier = Modifier.height(headerHeight))
            }

            item {
                Box(modifier = Modifier.padding(horizontal = TnyxTheme.dimens.SpaceS)) {
                    UserProfileCard(
                        state = uiState,
                        onEditPhoto = { /* TODO: Wire profile photo editing. */ },
                        onClick = { /* TODO: Wire personal information. */ },
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(TnyxTheme.colors.surfaceRaised)
                .statusBarsPadding(),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(headerHeight)
                    .padding(horizontal = TnyxTheme.dimens.SpaceS),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(
                    onClick = { onAction(ProfileHomeAction.BackClicked) },
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Back",
                        tint = TnyxTheme.colors.textPrimary,
                    )
                }
                Text(
                    text = if (showUsernameInHeader) uiState.displayName else "Profile",
                    style = TnyxTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                    ),
                    color = TnyxTheme.colors.textPrimary,
                    modifier = Modifier
                        .padding(start = 16.dp)
                        .weight(1f),
                )
                IconButton(
                    onClick = { onAction(ProfileHomeAction.SettingsClicked) },
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Settings,
                        contentDescription = "Settings",
                        tint = TnyxTheme.colors.textPrimary,
                    )
                }
            }
        }
    }
}

@Composable
private fun UserProfileCard(
    state: ProfileHomeUiState,
    onEditPhoto: () -> Unit,
    onClick: () -> Unit,
) {
    TnyxCard(
        variant = TnyxCardVariant.Normal,
        padding = 0.dp,
        onClick = onClick,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TnyxUserAvatar(
                    imageUrl = state.avatarUrl,
                    displayName = state.displayName,
                    membershipTier = state.membershipTier,
                    size = TnyxAvatarSize.Large,
                    onClick = onClick,
                    showEditBadge = true,
                    onEditClick = onEditPhoto,
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = state.displayName,
                            style = TnyxTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TnyxTheme.colors.textPrimary,
                        )
                        if (state.planLabel.isNotEmpty()) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Rounded.CheckCircle,
                                contentDescription = "Verified Premium",
                                tint = Color(0xFF1DA1F2),
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = state.status,
                        style = TnyxTheme.typography.bodyMedium,
                        color = TnyxTheme.colors.textSecondary,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .border(
                                width = 0.5.dp,
                                color = TnyxTheme.colors.warning.copy(alpha = 0.4f),
                                shape = CircleShape,
                            )
                            .background(TnyxTheme.colors.warning.copy(alpha = 0.1f))
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.Star,
                                contentDescription = null,
                                tint = TnyxTheme.colors.warning,
                                modifier = Modifier.size(12.dp),
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = state.planLabel.uppercase(),
                                style = TnyxTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    letterSpacing = 0.5.sp,
                                ),
                                color = TnyxTheme.colors.warning,
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier
                        .align(Alignment.Top)
                        .padding(start = 8.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "BMI: ",
                            style = TnyxTheme.typography.bodySmall,
                            color = TnyxTheme.colors.textSecondary,
                        )
                        Text(
                            text = if (state.bmi > 0.0) state.bmi.toString() else "-",
                            style = TnyxTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = TnyxTheme.colors.textPrimary,
                        )
                    }

                    val (bmiStatus, bmiColor) = remember(state.bmi) {
                        when {
                            state.bmi <= 0.0 -> Pair("", Color.Transparent)
                            state.bmi < 18.5 -> Pair("Underweight", Color(0xFF03A9F4))
                            state.bmi < 25.0 -> Pair("Healthy", Color(0xFF4CAF50))
                            state.bmi < 30.0 -> Pair("Overweight", Color(0xFFFF9800))
                            else -> Pair("Obese", Color(0xFFF44336))
                        }
                    }

                    if (bmiStatus.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = bmiStatus,
                            style = TnyxTheme.typography.labelSmall.copy(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                            ),
                            color = bmiColor,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            HorizontalDivider(
                thickness = 0.5.dp,
                color = TnyxTheme.colors.textPrimary.copy(alpha = 0.2f),
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ProfileMetric(
                    icon = {
                        Icon(
                            imageVector = Icons.Rounded.MonitorWeight,
                            contentDescription = null,
                            tint = TnyxTheme.colors.textPrimary,
                            modifier = Modifier.size(20.dp),
                        )
                    },
                    label = "WEIGHT",
                    value = if (state.weight > 0.0) "${state.weight} kg" else "--",
                    modifier = Modifier.weight(1f),
                )
                ProfileMetric(
                    icon = {
                        Icon(
                            imageVector = Icons.Rounded.Straighten,
                            contentDescription = null,
                            tint = TnyxTheme.colors.textPrimary,
                            modifier = Modifier.size(20.dp),
                        )
                    },
                    label = "HEIGHT",
                    value = if (state.height > 0) "${state.height} cm" else "--",
                    modifier = Modifier.weight(1f),
                )
                ProfileMetric(
                    icon = {
                        Icon(
                            imageVector = Icons.Rounded.Whatshot,
                            contentDescription = null,
                            tint = TnyxTheme.colors.textPrimary,
                            modifier = Modifier.size(20.dp),
                        )
                    },
                    label = "BMR",
                    value = if (state.bmr > 0) "${state.bmr} kcal" else "--",
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun ProfileMetric(
    icon: @Composable () -> Unit,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
    ) {
        icon()
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = label,
                style = TnyxTheme.typography.labelSmall.copy(
                    fontSize = 9.sp,
                    fontWeight = FontWeight.SemiBold,
                ),
                color = TnyxTheme.colors.textMuted,
            )
            Text(
                text = value,
                style = TnyxTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = TnyxTheme.colors.textPrimary,
            )
        }
    }
}
