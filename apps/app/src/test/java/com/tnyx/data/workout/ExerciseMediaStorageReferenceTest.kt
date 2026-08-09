package com.tnyx.data.workout

import com.tnyx.shared.workout.domain.model.ExerciseDefinition
import com.tnyx.shared.workout.domain.model.ExerciseMediaAsset
import com.tnyx.shared.workout.domain.model.ExerciseMediaReleaseStatus
import com.tnyx.shared.workout.domain.model.ExerciseMediaVariant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ExerciseMediaStorageReferenceTest {

    @Test
    fun `canonical reference resolves to its object path`() {
        val objectPath = "$OWNER_ID/custom-exercise/media.mp4"

        assertEquals(
            objectPath,
            objectPath.toExerciseMediaStorageReference().toExerciseMediaObjectPath(),
        )
    }

    @Test
    fun `legacy public and signed urls resolve to their object path`() {
        val objectPath = "$OWNER_ID/custom-exercise/media image.jpg"
        val encodedPath = "$OWNER_ID/custom-exercise/media%20image.jpg"

        assertEquals(
            objectPath,
            "https://project.supabase.co/storage/v1/object/public/$EXERCISE_MEDIA_BUCKET/$encodedPath"
                .toExerciseMediaObjectPath(),
        )
        assertEquals(
            objectPath,
            "https://project.supabase.co/storage/v1/object/sign/$EXERCISE_MEDIA_BUCKET/$encodedPath?token=abc"
                .toExerciseMediaObjectPath(),
        )
    }

    @Test
    fun `owner check rejects another users object path`() {
        val reference = "another-user/custom-exercise/media.mp4".toExerciseMediaStorageReference()

        assertNull(reference.toOwnedExerciseMediaObjectPath(OWNER_ID))
    }

    @Test
    fun `unsafe and unrelated references are rejected`() {
        assertNull("https://example.com/media.mp4".toExerciseMediaObjectPath())
        assertNull("supabase-storage://$EXERCISE_MEDIA_BUCKET/$OWNER_ID/../media.mp4".toExerciseMediaObjectPath())
    }

    @Test
    fun `exercise references normalize without changing external media`() {
        val objectPath = "$OWNER_ID/custom-exercise/media.png"
        val publicUrl =
            "https://project.supabase.co/storage/v1/object/public/$EXERCISE_MEDIA_BUCKET/$objectPath"
        val exercise = ExerciseDefinition(
            id = "custom-exercise",
            name = "Custom exercise",
            mediaAssets = listOf(
                ExerciseMediaAsset(
                    id = "custom-media",
                    variant = ExerciseMediaVariant.NEUTRAL,
                    imageRef = publicUrl,
                    thumbnailRef = "https://cdn.example.com/thumbnail.jpg",
                    provenanceId = "user-generated",
                    releaseStatus = ExerciseMediaReleaseStatus.APPROVED,
                )
            ),
            isCustom = true,
        )

        val normalized = exercise.withDurableExerciseMediaReferences()

        assertEquals(
            objectPath.toExerciseMediaStorageReference(),
            normalized.mediaAssets.single().imageRef,
        )
        assertEquals(
            "https://cdn.example.com/thumbnail.jpg",
            normalized.mediaAssets.single().thumbnailRef,
        )
    }

    private companion object {
        const val OWNER_ID = "11111111-1111-1111-1111-111111111111"
    }
}
