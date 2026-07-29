package com.tnyx.routing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tnyx.features.auth.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

sealed interface AppSessionEffect {
    data object SignedOut : AppSessionEffect
}

@HiltViewModel
class AppSessionViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {
    private val _effect = MutableSharedFlow<AppSessionEffect>()
    val effect = _effect.asSharedFlow()

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
            _effect.emit(AppSessionEffect.SignedOut)
        }
    }
}
