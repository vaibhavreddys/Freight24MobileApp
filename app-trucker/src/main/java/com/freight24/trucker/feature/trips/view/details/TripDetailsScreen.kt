package com.freight24.trucker.feature.trips.view.details

import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
}

fun extractStage(details: String): String? {
    return when {
        details.contains("PAID", ignoreCase = true) -> "PAID"
        details.contains("INVOICED", ignoreCase = true) -> "INVOICED"
        details.contains("UNLOADED", ignoreCase = true) -> "UNLOADED"
        details.contains("IN_TRANSIT", ignoreCase = true)
                || details.contains("IN-TRANSIT", ignoreCase = true) -> "IN TRANSIT"
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
        Log.e("TripDetailsContent", "Trip: $trip")
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
