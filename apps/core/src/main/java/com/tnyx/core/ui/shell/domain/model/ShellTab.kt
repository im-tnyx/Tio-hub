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
    Ai("ai"),
    Workout("workout"),
    Progress("progress");

    companion object {
        fun fromStableId(stableId: String): ShellTab? {
            return entries.firstOrNull { it.stableId == stableId }
        }
    }
}

const val MIN_BOTTOM_NAV_TABS = 3
const val MAX_BOTTOM_NAV_TABS = 6

val DEFAULT_BOTTOM_NAV_TABS: List<ShellTab> = listOf(
    ShellTab.Home,
    ShellTab.Nutrition,
    ShellTab.Ai,
    ShellTab.Workout,
    ShellTab.Progress,
)

/**
 * Produces a safe renderable tab list while preserving the user's valid order.
 */
fun normalizeBottomNavTabs(
    tabs: List<ShellTab>,
    availableTabs: Set<ShellTab> = ShellTab.entries.toSet(),
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
