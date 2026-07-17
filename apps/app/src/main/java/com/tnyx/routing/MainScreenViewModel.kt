package com.tnyx.routing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tnyx.core.ui.shell.domain.model.DEFAULT_BOTTOM_NAV_TABS
import com.tnyx.core.ui.shell.domain.model.ShellTab
import com.tnyx.core.ui.shell.domain.repository.BottomNavPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class MainScreenViewModel @Inject constructor(
    bottomNavPreferencesRepository: BottomNavPreferencesRepository,
) : ViewModel() {

    val bottomTabs: StateFlow<List<ShellTab>> = bottomNavPreferencesRepository.tabs
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
            initialValue = DEFAULT_BOTTOM_NAV_TABS,
        )
}
