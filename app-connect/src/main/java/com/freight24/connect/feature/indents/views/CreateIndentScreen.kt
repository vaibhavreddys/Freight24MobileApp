package com.freight24.connect.feature.indents.views

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.freight24.rest.response.ClientDto
import com.freight24.rest.response.LocationDto
import com.freight24.rest.response.TruckCategoryDto
import com.freight24.rest.response.TruckTypeDto
import com.freight24.ui.components.FilterSelectionChip

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateIndentScreen(
    onBackClick: () -> Unit,
    viewModel: CreateIndentViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onEvent(CreateIndentEvent.ClearError)
        }
    }

    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onEvent(CreateIndentEvent.ClearSuccess)
            // Optionally navigate back here
            // onBackClick()
        }
    }

    CreateIndentContent(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onBackClick = onBackClick,
        onEvent = viewModel::onEvent,
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CreateIndentContent(
    uiState: CreateIndentUiState,
    snackbarHostState: SnackbarHostState,
    onBackClick: () -> Unit,
    onEvent: (CreateIndentEvent) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Indent") },
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
                onClick = { onEvent(CreateIndentEvent.SubmitIndent) },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                enabled = !uiState.isSubmitting,
            ) {
                if (uiState.isSubmitting) {
                    CircularProgressIndicator(
                        modifier =
                            Modifier
                                .padding(end = 8.dp)
                                .width(20.dp)
                                .height(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp,
                    )
                }
                Text("Post Indent")
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
                    // 1. Client Name
                    ClientAutoComplete(
                        label = "Client Name",
                        query = uiState.clientQuery,
                        onQueryChange = { onEvent(CreateIndentEvent.SearchClient(it)) },
                        searchResults = uiState.clientSearchResults,
                        onSelect = { onEvent(CreateIndentEvent.SelectClient(it)) },
                    )

                    // 2. From Location & 3. To Location (Side by Side)
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        LocationAutoComplete(
                            label = "Loading",
                            query = uiState.fromLocationQuery,
                            onQueryChange = { onEvent(CreateIndentEvent.SearchFromLocation(it)) },
                            searchResults = uiState.fromLocationSearchResults,
                            onSelect = { onEvent(CreateIndentEvent.SelectFromLocation(it)) },
                            modifier = Modifier.weight(1f),
                        )

                        LocationAutoComplete(
                            label = "Unloading",
                            query = uiState.toLocationQuery,
                            onQueryChange = { onEvent(CreateIndentEvent.SearchToLocation(it)) },
                            searchResults = uiState.toLocationSearchResults,
                            onSelect = { onEvent(CreateIndentEvent.SelectToLocation(it)) },
                            modifier = Modifier.weight(1f),
                        )
                    }

                    // 4. Truck Type
                    Text("Truck Type", style = MaterialTheme.typography.labelLarge)

                    if (uiState.isLoadingTruckTypes) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                    } else {
                        TruckTypeSelection(
                            truckTypes = uiState.truckTypes,
                            selectedTruckType = uiState.selectedTruckType,
                            onSelect = { onEvent(CreateIndentEvent.SelectTruckType(it)) },
                        )
                    }

                    // 5. Weight & 6. Material Name
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        OutlinedTextField(
                            value = uiState.weight,
                            onValueChange = { onEvent(CreateIndentEvent.UpdateWeight(it)) },
                            label = { Text("Weight (MT)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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

            // Optional Fields
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
                                        Text(it, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                    }
                                } else {
                                    location.state?.let {
                                        Text(it, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
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

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (openTrucks.isNotEmpty()) {
            Text("Open", style = MaterialTheme.typography.labelMedium)

            androidx.compose.foundation.layout.FlowRow(
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
        }

        Spacer(Modifier.height(8.dp))

        if (closedTrucks.isNotEmpty()) {
            Text("Closed", style = MaterialTheme.typography.labelMedium)
            androidx.compose.foundation.layout.FlowRow(
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
}

@Composable
fun OptionalFieldsSection(
    uiState: CreateIndentUiState,
    onEvent: (CreateIndentEvent) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

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
                            label = { Text("Flooring") },
                            modifier = Modifier.weight(1f),
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = uiState.tatRequired,
                            onCheckedChange = { onEvent(CreateIndentEvent.UpdateOptionalField(tatRequired = it)) },
                        )
                        Text("TAT Required")

                        Spacer(Modifier.width(16.dp))

                        Checkbox(
                            checked = uiState.tarpaulinRequired,
                            onCheckedChange = { onEvent(CreateIndentEvent.UpdateOptionalField(tarpaulinRequired = it)) },
                        )
                        Text("Tarpaulin Required")
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = uiState.hideCost,
                            onCheckedChange = { onEvent(CreateIndentEvent.UpdateOptionalField(hideCost = it)) },
                        )
                        Text("Hide Cost")
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
