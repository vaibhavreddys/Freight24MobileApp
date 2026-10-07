package com.freight24.connect.feature.indents.views
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.freight24.common.util.formatLoadWeight
import com.freight24.connect.feature.indents.domain.viewmodel.CloneIndentEvent
import com.freight24.connect.feature.indents.domain.viewmodel.CloneIndentUiState
import com.freight24.connect.feature.indents.domain.viewmodel.CloneIndentViewModel
import com.freight24.connect.feature.indents.domain.viewmodel.CloneIndentViewModelImpl
import com.freight24.ui.components.FreightTopAppBar
import com.freight24.ui.components.formViewportInsets

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CloneIndentScreen(
    indentId: String,
    indentGroupId: String,
    onBackClick: () -> Unit,
    onReviewClick: () -> Unit,
    viewModel: CloneIndentViewModel = hiltViewModel<CloneIndentViewModelImpl>(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(indentId) {
        viewModel.onEvent(
            CloneIndentEvent.LoadIndent(
                indentId,
                indentGroupId,
            ),
        )
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onEvent(CloneIndentEvent.ClearError)
        }
    }

    Scaffold(
        topBar = {
            FreightTopAppBar(
                title = { Text("Clone Indent") },
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
                onClick = onReviewClick,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                // A rate above the client cost is refused downstream, so the review
                // step is closed rather than letting it fail after the fact.
                enabled = uiState.indent != null && uiState.costError == null,
            ) {
                Text("Review Clone Indent")
            }
        },
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (uiState.indent != null) {
            CloneIndentContent(
                paddingValues = paddingValues,
                uiState = uiState,
                onEvent = viewModel::onEvent,
            )
        }
    }
}

@Composable
fun CloneIndentContent(
    paddingValues: PaddingValues,
    uiState: CloneIndentUiState,
    onEvent: (CloneIndentEvent) -> Unit,
) {
    val indent = uiState.indent!!
    val groupDetails = indent.indentGroupDetails
    val loadingLocation = groupDetails.indentLocations.find { it.locationType == "loading" }
    val unloadingLocation = groupDetails.indentLocations.find { it.locationType == "unloading" }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                // Keyboard-aware viewport: without this the scroll bounds run behind the keyboard
                // and bring-into-view leaves the focused field hidden. See FormInsets.kt.
                .formViewportInsets(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Read-only Mandatory Fields Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    "Original Indent Details (Read-only)",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )

                OutlinedTextField(
                    value = groupDetails.clientProfile?.companyName ?: groupDetails.clientProfile?.fullName ?: "Unknown",
                    onValueChange = {},
                    label = { Text("Client Name") },
                    readOnly = true,
                    enabled = false,
                    modifier = Modifier.fillMaxWidth(),
                )

                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedTextField(
                        value = loadingLocation?.areaName ?: loadingLocation?.cityName ?: "Unknown",
                        onValueChange = {},
                        label = { Text("Loading") },
                        readOnly = true,
                        enabled = false,
                        modifier = Modifier.weight(1f),
                    )

                    OutlinedTextField(
                        value = unloadingLocation?.areaName ?: unloadingLocation?.cityName ?: "Unknown",
                        onValueChange = {},
                        label = { Text("Unloading") },
                        readOnly = true,
                        enabled = false,
                        modifier = Modifier.weight(1f),
                    )
                }

                OutlinedTextField(
                    value = groupDetails.truckType?.displayName ?: "Unknown",
                    onValueChange = {},
                    label = { Text("Vehicle Type") },
                    readOnly = true,
                    enabled = false,
                    modifier = Modifier.fillMaxWidth(),
                )

                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedTextField(
                        value = formatLoadWeight(groupDetails.loadWeight, "MT"),
                        onValueChange = {},
                        label = { Text("Weight") },
                        readOnly = true,
                        enabled = false,
                        modifier = Modifier.weight(1f),
                    )

                    OutlinedTextField(
                        value = groupDetails.material,
                        onValueChange = {},
                        label = { Text("Material") },
                        readOnly = true,
                        enabled = false,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }

        // Clone-specific Fields
