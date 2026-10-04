package com.freight24.connect.feature.verificationrequests.views

import com.freight24.ui.components.FreightTopAppBar
import com.freight24.ui.components.FreightEmptyState

import android.content.Context
import android.os.Build
import android.text.format.DateUtils
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Route
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.freight24.connect.feature.verificationrequests.domain.model.BusinessProfileChange
import com.freight24.connect.feature.verificationrequests.domain.model.ClientVerificationRequest
import com.freight24.connect.feature.verificationrequests.domain.model.TruckDetailChange
import com.freight24.connect.feature.verificationrequests.domain.model.TruckOwnerVerificationRequest
import com.freight24.connect.feature.verificationrequests.domain.model.TruckVerificationRequest
import com.freight24.connect.feature.verificationrequests.domain.model.VerificationRequest
import com.freight24.connect.feature.verificationrequests.domain.model.VerificationRequestDocument
import com.freight24.connect.feature.verificationrequests.domain.model.VerificationRequestLocation
import com.freight24.connect.feature.verificationrequests.domain.model.VerificationRequestStatus
import com.freight24.connect.feature.verificationrequests.domain.model.VerificationRequestTab
import com.freight24.connect.feature.verificationrequests.domain.viewmodel.VerificationRequestsEvent
import com.freight24.connect.feature.verificationrequests.domain.viewmodel.VerificationRequestsViewModel
import com.freight24.connect.feature.verificationrequests.domain.viewmodel.VerificationRequestsViewModelImpl
import com.freight24.ui.components.DocketCard
import com.freight24.ui.components.DocketCut
import com.freight24.ui.components.DocketShape
import com.freight24.ui.components.DocumentLinkChip
import com.freight24.ui.components.HatchMark
import com.freight24.ui.components.MetaTag
import com.freight24.ui.components.PanelShape
import com.freight24.ui.components.PerforationRule
import com.freight24.ui.components.RouteIndicatorWithCities
import com.freight24.ui.components.SpineWidth
import com.freight24.ui.components.TagShape
import com.freight24.ui.components.getPdfLaunchIntent
import com.freight24.ui.theme.FreightThemeTokens
import com.freight24.ui.components.formViewportInsets
import java.text.DateFormat
import java.time.OffsetDateTime
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.launch

// ===========================================================================
// Verification Requests — "clearing desk" presentation layer.
//
// The page is an intake desk: a queue of dockets an operations employee has to
// clear. The visual language is borrowed from the freight paperwork the app
// already speaks — waybills, lane markings, rubber stamps and perforated tear
// lines — rather than generic Material cards.
//
// Three primitives carry the whole screen:
//   • the LANE SPINE  — a 4dp colour bar down the left edge of every card,
//                       keyed to the request status. Reads like a file tab in
//                       a physical docket tray.
//   • the PERFORATION — a dashed tear line separating the docket body from its
//                       footer, so the card looks like a document, not a panel.
//   • the LEDGER ROW  — one field per row. New onboardings drop the "Before"
//                       column entirely and stamp the row NEW; genuine edits
//                       read as `was → now` in a single wrapping line.
//
// The card/stamp primitives themselves (DocketCard, PerforationRule,
// HatchMark, MetaTag, docket shapes) live in core/ui so the More desk and the
// Leads tray share this exact language.
// ---------------------------------------------------------------------------

// ---------------------------------------------------------------------------
// Page-local type helpers. Everything derives from MaterialTheme.typography so
// the app-wide Sora / Be Vietnam Pro scale stays the single source of truth;
// only tracking and weight are tuned here.
// ---------------------------------------------------------------------------

@Composable
private fun eyebrow() = MaterialTheme.typography.labelSmall.copy(
    fontWeight = FontWeight.SemiBold,
    letterSpacing = 1.4.sp,
)

@Composable
private fun ledgerLabel() = MaterialTheme.typography.labelSmall.copy(
    fontWeight = FontWeight.SemiBold,
    letterSpacing = 1.1.sp,
)

