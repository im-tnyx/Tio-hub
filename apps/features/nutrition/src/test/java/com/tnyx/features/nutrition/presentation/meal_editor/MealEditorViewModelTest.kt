package com.tnyx.features.nutrition.presentation.meal_editor

import androidx.lifecycle.SavedStateHandle
import com.tnyx.features.nutrition.domain.models.MealDiarySnapshot
import com.tnyx.features.nutrition.domain.models.MealItem
import com.tnyx.features.nutrition.domain.models.MealPhotoUpdate
import com.tnyx.features.nutrition.domain.models.MicronutrientSnapshot
import com.tnyx.features.nutrition.domain.models.NutritionMeal
import com.tnyx.features.nutrition.domain.models.NutritionSnapshot
import com.tnyx.features.nutrition.domain.models.NutritionTargetsSnapshot
import com.tnyx.features.nutrition.domain.models.ServingSnapshot
import com.tnyx.features.nutrition.domain.repository.NutritionRepository
import com.tnyx.features.nutrition.presentation.meal_diary.MainDispatcherRule
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MealEditorViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun draftItemIsSavedWithMealAggregate() = runTest {
        val repository = FakeMealEditorRepository()
        val viewModel = createViewModel(repository)
        val item = sampleItem()
        val effect = async(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.effect.first()
        }

        viewModel.handleAction(MealEditorAction.NameChanged("Lunch"))
        viewModel.acceptItemResult(Json.encodeToString(item))
        viewModel.handleAction(MealEditorAction.SaveClicked)
        advanceUntilIdle()

        assertTrue(effect.await() is MealEditorEffect.MealSaved)
        assertEquals("Lunch", repository.savedMeal?.name)
        assertEquals(item, repository.savedMeal?.items?.single())
        assertEquals(6.5, repository.savedMeal?.items?.single()?.fiber ?: 0.0, 0.0)
        assertEquals(1.2, repository.savedMeal?.items?.single()?.sugar ?: 0.0, 0.0)
        assertEquals(0.1, repository.savedMeal?.items?.single()?.transFat ?: 0.0, 0.0)
        assertEquals(1.4, repository.savedMeal?.items?.single()?.saturatedFat ?: 0.0, 0.0)
        assertEquals(320.0, repository.savedMeal?.items?.single()?.sodium ?: 0.0, 0.0)
        assertEquals(2.4, repository.savedMeal?.items?.single()?.micronutrients?.ironMg ?: 0.0, 0.0)
        assertEquals("edamam", repository.savedMeal?.items?.single()?.nutritionSnapshot?.provider)
        assertEquals("search", repository.savedMeal?.items?.single()?.inputSource)
        assertEquals(LocalDate.of(2026, 8, 9), repository.savedDateTime?.toLocalDate())
        assertFalse(viewModel.uiState.value.isSaving)
    }

    @Test
    fun failedSaveKeepsEditorOpenWithError() = runTest {
        val repository = FakeMealEditorRepository(saveError = IllegalStateException("Network unavailable"))
        val viewModel = createViewModel(repository)

        viewModel.handleAction(MealEditorAction.NameChanged("Dinner"))
        viewModel.acceptItemResult(Json.encodeToString(sampleItem()))
        viewModel.handleAction(MealEditorAction.SaveClicked)
        advanceUntilIdle()

        assertEquals("Network unavailable", viewModel.uiState.value.errorMessage)
        assertFalse(viewModel.uiState.value.isSaving)
        assertNotNull(repository.savedMeal)
    }

    @Test
    fun addItemReturnsToSearchWithoutDiscardingDraft() = runTest {
        val viewModel = createViewModel(FakeMealEditorRepository())
        val effect = async(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.effect.first()
        }

        viewModel.handleAction(MealEditorAction.NameChanged("Lunch"))
        viewModel.acceptItemResult(Json.encodeToString(sampleItem()))
        viewModel.handleAction(MealEditorAction.AddItemClicked)
        advanceUntilIdle()

        assertEquals(
            MealEditorEffect.NavigateToSearch(LocalDate.of(2026, 8, 9)),
            effect.await(),
        )
        assertEquals("Lunch", viewModel.uiState.value.meal.name)
        assertEquals(1, viewModel.uiState.value.meal.items.size)
    }

    @Test
    fun solidFoodServingEditorAppliesAmountAndUnit() = runTest {
        val viewModel = createViewModel(FakeMealEditorRepository())

        viewModel.acceptItemResult(Json.encodeToString(sampleItem()))
        assertEquals("bowl", viewModel.uiState.value.meal.servingsDescription)

        viewModel.handleAction(MealEditorAction.ServingEditorRequested)
        assertTrue(viewModel.uiState.value.isServingEditorVisible)
        assertEquals("1", viewModel.uiState.value.servingAmountInput)
        assertEquals("bowl", viewModel.uiState.value.servingUnitInput)
        assertTrue("g" in viewModel.uiState.value.servingUnitOptions)
        assertTrue("piece" in viewModel.uiState.value.servingUnitOptions)

        viewModel.handleAction(MealEditorAction.ServingAmountChanged("250"))
        viewModel.handleAction(MealEditorAction.ServingUnitSelected("g"))
        viewModel.handleAction(MealEditorAction.ServingEditorConfirmed)

        assertFalse(viewModel.uiState.value.isServingEditorVisible)
        assertEquals("250 g", viewModel.uiState.value.meal.servingsDescription)
    }

    @Test
    fun servingCountEditorChangesCountWithoutChangingServingSize() = runTest {
        val viewModel = createViewModel(FakeMealEditorRepository())

        viewModel.acceptItemResult(Json.encodeToString(sampleItem()))
        viewModel.handleAction(MealEditorAction.ServingCountEditorRequested)

        assertTrue(viewModel.uiState.value.isServingCountEditorVisible)
        assertEquals("1", viewModel.uiState.value.servingCountInput)

        viewModel.handleAction(MealEditorAction.ServingCountChanged("2.5"))
        viewModel.handleAction(MealEditorAction.ServingCountEditorConfirmed)

        assertFalse(viewModel.uiState.value.isServingCountEditorVisible)
        assertEquals(2.5, viewModel.uiState.value.meal.servingSize, 0.001)
        assertEquals("bowl", viewModel.uiState.value.meal.servingsDescription)
    }

    @Test
    fun mealNameChangesOnlyAfterEditorConfirmation() = runTest {
        val viewModel = createViewModel(FakeMealEditorRepository())

        viewModel.handleAction(MealEditorAction.NameChanged("Lunch"))
        viewModel.handleAction(MealEditorAction.EditNameRequested)
        viewModel.handleAction(MealEditorAction.NameEditorInputChanged("Post workout"))

        assertEquals("Lunch", viewModel.uiState.value.meal.name)
        assertTrue(viewModel.uiState.value.isNameEditorVisible)

        viewModel.handleAction(MealEditorAction.NameEditorConfirmed)

        assertEquals("Post workout", viewModel.uiState.value.meal.name)
        assertFalse(viewModel.uiState.value.isNameEditorVisible)
    }

    @Test
    fun liquidFoodServingEditorUsesLiquidUnits() = runTest {
        val viewModel = createViewModel(FakeMealEditorRepository())
        val milk = sampleItem().copy(name = "Milk", unit = "250 ml")

        viewModel.acceptItemResult(Json.encodeToString(milk))
        viewModel.handleAction(MealEditorAction.ServingEditorRequested)

        assertEquals("250", viewModel.uiState.value.servingAmountInput)
        assertEquals("ml", viewModel.uiState.value.servingUnitInput)
        assertTrue("L" in viewModel.uiState.value.servingUnitOptions)
        assertTrue("tsp" in viewModel.uiState.value.servingUnitOptions)
        assertTrue("glass" in viewModel.uiState.value.servingUnitOptions)
        assertFalse("kg" in viewModel.uiState.value.servingUnitOptions)
    }

    @Test
    fun datePickerRemainsVisibleWhileSelectionChanges() = runTest {
        val viewModel = createViewModel(FakeMealEditorRepository())
        val selectedDateTime = LocalDateTime.of(2026, 8, 8, 18, 30)

        viewModel.handleAction(MealEditorAction.LogDatePickerRequested)
        viewModel.handleAction(MealEditorAction.LogDateTimeChanged(selectedDateTime))

        assertTrue(viewModel.uiState.value.isLogDatePickerVisible)
        assertEquals(selectedDateTime, viewModel.uiState.value.logDateTime)

        viewModel.handleAction(MealEditorAction.LogDatePickerDismissed)

        assertFalse(viewModel.uiState.value.isLogDatePickerVisible)
    }

    @Test
    fun selectedDateTimeIsPassedToMealPersistence() = runTest {
        val repository = FakeMealEditorRepository()
        val viewModel = createViewModel(repository)
        val selectedDateTime = LocalDateTime.of(2026, 8, 8, 18, 30)

        viewModel.handleAction(MealEditorAction.NameChanged("Dinner"))
        viewModel.acceptItemResult(Json.encodeToString(sampleItem()))
        viewModel.handleAction(MealEditorAction.LogDateTimeChanged(selectedDateTime))
        viewModel.handleAction(MealEditorAction.SaveClicked)
        advanceUntilIdle()

        assertEquals(selectedDateTime, repository.savedDateTime)
    }

    @Test
    fun existingMealRestoresPersistedTimeInDeviceZone() = runTest {
        val loggedAt = Instant.parse("2026-08-08T13:00:00Z")
        val repository = FakeMealEditorRepository(
            loadedMeal = NutritionMeal(
                id = "meal-1",
                name = "Dinner",
                type = "DINNER",
                loggedAtEpochMillis = loggedAt.toEpochMilli(),
            ),
        )
        val viewModel = createViewModel(repository, mealId = "meal-1")

        advanceUntilIdle()

        assertEquals(
            loggedAt.atZone(ZoneId.systemDefault()).toLocalDateTime(),
            viewModel.uiState.value.logDateTime,
        )
    }

    @Test
    fun selectedPhotoIsUploadedOnlyWithMealSave() = runTest {
        val repository = FakeMealEditorRepository()
        val viewModel = createViewModel(repository)
        val photoBytes = byteArrayOf(1, 2, 3, 4)

        viewModel.handleAction(MealEditorAction.NameChanged("Lunch"))
        viewModel.acceptItemResult(Json.encodeToString(sampleItem()))
        viewModel.handleAction(MealEditorAction.PhotoClicked)
        assertTrue(viewModel.uiState.value.isPhotoSourceVisible)

        viewModel.handleAction(
            MealEditorAction.PhotoSelected(
                bytes = photoBytes,
                mimeType = "image/jpeg",
            )
        )

        assertTrue(viewModel.uiState.value.photoPreviewBytes?.contentEquals(photoBytes) == true)
        assertEquals(null, repository.savedPhotoUpdate)

        viewModel.handleAction(MealEditorAction.SaveClicked)
        advanceUntilIdle()

        val update = repository.savedPhotoUpdate as MealPhotoUpdate.Replace
        assertTrue(update.bytes.contentEquals(photoBytes))
        assertEquals("image/jpeg", update.mimeType)
    }

    @Test
    fun oversizedPhotoIsRejectedBeforeSave() = runTest {
        val repository = FakeMealEditorRepository()
        val viewModel = createViewModel(repository)

        viewModel.handleAction(
            MealEditorAction.PhotoSelected(
                bytes = ByteArray(10 * 1024 * 1024 + 1),
                mimeType = "image/jpeg",
            )
        )

        assertEquals("Meal photo is too large. Maximum size is 10 MB.", viewModel.uiState.value.errorMessage)
        assertEquals(null, viewModel.uiState.value.photoPreviewBytes)
        assertEquals(null, repository.savedPhotoUpdate)
    }

    private fun createViewModel(
        repository: NutritionRepository,
        mealId: String? = null,
    ): MealEditorViewModel {
        return MealEditorViewModel(
            savedStateHandle = SavedStateHandle(
                mapOf(
                    "mealId" to mealId,
                    "date" to "2026-08-09",
                ),
            ),
            nutritionRepository = repository,
        )
    }

    private fun sampleItem(): MealItem {
        return MealItem(
            id = "item-1",
            name = "Dal",
            calories = 180,
            protein = 9.0,
            quantity = 1.0,
            unit = "bowl",
            carbs = 28.0,
            fats = 4.0,
            fiber = 6.5,
            sugar = 1.2,
            transFat = 0.1,
            saturatedFat = 1.4,
            sodium = 320.0,
            cholesterol = 12.0,
            micronutrients = MicronutrientSnapshot(
                vitaminCMg = 4.5,
                ironMg = 2.4,
                potassiumMg = 410.0,
            ),
            servingSnapshot = ServingSnapshot(
                label = "1 bowl",
                amount = 1.0,
                unit = "bowl",
                grams = 180.0,
            ),
            rawInput = "dal",
            inputSource = "search",
            confidenceScore = 0.94,
            nutritionSnapshot = NutritionSnapshot(
                provider = "edamam",
                providerFoodId = "food-dal",
            ),
        )
    }
}

