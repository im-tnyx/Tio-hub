package com.tnyx.features.nutrition.presentation.meal_editor

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tnyx.features.nutrition.domain.models.MealItem
import com.tnyx.features.nutrition.domain.models.MealPhotoUpdate
import com.tnyx.features.nutrition.domain.models.NutritionMeal
import com.tnyx.features.nutrition.domain.repository.NutritionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

@HiltViewModel
class MealEditorViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val nutritionRepository: NutritionRepository,
) : ViewModel() {

    private val mealId = savedStateHandle.get<String>("mealId")
    private val initialItemJson = savedStateHandle.get<String>("initialItemJson")
    private val initialMealJson = savedStateHandle.get<String>("initialMealJson")
    private val logDate: LocalDate = savedStateHandle.get<String>("date")
        ?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
        ?: LocalDate.now()

    private val _uiState = MutableStateFlow(
        MealEditorUiState(
            meal = NutritionMeal(
                id = mealId.orEmpty(),
                name = "",
                type = "BREAKFAST",
            ),
            logDateTime = logDate.atTime(
                LocalDateTime.now().withSecond(0).withNano(0).toLocalTime(),
            ),
        ),
    )
    val uiState = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<MealEditorEffect>()
    val effect = _effect.asSharedFlow()

    private var pendingPhotoUpdate: MealPhotoUpdate = MealPhotoUpdate.Unchanged

    init {
        if (!mealId.isNullOrBlank()) {
            loadMeal(mealId)
        } else if (!initialMealJson.isNullOrBlank()) {
            acceptInitialMeal(initialMealJson)
        } else if (!initialItemJson.isNullOrBlank()) {
            acceptItemResult(initialItemJson)
        }
    }

    private fun acceptInitialMeal(serializedMeal: String) {
        runCatching { Json.decodeFromString<NutritionMeal>(serializedMeal) }
            .onSuccess { meal ->
                _uiState.update { state -> state.copy(meal = meal, errorMessage = null) }
            }
            .onFailure {
                _uiState.update { state ->
                    state.copy(errorMessage = "Detected meal could not be opened.")
                }
            }
    }

    fun acceptItemResult(serializedItem: String) {
        runCatching { Json.decodeFromString<MealItem>(serializedItem) }
            .onSuccess { handleAction(MealEditorAction.ItemUpserted(it)) }
            .onFailure {
                _uiState.update { state ->
                    state.copy(errorMessage = "The food item could not be restored. Please try again.")
                }
            }
    }

    fun acceptItemRemoval(itemId: String) {
        handleAction(MealEditorAction.ItemDeleted(itemId))
    }

    fun handleAction(action: MealEditorAction) {
        when (action) {
            is MealEditorAction.NameChanged -> {
                _uiState.update {
                    it.copy(meal = it.meal.copy(name = action.name), errorMessage = null)
                }
            }
            is MealEditorAction.NameEditorInputChanged -> {
                _uiState.update {
                    it.copy(nameInput = action.name, nameEditorError = null)
                }
            }
            MealEditorAction.NameEditorConfirmed -> confirmNameEditor()
            MealEditorAction.NameEditorDismissed -> {
                _uiState.update {
                    it.copy(isNameEditorVisible = false, nameEditorError = null)
                }
            }
            MealEditorAction.ServingCountEditorRequested -> {
                _uiState.update {
                    it.copy(
                        isServingCountEditorVisible = true,
                        servingCountInput = it.meal.servingSize.toCleanAmount(),
                        servingCountError = null,
                    )
                }
            }
            is MealEditorAction.ServingCountChanged -> {
                val normalized = action.count.filter { character ->
                    character.isDigit() || character == '.'
                }
                _uiState.update {
                    it.copy(servingCountInput = normalized, servingCountError = null)
                }
            }
            MealEditorAction.ServingCountEditorConfirmed -> confirmServingCountEditor()
            MealEditorAction.ServingCountEditorDismissed -> {
                _uiState.update {
                    it.copy(isServingCountEditorVisible = false, servingCountError = null)
                }
            }
            MealEditorAction.ServingEditorRequested -> openServingEditor()
            is MealEditorAction.ServingAmountChanged -> {
                val normalized = action.amount.filter { character ->
                    character.isDigit() || character == '.'
                }
                _uiState.update {
                    it.copy(
                        servingAmountInput = normalized,
                        servingEditorError = null,
                    )
                }
            }
            is MealEditorAction.ServingUnitSelected -> {
                _uiState.update {
                    it.copy(servingUnitInput = action.unit, servingEditorError = null)
                }
            }
            MealEditorAction.ServingEditorConfirmed -> confirmServingEditor()
            MealEditorAction.ServingEditorDismissed -> {
                _uiState.update {
                    it.copy(isServingEditorVisible = false, servingEditorError = null)
                }
            }
            MealEditorAction.PhotoClicked -> {
                _uiState.update { it.copy(isPhotoSourceVisible = true, errorMessage = null) }
            }
            MealEditorAction.PhotoSourceDismissed -> {
                _uiState.update { it.copy(isPhotoSourceVisible = false) }
            }
            MealEditorAction.CameraClicked -> launchPhotoSource(MealEditorEffect.LaunchCamera)
            MealEditorAction.GalleryClicked -> launchPhotoSource(MealEditorEffect.LaunchGallery)
            is MealEditorAction.PhotoSelected -> selectPhoto(action.bytes, action.mimeType)
            is MealEditorAction.PhotoSelectionFailed -> {
                _uiState.update {
                    it.copy(isPhotoSourceVisible = false, errorMessage = action.message)
                }
            }
            MealEditorAction.PhotoRemoved -> {
                pendingPhotoUpdate = MealPhotoUpdate.Remove
                _uiState.update {
                    it.copy(
                        meal = it.meal.copy(imageUrl = null),
                        isPhotoSourceVisible = false,
                        photoPreviewBytes = null,
                        errorMessage = null,
                    )
                }
            }
            is MealEditorAction.CategoryChanged -> {
                _uiState.update {
                    it.copy(meal = it.meal.copy(type = action.category), errorMessage = null)
                }
            }
            MealEditorAction.LogDatePickerRequested -> {
                _uiState.update { it.copy(isLogDatePickerVisible = true) }
            }
            is MealEditorAction.LogDateTimeChanged -> {
                _uiState.update {
                    it.copy(logDateTime = action.dateTime)
                }
            }
            MealEditorAction.LogDatePickerDismissed -> {
                _uiState.update { it.copy(isLogDatePickerVisible = false) }
            }
            is MealEditorAction.ItemDeleted -> {
                _uiState.update {
                    it.copy(
                        meal = it.meal.copy(
                            items = it.meal.items.filterNot { item -> item.id == action.itemId },
                        ),
                        errorMessage = null,
                    )
                }
            }
            is MealEditorAction.ItemClicked -> {
                viewModelScope.launch {
                    _effect.emit(MealEditorEffect.NavigateToItemEditor(action.item))
                }
            }
            is MealEditorAction.ItemUpserted -> upsertDraftItem(action.item)
            is MealEditorAction.ItemQuantityChanged -> {
                _uiState.update {
                    it.copy(
                        meal = it.meal.copy(
                            items = it.meal.items.map { item ->
                                if (item.id == action.itemId) {
                                    item.copy(quantity = action.quantity)
                                } else {
                                    item
                                }
                            },
                        ),
                    )
                }
            }
            MealEditorAction.AddItemClicked -> {
                viewModelScope.launch {
                    _effect.emit(
                        MealEditorEffect.NavigateToSearch(
                            _uiState.value.logDateTime.toLocalDate(),
                        ),
                    )
                }
            }
            MealEditorAction.SaveClicked -> saveMeal()
            MealEditorAction.DeleteMealClicked -> deleteMeal()
            MealEditorAction.BackClicked -> {
                viewModelScope.launch { _effect.emit(MealEditorEffect.NavigateBack) }
            }
            MealEditorAction.ShareClicked -> {
                viewModelScope.launch { _effect.emit(MealEditorEffect.ShowShareOptions) }
            }
            MealEditorAction.EditNameRequested -> {
                _uiState.update {
                    it.copy(
                        isNameEditorVisible = true,
                        nameInput = it.meal.name,
                        nameEditorError = null,
                    )
                }
            }
        }
    }

    private fun confirmNameEditor() {
        val name = _uiState.value.nameInput.trim()
        if (name.isBlank()) {
            _uiState.update { it.copy(nameEditorError = "Enter a meal name.") }
            return
        }
        _uiState.update {
            it.copy(
                meal = it.meal.copy(name = name),
                isNameEditorVisible = false,
                nameEditorError = null,
                errorMessage = null,
            )
        }
    }

    private fun loadMeal(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching { nutritionRepository.getMealLog(id) }
                .onSuccess { meal ->
                    _uiState.update {
                        if (meal == null) {
                            it.copy(isLoading = false, errorMessage = "Meal was not found.")
                        } else {
                            it.copy(
                                meal = meal,
                                logDateTime = meal.loggedAtEpochMillis
                                    ?.let { epochMillis ->
                                        Instant.ofEpochMilli(epochMillis)
                                            .atZone(ZoneId.systemDefault())
                                            .toLocalDateTime()
                                    }
                                    ?: it.logDateTime,
                                isLoading = false,
                            )
                        }
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.toUserMessage("Meal could not be loaded."),
                        )
                    }
                }
        }
    }

    private fun upsertDraftItem(item: MealItem) {
        _uiState.update { state ->
            val items = state.meal.items.toMutableList()
            val existingIndex = items.indexOfFirst { it.id == item.id }
            if (existingIndex >= 0) {
                items[existingIndex] = item
            } else {
                items += item
            }
            state.copy(
                meal = state.meal.copy(
                    name = state.meal.name.ifBlank { item.name },
                    servingsDescription = state.meal.servingsDescription.ifBlank {
                        item.unit.trim()
                    },
                    items = items,
                ),
                errorMessage = null,
            )
        }
    }

    private fun openServingEditor() {
        _uiState.update { state ->
            val parsedServing = parseServingDescription(
                description = state.meal.servingsDescription,
                fallbackUnit = state.meal.items.firstOrNull()?.unit.orEmpty(),
            )
            val options = servingUnitOptions(
                meal = state.meal,
                currentUnit = parsedServing.unit,
            )
            state.copy(
                isServingEditorVisible = true,
                servingAmountInput = parsedServing.amount,
                servingUnitInput = parsedServing.unit,
                servingUnitOptions = options,
                servingEditorError = null,
            )
        }
    }

    private fun confirmServingCountEditor() {
        val count = _uiState.value.servingCountInput.toDoubleOrNull()
        if (count == null || !count.isFinite() || count <= 0.0 || count > MAX_MEAL_SERVINGS) {
            _uiState.update {
                it.copy(servingCountError = "Enter servings between 0 and 99.")
            }
            return
        }
        _uiState.update {
            it.copy(
                meal = it.meal.copy(servingSize = count),
                isServingCountEditorVisible = false,
                servingCountError = null,
                errorMessage = null,
            )
        }
    }

    private fun confirmServingEditor() {
        val state = _uiState.value
        val amount = state.servingAmountInput.toDoubleOrNull()
        if (amount == null || !amount.isFinite() || amount <= 0.0) {
            _uiState.update {
                it.copy(servingEditorError = "Enter an amount greater than zero.")
            }
            return
        }
        val unit = state.servingUnitInput.trim()
        if (unit.isBlank()) {
            _uiState.update { it.copy(servingEditorError = "Select a serving unit.") }
            return
        }

        _uiState.update {
            it.copy(
                meal = it.meal.copy(
                    servingsDescription = "${amount.toCleanAmount()} $unit",
                ),
                isServingEditorVisible = false,
                servingEditorError = null,
                errorMessage = null,
            )
        }
    }

    private fun saveMeal() {
        val validationError = validateMeal(_uiState.value.meal)
        if (validationError != null) {
            _uiState.update { it.copy(errorMessage = validationError) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            runCatching {
                nutritionRepository.saveMealLogWithPhoto(
                    loggedAt = _uiState.value.logDateTime,
                    meal = _uiState.value.meal,
                    photoUpdate = pendingPhotoUpdate,
                )
            }.onSuccess { savedMeal ->
                pendingPhotoUpdate = MealPhotoUpdate.Unchanged
                _uiState.update { it.copy(meal = savedMeal, isSaving = false) }
                _effect.emit(MealEditorEffect.MealSaved)
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = error.toUserMessage("Meal could not be saved."),
                    )
                }
            }
        }
    }

    private fun deleteMeal() {
        val id = _uiState.value.meal.id
        if (id.isBlank()) {
            viewModelScope.launch { _effect.emit(MealEditorEffect.NavigateBack) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            runCatching { nutritionRepository.deleteMealLog(id) }
                .onSuccess {
                    _uiState.update { it.copy(isSaving = false) }
                    _effect.emit(MealEditorEffect.MealDeleted)
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            errorMessage = error.toUserMessage("Meal could not be deleted."),
                        )
                    }
                }
        }
    }

    private fun validateMeal(meal: NutritionMeal): String? {
        if (meal.name.isBlank()) return "Enter a meal name."
        if (meal.items.isEmpty()) return "Add at least one food item."
        return null
    }

    private fun Throwable.toUserMessage(fallback: String): String {
        return message?.takeIf { it.isNotBlank() } ?: fallback
    }

    private fun launchPhotoSource(effect: MealEditorEffect) {
        _uiState.update { it.copy(isPhotoSourceVisible = false, errorMessage = null) }
        viewModelScope.launch { _effect.emit(effect) }
    }

    private fun selectPhoto(bytes: ByteArray, mimeType: String) {
        val normalizedMimeType = mimeType.lowercase()
        val error = when {
            normalizedMimeType !in SUPPORTED_MEAL_PHOTO_MIME_TYPES ->
                "Select a JPEG, PNG, or WebP image."
            bytes.isEmpty() -> "Selected meal photo is empty."
            bytes.size > MAX_MEAL_PHOTO_BYTES -> "Meal photo is too large. Maximum size is 10 MB."
            else -> null
        }
        if (error != null) {
            _uiState.update { it.copy(errorMessage = error) }
            return
        }

        pendingPhotoUpdate = MealPhotoUpdate.Replace(bytes, normalizedMimeType)
        _uiState.update {
            it.copy(
                isPhotoSourceVisible = false,
                photoPreviewBytes = bytes,
                errorMessage = null,
            )
        }
    }
}

