package com.tnyx.core.legal.presentation.widgets

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.tnyx.core.legal.presentation.state.LegalSectionUiModel
import com.tnyx.core.legal.presentation.state.LegalUiState
import com.tnyx.core.theme.TnyxTheme

@Composable
fun LegalPlaceholderContent(
    state: LegalUiState,
    modifier: Modifier = Modifier
) {
    val colors = TnyxTheme.colors
    val dimens = TnyxTheme.dimens

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(
                start = dimens.SpaceL,
                top = dimens.SpaceM,
                end = dimens.SpaceL,
                bottom = dimens.SpaceM
            )
    ) {
        Text(
            text = state.title.toLegalDialogTitle(),
            style = TnyxTheme.typography.headlineMedium.copy(fontWeight = FontWeight.W800),
            color = colors.textPrimary,
            modifier = Modifier.padding(end = dimens.IconXL + dimens.SpaceS)
        )

        Spacer(modifier = Modifier.height(dimens.SpaceM))

        Text(
            text = state.uiOnlyModeTitle,
            style = TnyxTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = colors.textPrimary
        )

        Spacer(modifier = Modifier.height(dimens.SpaceS))

        Text(
            text = "${state.title.toLegalDialogTitle()} ${state.uiOnlyModeSubtitle}",
            style = TnyxTheme.typography.bodyLarge,
            color = colors.textPrimary
        )

        Spacer(modifier = Modifier.height(dimens.SpaceL))

        state.placeholderSections.forEach { section ->
            LegalSection(section = section)
        }

        if (state.enableRemoteDocsText.isNotBlank()) {
            Spacer(modifier = Modifier.height(dimens.SpaceS))
            Text(
                text = state.enableRemoteDocsText,
                style = TnyxTheme.typography.bodyMedium,
                color = colors.textSecondary
            )
        }

        if (state.legalInfoText.isNotBlank()) {
            Spacer(modifier = Modifier.height(dimens.SpaceL))
            LegalInfoBox(text = state.legalInfoText)
        }
    }
}

@Composable
private fun LegalSection(section: LegalSectionUiModel) {
    val colors = TnyxTheme.colors
    val dimens = TnyxTheme.dimens

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = dimens.SpaceL)
    ) {
        Text(
            text = section.title,
            style = TnyxTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = colors.textPrimary
        )
        Spacer(modifier = Modifier.height(dimens.SpaceS))
        Text(
            text = section.body,
            style = TnyxTheme.typography.bodyMedium,
            color = colors.textPrimary
        )
    }
}

private fun String.toLegalDialogTitle(): String {
    val trimmed = trim()
    if (trimmed.isEmpty()) return trimmed
    return if (trimmed.endsWith('.') || trimmed.endsWith('!') || trimmed.endsWith('?')) {
        trimmed
    } else {
        "$trimmed."
    }
}
