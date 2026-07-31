package com.tnyx.features.workout.presentation.library.widgets

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import com.tnyx.core.ui.components.sheets.TnyxModalBottomSheet

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
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
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
            .padding(vertical = 8.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Circular Dark Container for Icon
        Surface(
            shape = CircleShape,
            color = TnyxTheme.colors.surfaceVariant,
            modifier = Modifier.size(56.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = TnyxTheme.colors.textPrimary,
                    modifier = Modifier.size(26.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = TnyxTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = TnyxTheme.colors.textPrimary,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = TnyxTheme.typography.bodyMedium,
                color = TnyxTheme.colors.textSecondary,
            )
        }
    }
}
