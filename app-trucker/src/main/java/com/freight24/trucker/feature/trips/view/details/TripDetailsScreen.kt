package com.freight24.trucker.feature.trips.view.details

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
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
import kotlinx.coroutines.launch
import java.io.File

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

    val selectedUris by viewModel.selectedUris.collectAsState()
    val isPdf by viewModel.isPdf.collectAsState()
    val isUploading by viewModel.isUploading.collectAsState()
    val uploadStatus by viewModel.uploadStatus.collectAsState()
    val context = LocalContext.current

    var showSourcePicker by rememberSaveable { mutableStateOf(false) }
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }
    val sourceSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.errorEvent.collect { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short,
            )
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(),
        onResult = { uris ->
            if (uris.isNotEmpty()) {
                viewModel.resetUploadError()
                viewModel.onFilesPicked(uris, false)
            }
        },
    )

    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            uri?.let {
                viewModel.resetUploadError()
                viewModel.onFilesPicked(listOf(it), true)
            }
        },
    )

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success) {
                tempCameraUri?.let {
                    viewModel.resetUploadError()
                    viewModel.onFilesPicked(listOf(it), false)
                }
            }
            tempCameraUri = null
        },
    )

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                val imageUri = createTempImageUri(context)
                tempCameraUri = imageUri
                cameraLauncher.launch(imageUri)
            } else {
                scope.launch {
                    val result =
                        snackbarHostState.showSnackbar(
                            message = "Camera permission denied. Grant it from app settings to use camera upload.",
                            actionLabel = "Settings",
                            duration = SnackbarDuration.Long,
                        )
                    if (result == SnackbarResult.ActionPerformed) {
                        openAppSettings(context)
                    }
                }
            }
        },
    )

    fun startCameraCapture() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            val imageUri = createTempImageUri(context)
            tempCameraUri = imageUri
            cameraLauncher.launch(imageUri)
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    com.freight24.ui.components.FileSourcePickerSheet(
        showSheet = showSourcePicker,
        onDismissRequest = { showSourcePicker = false },
        onCameraClick = {
            scope.launch { sourceSheetState.hide() }.invokeOnCompletion {
                showSourcePicker = false
                startCameraCapture()
            }
        },
        onGalleryClick = {
            scope.launch { sourceSheetState.hide() }.invokeOnCompletion {
                showSourcePicker = false
                imagePickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }
        },
        onDocumentClick = {
            scope.launch { sourceSheetState.hide() }.invokeOnCompletion {
                showSourcePicker = false
                pdfPickerLauncher.launch(arrayOf("application/pdf"))
            }
        },
        sheetState = sourceSheetState,
    )

    if (selectedUris.isNotEmpty()) {
        com.freight24.ui.components.FileConfirmationSheet(
            selectedUris = selectedUris,
            isPdf = isPdf,
            isUploading = isUploading,
            uploadButtonText = "Confirm Upload",
            onUploadClick = {
                viewModel.uploadLR()
                viewModel.clearSelection()
            },
            onDismiss = {
                viewModel.clearSelection()
            },
        )
    }

    var showStatusConfirmation by rememberSaveable { mutableStateOf<String?>(null) }

    if (showStatusConfirmation != null) {
        val targetStatus = showStatusConfirmation!!
        AlertDialog(
            onDismissRequest = { showStatusConfirmation = null },
            title = { Text("Confirm Update") },
            text = { Text("Are you sure you want to update the trip status to ${targetStatus.replace("_", " ")}?" ) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.updateTripStatus(targetStatus)
                        showStatusConfirmation = null
                    },
                ) {
                    Text("Confirm", color = MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showStatusConfirmation = null }) {
                    Text("Cancel")
                }
            },
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            text = "Trip Details",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        if (uiState is UiState.Success) {
                            Text(
                                text = (uiState as UiState.Success<TripDetail>).data.tripCode,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                    Spacer(modifier = Modifier.width(48.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
        bottomBar = {
            if (uiState is UiState.Success) {
                val trip = (uiState as UiState.Success<TripDetail>).data
                val rawStatus = trip.status.uppercase().replace(" ", "_")
                val lrDoc = trip.tripDocuments.find { it.documentType.lowercase() == "lr" }
                val lrStatus = lrDoc?.verificationStatus?.lowercase()
                val lrRemarks = lrDoc?.verificationRemarks

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shadowElevation = 8.dp,
                    color = MaterialTheme.colorScheme.surface,
                ) {
                    Column(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .navigationBarsPadding()
                                .padding(16.dp),
                    ) {
                        when (rawStatus) {
                            "ASSIGNED" -> {
                                Button(
                                    onClick = { showStatusConfirmation = "LOADING" },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(50.dp),
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
                                        shape = RoundedCornerShape(50.dp),
                                    ) {
                                        Text("Update Trip Status to UNLOADED")
                                    }
                                } else if (lrStatus == "rejected") {
                                    val lrRemarksText = if (!lrRemarks.isNullOrEmpty()) ".\nReason: $lrRemarks" else ", upload again."
                                    ErrorBanner(
                                        text = "LR document was rejected$lrRemarksText"
                                    )
                                } else {
                                    InfoBanner(text = "Trip status can be changed to \"UNLOADED\" after LR is verified.")
                                }
                            }
                        }
                    }
                }
            }
        },
    ) { paddingValues ->
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.background),
        ) {
            when (val state = uiState) {
                is UiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is UiState.Error -> {
                    TripDetailsLoadErrorSection(
                        onRetry = { viewModel.retry() },
                        modifier = Modifier.padding(16.dp),
                    )
                }
                is UiState.Success -> {
                    TripDetailsContent(
                        trip = state.data,
                        uploadStatus = uploadStatus,
                        onUploadClick = { showSourcePicker = true },
                        onViewClick = { documentId ->
                            viewModel.fetchSignedUrl(documentId) { url ->
                                context.startActivity(getPdfLaunchIntent(url, context))
                            }
                        },
                    )
                }
                UiState.Idle -> Unit
            }
        }
    }
}

