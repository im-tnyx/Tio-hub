package com.tnyx.features.workout.presentation.library.createexercise

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tnyx.core.theme.TnyxTheme

private val AccentBlue = Color(0xFF3B82F6)

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
                title = {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Create Exercise",
                            style = TnyxTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 18.sp
                            ),
                            color = TnyxTheme.colors.textPrimary,
                        )
                    }
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
                    Button(
                        onClick = { onAction(CreateExerciseAction.SaveClicked) },
                        enabled = !state.isSaving,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AccentBlue,
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Text(
                            text = "Save",
                            style = TnyxTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = TnyxTheme.colors.background,
                )
            )
        },
        containerColor = TnyxTheme.colors.background,
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Hero Camera / Add Asset Section
            Spacer(modifier = Modifier.height(12.dp))
            Surface(
                shape = CircleShape,
                color = Color.Black,
                border = BorderStroke(1.dp, TnyxTheme.colors.textPrimary.copy(alpha = 0.2f)),
                modifier = Modifier
                    .size(110.dp)
                    .clickable { onAction(CreateExerciseAction.AddAssetClicked) }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Outlined.PhotoCamera,
                        contentDescription = "Camera",
                        tint = TnyxTheme.colors.textPrimary,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Add Asset",
                style = TnyxTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = AccentBlue
                ),
                modifier = Modifier.clickable { onAction(CreateExerciseAction.AddAssetClicked) }
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Exercise Name Field
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
                modifier = Modifier.fillMaxWidth()
            )

            HorizontalDivider(
                color = TnyxTheme.colors.textPrimary.copy(alpha = 0.12f),
                thickness = 1.dp
            )

            // Equipment Selector Row
            CreateExerciseOptionRow(
                title = "Equipment",
                selectedText = state.equipment,
                onClick = { onAction(CreateExerciseAction.EquipmentClicked) }
            )

            HorizontalDivider(
                color = TnyxTheme.colors.textPrimary.copy(alpha = 0.12f),
                thickness = 1.dp
            )

            // Primary Muscle Group Row
            CreateExerciseOptionRow(
                title = "Primary Muscle Group",
                selectedText = state.primaryMuscleGroup,
                onClick = { onAction(CreateExerciseAction.PrimaryMuscleClicked) }
            )

            HorizontalDivider(
                color = TnyxTheme.colors.textPrimary.copy(alpha = 0.12f),
                thickness = 1.dp
            )

            // Other Muscles Row
            CreateExerciseOptionRow(
                title = "Other Muscles",
                selectedText = state.otherMuscles,
                hasOptionalText = state.otherMuscles.contains("optional"),
                onClick = { onAction(CreateExerciseAction.OtherMusclesClicked) }
            )

            HorizontalDivider(
                color = TnyxTheme.colors.textPrimary.copy(alpha = 0.12f),
                thickness = 1.dp
            )

            // Exercise Type Row
            CreateExerciseOptionRow(
                title = "Exercise Type",
                selectedText = state.exerciseType,
                onClick = { onAction(CreateExerciseAction.ExerciseTypeClicked) }
            )
        }
    }
}

@Composable
private fun CreateExerciseOptionRow(
    title: String,
    selectedText: String,
    hasOptionalText: Boolean = false,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = title,
                style = TnyxTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = TnyxTheme.colors.textPrimary,
            )
            Spacer(modifier = Modifier.height(4.dp))
            if (hasOptionalText) {
                Row {
                    Text(
                        text = "Select ",
                        style = TnyxTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = AccentBlue,
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
                    color = AccentBlue,
                )
            }
        }

        Icon(
            imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
            contentDescription = "Select $title",
            tint = TnyxTheme.colors.textSecondary,
            modifier = Modifier.size(20.dp)
        )
    }
}
