package com.freight24.connect.feature.indents.views

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.freight24.connect.feature.indents.domain.viewmodel.CreateIndentEvent
import com.freight24.connect.feature.indents.domain.viewmodel.CreateIndentUiState
import com.freight24.connect.feature.indents.domain.viewmodel.IndentsViewModel
import com.freight24.connect.feature.indents.domain.viewmodel.IndentsViewModelImpl
import com.freight24.rest.response.LocationDto
import com.freight24.rest.response.TruckTypeDto
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
                    val client = Json.decodeFromString<com.freight24.rest.response.ClientDto>(it)
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
    val isDark = isSystemInDarkTheme()
    val scope = rememberCoroutineScope()
    val fieldErrors = remember { mutableStateOf(emptySet<String>()) }

    val bgGradient = if (isDark) {
        Brush.verticalGradient(
            colors = listOf(Color(0xFF0F172A), Color(0xFF111827))
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(Color(0xFFF8FAFC), Color(0xFFF1F5F9))
        )
    }

    fun validate(): Boolean {
        val errors = mutableSetOf<String>()

        if (uiState.selectedClient == null) errors.add("client")
        if (uiState.selectedFromLocation == null) errors.add("from")
        if (uiState.selectedToLocation == null) errors.add("to")
        if (uiState.selectedTruckType == null) errors.add("truck")
        if (uiState.weight.isBlank()) errors.add("weight")
        if (uiState.materialName.isBlank()) errors.add("material")
        if (uiState.tatRequired && uiState.tatHours.isBlank()) errors.add("tat")

        fieldErrors.value = errors

        if (errors.isNotEmpty()) {
            val message = when {
                errors.contains("client") -> "Please select a client"
                errors.contains("from") || errors.contains("to") -> "Please enter route information"
                errors.contains("truck") -> "Please select truck type"
                errors.contains("weight") || errors.contains("material") -> "Please enter cargo details"
                errors.contains("tat") -> "Please enter TAT hours"
                else -> "Please fill all required fields"
            }
            scope.launch { snackbarHostState.showSnackbar(message) }
            return false
        }
        return true
    }

    fun clearError(key: String) {
        fieldErrors.value = fieldErrors.value - key
    }

    Box(Modifier.fillMaxSize()) {
        Box(Modifier.fillMaxSize().background(bgGradient))

        if (isDark) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.45f)
                    .align(Alignment.TopEnd)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color.Transparent,
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.06f),
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
                            )
                        )
                    )
            )
        }

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text("Create Indent") },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) },
            bottomBar = {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .background(Color.Transparent)
                        .padding(horizontal = 20.dp)
                        .padding(bottom = 20.dp)
                ) {
                    Button(
                        onClick = {
                            if (validate()) onNavigateToReview()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(28.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp),
                    ) {
                        Text(
                            "Review Indent",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        Spacer(Modifier.width(8.dp))
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
            },
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
                    .padding(top = 16.dp)
                    .padding(bottom = 8.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                ClientSection(
                    selectedClient = uiState.selectedClient,
                    onClick = {
                        clearError("client")
                        onNavigateToSearch(SearchType.CLIENT.name)
                    },
                    isError = fieldErrors.value.contains("client"),
                )

                RouteSection(
                    fromLocation = uiState.selectedFromLocation?.name ?: "",
                    toLocation = uiState.selectedToLocation?.name ?: "",
                    onFromClick = {
                        clearError("from")
                        onNavigateToSearch(SearchType.LOCATION_FROM.name)
                    },
                    onToClick = {
                        clearError("to")
                        onNavigateToSearch(SearchType.LOCATION_TO.name)
                    },
                    fromError = fieldErrors.value.contains("from"),
                    toError = fieldErrors.value.contains("to"),
                )

                VehicleTypeSection(
                    truckTypes = uiState.truckTypes,
                    selectedTruckType = uiState.selectedTruckType,
                    isLoading = uiState.isLoadingTruckTypes,
                    onSelect = {
                        clearError("truck")
                        onEvent(CreateIndentEvent.SelectTruckType(it))
                    },
                    isError = fieldErrors.value.contains("truck"),
                )

                CargoSection(
                    weight = uiState.weight,
                    materialName = uiState.materialName,
                    onWeightChange = {
                        clearError("weight")
                        onEvent(CreateIndentEvent.UpdateWeight(it))
                    },
                    onMaterialChange = {
                        clearError("material")
                        onEvent(CreateIndentEvent.UpdateMaterialName(it))
                    },
                    weightError = fieldErrors.value.contains("weight"),
                    materialError = fieldErrors.value.contains("material"),
                )

                OptionalDetailsSection(
                    uiState = uiState,
                    onEvent = onEvent,
                    tatError = fieldErrors.value.contains("tat"),
                    onTatClear = { clearError("tat") },
                )

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun SectionCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSystemInDarkTheme()) 0.dp else 2.dp,
        ),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = content,
        )
    }
}

