package com.tnyx.data.nutrition

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class NutritionMediaStorageReferenceTest {

    @Test
    fun durableReferenceRoundTripsToOwnedObjectPath() {
        val ownerId = "11111111-1111-1111-1111-111111111111"
        val objectPath = "$ownerId/22222222-2222-2222-2222-222222222222/photo.jpg"

        val reference = objectPath.toNutritionMediaStorageReference()

        assertEquals(objectPath, reference.toOwnedNutritionMediaObjectPath(ownerId))
    }

    @Test
    fun signedUrlResolvesWithoutPersistingItsToken() {
        val ownerId = "11111111-1111-1111-1111-111111111111"
        val objectPath = "$ownerId/22222222-2222-2222-2222-222222222222/photo.jpg"
        val signedUrl =
            "https://example.supabase.co/storage/v1/object/sign/tio-nutrition-media/$objectPath?token=secret"

        assertEquals(objectPath, signedUrl.toOwnedNutritionMediaObjectPath(ownerId))
    }

    @Test
    fun anotherOwnersReferenceIsRejected() {
        val reference =
            "supabase-storage://tio-nutrition-media/other-user/meal/photo.jpg"

        assertNull(reference.toOwnedNutritionMediaObjectPath("current-user"))
    }
}
