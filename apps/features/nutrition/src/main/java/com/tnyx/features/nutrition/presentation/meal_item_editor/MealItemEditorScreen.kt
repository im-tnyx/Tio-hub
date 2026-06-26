package com.tnyx.features.nutrition.presentation.meal_item_editor

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tnyx.core.theme.TnyxTheme
import com.tnyx.core.ui.components.buttons.TnyxPrimaryButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealItemEditorScreen(
    state: MealItemEditorUiState,
    onAction: (MealItemEditorAction) -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Edit Item", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { onAction(MealItemEditorAction.BackClicked) }) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = TnyxTheme.colors.background
                )
            )
        },
        bottomBar = {
            MealItemEditorBottomBar(
                onRemove = { onAction(MealItemEditorAction.RemoveClicked) },
                onSave = { onAction(MealItemEditorAction.SaveClicked) }
            )
        },
        containerColor = TnyxTheme.colors.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = TnyxTheme.colors.surfaceRaised,
                border = BorderStroke(0.5.dp, TnyxTheme.colors.textPrimary.copy(alpha = 0.1f))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    // Item Name
                    OutlinedTextField(
                        value = state.item.name,
                        onValueChange = { onAction(MealItemEditorAction.NameChanged(it)) },
                        label = { Text("Item Name") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = TnyxTheme.colors.textPrimary.copy(alpha = 0.1f)
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    HorizontalDivider(thickness = 0.5.dp, color = TnyxTheme.colors.textPrimary.copy(alpha = 0.1f))
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Inputs
                    NutrientInputRow(
                        label = "Quantity",
                        value = state.item.quantity.toString(),
                        unit = state.item.unit,
                        onValueChanged = { onAction(MealItemEditorAction.QuantityChanged(it.toDoubleOrNull() ?: 1.0)) }
                    )
                    NutrientInputRow(
                        label = "Calories",
                        value = state.item.calories.toString(),
                        unit = "kcal",
                        onValueChanged = { onAction(MealItemEditorAction.NutrientChanged("calories", it.toDoubleOrNull() ?: 0.0)) }
                    )
                    NutrientInputRow(
                        label = "Protein",
                        value = state.item.protein.toString(),
                        unit = "g",
                        onValueChanged = { onAction(MealItemEditorAction.NutrientChanged("protein", it.toDoubleOrNull() ?: 0.0)) }
                    )
                    NutrientInputRow(
                        label = "Carbs",
                        value = state.item.carbs.toString(),
                        unit = "g",
                        onValueChanged = { onAction(MealItemEditorAction.NutrientChanged("carbs", it.toDoubleOrNull() ?: 0.0)) }
                    )
                    NutrientInputRow(
                        label = "Fats",
                        value = state.item.fats.toString(),
                        unit = "g",
                        onValueChanged = { onAction(MealItemEditorAction.NutrientChanged("fats", it.toDoubleOrNull() ?: 0.0)) }
                    )
                }
            }
        }
    }
}

@Composable
private fun NutrientInputRow(
    label: String,
    value: String,
    unit: String,
    onValueChanged: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            modifier = Modifier.width(100.dp),
            style = TnyxTheme.typography.bodyMedium,
            color = TnyxTheme.colors.textSecondary
        )
        
        OutlinedTextField(
            value = value,
            onValueChange = onValueChanged,
            modifier = Modifier.weight(1f).height(52.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            textStyle = TnyxTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = TnyxTheme.colors.background.copy(alpha = 0.5f),
                focusedContainerColor = TnyxTheme.colors.background.copy(alpha = 0.5f),
                unfocusedBorderColor = Color.Transparent,
                focusedBorderColor = TnyxTheme.colors.primary.copy(alpha = 0.5f)
            )
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Box(
            modifier = Modifier
                .width(100.dp)
                .height(52.dp)
                .background(TnyxTheme.colors.background.copy(alpha = 0.5f), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = unit,
                style = TnyxTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun MealItemEditorBottomBar(
    onRemove: () -> Unit,
    onSave: () -> Unit
) {
    Surface(
        color = TnyxTheme.colors.background,
        border = BorderStroke(0.5.dp, TnyxTheme.colors.textPrimary.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            OutlinedButton(
                onClick = onRemove,
                modifier = Modifier.weight(1f).height(56.dp),
                shape = RoundedCornerShape(28.dp),
                border = BorderStroke(1.dp, TnyxTheme.colors.textPrimary)
            ) {
                Text("Remove Item", color = TnyxTheme.colors.textPrimary, fontWeight = FontWeight.Bold)
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            TnyxPrimaryButton(
                text = "Save",
                onPressed = onSave,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
