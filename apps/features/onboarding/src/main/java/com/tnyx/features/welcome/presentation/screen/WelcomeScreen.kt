package com.tnyx.features.welcome.presentation.screen

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import com.tnyx.core.theme.TnyxTheme
import com.tnyx.features.welcome.presentation.action.WelcomeAction
import com.tnyx.features.welcome.presentation.state.WelcomeUiState
import com.tnyx.features.welcome.presentation.widgets.*
import kotlinx.coroutines.delay

@Composable
fun WelcomeScreen(
    state: WelcomeUiState,
    onAction: (WelcomeAction) -> Unit
) {
    // Animation States
    var imageVisible by remember { mutableStateOf(false) }
    var contentVisible by remember { mutableStateOf(false) }

    val motion = TnyxTheme.motion

    val imageAlpha by animateFloatAsState(
        targetValue = if (imageVisible) 1f else 0f,
        animationSpec = tween(durationMillis = motion.DurationExtraLong),
        label = "ImageAlpha"
    )

    val contentAlpha by animateFloatAsState(
        targetValue = if (contentVisible) 1f else 0f,
        animationSpec = tween(durationMillis = motion.DurationLong2),
        label = "ContentAlpha"
    )

    LaunchedEffect(Unit) {
        imageVisible = true
        delay(motion.DurationLong1.toLong())
        contentVisible = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(TnyxTheme.colors.background)
    ) {
        // --- बैकग्राउंड इमेज (एनिमेशन के साथ) ---
        WelcomeHeroBackground(alpha = imageAlpha)

        // --- मुख्य कंटेंट (टेक्स्ट और बटन्स) ---
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = TnyxTheme.insets.screenHorizontal)
                .safeDrawingPadding()
        ) {
            WelcomeTopSection(
                localeCode = state.localeCode,
                skipText = state.skipText,
                onAction = onAction,
                modifier = Modifier.alpha(contentAlpha)
            )

            // लचीली स्पेसिंग के लिए Spacer का उपयोग
            Spacer(modifier = Modifier.height(TnyxTheme.dimens.SpaceXL))
            Spacer(modifier = Modifier.weight(1f))

            Column(
                modifier = Modifier
                    .alpha(contentAlpha)
                    .padding(bottom = TnyxTheme.dimens.SpaceXXS) // कम पैडिंग, safeDrawingPadding ही काफी है
            ) {
                WelcomeMiddleSection(
                    title = state.title,
                    features = state.featureLines
                )

                Spacer(modifier = Modifier.height(TnyxTheme.dimens.SpaceXL))

                WelcomeActionSection(
                    ctaText = state.ctaText,
                    signInText = state.signInText,
                    onAction = onAction
                )

                Spacer(modifier = Modifier.height(TnyxTheme.dimens.SpaceM))

                WelcomeFooterSection(
                    termsPrefix = state.termsPrefix,
                    termsText = state.termsText,
                    andText = state.andText,
                    privacyText = state.privacyText,
                    termsSuffix = state.termsSuffix,
                    onAction = onAction
                )
            }
        }
    }
}
