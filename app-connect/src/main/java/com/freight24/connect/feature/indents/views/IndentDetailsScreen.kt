package com.freight24.connect.feature.indents.views

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.freight24.common.util.DateTimeUtils
import com.freight24.common.util.formatIndianCurrency
import com.freight24.common.util.formatWeightValue
import com.freight24.common.util.negotiatedCostExceedsClientError
import com.freight24.connect.R
import com.freight24.connect.feature.indents.domain.viewmodel.IndentDetailsEvent
import com.freight24.connect.feature.indents.domain.viewmodel.IndentDetailsUiState
import com.freight24.connect.feature.indents.domain.viewmodel.IndentDetailsViewModel
import com.freight24.connect.feature.indents.domain.viewmodel.IndentDetailsViewModelImpl
import com.freight24.ui.components.FreightTopAppBar
import com.freight24.ui.components.MetaTag
import com.freight24.ui.components.PerforationRule
import com.freight24.ui.components.RefreshOnForeground
import com.freight24.ui.components.formViewportInsets
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
    onViewProfileClick: (String) -> Unit = { _ -> },
    viewModel: IndentDetailsViewModel = hiltViewModel<IndentDetailsViewModelImpl>(),
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val colorScheme = MaterialTheme.colorScheme
    val extended = FreightThemeTokens.colors
    val snackbarHostState = remember { SnackbarHostState() }

    val refreshTrigger =
        navController.currentBackStackEntry
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

    RefreshOnForeground { viewModel.onEvent(IndentDetailsEvent.Refresh) }

    val assignNegotiatedCostError =
        if (uiState.isNegotiatedSameAsClient) {
            null
        } else {
            negotiatedCostExceedsClientError(uiState.assignNegotiatedCost, uiState.assignClientCost)
        }

    if (uiState.isAssignDialogVisible) {
        val assignSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { viewModel.onEvent(IndentDetailsEvent.HideAssignDialog) },
            sheetState = assignSheetState,
            containerColor = colorScheme.surface,
            dragHandle = { BottomSheetDefaults.DragHandle() },
        ) {
            AssignRateSheet(
                uiState = uiState,
                indentCode =
                    uiState.indent?.indentCode?.let {
                        if (it.startsWith("#")) it else "#$it"
                    } ?: "This indent",
                marginError = assignNegotiatedCostError,
                onEvent = viewModel::onEvent,
            )
        }
    }

    val lifecycleDialogVisible = uiState.isCancelDialogVisible || uiState.isReopenDialogVisible
    val lifecycleReasonOptions = listOf(
        "TRUCKER_WITHDREW" to "Trucker withdrew",
        "TRUCK_UNAVAILABLE" to "Truck unavailable",
        "CLIENT_CANCELLED" to "Client cancelled",
        "RATE_NOT_AGREED" to "Rate not agreed",
        "DUPLICATE" to "Duplicate indent",
        "OTHER" to "Other",
    )

    if (lifecycleDialogVisible) {
        val isReopen = uiState.isReopenDialogVisible
        val canConfirm =
            uiState.lifecycleReasonCode.isNotBlank() &&
                (uiState.lifecycleReasonCode != "OTHER" || uiState.lifecycleRemarks.isNotBlank())
        AlertDialog(
            onDismissRequest = {
                viewModel.onEvent(
                    if (isReopen) IndentDetailsEvent.HideReopenDialog else IndentDetailsEvent.HideCancelDialog,
                )
            },
            title = { Text(if (isReopen) "Find another truck" else "Cancel indent") },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        if (isReopen) {
                            "Return this indent to Open and withdraw its current trucker interests."
                        } else {
                            "Cancel this indent permanently. It will no longer be available for assignment."
                        },
                    )
                    Text("Reason", style = MaterialTheme.typography.labelLarge)
                    lifecycleReasonOptions.forEach { (code, label) ->
                        Row(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.onEvent(IndentDetailsEvent.UpdateLifecycleReason(code)) },
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RadioButton(
                                selected = uiState.lifecycleReasonCode == code,
                                onClick = { viewModel.onEvent(IndentDetailsEvent.UpdateLifecycleReason(code)) },
                            )
                            Text(label)
                        }
                    }
                    OutlinedTextField(
                        value = uiState.lifecycleRemarks,
                        onValueChange = { viewModel.onEvent(IndentDetailsEvent.UpdateLifecycleRemarks(it)) },
                        modifier = Modifier.fillMaxWidth(),
                        label = {
                            Text(if (uiState.lifecycleReasonCode == "OTHER") "Remarks *" else "Remarks (optional)")
                        },
                        minLines = 2,
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.onEvent(
                            if (isReopen) IndentDetailsEvent.ConfirmReopen else IndentDetailsEvent.ConfirmCancel,
                        )
                    },
                    enabled = !uiState.isCanceling && canConfirm,
                    colors =
                        if (isReopen) {
                            ButtonDefaults.buttonColors()
                        } else {
                            ButtonDefaults.buttonColors(containerColor = colorScheme.error)
                        },
                ) {
                    if (uiState.isCanceling) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp).padding(end = 8.dp),
                            color = colorScheme.onError,
                            strokeWidth = 2.dp,
                        )
                    }
                    Text(if (isReopen) "Find another truck" else "Cancel indent")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        viewModel.onEvent(
                            if (isReopen) IndentDetailsEvent.HideReopenDialog else IndentDetailsEvent.HideCancelDialog,
                        )
                    },
                ) {
                    Text("Keep indent")
                }
            },
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
            dragHandle = { BottomSheetDefaults.DragHandle() },
        ) {
            TruckerInterestSheetContent(
                truckerList = uiState.truckerList,
                isLoading = uiState.isTruckerListLoading,
                onClose = { viewModel.onEvent(IndentDetailsEvent.HideTruckerSheet) },
                onReject = { interestId ->
                    viewModel.onEvent(IndentDetailsEvent.RejectTruckerInterest(interestId))
                },
            )
        }
    }

    if (uiState.isSuitableTrucksSheetVisible) {
        val sheetState = rememberModalBottomSheetState()
        ModalBottomSheet(
            onDismissRequest = { viewModel.onEvent(IndentDetailsEvent.HideSuitableTrucksSheet) },
            sheetState = sheetState,
            containerColor = colorScheme.surface,
            dragHandle = { BottomSheetDefaults.DragHandle() },
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
    val indentCode =
        indent?.indentCode?.let {
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
                            overflow = TextOverflow.Ellipsis,
                        )
                        indent?.createdAt?.let { createdAt ->
                            Text(
                                text = "Created ${DateTimeUtils.formatIndentTimestamp(createdAt)}",
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
                            tint = colorScheme.onSurface,
                        )
                    }
                },
                actions = {
                    indent?.status?.let { status ->
                        IndentStatusChip(status)
                    }
                    if (
                        indent != null &&
                        (indent.status.equals("open", true) || indent.status.equals("READY_FOR_ASSIGNMENT", true)) &&
                        (indent.canEdit || uiState.canProgressIndent)
                    ) {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More options", tint = colorScheme.onSurface)
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false },
                            modifier = Modifier.background(colorScheme.surface),
                        ) {
                            if (indent.status.equals("READY_FOR_ASSIGNMENT", true)) {
                                DropdownMenuItem(
                                    text = { Text("Find another truck") },
                                    leadingIcon = {
                                        Icon(Icons.Default.Refresh, contentDescription = "Find another truck")
                                    },
                                    onClick = {
                                        menuExpanded = false
                                        viewModel.onEvent(IndentDetailsEvent.ShowReopenDialog)
                                    },
                                )
                            }
                            DropdownMenuItem(
                                text = { Text("Cancel Indent", color = colorScheme.error) },
                                leadingIcon = {
                                    Icon(
                                        Icons.Outlined.Delete,
                                        contentDescription = "Cancel Indent",
                                        tint = colorScheme.error,
                                    )
                                },
                                onClick = {
                                    menuExpanded = false
                                    viewModel.onEvent(IndentDetailsEvent.ShowCancelDialog)
                                },
                            )
                        }
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            indent?.let {
                if (
                    (it.canEdit || uiState.canProgressIndent) &&
                    (it.status.equals("open", true) || it.status.equals("READY_FOR_ASSIGNMENT", true))
                ) {
                    BottomActionBar(
                        status = it.status,
                        canProgressIndent = uiState.canProgressIndent,
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
                        onShareClick = { shareIndentOnWhatsApp(context, buildIndentShareMessage(it)) },
                    )
                }
            }
        },
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = { viewModel.onEvent(IndentDetailsEvent.PullToRefresh) },
            modifier =
                Modifier
                    .fillMaxSize()
                    // Keyboard-aware viewport: without this the scroll bounds run behind the
                    // keyboard and bring-into-view leaves the focused field hidden.
                    // See FormInsets.kt.
                    .formViewportInsets(paddingValues),
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                uiState.errorMessage != null -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center).padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
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
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        if (costError) {
                            val missingParts =
                                buildList {
                                    val clientMissing = currentIndent.indentGroupDetails.clientTripCost?.let { it <= 0 } ?: true
                                    val negotiatedMissing = currentIndent.negotiatedTripCost?.let { it <= 0 } ?: true
                                    if (clientMissing) add("Client trip cost")
                                    if (negotiatedMissing) add("Target trip cost")
                                }
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = colorScheme.errorContainer),
                                shape = MaterialTheme.shapes.medium,
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

                        ConsignorClause(
                            number = 1,
                            clientName =
                                groupDetails.clientProfile?.companyName
                                    ?: groupDetails.clientProfile?.fullName
                                    ?: "Unknown",
                            tagCode = groupDetails.clientProfile?.tagCode,
                            branchName = groupDetails.clientBranch?.branchName,
                            branchCode = groupDetails.clientBranch?.branchCode,
                            clientPhone = groupDetails.clientProfile?.phoneNumber,
                            callLines = groupDetails.indentContacts.toDocketCallLines(),
                            onViewProfileClick = {
                                onViewProfileClick(groupDetails.clientId)
                            },
                            onCall = { dialNumber ->
                                context.startActivity(
                                    Intent(Intent.ACTION_DIAL, Uri.parse("tel:$dialNumber")),
                                )
                            },
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
                                    },
                                )
                            }
                        }

                        RouteAndFinancialsSection(
                            number = 2,
                            originCity =
                                loadingLocation?.let {
                                    "${it.areaName ?: it.cityName}${it.address?.let { addr -> ", $addr" } ?: ""}"
                                } ?: "Unknown",
                            destinationCity =
                                unloadingLocation?.let {
                                    "${it.areaName ?: it.cityName}${it.address?.let { addr -> ", $addr" } ?: ""}"
                                } ?: "Unknown",
                            clientCost = formatCurrencyDetails(groupDetails.clientTripCost),
                            negotiatedCost = formatCurrencyDetails(currentIndent.negotiatedTripCost),
                            hideCost = currentIndent.hideCostInfo,
                            isCostVisible = !currentIndent.hideCostInfo,
                            hasError = costError,
                        )

                        SpecificationsSection(
                            number = 3,
                            weight = formatWeightValue(groupDetails.loadWeight),
                            weightUnit = "MT",
                            materialType = groupDetails.material,
                            truckType = groupDetails.truckType?.displayName,
                            truckTypeUnit = groupDetails.truckType?.bodyType ?: "",
                            tatRequired = groupDetails.tatEnabled,
                            tatHours = groupDetails.tatHours?.toString() ?: "",
                            tarpaulinRequired = groupDetails.tarpaulingRequired,
                        )

                        RemarksSection(
                            number = 4,
                            remarks = groupDetails.specialRequirements ?: "",
                        )
                    }
                }
            }
        }
    }
}

