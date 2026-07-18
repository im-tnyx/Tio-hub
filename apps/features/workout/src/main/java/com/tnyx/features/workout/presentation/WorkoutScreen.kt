package com.tnyx.features.workout.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.History
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.tnyx.core.theme.TnyxTheme
import com.tnyx.core.ui.components.buttons.TnyxPrimaryButton
import com.tnyx.core.ui.components.buttons.TnyxSecondaryButton
import com.tnyx.core.ui.components.cards.TnyxCard
import com.tnyx.core.ui.components.cards.TnyxCardVariant
import com.tnyx.core.ui.components.layouts.TnyxScreenHeader
import com.tnyx.features.workout.presentation.components.ExerciseEditorMode
import com.tnyx.features.workout.presentation.components.WorkoutExerciseEditor
import com.tnyx.features.workout.presentation.components.WorkoutRpeBottomSheet
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun WorkoutScreen(
    state: WorkoutUiState,
    onAction: (WorkoutAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TnyxTheme.colors.background)
            .statusBarsPadding(),
    ) {
        TnyxScreenHeader(
            title = "Workout",
            actions = {
                IconButton(onClick = { onAction(WorkoutAction.HistoryClicked) }) {
                    Icon(
                        imageVector = Icons.Rounded.History,
                        contentDescription = "Workout history",
                        tint = TnyxTheme.colors.textPrimary,
                    )
                }
            },
        )

        if (state.isLoading) {
            LoadingContent(modifier = Modifier.weight(1f))
        } else {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = TnyxTheme.insets.screenHorizontal)
                    .padding(
                        top = TnyxTheme.insets.screenVertical,
                        bottom = TnyxTheme.insets.bottomNavPadding,
                    ),
                verticalArrangement = Arrangement.spacedBy(TnyxTheme.insets.itemSpacing),
            ) {
                if (state.hasActiveSession) {
                    ActiveWorkoutContent(state = state, onAction = onAction)
                } else {
                    EmptyWorkoutContent(state = state, onAction = onAction)
                }

                state.errorMessage?.let { message ->
                    Text(
                        text = message,
                        style = TnyxTheme.typography.bodyMedium,
                        color = TnyxTheme.colors.error,
                    )
                }
            }
        }
    }

    WorkoutRpeBottomSheet(
        state = state.rpePicker,
        onDismiss = { onAction(WorkoutAction.RpeDismissed) },
        onSelected = { onAction(WorkoutAction.RpeSelected(it)) },
    )
}

@Composable
fun WorkoutHistoryScreen(
    state: WorkoutUiState,
    onAction: (WorkoutAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TnyxTheme.colors.background)
            .statusBarsPadding(),
    ) {
        TnyxScreenHeader(
            title = "History",
            navigationIcon = Icons.AutoMirrored.Rounded.ArrowBack,
            onNavigationClick = { onAction(WorkoutAction.BackClicked) },
        )

        when {
            state.isLoading -> LoadingContent(modifier = Modifier.weight(1f))
            state.history.isEmpty() -> EmptyHistoryContent(modifier = Modifier.weight(1f))
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = TnyxTheme.insets.screenHorizontal,
                    top = TnyxTheme.insets.screenVertical,
                    end = TnyxTheme.insets.screenHorizontal,
                    bottom = TnyxTheme.insets.bottomNavPadding,
                ),
                verticalArrangement = Arrangement.spacedBy(TnyxTheme.insets.itemSpacing),
            ) {
                items(items = state.history, key = WorkoutHistoryItemUi::id) { item ->
                    HistoryCard(item = item)
                }
            }
        }
    }
}

@Composable
private fun EmptyWorkoutContent(
    state: WorkoutUiState,
    onAction: (WorkoutAction) -> Unit,
) {
    TnyxCard(
        modifier = Modifier.fillMaxWidth(),
        variant = TnyxCardVariant.Elevated,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(TnyxTheme.dimens.SpaceM)) {
            Column(verticalArrangement = Arrangement.spacedBy(TnyxTheme.dimens.SpaceS)) {
                Text(
                    text = "Start an offline workout",
                    style = TnyxTheme.typography.titleLarge,
                    color = TnyxTheme.colors.textPrimary,
                )
                Text(
                    text = "Your active session and completed sets are saved on this phone.",
                    style = TnyxTheme.typography.bodyMedium,
                    color = TnyxTheme.colors.textSecondary,
                )
            }
            TnyxPrimaryButton(
                text = "Start blank workout",
                onPressed = { onAction(WorkoutAction.StartBlankWorkoutClicked) },
                enabled = !state.isMutating,
                expand = true,
            )
        }
    }

    if (state.history.isNotEmpty()) {
        TnyxSecondaryButton(
            text = "Open history (${state.history.size})",
            onPressed = { onAction(WorkoutAction.HistoryClicked) },
            expand = true,
        )
    }
}

