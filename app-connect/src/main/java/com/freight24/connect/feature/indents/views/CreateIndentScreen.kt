package com.freight24.connect.feature.indents.views

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.freight24.connect.feature.indents.domain.viewmodel.CreateIndentEvent
import com.freight24.connect.feature.indents.domain.viewmodel.CreateIndentUiState
import com.freight24.connect.feature.indents.domain.viewmodel.IndentsViewModel
import com.freight24.connect.feature.indents.domain.viewmodel.IndentsViewModelImpl
import com.freight24.connect.navigation.ConnectDestination
import com.freight24.rest.response.ClientDto
import com.freight24.rest.response.LocationDto
import com.freight24.rest.response.TruckCategoryDto
import com.freight24.rest.response.TruckTypeDto
import com.freight24.ui.components.FilterSelectionChip
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateIndentScreen(
    onBackClick: () -> Unit,
    onNavigateToSearch: (String) -> Unit,
    onNavigateToReview: () -> Unit,
    navController: NavController,
) {
    val viewModel: IndentsViewModel = hiltViewModel<IndentsViewModelImpl>()
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            Log.e("CreateIndentScreen", "Error: $it")
            snackbarHostState.showSnackbar(it)
            viewModel.onEvent(CreateIndentEvent.ClearError)
        }
    }

    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onEvent(CreateIndentEvent.ClearSuccess)
        }
    }

    val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle

    LaunchedEffect(savedStateHandle) {
        savedStateHandle?.getStateFlow<String?>("selected_from_location", null)
            ?.collect { json ->
                json?.let {
                    val location = Json.decodeFromString<LocationDto>(it)
                    viewModel.onEvent(CreateIndentEvent.SelectFromLocation(location))
                    savedStateHandle.remove<String>("selected_from_location")
                }
            }
    }

    LaunchedEffect(savedStateHandle) {
        savedStateHandle?.getStateFlow<String?>("selected_to_location", null)
            ?.collect { json ->
                json?.let {
                    val location = Json.decodeFromString<LocationDto>(it)
                    viewModel.onEvent(CreateIndentEvent.SelectToLocation(location))
                    savedStateHandle.remove<String>("selected_to_location")
                }
            }
    }

    LaunchedEffect(savedStateHandle) {
        savedStateHandle?.getStateFlow<String?>("selected_client", null)
            ?.collect { json ->
                json?.let {
                    val client = Json.decodeFromString<ClientDto>(it)
                    viewModel.onEvent(CreateIndentEvent.SelectClient(client))
                    savedStateHandle.remove<String>("selected_client")
                }
            }
    }

    CreateIndentContent(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onBackClick = onBackClick,
        onNavigateToSearch = onNavigateToSearch,
        onEvent = viewModel::onEvent,
        onNavigateToReview = onNavigateToReview
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CreateIndentContent(
    uiState: CreateIndentUiState,
    snackbarHostState: SnackbarHostState,
    onBackClick: () -> Unit,
    onNavigateToSearch: (String) -> Unit,
    onEvent: (CreateIndentEvent) -> Unit,
    onNavigateToReview: () -> Unit,
) {
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Indent") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            Button(
                onClick = {
                    val errorMessage = when {
                        uiState.selectedClient == null -> "Please select a client"
                        uiState.selectedFromLocation == null -> "Please select loading location"
                        uiState.selectedToLocation == null -> "Please select unloading location"
                        uiState.selectedTruckType == null -> "Please select truck type"
                        uiState.materialName.isBlank() -> "Please enter material name"
                        uiState.weight.isBlank() -> "Please enter weight"
                        uiState.tatRequired && uiState.tatHours.isBlank() -> "Please enter TAT hours"
                        else -> null
                    }
                    if (errorMessage != null) {
                        scope.launch {
                            snackbarHostState.showSnackbar(errorMessage)
                        }
                    } else {
                        onNavigateToReview()
                    }
                },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
            ) {
                Text("Review Indent")
            }
        },
    ) { paddingValues ->

        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    val clientName = uiState.selectedClient?.run {
                        "${uiState.selectedClient.companyName}, ${uiState.selectedClient.tagCode}"
                    } ?: ""

                    OutlinedTextField(
                        value = clientName,
                        onValueChange = {},
                        label = { Text("Client Name") },
                        readOnly = true,
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                            )
                        },
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onNavigateToSearch(SearchType.CLIENT.name)
                                },
                        enabled = false,
                        colors =
                            OutlinedTextFieldDefaults.colors(
                                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                disabledBorderColor = MaterialTheme.colorScheme.outline,
                                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            ),
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyMedium
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        OutlinedTextField(
                            value = uiState.selectedFromLocation?.name ?: "",
                            onValueChange = {},
                            label = { Text("Loading") },
                            readOnly = true,
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search",
                                )
                            },
                            modifier =
                                Modifier
                                    .weight(1f)
                                    .clickable {
                                        onNavigateToSearch(SearchType.LOCATION_FROM.name)
                                    },
                            enabled = false,
                            colors =
                                OutlinedTextFieldDefaults.colors(
                                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                    disabledBorderColor = MaterialTheme.colorScheme.outline,
                                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                ),
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodyMedium
                        )

                        OutlinedTextField(
                            value = uiState.selectedToLocation?.name ?: "",
                            onValueChange = {},
                            label = { Text("Unloading") },
                            readOnly = true,
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search",
                                )
                            },
                            modifier =
                                Modifier
                                    .weight(1f)
                                    .clickable {
                                        onNavigateToSearch(SearchType.LOCATION_TO.name)
                                    },
                            enabled = false,
                            colors =
                                OutlinedTextFieldDefaults.colors(
                                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                    disabledBorderColor = MaterialTheme.colorScheme.outline,
                                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                ),
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodyMedium
                        )
                    }

                    if (uiState.isLoadingTruckTypes) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                    } else {
                        TruckTypeSelection(
                            truckTypes = uiState.truckTypes,
                            selectedTruckType = uiState.selectedTruckType,
                            onSelect = { onEvent(CreateIndentEvent.SelectTruckType(it)) },
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        OutlinedTextField(
                            value = uiState.weight,
                            onValueChange = { onEvent(CreateIndentEvent.UpdateWeight(it)) },
                            label = { Text("Weight (MT)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                        )

                        OutlinedTextField(
                            value = uiState.materialName,
                            onValueChange = { onEvent(CreateIndentEvent.UpdateMaterialName(it)) },
                            label = { Text("Material Name") },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }

            OptionalFieldsSection(
                uiState = uiState,
                onEvent = onEvent,
            )
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(vertical = 8.dp),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationAutoComplete(
    label: String,
    query: String,
    onQueryChange: (String) -> Unit,
    searchResults: List<LocationDto>,
    onSelect: (LocationDto) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    ExposedDropdownMenuBox(
        modifier = modifier,
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = {
                onQueryChange(it)
                expanded = true
            },
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            modifier =
                modifier
                    .fillMaxWidth()
                    .menuAnchor(),
        )

        if (searchResults.isNotEmpty()) {
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                searchResults.forEach { location ->
                    DropdownMenuItem(
                        text = {
                                    Column {
                                        Text(location.name, style = MaterialTheme.typography.bodyLarge)
                                        if (location.type.equals("area", true)) {
                                            location.city?.let {
                                        Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                        } else {
                                            location.state?.let {
                                        Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                        }
                                    }
                        },
                        onClick = {
                            onSelect(location)
                            expanded = false
                            focusManager.clearFocus()
                        },
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientAutoComplete(
    label: String,
    query: String,
    onQueryChange: (String) -> Unit,
    searchResults: List<ClientDto>,
    onSelect: (ClientDto) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = {
                onQueryChange(it)
                expanded = true
            },
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            modifier =
                modifier
                    .fillMaxWidth()
                    .menuAnchor(),
        )

        if (searchResults.isNotEmpty()) {
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                searchResults.forEach { client ->
                    DropdownMenuItem(
                        text = { Text(client.companyName) },
                        onClick = {
                            onSelect(client)
                            expanded = false
                            focusManager.clearFocus()
                        },
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TruckTypeSelection(
    truckTypes: TruckCategoryDto,
    selectedTruckType: TruckTypeDto?,
    onSelect: (TruckTypeDto) -> Unit,
) {
    // Sort by name length descending so larger items likely appear first or fitting logic
    val openTrucks =
        truckTypes
            .open
            .sortedByDescending { it.displayName.length }

    val closedTrucks =
        truckTypes
            .closed
            .sortedByDescending { it.displayName.length }

    if (openTrucks.isNotEmpty()) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {

            Text("Open", style = MaterialTheme.typography.labelMedium)

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                openTrucks.forEach { truck ->
                    FilterSelectionChip(
                        label = truck.displayName,
                        isSelected = selectedTruckType?.id == truck.id,
                        onClick = { onSelect(truck) },
                    )
                }
            }

            Spacer(Modifier.height(8.dp))
        }
    }

    if (closedTrucks.isNotEmpty()) {
        Text("Closed", style = MaterialTheme.typography.labelMedium)

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            closedTrucks.forEach { truck ->
                FilterSelectionChip(
                    label = truck.displayName,
                    isSelected = selectedTruckType?.id == truck.id,
                    onClick = { onSelect(truck) },
                )
            }
        }
    }

}

@Composable
fun OptionalFieldsSection(
    uiState: CreateIndentUiState,
    onEvent: (CreateIndentEvent) -> Unit,
) {
    var expanded by remember { mutableStateOf(true) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clickable { expanded = !expanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "Optional Details",
                    style = MaterialTheme.typography.titleMedium,
                )
                Icon(
                    if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = "Expand",
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(),
                exit = shrinkVertically(),
            ) {
                Column(
                    modifier = Modifier.padding(top = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    OutlinedTextField(
                        value = uiState.clientCost,
                        onValueChange = { onEvent(CreateIndentEvent.UpdateOptionalField(clientCost = it)) },
                        label = { Text("Client Cost (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        OutlinedTextField(
                            value = uiState.negotiatedCost,
                            onValueChange = { onEvent(CreateIndentEvent.UpdateOptionalField(negotiatedCost = it)) },
                            label = { Text("Negotiated Cost (₹)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                        )
                        OutlinedTextField(
                            value = uiState.businessProposedCost,
                            onValueChange = { onEvent(CreateIndentEvent.UpdateOptionalField(businessProposedCost = it)) },
                            label = { Text("Proposed Cost (₹)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        OutlinedTextField(
                            value = uiState.vehicleHeight,
                            onValueChange = { onEvent(CreateIndentEvent.UpdateOptionalField(vehicleHeight = it)) },
                            label = { Text("Height (ft)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                        )
                        // Dropdown for Flooring often better but TextField for now as per simple impl
                        OutlinedTextField(
                            value = uiState.flooringType,
                            onValueChange = { onEvent(CreateIndentEvent.UpdateOptionalField(flooringType = it)) },
                            label = { Text("Flooring Type") },
                            modifier = Modifier.weight(1f),
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {

                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = uiState.tarpaulinRequired,
                                onCheckedChange = {
                                    onEvent(
                                        CreateIndentEvent.UpdateOptionalField(
                                            tarpaulinRequired = it
                                        )
                                    )
                                },
                            )
                            Text("Tarpaulin")
                        }

                        Spacer(Modifier.size(16.dp))

                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            Checkbox(
                                checked = uiState.hideCost,
                                onCheckedChange = {
                                    onEvent(
                                        CreateIndentEvent.UpdateOptionalField(
                                            hideCost = it
                                        )
                                    )
                                },
                            )

                            Text("Hide Cost")
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = uiState.tatRequired,
                                onCheckedChange = {
                                    onEvent(
                                        CreateIndentEvent.UpdateOptionalField(
                                            tatRequired = it
                                        )
                                    )
                                },
                            )
                            Text("TAT")
                        }

                        Spacer(Modifier.size(16.dp))

                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            OutlinedTextField(
                                value = uiState.tatHours,
                                enabled = uiState.tatRequired,
                                onValueChange = {
                                    onEvent(
                                        CreateIndentEvent.UpdateOptionalField(
                                            tatHours = it
                                        )
                                    )
                                },
                                label = { Text("TAT hours") },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            )
                        }
                    }

                    OutlinedTextField(
                        value = uiState.remarks,
                        onValueChange = { onEvent(CreateIndentEvent.UpdateOptionalField(remarks = it)) },
                        label = { Text("Remarks") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                    )
                }
            }
        }
    }
}
