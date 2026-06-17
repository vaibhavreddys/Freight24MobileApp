package com.freight24.connect.feature.indents.views
import com.freight24.ui.components.FreightTopAppBar

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.freight24.connect.feature.inbox.views.TruckerCallRow
import androidx.compose.material.icons.filled.Warning
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import com.freight24.connect.feature.findTrucks.views.SearchResultsContent
import com.freight24.connect.feature.indents.domain.viewmodel.IndentDetailsEvent
import com.freight24.connect.feature.indents.domain.viewmodel.IndentDetailsViewModel
import com.freight24.connect.feature.indents.domain.viewmodel.IndentDetailsViewModelImpl
import com.freight24.common.util.formatIndianCurrency
import com.freight24.common.util.negotiatedCostExceedsClientError

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
    val colorScheme = MaterialTheme.colorScheme
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

    var assignClientCostFieldValue by remember {
        mutableStateOf(TextFieldValue(uiState.assignClientCost, TextRange(uiState.assignClientCost.length)))
    }
    LaunchedEffect(uiState.assignClientCost) {
        if (assignClientCostFieldValue.text != uiState.assignClientCost) {
            assignClientCostFieldValue = TextFieldValue(uiState.assignClientCost, TextRange(uiState.assignClientCost.length))
        }
    }
    var assignNegotiatedCostFieldValue by remember {
        mutableStateOf(
            TextFieldValue(
                uiState.assignNegotiatedCost,
                TextRange(uiState.assignNegotiatedCost.length)
            )
        )
    }
    LaunchedEffect(uiState.assignNegotiatedCost) {
        if (assignNegotiatedCostFieldValue.text != uiState.assignNegotiatedCost) {
            assignNegotiatedCostFieldValue = TextFieldValue(uiState.assignNegotiatedCost, TextRange(uiState.assignNegotiatedCost.length))
        }
    }
    val assignNegotiatedCostError = if (uiState.isNegotiatedSameAsClient) {
        null
    } else {
        negotiatedCostExceedsClientError(uiState.assignNegotiatedCost, uiState.assignClientCost)
    }

    if (uiState.isAssignDialogVisible) {
        AlertDialog(
            onDismissRequest = { viewModel.onEvent(IndentDetailsEvent.HideAssignDialog) },
            title = { Text("Confirm Assignment") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = assignClientCostFieldValue,
                        onValueChange = { newValue ->
                            val rawText = newValue.text.replace(",", "")
                            val formatted = formatIndianCurrency(rawText)

                            val commaDiff = formatted.count { it == ',' } - assignClientCostFieldValue.text.count { it == ',' }
                            val newCursor = (newValue.selection.start + commaDiff).coerceIn(0, formatted.length)

                            assignClientCostFieldValue = TextFieldValue(formatted, TextRange(newCursor))
                            viewModel.onEvent(IndentDetailsEvent.UpdateAssignField(clientCost = formatted))
                        },
                        label = { Text("Client Cost") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
                        value = assignNegotiatedCostFieldValue,
                        onValueChange = { newValue ->
                            val rawText = newValue.text.replace(",", "")
                            val formatted = formatIndianCurrency(rawText)

                            val commaDiff = formatted.count { it == ',' } - assignNegotiatedCostFieldValue.text.count { it == ',' }
                            val newCursor = (newValue.selection.start + commaDiff).coerceIn(0, formatted.length)

                            assignNegotiatedCostFieldValue = TextFieldValue(formatted, TextRange(newCursor))
                            viewModel.onEvent(IndentDetailsEvent.UpdateAssignField(negotiatedCost = formatted))
                        },
                        label = { Text("Negotiated Cost") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        enabled = !uiState.isNegotiatedSameAsClient,
                        isError = assignNegotiatedCostError != null,
                        supportingText = assignNegotiatedCostError?.let { msg -> { Text(msg) } },
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
                    enabled = !uiState.isAssigning && assignNegotiatedCostError == null
                ) {
                    if (uiState.isAssigning) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp).padding(end = 8.dp),
                            color = colorScheme.onPrimary,
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
                        colors = ButtonDefaults.buttonColors(containerColor = colorScheme.error)
                    ) {
                        if (uiState.isCanceling) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp).padding(end = 8.dp),
                                color = colorScheme.onError,
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
    var costError by remember { mutableStateOf(false) }

    if (uiState.isTruckerSheetVisible) {
        val sheetState = rememberModalBottomSheetState()
            ModalBottomSheet(
                onDismissRequest = { viewModel.onEvent(IndentDetailsEvent.HideTruckerSheet) },
                sheetState = sheetState,
                containerColor = colorScheme.surface,
                dragHandle = { BottomSheetDefaults.DragHandle() }
            ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Interest Expressed",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.onSurface
                        )
                        val count = uiState.truckerList.size
                        Text(
                            text = if (count == 1) {
                                "$count trucker has shown interest"
                            } else {
                                "$count truckers have shown interest"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(
                        onClick = { viewModel.onEvent(IndentDetailsEvent.HideTruckerSheet) },
                        modifier = Modifier.background(colorScheme.surfaceVariant, CircleShape).size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Close",
                            modifier = Modifier.size(18.dp),
                            tint = colorScheme.onSurfaceVariant
                        )
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), thickness = 0.5.dp, color = colorScheme.outlineVariant)

                if (uiState.isTruckerListLoading) {
                    Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (uiState.truckerList.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        Text("No interests found", color = colorScheme.onSurfaceVariant)
                    }
                } else {
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        uiState.truckerList.forEach { trucker ->
                            TruckerCallRow(
                                name = trucker.name,
                                phone = trucker.phone ?: "",
                                expressedAt = trucker.expressedAt,
                                onReject = {
                                    viewModel.onEvent(IndentDetailsEvent.RejectTruckerInterest(trucker.interestId))
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    if (uiState.isSuitableTrucksSheetVisible) {
        val sheetState = rememberModalBottomSheetState()
            ModalBottomSheet(
                onDismissRequest = { viewModel.onEvent(IndentDetailsEvent.HideSuitableTrucksSheet) },
                sheetState = sheetState,
            containerColor = colorScheme.surface,
                dragHandle = { BottomSheetDefaults.DragHandle() }
            ) {
            if (uiState.isSuitableTrucksLoading) {
                Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                SearchResultsContent(uiState.suitableTrucksList)
            }
        }
    }

    Scaffold(
        topBar = {
            // Custom Top Bar with Actions Menu
            FreightTopAppBar(
                title = { Text("Indent Details") },
                navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                            tint = colorScheme.onSurface
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
                                Icon(Icons.Default.MoreVert, contentDescription = "More options", tint = colorScheme.onSurface)
                            }
                            DropdownMenu(
                                expanded = menuExpanded,
                                onDismissRequest = { menuExpanded = false },
                                modifier = Modifier.background(colorScheme.surface)
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Cancel Indent", color = colorScheme.error) },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Outlined.Delete,
                                            contentDescription = "Cancel Indent",
                                            tint = colorScheme.error
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
            )
        },
        bottomBar = {
            uiState.indent?.let { indent ->
                Box(
                    modifier = Modifier.fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(bottom = 20.dp, top = 10.dp)
                ) {
                    if (indent.status.equals("open", true)) {
                        Button(
                            onClick = { viewModel.onEvent(IndentDetailsEvent.ShowAssignDialog) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(28.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colorScheme.primary,
                                contentColor = colorScheme.onPrimary
                            ),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp),
                        ) {
                            Text(
                                "Ready for Assignment",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                            )
                            Spacer(Modifier.width(8.dp))
                        }
                    } else if (indent.status.equals("READY_FOR_ASSIGNMENT", true)) {
                        Button(
                            onClick = {
                                val clientMissing = indent.indentGroupDetails.clientTripCost?.let { it <= 0 } ?: true
                                val negotiatedMissing = indent.negotiatedTripCost?.let { it <= 0 } ?: true
                                if (clientMissing || negotiatedMissing) {
                                    costError = true
                                } else {
                                    costError = false
                                    onCreateTripClick(indentId)
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colorScheme.primary,
                                contentColor = colorScheme.onPrimary
                            )
                        ) {
                            Text("Create trip",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
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
                        Text(text = uiState.errorMessage ?: "Unknown Error", color = colorScheme.error)
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
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (costError) {
                            val missingParts = buildList {
                                val clientMissing = indent.indentGroupDetails.clientTripCost?.let { it <= 0 } ?: true
                                val negotiatedMissing = indent.negotiatedTripCost?.let { it <= 0 } ?: true
                                if (clientMissing) add("Client trip cost")
                                if (negotiatedMissing) add("Negotiated trip cost")
                            }
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = colorScheme.errorContainer),
                                shape = RoundedCornerShape(12.dp),
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Icon(
                                        Icons.Filled.Warning,
                                        contentDescription = null,
                                        tint = colorScheme.onErrorContainer,
                                        modifier = Modifier.size(20.dp),
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = "${missingParts.joinToString(" and ")} is required. Set it in Edit Indent.",
                                        color = colorScheme.onErrorContainer,
                                        style = MaterialTheme.typography.bodySmall,
                                    )
                                }
                            }
                        }

                        GeneralInfoSection(
                            clientName = groupDetails.clientProfile?.companyName ?: groupDetails.clientProfile?.fullName ?: "Unknown",
                            tagCode = groupDetails.clientProfile?.tagCode.orEmpty(),
                            indentId = indent.indentCode,
                            onEditClick = null
                        )

                        indent.metrics?.let { metrics ->
                            if (metrics.interestCount > 0 || metrics.suitableTrucks > 0) {
                                IndentMetricsSection(
                                    interestCount = metrics.interestCount,
                                    suitableTrucks = metrics.suitableTrucks,
                                    onInterestClick = {
                                        viewModel.onEvent(IndentDetailsEvent.ShowTruckerSheet)
                                    },
                                    onSuitableTrucksClick = {
                                        viewModel.onEvent(IndentDetailsEvent.ShowSuitableTrucksSheet)
                                    }
                                )
                            }
                        }

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
                            hasError = costError,
                            onEditClick = {
                                costError = false
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
