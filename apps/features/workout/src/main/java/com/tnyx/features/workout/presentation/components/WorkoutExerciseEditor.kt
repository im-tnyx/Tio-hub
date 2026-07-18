package com.tnyx.features.workout.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import com.tnyx.core.theme.TnyxTheme
import com.tnyx.core.ui.components.buttons.TnyxSecondaryButton
import com.tnyx.core.ui.components.buttons.TnyxSecondaryVariant
import com.tnyx.features.workout.presentation.WorkoutExerciseUi
import com.tnyx.features.workout.presentation.WorkoutMetricField
import com.tnyx.features.workout.presentation.WorkoutMetricUi
import com.tnyx.features.workout.presentation.WorkoutSetUi
import com.tnyx.shared.workout.domain.model.ExerciseTrackingType
import com.tnyx.shared.workout.domain.model.SetType

enum class ExerciseEditorMode {
    ROUTINE_EDIT,
    ACTIVE_WORKOUT,
    READ_ONLY,
}

@Composable
fun WorkoutExerciseEditor(
    state: WorkoutExerciseUi,
    mode: ExerciseEditorMode,
    enabled: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onMetricChange: (setId: String?, field: WorkoutMetricField, value: String) -> Unit,
    onPreviousSet: (setId: String?) -> Unit,
    onRpeClick: (setId: String?) -> Unit,
    onCompleteSet: (setId: String?) -> Unit,
    onAddSet: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        ExerciseHeader(state = state, onExpandedChange = onExpandedChange)

        if (!state.isExpanded) return@Column

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = TnyxTheme.dimens.SpaceS,
                    end = TnyxTheme.dimens.SpaceS,
                    top = TnyxTheme.dimens.SpaceS,
                    bottom = TnyxTheme.dimens.SpaceM,
                ),
            horizontalArrangement = Arrangement.spacedBy(TnyxTheme.dimens.SpaceS),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Rounded.Timer,
                contentDescription = null,
                modifier = Modifier.size(TnyxTheme.dimens.IconS),
                tint = TnyxTheme.colors.accent,
            )
            Text(
                text = "Rest: ${state.restSeconds}s",
                style = TnyxTheme.typography.bodyMedium,
                color = TnyxTheme.colors.accent,
            )
        }

        SetTableHeader(state)
        HorizontalDivider(
            color = TnyxTheme.colors.surfaceVariant,
            thickness = TnyxTheme.dimens.BorderThin,
        )
        state.sets.forEachIndexed { index, set ->
            WorkoutSetRow(
                state = set,
                rowIndex = index,
                mode = mode,
                showRpe = state.trackingType.supportsRpe(),
                enabled = enabled,
                onMetricChange = { field, value -> onMetricChange(set.id, field, value) },
                onPreviousSet = { onPreviousSet(set.id) },
                onRpeClick = { onRpeClick(set.id) },
                onComplete = { onCompleteSet(set.id) },
            )
        }

        if (mode != ExerciseEditorMode.READ_ONLY) {
            TnyxSecondaryButton(
                text = "Add set",
                onPressed = onAddSet,
                modifier = Modifier.padding(
                    horizontal = TnyxTheme.dimens.SpaceS,
                    vertical = TnyxTheme.dimens.SpaceM,
                ),
                variant = TnyxSecondaryVariant.Muted,
                enabled = enabled && state.sets.all { it.id != null },
                leading = {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = null,
                        modifier = Modifier.size(TnyxTheme.dimens.IconS),
                    )
                },
                expand = true,
            )
        }
    }
}

@Composable
private fun ExerciseHeader(
    state: WorkoutExerciseUi,
    onExpandedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onExpandedChange(!state.isExpanded) }
            .padding(TnyxTheme.dimens.SpaceS),
        horizontalArrangement = Arrangement.spacedBy(TnyxTheme.dimens.SpaceSM),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            modifier = Modifier.size(TnyxTheme.dimens.IconXL),
            shape = TnyxTheme.shapes.Material.small,
            color = TnyxTheme.colors.surfaceVariant,
            contentColor = TnyxTheme.colors.textSecondary,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Rounded.FitnessCenter,
                    contentDescription = null,
                    modifier = Modifier.size(TnyxTheme.dimens.IconM),
                )
            }
        }
        Text(
            text = state.name,
            modifier = Modifier.weight(1f),
            style = TnyxTheme.typography.titleMedium,
            color = TnyxTheme.colors.accent,
        )
        Icon(
            imageVector = if (state.isExpanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
            contentDescription = if (state.isExpanded) "Collapse exercise" else "Expand exercise",
            modifier = Modifier.size(TnyxTheme.dimens.IconM),
            tint = TnyxTheme.colors.textSecondary,
        )
    }
}

