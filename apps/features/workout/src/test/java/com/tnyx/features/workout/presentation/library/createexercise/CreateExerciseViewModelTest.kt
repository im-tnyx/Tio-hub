package com.tnyx.features.workout.presentation.library.createexercise

import androidx.lifecycle.SavedStateHandle
import com.tnyx.features.workout.domain.repository.CustomExerciseMediaUpdate
import com.tnyx.features.workout.domain.repository.ExerciseCatalogRepository
import com.tnyx.features.workout.presentation.MainDispatcherRule
import com.tnyx.shared.workout.domain.model.ExerciseDefinition
import com.tnyx.shared.workout.domain.model.ExerciseMediaAsset
import com.tnyx.shared.workout.domain.model.ExerciseMediaReleaseStatus
import com.tnyx.shared.workout.domain.model.ExerciseMediaVariant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
@OptIn(ExperimentalCoroutinesApi::class)
class CreateExerciseViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun selectedImageFileIsIncludedInSaveMutation() = runTest(mainDispatcherRule.testDispatcher) {
        val repository = FakeExerciseCatalogRepository()
        val viewModel = createViewModel(repository)
        val event = async(start = CoroutineStart.UNDISPATCHED) { viewModel.eventFlow.first() }

        viewModel.onAction(CreateExerciseAction.NameChanged("Cable Row"))
        viewModel.onAction(
            CreateExerciseAction.AssetSelected(
                uri = "file:///crop.png",
                localFilePath = "C:/cache/crop.png",
                mimeType = "image/png",
            )
        )
        viewModel.onAction(CreateExerciseAction.SaveClicked)
        advanceUntilIdle()

        assertIs<CreateExerciseEvent.SaveSuccess>(event.await())
        assertEquals("Cable Row", repository.savedExercise?.name)
        val mediaUpdate = assertIs<CustomExerciseMediaUpdate.Replace>(repository.savedMediaUpdate)
        assertEquals("C:/cache/crop.png", mediaUpdate.localFilePath)
        assertEquals("image/png", mediaUpdate.mimeType)
    }

    @Test
    fun selectedVideoFileIsIncludedInSaveMutation() = runTest(mainDispatcherRule.testDispatcher) {
        val repository = FakeExerciseCatalogRepository()
        val viewModel = createViewModel(repository)
        val event = async(start = CoroutineStart.UNDISPATCHED) { viewModel.eventFlow.first() }

        viewModel.onAction(CreateExerciseAction.NameChanged("Form Check Squat"))
        viewModel.onAction(
            CreateExerciseAction.AssetSelected(
                uri = "file:///form-check.mp4",
                localFilePath = "C:/cache/form-check.mp4",
                mimeType = "video/mp4",
            )
        )
        viewModel.onAction(CreateExerciseAction.SaveClicked)
        advanceUntilIdle()

        assertIs<CreateExerciseEvent.SaveSuccess>(event.await())
        val mediaUpdate = assertIs<CustomExerciseMediaUpdate.Replace>(repository.savedMediaUpdate)
        assertEquals("C:/cache/form-check.mp4", mediaUpdate.localFilePath)
        assertEquals("video/mp4", mediaUpdate.mimeType)
        assertEquals("video/mp4", viewModel.uiState.value.assetMimeType)
    }

    @Test
    fun removingExistingImageUsesRemoveMutation() = runTest(mainDispatcherRule.testDispatcher) {
        val existingExercise = ExerciseDefinition(
            id = "7fdd82d6-05e1-4a73-85f0-9fd965dd63ea",
            name = "Custom Row",
            mediaAssets = listOf(
                ExerciseMediaAsset(
                    id = "custom_row_image",
                    variant = ExerciseMediaVariant.NEUTRAL,
                    imageRef = "https://example.test/image.jpg",
                    provenanceId = "user-generated",
                    releaseStatus = ExerciseMediaReleaseStatus.APPROVED,
                )
            ),
            isCustom = true,
        )
        val repository = FakeExerciseCatalogRepository(existingExercise = existingExercise)
        val viewModel = createViewModel(repository, existingExercise.id)
        advanceUntilIdle()
        val event = async(start = CoroutineStart.UNDISPATCHED) { viewModel.eventFlow.first() }

        viewModel.onAction(CreateExerciseAction.RemoveAssetClicked)
        viewModel.onAction(CreateExerciseAction.SaveClicked)
        advanceUntilIdle()

        assertIs<CreateExerciseEvent.SaveSuccess>(event.await())
        assertIs<CustomExerciseMediaUpdate.Remove>(repository.savedMediaUpdate)
    }

    @Test
    fun unauthenticatedSaveReturnsActionableMessage() = runTest(mainDispatcherRule.testDispatcher) {
        val repository = FakeExerciseCatalogRepository(
            saveError = IllegalStateException(
                "A signed-in Supabase user is required to save custom exercises"
            )
        )
        val viewModel = createViewModel(repository)
        val event = async(start = CoroutineStart.UNDISPATCHED) { viewModel.eventFlow.first() }

        viewModel.onAction(CreateExerciseAction.NameChanged("Cable Row"))
        viewModel.onAction(CreateExerciseAction.SaveClicked)
        advanceUntilIdle()

        val error = assertIs<CreateExerciseEvent.SaveError>(event.await())
        assertEquals("Sign in to save a custom exercise.", error.message)
        assertEquals(false, viewModel.uiState.value.isSaving)
    }

    private fun createViewModel(
        repository: ExerciseCatalogRepository,
        exerciseId: String? = null,
    ): CreateExerciseViewModel {
        return CreateExerciseViewModel(
            savedStateHandle = SavedStateHandle(mapOf("exerciseId" to exerciseId)),
            catalogRepository = repository,
        )
    }

    private class FakeExerciseCatalogRepository(
        private val existingExercise: ExerciseDefinition? = null,
        private val saveError: Exception? = null,
    ) : ExerciseCatalogRepository {
        var savedExercise: ExerciseDefinition? = null
        var savedMediaUpdate: CustomExerciseMediaUpdate? = null

        override fun getExercises(): Flow<List<ExerciseDefinition>> = flowOf(emptyList())

        override fun searchExercises(
            query: String,
            muscleGroupFilter: String,
        ): Flow<List<ExerciseDefinition>> = flowOf(emptyList())

        override suspend fun getExerciseById(exerciseId: String): ExerciseDefinition? {
            return existingExercise?.takeIf { exercise -> exercise.id == exerciseId }
        }

        override suspend fun saveCustomExercise(
            exercise: ExerciseDefinition,
            mediaUpdate: CustomExerciseMediaUpdate,
        ) {
            saveError?.let { throw it }
            savedExercise = exercise
            savedMediaUpdate = mediaUpdate
        }

        override suspend fun deleteCustomExercise(exerciseId: String) = Unit
    }
}
