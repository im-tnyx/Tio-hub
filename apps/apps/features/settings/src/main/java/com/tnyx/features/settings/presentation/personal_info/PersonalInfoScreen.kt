package com.tnyx.features.settings.presentation.personal_info

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.tnyx.core.theme.TnyxTheme
import com.tnyx.core.ui.components.buttons.TnyxPrimaryButton
import com.tnyx.core.ui.components.buttons.TnyxSecondaryButton
import com.tnyx.core.ui.components.buttons.TnyxSecondaryVariant
import com.tnyx.core.ui.components.cards.TnyxCard
import com.tnyx.core.ui.components.cards.TnyxCardVariant
import com.tnyx.core.ui.components.inputs.TnyxTextField

@Composable
fun PersonalInfoScreen(
    state: PersonalInfoUiState,
    onAction: (PersonalInfoAction) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = TnyxTheme.dimens.SpaceM, vertical = TnyxTheme.dimens.SpaceS),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { onAction(PersonalInfoAction.BackClicked) }) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, null, tint = TnyxTheme.colors.textPrimary)
                }
                Text(
                    text = "Personal Information",
                    style = TnyxTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = TnyxTheme.colors.textPrimary,
                    modifier = Modifier.weight(1f)
                )
            }
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(TnyxTheme.dimens.SpaceM)
            ) {
                TnyxPrimaryButton(
                    text = if (state.isSaving) "Saving..." else "Save Changes",
                    onPressed = { onAction(PersonalInfoAction.SaveClicked) },
                    expand = true,
                    enabled = !state.isSaving && state.hasChanges
                )
            }
        },
        containerColor = TnyxTheme.colors.background
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = TnyxTheme.dimens.SpaceM),
            verticalArrangement = Arrangement.spacedBy(TnyxTheme.dimens.SpaceL)
        ) {
            // Avatar + change photo
            TnyxCard(variant = TnyxCardVariant.Normal, padding = TnyxTheme.dimens.SpaceM) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(TnyxTheme.colors.textPrimary.copy(alpha = 0.08f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Person,
                            contentDescription = null,
                            tint = TnyxTheme.colors.textPrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(TnyxTheme.dimens.SpaceM))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (state.name.isNotBlank()) state.name else "Your Name",
                            style = TnyxTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = TnyxTheme.colors.textPrimary
                        )
                        Text(
                            text = if (state.email.isNotBlank()) state.email else "you@example.com",
                            style = TnyxTheme.typography.labelSmall,
                            color = TnyxTheme.colors.textSecondary
                        )
                    }
                    TnyxSecondaryButton(
                        text = "Change Photo",
                        onPressed = { onAction(PersonalInfoAction.ChangePhotoClicked) },
                        variant = TnyxSecondaryVariant.Muted,
                        height = 32.dp
                    )
                }
            }

            // Basic Info
            TnyxCard(variant = TnyxCardVariant.Normal, padding = TnyxTheme.dimens.SpaceM) {
                Column(verticalArrangement = Arrangement.spacedBy(TnyxTheme.dimens.SpaceM)) {
                    TnyxTextField(
                        value = state.name,
                        onValueChange = { onAction(PersonalInfoAction.NameChanged(it)) },
                        label = { Text("Full Name") }
                    )
                    TnyxTextField(
                        value = state.email,
                        onValueChange = { onAction(PersonalInfoAction.EmailChanged(it)) },
                        label = { Text("Email") }
                    )
                }
            }

            // Details
            TnyxCard(variant = TnyxCardVariant.Normal, padding = TnyxTheme.dimens.SpaceM) {
                Column(verticalArrangement = Arrangement.spacedBy(TnyxTheme.dimens.SpaceM)) {
                    Text(
                        text = "Gender",
                        style = TnyxTheme.typography.labelMedium,
                        color = TnyxTheme.colors.textMuted
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        GenderChip(
                            label = "Male",
                            selected = state.gender.equals("Male", ignoreCase = true),
                            onClick = { onAction(PersonalInfoAction.GenderChanged("Male")) }
                        )
                        GenderChip(
                            label = "Female",
                            selected = state.gender.equals("Female", ignoreCase = true),
                            onClick = { onAction(PersonalInfoAction.GenderChanged("Female")) }
                        )
                        GenderChip(
                            label = "Other",
                            selected = state.gender.equals("Other", ignoreCase = true),
                            onClick = { onAction(PersonalInfoAction.GenderChanged("Other")) }
                        )
                    }

                    // DOB
                    TnyxTextField(
                        value = state.dob,
                        onValueChange = { onAction(PersonalInfoAction.DobChanged(it)) },
                        label = { Text("Date of Birth (YYYY-MM-DD)") },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Rounded.CalendarMonth,
                                contentDescription = null,
                                tint = TnyxTheme.colors.textSecondary
                            )
                        }
                    )

                    // Height & Weight
                    Row(horizontalArrangement = Arrangement.spacedBy(TnyxTheme.dimens.SpaceM)) {
                        Column(modifier = Modifier.weight(1f)) {
                            TnyxTextField(
                                value = state.heightCm,
                                onValueChange = { onAction(PersonalInfoAction.HeightChanged(it)) },
                                label = { Text("Height (cm)") },
                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            TnyxTextField(
                                value = state.weightKg,
                                onValueChange = { onAction(PersonalInfoAction.WeightChanged(it)) },
                                label = { Text("Weight (kg)") },
                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GenderChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val bg = if (selected) TnyxTheme.colors.textPrimary.copy(alpha = 0.12f) else TnyxTheme.colors.textPrimary.copy(alpha = 0.05f)
    val border = if (selected) TnyxTheme.colors.textPrimary.copy(alpha = 0.24f) else TnyxTheme.colors.textPrimary.copy(alpha = 0.12f)
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = Color.Transparent,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(bg)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            style = TnyxTheme.typography.labelMedium,
            color = TnyxTheme.colors.textPrimary
        )
    }
}
