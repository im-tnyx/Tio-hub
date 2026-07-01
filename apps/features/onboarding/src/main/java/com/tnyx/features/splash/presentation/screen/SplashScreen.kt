package com.tnyx.features.splash.presentation.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.tnyx.features.onboarding.R
import com.tnyx.core.theme.TnyxTheme
import com.tnyx.core.theme.tokens.foundation.TnyxPalette
import com.tnyx.features.splash.presentation.action.SplashAction
import com.tnyx.features.splash.presentation.state.SplashUiState

@Composable
fun SplashScreen(
    state: SplashUiState,
    onAction: (SplashAction) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1B1C1E)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.dark_logo),
                contentDescription = "TNYX Logo",
                modifier = Modifier.size(180.dp),
                contentScale = ContentScale.Fit
            )

            if (state.isLoading) {
                Spacer(modifier = Modifier.height(TnyxTheme.dimens.SpaceXXL))
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            }
        }
    }
}
