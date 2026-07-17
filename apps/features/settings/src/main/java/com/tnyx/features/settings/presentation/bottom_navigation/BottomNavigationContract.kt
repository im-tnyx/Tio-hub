package com.tnyx.features.settings.presentation.bottom_navigation

import androidx.compose.runtime.Immutable
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
    val errorMessage: String? = null,
) {
    val canSave: Boolean
        get() = !isLoading &&
            !isSaving &&
            draftTabs != savedTabs &&
            draftTabs.size in MIN_BOTTOM_NAV_TABS..MAX_BOTTOM_NAV_TABS

    val canReset: Boolean
        get() = !isLoading && !isSaving && draftTabs != DEFAULT_BOTTOM_NAV_TABS

    val availableTabs: List<ShellTab>
        get() = ShellTab.entries.filterNot(draftTabs::contains)
}

sealed interface BottomNavigationAction {
    data object BackClicked : BottomNavigationAction
    data class ToggleTab(val tab: ShellTab) : BottomNavigationAction
    data class MoveTabUp(val tab: ShellTab) : BottomNavigationAction
    data class MoveTabDown(val tab: ShellTab) : BottomNavigationAction
    data object ResetClicked : BottomNavigationAction
    data object SaveClicked : BottomNavigationAction
    data object DismissError : BottomNavigationAction
}

sealed interface BottomNavigationEffect {
    data object Saved : BottomNavigationEffect
}

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
    offset: Int,
): List<ShellTab> {
    if (tab == ShellTab.Home || offset == 0) return tabs

    val currentIndex = tabs.indexOf(tab)
    if (currentIndex <= 0) return tabs

    val targetIndex = currentIndex + offset
    if (targetIndex !in 1 until tabs.size) return tabs

    return tabs.toMutableList().apply {
        this[currentIndex] = this[targetIndex]
        this[targetIndex] = tab
    }
}
