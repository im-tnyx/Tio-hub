package com.tnyx.shared.workout.domain.logic

import com.tnyx.shared.workout.domain.model.ExerciseDefinition
import com.tnyx.shared.workout.domain.model.ExerciseMediaAsset
import com.tnyx.shared.workout.domain.model.ExerciseMediaPreference
import com.tnyx.shared.workout.domain.model.ExerciseMediaReleaseStatus
import com.tnyx.shared.workout.domain.model.ExerciseMediaResolutionReason
import com.tnyx.shared.workout.domain.model.ExerciseMediaVariant
import com.tnyx.shared.workout.domain.model.ResolvedExerciseMedia

object ExerciseMediaPreferenceResolver {
    fun resolve(
        preference: ExerciseMediaPreference,
        autoVariant: ExerciseMediaVariant?
    ): ExerciseMediaVariant = when (preference) {
        ExerciseMediaPreference.AUTO -> autoVariant ?: ExerciseMediaVariant.NEUTRAL
        ExerciseMediaPreference.MALE -> ExerciseMediaVariant.MALE
        ExerciseMediaPreference.FEMALE -> ExerciseMediaVariant.FEMALE
        ExerciseMediaPreference.NEUTRAL -> ExerciseMediaVariant.NEUTRAL
    }
}
object ExerciseMediaResolver {
    fun resolve(
        exercise: ExerciseDefinition,
        preference: ExerciseMediaPreference,
        autoVariant: ExerciseMediaVariant? = null
    ): ResolvedExerciseMedia {
        val requestedVariant = ExerciseMediaPreferenceResolver.resolve(preference, autoVariant)
        val eligibleAssets = exercise.mediaAssets.filter { it.isEligible() }
        val exactAsset = eligibleAssets.latestFor(requestedVariant)

        if (exactAsset != null) {
            return ResolvedExerciseMedia(
                exerciseId = exercise.id,
                requestedVariant = requestedVariant,
                resolvedVariant = exactAsset.variant,
                asset = exactAsset,
                reason = ExerciseMediaResolutionReason.EXACT
            )
        }

        val fallbackAsset = if (requestedVariant == ExerciseMediaVariant.NEUTRAL) {
            eligibleAssets.latestFor(ExerciseMediaVariant.MALE)
                ?: eligibleAssets.latestFor(ExerciseMediaVariant.FEMALE)
        } else {
            eligibleAssets.latestFor(ExerciseMediaVariant.NEUTRAL)
        }

        if (fallbackAsset != null) {
            return ResolvedExerciseMedia(
                exerciseId = exercise.id,
                requestedVariant = requestedVariant,
                resolvedVariant = fallbackAsset.variant,
                asset = fallbackAsset,
                reason = ExerciseMediaResolutionReason.NEUTRAL_FALLBACK
            )
        }

        return ResolvedExerciseMedia(
            exerciseId = exercise.id,
            requestedVariant = requestedVariant,
            reason = ExerciseMediaResolutionReason.PLACEHOLDER
        )
    }

    private fun ExerciseMediaAsset.isEligible(): Boolean =
        id.isNotBlank() &&
            mediaVersion > 0 &&
            provenanceId.isNotBlank() &&
            releaseStatus == ExerciseMediaReleaseStatus.APPROVED &&
            hasMedia

    private fun List<ExerciseMediaAsset>.latestFor(
        variant: ExerciseMediaVariant
    ): ExerciseMediaAsset? = filter { it.variant == variant }
        .maxWithOrNull(
            compareBy<ExerciseMediaAsset> { it.mediaVersion }
                .thenBy { it.id }
        )
}
