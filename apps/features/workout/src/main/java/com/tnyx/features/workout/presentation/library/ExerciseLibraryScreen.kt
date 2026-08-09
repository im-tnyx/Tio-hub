package com.tnyx.features.workout.presentation.library

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import com.tnyx.core.theme.TnyxTheme
import com.tnyx.core.theme.tokens.components.TnyxHeaderSize
import com.tnyx.core.ui.components.cards.TnyxCard
import com.tnyx.core.ui.components.cards.TnyxCardVariant
import com.tnyx.core.ui.components.layouts.TnyxScreenHeader
import com.tnyx.features.workout.presentation.library.widgets.LibraryActionBottomSheet

@Composable
fun ExerciseLibraryScreen(
    onSearchClick: () -> Unit = {},
    onCreateProgramClick: () -> Unit = {},
    onCreateRoutineClick: () -> Unit = {},
    onCreateExerciseClick: () -> Unit = {},
    onWorkoutSettingsClick: () -> Unit = {},
    onAppSettingsClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    var showActionBottomSheet by remember { mutableStateOf(false) }
    var showOptionsMenu by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TnyxTheme.colors.background)
            .statusBarsPadding()
    ) {
        TnyxScreenHeader(
            title = "Library",
            size = TnyxHeaderSize.Compact,
            uppercaseTitle = false,
            actions = {
                IconButton(onClick = onSearchClick) {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = "Search exercises",
                        tint = TnyxTheme.colors.textPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                IconButton(onClick = { showActionBottomSheet = true }) {
                    Icon(
                        imageVector = Icons.Outlined.Add,
                        contentDescription = "Open creation menu",
                        tint = TnyxTheme.colors.textPrimary,
                        modifier = Modifier.size(26.dp)
                    )
                }
                Box {
                    IconButton(onClick = { showOptionsMenu = true }) {
                        Icon(
                            imageVector = Icons.Rounded.MoreVert,
                            contentDescription = "More options",
                            tint = TnyxTheme.colors.textPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    if (showOptionsMenu) {
                        androidx.compose.ui.window.Popup(
                            alignment = Alignment.TopEnd,
                            offset = IntOffset(x = -12, y = 44),
                            onDismissRequest = { showOptionsMenu = false }
                        ) {
                            TnyxCard(
                                variant = TnyxCardVariant.Surface,
                                padding = 0.dp,
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .width(210.dp)
                                        .padding(vertical = 4.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(48.dp)
                                            .clickable {
                                                showOptionsMenu = false
                                                onWorkoutSettingsClick()
                                            }
                                            .padding(horizontal = 16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.FitnessCenter,
                                            contentDescription = null,
                                            tint = TnyxTheme.colors.textPrimary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        androidx.compose.material3.Text(
                                            text = "Workout Settings",
                                            style = TnyxTheme.typography.bodyMedium,
                                            color = TnyxTheme.colors.textPrimary
                                        )
                                    }

                                    HorizontalDivider(
                                        color = TnyxTheme.colors.textSecondary.copy(alpha = 0.15f),
                                        thickness = 0.5.dp
                                    )

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(48.dp)
                                            .clickable {
                                                showOptionsMenu = false
                                                onAppSettingsClick()
                                            }
                                            .padding(horizontal = 16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Settings,
                                            contentDescription = null,
                                            tint = TnyxTheme.colors.textPrimary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        androidx.compose.material3.Text(
                                            text = "App Settings",
                                            style = TnyxTheme.typography.bodyMedium,
                                            color = TnyxTheme.colors.textPrimary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        )

        // Library content container
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(TnyxTheme.colors.background)
        )
    }

    // Creation Menu Bottom Sheet (+ Icon Trigger)
    LibraryActionBottomSheet(
        visible = showActionBottomSheet,
        onDismissRequest = { showActionBottomSheet = false },
        onCreateProgramClick = onCreateProgramClick,
        onCreateRoutineClick = onCreateRoutineClick,
        onCreateExerciseClick = onCreateExerciseClick
    )
}
