package com.tnyx.features.nutrition.presentation.meal_item_editor

import androidx.lifecycle.SavedStateHandle
import com.tnyx.features.nutrition.presentation.meal_diary.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MealItemEditorViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun blankItemShowsValidationInsteadOfClosing() = runTest {
        val viewModel = createViewModel()

        viewModel.handleAction(MealItemEditorAction.SaveClicked)

        assertEquals("Enter a food item name.", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun validTextItemReturnsDraftToMealEditor() = runTest {
        val viewModel = createViewModel()
        val effect = async(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.effect.first()
        }

        viewModel.handleAction(MealItemEditorAction.NameEditorRequested)
        viewModel.handleAction(MealItemEditorAction.NameEditorInputChanged("Paneer tikka"))
        viewModel.handleAction(MealItemEditorAction.NameEditorConfirmed)
        viewModel.handleAction(MealItemEditorAction.QuantityChanged(2.0))
        viewModel.handleAction(MealItemEditorAction.UnitChanged("piece"))
        viewModel.handleAction(MealItemEditorAction.NutrientChanged("calories", 120.0))
        viewModel.handleAction(MealItemEditorAction.SaveClicked)

        val saved = effect.await() as MealItemEditorEffect.ItemSaved
        assertTrue(saved.item.id.isNotBlank())
        assertEquals("Paneer tikka", saved.item.name)
        assertEquals(2.0, saved.item.quantity, 0.0)
        assertEquals(120, saved.item.calories)
    }

    @Test
    fun nameEditorAppliesOnlyAfterConfirmation() = runTest {
        val viewModel = createViewModel()

        viewModel.handleAction(MealItemEditorAction.NameEditorRequested)
        viewModel.handleAction(MealItemEditorAction.NameEditorInputChanged("Potato"))

        assertEquals("", viewModel.uiState.value.item.name)

        viewModel.handleAction(MealItemEditorAction.NameEditorConfirmed)

        assertEquals("Potato", viewModel.uiState.value.item.name)
        assertTrue(!viewModel.uiState.value.isNameEditorVisible)
    }

    @Test
    fun micronutrientsCanBeExpandedAndUpdated() = runTest {
        val viewModel = createViewModel()

        viewModel.handleAction(MealItemEditorAction.MicronutrientsToggled)
        viewModel.handleAction(MealItemEditorAction.NutrientChanged("saturatedFat", 1.5))
        viewModel.handleAction(MealItemEditorAction.NutrientChanged("transFat", 0.2))

        assertTrue(viewModel.uiState.value.isMicronutrientsExpanded)
        assertEquals(1.5, viewModel.uiState.value.item.saturatedFat, 0.0)
        assertEquals(0.2, viewModel.uiState.value.item.transFat, 0.0)
    }

    private fun createViewModel(): MealItemEditorViewModel {
        return MealItemEditorViewModel(
            SavedStateHandle(mapOf("itemJson" to null)),
        )
    }
}