@Composable
private fun identifier() = MaterialTheme.typography.titleSmall.copy(
    fontWeight = FontWeight.SemiBold,
    letterSpacing = 0.9.sp,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerificationRequestsScreen(
    onBackClick: () -> Unit,
    viewModel: VerificationRequestsViewModel = hiltViewModel<VerificationRequestsViewModelImpl>(),
    tripCount: Int = 0,
    truckCount: Int = 0,
    truckOwnerCount: Int = 0,
    clientCount: Int = 0,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    var selectedRequestData by remember { mutableStateOf<VerificationRequest?>(null) }
    var selectedTruckData by remember { mutableStateOf<TruckVerificationRequest?>(null) }
    var selectedTruckOwnerData by remember { mutableStateOf<TruckOwnerVerificationRequest?>(null) }
    var selectedClientData by remember { mutableStateOf<ClientVerificationRequest?>(null) }
    var remarks by rememberSaveable { mutableStateOf("") }
    var remarksError by rememberSaveable { mutableStateOf<String?>(null) }
    var currentTripCount by remember { mutableIntStateOf(tripCount) }
    var currentTruckCount by remember { mutableIntStateOf(truckCount) }
    var currentTruckOwnerCount by remember { mutableIntStateOf(truckOwnerCount) }
    var currentClientCount by remember { mutableIntStateOf(clientCount) }
    val clearSelectedRequest = {
        viewModel.onEvent(VerificationRequestsEvent.ClearActionState)
        selectedRequestData = null
        selectedTruckData = null
        selectedTruckOwnerData = null
        selectedClientData = null
        remarks = ""
        remarksError = null
    }

    LaunchedEffect(uiState.actionCompletedAt) {
        if (uiState.actionCompletedAt != null) {
            when {
                selectedRequestData != null -> currentTripCount = (currentTripCount - 1).coerceAtLeast(0)
                selectedTruckData != null -> currentTruckCount = (currentTruckCount - 1).coerceAtLeast(0)
                selectedTruckOwnerData != null -> currentTruckOwnerCount = (currentTruckOwnerCount - 1).coerceAtLeast(0)
                selectedClientData != null -> currentClientCount = (currentClientCount - 1).coerceAtLeast(0)
            }
            remarks = ""
            remarksError = null
            if (selectedRequestData != null) {
                scope.launch { sheetState.hide() }.invokeOnCompletion { selectedRequestData = null }
            }
            if (selectedTruckData != null) {
                scope.launch { sheetState.hide() }.invokeOnCompletion { selectedTruckData = null }
            }
            if (selectedTruckOwnerData != null) {
                scope.launch { sheetState.hide() }.invokeOnCompletion { selectedTruckOwnerData = null }
            }
            if (selectedClientData != null) {
                scope.launch { sheetState.hide() }.invokeOnCompletion { selectedClientData = null }
            }
        }
    }

    LaunchedEffect(uiState.selectedTab, uiState.selectedStatus) {
        if (uiState.selectedTab != VerificationRequestTab.TRIPS) {
            selectedRequestData = null
        } else {
            selectedRequestData?.let { if (!uiState.visibleTrips.contains(it)) selectedRequestData = null }
        }
        if (uiState.selectedTab != VerificationRequestTab.TRUCKS) {
            selectedTruckData = null
        } else {
            selectedTruckData?.let { if (!uiState.visibleTrucks.contains(it)) selectedTruckData = null }
        }
        if (uiState.selectedTab != VerificationRequestTab.TRUCK_OWNERS) {
            selectedTruckOwnerData = null
        } else {
            selectedTruckOwnerData?.let { if (!uiState.visibleTruckOwners.contains(it)) selectedTruckOwnerData = null }
        }
        if (uiState.selectedTab != VerificationRequestTab.CLIENTS) {
            selectedClientData = null
        } else {
            selectedClientData?.let { if (!uiState.visibleClients.contains(it)) selectedClientData = null }
        }
    }

    Scaffold(
        topBar = {
            FreightTopAppBar(
                title = {
                    Text(
                        text = "Verification Requests",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                // Keyboard-aware viewport: without this the scroll bounds run behind the
                // keyboard and bring-into-view leaves the focused field hidden. See FormInsets.kt.
                .formViewportInsets(paddingValues),
        ) {
            VerificationQueueRail(
                selectedTab = uiState.selectedTab,
                onTabSelected = { viewModel.onEvent(VerificationRequestsEvent.SelectTab(it)) },
                tripCount = currentTripCount,
                truckCount = currentTruckCount,
                truckOwnerCount = currentTruckOwnerCount,
                clientCount = currentClientCount,
            )

            VerificationStatusFilters(
                tabType = uiState.selectedTab.name,
                selectedStatus = uiState.selectedStatus,
                onStatusSelected = { viewModel.onEvent(VerificationRequestsEvent.SelectStatus(it)) },
            )

            PullToRefreshBox(
                isRefreshing = uiState.isRefreshing,
                onRefresh = { viewModel.onEvent(VerificationRequestsEvent.Refresh) },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    when (uiState.selectedTab) {
                        VerificationRequestTab.TRIPS -> {
                            val empty = uiState.visibleTrips.isEmpty()
                            if (uiState.isLoading && empty) {
                                item { LoadingDockets() }
                            } else if (uiState.errorMessage != null && empty) {
                                item {
                                    VerificationErrorState(
                                        message = uiState.errorMessage.orEmpty(),
                                        onRetry = { viewModel.onEvent(VerificationRequestsEvent.Refresh) },
                                    )
                                }
                            } else if (empty) {
                                item {
                                    VerificationEmptyState(
                                        icon = Icons.Default.Route,
                                        title = "Nothing waiting on trips",
                                        subtitle = "Trip documents that need a decision will queue up here.",
                                        status = uiState.selectedStatus,
                                    )
                                }
                            } else {
                                items(uiState.visibleTrips, key = { it.id }) { request ->
                                    TripDocketCard(
                                        request = request,
                                        onClick = {
                                            viewModel.onEvent(VerificationRequestsEvent.ClearActionState)
                                            selectedRequestData = request
                                        },
                                    )
                                }
                            }
                        }

                        VerificationRequestTab.TRUCKS -> {
                            val empty = uiState.visibleTrucks.isEmpty()
                            if (uiState.isLoading && empty) {
                                item { LoadingDockets() }
                            } else if (uiState.errorMessage != null && empty) {
                                item {
                                    VerificationErrorState(
                                        message = uiState.errorMessage.orEmpty(),
                                        onRetry = { viewModel.onEvent(VerificationRequestsEvent.Refresh) },
                                    )
                                }
                            } else if (empty) {
                                item {
                                    VerificationEmptyState(
                                        icon = Icons.Default.LocalShipping,
                                        title = "Nothing waiting on trucks",
                                        subtitle = "New truck onboardings and detail edits land here for verification.",
                                        status = uiState.selectedStatus,
                                    )
                                }
                            } else {
                                items(uiState.visibleTrucks, key = { it.id }) { truck ->
                                    TruckDocketCard(
                                        request = truck,
                                        onClick = {
                                            viewModel.onEvent(VerificationRequestsEvent.ClearActionState)
                                            selectedTruckData = truck
                                        },
                                    )
                                }
                            }
                        }

                        VerificationRequestTab.TRUCK_OWNERS -> {
                            val empty = uiState.visibleTruckOwners.isEmpty()
                            if (uiState.isLoading && empty) {
                                item { LoadingDockets() }
                            } else if (uiState.errorMessage != null && empty) {
                                item {
                                    VerificationErrorState(
                                        message = uiState.errorMessage.orEmpty(),
                                        onRetry = { viewModel.onEvent(VerificationRequestsEvent.Refresh) },
                                    )
                                }
                            } else if (empty) {
                                item {
                                    VerificationEmptyState(
                                        icon = Icons.Default.People,
                                        title = "Nothing waiting on truck owners",
                                        subtitle = "Onboarding and profile changes from truck owners queue up here.",
                                        status = uiState.selectedStatus,
                                    )
                                }
                            } else {
                                items(uiState.visibleTruckOwners, key = { it.id }) { owner ->
                                    BusinessDocketCard(
                                        eyebrowText = "TRUCK OWNER",
                                        testTag = "verification_card_owner_${owner.id}",
                                        title = owner.fullName,
                                        contact = owner.phone.orEmpty().ifBlank { "Phone not on record" },
                                        updatedAt = owner.updatedAt,
                                        status = owner.status,
                                        icon = Icons.Default.People,
                                        changes = owner.changedBusinessProfile.map { it.toLedgerEntry() },
                                        onClick = {
                                            viewModel.onEvent(VerificationRequestsEvent.ClearActionState)
                                            selectedTruckOwnerData = owner
                                        },
                                    )
                                }
                            }
                        }

                        VerificationRequestTab.CLIENTS -> {
                            val empty = uiState.visibleClients.isEmpty()
                            if (uiState.isLoading && empty) {
                                item { LoadingDockets() }
                            } else if (uiState.errorMessage != null && empty) {
                                item {
                                    VerificationErrorState(
                                        message = uiState.errorMessage.orEmpty(),
                                        onRetry = { viewModel.onEvent(VerificationRequestsEvent.Refresh) },
                                    )
                                }
                            } else if (empty) {
                                item {
                                    VerificationEmptyState(
                                        icon = Icons.Default.Business,
                                        title = "Nothing waiting on clients",
                                        subtitle = "New client onboardings and profile edits queue up here.",
                                        status = uiState.selectedStatus,
                                    )
                                }
                            } else {
                                items(uiState.visibleClients, key = { it.id }) { client ->
                                    BusinessDocketCard(
                                        eyebrowText = "CLIENT",
                                        testTag = "verification_card_client_${client.id}",
                                        title = client.companyName,
                                        contact = client.fullName,
                                        updatedAt = client.updatedAt,
                                        status = client.status,
                                        icon = Icons.Default.Business,
                                        changes = client.changedBusinessProfile.map { it.toLedgerEntry() },
                                        onClick = {
                                            viewModel.onEvent(VerificationRequestsEvent.ClearActionState)
                                            selectedClientData = client
                                        },
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (selectedRequestData != null) {
        val activeRequest = selectedRequestData!!
        val isCompletedView = uiState.selectedStatus == VerificationRequestStatus.COMPLETED
        ModalBottomSheet(
            onDismissRequest = { clearSelectedRequest() },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
        ) {
            TripVerificationRequestDetailSheet(
                request = activeRequest,
                remarks = remarks,
                remarksError = remarksError,
                onRemarksChange = {
                    remarks = it
                    if (remarksError != null) remarksError = null
                },
                onDocumentClick = { document ->
                    openVerificationDocument(context = context, viewModel = viewModel, document = document)
                },
                resolutionNotes = activeRequest.resolutionNotes,
                onApproveClick = {
                    val isLR = activeRequest.documents.any { it.name.trim().lowercase() == "lr" }
                    val status = if (isLR) VerificationRequestStatus.VERIFIED else VerificationRequestStatus.APPROVED
                    viewModel.onEvent(
                        VerificationRequestsEvent.UpdateTripStatus(
                            request = activeRequest,
                            status = status,
                            resolutionNotes = remarks.takeUnless { it.isBlank() },
                        )
                    )
                },
                onRejectClick = {
                    if (remarks.isBlank()) {
                        remarksError = "Remarks are mandatory for rejecting a request."
                    } else {
                        viewModel.onEvent(
                            VerificationRequestsEvent.UpdateTripStatus(
                                request = activeRequest,
                                status = VerificationRequestStatus.REJECTED,
                                resolutionNotes = remarks,
                            )
                        )
                    }
                },
                onClose = { scope.launch { sheetState.hide() }.invokeOnCompletion { clearSelectedRequest() } },
                actionErrorMessage = uiState.actionErrorMessage,
                isApproveLoading = uiState.actionInProgressRequestId == activeRequest.id &&
                    (uiState.actionInProgressStatus == VerificationRequestStatus.APPROVED ||
                        uiState.actionInProgressStatus == VerificationRequestStatus.VERIFIED),
                isRejectLoading = uiState.actionInProgressRequestId == activeRequest.id &&
                    uiState.actionInProgressStatus == VerificationRequestStatus.REJECTED,
                isCompletedView = isCompletedView,
            )
        }
    }

    if (selectedTruckData != null) {
        val activeTruck = selectedTruckData!!
        val isCompletedView = uiState.selectedStatus == VerificationRequestStatus.COMPLETED
        ModalBottomSheet(
            onDismissRequest = { clearSelectedRequest() },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
        ) {
            TruckVerificationRequestDetailSheet(
                request = activeTruck,
                remarks = remarks,
                remarksError = remarksError,
                onRemarksChange = {
                    remarks = it
                    if (remarksError != null) remarksError = null
                },
                onDocumentClick = { document ->
                    openVerificationDocument(context = context, viewModel = viewModel, document = document)
                },
                resolutionNotes = activeTruck.resolutionNotes,
                onApproveClick = {
                    viewModel.onEvent(
                        VerificationRequestsEvent.UpdateTruckStatus(
                            request = activeTruck,
                            status = VerificationRequestStatus.APPROVED,
                            resolutionNotes = remarks.takeUnless { it.isBlank() },
                        )
                    )
                },
                onRejectClick = {
                    if (remarks.isBlank()) {
                        remarksError = "Remarks are mandatory for rejecting a request."
                    } else {
                        viewModel.onEvent(
                            VerificationRequestsEvent.UpdateTruckStatus(
                                request = activeTruck,
                                status = VerificationRequestStatus.REJECTED,
                                resolutionNotes = remarks,
                            )
                        )
                    }
                },
                onClose = { scope.launch { sheetState.hide() }.invokeOnCompletion { clearSelectedRequest() } },
                actionErrorMessage = uiState.actionErrorMessage,
                isApproveLoading = uiState.actionInProgressRequestId == activeTruck.id &&
                    uiState.actionInProgressStatus == VerificationRequestStatus.APPROVED,
                isRejectLoading = uiState.actionInProgressRequestId == activeTruck.id &&
                    uiState.actionInProgressStatus == VerificationRequestStatus.REJECTED,
                isCompletedView = isCompletedView,
            )
        }
    }

    if (selectedTruckOwnerData != null) {
        val activeOwner = selectedTruckOwnerData!!
        val isCompletedView = uiState.selectedStatus == VerificationRequestStatus.COMPLETED
        ModalBottomSheet(
            onDismissRequest = { clearSelectedRequest() },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
        ) {
            BusinessProfileVerificationDetailSheet(
                entityLabel = "Truck owner",
                title = activeOwner.fullName,
                subtitle = activeOwner.phone.orEmpty().ifBlank { "Phone not on record" },
                status = activeOwner.status,
                changes = activeOwner.changedBusinessProfile,
                remarks = remarks,
                remarksError = remarksError,
                resolutionNotes = activeOwner.resolutionNotes,
                onRemarksChange = {
                    remarks = it
                    if (remarksError != null) remarksError = null
                },
                onApproveClick = {
                    viewModel.onEvent(
                        VerificationRequestsEvent.UpdateTruckOwnerStatus(
                            request = activeOwner,
                            status = VerificationRequestStatus.APPROVED,
                            resolutionNotes = remarks.takeUnless { it.isBlank() },
                        )
                    )
                },
                onRejectClick = {
                    if (remarks.isBlank()) {
                        remarksError = "Remarks are mandatory for rejecting a request."
                    } else {
                        viewModel.onEvent(
                            VerificationRequestsEvent.UpdateTruckOwnerStatus(
                                request = activeOwner,
                                status = VerificationRequestStatus.REJECTED,
                                resolutionNotes = remarks,
                            )
                        )
                    }
                },
                onClose = { scope.launch { sheetState.hide() }.invokeOnCompletion { clearSelectedRequest() } },
                actionErrorMessage = uiState.actionErrorMessage,
                isApproveLoading = uiState.actionInProgressRequestId == activeOwner.id &&
                    uiState.actionInProgressStatus == VerificationRequestStatus.APPROVED,
                isRejectLoading = uiState.actionInProgressRequestId == activeOwner.id &&
                    uiState.actionInProgressStatus == VerificationRequestStatus.REJECTED,
                isCompletedView = isCompletedView,
            )
        }
    }

    if (selectedClientData != null) {
        val activeClient = selectedClientData!!
        val isCompletedView = uiState.selectedStatus == VerificationRequestStatus.COMPLETED
        ModalBottomSheet(
            onDismissRequest = { clearSelectedRequest() },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
        ) {
            BusinessProfileVerificationDetailSheet(
                entityLabel = "Client",
                title = activeClient.companyName,
                subtitle = activeClient.fullName,
                status = activeClient.status,
                changes = activeClient.changedBusinessProfile,
                remarks = remarks,
                remarksError = remarksError,
                resolutionNotes = activeClient.resolutionNotes,
                onRemarksChange = {
                    remarks = it
                    if (remarksError != null) remarksError = null
                },
                onApproveClick = {
                    viewModel.onEvent(
                        VerificationRequestsEvent.UpdateClientStatus(
                            request = activeClient,
                            status = VerificationRequestStatus.APPROVED,
                            resolutionNotes = remarks.takeUnless { it.isBlank() },
                        )
                    )
                },
                onRejectClick = {
                    if (remarks.isBlank()) {
                        remarksError = "Remarks are mandatory for rejecting a request."
                    } else {
                        viewModel.onEvent(
                            VerificationRequestsEvent.UpdateClientStatus(
                                request = activeClient,
                                status = VerificationRequestStatus.REJECTED,
                                resolutionNotes = remarks,
                            )
                        )
                    }
                },
                onClose = { scope.launch { sheetState.hide() }.invokeOnCompletion { clearSelectedRequest() } },
                actionErrorMessage = uiState.actionErrorMessage,
                isApproveLoading = uiState.actionInProgressRequestId == activeClient.id &&
                    uiState.actionInProgressStatus == VerificationRequestStatus.APPROVED,
                isRejectLoading = uiState.actionInProgressRequestId == activeClient.id &&
                    uiState.actionInProgressStatus == VerificationRequestStatus.REJECTED,
                isCompletedView = isCompletedView,
            )
        }
    }
}

// ===========================================================================
// Queue rail
//
// Four tally cells instead of a ScrollableTabRow. The old tab strip stacked a
// red count badge ABOVE the icon, which (a) pushed every counted tab down and
// left "Clients" — the only zero-count tab — sitting a few dp higher than its
// neighbours, and (b) made the row 96dp tall for no information gain.
//
// The rail is a fixed four-up grid rather than a scroller: a queue you have to
// swipe sideways to discover is a queue with a hidden tab, and "Clients" is
// exactly the tab that gets lost. Every cell is the same width and height, and
// the tally always renders — including 0.
// ===========================================================================

private fun VerificationRequestTab.railLabel(): String = when (this) {
    VerificationRequestTab.TRIPS -> "Trips"
    VerificationRequestTab.TRUCKS -> "Trucks"
    VerificationRequestTab.TRUCK_OWNERS -> "Owners"
    VerificationRequestTab.CLIENTS -> "Clients"
}

@Composable
private fun VerificationQueueRail(
    selectedTab: VerificationRequestTab,
    onTabSelected: (VerificationRequestTab) -> Unit,
    tripCount: Int,
    truckCount: Int,
    truckOwnerCount: Int,
    clientCount: Int,
) {
    val counts = mapOf(
        VerificationRequestTab.TRIPS to tripCount,
        VerificationRequestTab.TRUCKS to truckCount,
        VerificationRequestTab.TRUCK_OWNERS to truckOwnerCount,
        VerificationRequestTab.CLIENTS to clientCount,
    )
    val icons = mapOf(
        VerificationRequestTab.TRIPS to Icons.Default.Route,
        VerificationRequestTab.TRUCKS to Icons.Default.LocalShipping,
        VerificationRequestTab.TRUCK_OWNERS to Icons.Default.People,
        VerificationRequestTab.CLIENTS to Icons.Default.Business,
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            VerificationRequestTab.entries.forEach { tab ->
                QueueCell(
                    label = tab.railLabel(),
                    testTag = "verification_tab_${tab.name.lowercase(Locale.getDefault())}",
                    icon = icons[tab] ?: Icons.Default.Route,
                    count = counts[tab] ?: 0,
                    isSelected = tab == selectedTab,
                    onClick = { onTabSelected(tab) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
        HorizontalDivider(
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f),
        )
    }
}

@Composable
private fun QueueCell(
    label: String,
    testTag: String,
    icon: ImageVector,
    count: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val container = if (isSelected) scheme.primaryContainer else Color.Transparent
    val content = if (isSelected) scheme.onPrimaryContainer else scheme.onSurfaceVariant
    val outline = if (isSelected) scheme.primary.copy(alpha = 0.32f) else scheme.outlineVariant.copy(alpha = 0.8f)
    val tallyContainer = if (isSelected) scheme.primary else scheme.surfaceContainerHighest
    val tallyContent = if (isSelected) scheme.onPrimary else scheme.onSurfaceVariant

    Surface(
        modifier = modifier
            .heightIn(min = 56.dp)
            .testTag(testTag)
            .semantics { testTagsAsResourceId = true }
            .selectable(selected = isSelected, onClick = onClick, role = Role.Tab),
        shape = PanelShape,
        color = container,
        border = BorderStroke(1.dp, outline),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 9.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = content,
                )
                Spacer(Modifier.width(6.dp))
                Surface(
                    shape = TagShape,
                    color = tallyContainer,
                ) {
                    Text(
                        text = count.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = tallyContent,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp),
                    )
                }
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = content,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

// ===========================================================================
// Status filters
//
// Each chip leads with a status dot in the semantic status colour, so the
// filter row can be read by colour before it is read by text. Pending is amber,
// in-review is the info teal, completed is the brand green — all sourced from
// the theme, never hardcoded.
// ===========================================================================

@Composable
private fun VerificationStatusFilters(
    tabType: String,
    selectedStatus: VerificationRequestStatus,
    onStatusSelected: (VerificationRequestStatus) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        VerificationRequestStatus.filterValues(tabType).forEach { status ->
            StatusFilterChip(
                status = status,
                isSelected = selectedStatus == status,
                onClick = { onStatusSelected(status) },
            )
        }
    }
}

@Composable
private fun StatusFilterChip(
    status: VerificationRequestStatus,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val accent = statusAccentColor(status)
    val container = if (isSelected) accent.copy(alpha = 0.14f) else Color.Transparent
    val outline = if (isSelected) accent.copy(alpha = 0.55f) else scheme.outlineVariant
    val content = if (isSelected) scheme.onSurface else scheme.onSurfaceVariant

    Surface(
        modifier = Modifier
            .heightIn(min = 44.dp)
            .testTag("verification_filter_${status.apiValue.lowercase(Locale.getDefault())}")
            .semantics { testTagsAsResourceId = true }
            .selectable(selected = isSelected, onClick = onClick, role = Role.RadioButton),
        shape = PanelShape,
        color = container,
        border = BorderStroke(1.dp, outline),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(accent, CircleShape),
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = status.displayText.uppercase(Locale.getDefault()),
                style = ledgerLabel(),
                fontWeight = FontWeight.Bold,
                color = content,
                maxLines = 1,
            )
        }
    }
}

// ===========================================================================
// The little `›` affordance at the end of a docket footer.
// ===========================================================================

@Composable
private fun ReviewCue() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = "Review",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.primary,
        )
    }
}

/**
 * Decided requests need their outcome visible in the list. Under the Pending
 * and In-Review filters the chip already says the state for every row, so the
 * stamp is suppressed there; under Completed the list mixes approved, verified
 * and rejected items and each card has to carry its own verdict.
 */
private fun VerificationRequestStatus.isDecided(): Boolean =
    this == VerificationRequestStatus.APPROVED ||
        this == VerificationRequestStatus.VERIFIED ||
        this == VerificationRequestStatus.REJECTED

@Composable
private fun DocketFooter(
    status: VerificationRequestStatus,
    metaText: String,
    metaIcon: ImageVector? = null,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (status.isDecided()) {
                StatusStamp(status = status)
                Spacer(Modifier.width(8.dp))
            }
            if (metaIcon != null) {
                Icon(
                    imageVector = metaIcon,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.width(6.dp))
            }
            Text(
                text = metaText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(Modifier.width(8.dp))
        ReviewCue()
    }
}

/** Amber / teal / green / red marker that says "new record", not "edit". */
@Composable
private fun OnboardingTag() {
    MetaTag(
        text = "NEW ONBOARDING",
        container = MaterialTheme.colorScheme.tertiaryContainer,
        content = MaterialTheme.colorScheme.onTertiaryContainer,
        hatch = true,
    )
}

@Composable
private fun UpdateTag() {
    MetaTag(
        text = "UPDATE",
        container = MaterialTheme.colorScheme.surfaceContainerHighest,
        content = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun AgeStamp(updatedAt: String) {
    val label = remember(updatedAt) { formatRelativeTime(updatedAt) }
    Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surfaceContainerHigh, TagShape)
            .padding(horizontal = 8.dp, vertical = 4.dp),
    )
}

// ===========================================================================
// Trip docket
// ===========================================================================

@Composable
private fun TripDocketCard(
    request: VerificationRequest,
    onClick: () -> Unit,
) {
    val accent = statusAccentColor(request.status)
    val documentLabel = remember(request.documents) {
        val names = request.documents.map { displayDocumentName(it.name, true) }.distinct()
        when {
            names.isEmpty() -> "No documents attached"
            names.size <= 2 -> names.joinToString(" + ")
            else -> "${names.take(2).joinToString(" + ")} +${names.size - 2}"
        }
    }
    val documentCount = request.documents.size

    DocketCard(
        accent = accent,
        testTag = "verification_card_trip_${request.tripCode}",
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = request.tripCode,
                style = identifier(),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.width(10.dp))
            AgeStamp(updatedAt = request.updatedAt)
        }

        Spacer(Modifier.height(10.dp))

        Text(
            text = request.truckRegistrationNumber,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        Spacer(Modifier.height(8.dp))

        LaneStrip(
            from = request.origin.displayLabel(),
            to = request.destination.displayLabel(),
        )

        Spacer(Modifier.height(12.dp))
        PerforationRule()
        Spacer(Modifier.height(11.dp))

        DocketFooter(
            status = request.status,
            metaIcon = Icons.Default.Description,
            metaText = if (documentCount == 1) {
                "$documentLabel · 1 doc"
            } else {
                "$documentLabel · $documentCount docs"
            },
        )
    }
}

/**
 * Compact horizontal version of the app's route language: origin ring, dashed
 * lane, destination dot. The vertical [RouteIndicatorWithCities] stays for
 * detail views; this is its card-sized sibling.
 */
@Composable
private fun LaneStrip(
    from: String,
    to: String,
    modifier: Modifier = Modifier,
) {
    val ext = FreightThemeTokens.colors
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(9.dp)
                .border(2.dp, ext.routeOrigin, CircleShape),
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = from,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.widthIn(max = 112.dp),
        )
        Spacer(Modifier.width(8.dp))
        Canvas(
            modifier = Modifier
                .weight(1f)
                .height(8.dp),
        ) {
            drawLine(
                color = ext.routeDestination.copy(alpha = 0.45f),
                start = Offset(0f, size.height / 2f),
                end = Offset(size.width, size.height / 2f),
                strokeWidth = 2f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 5f), 0f),
            )
        }
        Spacer(Modifier.width(8.dp))
        Text(
            text = to,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.widthIn(max = 112.dp),
        )
        Spacer(Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .size(9.dp)
                .background(ext.routeDestination, CircleShape),
        )
    }
}

// ===========================================================================
// Truck docket
// ===========================================================================

@Composable
private fun TruckDocketCard(
    request: TruckVerificationRequest,
    onClick: () -> Unit,
) {
    val accent = statusAccentColor(request.status)
    val entries = remember(request.changedTruckDetails) { request.changedTruckDetails.map { it.toLedgerEntry() } }
    val onboarding = entries.isOnboarding()
    val preview = entries.take(2)
    val remaining = (entries.size - preview.size).coerceAtLeast(0)

    DocketCard(
        accent = accent,
        testTag = "verification_card_truck_${request.registrationNumber}",
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = request.registrationNumber,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.width(10.dp))
            AgeStamp(updatedAt = request.updatedAt)
        }

        Spacer(Modifier.height(10.dp))

        if (onboarding) OnboardingTag() else UpdateTag()

        Spacer(Modifier.height(10.dp))

        if (preview.isEmpty()) {
            Text(
                text = "No field changes on this request",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            preview.forEachIndexed { index, entry ->
                if (index > 0) Spacer(Modifier.height(6.dp))
                LedgerPreviewRow(entry = entry, isNew = entry.kind == ChangeKind.NEW)
            }
            if (remaining > 0) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "+$remaining more field${if (remaining == 1) "" else "s"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Spacer(Modifier.height(12.dp))
        PerforationRule()
        Spacer(Modifier.height(11.dp))

        DocketFooter(
            status = request.status,
            metaText = buildMetaLine(
                fields = entries.size,
                documents = request.changedDocuments.size,
            ),
        )
    }
}

// ===========================================================================
// Truck-owner / client docket
// ===========================================================================

@Composable
private fun BusinessDocketCard(
    eyebrowText: String,
    testTag: String,
    title: String,
    contact: String,
    updatedAt: String,
    status: VerificationRequestStatus,
    icon: ImageVector,
    changes: List<LedgerEntry>,
    onClick: () -> Unit,
) {
    val accent = statusAccentColor(status)
    val onboarding = changes.isOnboarding()
    val preview = changes.take(2)
    val remaining = (changes.size - preview.size).coerceAtLeast(0)

    DocketCard(accent = accent, testTag = testTag, onClick = onClick) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.width(10.dp))
            AgeStamp(updatedAt = updatedAt)
        }

        Spacer(Modifier.height(8.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = eyebrowText,
                style = ledgerLabel(),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = "·",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline,
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = contact,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        Spacer(Modifier.height(10.dp))

        if (onboarding) OnboardingTag() else UpdateTag()

        Spacer(Modifier.height(10.dp))

        if (preview.isEmpty()) {
            Text(
                text = "No field changes on this request",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            preview.forEachIndexed { index, entry ->
                if (index > 0) Spacer(Modifier.height(6.dp))
                LedgerPreviewRow(entry = entry, isNew = entry.kind == ChangeKind.NEW)
            }
            if (remaining > 0) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "+$remaining more field${if (remaining == 1) "" else "s"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Spacer(Modifier.height(12.dp))
        PerforationRule()
        Spacer(Modifier.height(11.dp))

        DocketFooter(
            status = status,
            metaText = "${changes.size} field${if (changes.size == 1) "" else "s"} submitted",
        )
    }
}

/** One line of the card-level ledger preview: label on the left, value right. */
@Composable
private fun LedgerPreviewRow(entry: LedgerEntry, isNew: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Fixed label column so every value in the preview starts on the same
        // x — that alignment is what makes it read as a ledger rather than a
        // stack of loose label/value pairs.
        Text(
            text = formatFieldName(entry.field),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.width(128.dp),
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = entry.displayAfter(),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isNew) FontWeight.SemiBold else FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
    }
}

// ===========================================================================
// Bottom sheets
// ===========================================================================

/**
 * Shared sheet chrome: eyebrow + title + status stamp + meta tags, a tear line,
 * the scrollable body, and — when the request is actionable — a pinned action
 * bar so Approve/Reject are reachable without scrolling past every field.
 */
@Composable
private fun DocketSheet(
    entityLabel: String,
    title: String,
    subtitle: String? = null,
    status: VerificationRequestStatus,
    metaTags: List<String> = emptyList(),
    showActions: Boolean,
    actionErrorMessage: String?,
    isApproveLoading: Boolean,
    isRejectLoading: Boolean,
    approveLabel: String = "Approve",
    onApprove: () -> Unit,
    onReject: () -> Unit,
    onClose: () -> Unit,
    body: @Composable ColumnScope.() -> Unit,
) {
    val accent = statusAccentColor(status)
    val barHeight = 84.dp

    Box(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = if (showActions) barHeight + 12.dp else 28.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = entityLabel.uppercase(Locale.getDefault()),
                        style = eyebrow(),
                        color = accent,
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    if (!subtitle.isNullOrBlank()) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        StatusStamp(status = status)
                        metaTags.forEach { MetaTag(text = it, container = MaterialTheme.colorScheme.surfaceContainerHighest, content = MaterialTheme.colorScheme.onSurfaceVariant) }
                    }
                }
                IconButton(
                    onClick = onClose,
                    modifier = Modifier
                        .testTag("verification_sheet_close")
                        .semantics { testTagsAsResourceId = true },
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(Modifier.height(18.dp))
            PerforationRule()
            Spacer(Modifier.height(18.dp))

            body()

            Spacer(Modifier.height(12.dp))
        }

        if (showActions) {
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 12.dp,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                ) {
                    if (actionErrorMessage != null) {
                        InlineNote(
                            text = actionErrorMessage,
                            container = MaterialTheme.colorScheme.errorContainer,
                            content = MaterialTheme.colorScheme.onErrorContainer,
                        )
                        Spacer(Modifier.height(10.dp))
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        ActionButton(
                            modifier = Modifier.weight(1f),
                            testTag = "verification_sheet_reject",
                            text = "Reject",
                            icon = Icons.Default.Close,
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.24f)),
                            isLoading = isRejectLoading,
                            onClick = onReject,
                        )
                        ActionButton(
                            modifier = Modifier.weight(1f),
                            testTag = "verification_sheet_approve",
                            text = approveLabel,
                            icon = Icons.Default.CheckCircle,
                            containerColor = FreightThemeTokens.colors.success,
                            contentColor = FreightThemeTokens.colors.onSuccess,
                            border = null,
                            isLoading = isApproveLoading,
                            onClick = onApprove,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TripVerificationRequestDetailSheet(
    request: VerificationRequest,
    remarks: String,
    remarksError: String?,
    resolutionNotes: String?,
    onRemarksChange: (String) -> Unit,
    onDocumentClick: (VerificationRequestDocument) -> Unit,
    onApproveClick: () -> Unit,
    onRejectClick: () -> Unit,
    onClose: () -> Unit,
    actionErrorMessage: String?,
    isApproveLoading: Boolean,
    isRejectLoading: Boolean,
    isCompletedView: Boolean,
) {
    DocketSheet(
        entityLabel = "Trip document review",
        title = request.truckRegistrationNumber,
        subtitle = request.tripCode,
        status = request.status,
        metaTags = listOf(
            if (request.documents.size == 1) "1 DOC" else "${request.documents.size} DOCS",
        ),
        showActions = !isCompletedView,
        actionErrorMessage = actionErrorMessage,
        isApproveLoading = isApproveLoading,
        isRejectLoading = isRejectLoading,
        approveLabel = if (request.documents.any { it.name.trim().lowercase() == "lr" }) "Verify" else "Approve",
        onApprove = onApproveClick,
        onReject = onRejectClick,
        onClose = onClose,
    ) {
        LedgerSectionLabel(text = "Lane")
        Spacer(Modifier.height(10.dp))
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = PanelShape,
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                RouteIndicatorWithCities(
                    fromCity = request.origin.displayLabel(),
                    toCity = request.destination.displayLabel(),
                    fromLabel = "Pickup",
                    toLabel = "Drop",
                )
            }
        }

        Spacer(Modifier.height(22.dp))

        LedgerSectionLabel(text = "Documents")
        Spacer(Modifier.height(10.dp))
        if (request.documents.isEmpty()) {
            EmptyLedgerNote(text = "No documents were attached to this request.")
        } else {
            request.documents.forEachIndexed { index, document ->
                if (index > 0) Spacer(Modifier.height(8.dp))
                DocumentLedgerRow(
                    document = document,
                    uploadDate = formatDisplayDate(request.updatedAt),
                    onClick = onDocumentClick,
                )
            }
        }

        Spacer(Modifier.height(22.dp))

        LedgerSectionLabel(text = if (isCompletedView) "Resolution" else "Remarks")
        Spacer(Modifier.height(10.dp))
        RemarksSection(
            isCompletedView = isCompletedView,
            resolutionNotes = resolutionNotes,
            remarks = remarks,
            remarksError = remarksError,
            onRemarksChange = onRemarksChange,
        )
    }
}

@Composable
private fun TruckVerificationRequestDetailSheet(
    request: TruckVerificationRequest,
    remarks: String,
    remarksError: String?,
    resolutionNotes: String?,
    onRemarksChange: (String) -> Unit,
    onDocumentClick: (VerificationRequestDocument) -> Unit,
    onApproveClick: () -> Unit,
    onRejectClick: () -> Unit,
    onClose: () -> Unit,
    actionErrorMessage: String?,
    isApproveLoading: Boolean,
    isRejectLoading: Boolean,
    isCompletedView: Boolean,
) {
    val entries = remember(request.changedTruckDetails) { request.changedTruckDetails.map { it.toLedgerEntry() } }
    val onboarding = entries.isOnboarding()

    DocketSheet(
        entityLabel = if (onboarding) "Truck onboarding" else "Truck detail update",
        title = request.registrationNumber,
        status = request.status,
        metaTags = buildList {
            add("${entries.size} FIELD${if (entries.size == 1) "" else "S"}")
            if (request.changedDocuments.isNotEmpty()) {
                add("${request.changedDocuments.size} DOC${if (request.changedDocuments.size == 1) "" else "S"}")
            }
        },
        showActions = !isCompletedView,
        actionErrorMessage = actionErrorMessage,
        isApproveLoading = isApproveLoading,
        isRejectLoading = isRejectLoading,
        onApprove = onApproveClick,
        onReject = onRejectClick,
        onClose = onClose,
    ) {
        if (entries.isNotEmpty()) {
            LedgerSectionLabel(
                text = if (onboarding) "New details submitted" else "Field changes",
                hatch = onboarding,
            )
            Spacer(Modifier.height(10.dp))
            entries.forEachIndexed { index, entry ->
                if (index > 0) Spacer(Modifier.height(8.dp))
                ChangeLedgerRow(entry = entry, showNewTag = !onboarding)
            }
            Spacer(Modifier.height(22.dp))
        }

        if (request.changedDocuments.isNotEmpty()) {
            LedgerSectionLabel(text = "Documents")
            Spacer(Modifier.height(10.dp))
            request.changedDocuments.forEachIndexed { index, document ->
                if (index > 0) Spacer(Modifier.height(8.dp))
                DocumentLedgerRow(
                    document = document,
                    uploadDate = formatDisplayDate(request.updatedAt),
                    onClick = onDocumentClick,
                )
            }
            Spacer(Modifier.height(22.dp))
        }

        if (entries.isEmpty() && request.changedDocuments.isEmpty()) {
            EmptyLedgerNote(text = "Nothing changed on this request.")
            Spacer(Modifier.height(22.dp))
        }

        LedgerSectionLabel(text = if (isCompletedView) "Resolution" else "Remarks")
        Spacer(Modifier.height(10.dp))
        RemarksSection(
            isCompletedView = isCompletedView,
            resolutionNotes = resolutionNotes,
            remarks = remarks,
            remarksError = remarksError,
            onRemarksChange = onRemarksChange,
        )
    }
}

@Composable
private fun BusinessProfileVerificationDetailSheet(
    entityLabel: String,
    title: String,
    subtitle: String,
    status: VerificationRequestStatus,
    changes: List<BusinessProfileChange>,
    remarks: String,
    remarksError: String?,
    resolutionNotes: String?,
    onRemarksChange: (String) -> Unit,
    onApproveClick: () -> Unit,
    onRejectClick: () -> Unit,
    onClose: () -> Unit,
    actionErrorMessage: String?,
    isApproveLoading: Boolean,
    isRejectLoading: Boolean,
    isCompletedView: Boolean,
) {
    val entries = remember(changes) { changes.map { it.toLedgerEntry() } }
    val onboarding = entries.isOnboarding()

    DocketSheet(
        entityLabel = if (onboarding) "$entityLabel onboarding" else "$entityLabel profile update",
        title = title,
        subtitle = subtitle,
        status = status,
        metaTags = listOf("${entries.size} FIELD${if (entries.size == 1) "" else "S"}"),
        showActions = !isCompletedView,
        actionErrorMessage = actionErrorMessage,
        isApproveLoading = isApproveLoading,
        isRejectLoading = isRejectLoading,
        onApprove = onApproveClick,
        onReject = onRejectClick,
        onClose = onClose,
    ) {
        if (entries.isEmpty()) {
            EmptyLedgerNote(text = "No profile changes on this request.")
            Spacer(Modifier.height(22.dp))
        } else {
            LedgerSectionLabel(
                text = if (onboarding) "New details submitted" else "Field changes",
                hatch = onboarding,
            )
            Spacer(Modifier.height(10.dp))
            entries.forEachIndexed { index, entry ->
                if (index > 0) Spacer(Modifier.height(8.dp))
                ChangeLedgerRow(entry = entry, showNewTag = !onboarding)
            }
            Spacer(Modifier.height(22.dp))
        }

        LedgerSectionLabel(text = if (isCompletedView) "Resolution" else "Remarks")
        Spacer(Modifier.height(10.dp))
        RemarksSection(
            isCompletedView = isCompletedView,
            resolutionNotes = resolutionNotes,
            remarks = remarks,
            remarksError = remarksError,
            onRemarksChange = onRemarksChange,
        )
    }
}

// ===========================================================================
// Ledger primitives
// ===========================================================================

@Composable
private fun LedgerSectionLabel(
    text: String,
    hatch: Boolean = false,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (hatch) {
            Surface(
                shape = TagShape,
                color = MaterialTheme.colorScheme.tertiaryContainer,
            ) {
                HatchMark(
                    modifier = Modifier
                        .size(width = 14.dp, height = 16.dp),
                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.55f),
                )
            }
            Spacer(Modifier.width(8.dp))
        }
        Text(
            text = text.uppercase(Locale.getDefault()),
            style = ledgerLabel(),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/**
 * One field, one row.
 *
 * This is the fix for the old sheet, which printed a literal "N/A" under a
 * "Before" heading for every field of a brand-new truck or profile. A new
 * record has no before — so the row shows only what was submitted. Genuine
 * edits read as a single wrapping `was → now` line, and a field that was
 * cleared says REMOVED. The Before/After column headings are gone; they were
 * repeated six times per sheet and carried no information.
 *
 * [showNewTag] is false when the whole request is an onboarding: five identical
 * NEW stamps stacked down a sheet read as a rendering artefact, not as five
 * facts. The eyebrow ("TRUCK ONBOARDING") and the hatched section label already
 * say it once, which is the right number of times.
 */
@Composable
private fun ChangeLedgerRow(
    entry: LedgerEntry,
    showNewTag: Boolean = true,
) {
    val scheme = MaterialTheme.colorScheme
    val accent = statusAccentColor(VerificationRequestStatus.PENDING)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = PanelShape,
        color = scheme.surfaceContainerLow,
        border = BorderStroke(1.dp, scheme.outlineVariant.copy(alpha = 0.5f)),
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = formatFieldName(entry.field),
                    style = ledgerLabel(),
                    color = scheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                when (entry.kind) {
                    ChangeKind.NEW -> if (showNewTag) {
                        Spacer(Modifier.width(8.dp))
                        MetaTag(
                            text = "NEW",
                            container = scheme.tertiaryContainer,
                            content = scheme.onTertiaryContainer,
                        )
                    }
                    ChangeKind.CLEARED -> {
                        Spacer(Modifier.width(8.dp))
                        MetaTag(
                            text = "REMOVED",
                            container = scheme.errorContainer,
                            content = scheme.onErrorContainer,
                        )
                    }
                    else -> Unit
                }
            }

            Spacer(Modifier.height(7.dp))

            when (entry.kind) {
                ChangeKind.NEW -> {
                    Text(
                        text = entry.displayAfter(),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = scheme.onSurface,
                    )
                }
                ChangeKind.UPDATED -> {
                    // One wrapping line: struck-through old value, arrow, new value.
                    Text(
                        text = buildAnnotatedString {
                            withStyle(
                                SpanStyle(
                                    color = scheme.onSurfaceVariant,
                                    textDecoration = TextDecoration.LineThrough,
                                )
                            ) { append(entry.displayBefore()) }
                            withStyle(
                                SpanStyle(
                                    color = accent,
                                    fontWeight = FontWeight.Bold,
                                )
                            ) { append("   \u2192   ") }
                            withStyle(
                                SpanStyle(
                                    color = scheme.onSurface,
                                    fontWeight = FontWeight.SemiBold,
                                )
                            ) { append(entry.displayAfter()) }
                        },
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
                ChangeKind.CLEARED -> {
                    Text(
                        text = entry.displayBefore(),
                        style = MaterialTheme.typography.bodyLarge,
                        color = scheme.onSurfaceVariant,
                        textDecoration = TextDecoration.LineThrough,
                    )
                }
                ChangeKind.SAME -> {
                    Text(
                        text = entry.displayAfter(),
                        style = MaterialTheme.typography.bodyLarge,
                        color = scheme.onSurface,
                    )
                }
            }
        }
    }
}

@Composable
private fun DocumentLedgerRow(
    document: VerificationRequestDocument,
    uploadDate: String,
    onClick: (VerificationRequestDocument) -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = PanelShape,
        color = scheme.surfaceContainerLow,
        border = BorderStroke(1.dp, scheme.outlineVariant.copy(alpha = 0.5f)),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(scheme.primaryContainer, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.Description,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = scheme.onPrimaryContainer,
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = displayDocumentName(document.name, false),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = scheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    text = "Uploaded $uploadDate",
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(Modifier.width(8.dp))
            if (!document.s3Url.isNullOrBlank()) {
                DocumentLinkChip(onClick = { onClick(document) })
            } else {
                MetaTag(
                    text = "UNAVAILABLE",
                    container = scheme.surfaceContainerHighest,
                    content = scheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun StatusStamp(status: VerificationRequestStatus) {
    val (container, content) = statusStampColors(status)
    val glyph = when (status) {
        VerificationRequestStatus.REJECTED -> Icons.Default.Close
        VerificationRequestStatus.APPROVED,
        VerificationRequestStatus.VERIFIED -> Icons.Default.Check
        else -> null
    }

    Surface(
        shape = TagShape,
        color = container,
    ) {
        Row(
            modifier = Modifier
                .height(22.dp)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (glyph != null) {
                Icon(
                    imageVector = glyph,
                    contentDescription = null,
                    modifier = Modifier.size(12.dp),
                    tint = content,
                )
                Spacer(Modifier.width(4.dp))
            }
            Text(
                text = status.displayText.uppercase(Locale.getDefault()),
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                fontWeight = FontWeight.Bold,
                color = content,
            )
        }
    }
}

@Composable
private fun RemarksSection(
    isCompletedView: Boolean,
    resolutionNotes: String?,
    remarks: String,
    remarksError: String?,
    onRemarksChange: (String) -> Unit,
) {
    if (isCompletedView) {
        val note = resolutionNotes?.trim().orEmpty()
        if (note.isBlank()) {
            EmptyLedgerNote(text = "No remarks were recorded for this request.")
        } else {
            Row(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .height(IntrinsicSize.Min)
                        .background(MaterialTheme.colorScheme.primary, TagShape),
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = note,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
        return
    }

    Column {
        OutlinedTextField(
            value = remarks,
            onValueChange = onRemarksChange,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("verification_sheet_remarks")
                .semantics { testTagsAsResourceId = true },
            minLines = 2,
            maxLines = 4,
            placeholder = {
                Text("Why you approved or rejected this request…")
            },
            textStyle = MaterialTheme.typography.bodyMedium,
            shape = PanelShape,
            isError = remarksError != null,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                errorBorderColor = MaterialTheme.colorScheme.error,
                cursorColor = MaterialTheme.colorScheme.primary,
                focusedLabelColor = MaterialTheme.colorScheme.primary,
                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                errorContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            ),
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = remarksError ?: "Required when rejecting. Optional when approving.",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (remarksError != null) FontWeight.Medium else FontWeight.Normal,
            color = if (remarksError != null) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
        )
    }
}

@Composable
private fun ActionButton(
    modifier: Modifier = Modifier,
    testTag: String,
    text: String,
    icon: ImageVector,
    containerColor: Color,
    contentColor: Color,
    isLoading: Boolean,
    border: BorderStroke? = null,
    onClick: () -> Unit,
) {
    Button(
        modifier = modifier
            .heightIn(min = 52.dp)
            .testTag(testTag)
            .semantics { testTagsAsResourceId = true },
        onClick = onClick,
        enabled = !isLoading,
        shape = PanelShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = containerColor.copy(alpha = 0.65f),
            disabledContentColor = contentColor.copy(alpha = 0.75f),
        ),
        border = border,
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 14.dp),
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                strokeWidth = 2.dp,
                color = contentColor,
            )
        } else {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(18.dp),
            )
        }
        Spacer(Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = contentColor,
        )
    }
}

@Composable
private fun InlineNote(
    text: String,
    container: Color,
    content: Color,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = container,
        shape = PanelShape,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = content,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
        )
    }
}

