package com.tnyx.features.nutrition.presentation.meal_item_editor.widgets

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import com.tnyx.core.theme.TnyxTheme
import com.tnyx.core.ui.components.cards.TnyxCard
import com.tnyx.core.ui.components.cards.TnyxCardVariant
import com.tnyx.core.ui.components.inputs.TnyxTextField
import com.tnyx.core.ui.components.inputs.TnyxTextFieldVariant
import com.tnyx.features.nutrition.domain.models.MealItem
import java.util.Locale

@Composable
fun IngredientQuantityEditor(
    item: MealItem,
    onQuantityChanged: (Double) -> Unit,
    onUnitChanged: (String) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(TnyxTheme.dimens.SpaceS),
    ) {
        Text(
            text = "Quantity",
            modifier = Modifier.weight(0.8f),
            style = TnyxTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
            color = TnyxTheme.colors.textSecondary,
        )
        EditableNumberField(
            value = item.quantity,
            onValueChanged = onQuantityChanged,
            modifier = Modifier.weight(0.85f),
        )
        IngredientUnitDropdown(
            itemName = item.name,
            selectedUnit = item.unit,
            onUnitChanged = onUnitChanged,
            modifier = Modifier.weight(1.45f),
        )
    }
}

@Composable
fun MacroNutrientEditorCard(
    item: MealItem,
    onNutrientChanged: (String, Double) -> Unit,
) {
    TnyxCard(
        modifier = Modifier.fillMaxWidth(),
        variant = TnyxCardVariant.Normal,
        padding = TnyxTheme.dimens.SpaceM,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(TnyxTheme.dimens.SpaceSM),
        ) {
            NutrientEditorRow("Calories", item.calories.toDouble(), "kcal") {
                onNutrientChanged("calories", it)
            }
            NutrientEditorRow("Protein", item.protein, "g") {
                onNutrientChanged("protein", it)
            }
            NutrientEditorRow("Carbs", item.carbs, "g") {
                onNutrientChanged("carbs", it)
            }
            NutrientEditorRow("Fats", item.fats, "g") {
                onNutrientChanged("fats", it)
            }
            NutrientEditorRow("Fiber", item.fiber, "g") {
                onNutrientChanged("fiber", it)
            }
            NutrientEditorRow("Sugar", item.sugar, "g") {
                onNutrientChanged("sugar", it)
            }
        }
    }
}

