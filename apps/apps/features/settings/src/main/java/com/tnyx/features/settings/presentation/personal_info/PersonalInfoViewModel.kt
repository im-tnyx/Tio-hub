package com.tnyx.features.settings.presentation.personal_info

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PersonalInfoViewModel @Inject constructor(
    // Inject repositories when backend is ready
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        PersonalInfoUiState(
            name = "",
            email = "",
            gender = "",
            dob = "",
            heightCm = "",
            weightKg = ""
        )
    )
    val uiState: StateFlow<PersonalInfoUiState> = _uiState

    fun handleAction(action: PersonalInfoAction) {
        when (action) {
            is PersonalInfoAction.NameChanged -> update { it.copy(name = action.value, hasChanges = true) }
            is PersonalInfoAction.EmailChanged -> update { it.copy(email = action.value, hasChanges = true) }
            is PersonalInfoAction.GenderChanged -> update { it.copy(gender = action.value, hasChanges = true) }
            is PersonalInfoAction.DobChanged -> update { it.copy(dob = action.value, hasChanges = true) }
            is PersonalInfoAction.HeightChanged -> update { it.copy(heightCm = action.value, hasChanges = true) }
            is PersonalInfoAction.WeightChanged -> update { it.copy(weightKg = action.value, hasChanges = true) }
            PersonalInfoAction.ChangePhotoClicked -> { /* TODO: trigger picker */ }
            PersonalInfoAction.BackClicked -> { /* handled by route */ }
            PersonalInfoAction.DobClicked -> { /* TODO: show date picker */ }
            PersonalInfoAction.SaveClicked -> save()
        }
    }

    private fun save() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            // TODO: persist via repository when available
            _uiState.update { it.copy(isSaving = false, hasChanges = false) }
        }
    }

    private inline fun update(block: (PersonalInfoUiState) -> PersonalInfoUiState) {
        _uiState.update(block)
    }
}
