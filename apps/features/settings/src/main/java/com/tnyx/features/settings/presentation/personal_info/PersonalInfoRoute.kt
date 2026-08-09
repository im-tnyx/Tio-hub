package com.tnyx.features.settings.presentation.personal_info

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun PersonalInfoRoute(
    onNavigateBack: () -> Unit,
    onOpenAvatarViewer: () -> Unit,
    viewModel: PersonalInfoViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    PersonalInfoScreen(
        state = uiState,
        onAction = { action ->
            when (action) {
                PersonalInfoAction.OnBackClicked -> onNavigateBack()
                PersonalInfoAction.OnChangePhotoClicked -> onOpenAvatarViewer()
                else -> viewModel.onAction(action)
            }
        },
    )

    uiState.avatarError?.let { error ->
        AlertDialog(
            onDismissRequest = {
                viewModel.onAction(PersonalInfoAction.OnDismissAvatarError)
            },
            title = { Text("Profile photo") },
            text = { Text(error) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.onAction(PersonalInfoAction.OnDismissAvatarError)
                    },
                ) {
                    Text("OK")
                }
            },
        )
    }
}
