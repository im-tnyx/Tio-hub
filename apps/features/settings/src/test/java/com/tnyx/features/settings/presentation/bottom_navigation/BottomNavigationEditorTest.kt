package com.tnyx.features.settings.presentation.bottom_navigation

import androidx.compose.ui.geometry.Rect
import com.tnyx.core.ui.shell.domain.model.DEFAULT_BOTTOM_NAV_TABS
import com.tnyx.core.ui.shell.domain.model.ShellTab
import org.junit.Assert.assertEquals
import org.junit.Test

class BottomNavigationEditorTest {

    @Test
    fun `home cannot be disabled`() {
        val tabs = listOf(
            ShellTab.Home,
            ShellTab.Nutrition,
            ShellTab.Ai,
            ShellTab.Workout,
        )

        assertEquals(tabs, toggleBottomNavigationTab(tabs, ShellTab.Home))
    }

    @Test
    fun `tab cannot be disabled below minimum`() {
        val tabs = listOf(
            ShellTab.Home,
            ShellTab.Nutrition,
            ShellTab.Ai,
        )

        assertEquals(tabs, toggleBottomNavigationTab(tabs, ShellTab.Ai))
    }

    @Test
    fun `disabled tab is appended when enabled`() {
        val tabs = listOf(
            ShellTab.Home,
            ShellTab.Nutrition,
            ShellTab.Workout,
        )

        assertEquals(
            listOf(
                ShellTab.Home,
                ShellTab.Nutrition,
                ShellTab.Workout,
                ShellTab.Progress,
            ),
            toggleBottomNavigationTab(tabs, ShellTab.Progress),
        )
    }

    @Test
    fun `all supported tabs remain visible regardless of current selection`() {
        val state = BottomNavigationUiState(
            savedTabs = DEFAULT_BOTTOM_NAV_TABS,
            draftTabs = DEFAULT_BOTTOM_NAV_TABS,
            isLoading = false,
        )

        assertEquals(
            listOf(
                ShellTab.Home,
                ShellTab.Nutrition,
                ShellTab.MealPlan,
                ShellTab.Ai,
                ShellTab.Workout,
                ShellTab.WorkoutLibrary,
                ShellTab.Progress,
                ShellTab.You,
            ),
            state.supportedTabs,
        )
    }

    @Test
    fun `tab moves up without crossing home`() {
        val tabs = listOf(
            ShellTab.Home,
            ShellTab.Nutrition,
            ShellTab.Ai,
            ShellTab.Workout,
        )

        assertEquals(
            listOf(
                ShellTab.Home,
                ShellTab.Ai,
                ShellTab.Nutrition,
                ShellTab.Workout,
            ),
            moveBottomNavigationTab(tabs, ShellTab.Ai, targetIndex = 1),
        )
        assertEquals(
            tabs,
            moveBottomNavigationTab(tabs, ShellTab.Nutrition, targetIndex = 0),
        )
    }

    @Test
    fun `tab moves down within visible list`() {
        val tabs = listOf(
            ShellTab.Home,
            ShellTab.Nutrition,
            ShellTab.Ai,
            ShellTab.Workout,
        )

        assertEquals(
            listOf(
                ShellTab.Home,
                ShellTab.Ai,
                ShellTab.Nutrition,
                ShellTab.Workout,
            ),
            moveBottomNavigationTab(tabs, ShellTab.Nutrition, targetIndex = 2),
        )
        assertEquals(
            tabs,
            moveBottomNavigationTab(tabs, ShellTab.Workout, targetIndex = 99),
        )
    }

    @Test
    fun `tab can move across multiple preview positions`() {
        val tabs = listOf(
            ShellTab.Home,
            ShellTab.Nutrition,
            ShellTab.Ai,
            ShellTab.Workout,
            ShellTab.Progress,
        )

        assertEquals(
            listOf(
                ShellTab.Home,
                ShellTab.Progress,
                ShellTab.Nutrition,
                ShellTab.Ai,
                ShellTab.Workout,
            ),
            moveBottomNavigationTab(tabs, ShellTab.Progress, targetIndex = 1),
        )
    }

