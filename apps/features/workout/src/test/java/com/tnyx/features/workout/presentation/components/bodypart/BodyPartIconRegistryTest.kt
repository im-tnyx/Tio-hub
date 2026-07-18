package com.tnyx.features.workout.presentation.components.bodypart

import com.tnyx.features.workout.R
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class BodyPartIconRegistryTest {
    @Test
    fun resolvesEverySupportedCanonicalBodyPart() {
        BodyPartIconKey.entries.forEach { entry ->
            assertEquals(entry, BodyPartIconRegistry.resolve(entry.name))
        }
    }

    @Test
    fun normalizesAliasesWithoutLeakingResourceNamesIntoDomainData() {
        assertEquals(BodyPartIconKey.BACK, BodyPartIconRegistry.resolve("Body Back"))
        assertEquals(BodyPartIconKey.QUADRICEPS, BodyPartIconRegistry.resolve("quads"))
        assertEquals(BodyPartIconKey.ABS, BodyPartIconRegistry.resolve("CORE"))
    }

    @Test
    fun unknownAndEmptyValuesUseReleaseSafePlaceholder() {
        assertNull(BodyPartIconRegistry.resolve(null))
        assertNull(BodyPartIconRegistry.resolve("  "))
        assertNull(BodyPartIconRegistry.resolve("full body"))
        assertEquals(
            R.drawable.tio_body_part_placeholder,
            BodyPartIconRegistry.drawableFor("full body"),
        )
    }
}