/**
 * The rate card an operator fills in to push an indent onto the load board.
 *
 * This was a stock [AlertDialog] holding three boxed `OutlinedTextField`s and a
 * bare `Checkbox` — the one surface on this screen that ignored the docket
 * language the rest of it is built from, and the only place in the app still
 * formatting Indian currency by hand.
 *
 * It is a sheet now because the job needs the keyboard: two rates, a note, and a
 * confirm, with real room to see what the margin does as they type. The fields
 * are the same ruled controls the Create and Edit indents use, so grouping,
 * caret behaviour and the "cannot exceed client cost" rule are shared rather than
 * reimplemented.
 */
@Composable
private fun AssignRateSheet(
    uiState: IndentDetailsUiState,
    indentCode: String,
    marginError: String?,
    onEvent: (IndentDetailsEvent) -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val extended = FreightThemeTokens.colors
    // Read from the state's own parsed amounts rather than re-parsing the raw
    // strings here, so the margin can never disagree with the rule the confirm
    // button and the ViewModel submit path are already using.
    val client = uiState.assignClientCostAmount
    val target = uiState.assignNegotiatedCostAmount
    val margin = if (client != null && target != null) client - target else null

    // The rate card scrolls; the actions below it do not.
    //
    // BoxWithConstraints is what makes that split hold: the sheet hands its content
    // the full screen height, and `imePadding` alone would only pad the bottom of
    // an unbounded column — pushing "Mark Ready" below the keyboard instead of
    // leaving room for it. Reading maxHeight here is the height that actually
    // remains once the IME is subtracted, so the scroll region can be capped to it
    // and the actions stay pinned in view while a field is focused.
    BoxWithConstraints(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .imePadding()
                .padding(bottom = 20.dp),
    ) {
        val actionBlockHeight = 70.dp
        // Captured here: inside the nested Column the BoxWithConstraints receiver
        // is no longer the implicit one.
        val scrollableHeight = (maxHeight - actionBlockHeight).coerceAtLeast(140.dp)

        Column(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier =
                    Modifier
                        .heightIn(max = scrollableHeight)
                        .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Ready for assignment",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = scheme.onSurface,
                            modifier = Modifier.weight(1f),
                        )
                        MetaTag(
                            text = indentCode,
                            container = scheme.primaryContainer,
                            content = scheme.onPrimaryContainer,
                            hatch = true,
                        )
                    }
                    Text(
                        text =
                            "This indent will be removed from the load board",
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.onSurfaceVariant,
                    )
                }

                MoneyRuleField(
                    label = stringResource(R.string.client_cost),
                    raw = uiState.assignClientCost,
                    testTag = "indent_details_assign_client_cost_field",
                    onCommit = { onEvent(IndentDetailsEvent.UpdateAssignField(clientCost = it)) },
                    trailingLabel = "INR",
                    wholeRupeesOnly = true,
                )

                MoneyRuleField(
                    label = stringResource(R.string.target_cost),
                    raw = uiState.assignNegotiatedCost,
                    testTag = "indent_details_assign_target_cost_field",
                    onCommit = { onEvent(IndentDetailsEvent.UpdateAssignField(negotiatedCost = it)) },
                    trailingLabel = "INR",
                    wholeRupeesOnly = true,
                    enabled = !uiState.isNegotiatedSameAsClient,
                    isError = marginError != null,
                    supportingText = marginError,
                )

                // Deliberately left enabled even while the target breaches the client
                // cost: turning it on rewrites the target to the client rate, so it is
                // the recovery from that error rather than something to disable during it.
                DocketSwitchRow(
                    title = "Target cost equals client cost",
                    description = "Nothing is held back — the truck earns the full rate.",
                    checked = uiState.isNegotiatedSameAsClient,
                    onCheckedChange = { onEvent(IndentDetailsEvent.UpdateAssignField(isNegotiatedSameAsClient = it)) },
                    testTag = "indent_details_assign_same_as_client_switch",
                )

                // Suppressed while a rate is unset: "Margin 0" on a card where neither
                // rate has been entered reads as a real result rather than "not yet".
                if (margin != null && margin >= 0 && !uiState.assignRatesMissing) {
                    Surface(
                        color = extended.success.copy(alpha = 0.12f),
                        shape = MaterialTheme.shapes.small,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "Margin",
                                style =
                                    MaterialTheme.typography.labelSmall.copy(
                                        letterSpacing = 1.3.sp,
                                    ),
                                fontWeight = FontWeight.Bold,
                                color = extended.success,
                            )
                            Spacer(Modifier.width(12.dp))
                            // Invisible flex spacer, so the amount sits hard
                            // against the trailing edge. Putting the amount in
                            // the weighted slot instead would left-align it
                            // immediately after the label.
                            Text(
                                text = "",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = scheme.onSurface,
                                modifier = Modifier.weight(1f),
                            )
                            Text(
                                text = margin.toIndianAmount(),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = scheme.onSurface,
                            )
                        }
                    }
                }

                PerforationRule()

                RuledField(
                    label = stringResource(R.string.notes),
                    value = uiState.assignNotes,
                    onValueChange = { onEvent(IndentDetailsEvent.UpdateAssignField(notes = it)) },
                    testTag = "indent_details_assign_notes_field",
                    placeholder = "Anything the truckers should read first",
                    minLines = 2,
                )
            }

            Spacer(Modifier.height(18.dp))

            // A calm line rather than red rules under both fields on open: an empty
            // rate is the normal starting state, not a mistake the operator has made
            // yet. It names what is still needed and disappears the moment it is.
            if (uiState.assignRatesMissing) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = null,
                        tint = scheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text =
                            "Both rates must be above 0 — " +
                                "${uiState.assignMissingRateLabels.joinToString(" and ")} still not set.",
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.onSurfaceVariant,
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(
                    onClick = { onEvent(IndentDetailsEvent.HideAssignDialog) },
                    modifier = Modifier.height(52.dp),
                ) {
                    Text("Not now")
                }
                Button(
                    onClick = { onEvent(IndentDetailsEvent.ConfirmAssignment) },
                    enabled = !uiState.isAssigning && uiState.canConfirmAssignment,
                    shape = MaterialTheme.shapes.large,
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = scheme.primary,
                            contentColor = scheme.onPrimary,
                        ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                    modifier =
                        Modifier
                            .weight(1f)
                            .height(52.dp)
                            .docketTag("indent_details_assign_confirm_button")
                            .semantics { contentDescription = "Mark ready for assignment" },
                ) {
                    if (uiState.isAssigning) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = scheme.onPrimary,
                            strokeWidth = 2.dp,
                        )
                        Spacer(Modifier.width(10.dp))
                    }
                    Text(
                        text = if (uiState.isAssigning) "Filing…" else "Mark Ready",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

/** Whole rupees, Indian-grouped — the same shape the fields render. */
private fun Int.toIndianAmount(): String = toString().let(::formatIndianCurrency)

@Composable
private fun BottomActionBar(
    status: String,
    canProgressIndent: Boolean,
    onAssignClick: () -> Unit,
    onCreateTripClick: () -> Unit,
    onEditClick: () -> Unit,
    onShareClick: () -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme

    Surface(
        color = colorScheme.surface,
        shadowElevation = 8.dp,
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 20.dp, top = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BarIconButton(
                icon = Icons.Default.Edit,
                contentDescription = "Edit indent",
                testTag = "indent_details_edit_button",
                onClick = onEditClick,
            )

            if (canProgressIndent) {
                Spacer(modifier = Modifier.width(4.dp))

                Button(
                    onClick = {
                        if (status.equals("open", true)) onAssignClick() else onCreateTripClick()
                    },
                    // min, not fixed: at large font scales even a one-line label
                    // can wrap, and a fixed height clipped the overflow mid-line.
                    modifier =
                        Modifier
                            .weight(1f)
                            .heightIn(min = 52.dp),
                    shape = MaterialTheme.shapes.large,
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = colorScheme.primary,
                            contentColor = colorScheme.onPrimary,
                        ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp),
                ) {
                    Text(
                        // Same wording the assign sheet's confirm button uses for
                        // this same action, and short enough to stay on one line
                        // between the two icon buttons.
                        text = if (status.equals("open", true)) "Mark Ready" else "Create Trip",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }
            } else {
                // Keeps the share icon on the trailing edge when there is no
                // primary action to flex between the two of them.
                Spacer(modifier = Modifier.weight(1f))
            }

            BarIconButton(
                icon = Icons.Default.Share,
                contentDescription = "Share indent",
                testTag = "indent_details_share_button",
                onClick = onShareClick,
            )
        }
    }
}