private class FakeMealEditorRepository(
    private val saveError: Throwable? = null,
    private val loadedMeal: NutritionMeal? = null,
) : NutritionRepository {
    var savedDateTime: LocalDateTime? = null
    var savedMeal: NutritionMeal? = null
    var savedPhotoUpdate: MealPhotoUpdate? = null

    override suspend fun getMealDiary(date: LocalDate): MealDiarySnapshot {
        error("Not needed for MealEditorViewModelTest")
    }

    override suspend fun getMealLog(mealId: String): NutritionMeal? = loadedMeal

    override suspend fun getNutritionTargets(): NutritionTargetsSnapshot {
        error("Not needed for MealEditorViewModelTest")
    }

    override suspend fun updateNutritionTargets(targets: NutritionTargetsSnapshot) = Unit

    override suspend fun saveMealLog(loggedAt: LocalDateTime, meal: NutritionMeal): NutritionMeal {
        savedDateTime = loggedAt
        savedMeal = meal
        saveError?.let { throw it }
        return meal.copy(id = meal.id.ifBlank { "meal-1" })
    }

    override suspend fun saveMealLogWithPhoto(
        loggedAt: LocalDateTime,
        meal: NutritionMeal,
        photoUpdate: MealPhotoUpdate,
    ): NutritionMeal {
        savedPhotoUpdate = photoUpdate
        return saveMealLog(loggedAt, meal)
    }

    override suspend fun deleteMealLog(mealId: String) = Unit
    override suspend fun saveMealLogItem(mealLogId: String, item: MealItem): MealItem = item
    override suspend fun updateMealLogItem(item: MealItem) = Unit
    override suspend fun deleteMealLogItem(itemId: String) = Unit
}
