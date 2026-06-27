package com.tnyx.features.settings.presentation.home

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class SettingsHomeViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow(SettingsHomeUiState())
    val uiState = _uiState.asStateFlow()
}
