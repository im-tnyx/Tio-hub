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

    fun openEditSheet() {
        _uiState.update { it.copy(isEditSheetVisible = true) }
    }

    fun dismissEditSheet() {
        _uiState.update { it.copy(isEditSheetVisible = false) }
    }

    fun uploadAvatar(jpegBytes: ByteArray) {
        if (jpegBytes.isEmpty()) return
        viewModelScope.launch {
            try {
                profileRepository.updateAvatar(jpegBytes)
                dismissEditSheet()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun openDeleteSheet() {
        _uiState.update { it.copy(isDeleteSheetVisible = true) }
    }

    fun dismissDeleteSheet() {
        _uiState.update { it.copy(isDeleteSheetVisible = false) }
    }

    fun confirmDeletePhoto(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isDeleting = true) }
            try {
                profileRepository.removeAvatar()
                _uiState.update { it.copy(isDeleting = false, isDeleteSheetVisible = false) }
                onSuccess()
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update { it.copy(isDeleting = false) }
            }
        }
    }
}