@Composable
fun MicronutrientEditorCard(
    item: MealItem,
    expanded: Boolean,
    onToggle: () -> Unit,
    onNutrientChanged: (String, Double) -> Unit,
    onMicronutrientChanged: (String, Double?) -> Unit,
) {
    TnyxCard(
        modifier = Modifier.fillMaxWidth(),
        variant = TnyxCardVariant.Normal,
        padding = TnyxTheme.dimens.SpaceM,
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggle),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "Micronutrients",
                    style = TnyxTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = TnyxTheme.colors.textPrimary,
                )
                Icon(
                    imageVector = if (expanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                    contentDescription = if (expanded) "Collapse micronutrients" else "Expand micronutrients",
                    tint = TnyxTheme.colors.textPrimary,
                )
            }
            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = TnyxTheme.dimens.SpaceM),
                    verticalArrangement = Arrangement.spacedBy(TnyxTheme.dimens.SpaceSM),
                ) {
                    NutrientEditorRow("Trans fat", item.transFat, "g") {
                        onNutrientChanged("transFat", it)
                    }
                    NutrientEditorRow("Saturated fat", item.saturatedFat, "g") {
                        onNutrientChanged("saturatedFat", it)
                    }
                    MicronutrientSectionLabel("Vitamins")
                    vitaminEditorValues(item).forEach { nutrient ->
                        NullableNutrientEditorRow(
                            label = nutrient.label,
                            value = nutrient.value,
                            unit = nutrient.unit,
                            onValueChanged = { value ->
                                onMicronutrientChanged(nutrient.field, value)
                            },
                        )
                    }
                    MicronutrientSectionLabel("Minerals")
                    mineralEditorValues(item).forEach { nutrient ->
                        NullableNutrientEditorRow(
                            label = nutrient.label,
                            value = nutrient.value,
                            unit = nutrient.unit,
                            onValueChanged = { value ->
                                onMicronutrientChanged(nutrient.field, value)
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NutrientEditorRow(
    label: String,
    value: Double,
    unit: String,
    onValueChanged: (Double) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(TnyxTheme.dimens.SpaceS),
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(1.1f),
            style = TnyxTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = TnyxTheme.colors.textSecondary,
            maxLines = 1,
        )
        EditableNumberField(
            value = value,
            onValueChanged = onValueChanged,
            modifier = Modifier.weight(0.9f),
        )
        TnyxCard(
            modifier = Modifier.weight(1.5f),
            variant = TnyxCardVariant.Surface,
            padding = TnyxTheme.dimens.SpaceSM,
        ) {
            Text(
                text = unit,
                style = TnyxTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                color = TnyxTheme.colors.textPrimary,
            )
        }
    }
}

@Composable
private fun EditableNumberField(
    value: Double,
    onValueChanged: (Double) -> Unit,
    modifier: Modifier = Modifier,
) {
    var input by remember(value) { mutableStateOf(value.toEditableNumber()) }

    TnyxTextField(
        value = input,
        onValueChange = { nextValue ->
            val normalized = nextValue.filter { it.isDigit() || it == '.' }
            input = normalized
            normalized.toDoubleOrNull()?.let(onValueChanged)
        },
        modifier = modifier,
        variant = TnyxTextFieldVariant.Compact,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
    )
}

@Composable
private fun MicronutrientSectionLabel(label: String) {
    Text(
        text = label,
        modifier = Modifier.padding(top = TnyxTheme.dimens.SpaceS),
        style = TnyxTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
        color = TnyxTheme.colors.textPrimary,
    )
}

@Composable
private fun NullableNutrientEditorRow(
    label: String,
    value: Double?,
    unit: String,
    onValueChanged: (Double?) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(TnyxTheme.dimens.SpaceS),
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(1.1f),
            style = TnyxTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = TnyxTheme.colors.textSecondary,
            maxLines = 1,
        )
        EditableNullableNumberField(
            value = value,
            onValueChanged = onValueChanged,
            modifier = Modifier.weight(0.9f),
        )
        TnyxCard(
            modifier = Modifier.weight(1.5f),
            variant = TnyxCardVariant.Surface,
            padding = TnyxTheme.dimens.SpaceSM,
        ) {
            Text(
                text = unit,
                style = TnyxTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                color = TnyxTheme.colors.textPrimary,
            )
        }
    }
}

@Composable
private fun EditableNullableNumberField(
    value: Double?,
    onValueChanged: (Double?) -> Unit,
    modifier: Modifier = Modifier,
) {
    var input by remember(value) {
        mutableStateOf(value?.toEditableNumber().orEmpty())
    }

    TnyxTextField(
        value = input,
        onValueChange = { nextValue ->
            val normalized = nextValue.filter { it.isDigit() || it == '.' }
            input = normalized
            onValueChanged(normalized.toDoubleOrNull())
        },
        modifier = modifier,
        variant = TnyxTextFieldVariant.Compact,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
    )
}

private data class MicronutrientEditorValue(
    val label: String,
    val field: String,
    val value: Double?,
    val unit: String,
)

private fun vitaminEditorValues(item: MealItem): List<MicronutrientEditorValue> = listOf(
    MicronutrientEditorValue("Vitamin A", "vitaminAMcgRae", item.micronutrients.vitaminAMcgRae, "mcg RAE"),
    MicronutrientEditorValue("Vitamin C", "vitaminCMg", item.micronutrients.vitaminCMg, "mg"),
    MicronutrientEditorValue("Vitamin D", "vitaminDMcg", item.micronutrients.vitaminDMcg, "mcg"),
    MicronutrientEditorValue("Vitamin E", "vitaminEMg", item.micronutrients.vitaminEMg, "mg"),
    MicronutrientEditorValue("Vitamin K", "vitaminKMcg", item.micronutrients.vitaminKMcg, "mcg"),
    MicronutrientEditorValue("Thiamin", "thiaminMg", item.micronutrients.thiaminMg, "mg"),
    MicronutrientEditorValue("Riboflavin", "riboflavinMg", item.micronutrients.riboflavinMg, "mg"),
    MicronutrientEditorValue("Niacin", "niacinMg", item.micronutrients.niacinMg, "mg"),
    MicronutrientEditorValue("Vitamin B6", "vitaminB6Mg", item.micronutrients.vitaminB6Mg, "mg"),
    MicronutrientEditorValue("Vitamin B12", "vitaminB12Mcg", item.micronutrients.vitaminB12Mcg, "mcg"),
    MicronutrientEditorValue("Folate", "folateMcg", item.micronutrients.folateMcg, "mcg"),
)

