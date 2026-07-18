package com.tnyx.core.ui.shell.presentation.state

import androidx.compose.runtime.Immutable
import com.tnyx.core.ui.shell.domain.model.DEFAULT_BOTTOM_NAV_TABS
import com.tnyx.core.ui.shell.domain.model.ShellTab
import com.tnyx.shared.profile.domain.model.MembershipTier

@Immutable
data class ShellAvatarState(
    val imageUrl: String? = null,
    val displayName: String = "",
    val membershipTier: MembershipTier = MembershipTier.Free,
)

@Immutable
data class ShellUiState(
    val selectedTab: ShellTab = ShellTab.Home,
    val bottomTabs: List<ShellTab> = DEFAULT_BOTTOM_NAV_TABS,
    val isBottomNavVisible: Boolean = true,
    val appBarOpacity: Float = 0f,
    val avatar: ShellAvatarState = ShellAvatarState(),
)
