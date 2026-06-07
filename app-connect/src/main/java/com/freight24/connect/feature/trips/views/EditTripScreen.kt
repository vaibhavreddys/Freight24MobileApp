package com.freight24.connect.feature.trips.views
import com.freight24.ui.components.FreightTopAppBar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Notes
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.CurrencyRupee
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.freight24.connect.feature.trips.domain.viewmodel.EditTripEvent
import com.freight24.connect.feature.trips.domain.viewmodel.EditTripViewModel
import com.freight24.connect.feature.trips.domain.viewmodel.EditTripViewModelImpl
import com.freight24.ui.theme.FreightThemeTokens
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTripScreen(
    navController: androidx.navigation.NavController,
    backStackEntry: androidx.navigation.NavBackStackEntry, // Added for robust savedStateHandle access
    tripId: String,
    onBackClick: () -> Unit,
    onNavigateToTruckSearch: (String) -> Unit,
    onSuccess: () -> Unit,
    viewModel: EditTripViewModel = hiltViewModel<EditTripViewModelImpl>()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val colorScheme = MaterialTheme.colorScheme
    val extendedColors = FreightThemeTokens.colors
    var isOptionalExpanded by remember { mutableStateOf(true) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Observe truck selection result from AssignTruckScreen using the specific backStackEntry
    val selectedTruckId = backStackEntry.savedStateHandle
        .getStateFlow<String?>("selected_truck_id", null)
        .collectAsStateWithLifecycle()

    val selectedTruckNumber = backStackEntry.savedStateHandle
        .getStateFlow<String?>("selected_truck_number", null)
        .collectAsStateWithLifecycle()

    LaunchedEffect(selectedTruckId.value, selectedTruckNumber.value) {
        if (selectedTruckId.value != null && selectedTruckNumber.value != null) {
            viewModel.onEvent(EditTripEvent.OnTruckSelected(selectedTruckId.value!!, selectedTruckNumber.value!!))
            // Clear results from the specific backStackEntry
            backStackEntry.savedStateHandle.remove<String>("selected_truck_id")
            backStackEntry.savedStateHandle.remove<String>("selected_truck_number")
        }
    }

    LaunchedEffect(tripId) {
        viewModel.onEvent(EditTripEvent.LoadTripDetails(tripId))
    }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onSuccess()
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onEvent(EditTripEvent.ClearError)
        }
    }

    Scaffold(
        topBar = {
            FreightTopAppBar(
                title = { Text("Edit Trip", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            Surface(
                color = colorScheme.surface,
                tonalElevation = 8.dp,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(modifier = Modifier.padding(16.dp)) {
                    Button(
                        onClick = { viewModel.onEvent(EditTripEvent.SaveChanges) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = extendedColors.success),
                        enabled = !uiState.isSaving && uiState.hasChanges
                    ) {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = colorScheme.onPrimary)
                        } else {
                            Text(
                                "Save Changes",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        },
        containerColor = colorScheme.background
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Trip ID Card
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            Text(
                                "Trip ID",
                                style = MaterialTheme.typography.bodyMedium,
                                color = colorScheme.onSurfaceVariant
                            )

                            Text(
                                " ${uiState.tripCode}",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.End,
                                color = colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // Truck Selection Card
                    val isTruckUpdated = uiState.truckNumber != uiState.initialTruckNumber
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
                        border = if (isTruckUpdated) androidx.compose.foundation.BorderStroke(1.dp, extendedColors.success) else null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val restrictedStatuses = listOf("IN_TRANSIT", "UNLOADED", "INVOICED", "POD_DISPATCHED", "PAID", "CLOSED")
                                if (uiState.truckerStatus in restrictedStatuses) {
                                    scope.launch {
                                        snackbarHostState.showSnackbar(
                                            "Truck updates are restricted once the trip has reached the '${uiState.truckerStatus.replace("_", " ")}' stage."
                                        )
                                    }
                                } else {
                                    onNavigateToTruckSearch(uiState.internalTripId)
                                }
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isTruckUpdated) extendedColors.success.copy(alpha = 0.12f) else colorScheme.surfaceVariant,
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.LocalShipping,
                                        contentDescription = null,
                                        tint = if (isTruckUpdated) extendedColors.success else colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        "Truck Number",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = colorScheme.onSurfaceVariant
                                    )
                                    if (isTruckUpdated) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Surface(
                                            color = extendedColors.success.copy(alpha = 0.12f),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                "Updated",
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = extendedColors.success,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                                Text(
                                    uiState.truckNumber,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isTruckUpdated) extendedColors.success else colorScheme.onSurface
                                )
                                if (isTruckUpdated) {
                                    Text(
                                        uiState.initialTruckNumber,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                                        ),
                                        color = colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Icon(
                                Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Basic Details Section
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Text(
                                "Driver Details",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = colorScheme.onSurface
                            )

                            DetailInputField(
                                label = "Name",
                                value = uiState.driverName,
                                onValueChange = { viewModel.onEvent(EditTripEvent.UpdateDriverName(it)) },
                                leadingIcon = Icons.Outlined.Person
                            )

                            DetailInputField(
                                label = "Phone",
                                value = uiState.driverPhone,
                                onValueChange = { viewModel.onEvent(EditTripEvent.UpdateDriverPhone(it)) },
                                leadingIcon = Icons.Outlined.Call
                            )
                        }
                    }

                    // Optional Details Section
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth()
                                    .clickable { isOptionalExpanded = !isOptionalExpanded },
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Cost Details",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = colorScheme.onSurface
                                )
                                Icon(
                                    if (isOptionalExpanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                                    contentDescription = null,
                                    tint = colorScheme.onSurfaceVariant
                                )
                            }

                            if (isOptionalExpanded) {
                                Spacer(modifier = Modifier.height(16.dp))
                                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                    DetailInputField(
                                        label = "Client Cost (₹)",
                                        value = uiState.clientTripCost,
                                        onValueChange = {
                                            viewModel.onEvent(
                                                EditTripEvent.UpdateClientCost(
                                                    it
                                                )
                                            )
                                        },
                                        leadingIcon = Icons.Outlined.CurrencyRupee
                                    )

                                    DetailInputField(
                                        label = "Trucker Cost (₹)",
                                        value = uiState.truckerTripCost,
                                        onValueChange = {
                                            viewModel.onEvent(
                                                EditTripEvent.UpdateTruckerCost(
                                                    it
                                                )
                                            )
                                        },
                                        leadingIcon = Icons.Outlined.CurrencyRupee
                                    )

                                    DetailInputField(
                                        label = "Notes",
                                        value = uiState.notes,
                                        onValueChange = { viewModel.onEvent(EditTripEvent.UpdateNotes(it)) },
                                        leadingIcon = Icons.AutoMirrored.Outlined.Notes,
                                        modifier = Modifier.height(150.dp),
                                        singleLine = false
                                    )

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth().clickable {
                                            viewModel.onEvent(EditTripEvent.ToggleLuIncluded)
                                        }
                                    ) {
                                        Checkbox(
                                            checked = uiState.luIncluded,
                                            onCheckedChange = { viewModel.onEvent(EditTripEvent.ToggleLuIncluded) },
                                            colors = CheckboxDefaults.colors(
                                                checkedColor = extendedColors.success
                                            )
                                        )
                                        Text(
                                            "Loading & Unloading Charges Included",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DetailInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = colorScheme.onSurfaceVariant
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            leadingIcon = {
                Icon(leadingIcon, contentDescription = null, tint = colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colorScheme.outlineVariant,
                unfocusedBorderColor = colorScheme.outlineVariant,
                focusedContainerColor = colorScheme.surface,
                unfocusedContainerColor = colorScheme.surface
            ),
            singleLine = singleLine
        )
    }
}
