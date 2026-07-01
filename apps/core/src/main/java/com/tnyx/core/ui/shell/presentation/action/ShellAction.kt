package com.tnyx.core.ui.shell.presentation.action

import com.tnyx.core.ui.shell.presentation.state.ShellTab

sealed class ShellAction {
    data class TabSelected(val tab: ShellTab) : ShellAction()
    data class ScrollChanged(val offset: Float) : ShellAction()
    data object PremiumClicked : ShellAction()
    data object StreakClicked : ShellAction()
    data object ProfileClicked : ShellAction()
    data object SettingsClicked : ShellAction()
}
