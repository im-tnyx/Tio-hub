package com.tnyx.core.ui.components.image

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.tnyx.core.theme.TnyxTheme
import com.tnyx.core.ui.components.buttons.TnyxButtonSize
import com.tnyx.core.ui.components.buttons.TnyxGhostButton
import com.tnyx.core.ui.components.buttons.TnyxSecondaryButton
import com.tnyx.core.ui.components.buttons.TnyxSecondaryVariant
import com.tnyx.core.ui.components.cards.TnyxCard
import com.tnyx.core.ui.components.cards.TnyxCardVariant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ImageCropperDiscardDialog(
    onKeepEditing: () -> Unit,
    onDiscard: () -> Unit,
) {
    val dimens = TnyxTheme.dimens

    BasicAlertDialog(onDismissRequest = onKeepEditing) {
        TnyxCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = dimens.SpaceL),
            variant = TnyxCardVariant.Elevated,
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Discard image?",
                    style = TnyxTheme.typography.titleMedium,
                    color = TnyxTheme.colors.textPrimary,
                )
                Spacer(modifier = Modifier.height(dimens.SpaceS))
                Text(
                    text = "Your crop and transform changes will be lost.",
                    style = TnyxTheme.typography.bodyMedium,
                    color = TnyxTheme.colors.textSecondary,
                )
                Spacer(modifier = Modifier.height(dimens.SpaceL))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TnyxGhostButton(
                        text = "Keep editing",
                        onPressed = onKeepEditing,
                        size = TnyxButtonSize.Compact,
                    )
                    TnyxSecondaryButton(
                        text = "Discard",
                        onPressed = onDiscard,
                        variant = TnyxSecondaryVariant.Destructive,
                        size = TnyxButtonSize.Compact,
                    )
                }
            }
        }
    }
}
