package com.tnyx.core.ui.shell.presentation.state

import androidx.compose.runtime.Immutable
import com.tnyx.core.ui.shell.domain.model.DEFAULT_BOTTOM_NAV_TABS
import com.tnyx.core.ui.shell.domain.model.ShellTab

enum class ShellPlanTier {
    Free,
    Plus,
    Premium
}

@Immutable
data class ShellUiState(
    val selectedTab: ShellTab = ShellTab.Home,
    val bottomTabs: List<ShellTab> = DEFAULT_BOTTOM_NAV_TABS,
    val isBottomNavVisible: Boolean = true,
    val appBarOpacity: Float = 0f,
    val planTier: ShellPlanTier = ShellPlanTier.Free
)
