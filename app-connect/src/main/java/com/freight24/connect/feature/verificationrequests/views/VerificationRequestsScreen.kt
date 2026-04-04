package com.freight24.connect.feature.verificationrequests.views

import android.content.Context
import android.os.Build
import android.text.format.DateUtils
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerificationRequestsScreen(
    onBackClick: () -> Unit,
    viewModel: VerificationRequestsViewModel = hiltViewModel<VerificationRequestsViewModelImpl>(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    var selectedRequestData by remember { mutableStateOf<VerificationRequest?>(null) }
    var remarks by rememberSaveable { mutableStateOf("") }
    var remarksError by rememberSaveable { mutableStateOf<String?>(null) }
    val clearSelectedRequest = {
        viewModel.onEvent(VerificationRequestsEvent.ClearActionState)
        selectedRequestData = null
        remarks = ""
        remarksError = null
    }

    LaunchedEffect(uiState.actionCompletedAt) {
        if (uiState.actionCompletedAt != null) {
            remarks = ""
            remarksError = null
            if (selectedRequestData != null) {
                scope.launch {
                    sheetState.hide()
                }.invokeOnCompletion {
                    selectedRequestData = null
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
    }

    androidx.compose.material3.Scaffold(
        topBar = {
            TopAppBar(
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
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
                onTabSelected = { viewModel.onEvent(VerificationRequestsEvent.SelectTab(it)) }
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

                        else -> {
                            item {
                                VerificationEmptyState(
                                    message = "This tab will be wired when the backend is ready.",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 96.dp),
                                )
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
            dragHandle = { androidx.compose.material3.BottomSheetDefaults.DragHandle() },
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
                    viewModel.onEvent(
                        VerificationRequestsEvent.UpdateTripStatus(
                            request = activeRequest,
                            status = VerificationRequestStatus.APPROVED,
                            resolutionNotes = remarks.takeUnless { it.isBlank() },
                        )
                    )
                },
                onRejectClick = {
                    if (remarks.isBlank()) {
                        remarksError = "Remarks are required when rejecting a request."
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
                    uiState.actionInProgressStatus == VerificationRequestStatus.APPROVED,
                isRejectLoading = uiState.actionInProgressRequestId == activeRequest.id &&
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
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        VerificationRequestTab.entries.forEach { tab ->
            val isSelected = tab == selectedTab
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 2.dp).clickable { onTabSelected(tab) }
            ) {
                Text(
                    text = tab.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .width(48.dp)
                        .height(2.dp)
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                            RoundedCornerShape(50)
                        )
                )
            }
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
        VerificationRequestStatus.APPROVED -> Color.Transparent
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
                    containerColor = FreightThemeTokens.colors.successColor,
                    contentColor = FreightThemeTokens.colors.onSuccessColor,
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
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    containerColor: androidx.compose.ui.graphics.Color,
    contentColor: androidx.compose.ui.graphics.Color,
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
