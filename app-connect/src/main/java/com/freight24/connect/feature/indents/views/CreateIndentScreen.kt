package com.freight24.connect.feature.indents.views

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.res.stringResource
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
import com.freight24.connect.R
import com.freight24.connect.feature.indents.domain.viewmodel.CreateIndentEvent
import com.freight24.connect.feature.indents.domain.viewmodel.CreateIndentUiState
import com.freight24.connect.feature.indents.domain.viewmodel.IndentContactRole
import com.freight24.connect.feature.indents.domain.viewmodel.IndentsViewModel
import com.freight24.connect.feature.indents.domain.viewmodel.IndentsViewModelImpl
import com.freight24.connect.domain.model.ClientPickerOption
import com.freight24.rest.response.LocationDto
import com.freight24.rest.response.TruckCategoryDto
import com.freight24.rest.response.TruckTypeDto
import com.freight24.ui.components.FreightTopAppBar
import com.freight24.ui.components.InlineErrorBanner
import com.freight24.ui.components.MetaTag
import com.freight24.ui.components.PerforationRule
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

// ===========================================================================
// Create Indent — a blank consignment docket.
//
// The page is laid out as six numbered clauses rather than a column of cards,
// and the clause spine is keyed to how far each clause has got:
//
//   grey  · nothing entered yet
//   amber · started, still owes something
//   green · every required input present
//   red   · something is wrong and submission is blocked
//
// Clauses 01–04 gate submission and are tallied in the docket head. Clauses 05
// and 06 are optional, so their spine uses the brand accent when filled rather
// than green — colouring an optional clause green would imply it had been
// required all along.
//
// Client information added by backend commit 1030f9c lives in clause 01: the
// client's branch, and the call sheet of four contact roles. `placement` is
// required by the backend even though it reads as optional data.
// ===========================================================================

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
                    val client = Json.decodeFromString<ClientPickerOption>(it)
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
        onNavigateToReview = onNavigateToReview,
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
    val scope = rememberCoroutineScope()
    val fieldErrors = remember { mutableStateOf(emptySet<String>()) }
    var showBranchSheet by remember { mutableStateOf(false) }
    var showMaterialSheet by remember { mutableStateOf(false) }
    var contactSheetRole by remember { mutableStateOf<IndentContactRole?>(null) }

    fun validate(): Boolean {
        val errors = mutableSetOf<String>()

        if (uiState.selectedClient == null) errors.add("client")
        if (uiState.placementContact == null) errors.add("placement")
        if (uiState.selectedFromLocation == null) errors.add("from")
        if (uiState.selectedToLocation == null) errors.add("to")
        if (uiState.selectedTruckType == null) errors.add("truck")
        if (uiState.weight.isBlank()) errors.add("weight")
        if (uiState.materialName.isBlank()) errors.add("material")
        if (uiState.tatRequired && uiState.tatHours.isBlank()) errors.add("tat")
        if (negotiatedCostExceedsClientError(uiState.negotiatedCost, uiState.clientCost) != null) {
            errors.add("negotiated")
        }

        fieldErrors.value = errors

        if (errors.isNotEmpty()) {
            val message =
                when {
                    errors.contains("client") -> "Clause 01 — select a client"
                    errors.contains("placement") -> "Clause 01 — select a placement contact"
                    errors.contains("from") || errors.contains("to") -> "Clause 02 — enter the route"
                    errors.contains("truck") -> "Clause 03 — select a vehicle type"
                    errors.contains("weight") || errors.contains("material") ->
                        "Clause 04 — enter the cargo details"
                    errors.contains("tat") -> "Clause 06 — enter the TAT hours"
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

    val errors = fieldErrors.value
    val consignorState = consignorClauseState(uiState, errors)
    val laneState = laneClauseState(uiState, errors)
    val vehicleState = vehicleClauseState(uiState, errors)
    val cargoState = cargoClauseState(uiState, errors)
    val commercialsState = commercialsClauseState(uiState, errors)
    val termsState = termsClauseState(uiState, errors)

    // Only the gating clauses are tallied — a tally that counted optional
    // clauses would sit permanently short of full and read as a blocker.
    val gatingStates = listOf(consignorState, laneState, vehicleState, cargoState)
    val gatingComplete = gatingStates.count { it == ClauseState.COMPLETE }

    val clientSelected = uiState.selectedClient != null

    val scrollState = rememberScrollState()

    Box(Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                FreightTopAppBar(
                    title = { Text("Create Indent") },
                    navigationIcon = {
                        IconButton(
                            onClick = onBackClick,
                            modifier = Modifier.docketTag("create_indent_back"),
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                            )
                        }
                    },
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) },
        ) { paddingValues ->
            // The window is edge-to-edge, so the IME is an overlay this layout knows nothing
            // about: the scroll viewport ran the full height of the window, behind the
            // keyboard. Compose's bring-into-view then measured the focused field (Remarks,
            // Cargo Weight, …) against that phantom viewport, decided it was "already
            // visible" and never scrolled — so the field being typed into sat under the
            // keyboard with no way to reach it. imePadding() here, *outside* verticalScroll,
            // shrinks everything below to the part of the screen the user can actually see,
            // so focusing a field scrolls it into view and the CTA rides above the keyboard
            // instead of hiding behind it.
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        // The scaffold's insets are already spent above; without this the IME
                        // inset below would be counted a second time and the CTA would float a
                        // navigation bar's height too high.
                        .consumeWindowInsets(paddingValues)
                        .imePadding(),
            ) {
                Column(Modifier.fillMaxSize()) {
                    Column(
                        modifier =
                            Modifier
                                // weight(1f), not fillMaxSize: the CTA below is a sibling in normal
                                // flow, so the scroll viewport ends where the CTA begins. An
                                // overlapping CTA plus trailing scroll padding is not enough — the
                                // field still scrolls into the strip the button covers, because
                                // bring-into-view only knows the scroll container's own bounds.
                                .weight(1f)
                                .verticalScroll(scrollState)
                                .padding(horizontal = 16.dp)
                                .padding(top = 8.dp, bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        DocketHead(
                            complete = gatingComplete,
                            total = gatingStates.size,
                            states = gatingStates,
                        )

                        ConsignorClause(
                            uiState = uiState,
                            state = consignorState,
                            errors = errors,
                            onEvent = onEvent,
                            onClientClick = {
                                clearError("client")
                                onNavigateToSearch(SearchType.CLIENT.name)
                            },
                            onBranchClick = { showBranchSheet = true },
                            onContactClick = { role ->
                                clearError("placement")
                                contactSheetRole = role
                            },
                        )

                        ClauseDocket(
                            number = 2,
                            title = stringResource(R.string.lane),
                            state = laneState,
                            testTag = "create_indent_clause_lane",
                        ) {
                            LanePicker(
                                originLabel = stringResource(R.string.location_from),
                                originValue = uiState.selectedFromLocation?.name.orEmpty(),
                                onOriginClick = {
                                    clearError("from")
                                    onNavigateToSearch(SearchType.LOCATION_FROM.name)
                                },
                                originTestTag = "create_indent_from_picker",
                                destinationLabel = stringResource(R.string.location_to),
                                destinationValue = uiState.selectedToLocation?.name.orEmpty(),
                                onDestinationClick = {
                                    clearError("to")
                                    onNavigateToSearch(SearchType.LOCATION_TO.name)
                                },
                                destinationTestTag = "create_indent_to_picker",
                                originError = errors.contains("from"),
                                destinationError = errors.contains("to"),
                            )
                        }

                        VehicleClause(
                            truckTypes = uiState.truckTypes,
                            selectedTruckType = uiState.selectedTruckType,
                            isLoading = uiState.isLoadingTruckTypes,
                            isError = errors.contains("truck"),
                            state = vehicleState,
                            onSelect = {
                                clearError("truck")
                                onEvent(CreateIndentEvent.SelectTruckType(it))
                            },
                        )

                        ClauseDocket(
                            number = 4,
                            title = stringResource(R.string.cargo),
                            state = cargoState,
                            testTag = "create_indent_clause_cargo",
                        ) {
                            RuledField(
                                label = stringResource(R.string.weight),
                                value = uiState.weight,
                                onValueChange = {
                                    clearError("weight")
                                    onEvent(CreateIndentEvent.UpdateWeight(it))
                                },
                                testTag = "create_indent_weight_field",
                                placeholder = "0.0",
                                keyboardType = KeyboardType.Decimal,
                                isError = errors.contains("weight"),
                                trailingLabel = stringResource(R.string.weight_mt),
                            )
                            Spacer(Modifier.height(18.dp))
                            DocketPicker(
                                label = stringResource(R.string.material),
                                value = uiState.materialName,
                                onClick = {
                                    clearError("material")
                                    showMaterialSheet = true
                                },
                                testTag = "create_indent_material_field",
                                placeholder = "Choose or type a category",
                                isError = errors.contains("material"),
                            )
                        }

                        CommercialsClause(
                            uiState = uiState,
                            state = commercialsState,
                            negotiatedError =
                                negotiatedCostExceedsClientError(
                                    uiState.negotiatedCost,
                                    uiState.clientCost,
                                ),
                            onEvent = onEvent,
                        )

                        TermsClause(
                            uiState = uiState,
                            state = termsState,
                            tatError = errors.contains("tat"),
                            onEvent = onEvent,
                        )
                    }

                    // Pinned CTA. A sibling in normal flow inside the imePadding() container
                    // rather than Scaffold's bottomBar slot: it rides up with the keyboard
                    // instead of sitting at the window bottom behind it, and it keeps the
                    // scroll viewport's bottom edge honest.
                    Box(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp)
                                .padding(bottom = 20.dp, top = 10.dp),
                    ) {
                        Button(
                            onClick = { if (validate()) onNavigateToReview() },
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .docketTag("create_indent_review_button"),
                            shape = MaterialTheme.shapes.large,
                            colors =
                                ButtonDefaults.buttonColors(
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
                }
            }
        }
    }

    if (showBranchSheet) {
        BranchPickerSheet(
            branches = uiState.branches,
            selectedId = uiState.selectedBranch?.id,
            onSelect = { onEvent(CreateIndentEvent.SelectBranch(it)) },
            onDismiss = { showBranchSheet = false },
        )
    }

    if (showMaterialSheet) {
        MaterialPickerSheet(
            materials = uiState.materials,
            selectedName = uiState.materialName,
            isLoading = uiState.isLoadingMaterials,
            onSelect = { onEvent(CreateIndentEvent.UpdateMaterialName(it)) },
            onDismiss = { showMaterialSheet = false },
        )
    }

    contactSheetRole?.let { role ->
        ContactPickerSheet(
            role = role,
            contacts = uiState.contacts,
            selectedId = uiState.contactFor(role)?.id,
            onSelect = { onEvent(CreateIndentEvent.SelectContact(role, it)) },
            onDismiss = { contactSheetRole = null },
        )
    }
}

// ---------------------------------------------------------------------------
// Docket head
// ---------------------------------------------------------------------------

@Composable
private fun DocketHead(
    complete: Int,
    total: Int,
    states: List<ClauseState>,
) {
    val scheme = MaterialTheme.colorScheme

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "NEW INDENT · DRAFT",
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.5.sp),
                fontWeight = FontWeight.Bold,
                color = scheme.primary,
            )
            Spacer(Modifier.weight(1f))
            MetaTag(
                text = "$complete / $total REQUIRED",
                container =
                    if (complete == total) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        scheme.surfaceContainerHighest
                    },
                content =
                    if (complete == total) {
                        scheme.onPrimaryContainer
                    } else {
                        scheme.onSurfaceVariant
                    },
            )
        }

        Text(
            text = "Consignment note",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = scheme.onSurface,
        )

        Text(
            text =
                "Clauses 01 to 04 have to be complete before this can be raised. " +
                    "The rest is optional.",
            style = MaterialTheme.typography.bodySmall,
            color = scheme.onSurfaceVariant,
        )

        ClauseTally(states = states)
    }
}

