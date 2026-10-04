package com.freight24.connect.feature.trucks.views
import com.freight24.ui.components.FreightTopAppBar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.freight24.connect.feature.trucks.domain.viewmodel.AssignTruckEvent
import com.freight24.connect.feature.trucks.domain.viewmodel.AssignTruckViewModel
import com.freight24.connect.feature.trucks.enums.TruckStatus
import com.freight24.ui.theme.FreightThemeTokens
import com.freight24.rest.response.SearchTruckResponse

// Indian vehicle registration number (e.g. KA01AB1234): 2 state letters, 2 district
// digits, up to 3 series letters, 1-4 number digits. The optional groups also accept
// partial queries like "KA", "KA01" or "KA01AB" while the user is still typing.
private val VEHICLE_NO_REGEX = Regex("^(?:[A-Z]{2}\\d{2}[A-Z]{0,2}\\d{1,4}|\\d{2}BH\\d{4}[A-Z]{1,2})$")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssignTruckScreen(
    onBackClick: () -> Unit,
    onTruckSelected: (String, String) -> Unit, // Callback with Truck ID and Registration Number
    viewModel: AssignTruckViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val colorScheme = MaterialTheme.colorScheme
    val extendedColors = FreightThemeTokens.colors

    Scaffold(
        topBar = {
            FreightTopAppBar(
                title = { Text("Assign Truck", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
        containerColor = colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Search Bar
            OutlinedTextField(
                value = uiState.query,
                onValueChange = { query ->
                    val filtered = query.uppercase().filter { it.isLetterOrDigit() }.take(11)
                    if (VEHICLE_NO_REGEX.matches(filtered)) {
                        viewModel.onEvent(AssignTruckEvent.UpdateQuery(filtered))
                    }
                },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colorScheme.surface, MaterialTheme.shapes.medium)
                    .testTag("assign_truck_search_input")
                    .semantics { testTagsAsResourceId = true },
                placeholder = { 
                    Text(
                        "Enter vehicle no. (e.g. KA01AB1234)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colorScheme.onSurfaceVariant
                    ) 
                },
                leadingIcon = { 
                    Icon(
                        Icons.Default.Search, 
                        contentDescription = "Search", 
                        tint = colorScheme.onSurfaceVariant
                    ) 
                },
                shape = MaterialTheme.shapes.medium,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = colorScheme.surface,
                    unfocusedContainerColor = colorScheme.surface,
                    focusedBorderColor = colorScheme.outlineVariant,
                    unfocusedBorderColor = colorScheme.outlineVariant,
                    disabledBorderColor = colorScheme.surfaceVariant
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Search Results Header
            if (uiState.trucks.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "SEARCH RESULTS",
                        style = MaterialTheme.typography.labelMedium,
                        color = colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = colorScheme.surfaceVariant
                    ) {
                        Text(
                            "${uiState.trucks.size} ${if (uiState.trucks.size == 1) "TRUCK" else "TRUCKS"} FOUND",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = FontWeight.Bold,
                            color = colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Results List
            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (uiState.errorMessage != null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(uiState.errorMessage!!, color = colorScheme.error)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(uiState.trucks) { truck ->
                        TruckItem(truck = truck, onSelect = { onTruckSelected(truck.truckId, truck.registrationNumber) })
                    }
                }
            }
        }
    }
}

@Composable
fun TruckItem(
    truck: SearchTruckResponse.Truck,
    onSelect: () -> Unit
) {
    val isEnabled = !truck.isBlacklisted &&
        truck.availability.lowercase() != "on_trip" &&
        truck.status.lowercase() != "inactive"
    val statusText = TruckStatus.from(truck).displayText
    val colorScheme = MaterialTheme.colorScheme
    val extendedColors = FreightThemeTokens.colors
    val statusColor = if (isEnabled) extendedColors.success else colorScheme.error
    val buttonColor = if (isEnabled) extendedColors.success else colorScheme.surfaceVariant
    val buttonContentColor = if (isEnabled) colorScheme.onPrimary else colorScheme.onSurfaceVariant

    Card(
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Truck Icon Box
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = colorScheme.surfaceVariant,
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.LocalShipping,
                        contentDescription = "Truck",
                        tint = colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Truck Details
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = truck.registrationNumber,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = truck.owner.tagCode,
                        style = MaterialTheme.typography.bodySmall,
                        color = colorScheme.onSurfaceVariant
                    )
                }
                
                Spacer(modifier = Modifier.height(2.dp))
                
                Text(
                    text = truck.displayName,
                    style = MaterialTheme.typography.bodySmall,
                    color = colorScheme.onSurfaceVariant
                )
                
                Text(
                    text = "Owner: ${truck.owner.name}",
                    style = MaterialTheme.typography.bodySmall,
                    color = colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Status
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(statusColor, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.bodySmall,
                        color = statusColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Select Button
            Button(
                onClick = onSelect,
                colors = ButtonDefaults.buttonColors(
                    containerColor = buttonColor,
                    contentColor = buttonContentColor,
                    disabledContainerColor = buttonColor,
                    disabledContentColor = buttonContentColor
                ),
                enabled = isEnabled,
                shape = MaterialTheme.shapes.small,
                contentPadding = PaddingValues(horizontal = 16.dp),
                modifier = Modifier.height(36.dp)
            ) {
                Text(
                    text = "Select",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// Extension to capitalize first letter
fun String.capitalize(): String {
    return this.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
}
