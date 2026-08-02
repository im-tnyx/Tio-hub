package com.tnyx.features.settings.presentation.bottom_navigation

import com.tnyx.core.ui.shell.domain.model.DEFAULT_BOTTOM_NAV_TABS
import com.tnyx.core.ui.shell.domain.model.ShellTab
import com.tnyx.core.ui.shell.domain.repository.BottomNavPreferencesRepository
import com.tnyx.features.settings.presentation.personal_info.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BottomNavigationViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `preset updates draft and back asks before discarding`() = runTest {
        val viewModel = BottomNavigationViewModel(TestBottomNavPreferencesRepository())
        advanceUntilIdle()

        viewModel.handleAction(
            BottomNavigationAction.ApplyMode(BottomNavigationMode.Workout)
        )
        viewModel.handleAction(BottomNavigationAction.BackClicked)

        assertEquals(
            BottomNavigationMode.Workout.presetTabs,
            viewModel.uiState.value.draftTabs,
        )
        assertTrue(viewModel.uiState.value.showDiscardDialog)

        viewModel.handleAction(BottomNavigationAction.KeepEditingClicked)

        assertFalse(viewModel.uiState.value.showDiscardDialog)
        assertTrue(viewModel.uiState.value.hasUnsavedChanges)
    }

    @Test
    fun `save persists normalized preset and clears unsaved state`() = runTest {
        val repository = TestBottomNavPreferencesRepository()
        val viewModel = BottomNavigationViewModel(repository)
        advanceUntilIdle()

        viewModel.handleAction(
            BottomNavigationAction.ApplyMode(BottomNavigationMode.Nutrition)
        )
        viewModel.handleAction(BottomNavigationAction.SaveClicked)
        advanceUntilIdle()

        assertEquals(BottomNavigationMode.Nutrition.presetTabs, repository.savedTabs)
        assertEquals(repository.savedTabs, viewModel.uiState.value.savedTabs)
        assertFalse(viewModel.uiState.value.hasUnsavedChanges)
        assertFalse(viewModel.uiState.value.isSaving)
    }
}

private class TestBottomNavPreferencesRepository(
    initialTabs: List<ShellTab> = DEFAULT_BOTTOM_NAV_TABS,
) : BottomNavPreferencesRepository {
    private val storedTabs = MutableStateFlow(initialTabs)
    var savedTabs: List<ShellTab>? = null

    override val tabs: Flow<List<ShellTab>> = storedTabs

    override suspend fun saveTabs(tabs: List<ShellTab>) {
        savedTabs = tabs
        storedTabs.value = tabs
    }

    override suspend fun resetToDefault() {
        savedTabs = DEFAULT_BOTTOM_NAV_TABS
        storedTabs.value = DEFAULT_BOTTOM_NAV_TABS
    }
}
