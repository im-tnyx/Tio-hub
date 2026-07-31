package com.tnyx.features.profile.presentation.avatar_viewer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.tnyx.core.theme.TnyxTheme
import com.tnyx.core.ui.components.avatar.TnyxAvatarSize
import com.tnyx.core.ui.components.buttons.TnyxPrimaryButton
import com.tnyx.core.ui.components.buttons.TnyxSecondaryButton
import com.tnyx.core.ui.components.layouts.TnyxScreenHeader
import com.tnyx.core.ui.components.sheets.TnyxModalBottomSheet
import com.tnyx.features.profile.presentation.widgets.ImageSourceBottomSheet

@Composable
fun AvatarViewerScreen(
    uiState: AvatarViewerUiState,
    onAction: (AvatarViewerAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(TnyxTheme.colors.background),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            TnyxScreenHeader(
                title = "",
                modifier = Modifier.statusBarsPadding(),
                navigationIcon = Icons.AutoMirrored.Rounded.ArrowBack,
                onNavigationClick = { onAction(AvatarViewerAction.BackClicked) },
                uppercaseTitle = false,
                reserveNavigationSpace = false,
                actions = {
                    IconButton(onClick = { onAction(AvatarViewerAction.DeleteClicked) }) {
                        Icon(
                            imageVector = Icons.Rounded.Delete,
                            contentDescription = "Delete photo",
                            tint = TnyxTheme.components.header.contentColor,
                        )
                    }
                    IconButton(onClick = { onAction(AvatarViewerAction.EditClicked) }) {
                        Icon(
                            imageVector = Icons.Rounded.Edit,
                            contentDescription = "Edit photo",
                            tint = TnyxTheme.components.header.contentColor,
                        )
                    }
                    IconButton(onClick = { onAction(AvatarViewerAction.DownloadClicked) }) {
                        Icon(
                            imageVector = Icons.Rounded.Download,
                            contentDescription = "Download photo",
                            tint = TnyxTheme.components.header.contentColor,
                        )
                    }
                },
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .background(TnyxTheme.colors.surfaceVariant),
                    contentAlignment = Alignment.Center,
                ) {
                    val avatarUrl = uiState.avatarUrl?.takeIf(String::isNotBlank)
                    if (avatarUrl != null) {
                        AsyncImage(
                            model = avatarUrl,
                            contentDescription = "Profile photo",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                        )
                    } else {
                        AvatarFallback()
                    }
                }
            }
        }

        // Edit Photo Bottom Sheet
        ImageSourceBottomSheet(
            visible = uiState.isEditSheetVisible,
            onDismissRequest = { onAction(AvatarViewerAction.DismissEditSheet) },
            onCameraClick = { onAction(AvatarViewerAction.CameraClicked) },
            onGalleryClick = { onAction(AvatarViewerAction.GalleryClicked) },
            title = "Change Profile Photo",
        )

        // Delete Photo Confirmation Bottom Sheet
        TnyxModalBottomSheet(
            visible = uiState.isDeleteSheetVisible,
            onDismissRequest = { onAction(AvatarViewerAction.DismissDeleteSheet) },
            title = "Remove profile photo",
        ) {
            Text(
                text = "Are you sure you want to remove your profile photo?",
                style = TnyxTheme.typography.bodyMedium,
                color = TnyxTheme.colors.textSecondary,
            )

            Spacer(modifier = Modifier.height(TnyxTheme.dimens.SpaceL))

            TnyxPrimaryButton(
                text = "Remove",
                onPressed = { onAction(AvatarViewerAction.ConfirmDeleteClicked) },
                expand = true,
                enabled = !uiState.isDeleting,
                leading = {
                    Icon(
                        imageVector = Icons.Rounded.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(TnyxTheme.dimens.IconM),
                    )
                },
            )

            Spacer(modifier = Modifier.height(TnyxTheme.dimens.SpaceM))

            TnyxSecondaryButton(
                text = "Cancel",
                onPressed = { onAction(AvatarViewerAction.DismissDeleteSheet) },
                expand = true,
                leading = {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = null,
                        modifier = Modifier.size(TnyxTheme.dimens.IconM),
                    )
                },
            )

            Spacer(modifier = Modifier.height(TnyxTheme.dimens.SpaceS))
        }
    }
}

@Composable
private fun AvatarFallback() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(TnyxTheme.colors.surfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Rounded.Person,
            contentDescription = null,
            tint = TnyxTheme.colors.textSecondary,
            modifier = Modifier.size(TnyxAvatarSize.XLarge.fallbackIconSize),
        )
    }
}