@Composable
private fun EmptyLedgerNote(text: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = PanelShape,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(14.dp),
        )
    }
}

// ===========================================================================
// Loading / empty / error
// ===========================================================================

@Composable
private fun LoadingDockets() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 96.dp),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun VerificationEmptyState(
    icon: ImageVector,
    title: String,
    subtitle: String,
    status: VerificationRequestStatus,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 64.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        FreightEmptyState(
            icon = icon,
            title = title,
            subtitle = subtitle,
        )
        Spacer(Modifier.height(14.dp))
        MetaTag(
            text = "SHOWING ${status.displayText.uppercase(Locale.getDefault())}",
            container = MaterialTheme.colorScheme.surfaceContainerHigh,
            content = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun VerificationErrorState(
    message: String,
    onRetry: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 72.dp, start = 24.dp, end = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        FreightEmptyState(
            icon = Icons.Default.Close,
            title = "Could not load the queue",
            subtitle = message,
        )
        Spacer(Modifier.height(14.dp))
        Button(
            onClick = onRetry,
            shape = PanelShape,
        ) {
            Text(
                text = "Retry",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

// ===========================================================================
// Ledger model + formatting
// ===========================================================================

private enum class ChangeKind { NEW, UPDATED, CLEARED, SAME }

private data class LedgerEntry(
    val field: String,
    val before: String?,
    val after: String?,
    val kind: ChangeKind,
) {
    fun displayBefore(): String = humanizeValue(before)
    fun displayAfter(): String = humanizeValue(after)
}

private fun TruckDetailChange.toLedgerEntry(): LedgerEntry = LedgerEntry(
    field = displayFieldName.ifBlank { fieldName },
    before = beforeValue,
    after = afterValue,
    kind = changeKind(beforeValue, afterValue),
)

private fun BusinessProfileChange.toLedgerEntry(): LedgerEntry = LedgerEntry(
    field = displayFieldName.ifBlank { fieldName },
    before = beforeValue,
    after = afterValue,
    kind = changeKind(beforeValue, afterValue),
)

/** A snapshot that never had the key is "new", not "changed from N/A". */
private fun normalized(value: String?): String? {
    val trimmed = value?.trim().orEmpty()
    return trimmed.takeUnless {
        it.isBlank() || it.equals("null", ignoreCase = true) || it.equals("N/A", ignoreCase = true)
    }
}

private fun changeKind(before: String?, after: String?): ChangeKind {
    val b = normalized(before)
    val a = normalized(after)
    return when {
        b == null && a == null -> ChangeKind.SAME
        b == null -> ChangeKind.NEW
        a == null -> ChangeKind.CLEARED
        b == a -> ChangeKind.SAME
        else -> ChangeKind.UPDATED
    }
}

/**
 * A request is an onboarding when every submitted field is new — i.e. there was
 * no prior record at all. That is what flips the sheet from "changes" to "new
 * details submitted" and what puts the NEW ONBOARDING marker on the card.
 */
private fun List<LedgerEntry>.isOnboarding(): Boolean =
    isNotEmpty() && all { it.kind == ChangeKind.NEW }

private fun humanizeValue(value: String?): String {
    val trimmed = value?.trim().orEmpty()
    return when (trimmed.lowercase(Locale.getDefault())) {
        "", "null" -> "—"
        "true" -> "Yes"
        "false" -> "No"
        else -> trimmed
    }
}

private fun buildMetaLine(fields: Int, documents: Int): String {
    val fieldPart = "$fields field${if (fields == 1) "" else "s"}"
    if (documents == 0) return fieldPart
    return "$fieldPart · $documents doc${if (documents == 1) "" else "s"}"
}

private val FIELD_ACRONYMS = setOf(
    "id", "tds", "pan", "gst", "gstin", "rc", "fc", "lr", "pod", "dob",
    "kyc", "ifsc", "upi", "emi", "doj", "hsn", "sac",
)

private fun formatFieldName(fieldName: String): String {
    return fieldName
        .replace('_', ' ')
        .replace('-', ' ')
        .trim()
        .split(Regex("\\s+"))
        .filter { it.isNotBlank() }
        .joinToString(" ") { word ->
            if (word.lowercase(Locale.getDefault()) in FIELD_ACRONYMS) {
                word.uppercase(Locale.getDefault())
            } else {
                word.replaceFirstChar { c ->
                    if (c.isLowerCase()) c.titlecase(Locale.getDefault()) else c.toString()
                }
            }
        }
        .ifBlank { fieldName }
}

@Composable
private fun statusAccentColor(status: VerificationRequestStatus): Color {
    val ext = FreightThemeTokens.colors
    return when (status) {
        VerificationRequestStatus.PENDING -> ext.warning
        VerificationRequestStatus.IN_REVIEW -> ext.info
        VerificationRequestStatus.APPROVED,
        VerificationRequestStatus.VERIFIED -> ext.success
        VerificationRequestStatus.REJECTED -> MaterialTheme.colorScheme.error
        VerificationRequestStatus.COMPLETED -> MaterialTheme.colorScheme.primary
    }
}

@Composable
private fun statusStampColors(status: VerificationRequestStatus): Pair<Color, Color> {
    val ext = FreightThemeTokens.colors
    val scheme = MaterialTheme.colorScheme
    return when (status) {
        VerificationRequestStatus.PENDING -> ext.surfaceWarning to ext.onSurfaceWarning
        VerificationRequestStatus.IN_REVIEW -> ext.surfaceInfo to ext.onSurfaceInfo
        VerificationRequestStatus.APPROVED,
        VerificationRequestStatus.VERIFIED -> ext.surfaceSuccess to ext.onSurfaceSuccess
        VerificationRequestStatus.REJECTED -> scheme.errorContainer to scheme.onErrorContainer
        VerificationRequestStatus.COMPLETED -> scheme.primaryContainer to scheme.onPrimaryContainer
    }
}

private fun openVerificationDocument(
    context: Context,
    viewModel: VerificationRequestsViewModel,
    document: VerificationRequestDocument,
) {
    if (document.s3Url.isNullOrBlank()) return
    viewModel.getSignedUrl(document) { signedUrl ->
        context.startActivity(getPdfLaunchIntent(signedUrl, context))
    }
}

@RequiresApi(Build.VERSION_CODES.O)
private fun formatRelativeTime(updatedAt: String): String {
    return runCatching {
        val instant = OffsetDateTime.parse(updatedAt).toInstant()
        DateUtils.getRelativeTimeSpanString(
            instant.toEpochMilli(),
            System.currentTimeMillis(),
            DateUtils.MINUTE_IN_MILLIS,
        ).toString()
    }.getOrElse { updatedAt }
}

@RequiresApi(Build.VERSION_CODES.O)
private fun formatDisplayDate(updatedAt: String): String {
    return runCatching {
        val instant = OffsetDateTime.parse(updatedAt).toInstant()
        DateFormat.getDateTimeInstance(
            DateFormat.MEDIUM,
            DateFormat.SHORT,
            Locale.getDefault(),
        ).format(Date.from(instant))
    }.getOrElse { updatedAt }
}

private fun VerificationRequestLocation?.displayLabel(): String {
    return this?.displayText().orEmpty().ifBlank { "N/A" }
}

private val DOCUMENT_ACRONYMS = setOf(
    "rc", "fc", "lr", "pod", "pan", "gst", "gstin", "kyc", "noc", "coi",
    "tds", "emi", "ifsc", "upi", "aadhar", "aadhaar", "hsn", "sac",
)

private fun displayDocumentName(name: String, shortform: Boolean): String {
    val normalized = name.trim().lowercase(Locale.getDefault())
    return when (normalized) {
        "lr" -> if (shortform) "LR" else "Loading Receipt"
        "pod" -> if (shortform) "POD" else "Proof Of Delivery"
        else -> {
            // The API hands back free-form keys ("rc card", "fc certificate",
            // "insurance"). Print them as real document names rather than
            // passing the raw lowercase key through to the reviewer.
            val titled = normalized
                .replace('_', ' ')
                .replace('-', ' ')
                .trim()
                .split(Regex("\\s+"))
                .filter { it.isNotBlank() }
                .joinToString(" ") { word ->
                    if (word in DOCUMENT_ACRONYMS) {
                        word.uppercase(Locale.getDefault())
                    } else {
                        word.replaceFirstChar { c ->
                            if (c.isLowerCase()) c.titlecase(Locale.getDefault()) else c.toString()
                        }
                    }
                }
            titled.ifBlank { "Document" }
        }
    }
}
