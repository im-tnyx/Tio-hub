package com.tnyx.features.onboarding.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.tnyx.features.onboarding.domain.model.OnboardingAuthState
import com.tnyx.features.onboarding.domain.model.OnboardingEntryPath
import com.tnyx.features.onboarding.domain.model.OnboardingRouteContext
import kotlinx.coroutines.flow.collectLatest

@Composable
fun OnboardingRoute(
    onCompleted: () -> Unit,
    onExit: () -> Unit,
    initialRouteContext: OnboardingRouteContext = OnboardingRouteContext(),
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(initialRouteContext) {
        viewModel.handleAction(OnboardingAction.Init(initialRouteContext))
    }

    LaunchedEffect(viewModel.effect) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                OnboardingEffect.Completed -> onCompleted()
                OnboardingEffect.Exit -> onExit()
            }
        }
    }

    OnboardingScreen(
        state = uiState,
        onAction = viewModel::handleAction,
    )
}

internal fun onboardingRouteContextFrom(
    args: com.tnyx.routing.routes.RootRoute.Onboarding,
): OnboardingRouteContext {
    return OnboardingRouteContext(
        entryPath = runCatching { OnboardingEntryPath.valueOf(args.entryPath) }
            .getOrDefault(OnboardingEntryPath.GetStarted),
        authState = runCatching { OnboardingAuthState.valueOf(args.authState) }
            .getOrDefault(OnboardingAuthState.SignedOut),
        signupCompleted = args.signupCompleted,
        workoutPlanEnabled = args.workoutPlanEnabled,
        mobilePresent = args.mobilePresent,
        mobileVerified = args.mobileVerified,
        namePrefilled = args.namePrefilled,
        authRequired = args.authRequired,
    )
}
