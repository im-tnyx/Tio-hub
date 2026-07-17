package com.tnyx.core.ui.shell.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ShellTabTest {

    @Test
    fun `default tabs keep Tio in the center position`() {
        assertEquals(
            listOf(
                ShellTab.Home,
                ShellTab.Nutrition,
                ShellTab.Ai,
                ShellTab.Workout,
                ShellTab.Progress,
            ),
            DEFAULT_BOTTOM_NAV_TABS,
        )
    }

    @Test
    fun `normalization keeps home first and removes duplicates`() {
        val result = normalizeBottomNavTabs(
            listOf(
                ShellTab.Workout,
                ShellTab.Home,
                ShellTab.Workout,
                ShellTab.Nutrition,
            )
        )

        assertEquals(
            listOf(
                ShellTab.Home,
                ShellTab.Workout,
                ShellTab.Nutrition,
            ),
            result,
        )
    }

    @Test
    fun `normalization restores minimum valid tab count`() {
        val result = normalizeBottomNavTabs(listOf(ShellTab.Home))

        assertEquals(
            listOf(
                ShellTab.Home,
                ShellTab.Nutrition,
                ShellTab.Ai,
            ),
            result,
        )
    }

    @Test
    fun `normalization removes unavailable destinations`() {
        val result = normalizeBottomNavTabs(
            tabs = DEFAULT_BOTTOM_NAV_TABS,
            availableTabs = setOf(
                ShellTab.Home,
                ShellTab.Nutrition,
                ShellTab.Workout,
            ),
        )

        assertEquals(
            listOf(
                ShellTab.Home,
                ShellTab.Nutrition,
                ShellTab.Workout,
            ),
            result,
        )
    }

    @Test
    fun `stable ids decode without depending on display labels`() {
        assertEquals(ShellTab.Ai, ShellTab.fromStableId("ai"))
        assertNull(ShellTab.fromStableId("Tio"))
    }
}
