package com.freight24.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.freight24.rest.response.CityResult
import kotlin.collections.isNotEmpty

@Composable
fun CitySearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    suggestions: List<CityResult>,
    onCitySelected: (CityResult) -> Unit,
    isLoading: Boolean,
    label: String,
    placeholder: String = "Search city...",
    enabled: Boolean = true,
    selectAllOnFocus: Boolean = false,
    modifier: Modifier = Modifier,
) {
    var fieldValue by remember() { mutableStateOf(TextFieldValue(query)) }
    var isFocused by remember { mutableStateOf(false) }
    // ✅ Only syncs when query changes EXTERNALLY (e.g. city selected, field cleared)
    // During typing: fieldValue.text == query, so this block never fires
    LaunchedEffect(query) {
        if (fieldValue.text != query) {
            fieldValue = TextFieldValue(
                text = query,
                selection = if (selectAllOnFocus && isFocused && query.isNotEmpty()) {
                    TextRange(0, query.length)
                } else {
                    TextRange(query.length) // cursor at end
                }
            )
        }
    }

    Column(modifier = modifier) {
        OutlinedTextField(
            value = fieldValue,
            onValueChange = {
                fieldValue = it
                onQueryChange(it.text)
            },
            label = { Text(label) },
            placeholder = { Text(placeholder, style = MaterialTheme.typography.bodyMedium) },
            enabled = enabled,
            singleLine = true,
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                imeAction = ImeAction.Search,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { state ->
                    if (selectAllOnFocus && state.isFocused && fieldValue.text.isNotEmpty()) {
                        fieldValue = fieldValue.copy(selection = TextRange(0, fieldValue.text.length))
                    }
                },
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                disabledBorderColor = MaterialTheme.colorScheme.outlineVariant,
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
        )

        if (isLoading) {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
        }

        if (suggestions.isNotEmpty() && enabled) {
            Spacer(Modifier.height(2.dp))
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 200.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surface),
            ) {
                items(suggestions, key = { it.id }) { city ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onCitySelected(city) }
                            .padding(horizontal = 14.dp, vertical = 11.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Place,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(Modifier.width(10.dp))

                        val (line1, line2) = if(city.type.equals("area", true)) {
                            Pair(city.name, city.city)
                        } else {
                            Pair(city.name, city.state)
                        }

                        Column {
                            Text(
                                line1,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                line2,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    if (city != suggestions.last()) {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant,
                            thickness = 0.5.dp,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RouteChip(
    fromCity: String,
    toCity: String,
    isStaged: Boolean = false,
    onDelete: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
        modifier = modifier.border(
            width = if (isStaged) 1.dp else 0.5.dp,
            color = MaterialTheme.colorScheme.primary.copy(alpha = if (isStaged) 0.6f else 0.4f),
            shape = RoundedCornerShape(20.dp),
        ),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(
                start = 10.dp,
                end = if (onDelete != null) 4.dp else 10.dp,
                top = 6.dp,
                bottom = 6.dp,
            ),
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(3.dp),
                    ),
            )
            Spacer(Modifier.width(5.dp))
            Text(
                fromCity,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.width(4.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "to",
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                modifier = Modifier.size(12.dp),
            )
            Spacer(Modifier.width(4.dp))
            Text(
                toCity,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            if (onDelete != null) {
                Spacer(Modifier.width(4.dp))
                IconButton(onClick = onDelete, modifier = Modifier.size(20.dp)) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Remove route",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(12.dp),
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRouteBottomSheet(
    fromQuery: String,
    onFromQueryChange: (String) -> Unit,
    fromSuggestions: List<CityResult>,
    onFromCitySelected: (CityResult) -> Unit,
    isFromLoading: Boolean,
    selectedFromCity: CityResult?,
    isFromPreFilled: Boolean,
    toQuery: String,
    onToQueryChange: (String) -> Unit,
    toSuggestions: List<CityResult>,
    onToCitySelected: (CityResult) -> Unit,
    isToLoading: Boolean,
    selectedToCity: CityResult?,
    selectedPriority: String,
    onPrioritySelected: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    errorMessage: String? = null,
) {
    val canConfirm = selectedFromCity != null && selectedToCity != null &&
        selectedFromCity.id != selectedToCity.id

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp),
        ) {
            Text(
                "Add preferred route",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                "Cities you regularly carry loads between",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp, bottom = 20.dp),
            )

            CitySearchField(
                query = fromQuery,
                onQueryChange = onFromQueryChange,
                suggestions = fromSuggestions,
                onCitySelected = onFromCitySelected,
                isLoading = isFromLoading,
                label = "From city",
                modifier = Modifier.fillMaxWidth(),
            )

            if (isFromPreFilled && selectedFromCity != null) {
                Text(
                    "Pre-filled from base city",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 4.dp, start = 4.dp, bottom = 2.dp),
                )
            }

            Spacer(Modifier.height(14.dp))

            CitySearchField(
                query = toQuery,
                onQueryChange = onToQueryChange,
                suggestions = toSuggestions,
                onCitySelected = onToCitySelected,
                isLoading = isToLoading,
                label = "To city",
                placeholder = "Search destination...",
                modifier = Modifier.fillMaxWidth(),
            )

            if (selectedFromCity != null || selectedToCity != null) {
                Spacer(Modifier.height(20.dp))
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            selectedFromCity?.city ?: "...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp),
                        )
                        val toLabel = selectedToCity?.city ?: "..."
                        Text(
                            toLabel,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (toLabel == "...") {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            } else {
                                MaterialTheme.colorScheme.onSecondaryContainer
                            },
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
            Text(
                "PRIORITY",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("High", "Medium", "Low").forEach { option ->
                    FilterChip(
                        selected = option == selectedPriority,
                        onClick = { onPrioritySelected(option) },
                        label = { Text(option, style = MaterialTheme.typography.labelMedium) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        ),
                    )
                }
            }

            if (errorMessage != null) {
                Spacer(Modifier.height(10.dp))
                Text(
                    errorMessage,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }

            Spacer(Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                    Text("Cancel")
                }
                Button(onClick = onConfirm, enabled = canConfirm, modifier = Modifier.weight(2f)) {
                    Text("Add route")
                }
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}
