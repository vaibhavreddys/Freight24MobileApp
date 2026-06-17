package com.freight24.connect.feature.indents.views
import com.freight24.ui.components.FreightTopAppBar

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
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.freight24.connect.feature.indents.domain.viewmodel.EditIndentEvent
import com.freight24.connect.feature.indents.domain.viewmodel.EditIndentUiState
import com.freight24.connect.feature.indents.domain.viewmodel.EditIndentViewModel
import com.freight24.connect.feature.indents.domain.viewmodel.EditIndentViewModelImpl
import kotlinx.coroutines.launch

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
            launch {
                snackbarHostState.showSnackbar(it)
            }
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

    EditIndentContent(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onBackClick = onBackClick,
        onEvent = viewModel::onEvent,
        onSubmitClick = { showConfirmDialog = true },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditIndentContent(
    uiState: EditIndentUiState,
    snackbarHostState: SnackbarHostState,
    onBackClick: () -> Unit,
    onEvent: (EditIndentEvent) -> Unit,
    onSubmitClick: () -> Unit,
) {
    val isDark = isSystemInDarkTheme()

    // Solid M3 background — replaces the legacy slate gradient. M3 prefers
    // tonal hierarchy over visible page-level gradients.
    val screenBackground = MaterialTheme.colorScheme.background

    Box(Modifier.fillMaxSize()) {
        Box(Modifier.fillMaxSize().background(screenBackground))

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
                FreightTopAppBar(
                    title = { Text("Edit Indent") },
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
                        onClick = onSubmitClick,
                        enabled = !uiState.isSubmitting && uiState.indent != null,
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
                        if (uiState.isSubmitting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp,
                            )
                            Spacer(Modifier.width(8.dp))
                        }
                        Text(
                            "Update",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        if (!uiState.isSubmitting) {
                            Spacer(Modifier.width(8.dp))
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }
                }
            },
        ) { paddingValues ->
            if (uiState.isLoading) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else if (uiState.indent != null) {
                EditFormContent(
                    uiState = uiState,
                    onEvent = onEvent,
                    paddingValues = paddingValues,
                )
            }
        }
    }
}

@Composable
private fun EditFormContent(
    uiState: EditIndentUiState,
    onEvent: (EditIndentEvent) -> Unit,
    paddingValues: PaddingValues,
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
            .padding(horizontal = 20.dp)
            .padding(top = 16.dp)
            .padding(bottom = 8.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        MandatoryDetailsSection(
            clientName = groupDetails.clientProfile?.companyName
                ?: groupDetails.clientProfile?.fullName ?: "Unknown",
            loadingLocation = loadingLocation?.areaName
                ?: loadingLocation?.cityName ?: "Unknown",
            unloadingLocation = unloadingLocation?.areaName
                ?: unloadingLocation?.cityName ?: "Unknown",
            vehicleType = groupDetails.truckType?.displayName ?: "Unknown",
            weight = "${groupDetails.loadWeight} MT",
            material = groupDetails.material,
        )

        EditOptionalDetailsSection(
            uiState = uiState,
            onEvent = onEvent,
        )

        Spacer(Modifier.height(24.dp))
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
private fun MandatoryDetailsSection(
    clientName: String,
    loadingLocation: String,
    unloadingLocation: String,
    vehicleType: String,
    weight: String,
    material: String,
) {
    SectionCard {
        SectionHeaderText("INDENT DETAILS (Read-Only)")

        ReadOnlyField(value = clientName, label = "Client Name")

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ReadOnlyField(
                value = loadingLocation,
                label = "Loading",
                modifier = Modifier.weight(1f),
            )
            ReadOnlyField(
                value = unloadingLocation,
                label = "Unloading",
                modifier = Modifier.weight(1f),
            )
        }

        ReadOnlyField(value = vehicleType, label = "Vehicle Type")

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ReadOnlyField(
                value = weight,
                label = "Weight",
                modifier = Modifier.weight(1f),
            )
            ReadOnlyField(
                value = material,
                label = "Material",
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun ReadOnlyField(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = value,
        onValueChange = {},
        label = { Text(label) },
        readOnly = true,
        enabled = false,
        modifier = modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
            disabledTextColor = MaterialTheme.colorScheme.onSurface,
            disabledBorderColor = MaterialTheme.colorScheme.outlineVariant,
            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            disabledContainerColor = Color.Transparent,
        ),
        shape = RoundedCornerShape(16.dp),
        singleLine = true,
        textStyle = MaterialTheme.typography.bodyMedium,
    )
}

@Composable
private fun EditOptionalDetailsSection(
    uiState: EditIndentUiState,
    onEvent: (EditIndentEvent) -> Unit,
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
                "OPTIONAL DETAILS",
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
                Spacer(Modifier.height(4.dp))
                SectionHeaderText("Cost Breakdown")

                OutlinedTextField(
                    value = uiState.clientCost,
                    onValueChange = { onEvent(EditIndentEvent.UpdateField(clientCost = it)) },
                    label = { Text("Client Cost (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                )

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = uiState.negotiatedCost,
                        onValueChange = { onEvent(EditIndentEvent.UpdateField(negotiatedCost = it)) },
                        label = { Text("Negotiated (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true,
                    )
                    OutlinedTextField(
                        value = uiState.businessProposedCost,
                        onValueChange = { onEvent(EditIndentEvent.UpdateField(businessProposedCost = it)) },
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
                        onValueChange = { onEvent(EditIndentEvent.UpdateField(vehicleHeight = it)) },
                        label = { Text("Height (ft)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true,
                    )
                    OutlinedTextField(
                        value = uiState.flooringType,
                        onValueChange = { onEvent(EditIndentEvent.UpdateField(flooringType = it)) },
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
                                onEvent(EditIndentEvent.UpdateField(tarpaulinRequired = it))
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
                                onEvent(EditIndentEvent.UpdateField(hideCost = it))
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
                                onEvent(EditIndentEvent.UpdateField(tatRequired = it))
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
                        onValueChange = { onEvent(EditIndentEvent.UpdateField(tatHours = it)) },
                        label = { Text("TAT hours") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                    )
                }

                OutlinedTextField(
                    value = uiState.remarks,
                    onValueChange = { onEvent(EditIndentEvent.UpdateField(remarks = it)) },
                    label = { Text("Remarks") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    minLines = 2,
                )
            }
        }
    }
}
