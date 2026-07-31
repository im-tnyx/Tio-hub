package com.tnyx.features.profile.presentation.avatar_viewer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import com.tnyx.core.theme.TnyxTheme
import com.tnyx.core.ui.components.layouts.TnyxScreenHeader
import com.tnyx.core.ui.components.sheets.TnyxModalBottomSheet

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
                        .fillMaxWidth(0.85f)
                        .aspectRatio(1f)
                        .clip(CircleShape)
                        .background(TnyxTheme.colors.surfaceVariant),
                    contentAlignment = Alignment.Center,
                ) {
                    val avatarUrl = uiState.avatarUrl?.takeIf(String::isNotBlank)
                    if (avatarUrl != null) {
                        SubcomposeAsyncImage(
                            model = avatarUrl,
                            contentDescription = "Profile photo",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                            loading = { AvatarLargeFallback() },
                            error = { AvatarLargeFallback() },
                            success = { SubcomposeAsyncImageContent() },
                        )
                    } else {
                        AvatarLargeFallback()
                    }
                }
            }
        }

        TnyxModalBottomSheet(
            visible = uiState.isBottomSheetVisible,
            onDismissRequest = { onAction(AvatarViewerAction.DismissBottomSheet) },
            title = "Change Profile Photo",
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onAction(AvatarViewerAction.CameraClicked) }
                    .padding(vertical = TnyxTheme.dimens.SpaceM),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Rounded.PhotoCamera,
                    contentDescription = "Camera",
                    tint = TnyxTheme.colors.textPrimary,
                    modifier = Modifier.size(TnyxTheme.dimens.IconM),
                )
                Spacer(modifier = Modifier.width(TnyxTheme.dimens.SpaceM))
                Text(
                    text = "Camera",
                    style = TnyxTheme.typography.bodyLarge,
                    color = TnyxTheme.colors.textPrimary,
                    fontWeight = FontWeight.Medium,
                )
            }

            HorizontalDivider(
                color = TnyxTheme.colors.surfaceVariant,
                thickness = TnyxTheme.dimens.BorderThin,
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onAction(AvatarViewerAction.GalleryClicked) }
                    .padding(vertical = TnyxTheme.dimens.SpaceM),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Rounded.PhotoLibrary,
                    contentDescription = "Gallery",
                    tint = TnyxTheme.colors.textPrimary,
                    modifier = Modifier.size(TnyxTheme.dimens.IconM),
                )
                Spacer(modifier = Modifier.width(TnyxTheme.dimens.SpaceM))
                Text(
                    text = "Gallery",
                    style = TnyxTheme.typography.bodyLarge,
                    color = TnyxTheme.colors.textPrimary,
                    fontWeight = FontWeight.Medium,
                )
            }

            Spacer(modifier = Modifier.height(TnyxTheme.dimens.SpaceS))
        }
    }
}

@Composable
private fun AvatarLargeFallback() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Rounded.Person,
            contentDescription = null,
            tint = TnyxTheme.colors.textMuted,
            modifier = Modifier.size(100.dp),
        )
    }
}
