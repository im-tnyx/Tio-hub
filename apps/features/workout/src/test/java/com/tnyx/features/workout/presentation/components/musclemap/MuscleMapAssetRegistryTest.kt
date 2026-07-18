package com.tnyx.features.workout.presentation.components.musclemap

import com.tnyx.shared.workout.domain.model.ExerciseMediaVariant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MuscleMapAssetRegistryTest {
    @Test
    fun resolvesCanonicalNamesAndAliasesToTypedRegions() {
        assertEquals(MuscleMapRegionKey.QUADRICEPS, MuscleMapAssetRegistry.resolveRegion("quads"))
        assertEquals(MuscleMapRegionKey.ABS, MuscleMapAssetRegistry.resolveRegion("CORE"))
        assertEquals(MuscleMapRegionKey.SHOULDERS, MuscleMapAssetRegistry.resolveRegion("deltoids"))
    }

    @Test
    fun resolvesGenderSpecificQuadricepsFrontAndBackLayers() {
        val male = requireNotNull(
            MuscleMapAssetRegistry.resolve(listOf("quadriceps"), ExerciseMediaVariant.MALE),
        )
        val female = requireNotNull(
            MuscleMapAssetRegistry.resolve(listOf("quadriceps"), ExerciseMediaVariant.FEMALE),
        )

        assertEquals("front_grey_body_male.webp", male.frontBaseAsset)
        assertEquals(listOf("front_quadriceps_male.webp"), male.frontOverlayAssets)
        assertEquals(listOf("back_quadriceps_male.webp"), male.backOverlayAssets)
        assertEquals("front_grey_body_female.webp", female.frontBaseAsset)
        assertEquals(listOf("front_quadriceps_female.webp"), female.frontOverlayAssets)
        assertEquals(listOf("back_quadriceps_female.webp"), female.backOverlayAssets)
    }

    @Test
    fun combinesMultipleRegionsAndDeduplicatesSharedLayers() {
        val layers = requireNotNull(
            MuscleMapAssetRegistry.resolve(
                rawMuscleGroups = listOf("calves", "soleus", "calves"),
                variant = ExerciseMediaVariant.FEMALE,
            ),
        )

        assertEquals(layers.frontOverlayAssets.distinct(), layers.frontOverlayAssets)
        assertEquals(layers.backOverlayAssets.distinct(), layers.backOverlayAssets)
        assertTrue(layers.frontOverlayAssets.size > 1)
        assertTrue(layers.backOverlayAssets.size > 1)
    }

    @Test
    fun neutralUnknownAndUnsupportedValuesUsePlaceholderBoundary() {
        assertNull(MuscleMapAssetRegistry.resolve(listOf("quadriceps"), ExerciseMediaVariant.NEUTRAL))
        assertNull(MuscleMapAssetRegistry.resolve(listOf("unknown"), ExerciseMediaVariant.MALE))
        assertNull(MuscleMapAssetRegistry.resolve(emptyList(), ExerciseMediaVariant.FEMALE))
    }
}
