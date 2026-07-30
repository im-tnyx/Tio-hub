package com.tnyx.features.auth.presentation.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.tnyx.core.theme.TnyxTheme
import com.tnyx.core.ui.components.buttons.TnyxGhostButton
import com.tnyx.core.ui.components.buttons.TnyxPrimaryButton
import com.tnyx.core.ui.components.buttons.TnyxSecondaryButton
import com.tnyx.core.ui.components.buttons.TnyxSecondaryVariant
import com.tnyx.core.ui.components.inputs.TnyxTextField

@Composable
fun LoginScreen(
    state: LoginUiState,
    onAction: (LoginAction) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TnyxTheme.colors.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 28.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            AuthHeader(
                eyebrow = "TNYX",
                title = "Welcome back",
                subtitle = "Sign in to continue your fitness plan."
            )

            Spacer(modifier = Modifier.height(32.dp))

            TnyxTextField(
                value = state.email,
                onValueChange = { onAction(LoginAction.EmailChanged(it)) },
                label = { Text("Email") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                isError = state.emailError != null,
                errorMessage = state.emailError
            )

            Spacer(modifier = Modifier.height(14.dp))

            TnyxTextField(
                value = state.password,
                onValueChange = { onAction(LoginAction.PasswordChanged(it)) },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                isError = state.passwordError != null,
                errorMessage = state.passwordError
            )

            Spacer(modifier = Modifier.height(24.dp))

            TnyxPrimaryButton(
                text = if (state.isLoading) "Signing in" else "Sign in",
                onPressed = { onAction(LoginAction.SignInClicked) },
                enabled = state.canSubmit,
                expand = true,
                leading = if (state.isLoading) {
                    {
                        CircularProgressIndicator(
                            modifier = Modifier.height(18.dp),
                            strokeWidth = 2.dp,
                            color = TnyxTheme.colors.background
                        )
                    }
                } else {
                    null
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            TnyxSecondaryButton(
                text = "Continue with Google",
                onPressed = { onAction(LoginAction.GoogleClicked) },
                variant = TnyxSecondaryVariant.Muted,
                expand = true,
                enabled = !state.isLoading
            )

        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 32.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "New to TNYX?",
                style = TnyxTheme.typography.bodyMedium,
                color = TnyxTheme.colors.textSecondary
            )
            TnyxGhostButton(
                text = "Create account",
                onPressed = { onAction(LoginAction.CreateAccountClicked) },
                enabled = !state.isLoading
            )
        }
    }
}

@Composable
private fun AuthHeader(
    eyebrow: String,
    title: String,
    subtitle: String
) {
    Column {
        Text(
            text = eyebrow,
            style = TnyxTheme.typography.labelLarge,
            color = TnyxTheme.colors.textMuted,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = title,
            style = TnyxTheme.typography.displaySmall,
            color = TnyxTheme.colors.textPrimary,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = subtitle,
            style = TnyxTheme.typography.bodyLarge,
            color = TnyxTheme.colors.textSecondary
        )
    }
}
