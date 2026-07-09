package com.tnyx.core.ui.components.inputs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tnyx.core.theme.TnyxTheme

data class Country(val name: String, val code: String, val flag: String)

val defaultCountries: List<Country> = listOf(
    Country("India", "+91", "🇮🇳"),
    Country("United States", "+1", "🇺🇸"),
    Country("United Kingdom", "+44", "🇬🇧"),
    Country("Canada", "+1", "🇨🇦"),
    Country("Australia", "+61", "🇦🇺")
)

fun countryForMobile(mobile: String): Country {
    val compact = mobile.trim().replace(" ", "")
    return defaultCountries
        .sortedByDescending { it.code.length }
        .firstOrNull { country ->
            compact.startsWith(country.code) ||
                    compact.startsWith(country.code.removePrefix("+"))
        }
        ?: defaultCountries.first()
}

fun nationalMobileNumber(mobile: String, country: Country): String {
    val compact = mobile.trim().replace(" ", "")
    val withoutCode = when {
        compact.startsWith(country.code) -> compact.removePrefix(country.code)
        compact.startsWith(country.code.removePrefix("+")) -> compact.removePrefix(country.code.removePrefix("+"))
        else -> compact
    }
    return withoutCode.filter(Char::isDigit).take(15)
}

@Composable
fun TnyxCountryPicker(
    country: Country,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showDivider: Boolean = false
) {
    Row(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(start = TnyxTheme.dimens.SpaceS, end = if (showDivider) 0.dp else TnyxTheme.dimens.SpaceXS),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = country.flag,
            fontSize = 18.sp
        )
        Spacer(modifier = Modifier.width(TnyxTheme.dimens.SpaceXS))
        Text(
            text = country.code,
            color = TnyxTheme.colors.textPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )
        Icon(
            imageVector = Icons.Default.ArrowDropDown,
            contentDescription = "Select country",
            tint = TnyxTheme.colors.textMuted
        )

        if (showDivider) {
            Box(
                modifier = Modifier
                    .padding(horizontal = TnyxTheme.dimens.SpaceS)
                    .width(TnyxTheme.dimens.BorderThin)
                    .height(24.dp)
                    .background(TnyxTheme.colors.textMuted.copy(alpha = 0.3f))
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TnyxCountryPickerSheet(
    onDismiss: () -> Unit,
    onCountrySelected: (Country) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredCountries = defaultCountries.filter {
        it.name.contains(searchQuery, ignoreCase = true) || it.code.contains(searchQuery)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = TnyxTheme.colors.surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = { BottomSheetDefaults.DragHandle(color = TnyxTheme.colors.textMuted.copy(alpha = 0.4f)) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = TnyxTheme.dimens.SpaceM)
                .padding(bottom = TnyxTheme.dimens.SpaceXL)
        ) {
            Text(
                text = "Select Country",
                style = TnyxTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TnyxTheme.colors.textPrimary,
                modifier = Modifier.padding(bottom = TnyxTheme.dimens.SpaceM)
            )

            TnyxTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search country or code...") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Search, null, tint = TnyxTheme.colors.textMuted) }
            )

            Spacer(modifier = Modifier.height(TnyxTheme.dimens.SpaceM))

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(filteredCountries) { country ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(TnyxTheme.shapes.Material.medium)
                            .clickable {
                                onCountrySelected(country)
                                onDismiss()
                            }
                            .padding(
                                horizontal = TnyxTheme.dimens.SpaceM,
                                vertical = TnyxTheme.dimens.SpaceS
                            ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = country.flag, fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(TnyxTheme.dimens.SpaceL))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = country.name,
                                style = TnyxTheme.typography.bodyLarge,
                                color = TnyxTheme.colors.textPrimary,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = country.code,
                                style = TnyxTheme.typography.bodySmall,
                                color = TnyxTheme.colors.textSecondary
                            )
                        }
                        Icon(
                            Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            null,
                            tint = TnyxTheme.colors.textMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
