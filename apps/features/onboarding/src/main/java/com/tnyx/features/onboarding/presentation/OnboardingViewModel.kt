package com.tnyx.features.onboarding.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tnyx.features.onboarding.domain.flow.DefaultOnboardingFlow
import com.tnyx.features.onboarding.domain.model.OnboardingAnswer
import com.tnyx.features.onboarding.domain.model.OnboardingCheckpoint
import com.tnyx.features.onboarding.domain.model.OnboardingFlowDefinition
import com.tnyx.features.onboarding.domain.repository.OnboardingRepository
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
import com.tnyx.features.onboarding.domain.usecase.ResolveBackNavigationResult
import com.tnyx.features.onboarding.domain.usecase.ResolveBackNavigationUseCase
import com.tnyx.features.onboarding.domain.usecase.RetryOnboardingSessionResult
import com.tnyx.features.onboarding.domain.usecase.RetryOnboardingSessionUseCase
import com.tnyx.features.onboarding.domain.usecase.SeedOnboardingRecommendationsUseCase
import com.tnyx.features.onboarding.domain.usecase.SkipOnboardingSectionUseCase
import com.tnyx.features.onboarding.domain.usecase.UpdateOnboardingAnswerUseCase
import com.tnyx.shared.profile.domain.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val repository: OnboardingRepository,
    private val profileRepository: ProfileRepository,
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
    private val seedOnboardingRecommendations: SeedOnboardingRecommendationsUseCase =
        SeedOnboardingRecommendationsUseCase(),
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
    private var initializationStarted = false

    fun handleAction(action: OnboardingAction) {
        when (action) {
            OnboardingAction.Init -> initialize()
            OnboardingAction.Retry -> retry()
            OnboardingAction.BackClicked -> runOperation(::navigateBack)
            OnboardingAction.ContinueClicked -> runOperation(::continueForward)
            OnboardingAction.SkipSectionClicked -> runOperation(::skipCurrentSection)
            is OnboardingAction.AnswerChanged -> {
                runOperation { updateCurrentAnswer(action.answer) }
            }
        }
    }

    private fun initialize() {
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
                    when (
                        val result = initializeOnboardingSession(
                            flow = flow,
                            onboardingRepository = repository,
                            profileRepository = profileRepository,
                        )
                    ) {
                        InitializeOnboardingSessionResult.ProfileAlreadyCompleted -> {
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
                            applyCheckpointState(
                                checkpoint = result.checkpoint,
                                status = OnboardingCheckpointUiStatus.Ready,
                            )
                        }

                        is InitializeOnboardingSessionResult.ResumeCompletedCheckpoint -> {
                            if (completeOnboarding(
                                    completedCheckpoint = result.checkpoint,
                                    persistCheckpoint = result.persistCheckpoint,
                                )
                            ) {
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
                RetryOnboardingSessionResult.Reinitialize -> initialize()
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
        saveAndRender(updateOnboardingAnswer(currentCheckpoint, answer))
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

        val advanceResult = advanceOnboardingStep(currentCheckpoint, flow)
        val previewCheckpoint = when (advanceResult) {
            is AdvanceOnboardingStepResult.Completed -> advanceResult.checkpoint
            is AdvanceOnboardingStepResult.Next -> advanceResult.checkpoint
        }
        applyCheckpointState(
            checkpoint = previewCheckpoint,
            status = OnboardingCheckpointUiStatus.Saving,
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
        applyCheckpointState(
            checkpoint = completedCheckpoint,
            status = OnboardingCheckpointUiStatus.Saving,
        )

        return when (
            completeOnboardingUseCase(
                checkpoint = completedCheckpoint,
                persistCheckpoint = persistCheckpoint,
                onboardingRepository = repository,
                profileRepository = profileRepository,
                finalizeOnboardingProfile = finalizeOnboardingProfile,
            )
        ) {
            is CompleteOnboardingResult.Success -> {
                applyCheckpointState(
                    checkpoint = completedCheckpoint,
                    status = OnboardingCheckpointUiStatus.Ready,
                )
                true
            }

            is CompleteOnboardingResult.Failure -> {
                applyCheckpointState(
                    checkpoint = completedCheckpoint,
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
        applyCheckpointState(
            checkpoint = updatedCheckpoint,
            status = OnboardingCheckpointUiStatus.Saving,
        )

        return when (persistOnboardingCheckpoint(updatedCheckpoint, repository)) {
            is PersistOnboardingCheckpointResult.Success -> {
                applyCheckpointState(
                    checkpoint = updatedCheckpoint,
                    status = OnboardingCheckpointUiStatus.Ready,
                )
                true
            }

            is PersistOnboardingCheckpointResult.Failure -> {
                applyCheckpointState(
                    checkpoint = updatedCheckpoint,
                    status = OnboardingCheckpointUiStatus.PersistenceError,
                )
                false
            }
        }
    }

    private fun applyCheckpointState(
        checkpoint: OnboardingCheckpoint,
        status: OnboardingCheckpointUiStatus,
    ) {
        val checkpointWithRecommendations = seedOnboardingRecommendations(checkpoint)
        this.checkpoint = checkpointWithRecommendations
        render(
            checkpoint = checkpointWithRecommendations,
            status = status,
        )
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
        _uiState.value = checkpointUiStateFactory(
            checkpoint = checkpoint,
            flow = flow,
            status = status,
        )
    }

    private fun runOperation(operation: suspend () -> Unit) {
        viewModelScope.launch {
            operationMutex.withLock {
                operation()
            }
        }
    }
}
