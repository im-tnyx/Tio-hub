package com.tnyx.features.profile.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Edit
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.tnyx.core.theme.TnyxTheme
import com.tnyx.core.ui.components.avatar.TnyxAvatarSize
import com.tnyx.core.ui.components.avatar.TnyxUserAvatar
import com.tnyx.core.ui.components.layouts.TnyxScreenHeader

import com.tnyx.features.profile.presentation.widgets.ImageSourceBottomSheet

@Composable
fun ProfileHomeScreen(
    uiState: ProfileHomeUiState,
    showBackButton: Boolean,
    onAction: (ProfileHomeAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val usernameLabel = "@${uiState.username.ifBlank { "username" }}"
    val headerHeight = TnyxTheme.components.header.height

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(TnyxTheme.colors.background),
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = TnyxTheme.dimens.SpaceXXL),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            item {
                Spacer(modifier = Modifier.statusBarsPadding())
                Spacer(modifier = Modifier.height(headerHeight))
            }

            item {
                ProfileIdentity(
                    state = uiState,
                    usernameLabel = usernameLabel,
                    onEditProfile = { onAction(ProfileHomeAction.EditProfileClicked) },
                    onAvatarClick = { onAction(ProfileHomeAction.AvatarClicked) },
                    onEditPhotoClick = { onAction(ProfileHomeAction.ChangePhotoClicked) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = TnyxTheme.dimens.SpaceL),
                )
            }
        }

        TnyxScreenHeader(
            title = usernameLabel,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding(),
            navigationIcon = if (showBackButton) {
                Icons.AutoMirrored.Rounded.ArrowBack
            } else {
                null
            },
            onNavigationClick = if (showBackButton) {
                { onAction(ProfileHomeAction.BackClicked) }
            } else {
                null
            },
            uppercaseTitle = false,
            reserveNavigationSpace = false,
            actions = {
                IconButton(onClick = { onAction(ProfileHomeAction.EditProfileClicked) }) {
                    Icon(
                        imageVector = Icons.Rounded.Edit,
                        contentDescription = "Edit profile",
                        tint = TnyxTheme.components.header.contentColor,
                    )
                }
                IconButton(onClick = { onAction(ProfileHomeAction.SettingsClicked) }) {
                    Icon(
                        imageVector = Icons.Rounded.Settings,
                        contentDescription = "Settings",
                        tint = TnyxTheme.components.header.contentColor,
                    )
                }
            },
        )

        ImageSourceBottomSheet(
            visible = uiState.isBottomSheetVisible,
            onDismissRequest = { onAction(ProfileHomeAction.DismissBottomSheet) },
            onCameraClick = { onAction(ProfileHomeAction.CameraClicked) },
            onGalleryClick = { onAction(ProfileHomeAction.GalleryClicked) },
            title = "Change Profile Photo",
        )
    }
}

@Composable
private fun ProfileIdentity(
    state: ProfileHomeUiState,
    usernameLabel: String,
    onEditProfile: () -> Unit,
    onAvatarClick: () -> Unit,
    onEditPhotoClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(TnyxTheme.dimens.SpaceL))

        TnyxUserAvatar(
            imageUrl = state.avatarUrl,
            displayName = state.displayName,
            membershipTier = state.membershipTier,
            size = TnyxAvatarSize.Large,
            showEditBadge = true,
            onClick = onAvatarClick,
            onEditClick = onEditPhotoClick,
        )

        Spacer(modifier = Modifier.height(TnyxTheme.dimens.SpaceSM))

        Text(
            text = state.displayName.ifBlank { "Your profile" },
            style = TnyxTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = TnyxTheme.colors.textPrimary,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(TnyxTheme.dimens.SpaceXS))

        Text(
            text = usernameLabel,
            style = TnyxTheme.typography.bodyMedium,
            color = TnyxTheme.colors.textSecondary,
            textAlign = TextAlign.Center,
        )

        if (state.status.isNotBlank()) {
            Spacer(modifier = Modifier.height(TnyxTheme.dimens.SpaceS))
            Text(
                text = state.status,
                style = TnyxTheme.typography.bodyMedium,
                color = TnyxTheme.colors.textSecondary,
                textAlign = TextAlign.Center,
            )
        }

        if (state.planLabel.isNotBlank()) {
            Spacer(modifier = Modifier.height(TnyxTheme.dimens.SpaceSM))
            Row(
                modifier = Modifier
                    .background(
                        color = TnyxTheme.colors.surfaceVariant,
                        shape = CircleShape,
                    )
                    .padding(
                        horizontal = TnyxTheme.dimens.SpaceSM,
                        vertical = TnyxTheme.dimens.SpaceXS,
                    ),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Rounded.Star,
                    contentDescription = null,
                    tint = TnyxTheme.colors.warning,
                    modifier = Modifier.size(TnyxTheme.dimens.IconXXS),
                )
                Spacer(modifier = Modifier.width(TnyxTheme.dimens.SpaceXS))
                Text(
                    text = state.planLabel.uppercase(),
                    style = TnyxTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = TnyxTheme.colors.warning,
                )
            }
        }

        Spacer(modifier = Modifier.height(TnyxTheme.dimens.SpaceL))

        HorizontalDivider(
            thickness = TnyxTheme.dimens.BorderSubtle,
            color = TnyxTheme.colors.surfaceVariant,
        )

        Spacer(modifier = Modifier.height(TnyxTheme.dimens.SpaceM))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ProfileMetric(
                icon = Icons.Rounded.MonitorWeight,
                label = "WEIGHT",
                value = if (state.weight > 0.0) "${state.weight} kg" else "--",
                modifier = Modifier.weight(1f),
            )
            ProfileMetric(
                icon = Icons.Rounded.Straighten,
                label = "HEIGHT",
                value = if (state.height > 0) "${state.height} cm" else "--",
                modifier = Modifier.weight(1f),
            )
            ProfileMetric(
                icon = Icons.Rounded.Whatshot,
                label = "BMR",
                value = if (state.bmr > 0) "${state.bmr} kcal" else "--",
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun ProfileMetric(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = TnyxTheme.colors.textPrimary,
            modifier = Modifier.size(TnyxTheme.dimens.IconS),
        )
        Spacer(modifier = Modifier.height(TnyxTheme.dimens.SpaceS))
        Text(
            text = label,
            style = TnyxTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = TnyxTheme.colors.textMuted,
        )
        Spacer(modifier = Modifier.height(TnyxTheme.dimens.SpaceXXS))
        Text(
            text = value,
            style = TnyxTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = TnyxTheme.colors.textPrimary,
            textAlign = TextAlign.Center,
        )
    }
}
