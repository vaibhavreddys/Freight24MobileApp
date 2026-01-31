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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.freight24.connect.feature.indents.domain.viewmodel.EditIndentEvent
import com.freight24.connect.feature.indents.domain.viewmodel.EditIndentUiState
import com.freight24.connect.feature.indents.domain.viewmodel.EditIndentViewModel
import com.freight24.connect.feature.indents.domain.viewmodel.EditIndentViewModelImpl

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditIndentScreen(
    indentId: String,
    onBackClick: () -> Unit,
    onSuccess: () -> Unit,
    viewModel: EditIndentViewModel = hiltViewModel<EditIndentViewModelImpl>()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showConfirmDialog by remember { mutableStateOf(false) }

    LaunchedEffect(indentId) {
        viewModel.onEvent(EditIndentEvent.LoadIndent(indentId))
    }

    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            onSuccess()
            viewModel.onEvent(EditIndentEvent.ClearSuccess)
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onEvent(EditIndentEvent.ClearError)
        }
    }

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Confirm Changes") },
            text = { Text("Are you sure you want to save these changes? Please review the details once more before confirming.") },
            confirmButton = {
                TextButton(onClick = {
                    showConfirmDialog = false
                    viewModel.onEvent(EditIndentEvent.SubmitUpdate)
                }) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Indent") },
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
                onClick = { showConfirmDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                enabled = !uiState.isSubmitting && uiState.indent != null
            ) {
                if (uiState.isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp).padding(end = 8.dp),
                        color = Color.White,
                        strokeWidth = 2.dp,
                    )
                }
                Text("Update")
            }
        },
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (uiState.indent != null) {
            EditIndentContent(
                paddingValues = paddingValues,
                uiState = uiState,
                onEvent = viewModel::onEvent
            )
        }
    }
}

@Composable
fun EditIndentContent(
    paddingValues: PaddingValues,
    uiState: EditIndentUiState,
    onEvent: (EditIndentEvent) -> Unit,
) {
    val indent = uiState.indent!!
    val groupDetails = indent.indentGroupDetails
    val loadingLocation = groupDetails.indentLocations.find { it.locationType == "loading" }
    val unloadingLocation = groupDetails.indentLocations.find { it.locationType == "unloading" }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
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
                    "Mandatory Details (Read-only)",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                OutlinedTextField(
                    value = groupDetails.clientProfile?.companyName ?: groupDetails.clientProfile?.fullName ?: "Unknown",
                    onValueChange = {},
                    label = { Text("Client Name") },
                    readOnly = true,
                    enabled = false,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedTextField(
                        value = loadingLocation?.areaName ?: loadingLocation?.cityName ?: "Unknown",
                        onValueChange = {},
                        label = { Text("Loading") },
                        readOnly = true,
                        enabled = false,
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = unloadingLocation?.areaName ?: unloadingLocation?.cityName ?: "Unknown",
                        onValueChange = {},
                        label = { Text("Unloading") },
                        readOnly = true,
                        enabled = false,
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = groupDetails.truckType?.displayName ?: "Unknown",
                    onValueChange = {},
                    label = { Text("Vehicle Type") },
                    readOnly = true,
                    enabled = false,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedTextField(
                        value = "${groupDetails.loadWeight} MT",
                        onValueChange = {},
                        label = { Text("Weight") },
                        readOnly = true,
                        enabled = false,
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = groupDetails.material,
                        onValueChange = {},
                        label = { Text("Material") },
                        readOnly = true,
                        enabled = false,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Editable Optional Fields Section
        EditOptionalFieldsSection(uiState = uiState, onEvent = onEvent)
    }
}

@Composable
fun EditOptionalFieldsSection(
    uiState: EditIndentUiState,
    onEvent: (EditIndentEvent) -> Unit,
) {
    var expanded by remember { mutableStateOf(true) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier
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
                    OutlinedTextField(
                        value = uiState.clientCost,
                        onValueChange = { onEvent(EditIndentEvent.UpdateField(clientCost = it)) },
                        label = { Text("Client Cost (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        OutlinedTextField(
                            value = uiState.negotiatedCost,
                            onValueChange = { onEvent(EditIndentEvent.UpdateField(negotiatedCost = it)) },
                            label = { Text("Negotiated Cost (₹)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                        )
                        OutlinedTextField(
                            value = uiState.businessProposedCost,
                            onValueChange = { onEvent(EditIndentEvent.UpdateField(businessProposedCost = it)) },
                            label = { Text("Proposed Cost (₹)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        OutlinedTextField(
                            value = uiState.vehicleHeight,
                            onValueChange = { onEvent(EditIndentEvent.UpdateField(vehicleHeight = it)) },
                            label = { Text("Height (ft)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                        )
                        OutlinedTextField(
                            value = uiState.flooringType,
                            onValueChange = { onEvent(EditIndentEvent.UpdateField(flooringType = it)) },
                            label = { Text("Flooring") },
                            modifier = Modifier.weight(1f),
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = uiState.tarpaulinRequired,
                                onCheckedChange = { onEvent(EditIndentEvent.UpdateField(tarpaulinRequired = it)) }
                            )
                            Text("Tarpaulin")
                        }
                        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = uiState.hideCost,
                                onCheckedChange = { onEvent(EditIndentEvent.UpdateField(hideCost = it)) }
                            )
                            Text("Hide Cost")
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = uiState.tatRequired,
                                onCheckedChange = { onEvent(EditIndentEvent.UpdateField(tatRequired = it)) }
                            )
                            Text("TAT")
                        }
                        OutlinedTextField(
                            value = uiState.tatHours,
                            enabled = uiState.tatRequired,
                            onValueChange = { onEvent(EditIndentEvent.UpdateField(tatHours = it)) },
                            label = { Text("TAT hours") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        )
                    }

                    OutlinedTextField(
                        value = uiState.remarks,
                        onValueChange = { onEvent(EditIndentEvent.UpdateField(remarks = it)) },
                        label = { Text("Remarks") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                    )
                }
            }
        }
    }
}