@Composable
private fun IndentSelectionField(
    value: String,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
) {
    val borderColor = when {
        isError -> MaterialTheme.colorScheme.error
        value.isNotEmpty() -> MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        else -> MaterialTheme.colorScheme.outlineVariant
    }

    OutlinedTextField(
        value = value,
        onValueChange = {},
        label = { Text(label) },
        readOnly = true,
        enabled = false,
        trailingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = OutlinedTextFieldDefaults.colors(
            disabledTextColor = MaterialTheme.colorScheme.onSurface,
            disabledBorderColor = borderColor,
            disabledLabelColor = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
            disabledTrailingIconColor = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
            disabledContainerColor = Color.Transparent,
        ),
        shape = RoundedCornerShape(16.dp),
        singleLine = true,
        textStyle = MaterialTheme.typography.bodyMedium,
    )
}

@Composable
private fun SectionHeaderText(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier,
    )
}

@Composable
private fun ClientSection(
    selectedClient: com.freight24.rest.response.ClientDto?,
    onClick: () -> Unit,
    isError: Boolean = false,
) {
    SectionCard {
        SectionHeaderText("Client Details")

        val clientDisplay = selectedClient?.run {
            "$companyName, $tagCode"
        } ?: ""

        IndentSelectionField(
            value = clientDisplay,
            label = "Search Client Name",
            onClick = onClick,
            isError = isError,
        )
    }
}

