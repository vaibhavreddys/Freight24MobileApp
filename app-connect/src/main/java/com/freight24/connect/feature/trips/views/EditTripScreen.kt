package com.freight24.connect.feature.trips.views

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
            TopAppBar(
                title = { Text("Edit Trip", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            Surface(
                color = Color.White,
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
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22C55E)), // Green as requested
                        enabled = !uiState.isSaving && uiState.hasChanges
                    ) {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
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
        containerColor = Color(0xFFF8FAFC)
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
                        colors = CardDefaults.cardColors(containerColor = Color.White),
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
                                color = Color(0xFF64748B)
                            )

                            Text(
                                " ${uiState.tripCode}",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.End,
                                color = Color(0xFF1E293B),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // Truck Selection Card
                    val isTruckUpdated = uiState.truckNumber != uiState.initialTruckNumber
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = if (isTruckUpdated) androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF22C55E)) else null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val restrictedStatuses = listOf("UNLOADED", "INVOICED", "POD_DISPATCHED", "PAID", "CLOSED")
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
                                color = if (isTruckUpdated) Color(0xFFDCFCE7) else Color(0xFFF1F5F9),
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.LocalShipping,
                                        contentDescription = null,
                                        tint = if (isTruckUpdated) Color(0xFF22C55E) else Color(0xFF64748B),
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
                                        color = Color(0xFF94A3B8)
                                    )
                                    if (isTruckUpdated) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Surface(
                                            color = Color(0xFFF0FDF4),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                "Updated",
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color(0xFF22C55E),
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                                Text(
                                    uiState.truckNumber,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isTruckUpdated) Color(0xFF22C55E) else Color(0xFF1E293B)
                                )
                                if (isTruckUpdated) {
                                    Text(
                                        uiState.initialTruckNumber,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                                        ),
                                        color = Color(0xFF94A3B8)
                                    )
                                }
                            }
                            Icon(
                                Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = Color(0xFF94A3B8)
                            )
                        }
                    }

                    // Basic Details Section
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Text(
                                "Driver Details",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E293B)
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
                        colors = CardDefaults.cardColors(containerColor = Color.White),
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
                                    color = Color(0xFF1E293B)
                                )
                                Icon(
                                    if (isOptionalExpanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                                    contentDescription = null,
                                    tint = Color(0xFF64748B)
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
                                                checkedColor = Color(
                                                    0xFF22C55E
                                                )
                                            ) // Green as requested
                                        )
                                        Text(
                                            "Loading & Unloading Charges Included",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = Color(0xFF475569)
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
            color = Color(0xFF64748B)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            leadingIcon = {
                Icon(leadingIcon, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(20.dp))
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFFE2E8F0),
                unfocusedBorderColor = Color(0xFFE2E8F0),
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            ),
            singleLine = singleLine
        )
    }
}
