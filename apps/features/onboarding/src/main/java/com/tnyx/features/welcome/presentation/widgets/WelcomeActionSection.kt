package com.tnyx.features.welcome.presentation.widgets

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.tnyx.core.theme.TnyxTheme
import com.tnyx.core.ui.components.buttons.TnyxPrimaryButton
import com.tnyx.core.ui.components.buttons.TnyxSecondaryButton
import com.tnyx.features.welcome.presentation.action.WelcomeAction

@Composable
fun WelcomeActionSection(
    ctaText: String,
    signInText: String,
    errorText: String?,
    onAction: (WelcomeAction) -> Unit
) {
    Column {
        TnyxPrimaryButton(
            text = ctaText,
            onPressed = { onAction(WelcomeAction.GetStartedClicked) },
            expand = true
        )

        Spacer(modifier = Modifier.height(TnyxTheme.dimens.SpaceM))

        TnyxSecondaryButton(
            text = signInText,
            onPressed = { onAction(WelcomeAction.SignInClicked) },
            expand = true
        )

        if (!errorText.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(TnyxTheme.dimens.SpaceM))
            Text(
                text = errorText,
                style = TnyxTheme.typography.bodyMedium,
                color = TnyxTheme.colors.error,
            )
        }
    }
}
