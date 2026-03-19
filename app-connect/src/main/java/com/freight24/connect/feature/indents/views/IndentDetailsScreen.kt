package com.freight24.connect.feature.indents.views

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.foundation.background
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.freight24.connect.feature.indents.domain.viewmodel.IndentDetailsEvent
import com.freight24.connect.feature.indents.domain.viewmodel.IndentDetailsViewModel
import com.freight24.connect.feature.indents.domain.viewmodel.IndentDetailsViewModelImpl

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IndentDetailsScreen(
    navController: NavController,
    indentId: String,
    onBackClick: () -> Unit,
    onEditClick: (String) -> Unit,
    onCreateTripClick: (String) -> Unit,
    viewModel: IndentDetailsViewModel = hiltViewModel<IndentDetailsViewModelImpl>()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val refreshTrigger = navController.currentBackStackEntry
        ?.savedStateHandle
        ?.getStateFlow("refresh", false)
        ?.collectAsStateWithLifecycle()

    LaunchedEffect(indentId, refreshTrigger) {
        if (refreshTrigger?.value == true) {
            viewModel.onEvent(IndentDetailsEvent.Refresh)
            navController.currentBackStackEntry?.savedStateHandle?.set("refresh", false)
        } else {
            viewModel.onEvent(IndentDetailsEvent.LoadIndent(indentId))
        }
    }

    if (uiState.isAssignDialogVisible) {
        AlertDialog(
            onDismissRequest = { viewModel.onEvent(IndentDetailsEvent.HideAssignDialog) },
            title = { Text("Confirm Assignment") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = uiState.assignClientCost,
                        onValueChange = { viewModel.onEvent(IndentDetailsEvent.UpdateAssignField(clientCost = it)) },
                        label = { Text("Client Cost") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = uiState.isNegotiatedSameAsClient,
                            onCheckedChange = { viewModel.onEvent(IndentDetailsEvent.UpdateAssignField(isNegotiatedSameAsClient = it)) }
                        )
                        Text("Negotiated cost same as client cost")
                    }
                    OutlinedTextField(
                        value = uiState.assignNegotiatedCost,
                        onValueChange = { viewModel.onEvent(IndentDetailsEvent.UpdateAssignField(negotiatedCost = it)) },
                        label = { Text("Negotiated Cost") },
                        enabled = !uiState.isNegotiatedSameAsClient,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = uiState.assignNotes,
                        onValueChange = { viewModel.onEvent(IndentDetailsEvent.UpdateAssignField(notes = it)) },
                        label = { Text("Notes") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.onEvent(IndentDetailsEvent.ConfirmAssignment) },
                    enabled = !uiState.isAssigning
                ) {
                    if (uiState.isAssigning) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp).padding(end = 8.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    }
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onEvent(IndentDetailsEvent.HideAssignDialog) }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (uiState.isCancelDialogVisible) {
        AlertDialog(
            onDismissRequest = { viewModel.onEvent(IndentDetailsEvent.HideCancelDialog) },
            title = { Text("Cancel Indent") },
            text = { Text("Are you sure you want to cancel this indent?") },
            confirmButton = {
                Button(
                    onClick = { viewModel.onEvent(IndentDetailsEvent.ConfirmCancel) },
                    enabled = !uiState.isCanceling,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE11D48))
                ) {
                    if (uiState.isCanceling) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp).padding(end = 8.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    }
                    Text("Yes, Cancel")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onEvent(IndentDetailsEvent.HideCancelDialog) }) {
                    Text("No")
                }
            }
        )
    }

    var menuExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            // Custom Top Bar with Actions Menu
            TopAppBar(
                title = { Text("Indent Details") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.Black
                        )
                    }
                },
                actions = {
                    uiState.indent?.let { indent ->
                        if (
                            indent.status.equals("open", true)
                            || indent.status.equals("READY_FOR_ASSIGNMENT", true)
                        ) {
                            IconButton(onClick = { menuExpanded = true }) {
                                Icon(Icons.Default.MoreVert, contentDescription = "More options", tint = Color.Black)
                            }
                            DropdownMenu(
                                expanded = menuExpanded,
                                onDismissRequest = { menuExpanded = false },
                                modifier = Modifier.background(Color.White)
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Cancel Indent", color = Color(0xFFE11D48)) },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Outlined.Delete,
                                            contentDescription = "Cancel Indent",
                                            tint = Color(0xFFE11D48)
                                        )
                                    },
                                    onClick = {
                                        menuExpanded = false
                                        viewModel.onEvent(IndentDetailsEvent.ShowCancelDialog)
                                    }
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            uiState.indent?.let { indent ->
                Box(
                    modifier = Modifier.fillMaxWidth().padding(16.dp)
                ) {
                    if (indent.status.equals("open", true)) {
                        Button(
                            onClick = { viewModel.onEvent(IndentDetailsEvent.ShowAssignDialog) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                        ) {
                            Text("Ready for Assignment", color = Color.White)
                        }
                    } else if (indent.status.equals("READY_FOR_ASSIGNMENT", true)) {
                        Button(
                            onClick = { onCreateTripClick(indentId) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                        ) {
                            Text("Create trip", color = Color.White)
                        }
                    } else if (!indent.status.equals("CANCELLED", true)) {
                        Button(
                            onClick = {},
                            modifier = Modifier.fillMaxWidth(),
                            enabled = false
                        ) {
                            Text(indent.status.replace("_", " "))
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                uiState.errorMessage != null -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center).padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = uiState.errorMessage ?: "Unknown Error", color = Color.Red)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { viewModel.onEvent(IndentDetailsEvent.Refresh) }) {
                            Text("Retry")
                        }
                    }
                }
                uiState.indent != null -> {
                    val indent = uiState.indent!!
                    val groupDetails = indent.indentGroupDetails
                    val loadingLocation = groupDetails.indentLocations.find { it.locationType == "loading" }
                    val unloadingLocation = groupDetails.indentLocations.find { it.locationType == "unloading" }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        GeneralInfoSection(
                            clientName = groupDetails.clientProfile?.companyName ?: groupDetails.clientProfile?.fullName ?: "Unknown",
                            tagCode = groupDetails.clientProfile?.tagCode.orEmpty(), // tagCode not available in IndentResponse
                            indentId = indent.indentCode,
                            onEditClick = null
                        )

                        RouteDetailsSection(
                            originCity = loadingLocation?.let {
                                "${it.areaName ?: it.cityName}${it.address?.let { addr -> ", $addr" } ?: ""}"
                            } ?: "Unknown",
                            destinationCity = unloadingLocation?.let {
                                "${it.areaName ?: it.cityName}${it.address?.let { addr -> ", $addr" } ?: ""}"
                            } ?: "Unknown",
                            onEditClick = null
                        )

                        FinancialDetailsSection(
                            clientCost = formatCurrencyDetails(groupDetails.clientTripCost),
                            negotiatedCost = formatCurrencyDetails(indent.negotiatedTripCost),
                            hideCost = indent.hideCostInfo,
                            onEditClick = {
                                onEditClick(indentId)
                            }
                        )

                        SpecificationsSection(
                            weight = groupDetails.loadWeight.toString(),
                            materialType = groupDetails.material,
                            truckType = groupDetails.truckType?.displayName,
                            tatRequired = groupDetails.tatEnabled,
                            tatHours = groupDetails.tatHours?.toString() ?: "",
                            tarpaulinRequired = groupDetails.tarpaulingRequired,
                            onEditClick = {
                                onEditClick(indentId)
                            }
                        )

                        RemarksSection(
                            remarks = groupDetails.specialRequirements ?: "",
                            onEditClick = null
                        )
                    }
                }
            }
        }
    }
}

private fun formatCurrencyDetails(amount: Double?): String {
    if (amount == null) return "0"
    return "%,.0f".format(amount)
}
