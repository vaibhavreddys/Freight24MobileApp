package com.freight24.connect.feature.adhocTruck.views

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.freight24.connect.feature.adhocTruck.domain.viewmodel.QuickAdhocTruckEvent
import com.freight24.connect.feature.adhocTruck.domain.viewmodel.QuickAdhocTruckViewModel
import com.freight24.connect.feature.adhocTruck.domain.viewmodel.QuickAdhocTruckViewModelImpl
import com.freight24.rest.response.LocationDto
import com.freight24.rest.response.TruckTypeDto
import com.freight24.ui.components.FilterSelectionChip

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun QuickAdhocTruckScreen(
    onBackClick: () -> Unit,
    onSuccess: () -> Unit,
    viewModel: QuickAdhocTruckViewModel = hiltViewModel<QuickAdhocTruckViewModelImpl>(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val colorScheme = MaterialTheme.colorScheme

    if (uiState.submitSuccess) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                modifier = Modifier.size(72.dp),
                tint = colorScheme.primary,
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Truck entry created!",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "The adhoc truck will be available for assignment.",
                style = MaterialTheme.typography.bodyMedium,
                color = colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onSuccess,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
            ) {
                Text("Done")
            }
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Adhoc Truck", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            // 1. Phone Number with live search
            PhoneSearchSection(
                phoneNumber = uiState.phoneNumber,
                matchedOwners = uiState.matchedOwners,
                isSearching = uiState.isSearchingPhone,
                selectedOwner = uiState.selectedOwner,
                onPhoneChanged = { viewModel.onEvent(QuickAdhocTruckEvent.PhoneChanged(it)) },
                onSelectOwner = { viewModel.onEvent(QuickAdhocTruckEvent.SelectOwner(it)) },
            )

            // 2. Truck Owner Name (always visible, disabled when matched)
            OutlinedTextField(
                value = uiState.ownerName,
                onValueChange = {
                    if (uiState.selectedOwner == null) {
                        viewModel.onEvent(QuickAdhocTruckEvent.OwnerNameChanged(it))
                    }
                },
                label = { Text("Truck Owner Name") },
                placeholder = { Text("Enter name if new owner") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                enabled = uiState.selectedOwner == null,
            )

            // 3. Location Search (above truck type so dropdown clears keyboard)
            LocationSearchSection(
                query = uiState.locationQuery,
                results = uiState.locationResults,
                isSearching = uiState.isSearchingLocation,
                selectedLocation = uiState.selectedLocation,
                onQueryChanged = { viewModel.onEvent(QuickAdhocTruckEvent.LocationQueryChanged(it)) },
                onSelectLocation = { viewModel.onEvent(QuickAdhocTruckEvent.SelectLocation(it)) },
            )

            // 4. Truck Type Selection (at bottom so location results are visible)
            TruckTypeSection(
                truckTypes = uiState.truckTypes,
                selectedTruckType = uiState.selectedTruckType,
                isLoading = uiState.isLoadingTruckTypes,
                onSelect = { viewModel.onEvent(QuickAdhocTruckEvent.SelectTruckType(it)) },
            )

            Spacer(modifier = Modifier.weight(1f))

            // 5. Submit
            Button(
                onClick = { viewModel.onEvent(QuickAdhocTruckEvent.Submit) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = !uiState.isSubmitting,
            ) {
                if (uiState.isSubmitting) {
                    CircularProgressIndicator(
                        color = colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp),
                    )
                } else {
                    Text(
                        "Create Adhoc Entry",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (uiState.error != null) {
        AlertDialog(
            onDismissRequest = { viewModel.onEvent(QuickAdhocTruckEvent.ClearError) },
            confirmButton = {
                TextButton(onClick = { viewModel.onEvent(QuickAdhocTruckEvent.ClearError) }) {
                    Text("OK")
                }
            },
            title = { Text("Error") },
            text = { Text(uiState.error!!) },
        )
    }
}

@Composable
fun PhoneSearchSection(
    phoneNumber: String,
    matchedOwners: List<com.freight24.rest.response.AdhocTruckOwnerDto>,
    isSearching: Boolean,
    selectedOwner: com.freight24.rest.response.AdhocTruckOwnerDto?,
    onPhoneChanged: (String) -> Unit,
    onSelectOwner: (com.freight24.rest.response.AdhocTruckOwnerDto) -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme

    Column {
        OutlinedTextField(
            value = phoneNumber,
            onValueChange = { if (it.all { c -> c.isDigit() || c == '+' }) onPhoneChanged(it) },
            label = { Text("Phone Number") },
            placeholder = { Text("Enter phone number") },
            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
            trailingIcon = {
                if (isSearching) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Phone),
        )

        if (matchedOwners.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, colorScheme.outlineVariant),
            ) {
                Column {
                    matchedOwners.forEach { owner ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectOwner(owner) }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                tint = colorScheme.primary,
                                modifier = Modifier.size(20.dp),
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    owner.fullName,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold,
                                )
                                Text(
                                    buildString {
                                        append(owner.phone)
                                        if (owner.tagCode != null) {
                                            append(" • ${owner.tagCode}")
                                        }
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                        if (owner != matchedOwners.last()) {
                            HorizontalDivider(color = colorScheme.outlineVariant)
                        }
                    }
                }
            }
        }

        if (selectedOwner != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = colorScheme.primaryContainer),
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = colorScheme.primary,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Matched: ${selectedOwner.fullName}", fontWeight = FontWeight.SemiBold)
                        Text(
                            selectedOwner.phone,
                            style = MaterialTheme.typography.bodySmall,
                            color = colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TruckTypeSection(
    truckTypes: com.freight24.rest.response.TruckCategoryDto?,
    selectedTruckType: TruckTypeDto?,
    isLoading: Boolean,
    onSelect: (TruckTypeDto) -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme

    Column {
        Text(
            "Truck Type",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.height(8.dp))

        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.size(24.dp))
        } else if (truckTypes != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, colorScheme.outlineVariant),
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    TruckCategoryChips("Open", truckTypes.open, selectedTruckType, onSelect)
                    TruckCategoryChips("Closed", truckTypes.closed, selectedTruckType, onSelect)
                    TruckCategoryChips("Trailer", truckTypes.trailer, selectedTruckType, onSelect)
                }
            }
        } else {
            Text(
                "Failed to load truck types",
                color = colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TruckCategoryChips(
    title: String,
    types: List<TruckTypeDto>,
    selectedTruckType: TruckTypeDto?,
    onSelect: (TruckTypeDto) -> Unit,
) {
    if (types.isEmpty()) return
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            title,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            types.forEach { truckType ->
                FilterSelectionChip(
                    label = truckType.displayName,
                    isSelected = selectedTruckType?.id == truckType.id,
                    onClick = { onSelect(truckType) },
                )
            }
        }
    }
}

@Composable
fun LocationSearchSection(
    query: String,
    results: List<LocationDto>,
    isSearching: Boolean,
    selectedLocation: LocationDto?,
    onQueryChanged: (String) -> Unit,
    onSelectLocation: (LocationDto) -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme

    Column {
        OutlinedTextField(
            value = if (selectedLocation != null) selectedLocation.name else query,
            onValueChange = onQueryChanged,
            label = { Text("Truck Available Location") },
            placeholder = { Text("Type city or area name") },
            leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
            trailingIcon = {
                if (isSearching) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
        )

        if (results.isNotEmpty() && selectedLocation == null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, colorScheme.outlineVariant),
            ) {
                Column {
                    results.forEach { location ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectLocation(location) }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = colorScheme.primary,
                                modifier = Modifier.size(20.dp),
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    location.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold,
                                )
                                Text(
                                    buildString {
                                        if (!location.city.isNullOrBlank()) {
                                            append(location.city)
                                            append(", ")
                                        }
                                        append(location.state ?: "")
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                        if (location != results.last()) {
                            HorizontalDivider(color = colorScheme.outlineVariant)
                        }
                    }
                }
            }
        }
    }
}
