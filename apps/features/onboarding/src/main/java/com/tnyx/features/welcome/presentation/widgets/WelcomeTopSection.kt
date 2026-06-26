package com.tnyx.features.welcome.presentation.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.tnyx.core.theme.TnyxTheme
import com.tnyx.features.welcome.presentation.action.WelcomeAction

@Composable
fun WelcomeTopSection(
    localeCode: String,
    skipText: String,
    onAction: (WelcomeAction) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = TnyxTheme.dimens.SpaceXXS),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // --- Language Selector Button ---
        Text(
            text = localeCode.uppercase(),
            style = TnyxTheme.typography.labelLarge,
            color = TnyxTheme.colors.textPrimary,
            modifier = Modifier
                .border(
                    width = TnyxTheme.dimens.BorderThin,
                    color = TnyxTheme.colors.textPrimary.copy(alpha = 0.1f),
                    shape = CircleShape
                )
                .background(
                    color = TnyxTheme.colors.surface.copy(alpha = 0.4f),
                    shape = CircleShape
                )
                .clip(CircleShape)
                .clickable { onAction(WelcomeAction.LanguageSelectorClicked) }
                .padding(horizontal = TnyxTheme.dimens.SpaceM, vertical = TnyxTheme.dimens.SpaceS)
        )

        // --- Skip Button ---
        Text(
            text = skipText,
            style = TnyxTheme.typography.labelLarge,
            color = TnyxTheme.colors.textPrimary,
            modifier = Modifier
                .border(
                    width = TnyxTheme.dimens.BorderThin,
                    color = TnyxTheme.colors.textPrimary.copy(alpha = 0.1f),
                    shape = CircleShape
                )
                .background(
                    color = TnyxTheme.colors.surface.copy(alpha = 0.4f),
                    shape = CircleShape
                )
                .clip(CircleShape)
                .clickable { onAction(WelcomeAction.SkipForNowClicked) }
                .padding(horizontal = TnyxTheme.dimens.SpaceM, vertical = TnyxTheme.dimens.SpaceS)
        )
    }
}