// ---------------------------------------------------------------------------
// Clause 01 — consignor
// ---------------------------------------------------------------------------

@Composable
private fun ConsignorClause(
    uiState: CreateIndentUiState,
    state: ClauseState,
    errors: Set<String>,
    onEvent: (CreateIndentEvent) -> Unit,
    onClientClick: () -> Unit,
    onBranchClick: () -> Unit,
    onContactClick: (IndentContactRole) -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val clientSelected = uiState.selectedClient != null

    val clientDisplay =
        uiState.selectedClient?.let { client ->
            listOfNotNull(
                client.companyName.takeIf { it.isNotBlank() },
                client.displayName?.takeIf { it.isNotBlank() },
            ).joinToString(" · ")
        }.orEmpty()

    val branchDisplay =
        uiState.selectedBranch?.let { branch ->
            listOfNotNull(
                branch.branchName?.takeIf { it.isNotBlank() },
                branch.branchCode?.takeIf { it.isNotBlank() },
            ).joinToString(" · ")
        }.orEmpty()

    ClauseDocket(
        number = 1,
        title = stringResource(R.string.consignor),
        state = state,
        testTag = "create_indent_clause_consignor",
        trailing = {
            if (uiState.isLoadingClientDirectory) {
                CircularProgressIndicator(
                    modifier = Modifier.size(14.dp),
                    strokeWidth = 2.dp,
                    color = scheme.primary,
                )
            }
        },
    ) {
        DocketPicker(
            label = stringResource(R.string.client),
            value = clientDisplay,
            onClick = onClientClick,
            testTag = "create_indent_client_picker",
            placeholder = "Search client",
            isError = errors.contains("client"),
        )

        Spacer(Modifier.height(18.dp))

        DocketPicker(
            label = stringResource(R.string.branch),
            value = branchDisplay,
            onClick = onBranchClick,
            testTag = "create_indent_branch_picker",
            placeholder = if (clientSelected) "Optional — none selected" else "Select a client first",
            enabled = clientSelected && !uiState.isLoadingBranches && uiState.branches.isNotEmpty(),
            supportingText =
                when {
                    !clientSelected -> null
                    uiState.isLoadingBranches -> "Loading branches…"
                    uiState.branches.isEmpty() -> "This client has no branches on file."
                    else -> "Optional. Inactive branches cannot be selected."
                },
        )

        Spacer(Modifier.height(18.dp))
        PerforationRule()
        Spacer(Modifier.height(14.dp))

        Text(
            text = "CALL SHEET",
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.4.sp),
            fontWeight = FontWeight.Bold,
            color = scheme.onSurface,
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = "Who the driver rings, and for what.",
            style = MaterialTheme.typography.bodySmall,
            color = scheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(4.dp))

        if (uiState.isLoadingContacts) {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = scheme.primary,
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text = "Loading contacts…",
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.onSurfaceVariant,
                )
            }
        } else {
            IndentContactRole.entries.forEachIndexed { index, role ->
                ContactRoleSlot(
                    role = role,
                    contact = uiState.contactFor(role),
                    onClick = { onContactClick(role) },
                    onClear = { onEvent(CreateIndentEvent.SelectContact(role, null)) },
                    testTag = "create_indent_contact_${role.wireKey}",
                    isError =
                        role == IndentContactRole.PLACEMENT &&
                            errors.contains("placement"),
                    enabled = clientSelected && !uiState.contactsUnavailable,
                    placeholder =
                        when {
                            !clientSelected -> "Select a client first"
                            uiState.contactsUnavailable -> "No contacts on file"
                            else -> "Choose contact"
                        },
                )
                if (index != IndentContactRole.entries.lastIndex) {
                    HorizontalDivider(
                        thickness = 1.dp,
                        color = scheme.outlineVariant.copy(alpha = 0.45f),
                    )
                }
            }
        }

        if (clientSelected && !uiState.isLoadingContacts && uiState.contactsUnavailable) {
            Spacer(Modifier.height(10.dp))
            InlineErrorBanner(
                message =
                    "This client has no active contacts. A placement contact is " +
                        "required to raise an indent — add one to the client first.",
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Clause 03 — vehicle
// ---------------------------------------------------------------------------

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun VehicleClause(
    truckTypes: TruckCategoryDto,
    selectedTruckType: TruckTypeDto?,
    isLoading: Boolean,
    isError: Boolean,
    state: ClauseState,
    onSelect: (TruckTypeDto) -> Unit,
) {
    val scheme = MaterialTheme.colorScheme

    ClauseDocket(
        number = 3,
        title = stringResource(R.string.vehicle),
        state = state,
        testTag = "create_indent_clause_vehicle",
    ) {
        if (isError) {
            Text(
                text = "Select a vehicle type to continue.",
                style = MaterialTheme.typography.bodySmall,
                color = scheme.error,
            )
            Spacer(Modifier.height(10.dp))
        }

        if (isLoading) {
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(
                    color = scheme.primary,
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                )
            }
        } else {
            TruckTypeGroup(
                label = "Open body",
                trucks = truckTypes.open,
                selectedTruckType = selectedTruckType,
                onSelect = onSelect,
                tagPrefix = "open",
            )
            if (truckTypes.open.isNotEmpty() && truckTypes.closed.isNotEmpty()) {
                Spacer(Modifier.height(14.dp))
            }
            TruckTypeGroup(
                label = "Closed body",
                trucks = truckTypes.closed,
                selectedTruckType = selectedTruckType,
                onSelect = onSelect,
                tagPrefix = "closed",
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TruckTypeGroup(
    label: String,
    trucks: List<TruckTypeDto>,
    selectedTruckType: TruckTypeDto?,
    onSelect: (TruckTypeDto) -> Unit,
    tagPrefix: String,
) {
    if (trucks.isEmpty()) return

    Column {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.1.sp),
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(8.dp))
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            trucks.forEach { truck ->
                VehicleTypeChip(
                    label = truck.displayName,
                    isSelected = selectedTruckType?.id == truck.id,
                    onClick = { onSelect(truck) },
                    testTag = "create_indent_truck_${tagPrefix}_${truck.id}",
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Clause 05 — commercials
// ---------------------------------------------------------------------------

@Composable
private fun CommercialsClause(
    uiState: CreateIndentUiState,
    state: ClauseState,
    negotiatedError: String?,
    onEvent: (CreateIndentEvent) -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val summary =
        listOfNotNull(
            uiState.clientCost.takeIf { it.isNotBlank() }?.let { "Client ₹$it" },
            uiState.negotiatedCost.takeIf { it.isNotBlank() }?.let { "Negotiated ₹$it" },
        ).joinToString(" · ")

    ClauseDocket(
        number = 5,
        title = stringResource(R.string.commercials),
        state = state,
        testTag = "create_indent_clause_commercials",
    ) {
        MoneyRuleField(
            label = stringResource(R.string.client_cost),
            raw = uiState.clientCost,
            testTag = "create_indent_client_cost_field",
            placeholder = "0",
            onCommit = { onEvent(CreateIndentEvent.UpdateOptionalField(clientCost = it)) },
            trailingLabel = "INR",
        )
        Spacer(Modifier.height(18.dp))
        MoneyRuleField(
            label = stringResource(R.string.business_proposed_cost),
            raw = uiState.businessProposedCost,
            testTag = "create_indent_proposed_cost_field",
            placeholder = "0",
            onCommit = { onEvent(CreateIndentEvent.UpdateOptionalField(businessProposedCost = it)) },
            trailingLabel = "INR",
        )
        Spacer(Modifier.height(18.dp))
        MoneyRuleField(
            label = stringResource(R.string.target_cost),
            raw = uiState.negotiatedCost,
            testTag = "create_indent_negotiated_cost_field",
            placeholder = "0",
            onCommit = { onEvent(CreateIndentEvent.UpdateOptionalField(negotiatedCost = it)) },
            trailingLabel = "INR",
            isError = negotiatedError != null,
            supportingText = negotiatedError,
        )

        Spacer(Modifier.height(14.dp))
        PerforationRule()
        Spacer(Modifier.height(4.dp))

        DocketSwitchRow(
            title = stringResource(R.string.hide_cost_from_load_board),
            description = stringResource(R.string.truck_owners_see_the_load_without_the_rate),
            checked = uiState.hideCost,
            onCheckedChange = { onEvent(CreateIndentEvent.UpdateOptionalField(hideCost = it)) },
            testTag = "create_indent_hide_cost_switch",
        )

        if (summary.isNotBlank()) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = summary,
                style = MaterialTheme.typography.bodySmall,
                color = scheme.onSurfaceVariant,
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Clause 06 — terms
// ---------------------------------------------------------------------------

@Composable
private fun TermsClause(
    uiState: CreateIndentUiState,
    state: ClauseState,
    tatError: Boolean,
    onEvent: (CreateIndentEvent) -> Unit,
) {
    ClauseDocket(
        number = 6,
        title = stringResource(R.string.terms),
        state = state,
        testTag = "create_indent_clause_terms",
    ) {
        DocketSwitchRow(
            title = stringResource(R.string.turn_around_time),
            description = "Commit to a delivery window from loading.",
            checked = uiState.tatRequired,
            onCheckedChange = { onEvent(CreateIndentEvent.UpdateOptionalField(tatRequired = it)) },
            testTag = "create_indent_tat_switch",
        )
        Spacer(Modifier.height(6.dp))
        RuledField(
            label = stringResource(R.string.tat_hours),
            value = uiState.tatHours,
            onValueChange = { onEvent(CreateIndentEvent.UpdateOptionalField(tatHours = it)) },
            testTag = "create_indent_tat_hours_field",
            placeholder = "0",
            keyboardType = KeyboardType.Decimal,
            enabled = uiState.tatRequired,
            isError = tatError,
            supportingText =
                if (!uiState.tatRequired) {
                    "Enable turnaround time to set this."
                } else {
                    null
                },
            trailingLabel = "HRS",
        )

        Spacer(Modifier.height(18.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            RuledField(
                label = stringResource(R.string.vehicle_height),
                value = uiState.vehicleHeight,
                onValueChange = {
                    onEvent(CreateIndentEvent.UpdateOptionalField(vehicleHeight = it))
                },
                testTag = "create_indent_height_field",
                placeholder = "0",
                keyboardType = KeyboardType.Number,
                trailingLabel = "FT",
                modifier = Modifier.weight(1f),
            )
            RuledField(
                label = stringResource(R.string.flooring),
                value = uiState.flooringType,
                onValueChange = {
                    onEvent(CreateIndentEvent.UpdateOptionalField(flooringType = it))
                },
                testTag = "create_indent_flooring_field",
                placeholder = "steel",
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(Modifier.height(14.dp))
        PerforationRule()
        Spacer(Modifier.height(4.dp))

        DocketSwitchRow(
            title = stringResource(R.string.tarpaulin_required),
            description = "The load must be sheeted.",
            checked = uiState.tarpaulinRequired,
            onCheckedChange = {
                onEvent(CreateIndentEvent.UpdateOptionalField(tarpaulinRequired = it))
            },
            testTag = "create_indent_tarpaulin_switch",
        )

        Spacer(Modifier.height(10.dp))

        RuledField(
            label = stringResource(R.string.remarks),
            value = uiState.remarks,
            onValueChange = { onEvent(CreateIndentEvent.UpdateOptionalField(remarks = it)) },
            testTag = "create_indent_remarks_field",
            placeholder = "Anything the driver should know",
            minLines = 2,
        )
    }
}

// ---------------------------------------------------------------------------
// Money input
// ---------------------------------------------------------------------------

/**
 * A money field on a rule. Owns its [TextFieldValue] so the caret stays put
 * while thousands separators are inserted — the plain-`String` overload loses
 * the selection on every recomposition and the caret snaps to the end.
 */
@Composable
fun MoneyRuleField(
    label: String,
    raw: String,
    testTag: String,
    onCommit: (String) -> Unit,
    placeholder: String = "0",
    isError: Boolean = false,
    supportingText: String? = null,
    trailingLabel: String? = null,
) {
    var field by remember { mutableStateOf(TextFieldValue(raw, TextRange(raw.length))) }
    LaunchedEffect(raw) {
        if (field.text != raw) {
            field = TextFieldValue(raw, TextRange(raw.length))
        }
    }

    RuledAmountField(
        label = label,
        value = field,
        onValueChange = { newValue ->
            val plain = newValue.text.replace(",", "")
            val formatted = formatIndianCurrency(plain)
            val commaDiff = formatted.count { it == ',' } - field.text.count { it == ',' }
            val cursor = (newValue.selection.start + commaDiff).coerceIn(0, formatted.length)
            field = TextFieldValue(formatted, TextRange(cursor))
            onCommit(formatted)
        },
        testTag = testTag,
        placeholder = placeholder,
        isError = isError,
        supportingText = supportingText,
        trailingLabel = trailingLabel,
    )
}

// ---------------------------------------------------------------------------
// Clause state
//
// Each clause reports how far it has got. Required clauses distinguish
// COMPLETE from PARTIAL; optional clauses use FILLED instead of COMPLETE so a
// filled-in optional clause is never mistaken for a satisfied requirement.
// ---------------------------------------------------------------------------

fun consignorClauseState(
    state: CreateIndentUiState,
    errors: Set<String>,
): ClauseState =
    when {
        errors.contains("client") || errors.contains("placement") -> ClauseState.ERROR
        state.selectedClient != null && state.placementContact != null -> ClauseState.COMPLETE
        state.selectedClient != null || state.selectedBranch != null ||
            state.placementContact != null || state.paymentContact != null ||
            state.operationsContact != null || state.podContact != null -> ClauseState.PARTIAL
        else -> ClauseState.EMPTY
    }

fun laneClauseState(
    state: CreateIndentUiState,
    errors: Set<String>,
): ClauseState =
    when {
        errors.contains("from") || errors.contains("to") -> ClauseState.ERROR
        state.selectedFromLocation != null && state.selectedToLocation != null -> ClauseState.COMPLETE
        state.selectedFromLocation != null || state.selectedToLocation != null -> ClauseState.PARTIAL
        else -> ClauseState.EMPTY
    }

fun vehicleClauseState(
    state: CreateIndentUiState,
    errors: Set<String>,
): ClauseState =
    when {
        errors.contains("truck") -> ClauseState.ERROR
        state.selectedTruckType != null -> ClauseState.COMPLETE
        else -> ClauseState.EMPTY
    }

fun cargoClauseState(
    state: CreateIndentUiState,
    errors: Set<String>,
): ClauseState {
    val weightOk = state.weight.toDoubleOrNull()?.let { it > 0 } == true
    val materialOk = state.materialName.isNotBlank()
    return when {
        errors.contains("weight") || errors.contains("material") -> ClauseState.ERROR
        weightOk && materialOk -> ClauseState.COMPLETE
        state.weight.isNotBlank() || state.materialName.isNotBlank() -> ClauseState.PARTIAL
        else -> ClauseState.EMPTY
    }
}

fun commercialsClauseState(
    state: CreateIndentUiState,
    errors: Set<String>,
): ClauseState {
    val touched =
        state.clientCost.isNotBlank() ||
            state.businessProposedCost.isNotBlank() ||
            state.negotiatedCost.isNotBlank() ||
            state.hideCost
    return when {
        errors.contains("negotiated") -> ClauseState.ERROR
        touched -> ClauseState.FILLED
        else -> ClauseState.EMPTY
    }
}

fun termsClauseState(
    state: CreateIndentUiState,
    errors: Set<String>,
): ClauseState {
    val touched =
        state.tatRequired ||
            state.tarpaulinRequired ||
            state.vehicleHeight.isNotBlank() ||
            state.remarks.isNotBlank()
    return when {
        errors.contains("tat") -> ClauseState.ERROR
        touched -> ClauseState.FILLED
        else -> ClauseState.EMPTY
    }
}
