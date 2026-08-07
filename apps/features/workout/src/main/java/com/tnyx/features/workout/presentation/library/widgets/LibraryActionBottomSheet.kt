package com.tnyx.features.workout.presentation.library.widgets

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tnyx.core.theme.TnyxTheme
import com.tnyx.core.theme.tokens.foundation.TnyxDimens
import com.tnyx.core.ui.components.sheets.TnyxModalBottomSheet

/**
 * Creation options bottom sheet triggered by (+) icon in Exercise Library screen.
 * Refactored for edge-to-edge row selection, zero extra bottom padding, and TnyxDimens tokens.
 */
@Composable
fun LibraryActionBottomSheet(
    visible: Boolean,
    onDismissRequest: () -> Unit,
    onCreateProgramClick: () -> Unit,
    onCreateRoutineClick: () -> Unit,
    onCreateExerciseClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TnyxModalBottomSheet(
        visible = visible,
        onDismissRequest = onDismissRequest,
        showDivider = false,
        contentHorizontalPadding = 0.dp,
        contentBottomPadding = TnyxDimens.SpaceXS,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = TnyxDimens.SpaceXXS),
            verticalArrangement = Arrangement.spacedBy(TnyxDimens.SpaceXXS)
        ) {
            // Program Option
            LibraryActionRowItem(
                icon = Icons.Outlined.Folder,
                title = "Program",
                subtitle = "Create a program with your routines",
                onClick = {
                    onDismissRequest()
                    onCreateProgramClick()
                }
            )

            // Routine Option
            LibraryActionRowItem(
                icon = Icons.AutoMirrored.Outlined.Assignment,
                title = "Routine",
                subtitle = "Create a reusable workout routine",
                onClick = {
                    onDismissRequest()
                    onCreateRoutineClick()
                }
            )

            // Exercise Option
            LibraryActionRowItem(
                icon = Icons.Outlined.FitnessCenter,
                title = "Exercise",
                subtitle = "Create a custom exercise",
                onClick = {
                    onDismissRequest()
                    onCreateExerciseClick()
                }
            )
        }
    }
}

@Composable
private fun LibraryActionRowItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = TnyxDimens.SpaceSM, horizontal = TnyxDimens.SpaceM),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Circular Dark Container for Icon
        Surface(
            shape = CircleShape,
            color = TnyxTheme.colors.surfaceVariant,
            modifier = Modifier.size(48.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = TnyxTheme.colors.textPrimary,
                    modifier = Modifier.size(TnyxDimens.IconM)
                )
            }
        }

        Spacer(modifier = Modifier.width(TnyxDimens.SpaceM))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = TnyxTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = TnyxTheme.colors.textPrimary,
            )
            Spacer(modifier = Modifier.height(TnyxDimens.SpaceXXS))
            Text(
                text = subtitle,
                style = TnyxTheme.typography.bodyMedium,
                color = TnyxTheme.colors.textSecondary,
            )
        }
    }
}
