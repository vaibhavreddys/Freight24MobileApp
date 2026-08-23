package com.freight24.connect.feature.indents.views
import com.freight24.ui.components.FreightTopAppBar

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
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.freight24.common.util.formatIndianCurrency
import com.freight24.common.util.negotiatedCostExceedsClientError
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

    // Screen background — solid M3 background, not a gradient. M3 prefers
    // tonal hierarchy (surface / surfaceContainer) over visible gradients
    // for page chrome, and a solid background tracks theme changes
    // automatically (light/dark/system/dynamic).
    val screenBackground = MaterialTheme.colorScheme.background

    fun validate(): Boolean {
        val errors = mutableSetOf<String>()

        if (uiState.selectedClient == null) errors.add("client")
        if (uiState.selectedFromLocation == null) errors.add("from")
        if (uiState.selectedToLocation == null) errors.add("to")
        if (uiState.selectedTruckType == null) errors.add("truck")
        if (uiState.weight.isBlank()) errors.add("weight")
        if (uiState.materialName.isBlank()) errors.add("material")
        if (uiState.tatRequired && uiState.tatHours.isBlank()) errors.add("tat")
        if (negotiatedCostExceedsClientError(uiState.negotiatedCost, uiState.clientCost) != null) errors.add("negotiated")

        fieldErrors.value = errors

        if (errors.isNotEmpty()) {
            val message = when {
                errors.contains("client") -> "Please select a client"
                errors.contains("from") || errors.contains("to") -> "Please enter route information"
                errors.contains("truck") -> "Please select truck type"
                errors.contains("weight") || errors.contains("material") -> "Please enter cargo details"
                errors.contains("tat") -> "Please enter TAT hours"
                errors.contains("negotiated") -> "Negotiated cost cannot exceed client cost"
                else -> "Please fill all required fields"
            }
            scope.launch { snackbarHostState.showSnackbar(message) }
            return false
        }
        return true
    }

    fun clearError(key: String) {
        fieldErrors.value -= key
    }

    Box(Modifier.fillMaxSize()) {
//        Box(Modifier.fillMaxSize().background(screenBackground))

//        if (isDark) {
//            Box(
//                Modifier
//                    .fillMaxWidth()
//                    .fillMaxHeight(0.45f)
//                    .align(Alignment.TopEnd)
//                    .background(
//                        Brush.horizontalGradient(
//                            colors = listOf(
//                                Color.Transparent,
//                                MaterialTheme.colorScheme.primary.copy(alpha = 0.06f),
//                                MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
//                            )
//                        )
//                    )
//            )
//        }

        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                FreightTopAppBar(
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
                        .background(MaterialTheme.colorScheme.background)
                        .navigationBarsPadding()
                        .padding(horizontal = 20.dp)
                        .padding(bottom = 20.dp, top = 10.dp)
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
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
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
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
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
        SectionHeaderText("Vehicle Type")

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
            val openTrucks = truckTypes.open
            val closedTrucks = truckTypes.closed

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
    val negotiatedCostError = negotiatedCostExceedsClientError(uiState.negotiatedCost, uiState.clientCost)

    var expanded by remember { mutableStateOf(true) }
    var costFieldValue by remember {
        mutableStateOf(TextFieldValue(uiState.clientCost, TextRange(uiState.clientCost.length)))
    }
    LaunchedEffect(uiState.clientCost) {
        if (costFieldValue.text != uiState.clientCost) {
            costFieldValue = TextFieldValue(uiState.clientCost, TextRange(uiState.clientCost.length))
        }
    }
    var proposedCostFieldValue by remember {
        mutableStateOf(TextFieldValue(uiState.businessProposedCost, TextRange(uiState.businessProposedCost.length)))
    }
    LaunchedEffect(uiState.businessProposedCost) {
        if (proposedCostFieldValue.text != uiState.businessProposedCost) {
            proposedCostFieldValue = TextFieldValue(uiState.businessProposedCost, TextRange(uiState.businessProposedCost.length))
        }
    }
    var negotiatedCostFieldValue by remember {
        mutableStateOf(TextFieldValue(uiState.negotiatedCost, TextRange(uiState.negotiatedCost.length)))
    }
    LaunchedEffect(uiState.negotiatedCost) {
        if (negotiatedCostFieldValue.text != uiState.negotiatedCost) {
            negotiatedCostFieldValue = TextFieldValue(uiState.negotiatedCost, TextRange(uiState.negotiatedCost.length))
        }
    }

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
                SectionHeaderText("Trip Cost Breakdown")

                OutlinedTextField(
                    value = costFieldValue,
                    onValueChange = { newValue ->
                        val rawText = newValue.text.replace(",", "")
                        val formatted = formatIndianCurrency(rawText)

                        // Calculate cursor shift caused by added/removed commas
                        val commaDiff = formatted.count { it == ',' } - costFieldValue.text.count { it == ',' }
                        val newCursor = (newValue.selection.start + commaDiff).coerceIn(0, formatted.length)

                        costFieldValue = TextFieldValue(formatted, TextRange(newCursor))
                        onEvent(CreateIndentEvent.UpdateOptionalField(clientCost = formatted))
                    },
                    label = {
                        Text(
                            text = "Client Cost (₹)",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                )

                // Costs stack full-width — the (₹) labels cramp in half-width fields.
                OutlinedTextField(
                    value = proposedCostFieldValue,
                    onValueChange = { newValue ->
                        val rawText = newValue.text.replace(",", "")
                        val formatted = formatIndianCurrency(rawText)

                        val commaDiff = formatted.count { it == ',' } - proposedCostFieldValue.text.count { it == ',' }
                        val newCursor = (newValue.selection.start + commaDiff).coerceIn(0, formatted.length)

                        proposedCostFieldValue = TextFieldValue(formatted, TextRange(newCursor))
                        onEvent(CreateIndentEvent.UpdateOptionalField(businessProposedCost = formatted))
                    },
                    label = {
                        Text(
                            text = "Proposed Cost (₹)",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                )
                OutlinedTextField(
                    value = negotiatedCostFieldValue,
                    onValueChange = { newValue ->
                        val rawText = newValue.text.replace(",", "")
                        val formatted = formatIndianCurrency(rawText)

                        val commaDiff = formatted.count { it == ',' } - negotiatedCostFieldValue.text.count { it == ',' }
                        val newCursor = (newValue.selection.start + commaDiff).coerceIn(0, formatted.length)

                        negotiatedCostFieldValue = TextFieldValue(formatted, TextRange(newCursor))
                        onEvent(CreateIndentEvent.UpdateOptionalField(negotiatedCost = formatted))
                    },
                    label = {
                        Text(
                            text = "Negotiated Cost (₹)",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                    isError = negotiatedCostError != null,
                    supportingText = negotiatedCostError?.let { msg -> { Text(msg) } },
                )

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    modifier = Modifier.padding(vertical = 4.dp),
                )

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = uiState.vehicleHeight,
                        onValueChange = { onEvent(CreateIndentEvent.UpdateOptionalField(vehicleHeight = it)) },
                        label = {
                            Text(
                                text = "Height (ft)",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true,
                    )
                    OutlinedTextField(
                        value = uiState.flooringType,
                        onValueChange = { onEvent(CreateIndentEvent.UpdateOptionalField(flooringType = it)) },
                        label = {
                            Text(
                                text = "Flooring Type",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        },
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
                            "Hide Indent Cost",
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