@Composable
private fun SetTableHeader(state: WorkoutExerciseUi) {
    val inputMetrics = state.sets.firstOrNull()?.metrics.orEmpty()
    val hasRpe = state.trackingType.supportsRpe()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = TnyxTheme.dimens.SpaceS),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TableLabel(text = "SET", weight = SET_COLUMN_WEIGHT)
        TableLabel(text = "PREVIOUS", weight = PREVIOUS_COLUMN_WEIGHT)
        inputMetrics.forEach { metric ->
            TableLabel(text = metric.shortLabel(), weight = METRIC_COLUMN_WEIGHT)
        }
        if (hasRpe) TableLabel(text = "RPE", weight = RPE_COLUMN_WEIGHT)
        if (state.sets.isNotEmpty()) TableLabel(text = "✓", weight = DONE_COLUMN_WEIGHT)
    }
}

@Composable
private fun RowScope.TableLabel(text: String, weight: Float) {
    Text(
        text = text,
        modifier = Modifier.weight(weight),
        style = TnyxTheme.typography.labelSmall,
        color = TnyxTheme.colors.textMuted,
        textAlign = TextAlign.Center,
    )
}

@Composable
private fun WorkoutSetRow(
    state: WorkoutSetUi,
    rowIndex: Int,
    mode: ExerciseEditorMode,
    showRpe: Boolean,
    enabled: Boolean,
    onMetricChange: (WorkoutMetricField, String) -> Unit,
    onPreviousSet: () -> Unit,
    onRpeClick: () -> Unit,
    onComplete: () -> Unit,
) {
    val rowColor = when {
        state.isCompleted -> TnyxTheme.colors.surfaceVariant
        rowIndex % 2 != 0 -> TnyxTheme.colors.surfaceRaised
        else -> Color.Transparent
    }
    val inputMetrics = state.metrics
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(rowColor)
            .heightIn(min = TnyxTheme.dimens.InputHeight)
            .padding(horizontal = TnyxTheme.dimens.SpaceS),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = state.setLabel(),
            modifier = Modifier.weight(SET_COLUMN_WEIGHT),
            style = TnyxTheme.typography.titleMedium,
            color = if (state.type == SetType.NORMAL) {
                TnyxTheme.colors.textPrimary
            } else {
                TnyxTheme.colors.warning
            },
            textAlign = TextAlign.Center,
        )
        Text(
            text = state.previousSummary ?: "—",
            modifier = Modifier
                .weight(PREVIOUS_COLUMN_WEIGHT)
                .clickable(
                    enabled = enabled && !state.isCompleted && state.previousSummary != null,
                    onClick = onPreviousSet,
                )
                .padding(vertical = TnyxTheme.dimens.SpaceS),
            style = TnyxTheme.typography.bodySmall,
            color = if (state.previousSummary == null) {
                TnyxTheme.colors.textMuted
            } else {
                TnyxTheme.colors.textSecondary
            },
            textAlign = TextAlign.Center,
            maxLines = 1,
        )
        inputMetrics.forEach { metric ->
            MetricCell(
                metric = metric,
                enabled = enabled && !state.isCompleted && mode != ExerciseEditorMode.READ_ONLY,
                onValueChange = { onMetricChange(metric.field, it) },
                modifier = Modifier.weight(METRIC_COLUMN_WEIGHT),
            )
        }
        if (showRpe) {
            RpeCell(
                value = state.rpeValue,
                enabled = enabled && !state.isCompleted && mode != ExerciseEditorMode.READ_ONLY,
                onClick = onRpeClick,
                modifier = Modifier.weight(RPE_COLUMN_WEIGHT),
            )
        }
        if (mode == ExerciseEditorMode.ACTIVE_WORKOUT) {
            Box(
                modifier = Modifier.weight(DONE_COLUMN_WEIGHT),
                contentAlignment = Alignment.Center,
            ) {
                IconButton(onClick = onComplete, enabled = enabled && !state.isCompleted) {
                    Surface(
                        shape = TnyxTheme.shapes.Material.small,
                        color = if (state.isCompleted) {
                            TnyxTheme.colors.success
                        } else {
                            TnyxTheme.colors.surfaceVariant
                        },
                        contentColor = if (state.isCompleted) {
                            TnyxTheme.colors.onPrimary
                        } else {
                            TnyxTheme.colors.textPrimary
                        },
                    ) {
                        Box(
                            modifier = Modifier.size(TnyxTheme.dimens.IconL),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Check,
                                contentDescription = if (state.isCompleted) {
                                    "Set ${state.setNumber} completed"
                                } else {
                                    "Complete set ${state.setNumber}"
                                },
                                modifier = Modifier.size(TnyxTheme.dimens.IconS),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricCell(
    metric: WorkoutMetricUi,
    enabled: Boolean,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (!enabled) {
        Text(
            text = metric.value.ifBlank { "—" },
            modifier = modifier,
            style = TnyxTheme.typography.titleMedium,
            color = TnyxTheme.colors.textPrimary,
            textAlign = TextAlign.Center,
        )
        return
    }
    BasicTextField(
        value = metric.value,
        onValueChange = onValueChange,
        modifier = modifier.padding(horizontal = TnyxTheme.dimens.SpaceXS),
        enabled = true,
        singleLine = true,
        textStyle = TnyxTheme.typography.titleMedium.merge(
            TextStyle(
                color = TnyxTheme.colors.textPrimary,
                textAlign = TextAlign.Center,
            ),
        ),
        keyboardOptions = KeyboardOptions(keyboardType = metric.keyboardType()),
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = TnyxTheme.dimens.SpaceS),
                contentAlignment = Alignment.Center,
            ) {
                if (metric.value.isBlank()) {
                    Text(
                        text = "—",
                        style = TnyxTheme.typography.titleMedium,
                        color = TnyxTheme.colors.textMuted,
                    )
                }
                innerTextField()
            }
        },
    )
}

@Composable
private fun RpeCell(
    value: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Surface(
            modifier = Modifier.clickable(enabled = enabled, onClick = onClick),
            shape = TnyxTheme.shapes.Material.small,
            color = TnyxTheme.colors.surfaceVariant,
            contentColor = if (value.isBlank()) TnyxTheme.colors.textSecondary else TnyxTheme.colors.accent,
        ) {
            Text(
                text = value.ifBlank { "RPE" },
                modifier = Modifier.padding(
                    horizontal = TnyxTheme.dimens.SpaceS,
                    vertical = TnyxTheme.dimens.SpaceXS,
                ),
                style = TnyxTheme.typography.labelMedium,
                textAlign = TextAlign.Center,
            )
        }
    }
}

private fun WorkoutSetUi.setLabel(): String = when (type) {
    SetType.NORMAL -> setNumber.toString()
    SetType.WARMUP -> "W"
    SetType.DROP_SET -> "D"
    SetType.FAILURE -> "F"
    SetType.SUPERSET -> "S"
}

private fun WorkoutMetricUi.shortLabel(): String = when (field) {
    WorkoutMetricField.WEIGHT -> "KG"
    WorkoutMetricField.REPS -> "REPS"
    WorkoutMetricField.DURATION -> "TIME"
    WorkoutMetricField.DISTANCE -> "DIST"
    WorkoutMetricField.STEPS -> "STEPS"
    WorkoutMetricField.RPE -> "RPE"
}

private fun WorkoutMetricUi.keyboardType(): KeyboardType = when (field) {
    WorkoutMetricField.WEIGHT,
    WorkoutMetricField.DISTANCE,
    -> KeyboardType.Decimal

    WorkoutMetricField.REPS,
    WorkoutMetricField.DURATION,
    WorkoutMetricField.STEPS,
    WorkoutMetricField.RPE,
    -> KeyboardType.Number
}

private fun ExerciseTrackingType.supportsRpe(): Boolean = when (this) {
    ExerciseTrackingType.WEIGHT_REPS,
    ExerciseTrackingType.BODYWEIGHT_REPS,
    ExerciseTrackingType.ASSISTED_BODYWEIGHT_REPS,
    -> true

    ExerciseTrackingType.DURATION,
    ExerciseTrackingType.DISTANCE_DURATION,
    ExerciseTrackingType.STEPS_DURATION,
    -> false
}

private const val SET_COLUMN_WEIGHT = 0.55f
private const val PREVIOUS_COLUMN_WEIGHT = 1.45f
private const val METRIC_COLUMN_WEIGHT = 1f
private const val RPE_COLUMN_WEIGHT = 0.8f
private const val DONE_COLUMN_WEIGHT = 0.7f
