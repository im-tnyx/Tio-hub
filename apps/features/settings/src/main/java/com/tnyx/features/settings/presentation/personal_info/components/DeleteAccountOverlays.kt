package com.tnyx.features.settings.presentation.personal_info.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.tnyx.core.theme.TnyxTheme
import com.tnyx.features.settings.presentation.personal_info.DeleteAccountStep
import com.tnyx.features.settings.presentation.personal_info.PersonalInfoAction
import com.tnyx.features.settings.presentation.personal_info.PersonalInfoUiState

@Composable
fun DeleteAccountOverlays(
    state: PersonalInfoUiState,
    onAction: (PersonalInfoAction) -> Unit,
) {
    if (state.deleteStep == DeleteAccountStep.Idle) return

    Dialog(
        onDismissRequest = { onAction(PersonalInfoAction.OnDismissOverlays) },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(TnyxTheme.colors.background.copy(alpha = 0.95f))
        ) {
            // Close Button
            IconButton(
                onClick = { onAction(PersonalInfoAction.OnDismissOverlays) },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(TnyxTheme.dimens.SpaceM)
                    .statusBarsPadding()
                    .background(TnyxTheme.colors.textPrimary.copy(alpha = 0.1f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = TnyxTheme.colors.textPrimary
                )
            }

            when (state.deleteStep) {
                DeleteAccountStep.Confirm -> {
                    ConfirmOverlay(onAction)
                }
                DeleteAccountStep.HoldToDelete -> {
                    HoldToDeleteOverlay(state, onAction)
                }
                DeleteAccountStep.Completed -> {
                    CompletedOverlay(onAction)
                }
                else -> {}
            }
        }
    }
}

@Composable
private fun BoxScope.ConfirmOverlay(onAction: (PersonalInfoAction) -> Unit) {
    Column(
        modifier = Modifier
            .align(Alignment.Center)
            .padding(TnyxTheme.dimens.SpaceM),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Are you sure?",
            style = TnyxTheme.typography.headlineLarge,
            color = TnyxTheme.colors.textPrimary,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(TnyxTheme.dimens.SpaceM))
        Text(
            text = "This means all your saved progress will be deleted permanently.",
            color = TnyxTheme.colors.textSecondary,
            textAlign = TextAlign.Center,
            style = TnyxTheme.typography.bodyLarge
        )
        Spacer(modifier = Modifier.height(TnyxTheme.dimens.SpaceS))
        Text(
            text = "This action can't be reversed",
            color = TnyxTheme.colors.error,
            textAlign = TextAlign.Center,
            style = TnyxTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(TnyxTheme.dimens.SpaceXL))

        Button(
            onClick = { onAction(PersonalInfoAction.OnKeepAccountClicked) },
            modifier = Modifier
                .fillMaxWidth()
                .height(TnyxTheme.components.button.height),
            shape = TnyxTheme.shapes.Material.medium,
            colors = ButtonDefaults.buttonColors(
                containerColor = TnyxTheme.colors.textPrimary,
                contentColor = TnyxTheme.colors.background
            )
        ) {
            Text("Keep Account", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(TnyxTheme.dimens.SpaceM))

        TextButton(
            onClick = { onAction(PersonalInfoAction.OnConfirmDeleteClicked) },
            modifier = Modifier
                .fillMaxWidth()
                .height(TnyxTheme.components.button.height)
                .clip(TnyxTheme.shapes.Material.medium)
                .background(TnyxTheme.colors.error.copy(alpha = 0.15f)),
            colors = ButtonDefaults.textButtonColors(contentColor = TnyxTheme.colors.error)
        ) {
            Text("Delete", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun BoxScope.HoldToDeleteOverlay(
    state: PersonalInfoUiState,
    onAction: (PersonalInfoAction) -> Unit
) {
    Column(
        modifier = Modifier
            .align(Alignment.Center)
            .padding(TnyxTheme.dimens.SpaceM),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Hold this button",
            style = TnyxTheme.typography.headlineLarge,
            color = TnyxTheme.colors.textPrimary,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(TnyxTheme.dimens.SpaceS))
        Text(
            text = "to delete all your progress permanently.",
            color = TnyxTheme.colors.textSecondary,
            textAlign = TextAlign.Center,
            style = TnyxTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(64.dp))

        HoldToDeleteButton(
            progress = state.holdProgress,
            remaining = state.remainingSeconds,
            onHoldStarted = { onAction(PersonalInfoAction.OnHoldStarted) },
            onHoldReleased = { onAction(PersonalInfoAction.OnHoldReleased) }
        )

        Spacer(modifier = Modifier.height(64.dp))

        Button(
            onClick = { onAction(PersonalInfoAction.OnKeepAccountClicked) },
            modifier = Modifier
                .fillMaxWidth()
                .height(TnyxTheme.components.button.height),
            shape = TnyxTheme.shapes.Material.medium,
            colors = ButtonDefaults.buttonColors(
                containerColor = TnyxTheme.colors.textPrimary,
                contentColor = TnyxTheme.colors.background
            )
        ) {
            Text("Keep Account", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun HoldToDeleteButton(
    progress: Float,
    remaining: Int,
    onHoldStarted: () -> Unit,
    onHoldReleased: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    LaunchedEffect(isPressed) {
        if (isPressed) onHoldStarted() else onHoldReleased()
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        if (progress > 0) {
            Text(
                text = "$remaining",
                color = TnyxTheme.colors.error,
                style = TnyxTheme.typography.displayMedium,
                fontWeight = FontWeight.Black,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        } else {
            Spacer(modifier = Modifier.height(70.dp))
        }

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(140.dp)
        ) {
            CircularProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxSize(),
                color = TnyxTheme.colors.error,
                strokeWidth = 6.dp,
                trackColor = TnyxTheme.colors.textPrimary.copy(alpha = 0.1f)
            )

            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(TnyxTheme.colors.error)
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = {}
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = TnyxTheme.colors.background,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

@Composable
private fun BoxScope.CompletedOverlay(onAction: (PersonalInfoAction) -> Unit) {
    Column(
        modifier = Modifier
            .align(Alignment.Center)
            .padding(TnyxTheme.dimens.SpaceM),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = null,
            tint = TnyxTheme.colors.success,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(TnyxTheme.dimens.SpaceM))
        Text(
            text = "Account Deleted",
            style = TnyxTheme.typography.headlineSmall,
            color = TnyxTheme.colors.textPrimary,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(TnyxTheme.dimens.SpaceXL))
        Button(
            onClick = { onAction(PersonalInfoAction.OnDeleteCompletedShown) },
            modifier = Modifier
                .fillMaxWidth()
                .height(TnyxTheme.components.button.height),
            shape = TnyxTheme.shapes.Material.medium,
            colors = ButtonDefaults.buttonColors(
                containerColor = TnyxTheme.colors.textPrimary,
                contentColor = TnyxTheme.colors.background
            )
        ) {
            Text("Close", fontWeight = FontWeight.Bold)
        }
    }
}
