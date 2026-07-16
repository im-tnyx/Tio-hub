package com.tnyx.shared.workout.domain.logic

import com.tnyx.shared.workout.domain.model.ExerciseDefinition
import com.tnyx.shared.workout.domain.model.ExerciseMediaAsset
import com.tnyx.shared.workout.domain.model.ExerciseMediaPreference
import com.tnyx.shared.workout.domain.model.ExerciseMediaReleaseStatus
import com.tnyx.shared.workout.domain.model.ExerciseMediaResolutionReason
import com.tnyx.shared.workout.domain.model.ExerciseMediaVariant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ExerciseMediaResolverTest {
    @Test
    fun explicitPreferenceUsesLatestApprovedExactVariant() {
        val exercise = exercise(
            asset("female-v1", ExerciseMediaVariant.FEMALE, version = 1),
            asset("female-v2", ExerciseMediaVariant.FEMALE, version = 2),
            asset("male-v9", ExerciseMediaVariant.MALE, version = 9)
        )

        val resolved = ExerciseMediaResolver.resolve(
            exercise = exercise,
            preference = ExerciseMediaPreference.FEMALE
        )

        assertEquals(ExerciseMediaVariant.FEMALE, resolved.requestedVariant)
        assertEquals(ExerciseMediaVariant.FEMALE, resolved.resolvedVariant)
        assertEquals("female-v2", resolved.asset?.id)
        assertEquals(ExerciseMediaResolutionReason.EXACT, resolved.reason)
    }

    @Test
    fun autoUsesProvidedProfileDefault() {
        val exercise = exercise(asset("male", ExerciseMediaVariant.MALE))

        val resolved = ExerciseMediaResolver.resolve(
            exercise = exercise,
            preference = ExerciseMediaPreference.AUTO,
            autoVariant = ExerciseMediaVariant.MALE
        )

        assertEquals(ExerciseMediaVariant.MALE, resolved.requestedVariant)
        assertEquals("male", resolved.asset?.id)
        assertEquals(ExerciseMediaResolutionReason.EXACT, resolved.reason)
    }

    @Test
    fun autoWithoutProfileDefaultUsesNeutral() {
        val exercise = exercise(asset("neutral", ExerciseMediaVariant.NEUTRAL))

        val resolved = ExerciseMediaResolver.resolve(
            exercise = exercise,
            preference = ExerciseMediaPreference.AUTO
        )

        assertEquals(ExerciseMediaVariant.NEUTRAL, resolved.requestedVariant)
        assertEquals("neutral", resolved.asset?.id)
        assertEquals(ExerciseMediaResolutionReason.EXACT, resolved.reason)
    }

    @Test
    fun missingRequestedVariantUsesNeutralButNeverOppositeVariant() {
        val exercise = exercise(
            asset("male", ExerciseMediaVariant.MALE),
            asset("neutral", ExerciseMediaVariant.NEUTRAL)
        )

        val resolved = ExerciseMediaResolver.resolve(
            exercise = exercise,
            preference = ExerciseMediaPreference.FEMALE
        )

        assertEquals(ExerciseMediaVariant.FEMALE, resolved.requestedVariant)
        assertEquals(ExerciseMediaVariant.NEUTRAL, resolved.resolvedVariant)
        assertEquals("neutral", resolved.asset?.id)
        assertEquals(ExerciseMediaResolutionReason.NEUTRAL_FALLBACK, resolved.reason)
    }

    @Test
    fun blockedUnprovenancedAndOppositeAssetsResolveToPlaceholder() {
        val exercise = exercise(
            asset(
                id = "blocked-female",
                variant = ExerciseMediaVariant.FEMALE,
                releaseStatus = ExerciseMediaReleaseStatus.BLOCKED
            ),
            asset(
                id = "unprovenanced-female",
                variant = ExerciseMediaVariant.FEMALE,
                provenanceId = ""
            ),
            asset("approved-male", ExerciseMediaVariant.MALE)
        )

        val resolved = ExerciseMediaResolver.resolve(
            exercise = exercise,
            preference = ExerciseMediaPreference.FEMALE
        )

        assertNull(resolved.resolvedVariant)
        assertNull(resolved.asset)
        assertEquals(ExerciseMediaResolutionReason.PLACEHOLDER, resolved.reason)
    }

    private fun exercise(vararg media: ExerciseMediaAsset): ExerciseDefinition = ExerciseDefinition(
        id = "exercise-1",
        name = "Test Exercise",
        mediaAssets = media.toList()
    )

    private fun asset(
        id: String,
        variant: ExerciseMediaVariant,
        version: Int = 1,
        provenanceId: String = "provenance-$id",
        releaseStatus: ExerciseMediaReleaseStatus = ExerciseMediaReleaseStatus.APPROVED
    ): ExerciseMediaAsset = ExerciseMediaAsset(
        id = id,
        variant = variant,
        imageRef = "image-$id",
        mediaVersion = version,
        provenanceId = provenanceId,
        releaseStatus = releaseStatus
    )
}
