package com.tnyx.features.onboarding.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tnyx.features.onboarding.domain.analytics.OnboardingAnalyticsEvent
import com.tnyx.features.onboarding.domain.analytics.OnboardingAnalyticsTracker
import com.tnyx.features.onboarding.domain.flow.DefaultOnboardingFlow
import com.tnyx.features.onboarding.domain.model.OnboardingAnswer
import com.tnyx.features.onboarding.domain.model.OnboardingAuthState
import com.tnyx.features.onboarding.domain.model.OnboardingCheckpoint
import com.tnyx.features.onboarding.domain.model.OnboardingFlowDefinition
import com.tnyx.features.onboarding.domain.model.OnboardingPosition
import com.tnyx.features.onboarding.domain.model.OnboardingRouteContext
import com.tnyx.features.onboarding.domain.repository.OnboardingRepository
import com.tnyx.features.onboarding.domain.usecase.BuildFlowUseCase
import com.tnyx.features.onboarding.domain.usecase.AdvanceOnboardingStepResult
import com.tnyx.features.onboarding.domain.usecase.AdvanceOnboardingStepUseCase
import com.tnyx.features.onboarding.domain.usecase.CompleteOnboardingResult
import com.tnyx.features.onboarding.domain.usecase.CompleteOnboardingUseCase
import com.tnyx.features.onboarding.domain.usecase.ContinueOnboardingSessionResult
import com.tnyx.features.onboarding.domain.usecase.ContinueOnboardingSessionUseCase
import com.tnyx.features.onboarding.domain.usecase.FinalizeOnboardingProfileUseCase
import com.tnyx.features.onboarding.domain.usecase.InitializeOnboardingSessionResult
import com.tnyx.features.onboarding.domain.usecase.InitializeOnboardingSessionUseCase
import com.tnyx.features.onboarding.domain.usecase.PersistOnboardingCheckpointResult
import com.tnyx.features.onboarding.domain.usecase.PersistOnboardingCheckpointUseCase
import com.tnyx.features.onboarding.domain.usecase.PrepareOnboardingCheckpointUseCase
import com.tnyx.features.onboarding.domain.usecase.ResolveBackNavigationResult
import com.tnyx.features.onboarding.domain.usecase.ResolveBackNavigationUseCase
import com.tnyx.features.onboarding.domain.usecase.RetryOnboardingSessionResult
import com.tnyx.features.onboarding.domain.usecase.RetryOnboardingSessionUseCase
import com.tnyx.features.onboarding.domain.usecase.SeedOnboardingRecommendationsUseCase
import com.tnyx.features.onboarding.domain.usecase.SkipOnboardingSectionUseCase
import com.tnyx.features.onboarding.domain.usecase.UpdateOnboardingAnswerUseCase
import com.tnyx.shared.auth.domain.repository.AuthSessionProvider
import com.tnyx.shared.profile.domain.model.UserProfile
import com.tnyx.shared.profile.domain.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val repository: OnboardingRepository,
    private val profileRepository: ProfileRepository,
    private val sessionProvider: AuthSessionProvider = EmptyAuthSessionProvider,
    private val finalizeOnboardingProfile: FinalizeOnboardingProfileUseCase =
        FinalizeOnboardingProfileUseCase(),
    private val advanceOnboardingStep: AdvanceOnboardingStepUseCase =
        AdvanceOnboardingStepUseCase(),
    private val resolveBackNavigation: ResolveBackNavigationUseCase =
        ResolveBackNavigationUseCase(),
    private val skipOnboardingSection: SkipOnboardingSectionUseCase =
        SkipOnboardingSectionUseCase(),
    private val persistOnboardingCheckpoint: PersistOnboardingCheckpointUseCase =
        PersistOnboardingCheckpointUseCase(),
    private val completeOnboardingUseCase: CompleteOnboardingUseCase =
        CompleteOnboardingUseCase(),
    private val continueOnboardingSession: ContinueOnboardingSessionUseCase =
        ContinueOnboardingSessionUseCase(),
    private val initializeOnboardingSession: InitializeOnboardingSessionUseCase =
        InitializeOnboardingSessionUseCase(),
    private val checkpointUiStateFactory: OnboardingCheckpointUiStateFactory =
        OnboardingCheckpointUiStateFactory(),
    private val retryOnboardingSession: RetryOnboardingSessionUseCase =
        RetryOnboardingSessionUseCase(),
    private val updateOnboardingAnswer: UpdateOnboardingAnswerUseCase =
        UpdateOnboardingAnswerUseCase(),
    private val prepareOnboardingCheckpoint: PrepareOnboardingCheckpointUseCase =
        PrepareOnboardingCheckpointUseCase(),
    private val buildFlowUseCase: BuildFlowUseCase = BuildFlowUseCase(),
    private val analyticsTracker: OnboardingAnalyticsTracker =
        OnboardingAnalyticsTracker(com.tnyx.features.onboarding.domain.analytics.OnboardingAnalyticsLogger()),
) : ViewModel() {
    companion object {
        private const val CompletionReadyDelayMillis = 900L
    }

    private val flow: OnboardingFlowDefinition = DefaultOnboardingFlow.definition
    private val operationMutex = Mutex()

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState = _uiState.asStateFlow()

    private val effectChannel = Channel<OnboardingEffect>(capacity = Channel.BUFFERED)
    val effect = effectChannel.receiveAsFlow()

    private var checkpoint: OnboardingCheckpoint? = null
    private var progressCheckpoint: OnboardingCheckpoint? = null
    private var currentProfileSnapshot: UserProfile? = null
    private var initialRouteContext: OnboardingRouteContext = OnboardingRouteContext()
    private var initializationStarted = false
    private var lastTrackedPosition: OnboardingPosition? = null
    private var completionTracked = false

    fun handleAction(action: OnboardingAction) {
        when (action) {
            is OnboardingAction.Init -> initialize(action.initialRouteContext)
            OnboardingAction.Retry -> retry()
            OnboardingAction.BackClicked -> runOperation {
                trackCurrentSectionAction(isNext = false)
                navigateBack()
            }
            OnboardingAction.ContinueClicked -> runOperation {
                trackCurrentSectionAction(isNext = true)
                continueForward()
            }
            OnboardingAction.SkipSectionClicked -> runOperation(::skipCurrentSection)
            is OnboardingAction.AnswerChanged -> {
                runOperation { updateCurrentAnswer(action.answer) }
            }
        }
    }

    private fun initialize(initialRouteContext: OnboardingRouteContext) {
        this.initialRouteContext = this.initialRouteContext.mergedWith(initialRouteContext)
        if (initializationStarted) return
        initializationStarted = true

        viewModelScope.launch {
            operationMutex.withLock {
                _uiState.update {
                    it.copy(
                        isLoading = true,
                        hasPersistenceError = false,
                    )
                }

                try {
                    currentProfileSnapshot = profileRepository.getCurrentProfile().first()
                    when (
                        val result = initializeOnboardingSession(
                            flow = flow,
                            onboardingRepository = repository,
                            profileRepository = profileRepository,
                        )
                    ) {
                        InitializeOnboardingSessionResult.ProfileAlreadyCompleted -> {
                            trackCompletionIfNeeded()
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    hasPersistenceError = false,
                                )
                            }
                            emitEffect(OnboardingEffect.Completed)
                            return@withLock
                        }

                        is InitializeOnboardingSessionResult.Ready -> {
                            val preparedCheckpoint = normalizedCheckpoint(result.checkpoint)
                            if (result.checkpoint != preparedCheckpoint) {
                                when (persistOnboardingCheckpoint(preparedCheckpoint, repository)) {
                                    is PersistOnboardingCheckpointResult.Failure -> {
                                        showInitializationFailure()
                                        return@withLock
                                    }

                                    is PersistOnboardingCheckpointResult.Success -> Unit
                                }
                            }
                            applyCheckpointState(
                                checkpoint = preparedCheckpoint,
                                status = OnboardingCheckpointUiStatus.Ready,
                            )
                        }

                        is InitializeOnboardingSessionResult.ResumeCompletedCheckpoint -> {
                            if (completeOnboarding(
                                    completedCheckpoint = result.checkpoint,
                                    persistCheckpoint = result.persistCheckpoint,
                                )
                            ) {
                                trackCompletionIfNeeded()
                                emitEffect(OnboardingEffect.Completed)
                            }
                            return@withLock
                        }
                    }
                } catch (exception: Exception) {
                    if (exception is CancellationException) throw exception
                    initializationStarted = false
                    showInitializationFailure()
                }
            }
        }
    }

    private fun retry() {
        runOperation {
            currentProfileSnapshot = profileRepository.getCurrentProfile().first()
            checkpoint?.let { currentCheckpoint ->
                render(
                    checkpoint = currentCheckpoint,
                    status = OnboardingCheckpointUiStatus.Saving,
                )
            }
            when (
                val result = retryOnboardingSession(
                    checkpoint = checkpoint,
                    onboardingRepository = repository,
                    profileRepository = profileRepository,
                    finalizeOnboardingProfile = finalizeOnboardingProfile,
                )
            ) {
                RetryOnboardingSessionResult.Reinitialize -> initialize(initialRouteContext)
                is RetryOnboardingSessionResult.Persisted -> {
                    applyCheckpointState(
                        checkpoint = result.checkpoint,
                        status = OnboardingCheckpointUiStatus.Ready,
                    )
                }

                is RetryOnboardingSessionResult.PersistFailed -> {
                    applyCheckpointState(
                        checkpoint = result.checkpoint,
                        status = OnboardingCheckpointUiStatus.PersistenceError,
                    )
                }

                is RetryOnboardingSessionResult.Completed -> {
                    applyCheckpointState(
                        checkpoint = result.checkpoint,
                        status = OnboardingCheckpointUiStatus.Ready,
                    )
                    showCompletionStates()
                }

                is RetryOnboardingSessionResult.CompleteFailed -> {
                    applyCheckpointState(
                        checkpoint = result.checkpoint,
                        status = OnboardingCheckpointUiStatus.PersistenceError,
                    )
                }
            }
        }
    }

    private suspend fun updateCurrentAnswer(answer: OnboardingAnswer?) {
        val currentCheckpoint = checkpoint ?: return
        saveAndRender(
            updatedCheckpoint = updateOnboardingAnswer(currentCheckpoint, answer),
            showSavingState = false,
            updateProgressCheckpoint = false,
        )
    }

    private suspend fun continueForward() {
        when (_uiState.value.completionStage) {
            OnboardingCompletionStage.SettingUp -> return
            OnboardingCompletionStage.Ready -> {
                emitEffect(OnboardingEffect.Completed)
                return
            }

            null -> Unit
        }

        val currentCheckpoint = checkpoint ?: return
        val currentState = _uiState.value
        if (!currentState.canContinue) {
            _uiState.update {
                it.copy(validationError = OnboardingValidationError.RequiredAnswerInvalid)
            }
            return
        }

        val advanceResult = preparedAdvanceResult(
            advanceOnboardingStep(currentCheckpoint, flow),
        )
        val previewCheckpoint = when (advanceResult) {
            is AdvanceOnboardingStepResult.Completed -> advanceResult.checkpoint
            is AdvanceOnboardingStepResult.Next -> advanceResult.checkpoint
        }
        applyCheckpointState(
            checkpoint = previewCheckpoint,
            status = OnboardingCheckpointUiStatus.Ready,
        )

        when (
            val result = continueOnboardingSession(
                advanceResult = advanceResult,
                onboardingRepository = repository,
                profileRepository = profileRepository,
                finalizeOnboardingProfile = finalizeOnboardingProfile,
            )
        ) {
            is ContinueOnboardingSessionResult.Persisted -> {
                applyCheckpointState(
                    checkpoint = result.checkpoint,
                    status = OnboardingCheckpointUiStatus.Ready,
                )
            }

            is ContinueOnboardingSessionResult.PersistFailed -> {
                applyCheckpointState(
                    checkpoint = result.checkpoint,
                    status = OnboardingCheckpointUiStatus.PersistenceError,
                )
            }

            is ContinueOnboardingSessionResult.Completed -> {
                applyCheckpointState(
                    checkpoint = result.checkpoint,
                    status = OnboardingCheckpointUiStatus.Ready,
                )
                trackCompletionIfNeeded()
                showCompletionStates()
            }

            is ContinueOnboardingSessionResult.CompleteFailed -> {
                applyCheckpointState(
                    checkpoint = result.checkpoint,
                    status = OnboardingCheckpointUiStatus.PersistenceError,
                )
            }
        }
    }

    private suspend fun completeOnboarding(
        completedCheckpoint: OnboardingCheckpoint,
        persistCheckpoint: Boolean = true,
    ): Boolean {
        val preparedCheckpoint = normalizedCheckpoint(completedCheckpoint)
        applyCheckpointState(
            checkpoint = preparedCheckpoint,
            status = OnboardingCheckpointUiStatus.Saving,
        )

        return when (
            completeOnboardingUseCase(
                checkpoint = preparedCheckpoint,
                persistCheckpoint = persistCheckpoint,
                onboardingRepository = repository,
                profileRepository = profileRepository,
                finalizeOnboardingProfile = finalizeOnboardingProfile,
            )
        ) {
            is CompleteOnboardingResult.Success -> {
                applyCheckpointState(
                    checkpoint = preparedCheckpoint,
                    status = OnboardingCheckpointUiStatus.Ready,
                )
                trackCompletionIfNeeded()
                true
            }

            is CompleteOnboardingResult.Failure -> {
                applyCheckpointState(
                    checkpoint = preparedCheckpoint,
                    status = OnboardingCheckpointUiStatus.PersistenceError,
                )
                false
            }
        }
    }

    private suspend fun navigateBack() {
        when (val result = resolveBackNavigation(checkpoint, flow)) {
            ResolveBackNavigationResult.Exit -> effectChannel.send(OnboardingEffect.Exit)
            is ResolveBackNavigationResult.Previous -> saveAndRender(result.checkpoint)
        }
    }

    private suspend fun skipCurrentSection() {
        val currentCheckpoint = checkpoint ?: return
        val skippedCheckpoint = skipOnboardingSection(currentCheckpoint, flow) ?: return
        saveAndRender(skippedCheckpoint)
    }

    private suspend fun saveAndRender(updatedCheckpoint: OnboardingCheckpoint): Boolean {
        return saveAndRender(
            updatedCheckpoint = updatedCheckpoint,
            showSavingState = false,
            updateProgressCheckpoint = true,
        )
    }

    private suspend fun saveAndRender(
        updatedCheckpoint: OnboardingCheckpoint,
        showSavingState: Boolean,
        updateProgressCheckpoint: Boolean,
    ): Boolean {
        val preparedCheckpoint = normalizedCheckpoint(updatedCheckpoint)
        applyCheckpointState(
            checkpoint = preparedCheckpoint,
            status = if (showSavingState) {
                OnboardingCheckpointUiStatus.Saving
            } else {
                OnboardingCheckpointUiStatus.Ready
            },
            updateProgressCheckpoint = updateProgressCheckpoint,
        )

        return when (persistOnboardingCheckpoint(preparedCheckpoint, repository)) {
            is PersistOnboardingCheckpointResult.Success -> {
                applyCheckpointState(
                    checkpoint = preparedCheckpoint,
                    status = OnboardingCheckpointUiStatus.Ready,
                    updateProgressCheckpoint = updateProgressCheckpoint,
                )
                true
            }

            is PersistOnboardingCheckpointResult.Failure -> {
                applyCheckpointState(
                    checkpoint = preparedCheckpoint,
                    status = OnboardingCheckpointUiStatus.PersistenceError,
                    updateProgressCheckpoint = updateProgressCheckpoint,
                )
                false
            }
        }
    }

    private fun applyCheckpointState(
        checkpoint: OnboardingCheckpoint,
        status: OnboardingCheckpointUiStatus,
        updateProgressCheckpoint: Boolean = true,
    ) {
        val preparedCheckpoint = normalizedCheckpoint(checkpoint)
        this.checkpoint = preparedCheckpoint
        if (updateProgressCheckpoint || progressCheckpoint == null) {
            progressCheckpoint = preparedCheckpoint
        }
        render(
            checkpoint = preparedCheckpoint,
            status = status,
        )
    }

    private fun normalizedCheckpoint(
        checkpoint: OnboardingCheckpoint,
    ): OnboardingCheckpoint {
        val routeAlignedCheckpoint = checkpoint.copy(
            routeContext = checkpoint.routeContext.mergedWith(initialRouteContext),
        )
        return prepareOnboardingCheckpoint(
            checkpoint = routeAlignedCheckpoint,
            currentProfile = currentProfileSnapshot,
            authSession = sessionProvider.currentSession(),
        )
    }

    private fun preparedAdvanceResult(
        advanceResult: AdvanceOnboardingStepResult,
    ): AdvanceOnboardingStepResult {
        return when (advanceResult) {
            is AdvanceOnboardingStepResult.Completed -> {
                AdvanceOnboardingStepResult.Completed(
                    checkpoint = normalizedCheckpoint(advanceResult.checkpoint),
                )
            }

            is AdvanceOnboardingStepResult.Next -> {
                AdvanceOnboardingStepResult.Next(
                    checkpoint = normalizedCheckpoint(advanceResult.checkpoint),
                )
            }
        }
    }

    private fun showInitializationFailure() {
        _uiState.update {
            it.copy(
                isLoading = false,
                hasPersistenceError = true,
            )
        }
    }

    private suspend fun emitEffect(effect: OnboardingEffect) {
        effectChannel.send(effect)
    }

    private suspend fun showCompletionStates() {
        _uiState.update {
            it.copy(
                completionStage = OnboardingCompletionStage.SettingUp,
                isSaving = false,
                hasPersistenceError = false,
                validationError = null,
            )
        }
        delay(CompletionReadyDelayMillis)
        _uiState.update {
            it.copy(completionStage = OnboardingCompletionStage.Ready)
        }
    }

    private fun render(
        checkpoint: OnboardingCheckpoint,
        status: OnboardingCheckpointUiStatus = OnboardingCheckpointUiStatus.Ready,
    ) {
        buildFlowUseCase(flow, checkpoint)
        trackScreenViewIfNeeded(checkpoint.progress.position)
        _uiState.value = checkpointUiStateFactory(
            checkpoint = checkpoint,
            flow = flow,
            progressSourceCheckpoint = progressCheckpoint ?: checkpoint,
            status = status,
        )
    }

    private fun runOperation(operation: suspend () -> Unit) {
        if (operationMutex.isLocked) return
        viewModelScope.launch {
            if (!operationMutex.tryLock()) return@launch
            try {
                operation()
            } finally {
                operationMutex.unlock()
            }
        }
    }

    private fun trackCurrentSectionAction(isNext: Boolean) {
        val sectionId = checkpoint?.progress?.position?.sectionId?.value ?: return
        analyticsTracker.track(
            if (isNext) {
                OnboardingAnalyticsEvent.NextClicked(sectionId)
            } else {
                OnboardingAnalyticsEvent.BackClicked(sectionId)
            },
        )
    }

    private fun trackScreenViewIfNeeded(position: OnboardingPosition) {
        if (position == lastTrackedPosition) return
        lastTrackedPosition = position
        analyticsTracker.track(
            OnboardingAnalyticsEvent.ScreenView(
                sectionId = position.sectionId.value,
                stepId = position.stepId.value,
            ),
        )
    }

    private fun trackCompletionIfNeeded() {
        if (completionTracked) return
        completionTracked = true
        analyticsTracker.track(OnboardingAnalyticsEvent.OnboardingCompleted)
    }
}

private fun OnboardingRouteContext.mergedWith(
    initialRouteContext: OnboardingRouteContext,
): OnboardingRouteContext {
    return copy(
        entryPath = if (initialRouteContext.entryPath != com.tnyx.features.onboarding.domain.model.OnboardingEntryPath.GetStarted) {
            initialRouteContext.entryPath
        } else {
            entryPath
        },
        authState = if (initialRouteContext.authState != OnboardingAuthState.SignedOut) {
            initialRouteContext.authState
        } else {
            authState
        },
        signupCompleted = signupCompleted || initialRouteContext.signupCompleted,
        workoutPlanEnabled = initialRouteContext.workoutPlanEnabled ?: workoutPlanEnabled,
        mobilePresent = mobilePresent || initialRouteContext.mobilePresent,
        mobileVerified = mobileVerified || initialRouteContext.mobileVerified,
        namePrefilled = namePrefilled || initialRouteContext.namePrefilled,
        authRequired = authRequired || initialRouteContext.authRequired,
    )
}

private object EmptyAuthSessionProvider : AuthSessionProvider {
    override fun observeSession() = kotlinx.coroutines.flow.flowOf(null)

    override fun currentSession() = null
}