/** One of the bar's round, flat icon controls — edit and share are the same device. */
@Composable
private fun BarIconButton(
    icon: ImageVector,
    contentDescription: String,
    testTag: String,
    onClick: () -> Unit,
) {
    IconButton(
        onClick = onClick,
        modifier =
            Modifier
                .size(48.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                .docketTag(testTag)
                .semantics { this.contentDescription = contentDescription },
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp),
        )
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
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Interest Expressed",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface,
                )
                val count = truckerList.size
                Text(
                    text =
                        if (count == 1) {
                            "$count trucker has shown interest"
                        } else {
                            "$count truckers have shown interest"
                        },
                    style = MaterialTheme.typography.bodySmall,
                    color = colorScheme.onSurfaceVariant,
                )
            }
            IconButton(
                onClick = onClose,
                modifier = Modifier.background(colorScheme.surfaceVariant, CircleShape).size(32.dp),
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Close",
                    modifier = Modifier.size(18.dp),
                    tint = colorScheme.onSurfaceVariant,
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
                        onReject = { onReject(trucker.interestId) },
                    )
                }
            }
        }
    }
}

@Composable
private fun SuitsableTrucksContent(trucks: com.freight24.rest.response.SearchTruckerResponse.TruckerData?) {
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

// Timestamp formatting lives in DateTimeUtils.formatIndentTimestamp — it was
// duplicated here and in IndentSuccessScreen, and only this copy handled the
// offset/microseconds Supabase actually sends, so the counterfoil printed a raw
// ISO string. One implementation, one answer.
