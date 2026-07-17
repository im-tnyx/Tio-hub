package com.tnyx.features.settings.presentation.bottom_navigation

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
            moveBottomNavigationTab(tabs, ShellTab.Ai, offset = -1),
        )
        assertEquals(
            tabs,
            moveBottomNavigationTab(tabs, ShellTab.Nutrition, offset = -1),
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
            moveBottomNavigationTab(tabs, ShellTab.Nutrition, offset = 1),
        )
        assertEquals(
            tabs,
            moveBottomNavigationTab(tabs, ShellTab.Workout, offset = 1),
        )
    }
}
