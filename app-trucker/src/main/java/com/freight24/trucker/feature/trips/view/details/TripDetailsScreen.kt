package com.freight24.trucker.feature.trips.view.details

import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.freight24.common.model.Trip
import com.freight24.common.model.TripDetail
import com.freight24.common.util.DateTimeUtils
import com.freight24.ui.UiState
import com.freight24.ui.components.DocumentStatus
import com.freight24.ui.components.DocumentUploadItem
import com.freight24.ui.components.ExpandableDetailCard
import com.freight24.ui.components.TimelineItem
import com.freight24.ui.components.TimelineStatus
import com.freight24.ui.components.TimelineView
import com.freight24.ui.components.TripCard
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TripDetailsScreen(
    onBackClick: () -> Unit,
    onNavigateToPayments: (String) -> Unit = {},
    viewModel: TripDetailsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showBottomSheet by rememberSaveable { mutableStateOf(false) }
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)


    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            Log.d("TripDetailsScreen", "Selected document URI: $uri")
            // Future implementation: viewModel.uploadDocument(uri)
            scope.launch {
                snackbarHostState.showSnackbar("Upload feature incomplete. Selected URI: $uri")
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Trip Details",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (uiState is UiState.Success) {
                            Text(
                                text = (uiState as UiState.Success<TripDetail>).data.tripCode,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Spacer to center the title
                    Spacer(modifier = Modifier.width(48.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            if (uiState is UiState.Success) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shadowElevation = 8.dp,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Button(
                        onClick = { showBottomSheet = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(16.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Update Status")
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (val state = uiState) {
                is UiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is UiState.Error -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = state.message,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(16.dp),
                            textAlign = TextAlign.Center
                        )
                        Button(onClick = { viewModel.retry() }) {
                            Text("Retry")
                        }
                    }
                }
                is UiState.Success -> {
                    TripDetailsContent(
                        trip = state.data,
                        onNavigateToPayments = { onNavigateToPayments(state.data.id) },
                        onUploadClick = { filePickerLauncher.launch("image/*") }
                    )
                }
                UiState.Idle -> {}
            }
        }
    }

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = bottomSheetState,
            containerColor = MaterialTheme.colorScheme.surface,
        ) {
            val currentStatus = (uiState as? UiState.Success)?.data?.status ?: ""
            // Map "In Transit" back to "IN_TRANSIT" for comparison if needed, or rely on extracting the raw enum value.
            // The status from UI state might be formatted (e.g. "In Transit"), so we need to be careful.
            // Let's rely on the raw status if possible, or normalize.
            // The `status` field in TripDetail seems to be formatted message in mapper: `NameFormatter.formatTripStatusMessage(trucker_status)`.
            // We should arguably use the raw status from API, but we might not have it easily in domain model if we mapped it away.
            // Let's check TripMappers.kt again.
            // TripDetail.status = NameFormatter.formatTripStatusMessage(trucker_status)
            // This is "In Transit", "Loading", etc.
            // We need to map it back or change TripDetail to store raw status.
            // For now, let's normalize "In Transit" -> "IN_TRANSIT" etc.
            
            StatusSelectionContent(
                currentStatus = currentStatus, // This is the formatted string like "Loading"
                onStatusSelected = { status ->
                    viewModel.updateTripStatus(status)
                    scope.launch { bottomSheetState.hide() }.invokeOnCompletion {
                        if (!bottomSheetState.isVisible) {
                            showBottomSheet = false
                        }
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatusSelectionContent(
    currentStatus: String,
    onStatusSelected: (String) -> Unit
) {
    val statuses = listOf("ASSIGNED", "LOADING", "IN_TRANSIT", "UNLOADED", "INVOICED", "PAID")
    
    // Normalize the display status back to API status format for comparison
    val normalizedCurrentStatus = when (currentStatus.uppercase()) {
        "IN TRANSIT" -> "IN_TRANSIT"
        else -> currentStatus.uppercase().replace(" ", "_")
    }

    val currentIndex = statuses.indexOf(normalizedCurrentStatus)
    // Filter out statuses that are before the current status
    val availableStatuses = if (currentIndex != -1) {
        statuses.drop(currentIndex + 1) // Only show next statuses
         // Or if you want to show all but disable previous:
         // statuses
    } else {
        statuses
    }

    // Bug Fix 1: Use normalizedCurrentStatus as key or in LaunchedEffect, but simplest is to initialize state with it correctly 
    // AND update it if the prop changes (though bottom sheet usually recreates). 
    // To ensure it's selected when opening, we default to the current status (or the next one?). 
    // The user usually wants to select the NEXT status.
    // If we want to show the current one as selected initially:
    var selectedOption by rememberSaveable(normalizedCurrentStatus) { mutableStateOf(normalizedCurrentStatus) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .padding(bottom = 32.dp)
    ) {
        Text(
            text = "Update Trip Status",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Show all statuses but disable previous ones
        statuses.forEachIndexed { index, status ->
            val isEnabled = index >= currentIndex
            
            Row(
                Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = (status == selectedOption),
                        onClick = { if (isEnabled) selectedOption = status },
                        enabled = isEnabled
                    )
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = (status == selectedOption),
                    onClick = { if (isEnabled) selectedOption = status },
                    enabled = isEnabled
                )
                Text(
                    text = status.replace("_", " "),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(start = 16.dp),
                    color = if (isEnabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { onStatusSelected(selectedOption) },
            modifier = Modifier.fillMaxWidth(),
            enabled = selectedOption != normalizedCurrentStatus
        ) {
            Text("Update Status")
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

fun extractStage(details: String): String? {
    return when {
        details.contains("PAID", ignoreCase = true) -> "PAID"
        details.contains("INVOICED", ignoreCase = true) -> "INVOICED"
        details.contains("UNLOADED", ignoreCase = true) -> "UNLOADED"
        details.contains("IN_TRANSIT", ignoreCase = true)
                || details.contains("IN-TRANSIT", ignoreCase = true) -> "IN_TRANSIT"
        details.contains("LOADING", ignoreCase = true) -> "LOADING"
        details.contains("ASSIGNED", ignoreCase = true) -> "ASSIGNED"
        else -> null
    }
}

@Composable
fun TripDetailsContent(
    trip: TripDetail,
    onNavigateToPayments: () -> Unit,
    onUploadClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 24.dp)
    ) {
        // Hero Trip Card (Converted from TripDetail to Trip for reuse)
        TripCard(
            trip = Trip(
                id = trip.id,
                registrationNumber = trip.registrationNumber,
                truckType = trip.truckType,
                status = trip.status,
                createdAt = trip.createdAt,
                tripCost = trip.tripCost,
                advanceAmount = trip.advanceAmount,
                origin = trip.origin.toDisplayString(),
                destination = trip.destination.toDisplayString()
            ),
            modifier = Modifier.padding(top = 8.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Documents Card
        ExpandableDetailCard(
            title = "Documents",
            icon = Icons.Default.Description,
            initiallyExpanded = false
        ) {
            if (trip.tripDocuments.isEmpty()) {
                if (trip.status.uppercase() != "ASSIGNED") {
                    DocumentUploadItem(
                        title = "Loading Receipt (LR)",
                        subtitle = "Upload LR",
                        status = DocumentStatus.Pending,
                        onUploadClick = onUploadClick
                    )
                } else {
                    Text(
                        text = "No documents currently available for this trip.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            } else {
                trip.tripDocuments.forEach { doc ->
                    val status = when (doc.verificationStatus.lowercase()) {
                        "verified" -> DocumentStatus.Uploaded(id = doc.id ?: "", isVerified = true)
                        "pending" -> DocumentStatus.Uploaded(id = doc.id ?: "", isVerified = false)
                        else -> DocumentStatus.Pending
                    }

                    DocumentUploadItem(
                        title = doc.documentType.uppercase(),
                        subtitle = if (doc.verificationStatus == "pending") "Verification pending" else doc.originalFilename ?: "Upload new document",
                        status = status,
                        onUploadClick = { /* Camera or File picker logic */ }
                    )
                }
                Text(
                    text = "Please ensure all uploaded documents are clear. Verification usually takes 2-4 hours.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                )
            }
            
        }

        // Payments Card
        ExpandableDetailCard(
            title = "Payments",
            icon = Icons.Default.Payments,
            modifier = Modifier.clickable { onNavigateToPayments() }
        ) {
            Text(
                text = "Payment details placeholder showing cost ₹${trip.tripCost.toInt()} and advance ₹${trip.advanceAmount.toInt()}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Trip Timeline Card
        ExpandableDetailCard(
            title = "Trip Timeline",
            icon = Icons.Default.History
        ) {
            val stages = listOf("ASSIGNED", "LOADING", "IN_TRANSIT", "UNLOADED", "INVOICED", "PAID")

            val completedStages = trip.statusEvents
                .mapNotNull { extractStage(it.details) }
                .distinct()

            val currentStageIndex =
                completedStages.maxOfOrNull { stages.indexOf(it) } ?: -1

            val timelineItems = stages.mapIndexed { index, stage ->

                val isCompleted = index < currentStageIndex
                val isCurrent = index == currentStageIndex

                TimelineItem(
                    title = stage
                        .lowercase()
                        .replace("_", " ")
                        .replaceFirstChar { it.uppercase() },

                    description = when {
                        isCurrent -> "In progress"
                        isCompleted -> "Completed"
                        else -> "Waiting for update"
                    },

                    time = when (index) {
                        0 -> trip.createdAt // For first stage, show trip creation time
                        else -> trip.statusEvents
                            .firstOrNull { extractStage(it.details) == stage }
                            ?.eventAt
                            ?.let { DateTimeUtils.formatRelativeTime(it) }
                            ?: " "
                    },

                    status = when {
                        isCurrent -> TimelineStatus.ACTIVE
                        isCompleted -> TimelineStatus.COMPLETED
                        else -> TimelineStatus.PENDING
                    }
                )
            }
            TimelineView(items = timelineItems)
        }
    }
}
