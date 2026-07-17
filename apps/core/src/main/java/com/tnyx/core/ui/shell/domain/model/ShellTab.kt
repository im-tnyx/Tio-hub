package com.tnyx.core.ui.shell.domain.model

/**
 * Stable top-level destinations supported by the app shell.
 *
 * [stableId] is persisted. Display labels and icons remain UI concerns and may
 * change without migrating stored navigation preferences.
 */
enum class ShellTab(val stableId: String) {
    Home("home"),
    Nutrition("nutrition"),
    MealPlan("meal_plan"),
    Ai("ai"),
    Workout("workout"),
    WorkoutLibrary("workout_library"),
    Progress("progress"),
    You("you");

    companion object {
        fun fromStableId(stableId: String): ShellTab? {
            return entries.firstOrNull { it.stableId == stableId }
        }
    }
}

const val MIN_BOTTOM_NAV_TABS = 3
const val MAX_BOTTOM_NAV_TABS = 6

/**
 * Ordered catalog shown by Settings. New enum entries are not automatically
 * exposed until they are deliberately added here with a supported route.
 */
val BOTTOM_NAV_TAB_CATALOG: List<ShellTab> = listOf(
    ShellTab.Home,
    ShellTab.Nutrition,
    ShellTab.MealPlan,
    ShellTab.Ai,
    ShellTab.Workout,
    ShellTab.WorkoutLibrary,
    ShellTab.Progress,
    ShellTab.You,
)

val DEFAULT_BOTTOM_NAV_TABS: List<ShellTab> = listOf(
    ShellTab.Home,
    ShellTab.Nutrition,
    ShellTab.Ai,
    ShellTab.Workout,
    ShellTab.Progress,
)

enum class HomeExperienceMode {
    Nutrition,
    Workout,
    Balanced,
    Custom,
}

/**
 * Defines the high-level Home emphasis without moving feature business logic
 * into the app shell. Home remains a summary; owning tabs keep detailed flows.
 */
fun deriveHomeExperienceMode(tabs: Collection<ShellTab>): HomeExperienceMode {
    val hasNutrition = ShellTab.Nutrition in tabs || ShellTab.MealPlan in tabs
    val hasWorkout = ShellTab.Workout in tabs || ShellTab.WorkoutLibrary in tabs

    return when {
        hasNutrition && hasWorkout -> HomeExperienceMode.Balanced
        hasNutrition -> HomeExperienceMode.Nutrition
        hasWorkout -> HomeExperienceMode.Workout
        else -> HomeExperienceMode.Custom
    }
}

/**
 * Produces a safe renderable tab list while preserving the user's valid order.
 */
fun normalizeBottomNavTabs(
    tabs: List<ShellTab>,
    availableTabs: Set<ShellTab> = BOTTOM_NAV_TAB_CATALOG.toSet(),
): List<ShellTab> {
    val allowedTabs = availableTabs + ShellTab.Home
    val normalized = mutableListOf(ShellTab.Home)

    tabs.asSequence()
        .filter { it != ShellTab.Home && it in allowedTabs }
        .distinct()
        .take(MAX_BOTTOM_NAV_TABS - 1)
        .forEach(normalized::add)

    if (normalized.size < MIN_BOTTOM_NAV_TABS) {
        DEFAULT_BOTTOM_NAV_TABS.asSequence()
            .filter { it in allowedTabs && it !in normalized }
            .forEach { defaultTab ->
                if (normalized.size < MIN_BOTTOM_NAV_TABS) {
                    normalized += defaultTab
                }
            }
    }

    if (normalized.size >= MIN_BOTTOM_NAV_TABS) {
        return normalized
    }

    return DEFAULT_BOTTOM_NAV_TABS
        .filter { it in allowedTabs }
        .take(MAX_BOTTOM_NAV_TABS)
}
