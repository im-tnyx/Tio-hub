package com.tnyx.features.nutrition.presentation.meal_diary

import com.tnyx.features.nutrition.domain.models.MealDiarySnapshot
import com.tnyx.features.nutrition.domain.models.MealItem
import com.tnyx.features.nutrition.domain.models.NutritionMeal
import com.tnyx.features.nutrition.domain.models.NutritionTargetsSnapshot
import com.tnyx.features.nutrition.domain.repository.NutritionRepository
import java.time.LocalDate
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MealDiaryViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun initLoadsRepositoryBackedDiary() = runTest {
        val selectedDate = LocalDate.of(2026, 7, 30)
        val repository = FakeNutritionRepository(
            snapshots = mapOf(selectedDate to snapshot(selectedDate)),
        )

        val viewModel = MealDiaryViewModel(repository, selectedDate)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(selectedDate, repository.requestedDates.single())
        assertEquals(selectedDate, state.selectedDate)
        assertEquals(355, state.caloriesConsumed)
        assertEquals(1700, state.caloriesGoal)
        assertEquals(16.5, state.proteinConsumed, 0.0)
        assertEquals(130.0, state.proteinGoal, 0.0)
        assertEquals(6.0, state.fiberConsumed, 0.0)
        assertEquals(40.0, state.carbsConsumed, 0.0)
        assertEquals(8.0, state.sugarConsumed, 0.0)
        assertEquals(12.0, state.fatsConsumed, 0.0)
        assertEquals(2.2, state.waterConsumed, 0.0)
        assertEquals(3.4, state.waterGoal, 0.0)
        assertFalse(state.isLoading)
    }

    @Test
    fun dateSelectionReloadsDiaryFromRepository() = runTest {
        val initialDate = LocalDate.of(2026, 7, 30)
        val nextDate = initialDate.minusDays(1)
        val repository = FakeNutritionRepository(
            snapshots = mapOf(
                initialDate to snapshot(initialDate, mealName = "Initial Meal"),
                nextDate to snapshot(nextDate, mealName = "Next Meal"),
            ),
        )

        val viewModel = MealDiaryViewModel(repository, initialDate)
        advanceUntilIdle()

        viewModel.handleAction(MealDiaryAction.DateSelected(nextDate))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(listOf(initialDate, nextDate), repository.requestedDates)
        assertEquals(nextDate, state.selectedDate)
        assertEquals("Next Meal", state.meals.single().name)
        assertFalse(state.isLoading)
    }

    @Test
    fun repositoryFailureIsNotRenderedAsEmptyDiary() = runTest {
        val selectedDate = LocalDate.of(2026, 8, 9)
        val repository = FakeNutritionRepository(snapshots = emptyMap())

        val viewModel = MealDiaryViewModel(repository, selectedDate)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoading)
        assertNotNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun cameraFabNavigatesToDedicatedCameraForSelectedDate() = runTest {
        val selectedDate = LocalDate.of(2026, 8, 10)
        val viewModel = MealDiaryViewModel(
            FakeNutritionRepository(mapOf(selectedDate to snapshot(selectedDate))),
            selectedDate,
        )
        advanceUntilIdle()
        viewModel.handleAction(MealDiaryAction.FabToggled)
        val effect = async(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.effect.first()
        }

        viewModel.handleAction(MealDiaryAction.AddMealCameraClicked)
        advanceUntilIdle()

        assertEquals(
            MealDiaryEffect.NavigateToMealCamera(selectedDate),
            effect.await(),
        )
        assertFalse(viewModel.uiState.value.isFabExpanded)
    }

    private fun snapshot(
        date: LocalDate,
        mealName: String = "Repository Meal",
    ): MealDiarySnapshot {
        return MealDiarySnapshot(
            selectedDate = date,
            hasDietPlan = true,
            caloriesGoal = 1700,
            proteinGoal = 130.0,
            fiberGoal = 28.0,
            carbsGoal = 180.0,
            sugarGoal = 35.0,
            fatsGoal = 45.0,
            waterConsumedLiters = 2.2,
            waterGoalLiters = 3.4,
            vitaminsProgress = 0.63,
            mineralsProgress = 0.51,
            meals = listOf(
                NutritionMeal(
                    id = "meal-${date}",
                    name = mealName,
                    type = "LUNCH",
                    items = listOf(
                        MealItem(
                            id = "item-${date}",
                            name = "Seeded Item",
                            calories = 355,
                            protein = 16.5,
                            quantity = 1.0,
                            unit = "plate",
                            carbs = 40.0,
                            fats = 12.0,
                            fiber = 6.0,
                            sugar = 8.0,
                        ),
                    ),
                ),
            ),
        )
    }
}

private class FakeNutritionRepository(
    private val snapshots: Map<LocalDate, MealDiarySnapshot>,
) : NutritionRepository {
    val requestedDates = mutableListOf<LocalDate>()

    override suspend fun getMealDiary(date: LocalDate): MealDiarySnapshot {
        requestedDates += date
        return requireNotNull(snapshots[date]) {
            "Missing test snapshot for $date"
        }
    }

    override suspend fun getMealLog(mealId: String): NutritionMeal? = null

    override suspend fun getNutritionTargets(): NutritionTargetsSnapshot {
        error("Not needed for MealDiaryViewModelTest")
    }

    override suspend fun updateNutritionTargets(targets: NutritionTargetsSnapshot) {
        error("Not needed for MealDiaryViewModelTest")
    }

    override suspend fun saveMealLog(date: LocalDate, meal: NutritionMeal): NutritionMeal = meal
    override suspend fun deleteMealLog(mealId: String) {}
    override suspend fun saveMealLogItem(mealLogId: String, item: MealItem): MealItem = item
    override suspend fun updateMealLogItem(item: MealItem) {}
    override suspend fun deleteMealLogItem(itemId: String) {}
}