@Composable
private fun TripDetailsLoadErrorSection(
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
    ) {
        ExpandableDetailCard(
            title = "Documents",
            icon = Icons.Default.Description,
            initiallyExpanded = true,
        ) {
            Text(
                text = "Unable to load trip details right now.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            TextButton(onClick = onRetry) {
                Text("Retry", color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

private fun createTempImageUri(context: Context): Uri {
    val tempFile = File(context.cacheDir, "camera_capture_${System.currentTimeMillis()}.jpg")
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        tempFile,
    )
}

private fun openAppSettings(context: Context) {
    val intent =
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
        }
    context.startActivity(intent)
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
    // Create infinite transition for continuous animation
    val infiniteTransition = rememberInfiniteTransition(label = "pulse transition")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse scale"
    )

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
                modifier = Modifier
                    .size(20.dp)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    }
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

@Composable
internal fun TripDetailsContent(
    trip: TripDetail,
    uploadStatus: UploadStatus,
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
                totalPaidAmount = trip.totalPaidAmount,
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

        val lrInlineStatus: DocumentStatus = when (uploadStatus) {
            UploadStatus.UPLOADING -> DocumentStatus.Uploading
            UploadStatus.ERROR -> DocumentStatus.UploadError
            UploadStatus.IDLE -> DocumentStatus.Pending
        }

        ExpandableDetailCard(
            title = "Documents",
            icon = Icons.Default.Description,
            initiallyExpanded = expandDocuments,
        ) {
            if (trip.tripDocuments.isEmpty()) {
                if (trip.status.uppercase() != "ASSIGNED") {
                    DocumentUploadItem(
                        title = "Loading Receipt (LR)",
                        subtitle = when (uploadStatus) {
                            UploadStatus.UPLOADING -> "Uploading LR..."
                            UploadStatus.ERROR -> "Upload failed. Tap retry"
                            UploadStatus.IDLE -> "Upload LR"
                        },
                        status = lrInlineStatus,
                        onUploadClick = onUploadClick,
                    )
                } else {
                    Text(
                        text = "No documents currently available for this trip.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 8.dp),
                    )
                }
            } else {
                var hasLrRow = false

                trip.tripDocuments.forEach { doc ->
                    val isLrRow = doc.documentType.equals("lr", ignoreCase = true)
                    if (isLrRow) hasLrRow = true

                    val (status, subtitleText) = when {
                        isLrRow && uploadStatus == UploadStatus.UPLOADING -> {
                            DocumentStatus.Uploading to "Uploading LR..."
                        }

                        isLrRow && uploadStatus == UploadStatus.ERROR -> {
                            DocumentStatus.UploadError to "Upload failed. Tap retry"
                        }

                        else -> {
                            when (doc.verificationStatus.lowercase()) {
                                "verified" -> {
                                    DocumentStatus.Uploaded(
                                        id = doc.id ?: "",
                                        label = "Verified",
                                        backgroundColor = MaterialTheme.colorScheme.primaryContainer,
                                        textColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                    ) to "Your LR is verified."
                                }

                                "pending" -> {
                                    DocumentStatus.Uploaded(
                                        id = doc.id ?: "",
                                        label = "Pending",
                                        backgroundColor = MaterialTheme.colorScheme.tertiaryContainer,
                                        textColor = MaterialTheme.colorScheme.onTertiaryContainer,
                                    ) to "Verification pending"
                                }

                                "rejected" -> {
                                    DocumentStatus.Uploaded(
                                        id = doc.id ?: "",
                                        label = "Rejected",
                                        backgroundColor = MaterialTheme.colorScheme.errorContainer,
                                        textColor = MaterialTheme.colorScheme.onErrorContainer,
                                        canReupload = true,
                                    ) to "Your LR is rejected, upload again"
                                }

                                else -> DocumentStatus.Pending to "Upload new document"
                            }
                        }
                    }

                    DocumentUploadItem(
                        title = doc.documentType.uppercase(),
                        subtitle = subtitleText,
                        status = status,
                        onUploadClick = onUploadClick,
                        onViewClick = onViewClick,
                    )
                }

                if (!hasLrRow && trip.status.uppercase() != "ASSIGNED") {
                    DocumentUploadItem(
                        title = "Loading Receipt (LR)",
                        subtitle = when (uploadStatus) {
                            UploadStatus.UPLOADING -> "Uploading LR..."
                            UploadStatus.ERROR -> "Upload failed. Tap retry"
                            UploadStatus.IDLE -> "Upload LR"
                        },
                        status = lrInlineStatus,
                        onUploadClick = onUploadClick,
                    )
                }
            }
        }

        // Payments Card
        ExpandableDetailCard(
            title = "Payments",
            icon = Icons.Default.Payments,
        ) {
            val financials = trip.financials
            if (financials != null) {
                PaymentsContent(financials = financials)
            } else {
                Text(
                    text = "Payment details not available",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(16.dp),
                )
            }
        }

        // Trip Timeline Card
        ExpandableDetailCard(
            title = "Trip Timeline",
            icon = Icons.Default.History
        ) {
            val stages = listOf("ASSIGNED", "LOADING", "IN_TRANSIT", "UNLOADED", "INVOICED", "PAID")

            val filteredEvents = trip.statusEvents
                .filter { !it.details.isNullOrBlank() }
                .filter { it.oldStatus != it.newStatus }

            val completedStatuses = filteredEvents.map { it.newStatus }.distinct()
            val currentStageIndex = completedStatuses.maxOfOrNull { stages.indexOf(it) } ?: -1

            val timelineItems = stages.mapIndexed { index, stage ->
                val matchingEvent = filteredEvents.firstOrNull { it.newStatus == stage }
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
                        0 -> trip.createdAt
                        else -> matchingEvent?.eventAt?.let { DateTimeUtils.formatRelativeTime(it) } ?: " "
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

@Composable
private fun PaymentsContent(financials: com.freight24.common.model.TripFinancials) {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        PaymentDetailRow("Trip Cost", financials.truckerTripCost)

        if (financials.halting.loading.totalAmount > 0) {
            PaymentDetailRow(
                label = "Loading",
                amount = financials.halting.loading.totalAmount,
                isAddition = true,
                detailText = "(${financials.halting.loading.days} Days × ₹${formatAmount(financials.halting.loading.perDayAmount.toString())})",
                addHeader = true,
                headerName = "Halting Charges",
            )
        }

        if (financials.halting.unloading.totalAmount > 0) {
            PaymentDetailRow(
                label = "Unloading",
                amount = financials.halting.unloading.totalAmount,
                isAddition = true,
                detailText = "(${financials.halting.unloading.days} Days × ₹${formatAmount(financials.halting.unloading.perDayAmount.toString())})",
            )
        }

        if (financials.luCharges.loading.total > 0) {
            PaymentDetailRow(
                label = "Loading Charges",
                amount = financials.luCharges.loading.total,
                isAddition = true,
            )
        }

        if (financials.luCharges.unloading.total > 0) {
            PaymentDetailRow(
                label = "Unloading Charges",
                amount = financials.luCharges.unloading.total,
                isAddition = true,
            )
        }

        if (financials.otherDeductions > 0) {
            PaymentDetailRow("Other Deductions", financials.otherDeductions, isDeduction = true)
        }

        if (financials.advance.paidAmount > 0) {
            PaymentDetailRow("Advance Paid", financials.advance.paidAmount)
        }

        PaymentDetailRow("Platform Fee", financials.platformFee.amount, isDeduction = true)
        PaymentDetailRow("TDS", financials.tds.amount, isDeduction = true)

        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant,
            modifier = Modifier.padding(vertical = 4.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Net Payable",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "₹${formatAmount(financials.netPayable.toString())}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Total Paid",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "₹${formatAmount(financials.totalPaidAmount.toString())}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        val balanceDue = financials.netPayable - financials.totalPaidAmount
        if (balanceDue > 0) {
            Spacer(modifier = Modifier.height(4.dp))
            Surface(
                color = MaterialTheme.colorScheme.tertiaryContainer,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Balance Due",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "₹${formatAmount(balanceDue.toString())}",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}

@Composable
private fun PaymentDetailRow(
    label: String,
    amount: Double,
    isDeduction: Boolean = false,
    isAddition: Boolean = false,
    bold: Boolean = false,
    detailText: String? = null,
    addHeader: Boolean = false,
    headerName: String = "",
) {
    Column(modifier = Modifier.padding(vertical = 2.dp)) {
        if (addHeader && headerName.isNotEmpty()) {
            Text(
                text = headerName,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 4.dp, bottom = 2.dp),
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
            )
            Text(
                text = when {
                    isDeduction -> "- ₹${formatAmount(amount.toString())}"
                    isAddition && amount > 0 -> "+ ₹${formatAmount(amount.toString())}"
                    else -> "₹${formatAmount(amount.toString())}"
                },
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
                color = when {
                    isDeduction -> MaterialTheme.colorScheme.error
                    isAddition && amount > 0 -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.onSurface
                }
            )
        }
        if (!detailText.isNullOrEmpty()) {
            Text(
                text = detailText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 4.dp, top = 2.dp),
            )
        }
    }
}

private fun formatAmount(amount: String): String {
    return try {
        "%,.0f".format(amount.toDouble())
    } catch (e: Exception) {
        amount
    }
}
