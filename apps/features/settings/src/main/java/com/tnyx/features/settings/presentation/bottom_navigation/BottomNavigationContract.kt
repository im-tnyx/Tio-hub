package com.tnyx.features.settings.presentation.bottom_navigation

import androidx.compose.runtime.Immutable
import com.tnyx.core.ui.shell.domain.model.BOTTOM_NAV_TAB_CATALOG
import com.tnyx.core.ui.shell.domain.model.DEFAULT_BOTTOM_NAV_TABS
import com.tnyx.core.ui.shell.domain.model.MAX_BOTTOM_NAV_TABS
import com.tnyx.core.ui.shell.domain.model.MIN_BOTTOM_NAV_TABS
import com.tnyx.core.ui.shell.domain.model.ShellTab

@Immutable
data class BottomNavigationUiState(
    val savedTabs: List<ShellTab> = DEFAULT_BOTTOM_NAV_TABS,
    val draftTabs: List<ShellTab> = DEFAULT_BOTTOM_NAV_TABS,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val showDiscardDialog: Boolean = false,
    val errorMessage: String? = null,
) {
    val hasUnsavedChanges: Boolean
        get() = draftTabs != savedTabs

    val canSave: Boolean
        get() = !isLoading &&
            !isSaving &&
            hasUnsavedChanges &&
            draftTabs.size in MIN_BOTTOM_NAV_TABS..MAX_BOTTOM_NAV_TABS

    val canReset: Boolean
        get() = !isLoading && !isSaving && draftTabs != DEFAULT_BOTTOM_NAV_TABS

    val supportedTabs: List<ShellTab>
        get() = BOTTOM_NAV_TAB_CATALOG

    val selectedMode: BottomNavigationMode
        get() = resolveBottomNavigationMode(draftTabs)
}

sealed interface BottomNavigationAction {
    data object BackClicked : BottomNavigationAction
    data object KeepEditingClicked : BottomNavigationAction
    data object DiscardChangesClicked : BottomNavigationAction
    data class ApplyMode(val mode: BottomNavigationMode) : BottomNavigationAction
    data class ToggleTab(val tab: ShellTab) : BottomNavigationAction
    data class AddTab(val tab: ShellTab, val targetIndex: Int) : BottomNavigationAction
    data class MoveTab(val tab: ShellTab, val targetIndex: Int) : BottomNavigationAction
    data object ResetClicked : BottomNavigationAction
    data object SaveClicked : BottomNavigationAction
    data object DismissError : BottomNavigationAction
}

sealed interface BottomNavigationEffect {
    data object Saved : BottomNavigationEffect
    data object NavigateBack : BottomNavigationEffect
}

enum class BottomNavigationMode(
    val presetTabs: List<ShellTab>?,
) {
    Workout(
        listOf(
            ShellTab.Home,
            ShellTab.Workout,
            ShellTab.Ai,
            ShellTab.WorkoutLibrary,
            ShellTab.Progress,
        )
    ),
    Nutrition(
        listOf(
            ShellTab.Home,
            ShellTab.Nutrition,
            ShellTab.Ai,
            ShellTab.MealPlan,
            ShellTab.Progress,
        )
    ),
    Hybrid(DEFAULT_BOTTOM_NAV_TABS),
    Custom(null),
}

internal fun resolveBottomNavigationMode(tabs: List<ShellTab>): BottomNavigationMode {
    return BottomNavigationMode.entries.firstOrNull { mode ->
        mode.presetTabs?.toSet() == tabs.toSet()
    } ?: BottomNavigationMode.Custom
}

internal fun applyBottomNavigationMode(
    tabs: List<ShellTab>,
    mode: BottomNavigationMode,
): List<ShellTab> = mode.presetTabs ?: tabs

internal fun toggleBottomNavigationTab(
    tabs: List<ShellTab>,
    tab: ShellTab,
): List<ShellTab> {
    if (tab == ShellTab.Home) return tabs

    return if (tab in tabs) {
        if (tabs.size <= MIN_BOTTOM_NAV_TABS) tabs else tabs - tab
    } else {
        if (tabs.size >= MAX_BOTTOM_NAV_TABS) tabs else tabs + tab
    }
}

internal fun moveBottomNavigationTab(
    tabs: List<ShellTab>,
    tab: ShellTab,
    targetIndex: Int,
): List<ShellTab> {
    if (tab == ShellTab.Home) return tabs

    val currentIndex = tabs.indexOf(tab)
    if (currentIndex <= 0) return tabs

    val resolvedTargetIndex = targetIndex.coerceIn(1, tabs.lastIndex)
    if (resolvedTargetIndex == currentIndex) return tabs

    return tabs.toMutableList().apply {
        removeAt(currentIndex)
        add(resolvedTargetIndex, tab)
    }
}

internal fun addBottomNavigationTab(
    tabs: List<ShellTab>,
    tab: ShellTab,
    targetIndex: Int,
): List<ShellTab> {
    if (tab == ShellTab.Home || tab in tabs || tabs.size >= MAX_BOTTOM_NAV_TABS) {
        return tabs
    }

    val resolvedTargetIndex = targetIndex.coerceIn(1, tabs.size)
    return tabs.toMutableList().apply {
        add(resolvedTargetIndex, tab)
    }
}
