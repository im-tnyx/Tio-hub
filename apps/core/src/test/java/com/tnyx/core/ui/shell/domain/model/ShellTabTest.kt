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
    fun `optional catalog includes meal plan library and you without changing defaults`() {
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
            BOTTOM_NAV_TAB_CATALOG,
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
    fun `normalization caps optional configuration at six tabs`() {
        val result = normalizeBottomNavTabs(BOTTOM_NAV_TAB_CATALOG)

        assertEquals(
            listOf(
                ShellTab.Home,
                ShellTab.Nutrition,
                ShellTab.MealPlan,
                ShellTab.Ai,
                ShellTab.Workout,
                ShellTab.WorkoutLibrary,
            ),
            result,
        )
    }

    @Test
    fun `normalization removes unavailable destinations`() {
        val result = normalizeBottomNavTabs(
            tabs = listOf(
                ShellTab.Home,
                ShellTab.MealPlan,
                ShellTab.Nutrition,
                ShellTab.You,
            ),
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
        assertEquals(ShellTab.MealPlan, ShellTab.fromStableId("meal_plan"))
        assertEquals(ShellTab.WorkoutLibrary, ShellTab.fromStableId("workout_library"))
        assertEquals(ShellTab.You, ShellTab.fromStableId("you"))
        assertNull(ShellTab.fromStableId("Tio"))
        assertNull(ShellTab.fromStableId("Library"))
    }

    @Test
    fun `home mode follows enabled nutrition and workout domains`() {
        assertEquals(
            HomeExperienceMode.Nutrition,
            deriveHomeExperienceMode(
                listOf(ShellTab.Home, ShellTab.Nutrition, ShellTab.MealPlan),
            ),
        )
        assertEquals(
            HomeExperienceMode.Workout,
            deriveHomeExperienceMode(
                listOf(ShellTab.Home, ShellTab.Workout, ShellTab.WorkoutLibrary),
            ),
        )
        assertEquals(
            HomeExperienceMode.Balanced,
            deriveHomeExperienceMode(DEFAULT_BOTTOM_NAV_TABS),
        )
        assertEquals(
            HomeExperienceMode.Custom,
            deriveHomeExperienceMode(
                listOf(ShellTab.Home, ShellTab.Ai, ShellTab.You),
            ),
        )
    }
}
