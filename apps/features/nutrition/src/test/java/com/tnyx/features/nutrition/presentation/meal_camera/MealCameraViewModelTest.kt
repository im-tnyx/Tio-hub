package com.tnyx.features.nutrition.presentation.meal_camera

import androidx.lifecycle.SavedStateHandle
import com.tnyx.features.nutrition.domain.models.MealItem
import com.tnyx.features.nutrition.domain.models.MealPhotoAnalysis
import com.tnyx.features.nutrition.domain.repository.MealPhotoAnalysisException
import com.tnyx.features.nutrition.domain.repository.MealPhotoRecognitionRepository
import com.tnyx.features.nutrition.domain.repository.FoodSearchRepository
import com.tnyx.features.nutrition.presentation.meal_diary.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MealCameraViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun barcodeWithoutExactMatchOpensPrefilledSearch() = runTest {
        val viewModel = viewModel(FakeMealPhotoRecognitionRepository())
        val effect = async(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.effect.first()
        }

        viewModel.handleAction(MealCameraAction.BarcodeClicked)
        assertEquals(true, viewModel.uiState.value.isBarcodeMode)

        viewModel.handleAction(MealCameraAction.BarcodeDetected("8901234567890"))
        advanceUntilIdle()

        assertEquals(
            "8901234567890",
            (effect.await() as MealCameraEffect.OpenBarcodeSearch).barcode,
        )
        assertFalse(viewModel.uiState.value.isBarcodeMode)
    }

    @Test
    fun exactBarcodeMatchOpensMealEditor() = runTest {
        val item = MealItem(
            id = "7d6418c9-cf72-4a4d-8d84-85566199c31a",
            name = "Indexed food",
            calories = 120,
            protein = 4.0,
            quantity = 1.0,
            unit = "100 g",
        )
        val viewModel = viewModel(
            repository = FakeMealPhotoRecognitionRepository(),
            foodSearchRepository = FakeBarcodeFoodSearchRepository(item),
        )
        val effect = async(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.effect.first()
        }

        viewModel.handleAction(MealCameraAction.BarcodeDetected("3017620422003"))
        advanceUntilIdle()

        assertEquals(
            item,
            (effect.await() as MealCameraEffect.OpenBarcodeMealEditor).item,
        )
        assertFalse(viewModel.uiState.value.isResolvingBarcode)
    }

    @Test
    fun capturedPhotoCanBeRetriedWithoutCallingRecognition() = runTest {
        val repository = FakeMealPhotoRecognitionRepository()
        val viewModel = viewModel(repository)

        viewModel.handleAction(
            MealCameraAction.PhotoCaptured("G:/temp/meal.jpg", "image/jpeg")
        )
        viewModel.handleAction(MealCameraAction.RetryClicked)

        assertNull(viewModel.uiState.value.capturedPhotoPath)
        assertFalse(viewModel.uiState.value.isAnalyzing)
        assertEquals(0, repository.calls)
    }

    @Test
    fun preparedPhotoOpensMealEditorWithDetectedItems() = runTest {
        val repository = FakeMealPhotoRecognitionRepository(
            result = MealPhotoAnalysis(
                suggestedName = "Banana and oats",
                items = listOf(
                    MealItem(
                        id = "3cb1465f-4ec7-40f6-91fb-00b7d15c610f",
                        name = "Banana",
                        calories = 105,
                        protein = 1.3,
                        quantity = 1.0,
                        unit = "1 medium",
                    )
                ),
            )
        )
        val viewModel = viewModel(repository)
        viewModel.handleAction(
            MealCameraAction.PhotoCaptured("G:/temp/meal.jpg", "image/jpeg")
        )
        val effect = async(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.effect.first()
        }

        viewModel.handleAction(MealCameraAction.AnalysisPreparationStarted)
        viewModel.handleAction(MealCameraAction.AnalysisPrepared(byteArrayOf(1, 2, 3)))
        advanceUntilIdle()

        val navigation = effect.await() as MealCameraEffect.OpenMealEditor
        assertEquals("Banana and oats", navigation.meal.name)
        assertEquals("Banana", navigation.meal.items.single().name)
        assertEquals("G:/temp/meal.jpg", navigation.photoPath)
        assertEquals(1, repository.calls)
        assertFalse(viewModel.uiState.value.isAnalyzing)
    }

    @Test
    fun providerFailureUsesSafeDomainMessage() = runTest {
        val viewModel = viewModel(
            FakeMealPhotoRecognitionRepository(
                error = MealPhotoAnalysisException("Meal photo provider is unavailable.")
            )
        )
        viewModel.handleAction(MealCameraAction.PhotoCaptured("G:/temp/meal.jpg", "image/jpeg"))

        viewModel.handleAction(MealCameraAction.AnalysisPrepared(byteArrayOf(1)))
        advanceUntilIdle()

        assertEquals("Meal photo provider is unavailable.", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun unexpectedFailureDoesNotExposeRawException() = runTest {
        val viewModel = viewModel(
            FakeMealPhotoRecognitionRepository(
                error = IllegalStateException("Authorization=[Bearer sensitive-token]")
            )
        )
        viewModel.handleAction(MealCameraAction.PhotoCaptured("G:/temp/meal.jpg", "image/jpeg"))

        viewModel.handleAction(MealCameraAction.AnalysisPrepared(byteArrayOf(1)))
        advanceUntilIdle()

        assertEquals("Meal photo could not be analyzed.", viewModel.uiState.value.errorMessage)
    }

    private fun viewModel(
        repository: MealPhotoRecognitionRepository,
        foodSearchRepository: FoodSearchRepository = FakeBarcodeFoodSearchRepository(),
    ) = MealCameraViewModel(
        savedStateHandle = SavedStateHandle(mapOf("date" to "2026-08-10")),
        recognitionRepository = repository,
        foodSearchRepository = foodSearchRepository,
    )
}

private class FakeBarcodeFoodSearchRepository(
    private val result: MealItem? = null,
) : FoodSearchRepository {
    override suspend fun search(query: String): List<MealItem> = emptyList()

    override suspend fun lookupBarcode(barcode: String): MealItem? = result
}

private class FakeMealPhotoRecognitionRepository(
    private val result: MealPhotoAnalysis = MealPhotoAnalysis("", emptyList()),
    private val error: Throwable? = null,
) : MealPhotoRecognitionRepository {
    var calls = 0

    override suspend fun analyze(
        imageBytes: ByteArray,
        mimeType: String,
    ): MealPhotoAnalysis {
        calls += 1
        error?.let { throw it }
        return result
    }
}