@Composable
private fun ActiveWorkoutContent(
    state: WorkoutUiState,
    onAction: (WorkoutAction) -> Unit,
) {
    TnyxCard(
        modifier = Modifier.fillMaxWidth(),
        variant = TnyxCardVariant.Outlined,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(TnyxTheme.dimens.SpaceXS)) {
            Text(
                text = "Active workout",
                style = TnyxTheme.typography.titleMedium,
                color = TnyxTheme.colors.textPrimary,
            )
            state.activeStartedAtMs?.let { startedAtMs ->
                Text(
                    text = "Started ${formatTime(startedAtMs)} · Saved offline",
                    style = TnyxTheme.typography.bodySmall,
                    color = TnyxTheme.colors.textSecondary,
                )
            }
        }
    }

    if (state.exercises.isEmpty()) {
        TnyxCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(TnyxTheme.dimens.SpaceM)) {
                Column(verticalArrangement = Arrangement.spacedBy(TnyxTheme.dimens.SpaceS)) {
                    Text(
                        text = "Add your first exercise",
                        style = TnyxTheme.typography.titleMedium,
                        color = TnyxTheme.colors.textPrimary,
                    )
                    Text(
                        text = "This Stage 3 proof uses one first-party starter exercise. The full library comes next.",
                        style = TnyxTheme.typography.bodyMedium,
                        color = TnyxTheme.colors.textSecondary,
                    )
                }
                TnyxPrimaryButton(
                    text = "Add Bodyweight Squat",
                    onPressed = { onAction(WorkoutAction.AddStarterExerciseClicked) },
                    enabled = !state.isMutating,
                    expand = true,
                )
            }
        }
        return
    }

    state.exercises.forEach { exercise ->
        WorkoutExerciseEditor(
            state = exercise,
            mode = ExerciseEditorMode.ACTIVE_WORKOUT,
            enabled = !state.isMutating,
            onExpandedChange = { isExpanded ->
                onAction(
                    WorkoutAction.ExerciseExpandedChanged(
                        exerciseEntryId = exercise.id,
                        isExpanded = isExpanded,
                    ),
                )
            },
            onMetricChange = { setId, field, value ->
                onAction(
                    WorkoutAction.MetricChanged(
                        exerciseEntryId = exercise.id,
                        setId = setId,
                        field = field,
                        value = value,
                    ),
                )
            },
            onPreviousSet = { setId ->
                onAction(
                    WorkoutAction.PreviousSetClicked(
                        exerciseEntryId = exercise.id,
                        setId = setId,
                    ),
                )
            },
            onRpeClick = { setId ->
                onAction(
                    WorkoutAction.RpeClicked(
                        exerciseEntryId = exercise.id,
                        setId = setId,
                    ),
                )
            },
            onCompleteSet = { setId ->
                onAction(
                    WorkoutAction.CompleteSetClicked(
                        exerciseEntryId = exercise.id,
                        setId = setId,
                    ),
                )
            },
            onAddSet = {
                onAction(WorkoutAction.AddSetClicked(exercise.id))
            },
        )
    }

    TnyxPrimaryButton(
        text = "Finish workout",
        onPressed = { onAction(WorkoutAction.FinishWorkoutClicked) },
        enabled = state.hasCompletedSet && !state.isMutating,
        expand = true,
    )
}

@Composable
private fun HistoryCard(item: WorkoutHistoryItemUi) {
    TnyxCard(
        modifier = Modifier.fillMaxWidth(),
        variant = TnyxCardVariant.Outlined,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(TnyxTheme.dimens.SpaceXS)) {
            Text(
                text = item.title,
                style = TnyxTheme.typography.titleMedium,
                color = TnyxTheme.colors.textPrimary,
            )
            Text(
                text = formatHistoryDate(item.endedAtMs),
                style = TnyxTheme.typography.bodySmall,
                color = TnyxTheme.colors.textSecondary,
            )
            Text(
                text = "${item.completedSets} completed set · ${item.totalReps} reps",
                style = TnyxTheme.typography.bodyMedium,
                color = TnyxTheme.colors.textPrimary,
            )
        }
    }
}

@Composable
private fun EmptyHistoryContent(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Text(
            text = "Finished workouts will appear here.",
            style = TnyxTheme.typography.bodyLarge,
            color = TnyxTheme.colors.textSecondary,
            modifier = Modifier.padding(TnyxTheme.insets.screenHorizontal),
        )
    }
}

@Composable
private fun LoadingContent(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = TnyxTheme.colors.primary)
    }
}

private fun formatTime(timestampMs: Long): String = TIME_FORMATTER.format(
    Instant.ofEpochMilli(timestampMs).atZone(ZoneId.systemDefault()),
)

private fun formatHistoryDate(timestampMs: Long): String = HISTORY_FORMATTER.format(
    Instant.ofEpochMilli(timestampMs).atZone(ZoneId.systemDefault()),
)

private val TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("h:mm a")
private val HISTORY_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMM yyyy, h:mm a")