private data class ParsedServing(
    val amount: String,
    val unit: String,
)

private fun parseServingDescription(
    description: String,
    fallbackUnit: String,
): ParsedServing {
    val source = description.trim().ifBlank { fallbackUnit.trim() }
    val match = SERVING_DESCRIPTION_PATTERN.matchEntire(source)
    val amount = match?.groupValues?.getOrNull(1)?.replace(',', '.') ?: "1"
    val unit = match?.groupValues?.getOrNull(2)?.trim().orEmpty()
        .ifBlank { source.ifBlank { "serving" } }
    return ParsedServing(amount = amount, unit = unit)
}

private fun servingUnitOptions(
    meal: NutritionMeal,
    currentUnit: String,
): List<String> {
    val item = meal.items.firstOrNull()
    val context = listOf(currentUnit, item?.unit, item?.name)
        .filterNotNull()
        .joinToString(" ")
        .lowercase()
    val units = when {
        SOLID_UNIT_MARKERS.any(context::contains) -> SOLID_SERVING_UNITS
        LIQUID_UNIT_MARKERS.any(context::contains) -> LIQUID_SERVING_UNITS
        LIQUID_FOOD_MARKERS.any(context::contains) -> LIQUID_SERVING_UNITS
        else -> GENERAL_SERVING_UNITS
    }
    return buildList {
        currentUnit.takeIf(String::isNotBlank)?.let(::add)
        units.forEach { unit -> if (unit !in this) add(unit) }
    }
}

