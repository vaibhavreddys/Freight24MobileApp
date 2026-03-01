package com.freight24.trucker.feature.trips.view.details

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil3.compose.rememberAsyncImagePainter
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
import com.freight24.ui.components.getPdfLaunchIntent

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


    val selectedUris by viewModel.selectedUris.collectAsState()
    val isPdf by viewModel.isPdf.collectAsState()
    val isUploading by viewModel.isUploading.collectAsState()

    var showSelectionDialog by rememberSaveable { mutableStateOf(false) }
    var showSheet by rememberSaveable { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(),
        onResult = { uris ->
            if (uris.isNotEmpty()) {
                viewModel.onFilesPicked(uris, false)
                showSheet = true
            }
        }
    )

    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            uri?.let {
                viewModel.onFilesPicked(listOf(it), true)
                showSheet = true
            }
        }
    )

    if (showSelectionDialog) {
        AlertDialog(
            onDismissRequest = { showSelectionDialog = false },
            title = { Text("Select Document Type") },
            text = { Text("Select images from gallery or a PDF file to upload.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        imagePickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        showSelectionDialog = false
                    }
                ) {
                    Text("Images", color = MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        pdfPickerLauncher.launch("application/pdf")
                        showSelectionDialog = false
                    }
                ) {
                    Text("PDF", color = MaterialTheme.colorScheme.primary)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(12.dp)
        )
    }

    if (showSheet && selectedUris.isNotEmpty()) {
        ModalBottomSheet(
            onDismissRequest = {
                showSheet = false
                viewModel.clearSelection()
            },
            sheetState = sheetState,
            containerColor = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (isPdf) "PDF Selected" else "Images Selected (${selectedUris.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (isPdf) {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = Color.Red
                    )
                    Text(text = "PDF Document", style = MaterialTheme.typography.bodyMedium)
                } else {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(selectedUris) { uri ->
                            Box {
                                Image(
                                    painter = rememberAsyncImagePainter(uri),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(100.dp)
                                        .clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        viewModel.uploadLR()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    enabled = !isUploading
                ) {
                    if (isUploading) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Uploading...", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Text("Upload", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    var showStatusConfirmation by rememberSaveable { mutableStateOf<String?>(null) }

    if (showStatusConfirmation != null) {
        val targetStatus = showStatusConfirmation!!
        AlertDialog(
            onDismissRequest = { showStatusConfirmation = null },
            title = { Text("Confirm Update") },
            text = { Text("Are you sure you want to update the trip status to ${targetStatus.replace("_", " ")}?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.updateTripStatus(targetStatus)
                        showStatusConfirmation = null
                    }
                ) {
                    Text("Confirm", color = MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showStatusConfirmation = null }) {
                    Text("Cancel")
                }
            }
        )
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
                val trip = (uiState as UiState.Success<TripDetail>).data
                val rawStatus = trip.status.uppercase().replace(" ", "_")
                val lrDoc = trip.tripDocuments.find { it.documentType.lowercase() == "lr" }
                val lrStatus = lrDoc?.verificationStatus?.lowercase()

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shadowElevation = 8.dp,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(16.dp)
                    ) {
                        when (rawStatus) {
                            "ASSIGNED" -> {
                                Button(
                                    onClick = { showStatusConfirmation = "LOADING" },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(50.dp)
                                ) {
                                    Text("Update Trip Status to LOADING")
                                }
                            }
                            "LOADING" -> {
                                InfoBanner(text = "Please upload LR to change status to IN TRANSIT")
                            }
                            "IN_TRANSIT" -> {
                                if (lrStatus == "verified") {
                                    Button(
                                        onClick = { showStatusConfirmation = "UNLOADED" },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(50.dp)
                                    ) {
                                        Text("Update Trip Status to UNLOADED")
                                    }
                                } else if (lrStatus == "rejected") {
                                    ErrorBanner(text = "LR document was rejected. Upload new one to proceed.")
                                } else {
                                    InfoBanner(text = "Trip status can be changed to \"UNLOADED\" after LR is verified.")
                                }
                            }
                            // Other statuses managed by backend
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
                    val context = LocalContext.current
                    TripDetailsContent(
                        trip = state.data,
                        onNavigateToPayments = { onNavigateToPayments(state.data.id) },
                        onUploadClick = { showSelectionDialog = true },
                        onViewClick = { documentId ->
                            viewModel.fetchSignedUrl(documentId) { url ->
                                context.startActivity(getPdfLaunchIntent(url, context))
                            }
                        }
                    )
                }
                UiState.Idle -> {}
            }
        }
    }
}

@Composable
fun InfoBanner(text: String) {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Description,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun ErrorBanner(text: String) {
    Surface(
        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun StatusSelectionContent(
    currentStatus: String,
    onStatusSelected: (String) -> Unit
) {
    // This component is no longer used in the streamlined flow but kept for potential future use or until fully deprecated
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
    onUploadClick: () -> Unit,
    onViewClick: (String) -> Unit
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
        val lrDoc = trip.tripDocuments.find { it.documentType.lowercase() == "lr" }
        val isLrMissing = lrDoc == null
        val isLrRejected = lrDoc?.verificationStatus?.lowercase() == "rejected"
        val expandDocuments = isLrMissing || isLrRejected

        ExpandableDetailCard(
            title = "Documents",
            icon = Icons.Default.Description,
            initiallyExpanded = expandDocuments
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
                    val (status, subtitleText) = when (doc.verificationStatus.lowercase()) {
                        "verified" -> {
                            DocumentStatus.Uploaded(
                                id = doc.id ?: "",
                                label = "Verified",
                                backgroundColor = Color(0xFFE8F5E9),
                                textColor = Color(0xFF2E7D32)
                            ) to "Your LR is verified."
                        }
                        "pending" -> {
                            DocumentStatus.Uploaded(
                                id = doc.id ?: "",
                                label = "Pending",
                                backgroundColor = Color(0xFFFFF3E0),
                                textColor = Color(0xFFEF6C00)
                            ) to "Verification pending"
                        }
                        "rejected" -> {
                            DocumentStatus.Uploaded(
                                id = doc.id ?: "",
                                label = "Rejected",
                                backgroundColor = Color(0xFFFFEBEE),
                                textColor = Color(0xFFC62828),
                                canReupload = true
                            ) to "Your LR is rejected, upload again"
                        }
                        else -> DocumentStatus.Pending to "Upload new document"
                    }

                    DocumentUploadItem(
                        title = doc.documentType.uppercase(),
                        subtitle = subtitleText,
                        status = status,
                        onUploadClick = onUploadClick,
                        onViewClick = onViewClick
                    )
                }
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
