package com.tnyx.features.workout.presentation.library.createexercise

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Accessibility
import androidx.compose.material.icons.outlined.AccessibilityNew
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.tnyx.core.theme.TnyxTheme
import com.tnyx.core.theme.tokens.foundation.TnyxDimens
import com.tnyx.core.R as CoreR
import com.tnyx.core.ui.components.buttons.TnyxPrimaryButton
import com.tnyx.features.workout.presentation.components.bodypart.BodyPartIconRegistry
import com.tnyx.features.workout.presentation.components.bodypart.toMuscleRegionKey
import com.tnyx.features.workout.presentation.library.createexercise.widgets.BodyPartSelectionBottomSheet
import com.tnyx.features.workout.presentation.library.createexercise.widgets.CreateExerciseOptionRow
import com.tnyx.features.workout.presentation.library.createexercise.widgets.EquipmentSelectionBottomSheet
import com.tnyx.features.workout.presentation.library.createexercise.widgets.ExerciseTypeSelectionBottomSheet
import com.tnyx.features.workout.presentation.library.createexercise.widgets.MuscleSelectionBottomSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateExerciseScreen(
    state: CreateExerciseUiState,
    snackbarHostState: SnackbarHostState,
    onAction: (CreateExerciseAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Create Exercise",
                        style = TnyxTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                        ),
                        color = TnyxTheme.colors.textPrimary,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { onAction(CreateExerciseAction.BackClicked) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TnyxTheme.colors.textPrimary,
                        )
                    }
                },
                actions = {
                    TnyxPrimaryButton(
                        text = "Save",
                        onPressed = { onAction(CreateExerciseAction.SaveClicked) },
                        enabled = !state.isSaving,
                        size = com.tnyx.core.ui.components.buttons.TnyxButtonSize.Compact,
                        modifier = Modifier.padding(end = TnyxDimens.SpaceS)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = TnyxTheme.colors.surface,
                )
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = TnyxTheme.colors.background,
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(vertical = TnyxDimens.SpaceSM),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Hero Camera / Add Asset Section
            val hasAsset = !state.assetUri.isNullOrBlank()
            val isVideoAsset = state.assetMimeType?.startsWith("video/") == true
            val assetLabel = if (hasAsset) "Replace Media" else "Add Media"

            Spacer(modifier = Modifier.height(TnyxDimens.SpaceM))
            Surface(
                onClick = { onAction(CreateExerciseAction.AddAssetClicked) },
                shape = CircleShape,
                color = TnyxTheme.colors.surfaceVariant,
                border = BorderStroke(TnyxDimens.BorderSubtle, TnyxTheme.colors.textSecondary.copy(alpha = 0.25f)),
                modifier = Modifier.size(110.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (hasAsset && isVideoAsset) {
                        Icon(
                            imageVector = Icons.Rounded.PlayCircle,
                            contentDescription = "Selected video",
                            tint = TnyxTheme.colors.accent,
                            modifier = Modifier.size(TnyxDimens.IconL),
                        )
                    } else if (hasAsset) {
                        AsyncImage(
                            model = state.assetUri,
                            contentDescription = "Asset Preview",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            painter = painterResource(id = CoreR.drawable.ic_camera),
                            contentDescription = "Camera",
                            tint = TnyxTheme.colors.textSecondary,
                            modifier = Modifier.size(TnyxDimens.IconM)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(TnyxDimens.SpaceSM))

            Text(
                text = assetLabel,
                style = TnyxTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium,
                    color = TnyxTheme.colors.accent
                ),
                modifier = Modifier.clickable { onAction(CreateExerciseAction.AddAssetClicked) }
            )

            Spacer(modifier = Modifier.height(TnyxDimens.SpaceXL))

            // Basic Exercise Info Section (Flat Layout)
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = state.exerciseName,
                    onValueChange = { onAction(CreateExerciseAction.NameChanged(it)) },
                    placeholder = {
                        Text(
                            text = "Exercise Name",
                            style = TnyxTheme.typography.bodyLarge,
                            color = TnyxTheme.colors.textSecondary.copy(alpha = 0.6f)
                        )
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedTextColor = TnyxTheme.colors.textPrimary,
                        unfocusedTextColor = TnyxTheme.colors.textPrimary,
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = TnyxDimens.SpaceM)
                )

                HorizontalDivider(
                    color = TnyxTheme.colors.textPrimary.copy(alpha = 0.12f),
                    thickness = TnyxDimens.BorderThin
                )

                OutlinedTextField(
                    value = state.instructions,
                    onValueChange = { onAction(CreateExerciseAction.InstructionsChanged(it)) },
                    placeholder = {
                        Text(
                            text = "Add instruction (optional)",
                            style = TnyxTheme.typography.bodyLarge,
                            color = TnyxTheme.colors.textSecondary.copy(alpha = 0.6f)
                        )
                    },
                    singleLine = false,
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedTextColor = TnyxTheme.colors.textPrimary,
                        unfocusedTextColor = TnyxTheme.colors.textPrimary,
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = TnyxDimens.SpaceM)
                )

                HorizontalDivider(
                    color = TnyxTheme.colors.textPrimary.copy(alpha = 0.12f),
                    thickness = TnyxDimens.BorderThin
                )
            }

            Spacer(modifier = Modifier.height(TnyxDimens.SpaceSM))

            // Categorization & Details Options Section (Flat Layout)
            Column(modifier = Modifier.fillMaxWidth()) {
                // Equipment Selector Row
                CreateExerciseOptionRow(
                    icon = Icons.Outlined.FitnessCenter,
                    title = "Equipment",
                    selectedText = state.equipment,
                    hasOptionalText = state.equipment.contains("optional"),
                    onClick = { onAction(CreateExerciseAction.EquipmentClicked) }
                )

                HorizontalDivider(
                    color = TnyxTheme.colors.textPrimary.copy(alpha = 0.12f),
                    thickness = TnyxDimens.BorderThin
                )

                // Body Part Row (Optional)
                CreateExerciseOptionRow(
                    icon = Icons.Outlined.Accessibility,
                    title = "Body Part",
                    selectedText = state.bodyPart,
                    hasOptionalText = state.bodyPart.contains("optional"),
                    onClick = { onAction(CreateExerciseAction.BodyPartClicked) }
                )

                HorizontalDivider(
                    color = TnyxTheme.colors.textPrimary.copy(alpha = 0.12f),
                    thickness = TnyxDimens.BorderThin
                )

                // Primary Muscle Group Row
                CreateExerciseOptionRow(
                    icon = Icons.Outlined.AccessibilityNew,
                    title = "Primary Muscle Group",
                    selectedText = state.primaryMuscleGroup,
                    onClick = { onAction(CreateExerciseAction.PrimaryMuscleClicked) }
                )

                HorizontalDivider(
                    color = TnyxTheme.colors.textPrimary.copy(alpha = 0.12f),
                    thickness = TnyxDimens.BorderThin
                )

                // Other Muscles Row (Optional)
                CreateExerciseOptionRow(
                    icon = Icons.Outlined.Layers,
                    title = "Other Muscles",
                    selectedText = state.otherMuscles,
                    hasOptionalText = state.otherMuscles.contains("optional"),
                    onClick = { onAction(CreateExerciseAction.OtherMusclesClicked) }
                )

                HorizontalDivider(
                    color = TnyxTheme.colors.textPrimary.copy(alpha = 0.12f),
                    thickness = TnyxDimens.BorderThin
                )

                // Exercise Type Row
                CreateExerciseOptionRow(
                    icon = Icons.Outlined.Repeat,
                    title = "Exercise Type",
                    selectedText = state.exerciseType,
                    onClick = { onAction(CreateExerciseAction.ExerciseTypeClicked) }
                )

                HorizontalDivider(
                    color = TnyxTheme.colors.textPrimary.copy(alpha = 0.12f),
                    thickness = TnyxDimens.BorderThin
                )
            }
        }
    }

    // Derive body part filter for muscle sheet (supports multi-selected body parts)
    val activeMuscleFilter = remember(state.bodyPart) {
        if (state.bodyPart.isBlank() || state.bodyPart.contains("optional", ignoreCase = true) || state.bodyPart.equals("Select", ignoreCase = true)) {
            null
        } else {
            state.bodyPart.split(",")
                .mapNotNull { BodyPartIconRegistry.resolve(it.trim())?.toMuscleRegionKey() }
                .distinct()
                .joinToString(",")
                .ifBlank { null }
        }
    }

    // Image Source Selection Bottom Sheet (Camera / Gallery / Remove Photo)
    com.tnyx.core.ui.components.sheets.ImageSourceBottomSheet(
        visible = state.showImageSourceBottomSheet,
        onDismissRequest = { onAction(CreateExerciseAction.ImageSourceBottomSheetDismissed) },
        onCameraClick = { onAction(CreateExerciseAction.CameraClicked) },
        onGalleryClick = { onAction(CreateExerciseAction.GalleryClicked) },
        onRemoveClick = if (!state.assetUri.isNullOrBlank()) {
            { onAction(CreateExerciseAction.RemoveAssetClicked) }
        } else null,
        title = "Select Exercise Media",
        removeText = "Remove Media",
    )

    // Equipment Selection Bottom Sheet
    EquipmentSelectionBottomSheet(
        visible = state.showEquipmentBottomSheet,
        selectedEquipment = state.equipment,
        onEquipmentSelected = { onAction(CreateExerciseAction.EquipmentSelected(it)) },
        onDismissRequest = { onAction(CreateExerciseAction.EquipmentBottomSheetDismissed) }
    )

    // Body Part Selection Bottom Sheet
    BodyPartSelectionBottomSheet(
        visible = state.showBodyPartBottomSheet,
        selectedBodyPart = state.bodyPart,
        onBodyPartSelected = { onAction(CreateExerciseAction.BodyPartSelected(it)) },
        onDismissRequest = { onAction(CreateExerciseAction.BodyPartBottomSheetDismissed) }
    )

    // Primary Muscle Group Selection Bottom Sheet
    MuscleSelectionBottomSheet(
        visible = state.showPrimaryMuscleBottomSheet,
        title = if (activeMuscleFilter != null) "Select Primary Muscles" else "Select Primary Muscle Group",
        selectedMuscle = state.primaryMuscleGroup,
        isMultiSelect = true,
        bodyPartFilter = activeMuscleFilter,
        onMuscleSelected = { onAction(CreateExerciseAction.PrimaryMuscleSelected(it)) },
        onDismissRequest = { onAction(CreateExerciseAction.PrimaryMuscleBottomSheetDismissed) }
    )

    // Other Muscles Selection Bottom Sheet (secondary blue tint)
    MuscleSelectionBottomSheet(
        visible = state.showOtherMusclesBottomSheet,
        title = "Select Other Muscles",
        selectedMuscle = state.otherMuscles,
        isMultiSelect = true,
        isSecondarySheet = true,
        onMuscleSelected = { onAction(CreateExerciseAction.OtherMusclesSelected(it)) },
        onDismissRequest = { onAction(CreateExerciseAction.OtherMusclesBottomSheetDismissed) }
    )

    // Exercise Type Selection Bottom Sheet
    ExerciseTypeSelectionBottomSheet(
        visible = state.showExerciseTypeBottomSheet,
        selectedExerciseType = state.exerciseType,
        onExerciseTypeSelected = { onAction(CreateExerciseAction.ExerciseTypeSelected(it)) },
        onDismissRequest = { onAction(CreateExerciseAction.ExerciseTypeBottomSheetDismissed) }
    )
}
