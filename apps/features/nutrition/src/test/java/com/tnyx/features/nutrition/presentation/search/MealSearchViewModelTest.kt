package com.tnyx.features.nutrition.presentation.search

import androidx.lifecycle.SavedStateHandle
import com.tnyx.features.nutrition.domain.models.MealItem
import com.tnyx.features.nutrition.domain.repository.FoodSearchRepository
import com.tnyx.features.nutrition.presentation.meal_diary.MainDispatcherRule
import java.time.LocalDate
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MealSearchViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun barcodeRouteQuerySearchesImmediately() = runTest {
        val repository = FakeFoodSearchRepository(results = listOf(foodItem()))
        val viewModel = MealSearchViewModel(
            SavedStateHandle(
                mapOf(
                    "date" to "2026-08-09",
                    "initialQuery" to "8901234567890",
                ),
            ),
            repository,
        )

        advanceUntilIdle()

        assertEquals("8901234567890", viewModel.uiState.value.query)
        assertEquals(listOf("8901234567890"), repository.queries)
        assertTrue(viewModel.uiState.value.hasSearched)
    }

    @Test
    fun queryChangeLoadsLiveRepositoryResultsAfterDebounce() = runTest {
        val repository = FakeFoodSearchRepository(results = listOf(foodItem()))
        val viewModel = MealSearchViewModel(
            SavedStateHandle(mapOf("date" to "2026-08-09")),
            repository,
        )

        viewModel.handleAction(MealSearchAction.QueryChanged("paneer"))
        advanceUntilIdle()

        assertEquals(listOf("paneer"), repository.queries)
        assertEquals(LocalDate.of(2026, 8, 9), viewModel.uiState.value.date)
        assertEquals("Paneer", viewModel.uiState.value.searchResults.single().name)
        assertTrue(viewModel.uiState.value.hasSearched)
        assertFalse(viewModel.uiState.value.isSearching)
    }

    @Test
    fun shortQueryDoesNotCallRepository() = runTest {
        val repository = FakeFoodSearchRepository()
        val viewModel = MealSearchViewModel(SavedStateHandle(), repository)

        viewModel.handleAction(MealSearchAction.QueryChanged("p"))
        advanceUntilIdle()

        assertTrue(repository.queries.isEmpty())
        assertEquals(null, viewModel.uiState.value.errorMessage)
    }

    @Test
    fun repositoryFailureStaysRetryable() = runTest {
        val repository = FakeFoodSearchRepository(failure = true)
        val viewModel = MealSearchViewModel(SavedStateHandle(), repository)

        viewModel.handleAction(MealSearchAction.QueryChanged("dal"))
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.hasSearched)
        assertFalse(viewModel.uiState.value.isSearching)
        assertEquals(
            "Food search is unavailable. Check your connection and try again.",
            viewModel.uiState.value.errorMessage,
        )
    }

    @Test
    fun foodDatabaseGroupCanOpenAndReturnToOverview() = runTest {
        val viewModel = MealSearchViewModel(
            SavedStateHandle(),
            FakeFoodSearchRepository(),
        )

        viewModel.handleAction(
            MealSearchAction.GroupSelected(MealSearchGroup.FoodDatabase),
        )
        assertEquals(MealSearchGroup.FoodDatabase, viewModel.uiState.value.selectedGroup)

        viewModel.handleAction(MealSearchAction.GroupClosed)
        assertNull(viewModel.uiState.value.selectedGroup)
    }
}

private class FakeFoodSearchRepository(
    private val results: List<MealItem> = emptyList(),
    private val failure: Boolean = false,
) : FoodSearchRepository {
    val queries = mutableListOf<String>()

    override suspend fun search(query: String): List<MealItem> {
        queries += query
        if (failure) error("Unavailable")
        return results
    }

    override suspend fun lookupBarcode(barcode: String): MealItem? = results.firstOrNull()
}

private fun foodItem() = MealItem(
    id = "off:1",
    name = "Paneer",
    calories = 265,
    protein = 18.0,
    quantity = 1.0,
    unit = "100 g",
)
