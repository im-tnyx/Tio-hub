package com.tnyx.features.workout.presentation.library.exerciseinfo

import com.tnyx.shared.workout.domain.model.ExerciseMediaVariant
import com.tnyx.shared.workout.domain.logic.ExerciseMediaResolver
import com.tnyx.shared.workout.domain.model.ExerciseDefinition
import com.tnyx.shared.workout.domain.model.ExerciseMediaAsset
import com.tnyx.shared.workout.domain.model.ExerciseMediaPreference

internal fun resolveExerciseInfoMediaAsset(
    exercise: ExerciseDefinition,
    mediaVariant: ExerciseMediaVariant?,
): ExerciseMediaAsset? {
    return ExerciseMediaResolver.resolve(
        exercise = exercise,
        preference = ExerciseMediaPreference.AUTO,
        autoVariant = mediaVariant,
    ).asset
}
