package com.tnyx.features.welcome.presentation.widgets

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.tnyx.features.onboarding.R

@Composable
fun WelcomeHeroBackground(alpha: Float) {
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.landing_screen),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .alpha(alpha),
            alignment = Alignment.TopCenter,
            contentScale = ContentScale.FillWidth
        )

        WelcomeBackdrop()
    }
}
