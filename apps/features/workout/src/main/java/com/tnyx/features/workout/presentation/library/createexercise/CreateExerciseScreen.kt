package com.tnyx.features.workout.presentation.library.createexercise

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Accessibility
import androidx.compose.material.icons.outlined.AccessibilityNew
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tnyx.core.theme.TnyxTheme
import com.tnyx.core.theme.tokens.foundation.TnyxDimens
import com.tnyx.core.ui.components.buttons.TnyxPrimaryButton
import com.tnyx.core.ui.components.cards.TnyxCard
import com.tnyx.core.ui.components.cards.TnyxCardVariant

import androidx.compose.foundation.layout.navigationBarsPadding

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateExerciseScreen(
    state: CreateExerciseUiState,
    onAction: (CreateExerciseAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier.height(56.dp),
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = TnyxTheme.colors.surface,
                )
            )
        },
        bottomBar = {
            Surface(
                color = TnyxTheme.colors.surface,
                tonalElevation = TnyxDimens.BorderSubtle,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = TnyxDimens.SpaceM, vertical = TnyxDimens.SpaceSM)
                ) {
                    TnyxPrimaryButton(
                        text = "Save Exercise",
                        onPressed = { onAction(CreateExerciseAction.SaveClicked) },
                        enabled = !state.isSaving,
                        expand = true,
                        height = TnyxDimens.ButtonHeight
                    )
                }
            }
        },
        containerColor = TnyxTheme.colors.background,
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = TnyxDimens.SpaceM, vertical = TnyxDimens.SpaceSM),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Hero Camera / Add Asset Section
            Spacer(modifier = Modifier.height(TnyxDimens.SpaceS))
            Surface(
                shape = CircleShape,
                color = TnyxTheme.colors.surfaceVariant,
                border = BorderStroke(TnyxDimens.BorderThin, TnyxTheme.colors.textMuted.copy(alpha = 0.3f)),
                modifier = Modifier
                    .size(TnyxDimens.HeaderGradientHeight)
                    .clickable { onAction(CreateExerciseAction.AddAssetClicked) }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Outlined.PhotoCamera,
                        contentDescription = "Camera",
                        tint = TnyxTheme.colors.textPrimary,
                        modifier = Modifier.size(TnyxDimens.IconL)
                    )
                }
            }

            Spacer(modifier = Modifier.height(TnyxDimens.SpaceSM))

            Text(
                text = "Add Asset",
                style = TnyxTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = TnyxTheme.colors.accent
                ),
                modifier = Modifier.clickable { onAction(CreateExerciseAction.AddAssetClicked) }
            )

            Spacer(modifier = Modifier.height(TnyxDimens.SpaceL))

            // Card 1: Basic Exercise Info
            TnyxCard(
                modifier = Modifier.fillMaxWidth(),
                variant = TnyxCardVariant.Surface,
                shape = RoundedCornerShape(TnyxDimens.RadiusM),
                padding = TnyxDimens.SpaceNone
            ) {
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
                            .padding(horizontal = TnyxDimens.SpaceS)
                    )

                    HorizontalDivider(
                        color = TnyxTheme.colors.background,
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
                            .padding(horizontal = TnyxDimens.SpaceS)
                    )
                }
            }

            Spacer(modifier = Modifier.height(TnyxDimens.SpaceM))

            // Card 2: Categorization & Details Options
            TnyxCard(
                modifier = Modifier.fillMaxWidth(),
                variant = TnyxCardVariant.Surface,
                shape = RoundedCornerShape(TnyxDimens.RadiusM),
                padding = TnyxDimens.SpaceNone
            ) {
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
                        color = TnyxTheme.colors.background,
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
                        color = TnyxTheme.colors.background,
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
                        color = TnyxTheme.colors.background,
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
                        color = TnyxTheme.colors.background,
                        thickness = TnyxDimens.BorderThin
                    )

                    // Exercise Type Row
                    CreateExerciseOptionRow(
                        icon = Icons.Outlined.Repeat,
                        title = "Exercise Type",
                        selectedText = state.exerciseType,
                        onClick = { onAction(CreateExerciseAction.ExerciseTypeClicked) }
                    )
                }
            }
        }
    }
}

@Composable
private fun CreateExerciseOptionRow(
    icon: ImageVector,
    title: String,
    selectedText: String,
    hasOptionalText: Boolean = false,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = TnyxDimens.SpaceSM, horizontal = TnyxDimens.SpaceM),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = TnyxTheme.colors.textSecondary,
                modifier = Modifier.size(TnyxDimens.IconS)
            )

            Spacer(modifier = Modifier.width(TnyxDimens.SpaceSM))

            Column {
                Text(
                    text = title,
                    style = TnyxTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = TnyxTheme.colors.textPrimary,
                )
                Spacer(modifier = Modifier.height(TnyxDimens.SpaceXXS))
                if (hasOptionalText) {
                    Row {
                        Text(
                            text = "Select ",
                            style = TnyxTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                            color = TnyxTheme.colors.accent,
                        )
                        Text(
                            text = "(optional)",
                            style = TnyxTheme.typography.bodySmall,
                            color = TnyxTheme.colors.textSecondary,
                        )
                    }
                } else {
                    Text(
                        text = selectedText,
                        style = TnyxTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = TnyxTheme.colors.accent,
                    )
                }
            }
        }

        Icon(
            imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
            contentDescription = "Select $title",
            tint = TnyxTheme.colors.textSecondary,
            modifier = Modifier.size(TnyxDimens.IconS)
        )
    }
}
