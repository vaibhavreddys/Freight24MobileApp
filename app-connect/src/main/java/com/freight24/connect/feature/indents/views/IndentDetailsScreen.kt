package com.freight24.connect.feature.indents.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.freight24.connect.feature.indents.domain.viewmodel.IndentDetailsEvent
import com.freight24.connect.feature.indents.domain.viewmodel.IndentDetailsViewModel
import com.freight24.connect.feature.indents.domain.viewmodel.IndentDetailsViewModelImpl
import com.freight24.common.util.formatIndianCurrency
import com.freight24.common.util.negotiatedCostExceedsClientError
import com.freight24.ui.components.FreightTopAppBar
import com.freight24.ui.theme.FreightThemeTokens
import kotlinx.coroutines.flow.first

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IndentDetailsScreen(
    navController: NavController,
    indentId: String,
    openInterests: Boolean = false,
    onBackClick: () -> Unit,
    onEditClick: (String) -> Unit,
    onCreateTripClick: (String) -> Unit,
    viewModel: IndentDetailsViewModel = hiltViewModel<IndentDetailsViewModelImpl>()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val colorScheme = MaterialTheme.colorScheme
    val extended = FreightThemeTokens.colors
    val snackbarHostState = remember { SnackbarHostState() }

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

    var hasAutoOpenedSheet by remember { mutableStateOf(false) }
    LaunchedEffect(openInterests, indentId) {
        if (openInterests && !hasAutoOpenedSheet) {
            viewModel.uiState.first { it.indent != null && !it.isLoading }
            hasAutoOpenedSheet = true
            viewModel.onEvent(IndentDetailsEvent.ShowTruckerSheet)
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
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
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
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
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
            TruckerInterestSheetContent(
                truckerList = uiState.truckerList,
                isLoading = uiState.isTruckerListLoading,
                onClose = { viewModel.onEvent(IndentDetailsEvent.HideTruckerSheet) },
                onReject = { interestId ->
                    viewModel.onEvent(IndentDetailsEvent.RejectTruckerInterest(interestId))
                }
            )
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
                SuitsableTrucksContent(trucks = uiState.suitableTrucksList)
            }
        }
    }

    val indent = uiState.indent
    val indentCode = indent?.indentCode?.let {
        if (it.startsWith("#")) it else "#$it"
    } ?: ""

    Scaffold(
        topBar = {
            FreightTopAppBar(
                title = {
                    Column {
                        Text(
                            text = indentCode,
                            style = MaterialTheme.typography.titleMedium,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.SemiBold,
                            color = colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        indent?.createdAt?.let { createdAt ->
                            Text(
                                text = "Created ${formatCreatedAt(createdAt)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                },
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
                    indent?.status?.let { status ->
                        IndentStatusChip(status)
                    }
                    if (indent != null && indent.status.equals("open", true)) {
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
                },
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            indent?.let {
                if (it.status.equals("open", true) || it.status.equals("READY_FOR_ASSIGNMENT", true)) {
                    BottomActionBar(
                        status = it.status,
                        onAssignClick = { viewModel.onEvent(IndentDetailsEvent.ShowAssignDialog) },
                        onCreateTripClick = {
                            val clientMissing = it.indentGroupDetails.clientTripCost?.let { cost -> cost <= 0 } ?: true
                            val negotiatedMissing = it.negotiatedTripCost?.let { cost -> cost <= 0 } ?: true
                            if (clientMissing || negotiatedMissing) {
                                costError = true
                            } else {
                                costError = false
                                onCreateTripClick(indentId)
                            }
                        },
                        onEditClick = { onEditClick(indentId) },
                    )
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
                    val currentIndent = indent!!
                    val groupDetails = currentIndent.indentGroupDetails
                    val loadingLocation = groupDetails.indentLocations.find { it.locationType == "loading" }
                    val unloadingLocation = groupDetails.indentLocations.find { it.locationType == "unloading" }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (costError) {
                            val missingParts = buildList {
                                val clientMissing = currentIndent.indentGroupDetails.clientTripCost?.let { it <= 0 } ?: true
                                val negotiatedMissing = currentIndent.negotiatedTripCost?.let { it <= 0 } ?: true
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
                            clientPhone = groupDetails.clientProfile?.phoneNumber,
                            tagCode = groupDetails.clientProfile?.tagCode,
                        )

                        currentIndent.metrics?.let { metrics ->
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

                        RouteAndFinancialsSection(
                            originCity = loadingLocation?.let {
                                "${it.areaName ?: it.cityName}${it.address?.let { addr -> ", $addr" } ?: ""}"
                            } ?: "Unknown",
                            destinationCity = unloadingLocation?.let {
                                "${it.areaName ?: it.cityName}${it.address?.let { addr -> ", $addr" } ?: ""}"
                            } ?: "Unknown",
                            clientCost = formatCurrencyDetails(groupDetails.clientTripCost),
                            negotiatedCost = formatCurrencyDetails(currentIndent.negotiatedTripCost),
                            hideCost = currentIndent.hideCostInfo,
                            isCostVisible = !currentIndent.hideCostInfo,
                            hasError = costError,
                        )

                        SpecificationsSection(
                            weight = groupDetails.loadWeight.toString(),
                            weightUnit = "MT",
                            materialType = groupDetails.material,
                            truckType = groupDetails.truckType?.displayName,
                            truckTypeUnit = groupDetails.truckType?.bodyType ?: "",
                            tatRequired = groupDetails.tatEnabled,
                            tatHours = groupDetails.tatHours?.toString() ?: "",
                            tarpaulinRequired = groupDetails.tarpaulingRequired,
                        )

                        RemarksSection(
                            remarks = groupDetails.specialRequirements ?: "",
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BottomActionBar(
    status: String,
    onAssignClick: () -> Unit,
    onCreateTripClick: () -> Unit,
    onEditClick: () -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme

    Surface(
        color = colorScheme.surface,
        shadowElevation = 8.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(
                onClick = onEditClick,
                modifier = Modifier
                    .size(48.dp)
                    .background(colorScheme.surfaceVariant, CircleShape)
                    .semantics { contentDescription = "Edit indent" }
            ) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = null,
                    tint = colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp),
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            Button(
                onClick = {
                    if (status.equals("open", true)) onAssignClick() else onCreateTripClick()
                },
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorScheme.primary,
                    contentColor = colorScheme.onPrimary
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp),
            ) {
                Text(
                    text = if (status.equals("open", true)) "Mark Ready for Assignment" else "Create Trip",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun TruckerInterestSheetContent(
    truckerList: List<com.freight24.rest.response.TruckerInboxInterestResponse.TruckerInboxInterestInfo>,
    isLoading: Boolean,
    onClose: () -> Unit,
    onReject: (String) -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 32.dp)
    ) {
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
                val count = truckerList.size
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
                onClick = onClose,
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

        if (isLoading) {
            Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (truckerList.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                Text("No interests found", color = colorScheme.onSurfaceVariant)
            }
        } else {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                truckerList.forEach { trucker ->
                    com.freight24.connect.feature.inbox.views.TruckerCallRow(
                        name = trucker.name,
                        phone = trucker.phone ?: "",
                        expressedAt = trucker.expressedAt,
                        onReject = { onReject(trucker.interestId) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SuitsableTrucksContent(
    trucks: com.freight24.rest.response.SearchTruckerResponse.TruckerData?
) {
    if (trucks == null) {
        Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
            Text("No suitable trucks found", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    } else {
        com.freight24.connect.feature.findTrucks.views.SearchResultsContent(trucks)
    }
}

private fun formatCurrencyDetails(amount: Double?): String {
    if (amount == null) return "0"
    return "%,.0f".format(amount)
}

private fun formatCreatedAt(isoTimestamp: String): String {
    return try {
        val digits = isoTimestamp.replace(Regex("[^0-9]"), "")
        if (digits.length >= 14) {
            val year = digits.substring(0, 4)
            val month = digits.substring(4, 6).toIntOrNull() ?: 1
            val day = digits.substring(6, 8).toIntOrNull() ?: 1
            val hour = digits.substring(8, 10).toIntOrNull() ?: 0
            val minute = digits.substring(10, 12).toIntOrNull() ?: 0
            val amPm = if (hour < 12) "AM" else "PM"
            val displayHour = when {
                hour == 0 -> 12
                hour > 12 -> hour - 12
                else -> hour
            }
            val monthNames = arrayOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
            "${monthNames[month - 1]} $day, $year at $displayHour:${"%02d".format(minute)} $amPm"
        } else {
            isoTimestamp
        }
    } catch (e: Exception) {
        isoTimestamp
    }
}