//        Card(
//            modifier = Modifier.fillMaxWidth(),
//            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
//            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
//        ) {
//            Column(
//                modifier = Modifier.padding(16.dp),
//                verticalArrangement = Arrangement.spacedBy(12.dp),
//            ) {
//                Text(
//                    "Clone Settings",
//                    style = MaterialTheme.typography.titleMedium,
//                )
//
//                OutlinedTextField(
//                    value = uiState.assignedTo,
//                    onValueChange = { onEvent(CloneIndentEvent.UpdateField(assignedTo = it)) },
//                    label = { Text("Assigned To (Optional)") },
//                    modifier = Modifier.fillMaxWidth(),
//                )
//
//                OutlinedTextField(
//                    value = uiState.notes,
//                    onValueChange = { onEvent(CloneIndentEvent.UpdateField(notes = it)) },
//                    label = { Text("Notes (Optional)") },
//                    modifier = Modifier.fillMaxWidth(),
//                    minLines = 2,
//                )
//            }
//        }

        // Editable Optional Fields Section
        CloneOptionalFieldsSection(uiState = uiState, onEvent = onEvent)
    }
}

@Composable
fun CloneOptionalFieldsSection(
    uiState: CloneIndentUiState,
    onEvent: (CloneIndentEvent) -> Unit,
) {
    var expanded by remember { mutableStateOf(true) }

    // Flagged live, exactly as Create does: the rule turns red and states the
    // breach under the offending field rather than failing at submit time. Both
    // come off the state so the field flags and the review button can never
    // disagree about whether the rates are valid.
    val negotiatedError = uiState.negotiatedCostError
    val proposedError = uiState.proposedCostError

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
                    "Optional Details (Editable)",
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
                    MoneyRuleField(
                        label = "Client Cost",
                        raw = uiState.clientCost,
                        testTag = "clone_indent_client_cost_field",
                        onCommit = { onEvent(CloneIndentEvent.UpdateField(clientCost = it)) },
                        trailingLabel = "INR",
                        wholeRupeesOnly = true,
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        MoneyRuleField(
                            label = "Negotiated Cost",
                            raw = uiState.negotiatedCost,
                            testTag = "clone_indent_negotiated_cost_field",
                            onCommit = { onEvent(CloneIndentEvent.UpdateField(negotiatedCost = it)) },
                            trailingLabel = "INR",
                            modifier = Modifier.weight(1f),
                            isError = negotiatedError != null,
                            supportingText = negotiatedError,
                            wholeRupeesOnly = true,
                        )
                        MoneyRuleField(
                            label = "Proposed Cost",
                            raw = uiState.businessProposedCost,
                            testTag = "clone_indent_proposed_cost_field",
                            onCommit = { onEvent(CloneIndentEvent.UpdateField(businessProposedCost = it)) },
                            trailingLabel = "INR",
                            modifier = Modifier.weight(1f),
                            isError = proposedError != null,
                            supportingText = proposedError,
                            wholeRupeesOnly = true,
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        OutlinedTextField(
                            value = uiState.vehicleHeight,
                            onValueChange = { onEvent(CloneIndentEvent.UpdateField(vehicleHeight = it)) },
                            label = { Text("Height (ft)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                        )
                        OutlinedTextField(
                            value = uiState.flooringType,
                            onValueChange = { onEvent(CloneIndentEvent.UpdateField(flooringType = it)) },
                            label = { Text("Flooring") },
                            modifier = Modifier.weight(1f),
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = uiState.tarpaulinRequired,
                                onCheckedChange = { onEvent(CloneIndentEvent.UpdateField(tarpaulinRequired = it)) },
                            )
                            Text("Tarpaulin")
                        }
                        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = uiState.hideCost,
                                onCheckedChange = { onEvent(CloneIndentEvent.UpdateField(hideCost = it)) },
                            )
                            Text("Hide Cost")
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = uiState.tatRequired,
                                onCheckedChange = { onEvent(CloneIndentEvent.UpdateField(tatRequired = it)) },
                            )
                            Text("TAT")
                        }
                        OutlinedTextField(
                            value = uiState.tatHours,
                            enabled = uiState.tatRequired,
                            onValueChange = { onEvent(CloneIndentEvent.UpdateField(tatHours = it)) },
                            label = { Text("TAT hours") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        )
                    }

                    OutlinedTextField(
                        value = uiState.remarks,
                        onValueChange = { onEvent(CloneIndentEvent.UpdateField(remarks = it)) },
                        label = { Text("Remarks") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                    )
                }
            }
        }
    }
}
