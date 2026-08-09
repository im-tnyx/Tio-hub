package com.tnyx.features.workout.presentation.library

import com.tnyx.shared.workout.domain.model.ExerciseMediaVariant

internal fun String?.toExerciseMediaVariant(): ExerciseMediaVariant? = when (this.orEmpty().trim().lowercase()) {
    "male", "man", "m" -> ExerciseMediaVariant.MALE
    "female", "woman", "f" -> ExerciseMediaVariant.FEMALE
    else -> null
}
