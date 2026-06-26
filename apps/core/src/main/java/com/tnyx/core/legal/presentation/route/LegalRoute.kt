package com.tnyx.core.legal.presentation.route

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tnyx.core.legal.presentation.screen.LegalScreen
import com.tnyx.core.legal.presentation.state.LegalEffect
import com.tnyx.core.legal.presentation.view_model.LegalViewModel
import kotlinx.coroutines.flow.collectLatest

@Composable
fun LegalRoute(
    title: String,
    url: String,
    isRemoteEnabled: Boolean,
    onClose: () -> Unit,
    viewModel: LegalViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(title, url, isRemoteEnabled) {
        viewModel.init(title, url, isRemoteEnabled)
    }

    LaunchedEffect(viewModel.effect) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                LegalEffect.Close -> onClose()
            }
        }
    }

    LegalScreen(
        state = uiState,
        onAction = viewModel::handleAction,
        onLoadingFinished = viewModel::onLoadingFinished
    )
}
