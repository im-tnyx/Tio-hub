package com.tnyx.features.welcome.presentation.widgets

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.ClickableText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import com.tnyx.core.theme.TnyxTheme
import com.tnyx.features.welcome.presentation.action.WelcomeAction
import com.tnyx.features.welcome.presentation.state.LegalDocumentType

@Composable
fun WelcomeFooterSection(
    termsPrefix: String,
    termsText: String,
    andText: String,
    privacyText: String,
    termsSuffix: String,
    onAction: (WelcomeAction) -> Unit
) {
    val annotatedString = buildAnnotatedString {
        append(termsPrefix)

        pushStringAnnotation(tag = "terms", annotation = "terms")
        withStyle(style = SpanStyle(color = TnyxTheme.colors.primary, textDecoration = TextDecoration.Underline)) {
            append(termsText)
        }
        pop()

        append(andText)

        pushStringAnnotation(tag = "policy", annotation = "policy")
        withStyle(style = SpanStyle(color = TnyxTheme.colors.primary, textDecoration = TextDecoration.Underline)) {
            append(privacyText)
        }
        pop()

        append(termsSuffix)
    }

    ClickableText(
        text = annotatedString,
        style = TnyxTheme.typography.labelMedium.copy(
            color = TnyxTheme.colors.textSecondary,
            textAlign = TextAlign.Center
        ),
        modifier = Modifier
            .padding(horizontal = TnyxTheme.dimens.SpaceL)
            .padding(top = TnyxTheme.dimens.SpaceSM),
        onClick = { offset ->
            annotatedString.getStringAnnotations(tag = "terms", start = offset, end = offset).firstOrNull()?.let {
                onAction(WelcomeAction.LegalDocumentClicked(LegalDocumentType.TERMS_AND_CONDITIONS))
            }
            annotatedString.getStringAnnotations(tag = "policy", start = offset, end = offset).firstOrNull()?.let {
                onAction(WelcomeAction.LegalDocumentClicked(LegalDocumentType.PRIVACY_POLICY))
            }
        }
    )
}
