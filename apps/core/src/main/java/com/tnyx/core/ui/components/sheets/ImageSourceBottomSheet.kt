package com.tnyx.core.ui.components.sheets

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.tnyx.core.theme.TnyxTheme
import com.tnyx.core.ui.components.buttons.TnyxPrimaryButton
import com.tnyx.core.ui.components.buttons.TnyxSecondaryButton

@Composable
fun ImageSourceBottomSheet(
    visible: Boolean,
    onDismissRequest: () -> Unit,
    onCameraClick: () -> Unit,
    onGalleryClick: () -> Unit,
    onRemoveClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    title: String = "Select Photo",
) {
    TnyxModalBottomSheet(
        visible = visible,
        onDismissRequest = onDismissRequest,
        title = title,
        modifier = modifier,
    ) {
        TnyxPrimaryButton(
            text = "Camera",
            onPressed = onCameraClick,
            expand = true,
            size = com.tnyx.core.ui.components.buttons.TnyxButtonSize.Compact,
            leading = {
                Icon(
                    imageVector = Icons.Rounded.PhotoCamera,
                    contentDescription = null,
                    modifier = Modifier.size(TnyxTheme.dimens.IconM),
                )
            },
        )

        Spacer(modifier = Modifier.height(TnyxTheme.dimens.SpaceM))

        TnyxSecondaryButton(
            text = "Gallery",
            onPressed = onGalleryClick,
            expand = true,
            size = com.tnyx.core.ui.components.buttons.TnyxButtonSize.Compact,
            leading = {
                Icon(
                    imageVector = Icons.Rounded.PhotoLibrary,
                    contentDescription = null,
                    modifier = Modifier.size(TnyxTheme.dimens.IconM),
                )
            },
        )

        if (onRemoveClick != null) {
            Spacer(modifier = Modifier.height(TnyxTheme.dimens.SpaceM))

            TnyxSecondaryButton(
                text = "Remove Photo",
                onPressed = onRemoveClick,
                expand = true,
                size = com.tnyx.core.ui.components.buttons.TnyxButtonSize.Compact,
                variant = com.tnyx.core.ui.components.buttons.TnyxSecondaryVariant.Destructive,
                leading = {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = null,
                        tint = TnyxTheme.colors.error,
                        modifier = Modifier.size(TnyxTheme.dimens.IconM),
                    )
                },
            )
        }
    }
}