@Composable
private fun RouteSection(
    fromLocation: String,
    toLocation: String,
    onFromClick: () -> Unit,
    onToClick: () -> Unit,
    fromError: Boolean = false,
    toError: Boolean = false,
) {
    SectionCard {
        SectionHeaderText("Route")

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            IndentSelectionField(
                value = fromLocation,
                label = "Loading",
                onClick = onFromClick,
                modifier = Modifier.weight(1f),
                isError = fromError,
            )

            IndentSelectionField(
                value = toLocation,
                label = "Unloading",
                onClick = onToClick,
                modifier = Modifier.weight(1f),
                isError = toError,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun VehicleTypeSection(
    truckTypes: com.freight24.rest.response.TruckCategoryDto,
    selectedTruckType: TruckTypeDto?,
    isLoading: Boolean,
    onSelect: (TruckTypeDto) -> Unit,
    isError: Boolean = false,
) {
    SectionCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SectionHeaderText("Vehicle Type")

            Surface(
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.surfaceVariant,
            ) {
                Text(
                    "CLOSED",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        if (isError) {
            Text(
                "Please select a vehicle type",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp,
                )
            }
        } else {
            val openTrucks = truckTypes.open.sortedByDescending { it.displayName.length }
            val closedTrucks = truckTypes.closed.sortedByDescending { it.displayName.length }

            if (openTrucks.isNotEmpty()) {
                Text(
                    "Open",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    openTrucks.forEach { truck ->
                        VehicleTypeChip(
                            label = truck.displayName,
                            isSelected = selectedTruckType?.id == truck.id,
                            onClick = { onSelect(truck) },
                        )
                    }
                }
            }

            if (closedTrucks.isNotEmpty()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    "Closed",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    closedTrucks.forEach { truck ->
                        VehicleTypeChip(
                            label = truck.displayName,
                            isSelected = selectedTruckType?.id == truck.id,
                            onClick = { onSelect(truck) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun VehicleTypeChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val bgColor = if (isSelected) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
    } else {
        Color.Transparent
    }
    val borderColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outlineVariant
    }
    val textColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(50),
        color = bgColor,
        border = BorderStroke(1.dp, borderColor),
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = textColor,
        )
    }
}

@Composable
private fun CargoSection(
    weight: String,
    materialName: String,
    onWeightChange: (String) -> Unit,
    onMaterialChange: (String) -> Unit,
    weightError: Boolean = false,
    materialError: Boolean = false,
) {
    SectionCard {
        SectionHeaderText("Cargo Details")

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = weight,
                onValueChange = onWeightChange,
                label = { Text("Weight (MT)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                isError = weightError,
            )
            OutlinedTextField(
                value = materialName,
                onValueChange = onMaterialChange,
                label = { Text("Material Name") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                isError = materialError,
            )
        }
    }
}

@Composable
private fun OptionalDetailsSection(
    uiState: CreateIndentUiState,
    onEvent: (CreateIndentEvent) -> Unit,
    tatError: Boolean = false,
    onTatClear: () -> Unit = {},
) {
    var expanded by remember { mutableStateOf(true) }

    SectionCard {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "Optional Details",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Icon(
                imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = "Expand",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(),
            exit = shrinkVertically(),
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                SectionHeaderText("Cost Breakdown")

                OutlinedTextField(
                    value = uiState.clientCost,
                    onValueChange = { onEvent(CreateIndentEvent.UpdateOptionalField(clientCost = it)) },
                    label = { Text("Client Cost (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                )

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = uiState.negotiatedCost,
                        onValueChange = { onEvent(CreateIndentEvent.UpdateOptionalField(negotiatedCost = it)) },
                        label = { Text("Negotiated (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true,
                    )
                    OutlinedTextField(
                        value = uiState.businessProposedCost,
                        onValueChange = { onEvent(CreateIndentEvent.UpdateOptionalField(businessProposedCost = it)) },
                        label = { Text("Proposed (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true,
                    )
                }

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    modifier = Modifier.padding(vertical = 4.dp),
                )

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = uiState.vehicleHeight,
                        onValueChange = { onEvent(CreateIndentEvent.UpdateOptionalField(vehicleHeight = it)) },
                        label = { Text("Height (ft)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true,
                    )
                    OutlinedTextField(
                        value = uiState.flooringType,
                        onValueChange = { onEvent(CreateIndentEvent.UpdateOptionalField(flooringType = it)) },
                        label = { Text("Flooring Type") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true,
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Checkbox(
                            checked = uiState.tarpaulinRequired,
                            onCheckedChange = {
                                onEvent(CreateIndentEvent.UpdateOptionalField(tarpaulinRequired = it))
                            },
                            colors = CheckboxDefaults.colors(
                                checkedColor = MaterialTheme.colorScheme.primary,
                            ),
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "Tarpaulin",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }

                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Checkbox(
                            checked = uiState.hideCost,
                            onCheckedChange = {
                                onEvent(CreateIndentEvent.UpdateOptionalField(hideCost = it))
                            },
                            colors = CheckboxDefaults.colors(
                                checkedColor = MaterialTheme.colorScheme.primary,
                            ),
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "Hide Cost",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Checkbox(
                            checked = uiState.tatRequired,
                            onCheckedChange = {
                                onEvent(CreateIndentEvent.UpdateOptionalField(tatRequired = it))
                            },
                            colors = CheckboxDefaults.colors(
                                checkedColor = MaterialTheme.colorScheme.primary,
                            ),
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "TAT",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }

                    OutlinedTextField(
                        value = uiState.tatHours,
                        enabled = uiState.tatRequired,
                        onValueChange = {
                            onTatClear()
                            onEvent(CreateIndentEvent.UpdateOptionalField(tatHours = it))
                        },
                        label = { Text("TAT hours") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        isError = tatError,
                    )
                }

                OutlinedTextField(
                    value = uiState.remarks,
                    onValueChange = { onEvent(CreateIndentEvent.UpdateOptionalField(remarks = it)) },
                    label = { Text("Remarks") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    minLines = 2,
                )
            }
        }
    }
}
