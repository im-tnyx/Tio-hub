package com.tnyx.routing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tnyx.core.ui.shell.domain.model.DEFAULT_BOTTOM_NAV_TABS
import com.tnyx.core.ui.shell.domain.model.ShellTab
import com.tnyx.core.ui.shell.domain.repository.BottomNavPreferencesRepository
import com.tnyx.core.ui.shell.presentation.state.ShellAvatarState
import com.tnyx.shared.profile.domain.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class MainScreenViewModel @Inject constructor(
    bottomNavPreferencesRepository: BottomNavPreferencesRepository,
    profileRepository: ProfileRepository,
) : ViewModel() {

    val bottomTabs: StateFlow<List<ShellTab>> = bottomNavPreferencesRepository.tabs
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
            initialValue = DEFAULT_BOTTOM_NAV_TABS,
        )

    val avatar: StateFlow<ShellAvatarState> = profileRepository.getCurrentProfile()
        .map { profile ->
            ShellAvatarState(
                imageUrl = profile.avatarUrl,
                displayName = profile.displayName,
                membershipTier = profile.membershipTier,
            )
        }
        .catch {
            emit(ShellAvatarState())
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
            initialValue = ShellAvatarState(),
        )
}
