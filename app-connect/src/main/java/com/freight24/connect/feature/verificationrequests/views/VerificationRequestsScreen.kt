package com.freight24.connect.feature.verificationrequests.views
import com.freight24.ui.components.FreightTopAppBar

import android.content.Context
import android.os.Build
import android.text.format.DateUtils
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Business
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import com.freight24.connect.feature.verificationrequests.domain.model.TruckDetailChange
import com.freight24.connect.feature.verificationrequests.domain.model.TruckVerificationRequest
import com.freight24.connect.feature.verificationrequests.domain.model.VerificationRequest
import com.freight24.connect.feature.verificationrequests.domain.model.VerificationRequestDocument
import com.freight24.connect.feature.verificationrequests.domain.model.VerificationRequestLocation
import com.freight24.connect.feature.verificationrequests.domain.model.VerificationRequestStatus
import com.freight24.connect.feature.verificationrequests.domain.model.VerificationRequestTab
import com.freight24.connect.feature.verificationrequests.domain.viewmodel.VerificationRequestsEvent
import com.freight24.connect.feature.verificationrequests.domain.viewmodel.VerificationRequestsViewModel
import com.freight24.connect.feature.verificationrequests.domain.viewmodel.VerificationRequestsViewModelImpl
import com.freight24.ui.components.FilterSelectionChip
import com.freight24.ui.components.DocumentLinkChip
import com.freight24.ui.components.RouteIndicatorWithCities
import com.freight24.ui.components.getPdfLaunchIntent
import com.freight24.ui.theme.FreightThemeTokens
import java.text.DateFormat
import java.util.Date
import java.util.Locale
import java.time.OffsetDateTime
import kotlinx.coroutines.launch
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.TripOrigin
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import com.freight24.connect.feature.verificationrequests.domain.model.BusinessProfileChange
import com.freight24.connect.feature.verificationrequests.domain.model.ClientVerificationRequest
import com.freight24.connect.feature.verificationrequests.domain.model.TruckOwnerVerificationRequest

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
                scope.launch {
                    sheetState.hide()
                }.invokeOnCompletion {
                    selectedRequestData = null
                }
            }
            if (selectedTruckData != null) {
                scope.launch {
                    sheetState.hide()
                }.invokeOnCompletion {
                    selectedTruckData = null
                }
            }
            if (selectedTruckOwnerData != null) {
                scope.launch {
                    sheetState.hide()
                }.invokeOnCompletion {
                    selectedTruckOwnerData = null
                }
            }
            if (selectedClientData != null) {
                scope.launch {
                    sheetState.hide()
                }.invokeOnCompletion {
                    selectedClientData = null
                }
            }
        }
    }

    LaunchedEffect(uiState.selectedTab, uiState.selectedStatus) {
        if (uiState.selectedTab != VerificationRequestTab.TRIPS) {
            selectedRequestData = null
        } else {
            selectedRequestData?.let {
                if (!uiState.visibleTrips.contains(it)) {
                    selectedRequestData = null
                }
            }
        }
        if (uiState.selectedTab != VerificationRequestTab.TRUCKS) {
            selectedTruckData = null
        } else {
            selectedTruckData?.let {
                if (!uiState.visibleTrucks.contains(it)) {
                    selectedTruckData = null
                }
            }
        }
        if (uiState.selectedTab != VerificationRequestTab.TRUCK_OWNERS) {
            selectedTruckOwnerData = null
        } else {
            selectedTruckOwnerData?.let {
                if (!uiState.visibleTruckOwners.contains(it)) {
                    selectedTruckOwnerData = null
                }
            }
        }
        if (uiState.selectedTab != VerificationRequestTab.CLIENTS) {
            selectedClientData = null
        } else {
            selectedClientData?.let {
                if (!uiState.visibleClients.contains(it)) {
                    selectedClientData = null
                }
            }
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
                .padding(paddingValues)
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            VerificationTabs(
                selectedTab = uiState.selectedTab,
                onTabSelected = { viewModel.onEvent(VerificationRequestsEvent.SelectTab(it)) },
                tripCount = currentTripCount,
                truckCount = currentTruckCount,
                truckOwnerCount = currentTruckOwnerCount,
                clientCount = currentClientCount,
            )

            Spacer(modifier = Modifier.height(14.dp))
            VerificationStatusFilters(
                tabType = uiState.selectedTab.name,
                selectedStatus = uiState.selectedStatus,
                onStatusSelected = { viewModel.onEvent(VerificationRequestsEvent.SelectStatus(it)) }
            )

            Spacer(modifier = Modifier.height(12.dp))

            PullToRefreshBox(
                isRefreshing = uiState.isRefreshing,
                onRefresh = { viewModel.onEvent(VerificationRequestsEvent.Refresh) },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    when (uiState.selectedTab) {
                        VerificationRequestTab.TRIPS -> {
                            if (uiState.isLoading && uiState.visibleTrips.isEmpty()) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 96.dp),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        CircularProgressIndicator()
                                    }
                                }
                            } else if (uiState.errorMessage != null && uiState.visibleTrips.isEmpty()) {
                                item {
                                    VerificationErrorState(
                                        message = uiState.errorMessage.orEmpty(),
                                        onRetry = { viewModel.onEvent(VerificationRequestsEvent.Refresh) },
                                        modifier = Modifier.fillMaxWidth(),
                                    )
                                }
                            } else if (uiState.visibleTrips.isEmpty()) {
                                item {
                                    VerificationEmptyState(
                                        message = "No verification requests found.",
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 96.dp),
                                    )
                                }
                            } else {
                                items(uiState.visibleTrips, key = { it.id }) { request ->
                                    VerificationRequestCard(
                                        request = request,
                                        onClick = {
                                            viewModel.onEvent(VerificationRequestsEvent.ClearActionState)
                                            selectedRequestData = request
                                        }
                                    )
                                }
                            }
                        }

                        VerificationRequestTab.TRUCKS -> {
                            if (uiState.isLoading && uiState.visibleTrucks.isEmpty()) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 96.dp),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        CircularProgressIndicator()
                                    }
                                }
                            } else if (uiState.errorMessage != null && uiState.visibleTrucks.isEmpty()) {
                                item {
                                    VerificationErrorState(
                                        message = uiState.errorMessage.orEmpty(),
                                        onRetry = { viewModel.onEvent(VerificationRequestsEvent.Refresh) },
                                        modifier = Modifier.fillMaxWidth(),
                                    )
                                }
                            } else if (uiState.visibleTrucks.isEmpty()) {
                                item {
                                    VerificationEmptyState(
                                        message = "No truck verification requests found.",
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 96.dp),
                                    )
                                }
                            } else {
                                items(uiState.visibleTrucks, key = { it.id }) { truck ->
                                    TruckVerificationRequestCard(
                                        request = truck,
                                        onClick = {
                                            viewModel.onEvent(VerificationRequestsEvent.ClearActionState)
                                            selectedTruckData = truck
                                        }
                                    )
                                }
                            }
                        }

                        VerificationRequestTab.TRUCK_OWNERS -> {
                            if (uiState.isLoading && uiState.visibleTruckOwners.isEmpty()) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 96.dp),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        CircularProgressIndicator()
                                    }
                                }
                            } else if (uiState.errorMessage != null && uiState.visibleTruckOwners.isEmpty()) {
                                item {
                                    VerificationErrorState(
                                        message = uiState.errorMessage.orEmpty(),
                                        onRetry = { viewModel.onEvent(VerificationRequestsEvent.Refresh) },
                                        modifier = Modifier.fillMaxWidth(),
                                    )
                                }
                            } else if (uiState.visibleTruckOwners.isEmpty()) {
                                item {
                                    VerificationEmptyState(
                                        message = "No truck owner verification requests found.",
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 96.dp),
                                    )
                                }
                            } else {
                                items(uiState.visibleTruckOwners, key = { it.id }) { owner ->
                                    BusinessVerificationRequestCard(
                                        title = owner.fullName,
                                        subtitle = owner.phone.orEmpty().ifBlank { "Phone unavailable" },
                                        updatedAt = owner.updatedAt,
                                        status = owner.status,
                                        icon = Icons.Default.Person,
                                        changedLabels = owner.changedBusinessProfile.map { formatFieldName(it.displayFieldName) },
                                        onClick = {
                                            viewModel.onEvent(VerificationRequestsEvent.ClearActionState)
                                            selectedTruckOwnerData = owner
                                        },
                                    )
                                }
                            }
                        }

                        VerificationRequestTab.CLIENTS -> {
                            if (uiState.isLoading && uiState.visibleClients.isEmpty()) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 96.dp),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        CircularProgressIndicator()
                                    }
                                }
                            } else if (uiState.errorMessage != null && uiState.visibleClients.isEmpty()) {
                                item {
                                    VerificationErrorState(
                                        message = uiState.errorMessage.orEmpty(),
                                        onRetry = { viewModel.onEvent(VerificationRequestsEvent.Refresh) },
                                        modifier = Modifier.fillMaxWidth(),
                                    )
                                }
                            } else if (uiState.visibleClients.isEmpty()) {
                                item {
                                    VerificationEmptyState(
                                        message = "No client verification requests found.",
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 96.dp),
                                    )
                                }
                            } else {
                                items(uiState.visibleClients, key = { it.id }) { client ->
                                    BusinessVerificationRequestCard(
                                        title = client.companyName,
                                        subtitle = client.fullName,
                                        updatedAt = client.updatedAt,
                                        status = client.status,
                                        icon = Icons.Default.Business,
                                        changedLabels = client.changedBusinessProfile.map { formatFieldName(it.displayFieldName) },
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
            onDismissRequest = {
                clearSelectedRequest()
            },
            sheetState = sheetState,
            dragHandle = { BottomSheetDefaults.DragHandle() },
            containerColor = MaterialTheme.colorScheme.surface,
        ) {
            TripVerificationRequestDetailSheet(
                request = activeRequest,
                remarks = remarks,
                remarksError = remarksError,
                onRemarksChange = {
                    remarks = it
                    if (remarksError != null) {
                        remarksError = null
                    }
                },
                onDocumentClick = { document ->
                    openVerificationDocument(
                        context = context,
                        viewModel = viewModel,
                        document = document,
                    )
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
                onClose = {
                    scope.launch {
                        sheetState.hide()
                    }.invokeOnCompletion {
                        clearSelectedRequest()
                    }
                },
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
            onDismissRequest = {
                clearSelectedRequest()
            },
            sheetState = sheetState,
            dragHandle = { BottomSheetDefaults.DragHandle() },
            containerColor = MaterialTheme.colorScheme.surface,
        ) {
            TruckVerificationRequestDetailSheet(
                request = activeTruck,
                remarks = remarks,
                remarksError = remarksError,
                onRemarksChange = {
                    remarks = it
                    if (remarksError != null) {
                        remarksError = null
                    }
                },
                onDocumentClick = { document ->
                    openVerificationDocument(
                        context = context,
                        viewModel = viewModel,
                        document = document,
                    )
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
                onClose = {
                    scope.launch {
                        sheetState.hide()
                    }.invokeOnCompletion {
                        clearSelectedRequest()
                    }
                },
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
            dragHandle = { BottomSheetDefaults.DragHandle() },
            containerColor = MaterialTheme.colorScheme.surface,
        ) {
            BusinessProfileVerificationDetailSheet(
                title = activeOwner.fullName,
                subtitle = activeOwner.phone.orEmpty().ifBlank { "Truck Owner" },
                status = activeOwner.status,
                changes = activeOwner.changedBusinessProfile,
                remarks = remarks,
                remarksError = remarksError,
                resolutionNotes = activeOwner.resolutionNotes,
                onRemarksChange = {
                    remarks = it
                    if (remarksError != null) {
                        remarksError = null
                    }
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
                onClose = {
                    scope.launch {
                        sheetState.hide()
                    }.invokeOnCompletion {
                        clearSelectedRequest()
                    }
                },
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
            dragHandle = { BottomSheetDefaults.DragHandle() },
            containerColor = MaterialTheme.colorScheme.surface,
        ) {
            BusinessProfileVerificationDetailSheet(
                title = activeClient.companyName,
                subtitle = activeClient.fullName,
                status = activeClient.status,
                changes = activeClient.changedBusinessProfile,
                remarks = remarks,
                remarksError = remarksError,
                resolutionNotes = activeClient.resolutionNotes,
                onRemarksChange = {
                    remarks = it
                    if (remarksError != null) {
                        remarksError = null
                    }
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
                onClose = {
                    scope.launch {
                        sheetState.hide()
                    }.invokeOnCompletion {
                        clearSelectedRequest()
                    }
                },
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

@Composable
private fun VerificationTabs(
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
    val tabIcons = mapOf(
        VerificationRequestTab.TRIPS to Icons.Default.LocalShipping,
        VerificationRequestTab.TRUCKS to Icons.Default.LocalShipping,
        VerificationRequestTab.TRUCK_OWNERS to Icons.Default.People,
        VerificationRequestTab.CLIENTS to Icons.Default.Business,
    )

    ScrollableTabRow(
        selectedTabIndex = selectedTab.ordinal,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.primary,
        edgePadding = 16.dp,
        indicator = { tabPositions ->
            TabRowDefaults.SecondaryIndicator(
                modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab.ordinal]),
                color = MaterialTheme.colorScheme.primary,
            )
        },
    ) {
        VerificationRequestTab.entries.forEach { tab ->
            val isSelected = tab == selectedTab
            val count = counts[tab] ?: 0

            Tab(
                selected = isSelected,
                onClick = { onTabSelected(tab) },
                icon = {
                    TabWithBadge(
                        icon = tabIcons[tab] ?: Icons.Filled.Route,
                        count = count,
                        isSelected = isSelected,
                        onClick = { onTabSelected(tab) }
                    )
                },
                text = {
                    Text(
                        text = tab.title,
                        maxLines = 1,
                    )
                },
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TabWithBadge(
    icon: ImageVector,
    count: Int,
    isSelected: Boolean = false,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = LocalIndication.current,
                onClick = onClick,
                role = Role.Tab
            )
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .wrapContentSize()
            .minimumInteractiveComponentSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (count > 0) {
                Box(
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.error,
                            shape = RoundedCornerShape(percent = 50)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (count > 99) "99+" else count.toString(),
                        color = MaterialTheme.colorScheme.onError,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 12.sp
                    )
                }
            }

            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = if (isSelected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun VerificationStatusFilters(
    tabType: String,
    selectedStatus: VerificationRequestStatus,
    onStatusSelected: (VerificationRequestStatus) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        VerificationRequestStatus.filterValues(tabType).forEach { status ->
            FilterSelectionChip(
                label = status.displayText,
                isSelected = selectedStatus == status,
                onClick = { onStatusSelected(status) }
            )
        }
    }
}

@Composable
private fun VerificationRequestCard(
    request: VerificationRequest,
    onClick: () -> Unit,
) {
    val relativeTime = remember(request.updatedAt) { formatRelativeTime(request.updatedAt) }
    val primaryDocument = request.documents.firstOrNull()
    val isCompleted = request.status == VerificationRequestStatus.APPROVED ||
        request.status == VerificationRequestStatus.REJECTED
    val iconContainerColor = when (request.status) {
        VerificationRequestStatus.REJECTED -> MaterialTheme.colorScheme.errorContainer
        VerificationRequestStatus.APPROVED -> MaterialTheme.colorScheme.surface.copy(alpha = 0f)
        else -> MaterialTheme.colorScheme.primaryContainer
    }
    val iconTint = when (request.status) {
        VerificationRequestStatus.REJECTED -> MaterialTheme.colorScheme.onErrorContainer
        VerificationRequestStatus.APPROVED -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onPrimaryContainer
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = request.tripCode,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        if (isCompleted) {
                            Spacer(modifier = Modifier.width(8.dp))
                            CompletedStatusMark(status = request.status)
                        }
                    }
                }
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(999.dp),
                ) {
                    Text(
                        text = relativeTime,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Normal,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
            ) {
                Surface(
                    color = iconContainerColor,
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Box(
                        modifier = Modifier.size(48.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            tint = iconTint,
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = request.truckRegistrationNumber,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "${request.origin.displayLabel()} → ${request.destination.displayLabel()}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                if (primaryDocument != null) {
                    DocumentBadge(document = primaryDocument)
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
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = request.truckRegistrationNumber,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = request.tripCode,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (isCompletedView) {
                    Spacer(modifier = Modifier.height(10.dp))
                    StatusBannerChip(status = request.status)
                }
            }

            TextButton(onClick = onClose) {
                Text("Close")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Spacer(modifier = Modifier.height(18.dp))

        SectionLabel(text = "Route")
        Spacer(modifier = Modifier.height(10.dp))
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                RouteIndicatorWithCities(
                    fromCity = request.origin.displayLabel(),
                    toCity = request.destination.displayLabel(),
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        SectionLabel(text = "Documents")
        Spacer(modifier = Modifier.height(10.dp))

        if (request.documents.isEmpty()) {
            EmptySheetNote(
                text = "No uploaded documents found.",
            )
        } else {
            request.documents.forEach { document ->
                DocumentRow(
                    document = document,
                    onClick = onDocumentClick,
                    uploadDate = formatDisplayDate(request.updatedAt),
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        SectionLabel(text = "Remarks")
        Spacer(modifier = Modifier.height(10.dp))

        if (isCompletedView) {
            RemarksField(
                remarks = resolutionNotes.orEmpty(),
                remarksError = null,
                enabled = false,
                onRemarksChange = onRemarksChange,
            )
        } else {
            RemarksField(
                remarks = remarks,
                remarksError = remarksError,
                enabled = true,
                onRemarksChange = onRemarksChange,
            )

            if (actionErrorMessage != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.65f),
                    shape = RoundedCornerShape(14.dp),
                ) {
                    Text(
                        text = actionErrorMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                ActionButton(
                    modifier = Modifier.weight(1f),
                    text = "Reject",
                    icon = Icons.Default.Close,
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.24f)),
                    isLoading = isRejectLoading,
                    onClick = onRejectClick,
                )
                ActionButton(
                    modifier = Modifier.weight(1f),
                    text = "Approve",
                    icon = Icons.Default.CheckCircle,
                    containerColor = FreightThemeTokens.colors.success,
                    contentColor = FreightThemeTokens.colors.onSuccess,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)),
                    isLoading = isApproveLoading,
                    onClick = onApproveClick,
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun SummaryCard(
    request: VerificationRequest,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = "Trip overview",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = request.truckRegistrationNumber,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = request.tripCode,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(14.dp))
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = RoundedCornerShape(999.dp),
            ) {
                Text(
                    text = request.status.displayText,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                )
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(horizontal = 4.dp),
    )
}

@Composable
private fun StatusBannerChip(status: VerificationRequestStatus) {
    val (containerColor, contentColor) = when (status) {
        VerificationRequestStatus.REJECTED -> {
            MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
        }
        VerificationRequestStatus.APPROVED -> {
            MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
        }
        else -> {
            MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
        }
    }

    Surface(
        color = containerColor,
        shape = RoundedCornerShape(999.dp),
    ) {
        Text(
            text = status.displayText.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = contentColor,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
        )
    }
}

@Composable
private fun CompletedStatusMark(status: VerificationRequestStatus) {
    val isRejected = status == VerificationRequestStatus.REJECTED
    val containerColor = if (isRejected) {
        MaterialTheme.colorScheme.errorContainer
    } else {
        MaterialTheme.colorScheme.primaryContainer
    }
    val contentColor = if (isRejected) {
        MaterialTheme.colorScheme.onErrorContainer
    } else {
        MaterialTheme.colorScheme.onPrimaryContainer
    }

    Surface(
        color = containerColor,
        shape = RoundedCornerShape(999.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = if (isRejected) Icons.Default.Close else Icons.Default.Check,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(14.dp),
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = if (isRejected) "Rejected" else "Approved",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = contentColor,
            )
        }
    }
}

@Composable
private fun DocumentRow(
    document: VerificationRequestDocument,
    uploadDate: String,
    onClick: (VerificationRequestDocument) -> Unit,
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier.size(46.dp),
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.primary,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = displayDocumentName(document.displayName, false),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Uploaded on $uploadDate",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (!document.s3Url.isNullOrBlank()) {
                DocumentLinkChip(
                    onClick = { onClick(document) },
                )
            } else {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(999.dp),
                ) {
                    Text(
                        text = "Unavailable",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun RemarksField(
    remarks: String,
    remarksError: String?,
    enabled: Boolean,
    onRemarksChange: (String) -> Unit,
) {
    Column {
        OutlinedTextField(
            value = remarks,
            onValueChange = onRemarksChange,
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            minLines = 2,
            maxLines = 3,
            placeholder = {
                Text(if (enabled) "Add remarks for approval or rejection..." else "No remarks provided")
            },
            textStyle = MaterialTheme.typography.bodyMedium,
            shape = RoundedCornerShape(18.dp),
            isError = remarksError != null,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                errorBorderColor = MaterialTheme.colorScheme.error,
                cursorColor = MaterialTheme.colorScheme.primary,
                focusedLeadingIconColor = MaterialTheme.colorScheme.primary,
                unfocusedLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                focusedLabelColor = MaterialTheme.colorScheme.primary,
                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f),
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
            ),
        )
        if (remarksError != null) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = remarksError,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

@Composable
private fun ActionButton(
    modifier: Modifier = Modifier,
    text: String,
    icon: ImageVector,
    containerColor: Color,
    contentColor: Color,
    isLoading: Boolean,
    border: BorderStroke? = null,
    onClick: () -> Unit,
) {
    Button(
        modifier = modifier,
        onClick = onClick,
        enabled = !isLoading,
        shape = RoundedCornerShape(16.dp),
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
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = contentColor,
        )
    }
}

@Composable
private fun TruckVerificationRequestCard(
    request: TruckVerificationRequest,
    onClick: () -> Unit,
) {
    val relativeTime = remember(request.updatedAt) { formatRelativeTime(request.updatedAt) }
    val isCompleted = request.status == VerificationRequestStatus.APPROVED ||
        request.status == VerificationRequestStatus.REJECTED
    val changedLabels = buildChangedSummaryLabels(request)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(
                        text = request.registrationNumber,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    if (isCompleted) {
                        CompletedStatusChip(status = request.status)
                    }
                }
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
                    shape = RoundedCornerShape(999.dp),
                ) {
                    Text(
                        text = relativeTime,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(10.dp),
                ) {
                    Box(
                        modifier = Modifier.size(36.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalShipping,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                if (changedLabels.isNotEmpty()) {
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        changedLabels.forEach { label ->
                            ChangedFieldChip(label = label)
                        }
                    }
                } else {
                    Text(
                        text = "Verification changes",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    )
                }
            }
        }
    }
}

@Composable
private fun CompletedStatusChip(status: VerificationRequestStatus) {
    val isRejected = status == VerificationRequestStatus.REJECTED
    val containerColor = if (isRejected) {
        MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f)
    } else {
        FreightThemeTokens.colors.success.copy(alpha = 0.15f)
    }
    val contentColor = if (isRejected) {
        MaterialTheme.colorScheme.onErrorContainer
    } else {
        FreightThemeTokens.colors.success
    }

    Surface(
        color = containerColor,
        shape = RoundedCornerShape(999.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Icon(
                imageVector = if (isRejected) Icons.Default.Close else Icons.Default.Check,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(12.dp),
            )
            Text(
                text = if (isRejected) "Rejected" else "Approved",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = contentColor,
            )
        }
    }
}

@Composable
private fun ChangedFieldChip(label: String) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = RoundedCornerShape(999.dp),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
        )
    }
}

private fun buildChangedSummaryLabels(request: TruckVerificationRequest): List<String> {
    return buildList {
        request.changedDocuments.forEach { doc ->
            add(displayDocumentName(doc.displayName, false))
        }
        request.changedTruckDetails.forEach { change ->
            add(formatFieldName(change.displayFieldName))
        }
    }
}

@Composable
private fun BusinessVerificationRequestCard(
    title: String,
    subtitle: String,
    updatedAt: String,
    status: VerificationRequestStatus,
    icon: ImageVector,
    changedLabels: List<String>,
    onClick: () -> Unit,
) {
    val relativeTime = remember(updatedAt) { formatRelativeTime(updatedAt) }
    val isCompleted = status == VerificationRequestStatus.APPROVED ||
        status == VerificationRequestStatus.REJECTED

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    if (isCompleted) {
                        CompletedStatusChip(status = status)
                    }
                }
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
                    shape = RoundedCornerShape(999.dp),
                ) {
                    Text(
                        text = relativeTime,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(10.dp),
                ) {
                    Box(
                        modifier = Modifier.size(36.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    if (changedLabels.isNotEmpty()) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            changedLabels.take(3).forEach { label ->
                                ChangedFieldChip(label = label)
                            }
                        }
                    } else {
                        Text(
                            text = "Business profile changes",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BusinessProfileVerificationDetailSheet(
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
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(10.dp))
                StatusBannerChip(status = status)
            }

            TextButton(onClick = onClose) {
                Text("Close")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Spacer(modifier = Modifier.height(18.dp))

        SectionLabel(text = "Changed Business Profile")
        Spacer(modifier = Modifier.height(10.dp))

        if (changes.isEmpty()) {
            EmptySheetNote(text = "No business profile changes to display.")
        } else {
            changes.forEach { change ->
                BusinessProfileChangeRow(change = change)
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        SectionLabel(text = "Remarks")
        Spacer(modifier = Modifier.height(10.dp))

        if (isCompletedView) {
            RemarksField(
                remarks = resolutionNotes.orEmpty(),
                remarksError = null,
                enabled = false,
                onRemarksChange = onRemarksChange,
            )
        } else {
            RemarksField(
                remarks = remarks,
                remarksError = remarksError,
                enabled = true,
                onRemarksChange = onRemarksChange,
            )

            if (actionErrorMessage != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.65f),
                    shape = RoundedCornerShape(14.dp),
                ) {
                    Text(
                        text = actionErrorMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                ActionButton(
                    modifier = Modifier.weight(1f),
                    text = "Reject",
                    icon = Icons.Default.Close,
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.24f)),
                    isLoading = isRejectLoading,
                    onClick = onRejectClick,
                )
                ActionButton(
                    modifier = Modifier.weight(1f),
                    text = "Approve",
                    icon = Icons.Default.CheckCircle,
                    containerColor = FreightThemeTokens.colors.success,
                    contentColor = FreightThemeTokens.colors.onSuccess,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)),
                    isLoading = isApproveLoading,
                    onClick = onApproveClick,
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun BusinessProfileChangeRow(change: BusinessProfileChange) {
    TruckDetailChangeRow(
        change = TruckDetailChange(
            fieldName = change.fieldName,
            displayFieldName = change.displayFieldName,
            beforeValue = change.beforeValue,
            afterValue = change.afterValue,
        )
    )
}

private fun formatFieldName(fieldName: String): String {
    return fieldName.replace('_', ' ').replace('-', ' ')
        .trim()
        .split(' ')
        .joinToString(" ") { word ->
            word.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
        }
        .ifBlank { fieldName }
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
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = request.registrationNumber,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.height(10.dp))
                StatusBannerChip(status = request.status)
            }

            TextButton(onClick = onClose) {
                Text("Close")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Spacer(modifier = Modifier.height(18.dp))

        if (request.changedTruckDetails.isNotEmpty()) {
            SectionLabel(text = "Changed Details")
            Spacer(modifier = Modifier.height(10.dp))

            request.changedTruckDetails.forEach { change ->
                TruckDetailChangeRow(change = change)
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        if (request.changedDocuments.isNotEmpty()) {
            SectionLabel(text = "Documents")
            Spacer(modifier = Modifier.height(10.dp))

            request.changedDocuments.forEach { document ->
                DocumentRow(
                    document = document,
                    onClick = onDocumentClick,
                    uploadDate = formatDisplayDate(request.updatedAt),
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        if (request.changedTruckDetails.isEmpty() && request.changedDocuments.isEmpty()) {
            EmptySheetNote(text = "No changes to display.")
        }

        Spacer(modifier = Modifier.height(18.dp))

        SectionLabel(text = "Remarks")
        Spacer(modifier = Modifier.height(10.dp))

        if (isCompletedView) {
            RemarksField(
                remarks = resolutionNotes.orEmpty(),
                remarksError = null,
                enabled = false,
                onRemarksChange = onRemarksChange,
            )
        } else {
            RemarksField(
                remarks = remarks,
                remarksError = remarksError,
                enabled = true,
                onRemarksChange = onRemarksChange,
            )

            if (actionErrorMessage != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.65f),
                    shape = RoundedCornerShape(14.dp),
                ) {
                    Text(
                        text = actionErrorMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                ActionButton(
                    modifier = Modifier.weight(1f),
                    text = "Reject",
                    icon = Icons.Default.Close,
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.24f)),
                    isLoading = isRejectLoading,
                    onClick = onRejectClick,
                )
                ActionButton(
                    modifier = Modifier.weight(1f),
                    text = "Approve",
                    icon = Icons.Default.CheckCircle,
                    containerColor = FreightThemeTokens.colors.success,
                    contentColor = FreightThemeTokens.colors.onSuccess,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)),
                    isLoading = isApproveLoading,
                    onClick = onApproveClick,
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun TruckDetailChangeRow(change: TruckDetailChange) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = formatFieldName(change.displayFieldName),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Before",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = change.beforeValue?.takeUnless { it.isBlank() } ?: "N/A",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "After",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = change.afterValue?.takeUnless { it.isBlank() } ?: "N/A",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
    }
}

@Composable
private fun VerificationErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error,
        )
        Spacer(modifier = Modifier.height(12.dp))
        Button(onClick = onRetry) {
            Text("Retry")
        }
    }
}

@Composable
private fun VerificationEmptyState(
    message: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun EmptySheetNote(text: String) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(14.dp),
        )
    }
}

private fun openVerificationDocument(
    context: Context,
    viewModel: VerificationRequestsViewModel,
    document: VerificationRequestDocument,
) {
    val s3Url = document.s3Url ?: return
    viewModel.getSignedUrl(s3Url) { signedUrl ->
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

@Composable
private fun DocumentBadge(document: VerificationRequestDocument) {
    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer,
        shape = RoundedCornerShape(999.dp),
    ) {
        Text(
            text = displayDocumentName(document.name, true),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
        )
    }
}

private fun VerificationRequestLocation?.displayLabel(): String {
    return this?.displayText().orEmpty().ifBlank { "N/A" }
}

private fun displayDocumentName(name: String, shortform: Boolean): String {
    return when (name.trim().lowercase()) {
        "lr" -> if (shortform) "LR" else "Loading Receipt"
        "pod" -> if (shortform) "POD" else "Proof Of Delivery"
        else -> name.replace('_', ' ').replace('-', ' ').trim().ifBlank { "Document" }
    }
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