    @Test
    fun `known presets resolve regardless of manual order`() {
        assertEquals(
            BottomNavigationMode.Hybrid,
            resolveBottomNavigationMode(DEFAULT_BOTTOM_NAV_TABS),
        )
        assertEquals(
            BottomNavigationMode.Workout,
            resolveBottomNavigationMode(BottomNavigationMode.Workout.presetTabs.orEmpty()),
        )
        assertEquals(
            BottomNavigationMode.Hybrid,
            resolveBottomNavigationMode(
                listOf(
                    ShellTab.Home,
                    ShellTab.Ai,
                    ShellTab.Nutrition,
                    ShellTab.Workout,
                    ShellTab.Progress,
                )
            ),
        )
    }

    @Test
    fun `adding or removing a preset icon becomes custom`() {
        assertEquals(
            BottomNavigationMode.Custom,
            resolveBottomNavigationMode(
                DEFAULT_BOTTOM_NAV_TABS + ShellTab.You
            ),
        )
        assertEquals(
            BottomNavigationMode.Custom,
            resolveBottomNavigationMode(
                DEFAULT_BOTTOM_NAV_TABS - ShellTab.Progress
            ),
        )
    }

    @Test
    fun `tio is centered in every selectable preset`() {
        val presets = listOf(
            BottomNavigationMode.Workout,
            BottomNavigationMode.Nutrition,
            BottomNavigationMode.Hybrid,
        )

        presets.forEach { mode ->
            val tabs = mode.presetTabs.orEmpty()
            assertEquals(tabs.size / 2, tabs.indexOf(ShellTab.Ai))
        }
    }

    @Test
    fun `custom mode keeps current draft while preset replaces it`() {
        val tabs = listOf(ShellTab.Home, ShellTab.Nutrition, ShellTab.Ai)

        assertEquals(tabs, applyBottomNavigationMode(tabs, BottomNavigationMode.Custom))
        assertEquals(
            BottomNavigationMode.Nutrition.presetTabs,
            applyBottomNavigationMode(tabs, BottomNavigationMode.Nutrition),
        )
    }

    @Test
    fun `available tab can be inserted at preview drop position`() {
        val tabs = listOf(
            ShellTab.Home,
            ShellTab.Nutrition,
            ShellTab.Ai,
            ShellTab.Workout,
            ShellTab.Progress,
        )

        assertEquals(
            listOf(
                ShellTab.Home,
                ShellTab.Nutrition,
                ShellTab.WorkoutLibrary,
                ShellTab.Ai,
                ShellTab.Workout,
                ShellTab.Progress,
            ),
            addBottomNavigationTab(
                tabs = tabs,
                tab = ShellTab.WorkoutLibrary,
                targetIndex = 2,
            ),
        )
    }

    @Test
    fun `home duplicate and seventh tab cannot be dropped into preview`() {
        val fullTabs = listOf(
            ShellTab.Home,
            ShellTab.Nutrition,
            ShellTab.MealPlan,
            ShellTab.Ai,
            ShellTab.Workout,
            ShellTab.Progress,
        )

        assertEquals(
            fullTabs,
            addBottomNavigationTab(fullTabs, ShellTab.Home, targetIndex = 3),
        )
        assertEquals(
            fullTabs,
            addBottomNavigationTab(fullTabs, ShellTab.You, targetIndex = 3),
        )
    }

    @Test
    fun `preview drop position keeps home protected`() {
        val bounds = Rect(
            left = 100f,
            top = 0f,
            right = 600f,
            bottom = 100f,
        )

        assertEquals(1, previewDropIndex(bounds, dropX = 100f, currentTabCount = 5))
        assertEquals(3, previewDropIndex(bounds, dropX = 350f, currentTabCount = 5))
        assertEquals(5, previewDropIndex(bounds, dropX = 600f, currentTabCount = 5))
    }
}
