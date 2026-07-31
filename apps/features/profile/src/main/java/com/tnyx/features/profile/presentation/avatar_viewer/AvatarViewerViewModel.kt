package com.tnyx.features.profile.presentation.avatar_viewer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tnyx.shared.profile.domain.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class AvatarViewerViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(AvatarViewerUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            try {
                profileRepository.getCurrentProfile()
                    .collect { profile ->
                        _uiState.update { state ->
                            state.copy(
                                avatarUrl = profile.avatarUrl,
                                displayName = profile.displayName,
                            )
                        }
                    }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun openBottomSheet() {
        _uiState.update { it.copy(isBottomSheetVisible = true) }
    }

    fun dismissBottomSheet() {
        _uiState.update { it.copy(isBottomSheetVisible = false) }
    }
}
