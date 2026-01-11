package com.freight24.trucker.feature.trips.details

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.freight24.core.common.model.Trip
import com.freight24.core.common.util.DateTimeUtils
import com.freight24.core.ui.components.DocumentStatus
import com.freight24.core.ui.components.DocumentUploadItem
import com.freight24.core.ui.components.ExpandableDetailCard
import com.freight24.core.ui.components.StatusChip
import com.freight24.core.ui.components.TimelineItem
import com.freight24.core.ui.components.TimelineStatus
import com.freight24.core.ui.components.TimelineView
import com.freight24.core.ui.components.TripCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripDetailsScreen(
    onBackClick: () -> Unit,
    onNavigateToPayments: (String) -> Unit,
    snackbarHostState: androidx.compose.material3.SnackbarHostState = androidx.compose.runtime.remember { androidx.compose.material3.SnackbarHostState() },
    viewModel: TripDetailsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    androidx.compose.runtime.LaunchedEffect(uiState) {
        if (uiState is TripDetailUiState.Error) {
            snackbarHostState.showSnackbar((uiState as TripDetailUiState.Error).message)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Trip Details",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (uiState is TripDetailUiState.Success) {
                            Text(
                                text = (uiState as TripDetailUiState.Success).trip.tripCode,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Spacer to center the title
                    Spacer(modifier = Modifier.padding(start = 48.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        snackbarHost = { androidx.compose.material3.SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (val state = uiState) {
                is TripDetailUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is TripDetailUiState.Error -> {
                    Text(
                        text = state.message,
                        modifier = Modifier.align(Alignment.Center),
                        color = Color.Red
                    )
                }
                is TripDetailUiState.Success -> {
                    TripDetailsContent(
                        trip = state.trip,
                        onNavigateToPayments = { onNavigateToPayments(state.trip.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun TripDetailsContent(
    trip: com.freight24.core.common.model.TripDetail,
    onNavigateToPayments: () -> Unit
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
                status = trip.statusEvents.lastOrNull()?.details?.substringAfter("→ ")?.trim() ?: "ASSIGNED",
                createdAt = trip.createdAt,
                tripCost = trip.tripCost,
                advanceAmount = trip.advanceAmount,
                origin = trip.origin.city ?: trip.origin.area ?: "",
                destination = trip.destination.city ?: trip.destination.area ?: ""
            ),
            modifier = Modifier.padding(top = 8.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Documents Card
        ExpandableDetailCard(
            title = "Documents",
            icon = Icons.Default.Description,
            initiallyExpanded = true
        ) {
            if (trip.tripDocuments.isEmpty()) {
                Text(
                    text = "No documents currently available for this trip.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
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
            }
            
            Text(
                text = "Please ensure all uploaded documents are clear and legible. Verification usually takes 2-4 hours.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            )
        }

        // Payments Card
        ExpandableDetailCard(
            title = "Payments",
            icon = Icons.Default.Payments,
            modifier = Modifier.clickable { onNavigateToPayments() }
        ) {
            Text(
                text = "Payment details placeholder",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Trip Timeline Card
        ExpandableDetailCard(
            title = "Trip Timeline",
            icon = Icons.Default.History,
            trailingContent = {
                val currentStatus = trip.statusEvents.lastOrNull()?.details?.substringAfter("→ ")?.trim() ?: "ASSIGNED"
                StatusChip(status = currentStatus)
            }
        ) {
            val stages = listOf("ASSIGNED", "LOADING", "IN_TRANSIT", "UNLOADED", "INVOICED", "PAID")
            val timelineItems = stages.map { stage ->
                val event = trip.statusEvents.find { it.details.contains(stage) }
                val status = when {
                    event != null -> TimelineStatus.COMPLETED
                    else -> TimelineStatus.PENDING
                }
                // Mark the current status as ACTIVE if it's the latest one found
                val isCurrent = trip.statusEvents.lastOrNull()?.details?.contains(stage) == true
                
                TimelineItem(
                    title = stage.lowercase().replaceFirstChar { it.uppercase() },
                    description = if (event != null) "Completed" else "Waiting for update",
                    time = DateTimeUtils.formatRelativeTime(event?.eventAt),
                    status = if (isCurrent) TimelineStatus.ACTIVE else status
                )
            }
            
            TimelineView(items = timelineItems)
        }
    }
}
