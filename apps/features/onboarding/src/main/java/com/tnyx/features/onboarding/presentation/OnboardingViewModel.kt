package com.tnyx.features.onboarding.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tnyx.features.onboarding.domain.flow.DefaultOnboardingFlow
import com.tnyx.features.onboarding.domain.flow.OnboardingCheckpointResolver
import com.tnyx.features.onboarding.domain.model.OnboardingAnswer
import com.tnyx.features.onboarding.domain.model.OnboardingCheckpoint
import com.tnyx.features.onboarding.domain.model.OnboardingFlowDefinition
import com.tnyx.features.onboarding.domain.model.OnboardingPosition
import com.tnyx.features.onboarding.domain.repository.OnboardingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
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
) : ViewModel() {
    private val flow: OnboardingFlowDefinition = DefaultOnboardingFlow.definition
    private val resolver = OnboardingCheckpointResolver()
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
                    val storedCheckpoint = repository.observeCheckpoint().first()
                    val resolvedCheckpoint = resolver.resolve(storedCheckpoint, flow)
                    if (storedCheckpoint != resolvedCheckpoint) {
                        repository.saveCheckpoint(resolvedCheckpoint)
                    }
                    checkpoint = resolvedCheckpoint
                    render(resolvedCheckpoint)
                    if (resolvedCheckpoint.progress.isCompleted) {
                        effectChannel.send(OnboardingEffect.Completed)
                    }
                } catch (exception: Exception) {
                    if (exception is CancellationException) throw exception
                    initializationStarted = false
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            hasPersistenceError = true,
                        )
                    }
                }
            }
        }
    }

    private fun retry() {
        val currentCheckpoint = checkpoint
        if (currentCheckpoint == null) {
            initialize()
            return
        }

        runOperation {
            saveAndRender(currentCheckpoint)
        }
    }

    private suspend fun updateCurrentAnswer(answer: OnboardingAnswer?) {
        val currentCheckpoint = checkpoint ?: return
        val stepId = currentCheckpoint.progress.position.stepId
        val updatedDraft = if (answer == null) {
            currentCheckpoint.draft.withoutAnswer(stepId)
        } else {
            currentCheckpoint.draft.withAnswer(stepId, answer)
        }
        saveAndRender(currentCheckpoint.copy(draft = updatedDraft))
    }

    private suspend fun continueForward() {
        val currentCheckpoint = checkpoint ?: return
        val currentState = _uiState.value
        if (!currentState.canContinue) {
            _uiState.update {
                it.copy(validationError = OnboardingValidationError.RequiredAnswerMissing)
            }
            return
        }

        val currentPosition = currentCheckpoint.progress.position
        val nextPosition = flow.next(currentPosition)
        if (nextPosition == null) {
            val completedCheckpoint = currentCheckpoint.copy(
                progress = currentCheckpoint.progress.copy(
                    completedSectionIds = currentCheckpoint.progress.completedSectionIds +
                        currentPosition.sectionId,
                    isCompleted = true,
                ),
            )
            if (saveAndRender(completedCheckpoint)) {
                effectChannel.send(OnboardingEffect.Completed)
            }
            return
        }

        val completedSections = if (nextPosition.sectionId != currentPosition.sectionId) {
            currentCheckpoint.progress.completedSectionIds + currentPosition.sectionId
        } else {
            currentCheckpoint.progress.completedSectionIds
        }
        saveAndRender(
            currentCheckpoint.copy(
                progress = currentCheckpoint.progress.copy(
                    position = nextPosition,
                    completedSectionIds = completedSections,
                ),
            ),
        )
    }

    private suspend fun navigateBack() {
        val currentCheckpoint = checkpoint
        if (currentCheckpoint == null) {
            effectChannel.send(OnboardingEffect.Exit)
            return
        }
        val previousPosition = flow.previous(currentCheckpoint.progress.position)
        if (previousPosition == null) {
            effectChannel.send(OnboardingEffect.Exit)
            return
        }

        saveAndRender(
            currentCheckpoint.copy(
                progress = currentCheckpoint.progress.copy(
                    position = previousPosition,
                    isCompleted = false,
                ),
            ),
        )
    }

    private suspend fun skipCurrentSection() {
        val currentCheckpoint = checkpoint ?: return
        val currentPosition = currentCheckpoint.progress.position
        val currentSection = flow.sections.first { section ->
            section.id == currentPosition.sectionId
        }
        if (!currentSection.isSkippable) return

        var nextPosition: OnboardingPosition? = flow.next(currentPosition)
        while (nextPosition?.sectionId == currentPosition.sectionId) {
            nextPosition = flow.next(nextPosition)
        }
        val targetPosition = nextPosition ?: return

        saveAndRender(
            currentCheckpoint.copy(
                progress = currentCheckpoint.progress.copy(
                    position = targetPosition,
                    isCompleted = false,
                ),
            ),
        )
    }

    private suspend fun saveAndRender(updatedCheckpoint: OnboardingCheckpoint): Boolean {
        checkpoint = updatedCheckpoint
        render(
            updatedCheckpoint,
            isSaving = true,
        )

        return try {
            repository.saveCheckpoint(updatedCheckpoint)
            render(updatedCheckpoint)
            true
        } catch (exception: Exception) {
            if (exception is CancellationException) throw exception
            render(
                updatedCheckpoint,
                hasPersistenceError = true,
            )
            false
        }
    }

    private fun render(
        checkpoint: OnboardingCheckpoint,
        isSaving: Boolean = false,
        hasPersistenceError: Boolean = false,
    ) {
        val position = checkpoint.progress.position
        val sectionIndex = flow.sections.indexOfFirst { section -> section.id == position.sectionId }
        val section = flow.sections[sectionIndex]
        val step = section.steps.first { definition -> definition.id == position.stepId }
        val stepIndex = flow.sections
            .take(sectionIndex)
            .sumOf { definition -> definition.steps.size } +
            section.steps.indexOf(step)
        val currentAnswer = checkpoint.draft.answerFor(position.stepId)
        val hasRequiredAnswer = !step.isRequired || currentAnswer.isMeaningful()

        _uiState.value = OnboardingUiState(
            isLoading = false,
            isSaving = isSaving,
            position = position,
            currentAnswer = currentAnswer,
            completedFraction = flow.completedFraction(position),
            sectionNumber = sectionIndex + 1,
            sectionCount = flow.sections.size,
            stepNumber = stepIndex + 1,
            totalSteps = flow.totalSteps,
            canContinue = !checkpoint.progress.isCompleted && hasRequiredAnswer,
            canSkipSection = !checkpoint.progress.isCompleted && section.isSkippable,
            isLastStep = flow.next(position) == null,
            validationError = null,
            hasPersistenceError = hasPersistenceError,
        )
    }

    private fun runOperation(operation: suspend () -> Unit) {
        viewModelScope.launch {
            operationMutex.withLock {
                operation()
            }
        }
    }

    private fun OnboardingAnswer?.isMeaningful(): Boolean {
        return when (this) {
            null -> false
            is OnboardingAnswer.Text -> value.isNotBlank()
            is OnboardingAnswer.Decimal -> true
            is OnboardingAnswer.Selections -> values.isNotEmpty()
            is OnboardingAnswer.Toggle -> true
        }
    }
}