private fun Double.toCleanAmount(): String {
    return if (this % 1.0 == 0.0) toLong().toString() else toString().trimEnd('0').trimEnd('.')
}

private val SERVING_DESCRIPTION_PATTERN = Regex("^([0-9]+(?:[.,][0-9]+)?)\\s*(.*)$")
private val SOLID_UNIT_MARKERS = listOf("kg", "gram", " g", "piece", "slice", "bowl", "plate", "oz")
private val LIQUID_UNIT_MARKERS = listOf("ml", "millil", "litre", "liter", "tsp", "tbsp", "glass", "bottle")
private val LIQUID_FOOD_MARKERS = listOf("milk", "water", "juice", "shake", "coffee", "tea", "drink", "oil", "soup", "sauce")
private val SOLID_SERVING_UNITS = listOf("g", "kg", "piece", "slice", "cup")
private val LIQUID_SERVING_UNITS = listOf("ml", "L", "tsp", "tbsp", "cup", "glass")
private val GENERAL_SERVING_UNITS = listOf("serving", "piece", "g", "ml", "cup")
private const val MAX_MEAL_SERVINGS = 99.0
private const val MAX_MEAL_PHOTO_BYTES = 10 * 1024 * 1024
private val SUPPORTED_MEAL_PHOTO_MIME_TYPES = setOf(
    "image/jpeg",
    "image/jpg",
    "image/png",
    "image/webp",
)
