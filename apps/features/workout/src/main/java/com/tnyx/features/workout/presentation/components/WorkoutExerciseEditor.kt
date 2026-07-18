package com.tnyx.features.workout.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import com.tnyx.core.theme.TnyxTheme
import com.tnyx.core.ui.components.cards.TnyxCard
import com.tnyx.core.ui.components.cards.TnyxCardVariant
import com.tnyx.core.ui.components.inputs.TnyxTextField
import com.tnyx.features.workout.presentation.WorkoutExerciseUi
import com.tnyx.features.workout.presentation.WorkoutMetricField
import com.tnyx.features.workout.presentation.WorkoutMetricUi
import com.tnyx.features.workout.presentation.WorkoutSetUi
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
    onCompleteSet: (setId: String?) -> Unit,
    modifier: Modifier = Modifier,
    errorMessage: String? = null,
) {
    TnyxCard(
        modifier = modifier.fillMaxWidth(),
        variant = TnyxCardVariant.Outlined,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(TnyxTheme.dimens.SpaceM)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onExpandedChange(!state.isExpanded) },
                horizontalArrangement = Arrangement.spacedBy(TnyxTheme.dimens.SpaceS),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(TnyxTheme.dimens.SpaceXXS),
                ) {
                    Text(
                        text = state.name,
                        style = TnyxTheme.typography.titleLarge,
                        color = TnyxTheme.colors.textPrimary,
                    )
                    Text(
                        text = "${state.sets.size} set · ${state.restSeconds} sec rest",
                        style = TnyxTheme.typography.bodySmall,
                        color = TnyxTheme.colors.textSecondary,
                    )
                }
                Icon(
                    imageVector = if (state.isExpanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                    contentDescription = if (state.isExpanded) "Collapse exercise" else "Expand exercise",
                    tint = TnyxTheme.colors.textSecondary,
                )
            }

            if (state.isExpanded) {
                Column(verticalArrangement = Arrangement.spacedBy(TnyxTheme.dimens.SpaceS)) {
                    state.sets.forEach { set ->
                        WorkoutSetRow(
                            state = set,
                            mode = mode,
                            enabled = enabled,
                            onMetricChange = { field, value -> onMetricChange(set.id, field, value) },
                            onComplete = { onCompleteSet(set.id) },
                            errorMessage = errorMessage,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WorkoutSetRow(
    state: WorkoutSetUi,
    mode: ExerciseEditorMode,
    enabled: Boolean,
    onMetricChange: (WorkoutMetricField, String) -> Unit,
    onComplete: () -> Unit,
    errorMessage: String?,
) {
    TnyxCard(
        modifier = Modifier.fillMaxWidth(),
        variant = TnyxCardVariant.Normal,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(TnyxTheme.dimens.SpaceS)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(TnyxTheme.dimens.SpaceS),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Set ${state.setNumber}",
                        style = TnyxTheme.typography.titleMedium,
                        color = TnyxTheme.colors.textPrimary,
                    )
                    Text(
                        text = state.type.displayName(),
                        style = TnyxTheme.typography.bodySmall,
                        color = TnyxTheme.colors.textSecondary,
                    )
                }
                if (mode == ExerciseEditorMode.ACTIVE_WORKOUT) {
                    IconButton(
                        onClick = onComplete,
                        enabled = enabled && !state.isCompleted,
                    ) {
                        Icon(
                            imageVector = if (state.isCompleted) {
                                Icons.Rounded.CheckCircle
                            } else {
                                Icons.Rounded.RadioButtonUnchecked
                            },
                            contentDescription = if (state.isCompleted) {
                                "Set ${state.setNumber} completed"
                            } else {
                                "Complete set ${state.setNumber}"
                            },
                            tint = if (state.isCompleted) {
                                TnyxTheme.colors.success
                            } else {
                                TnyxTheme.colors.textSecondary
                            },
                        )
                    }
                }
            }

            when {
                state.isCompleted || mode == ExerciseEditorMode.READ_ONLY -> {
                    Text(
                        text = state.completedSummary ?: state.metrics.readOnlySummary(),
                        style = TnyxTheme.typography.bodyLarge,
                        color = TnyxTheme.colors.textPrimary,
                    )
                }

                else -> MetricFields(
                    metrics = state.metrics,
                    enabled = enabled,
                    onMetricChange = onMetricChange,
                    errorMessage = errorMessage,
                )
            }
        }
    }
}

@Composable
private fun MetricFields(
    metrics: List<WorkoutMetricUi>,
    enabled: Boolean,
    onMetricChange: (WorkoutMetricField, String) -> Unit,
    errorMessage: String?,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(TnyxTheme.dimens.SpaceS),
        verticalAlignment = Alignment.Top,
    ) {
        metrics.forEach { metric ->
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(TnyxTheme.dimens.SpaceXXS),
            ) {
                metric.previousValue?.let { previous ->
                    Text(
                        text = "Previous: $previous",
                        style = TnyxTheme.typography.labelSmall,
                        color = TnyxTheme.colors.textSecondary,
                    )
                }
                TnyxTextField(
                    value = metric.value,
                    onValueChange = { onMetricChange(metric.field, it) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(metric.label) },
                    keyboardOptions = KeyboardOptions(keyboardType = metric.keyboardType()),
                    enabled = enabled,
                    isError = errorMessage != null,
                )
            }
        }
    }
}

private fun WorkoutMetricUi.keyboardType(): KeyboardType = when (field) {
    WorkoutMetricField.WEIGHT,
    WorkoutMetricField.DISTANCE,
    -> KeyboardType.Decimal

    WorkoutMetricField.REPS,
    WorkoutMetricField.DURATION,
    WorkoutMetricField.STEPS,
    -> KeyboardType.Number
}

private fun List<WorkoutMetricUi>.readOnlySummary(): String =
    joinToString(separator = " · ") { metric -> "${metric.label}: ${metric.value.ifBlank { "—" }}" }

private fun SetType.displayName(): String = when (this) {
    SetType.NORMAL -> "Normal"
    SetType.WARMUP -> "Warm-up"
    SetType.DROP_SET -> "Drop set"
    SetType.FAILURE -> "Failure"
    SetType.SUPERSET -> "Superset"
}
