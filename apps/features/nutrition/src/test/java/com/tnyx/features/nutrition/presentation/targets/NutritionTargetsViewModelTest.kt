package com.tnyx.features.nutrition.presentation.targets

import com.tnyx.features.nutrition.domain.models.MealDiarySnapshot
import com.tnyx.features.nutrition.domain.models.MealItem
import com.tnyx.features.nutrition.domain.models.NutritionMeal
import com.tnyx.features.nutrition.domain.models.NutritionTargetsSnapshot
import com.tnyx.features.nutrition.domain.repository.NutritionRepository
import com.tnyx.features.nutrition.presentation.meal_diary.MainDispatcherRule
import java.time.LocalDate
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NutritionTargetsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun initLoadsTargetsFromRepository() = runTest {
        val repository = FakeNutritionTargetsRepository(
            initialTargets = sampleTargets(),
        )

        val viewModel = NutritionTargetsViewModel(repository)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, repository.getTargetsCallCount)
        assertFalse(state.isLoading)
        assertEquals(2050, state.caloriesTarget)
        assertEquals(155.0, state.proteinTarget, 0.0)
        assertEquals("11:15 PM", state.formattedSleepTime)
        assertEquals("07:15 AM", state.formattedWakeTime)
    }

    @Test
    fun editSavePersistsRepositoryAndUpdatesUiState() = runTest {
        val repository = FakeNutritionTargetsRepository(
            initialTargets = sampleTargets(),
        )
        val viewModel = NutritionTargetsViewModel(repository)
        advanceUntilIdle()

        viewModel.handleAction(NutritionTargetsAction.EditTargetClicked(NutritionTargetField.Protein))
        viewModel.handleAction(NutritionTargetsAction.EditValueChanged("165"))
        viewModel.handleAction(NutritionTargetsAction.EditSaved)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        val savedTargets = repository.lastUpdatedTargets

        assertFalse(state.isSaving)
        assertEquals(165.0, state.proteinTarget, 0.0)
        assertEquals("Updated just now", state.lastUpdatedLabel)
        assertEquals(165.0, savedTargets?.proteinTarget ?: 0.0, 0.0)
    }

    @Test
    fun dynamicTogglePersistsRepository() = runTest {
        val repository = FakeNutritionTargetsRepository(
            initialTargets = sampleTargets().copy(dynamicCaloriesEnabled = false),
        )
        val viewModel = NutritionTargetsViewModel(repository)
        advanceUntilIdle()

        viewModel.handleAction(NutritionTargetsAction.DynamicCaloriesChanged(true))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.dynamicCaloriesEnabled)
        assertTrue(repository.lastUpdatedTargets?.dynamicCaloriesEnabled == true)
    }

    private fun sampleTargets(): NutritionTargetsSnapshot {
        return NutritionTargetsSnapshot(
            dynamicCaloriesEnabled = true,
            caloriesTarget = 2050,
            calorieSurplusTarget = 180,
            proteinTarget = 155.0,
            carbsTarget = 225.0,
            fatTarget = 68.0,
            fiberTarget = 32.0,
            waterTargetLitres = 3.1,
            glassSizeMl = 300,
            stepsTarget = 9000,
            targetWeight = 72.5,
            sleepTargetHours = "8",
            formattedSleepTime = "11:15 PM",
            formattedWakeTime = "07:15 AM",
        )
    }
}

private class FakeNutritionTargetsRepository(
    initialTargets: NutritionTargetsSnapshot,
) : NutritionRepository {
    private var currentTargets: NutritionTargetsSnapshot = initialTargets

    var getTargetsCallCount: Int = 0
        private set

    var lastUpdatedTargets: NutritionTargetsSnapshot? = null
        private set

    override suspend fun getMealDiary(date: LocalDate): MealDiarySnapshot {
        error("Not needed for NutritionTargetsViewModelTest")
    }

    override suspend fun getNutritionTargets(): NutritionTargetsSnapshot {
        getTargetsCallCount += 1
        return currentTargets
    }

    override suspend fun updateNutritionTargets(targets: NutritionTargetsSnapshot) {
        currentTargets = targets
        lastUpdatedTargets = targets
    }

    override suspend fun saveMealLog(date: LocalDate, meal: NutritionMeal): NutritionMeal = meal
    override suspend fun deleteMealLog(mealId: String) {}
    override suspend fun saveMealLogItem(mealLogId: String, item: MealItem): MealItem = item
    override suspend fun updateMealLogItem(item: MealItem) {}
    override suspend fun deleteMealLogItem(itemId: String) {}
}