private fun mineralEditorValues(item: MealItem): List<MicronutrientEditorValue> = listOf(
    MicronutrientEditorValue("Calcium", "calciumMg", item.micronutrients.calciumMg, "mg"),
    MicronutrientEditorValue("Iron", "ironMg", item.micronutrients.ironMg, "mg"),
    MicronutrientEditorValue("Magnesium", "magnesiumMg", item.micronutrients.magnesiumMg, "mg"),
    MicronutrientEditorValue("Potassium", "potassiumMg", item.micronutrients.potassiumMg, "mg"),
    MicronutrientEditorValue("Zinc", "zincMg", item.micronutrients.zincMg, "mg"),
    MicronutrientEditorValue("Selenium", "seleniumMcg", item.micronutrients.seleniumMcg, "mcg"),
    MicronutrientEditorValue("Phosphorus", "phosphorusMg", item.micronutrients.phosphorusMg, "mg"),
    MicronutrientEditorValue("Copper", "copperMg", item.micronutrients.copperMg, "mg"),
    MicronutrientEditorValue("Manganese", "manganeseMg", item.micronutrients.manganeseMg, "mg"),
    MicronutrientEditorValue("Iodine", "iodineMcg", item.micronutrients.iodineMcg, "mcg"),
)

@Composable
private fun IngredientUnitDropdown(
    itemName: String,
    selectedUnit: String,
    onUnitChanged: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    val options = remember(itemName, selectedUnit) {
        ingredientUnitOptions(itemName = itemName, currentUnit = selectedUnit)
    }

    BoxWithConstraints(modifier = modifier) {
        val menuWidth = maxWidth
        TnyxCard(
            modifier = Modifier.fillMaxWidth(),
            variant = TnyxCardVariant.Surface,
            padding = TnyxTheme.dimens.SpaceSM,
            onClick = { expanded = true },
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = selectedUnit,
                    style = TnyxTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = TnyxTheme.colors.textPrimary,
                    maxLines = 1,
                )
                Icon(
                    imageVector = Icons.Rounded.ExpandMore,
                    contentDescription = "Select quantity unit",
                    tint = TnyxTheme.colors.textSecondary,
                )
            }
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.width(menuWidth),
        ) {
            options.forEach { unit ->
                DropdownMenuItem(
                    text = { Text(unit) },
                    onClick = {
                        onUnitChanged(unit)
                        expanded = false
                    },
                )
            }
        }
    }
}

private fun ingredientUnitOptions(itemName: String, currentUnit: String): List<String> {
    val context = "$itemName $currentUnit".lowercase(Locale.US)
    val defaults = when {
        LIQUID_MARKERS.any(context::contains) -> LIQUID_UNITS
        else -> SOLID_UNITS
    }
    return buildList {
        currentUnit.trim().takeIf(String::isNotBlank)?.let(::add)
        defaults.forEach { unit -> if (unit !in this) add(unit) }
    }
}

private fun Double.toEditableNumber(): String {
    return if (this % 1.0 == 0.0) String.format(Locale.US, "%.1f", this) else toString()
}

private val LIQUID_MARKERS = listOf("ml", "litre", "liter", "milk", "water", "juice", "tea", "coffee", "drink", "soup")
private val LIQUID_UNITS = listOf("ml", "L", "tsp", "tbsp", "cup", "glass")
private val SOLID_UNITS = listOf("g", "kg", "piece", "serving", "slice", "cup", "bowl", "plate", "oz")
