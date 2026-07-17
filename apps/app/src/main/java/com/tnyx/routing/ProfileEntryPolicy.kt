package com.tnyx.routing

import com.tnyx.core.ui.shell.domain.model.ShellTab

internal enum class ProfileEntryAction {
    OpenStandaloneProfile,
    SelectYouTab,
    NoOp,
}

/**
 * Resolves avatar behavior without creating duplicate Profile/You destinations.
 */
internal fun resolveProfileEntryAction(
    bottomTabs: Collection<ShellTab>,
    selectedTab: ShellTab,
): ProfileEntryAction {
    if (ShellTab.You !in bottomTabs) {
        return ProfileEntryAction.OpenStandaloneProfile
    }

    return if (selectedTab == ShellTab.You) {
        ProfileEntryAction.NoOp
    } else {
        ProfileEntryAction.SelectYouTab
    }
}
