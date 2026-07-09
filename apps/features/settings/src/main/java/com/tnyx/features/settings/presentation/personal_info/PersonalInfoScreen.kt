package com.tnyx.features.settings.presentation.personal_info

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.tnyx.core.theme.TnyxTheme
import com.tnyx.core.ui.components.buttons.TnyxPrimaryButton
import com.tnyx.core.ui.components.inputs.*
import com.tnyx.core.ui.components.navigation.TnyxTabItem
import com.tnyx.core.ui.components.navigation.TnyxTabSwitcher
import com.tnyx.features.settings.presentation.personal_info.components.DeleteAccountOverlays
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalInfoScreen(
    state: PersonalInfoUiState,
    onAction: (PersonalInfoAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    var isSexExpanded by remember { mutableStateOf(false) }
    val sexOptions = listOf("Male", "Female", "Other")

    Scaffold(
        topBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding(),
                color = TnyxTheme.colors.background,
                tonalElevation = 0.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .padding(horizontal = TnyxTheme.dimens.SpaceM),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    // 1. यहाँ से Surface और Border हटा दिया गया है
                    // सिर्फ IconButton का इस्तेमाल किया गया है
                    IconButton(
                        onClick = { onAction(PersonalInfoAction.OnBackClicked) }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TnyxTheme.colors.textPrimary,
                            modifier = Modifier.size(24.dp) // साइज़ 24.dp कर दिया है ताकि सही दिखे
                        )
                    }

                    Spacer(modifier = Modifier.width(TnyxTheme.dimens.SpaceM))

                    Text(
                        text = "Personal Information",
                        style = TnyxTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, fontSize = 20.sp),
                        color = TnyxTheme.colors.textPrimary,
                        modifier = Modifier.weight(1f)
                    )

                    // दाईं ओर वाला प्रोफाइल आइकन (इसे वैसे ही रखा है)
                    Surface(
                        modifier = Modifier.size(40.dp),
                        shape = CircleShape,
                        color = TnyxTheme.colors.surfaceVariant,
                        border = BorderStroke(0.5.dp, TnyxTheme.colors.textPrimary.copy(alpha = 0.1f))
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = TnyxTheme.colors.textPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(TnyxTheme.colors.background)
                    .navigationBarsPadding()
                    .padding(TnyxTheme.dimens.SpaceM),
                verticalArrangement = Arrangement.spacedBy(TnyxTheme.dimens.SpaceS)
            ) {
                TnyxPrimaryButton(
                    text = if (state.isSaving) "Saving..." else "Save Changes",
                    onPressed = { onAction(PersonalInfoAction.OnSaveClicked) },
                    enabled = !state.isSaving && state.hasChanges,
                    expand = true
                )

                Button(
                    onClick = { onAction(PersonalInfoAction.OnDeleteAccountClicked) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(TnyxTheme.components.button.height),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TnyxTheme.colors.error.copy(alpha = 0.1f),
                        contentColor = TnyxTheme.colors.error
                    ),
                    shape = TnyxTheme.shapes.Material.medium,
                    border = BorderStroke(1.dp, TnyxTheme.colors.error.copy(alpha = 0.2f))
                ) {
                    Text(
                        text = "Delete Account",
                        style = TnyxTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        },
        containerColor = TnyxTheme.colors.background
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(horizontal = TnyxTheme.dimens.SpaceM),
            verticalArrangement = Arrangement.spacedBy(TnyxTheme.dimens.SpaceXL)
        ) {
            Spacer(modifier = Modifier.height(TnyxTheme.dimens.SpaceS))

            PersonalInfoField(
                label = "FULL NAME",
                value = state.fullName,
                onValueChange = { onAction(PersonalInfoAction.OnFullNameChange(it)) },
                icon = Icons.Default.Person
            )

            ReadOnlyField(
                label = "EMAIL",
                value = state.email.ifBlank { "Not provided" },
                icon = Icons.Default.Email
            )

            Column {
                Text(
                    text = "MOBILE NUMBER",
                    style = TnyxTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = TnyxTheme.colors.textMuted
                )
                Spacer(modifier = Modifier.height(TnyxTheme.dimens.SpaceS))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    state.selectedCountry?.let { country ->
                        Box(
                            modifier = Modifier
                                .height(TnyxTheme.components.input.height)
                                .clickable { onAction(PersonalInfoAction.OnCountryPickerClicked) }
                                .padding(end = TnyxTheme.dimens.SpaceS),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = country.flag, fontSize = 22.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = country.code,
                                    style = TnyxTheme.typography.bodyLarge,
                                    color = TnyxTheme.colors.textPrimary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                    TnyxTextField(
                        value = state.phoneNumber,
                        onValueChange = { onAction(PersonalInfoAction.OnMobileChange(it)) },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Enter phone number") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        label = null
                    )
                }
            }

            ClickableField(
                label = "DATE OF BIRTH",
                value = state.dobMillis.toDisplayDate().ifBlank { "Select Date" },
                icon = Icons.Default.CalendarMonth,
                onClick = { onAction(PersonalInfoAction.OnDobClicked) }
            )

            Column {
                Text(
                    text = "BIOLOGICAL SEX",
                    style = TnyxTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = TnyxTheme.colors.textMuted
                )
                Spacer(modifier = Modifier.height(TnyxTheme.dimens.SpaceS))
                Box(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(TnyxTheme.shapes.Material.medium)
                            .background(TnyxTheme.colors.surfaceVariant) // Alpha हटाया
                            .clickable { isSexExpanded = true }
                            .padding(TnyxTheme.dimens.SpaceM),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Person, null, tint = TnyxTheme.colors.textMuted, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(TnyxTheme.dimens.SpaceM))
                        Text(
                            text = state.gender.ifBlank { "Select Gender" },
                            color = if (state.gender.isBlank()) TnyxTheme.colors.textMuted else TnyxTheme.colors.textPrimary,
                            style = TnyxTheme.typography.bodyLarge,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(Icons.Default.ArrowDropDown, null, tint = TnyxTheme.colors.textMuted)
                    }
                    DropdownMenu(
                        expanded = isSexExpanded,
                        onDismissRequest = { isSexExpanded = false },
                        modifier = Modifier.background(TnyxTheme.colors.surfaceRaised) // ड्रॉपडाउन के लिए surfaceRaised
                    ) {
                        sexOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option, color = TnyxTheme.colors.textPrimary) },
                                onClick = {
                                    onAction(PersonalInfoAction.OnGenderChange(option))
                                    isSexExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            HeightSection(
                heightUnit = state.heightUnit,
                heightCmText = state.heightCm,
                heightFeetText = state.heightFeet,
                heightInchesText = state.heightInches,
                onUnitToggle = { onAction(PersonalInfoAction.OnHeightUnitChange(it)) },
                onEditClick = { onAction(PersonalInfoAction.OnHeightEditClicked) }
            )

            Spacer(modifier = Modifier.height(TnyxTheme.dimens.SpaceXL))
        }
    }

    // --- Overlays ---
    DeleteAccountOverlays(state = state, onAction = onAction)

    if (state.showDobPicker) {
        val initialDate = remember(state.dobMillis) {
            if (state.dobMillis > 0) {
                Instant.ofEpochMilli(state.dobMillis)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
            } else {
                LocalDate.of(1995, 6, 5)
            }
        }

        TnyxDatePickerDialog(
            initialDate = initialDate,
            onDismiss = { onAction(PersonalInfoAction.OnDismissOverlays) },
            onConfirm = { selectedDate ->
                val millis = selectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                onAction(PersonalInfoAction.OnDobChange(millis))
                onAction(PersonalInfoAction.OnDismissOverlays)
            }
        )
    }

    if (state.showHeightPopup) {
        HeightEditPopup(
            heightUnit = state.heightUnit,
            heightCmText = state.heightCm,
            heightFeetText = state.heightFeet,
            heightInchesText = state.heightInches,
            isSaving = false,
            onHeightCmChange = { onAction(PersonalInfoAction.OnHeightCmChange(it)) },
            onHeightFeetChange = { onAction(PersonalInfoAction.OnHeightFeetChange(it)) },
            onHeightInchesChange = { onAction(PersonalInfoAction.OnHeightInchesChange(it)) },
            onDismiss = { onAction(PersonalInfoAction.OnDismissOverlays) },
            onSave = { onAction(PersonalInfoAction.OnDismissOverlays) }
        )
    }

    if (state.showCountryPicker) {
        TnyxCountryPickerSheet(
            onDismiss = { onAction(PersonalInfoAction.OnDismissOverlays) },
            onCountrySelected = { onAction(PersonalInfoAction.OnCountrySelected(it)) }
        )
    }
}

@Composable
private fun HeightSection(
    heightUnit: String,
    heightCmText: String,
    heightFeetText: String,
    heightInchesText: String,
    onUnitToggle: (String) -> Unit,
    onEditClick: () -> Unit
) {
    Column {
        Text(
            text = "HEIGHT",
            style = TnyxTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = TnyxTheme.colors.textMuted
        )
        Spacer(modifier = Modifier.height(TnyxTheme.dimens.SpaceS))
        val heightTabs = remember {
            listOf(
                TnyxTabItem("cm", Icons.Default.Straighten, "cm"),
                TnyxTabItem("ft", Icons.Default.Height, "ft")
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(TnyxTheme.dimens.SpaceM)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(TnyxTheme.shapes.Material.medium)
                    .background(TnyxTheme.colors.surfaceVariant) // Alpha हटाया
                    .clickable { onEditClick() }
                    .padding(TnyxTheme.dimens.SpaceM)
            ) {
                Text(
                    text = if (heightUnit == "cm") heightCmText.ifBlank { "---" } else formatFeetInches(heightFeetText, heightInchesText),
                    color = TnyxTheme.colors.textPrimary,
                    style = TnyxTheme.typography.bodyLarge
                )
            }

            TnyxTabSwitcher(
                tabs = heightTabs,
                selectedValue = heightUnit,
                onTabSelected = onUnitToggle,
                modifier = Modifier.width(140.dp)
            )
        }
    }
}

// 🛠️ यहाँ मुख्य सुधार किया गया है (कीबोर्ड और नेविगेशन बार के लिए)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HeightEditPopup(
    heightUnit: String,
    heightCmText: String,
    heightFeetText: String,
    heightInchesText: String,
    isSaving: Boolean,
    onHeightCmChange: (String) -> Unit,
    onHeightFeetChange: (String) -> Unit,
    onHeightInchesChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    // यह बॉटम शीट की स्टेट को मैनेज करेगा और इसे आधी खुली रहने से रोकेगा
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = TnyxTheme.colors.surfaceRaised, // बैकग्राउंड कलर
        shape = TnyxTheme.shapes.Material.large,
        // BottomSheet डिफ़ॉल्ट रूप से सिस्टम बार्स को काफी अच्छे से हैंडल करता है
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = TnyxTheme.dimens.SpaceM)
                .padding(bottom = TnyxTheme.dimens.SpaceXL)
                .navigationBarsPadding() // नेविगेशन बटन से बचाएगा
                .imePadding() // कीबोर्ड के साथ एकदम परफेक्ट तरीके से ऊपर जाएगा
        ) {
            Text(
                text = "Edit Height",
                style = TnyxTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TnyxTheme.colors.textPrimary
            )

            Spacer(modifier = Modifier.height(TnyxTheme.dimens.SpaceXL))

            if (heightUnit == "cm") {
                TnyxTextField(
                    value = heightCmText,
                    onValueChange = onHeightCmChange,
                    label = null,
                    trailingIcon = { Text("cm", style = TnyxTheme.typography.bodyMedium, color = TnyxTheme.colors.textSecondary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(TnyxTheme.dimens.SpaceM)
                ) {
                    TnyxTextField(
                        value = heightFeetText,
                        onValueChange = onHeightFeetChange,
                        label = null,
                        trailingIcon = { Text("ft", style = TnyxTheme.typography.bodyMedium, color = TnyxTheme.colors.textSecondary) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    TnyxTextField(
                        value = heightInchesText,
                        onValueChange = onHeightInchesChange,
                        label = null,
                        trailingIcon = { Text("in", style = TnyxTheme.typography.bodyMedium, color = TnyxTheme.colors.textSecondary) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(TnyxTheme.dimens.SpaceXL))

            TnyxPrimaryButton(text = "Confirm", onPressed = onSave, expand = true)
        }
    }
}
@Composable
private fun PersonalInfoField(label: String, value: String, onValueChange: (String) -> Unit, icon: ImageVector) {
    Column {
        Text(label, style = TnyxTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = TnyxTheme.colors.textMuted)
        Spacer(modifier = Modifier.height(TnyxTheme.dimens.SpaceS))
        TnyxTextField(value, onValueChange, leadingIcon = { Icon(icon, null, modifier = Modifier.size(20.dp)) }, modifier = Modifier.fillMaxWidth(), label = null)
    }
}

@Composable
private fun ReadOnlyField(label: String, value: String, icon: ImageVector) {
    Column {
        Text(label, style = TnyxTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = TnyxTheme.colors.textMuted)
        Spacer(modifier = Modifier.height(TnyxTheme.dimens.SpaceS))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(TnyxTheme.shapes.Material.medium)
                .background(TnyxTheme.colors.surfaceVariant) // Alpha हटाया
                .padding(TnyxTheme.dimens.SpaceM),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = TnyxTheme.colors.textMuted, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(TnyxTheme.dimens.SpaceM))
            Text(value, style = TnyxTheme.typography.bodyLarge, color = TnyxTheme.colors.textMuted)
        }
    }
}

@Composable
private fun ClickableField(label: String, value: String, icon: ImageVector, onClick: () -> Unit) {
    Column {
        Text(label, style = TnyxTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = TnyxTheme.colors.textMuted)
        Spacer(modifier = Modifier.height(TnyxTheme.dimens.SpaceS))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(TnyxTheme.shapes.Material.medium)
                .background(TnyxTheme.colors.surfaceVariant) // Alpha हटाया
                .clickable(onClick = onClick)
                .padding(TnyxTheme.dimens.SpaceM),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = TnyxTheme.colors.textMuted, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(TnyxTheme.dimens.SpaceM))
            Text(value, style = TnyxTheme.typography.bodyLarge, color = TnyxTheme.colors.textPrimary)
        }
    }
}