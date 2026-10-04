package com.freight24.trucker.feature.trips.view.details

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.border
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.freight24.common.model.TripDetail
import com.freight24.common.model.TripFinancials
import com.freight24.common.model.TruckerPayment
import com.freight24.common.util.DateTimeUtils
import com.freight24.ui.UiState
import com.freight24.ui.components.FileConfirmationSheet
import com.freight24.ui.components.FileSourcePickerSheet
import com.freight24.ui.components.formatStageName
import com.freight24.ui.components.getPdfLaunchIntent
import com.freight24.ui.components.tripStageTone
import com.freight24.ui.trucker.DocWalletRow
import com.freight24.ui.trucker.FleetCardShape
import com.freight24.ui.trucker.FleetCellShape
import com.freight24.ui.trucker.FleetChip
import com.freight24.ui.trucker.FleetEmptyState
import com.freight24.ui.trucker.FleetNotice
import com.freight24.ui.trucker.FleetRule
import com.freight24.ui.trucker.FleetSectionHeader
import com.freight24.ui.trucker.FleetTone
import com.freight24.ui.trucker.FleetTopBar
import com.freight24.ui.trucker.LabelledStageRail
import com.freight24.ui.trucker.LedgerRow
import com.freight24.ui.trucker.LogState
import com.freight24.ui.trucker.RigShape
import com.freight24.ui.trucker.RoadSpine
import com.freight24.ui.trucker.StampSeal
import com.freight24.ui.trucker.TripLogRow
import com.freight24.ui.trucker.TruckerColors
import com.freight24.ui.trucker.TruckerPrimaryButton
import com.freight24.ui.trucker.TruckerType
import com.freight24.ui.trucker.inr
import com.freight24.ui.trucker.wash
import com.freight24.ui.trucker.rememberTruckerColors
import com.freight24.ui.trucker.tripStageCount
import com.freight24.ui.trucker.tripStageIndex
import com.freight24.ui.trucker.tripStageLabel
import kotlinx.coroutines.launch
import java.io.File

// ---------------------------------------------------------------------------
// Trip detail — the consignment dossier.
//
// The previous version hid documents, pricing and timeline behind three
// collapsed dropdowns, which meant the driver had to know what was inside
// before opening anything. Everything is now flat and readable in one pass,
// in the order the questions are actually asked:
//
//   HERO      trip code, the lane, the truck, where it is along the siding
//   DOCUMENTS what has been lodged and what is still owed, as stamps
//   FREIGHT   revenue + advances until the trip is billed; then the full bill
//   TRIP LOG  every checkpoint, with who moved it and when
//
// Sections are separated by the dock hatch rather than a hairline, and the
// three questions never fold away — an LR that needs re-uploading is the
// single most expensive thing on this page to hide.
// ---------------------------------------------------------------------------

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
    val colors = rememberTruckerColors()

    val selectedUris by viewModel.selectedUris.collectAsState()
    val isPdf by viewModel.isPdf.collectAsState()
    val isUploading by viewModel.isUploading.collectAsState()
    val uploadStatus by viewModel.uploadStatus.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val context = LocalContext.current

    var showSourcePicker by rememberSaveable { mutableStateOf(false) }
    // Which document the current pick → confirm cycle is for: "lr" or "pod".
    var uploadTargetDoc by rememberSaveable { mutableStateOf("lr") }
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }
    val sourceSheetState = androidx.compose.material3.rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(Unit) {
        viewModel.errorEvent.collect { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short,
            )
        }
    }

    val imagePickerLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.PickMultipleVisualMedia(),
            onResult = { uris ->
                if (uris.isNotEmpty()) {
                    viewModel.resetUploadError()
                    viewModel.onFilesPicked(uris, false)
                }
            },
        )

    val pdfPickerLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.OpenDocument(),
            onResult = { uri ->
                uri?.let {
                    viewModel.resetUploadError()
                    viewModel.onFilesPicked(listOf(it), true)
                }
            },
        )

    val cameraLauncher =
        rememberLauncherForActivityResult(
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

    val cameraPermissionLauncher =
        rememberLauncherForActivityResult(
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

    FileSourcePickerSheet(
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
        FileConfirmationSheet(
            selectedUris = selectedUris,
            isPdf = isPdf,
            isUploading = isUploading,
            uploadButtonText = "Confirm Upload",
            onUploadClick = {
                if (uploadTargetDoc == "pod") {
                    viewModel.uploadPOD()
                } else {
                    viewModel.uploadLR()
                }
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
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showStatusConfirmation = null },
            title = { TextDialog("Confirm Update") },
            text = { TextDialog("Move this trip to ${formatStageName(targetStatus)}?") },
            confirmButton = {
                androidx.compose.material3.TextButton(
                    onClick = {
                        viewModel.updateTripStatus(targetStatus)
                        showStatusConfirmation = null
                    },
                ) {
                    TextDialog("Confirm", colors.brand)
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { showStatusConfirmation = null }) {
                    TextDialog("Cancel", colors.inkMuted)
                }
            },
            containerColor = colors.surface,
            shape = FleetCardShape,
        )
    }

    Scaffold(
        containerColor = colors.canvas,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            val tripCode = (uiState as? UiState.Success<TripDetail>)?.data?.tripCode
            FleetTopBar(
                title = "Trip Details",
                subtitle = tripCode,
                colors = colors,
                onBackClick = onBackClick,
                backTestTag = "trip_details_back_btn",
                subtitleAction = {
                    if (!tripCode.isNullOrBlank()) {
                        Box(
                            modifier =
                                Modifier
                                    .size(32.dp)
                                    .clip(androidx.compose.foundation.shape.CircleShape)
                                    .clickable {
                                        val clipboard =
                                            context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        clipboard.setPrimaryClip(ClipData.newPlainText("Trip ID", tripCode))
                                        scope.launch {
                                            snackbarHostState.showSnackbar(
                                                message = "Trip ID copied",
                                                duration = SnackbarDuration.Short,
                                            )
                                        }
                                    }
                                    .semantics { contentDescription = "Copy trip ID" }
                                    .testTag("trip_id_copy_btn"),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Filled.ContentCopy,
                                contentDescription = null,
                                tint = colors.inkMuted,
                                modifier = Modifier.size(15.dp),
                            )
                        }
                    }
                },
            )
        },
        bottomBar = {
            if (uiState is UiState.Success) {
                val trip = (uiState as UiState.Success<TripDetail>).data
                val rawStatus = trip.status.uppercase().replace(" ", "_")
                val lrDoc = trip.tripDocuments.find { it.documentType.lowercase() == "lr" }
                val lrStatus = lrDoc?.verificationStatus?.lowercase()
                val lrRemarks = lrDoc?.verificationRemarks

                Column(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .background(colors.canvas)
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            .navigationBarsPadding(),
                ) {
                    when (rawStatus) {
                        "ASSIGNED" -> {
                            TruckerPrimaryButton(
                                text = "Start Loading",
                                enabled = true,
                                colors = colors,
                                onClick = { showStatusConfirmation = "LOADING" },
                                testTag = "trip_details_status_btn",
                            )
                        }
                        "LOADING" -> {
                            FleetNotice(
                                text = "Upload the LR to move this trip into transit.",
                                tone = FleetTone.ACCENT,
                                colors = colors,
                            )
                        }
                        "IN_TRANSIT" -> {
                            when (lrStatus) {
                                "verified" -> {
                                    TruckerPrimaryButton(
                                        text = "Mark Unloaded",
                                        enabled = true,
                                        colors = colors,
                                        onClick = { showStatusConfirmation = "UNLOADED" },
                                        testTag = "trip_details_status_btn",
                                    )
                                }
                                "rejected" -> {
                                    val reason = lrRemarks?.takeIf { it.isNotBlank() }?.let { " Reason: $it" } ?: " Upload it again."
                                    FleetNotice(
                                        text = "The LR was rejected.$reason",
                                        tone = FleetTone.ERROR,
                                        colors = colors,
                                    )
                                }
                                else -> {
                                    FleetNotice(
                                        text = "Once the LR is verified you can mark this trip unloaded.",
                                        tone = FleetTone.ACCENT,
                                        colors = colors,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = viewModel::onRefresh,
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
        ) {
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .background(colors.canvas),
            ) {
                when (val state = uiState) {
                    is UiState.Loading -> {
                        CircularProgressIndicator(
                            color = colors.brand,
                            modifier = Modifier.align(Alignment.Center),
                        )
                    }
                    is UiState.Error -> {
                        Box(
                            modifier =
                                Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 16.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                FleetEmptyState(
                                    title = "Could not load this trip",
                                    subtitle = state.message,
                                    colors = colors,
                                    shape = RigShape.OPEN,
                                )
                                TruckerPrimaryButton(
                                    text = "Retry",
                                    enabled = true,
                                    colors = colors,
                                    onClick = { viewModel.retry() },
                                )
                            }
                        }
                    }
                    is UiState.Success -> {
                        TripDossier(
                            trip = state.data,
                            uploadStatus = uploadStatus,
                            uploadTargetDoc = uploadTargetDoc,
                            colors = colors,
                            onUploadClick = { docType ->
                                uploadTargetDoc = docType
                                showSourcePicker = true
                            },
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
}

/** Small helper so the dialog uses the fleet type scale, not Material's. */
@Composable
private fun TextDialog(
    text: String,
    color: androidx.compose.ui.graphics.Color = androidx.compose.ui.graphics.Color.Unspecified,
) {
    androidx.compose.material3.Text(
        text = text,
        style = TruckerType.body,
        color = color,
    )
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

// ---------------------------------------------------------------------------
// The dossier
// ---------------------------------------------------------------------------

@Composable
internal fun TripDossier(
    trip: TripDetail,
    uploadStatus: UploadStatus,
    uploadTargetDoc: String,
    colors: TruckerColors,
    onUploadClick: (String) -> Unit,
    onViewClick: (String) -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 24.dp),
    ) {
        TripHero(trip = trip, colors = colors)

        Spacer(modifier = Modifier.height(26.dp))

        DocumentsSection(
            trip = trip,
            uploadStatus = uploadStatus,
            uploadTargetDoc = uploadTargetDoc,
            colors = colors,
            onUploadClick = onUploadClick,
            onViewClick = onViewClick,
        )

        Spacer(modifier = Modifier.height(26.dp))

        FreightBillSection(
            financials = trip.financials,
            payments = trip.truckerPayments,
            status = trip.truckerStatus ?: trip.status,
            colors = colors,
        )

        Spacer(modifier = Modifier.height(26.dp))

        TripLogSection(trip = trip, colors = colors)

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun TripHero(
    trip: TripDetail,
    colors: TruckerColors,
) {
    val stageIndex = tripStageIndex(trip.status)
    val tone = tripStageTone(stageIndex)

    Column(
        modifier =
            Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .fillMaxWidth()
                .background(colors.surface, FleetCardShape)
                .border(1.5.dp, colors.line, FleetCardShape)
                .padding(16.dp),
    ) {
        // Identity: the vehicle leads — the trip code already sits in the
        // top bar, so repeating it here doubles the loudest line on screen.
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = trip.registrationNumber.uppercase(),
                    style = TruckerType.headline,
                    color = colors.ink,
                    maxLines = 1,
                    overflow = TextOverflow.Clip,
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = listOf(trip.truckType, trip.createdAt).filter { it.isNotBlank() }.joinToString(" • "),
                    style = TruckerType.caption,
                    color = colors.inkMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            StampSeal(
                text = if (stageIndex in 0 until tripStageCount()) tripStageLabel(stageIndex) else formatStageName(trip.status),
                tone = tone,
                colors = colors,
                modifier = Modifier.padding(top = 4.dp),
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        RoadSpine(
            from = trip.origin.toDisplayString(),
            to = trip.destination.toDisplayString(),
            colors = colors,
            progress = if (stageIndex < 0) 0f else (stageIndex + 1).toFloat() / tripStageCount(),
        )

        Spacer(modifier = Modifier.height(20.dp))
        FleetRule(colors = colors)
        Spacer(modifier = Modifier.height(18.dp))

        LabelledStageRail(
            currentIndex = stageIndex,
            colors = colors,
        )
    }
}

// ---------------------------------------------------------------------------
// Documents
// ---------------------------------------------------------------------------

@Composable
private fun DocumentsSection(
    trip: TripDetail,
    uploadStatus: UploadStatus,
    uploadTargetDoc: String,
    colors: TruckerColors,
    onUploadClick: (String) -> Unit,
    onViewClick: (String) -> Unit,
) {
    val lrDoc = trip.tripDocuments.find { it.documentType.lowercase() == "lr" }
    val podDoc = trip.tripDocuments.find { it.documentType.lowercase() == "pod" }
    val lrMissing = lrDoc == null
    val lrRejected = lrDoc?.verificationStatus?.lowercase() == "rejected"
    // Upload progress belongs to whichever document the current pick → confirm
    // cycle is for — the other rows must not flicker to "Sending"/"Failed".
    val isPodUpload = uploadTargetDoc == "pod"
    val lrUploading = uploadStatus == UploadStatus.UPLOADING && !isPodUpload
    val lrFailed = uploadStatus == UploadStatus.ERROR && !isPodUpload
    val podUploading = uploadStatus == UploadStatus.UPLOADING && isPodUpload
    val podFailed = uploadStatus == UploadStatus.ERROR && isPodUpload
    // The LR slot is offered whenever the trip can still take one and none
    // is on file. A rejected LR is handled in place, on its own row.
    val showLrSlot = lrMissing && trip.status.uppercase() != "ASSIGNED"
    // The POD is the trucker's last lodgement: it exists only after unloading,
    // and the backend refuses one before it.
    val showPodSlot = podDoc == null && trip.truckerStatus?.uppercase() == "UNLOADED"

    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        FleetSectionHeader(
            title = "Documents",
            colors = colors,
            trailing = {
                Text(
                    text = "${trip.tripDocuments.size} lodged",
                    style = TruckerType.caption,
                    color = colors.inkMuted,
                )
            },
        )

        Spacer(modifier = Modifier.height(12.dp))

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            trip.tripDocuments.forEach { doc ->
                val isLr = doc.documentType.equals("lr", ignoreCase = true)
                val isPod = doc.documentType.equals("pod", ignoreCase = true)
                val effectiveStatus =
                    when {
                        isLr && lrUploading -> "uploading"
                        isLr && lrFailed -> "error"
                        isPod && podUploading -> "uploading"
                        isPod && podFailed -> "error"
                        else -> doc.verificationStatus.lowercase()
                    }

                val (sealText, sealTone, subtitle) =
                    when (effectiveStatus) {
                        "uploading" -> Triple("Sending", FleetTone.ACCENT, "Uploading the ${if (isPod) "POD" else "LR"} now…")
                        "error" -> Triple("Failed", FleetTone.ERROR, "Upload failed. Try again.")
                        "verified" -> {
                            // No generic filler: the stamp already says it.
                            // A stamp date differentiates rows; without one
                            // the row collapses to title + stamp.
                            val stampedAt =
                                doc.uploadedAt
                                    ?.takeIf { it.isNotBlank() }
                                    ?.let { DateTimeUtils.formatRelativeTime(it) }
                                    ?.takeIf { it.isNotBlank() && it != "--" }
                            Triple("Verified", FleetTone.BRAND, stampedAt?.let { "Verified · $it" } ?: "")
                        }
                        "pending" -> Triple("In Review", FleetTone.ACCENT, "Lodged. Waiting on verification.")
                        "rejected" -> {
                            val remarks = doc.verificationRemarks?.takeIf { it.isNotBlank() }
                            Triple("Rejected", FleetTone.ERROR, remarks ?: "Rejected. Upload a fresh copy.")
                        }
                        else -> Triple("Lodged", FleetTone.MUTED, "On file.")
                    }

                val needsUpload = effectiveStatus == "rejected" || effectiveStatus == "error"
                val docTitle =
                    when {
                        isLr -> "Lorry Receipt (LR)"
                        isPod -> "Proof of Delivery (POD)"
                        else -> doc.documentType.uppercase()
                    }

                DocWalletRow(
                    title = docTitle,
                    subtitle = subtitle,
                    icon = Icons.AutoMirrored.Filled.InsertDriveFile,
                    colors = colors,
                    sealText = if (effectiveStatus == "uploading") null else sealText,
                    sealTone = sealTone,
                    actionIcon =
                        when {
                            effectiveStatus == "uploading" -> null
                            needsUpload -> Icons.Filled.Refresh
                            else -> null
                        },
                    actionContentDescription = if (needsUpload) "Upload again" else null,
                    onActionClick = if (needsUpload) {
                        { onUploadClick(doc.documentType.lowercase()) }
                    } else {
                        null
                    },
                    onRowClick = if (!needsUpload && effectiveStatus != "uploading") {
                        { doc.id?.let(onViewClick) }
                    } else {
                        null
                    },
                )

                if (effectiveStatus == "uploading") {
                    LinearProgressIndicator(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp),
                        color = colors.brand,
                        trackColor = colors.brandSoft,
                    )
                }
            }

            // The LR slot — always visible while the trip can still take one.
            if (showLrSlot) {
                DocWalletRow(
                    title = "Lorry Receipt (LR)",
                    subtitle =
                        when {
                            lrUploading -> "Uploading the LR now…"
                            lrFailed -> "Upload failed. Try again."
                            lrRejected -> "Rejected. Upload a fresh copy to move this trip forward."
                            else -> "Required before this trip can move into transit."
                        },
                    icon = if (lrUploading) Icons.Filled.HourglassEmpty else Icons.Filled.CloudUpload,
                    colors = colors,
                    sealText = if (lrRejected) "Rejected" else "Required",
                    sealTone = if (lrRejected) FleetTone.ERROR else FleetTone.ACCENT,
                    actionIcon = if (lrUploading) null else Icons.Filled.CloudUpload,
                    actionContentDescription = "Upload LR",
                    onActionClick = if (lrUploading) {
                        null
                    } else {
                        { onUploadClick("lr") }
                    },
                )
            }

            // The POD slot — offered once the consignment is unloaded and no
            // POD is on file. A rejected POD is handled on its own row above.
            if (showPodSlot) {
                DocWalletRow(
                    title = "Proof of Delivery (POD)",
                    subtitle =
                        when {
                            podUploading -> "Uploading the POD now…"
                            podFailed -> "Upload failed. Try again."
                            else -> "Upload the POD for verification."
                        },
                    icon = if (podUploading) Icons.Filled.HourglassEmpty else Icons.Filled.CloudUpload,
                    colors = colors,
                    sealText = "Required",
                    sealTone = FleetTone.ACCENT,
                    actionIcon = if (podUploading) null else Icons.Filled.CloudUpload,
                    actionContentDescription = "Upload POD",
                    onActionClick = if (podUploading) {
                        null
                    } else {
                        { onUploadClick("pod") }
                    },
                )
            }

            if (trip.tripDocuments.isEmpty() && !showLrSlot && !showPodSlot) {
                Text(
                    text = "Nothing to lodge yet. Documents appear here once the trip is booked.",
                    style = TruckerType.body,
                    color = colors.inkMuted,
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Freight bill
// ---------------------------------------------------------------------------

@Composable
private fun FreightBillSection(
    financials: TripFinancials?,
    payments: List<TruckerPayment>,
    status: String,
    colors: TruckerColors,
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        FleetSectionHeader(title = "Freight Bill", colors = colors)

        Spacer(modifier = Modifier.height(14.dp))

        // The itemised bill (deductions, net payable, balance) is raised once
        // the trip is billed — INVOICED onward. Until then the driver sees the
        // two figures that concern them: the revenue and any advance received.
        val showDetailedBill = tripStageIndex(status) >= tripStageIndex("INVOICED")

        if (financials == null) {
            Text(
                text = if (showDetailedBill) {
                    "The bill is prepared once the trip is unloaded."
                } else {
                    "Trip revenue will appear here once the freight is confirmed."
                },
                style = TruckerType.body,
                color = colors.inkMuted,
            )
            return
        }

        // The bill sits on its own surface so it scans as one invoice.
        // Depth stays tonal (surface + hairline border), per the fleet
        // language — no shadows.
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(colors.surface, FleetCardShape)
                    .border(1.5.dp, colors.line, FleetCardShape)
                    .padding(16.dp),
        ) {
            if (!showDetailedBill) {
                SimpleBill(
                    financials = financials,
                    payments = payments,
                    colors = colors,
                )
            } else {
                DetailedBill(
                    financials = financials,
                    payments = payments,
                    colors = colors,
                )
            }
        }
    }
}

/** Pre-bill view: what the trip earns and what has already landed. */
@Composable
private fun SimpleBill(
    financials: TripFinancials,
    payments: List<TruckerPayment>,
    colors: TruckerColors,
) {
    BillGroupLabel(text = "Earnings", colors = colors)
    Spacer(modifier = Modifier.height(10.dp))
    LedgerRow(
        label = "Trip Revenue",
        amount = inr(financials.truckerTripCost),
        colors = colors,
    )

    val advances = payments.filter { it.type.equals("advance_to_trucker", ignoreCase = true) }
    if (advances.isEmpty()) return

    Spacer(modifier = Modifier.height(14.dp))
    FleetRule(colors = colors)
    Spacer(modifier = Modifier.height(16.dp))
    BillGroupLabel(text = "Advance Payments", colors = colors)
    Spacer(modifier = Modifier.height(10.dp))
    advances.forEachIndexed { index, payment ->
        if (index != 0) {
            Spacer(modifier = Modifier.height(10.dp))
        }
        PaymentCell(payment = payment, colors = colors)
    }
    if (advances.size > 1) {
        Spacer(modifier = Modifier.height(12.dp))
        LedgerRow(
            label = "Advance received",
            amount = inr(advances.sumOf { it.amount }),
            colors = colors,
            emphasis = true,
        )
    }
}

/** The itemised invoice — shown once the trip is billed (INVOICED onward). */
@Composable
private fun DetailedBill(
    financials: TripFinancials,
    payments: List<TruckerPayment>,
    colors: TruckerColors,
) {
    // Earnings
    BillGroupLabel(text = "Earnings", colors = colors)
    Spacer(modifier = Modifier.height(10.dp))
    LedgerRow(
        label = "Trip Revenue",
        amount = inr(financials.truckerTripCost),
        colors = colors,
    )
    Spacer(modifier = Modifier.height(12.dp))
    if (financials.halting.loading.totalAmount > 0) {
        LedgerRow(
            label = "Halting — loading",
            amount = "+ ${inr(financials.halting.loading.totalAmount)}",
            colors = colors,
            tone = FleetTone.BRAND,
            detail = "${financials.halting.loading.days} days × ${inr(financials.halting.loading.perDayAmount)}",
        )
        Spacer(modifier = Modifier.height(12.dp))
    }
    if (financials.halting.unloading.totalAmount > 0) {
        LedgerRow(
            label = "Halting — unloading",
            amount = "+ ${inr(financials.halting.unloading.totalAmount)}",
            colors = colors,
            tone = FleetTone.BRAND,
            detail = "${financials.halting.unloading.days} days × ${inr(financials.halting.unloading.perDayAmount)}",
        )
        Spacer(modifier = Modifier.height(12.dp))
    }
    if (financials.luCharges.loading.total > 0) {
        LedgerRow(
            label = "Loading charges",
            amount = "+ ${inr(financials.luCharges.loading.total)}",
            colors = colors,
            tone = FleetTone.BRAND,
        )
        Spacer(modifier = Modifier.height(12.dp))
    }
    if (financials.luCharges.unloading.total > 0) {
        LedgerRow(
            label = "Unloading charges",
            amount = "+ ${inr(financials.luCharges.unloading.total)}",
            colors = colors,
            tone = FleetTone.BRAND,
        )
        Spacer(modifier = Modifier.height(12.dp))
    }

    Spacer(modifier = Modifier.height(8.dp))
    FleetRule(colors = colors)
    Spacer(modifier = Modifier.height(16.dp))

    // Deductions sit in a soft tint so the red column scans apart
    // from the gross earnings above it.
    BillGroupLabel(text = "Deductions", colors = colors)
    Spacer(modifier = Modifier.height(8.dp))
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(colors.line.copy(alpha = 0.22f), FleetCellShape)
                .padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        LedgerRow(
            label = "Platform fee${if (financials.platformFee.percent > 0) " (${financials.platformFee.percent}%)" else ""}",
            amount = "− ${inr(financials.platformFee.amount)}",
            colors = colors,
            tone = FleetTone.ERROR,
        )
        Spacer(modifier = Modifier.height(12.dp))
        LedgerRow(
            label = "TDS${if (financials.tds.percent > 0f) " (${financials.tds.percent}%)" else ""}",
            amount = "− ${inr(financials.tds.amount)}",
            colors = colors,
            tone = FleetTone.ERROR,
        )
        if (financials.otherDeductions > 0) {
            Spacer(modifier = Modifier.height(12.dp))
            LedgerRow(
                label = "Other deductions",
                amount = "− ${inr(financials.otherDeductions)}",
                colors = colors,
                tone = FleetTone.ERROR,
                detail = financials.deductionRemarks,
            )
        }
    }

    Spacer(modifier = Modifier.height(18.dp))

    // Net payable — the anchor figure of the bill.
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Bottom,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Net payable",
                style = TruckerType.title,
                color = colors.ink,
                maxLines = 1,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Gross earnings minus deductions",
                style = TruckerType.caption,
                color = colors.inkMuted,
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = inr(financials.netPayable),
            style = TruckerType.digit,
            color = colors.ink,
            maxLines = 1,
            textAlign = TextAlign.End,
        )
    }

    Spacer(modifier = Modifier.height(16.dp))
    FleetRule(colors = colors)
    Spacer(modifier = Modifier.height(16.dp))

    // Payments made. Each transfer gets its own tinted cell rather than a bare
    // ledger line: the UTR and the settlement rail are what the driver
    // reconciles against, and a trip can settle across several payments.
    // Stacked as plain rows the reference was unreadable at arm's length.
    BillGroupLabel(text = "Payments Made", colors = colors)
    Spacer(modifier = Modifier.height(10.dp))
    if (payments.isEmpty()) {
        Text(
            text = "No payments made yet.",
            style = TruckerType.body,
            color = colors.inkMuted,
        )
    } else {
        payments.forEachIndexed { index, payment ->
            if (index != 0) {
                Spacer(modifier = Modifier.height(10.dp))
            }
            PaymentCell(payment = payment, colors = colors)
        }
        Spacer(modifier = Modifier.height(12.dp))
        LedgerRow(
            label = "Paid so far",
            amount = inr(payments.sumOf { it.amount }),
            colors = colors,
            emphasis = true,
        )
    }

    val balanceDue = financials.netPayable - financials.totalPaidAmount
    if (balanceDue > 0) {
        Spacer(modifier = Modifier.height(14.dp))
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(FleetTone.ACCENT.wash(colors), FleetCardShape)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Balance due",
                        style = TruckerType.bodyStrong,
                        color = colors.ink,
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier =
                            Modifier
                                .size(6.dp)
                                .clip(androidx.compose.foundation.shape.CircleShape)
                                .background(colors.accent),
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Info,
                        contentDescription = null,
                        tint = colors.inkMuted,
                        modifier = Modifier.size(14.dp),
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Settled after POD is verified",
                        style = TruckerType.caption,
                        color = colors.inkMuted,
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = inr(balanceDue),
                style = TruckerType.title,
                color = colors.accent,
                maxLines = 1,
                textAlign = TextAlign.End,
            )
        }
    }
}

/**
 * One transfer, as its own cell: what it settled, how it moved, and the UTR
 * that ties it to the driver's bank statement.
 *
 * The amount leads because it is the figure being checked. The UTR sits below it
 * in the body-strong face with tabular figures — a reference number is read
 * character by character to compare against a bank statement, not skimmed, so
 * it must not be set in the muted face the surrounding metadata uses.
 *
 * A payment with nothing but an amount renders as a single ledger line: the
 * rules and metadata rows collapse rather than leaving empty scaffolding.
 */
@Composable
private fun PaymentCell(
    payment: TruckerPayment,
    colors: TruckerColors,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(FleetTone.BRAND.wash(colors), FleetCellShape)
                .padding(horizontal = 14.dp, vertical = 13.dp),
    ) {
        LedgerRow(
            label = paymentTitle(payment),
            amount = inr(payment.amount),
            colors = colors,
        )

        val utr = payment.utrNumber
        val method = payment.method?.takeIf { it.isNotBlank() }?.let(::paymentMethodLabel)
        // Only claim a timestamp when there is one — "Paid on --" is worse than
        // leaving the line out entirely.
        val paidOn = payment.paidOn?.let(DateTimeUtils::formatPaymentTimestamp)?.takeIf { it != "--" }

        if (utr != null) {
            Spacer(modifier = Modifier.height(11.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "UTR",
                    style = TruckerType.caption.copy(letterSpacing = 1.2.sp),
                    color = colors.inkMuted,
                )
                Spacer(modifier = Modifier.width(9.dp))
                Text(
                    text = utr,
                    style = TruckerType.bodyStrong.copy(fontFeatureSettings = "tnum"),
                    color = colors.ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        if (method != null || paidOn != null) {
            Spacer(modifier = Modifier.height(9.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (method != null) {
                    FleetChip(text = method, colors = colors)
                }
                if (method != null && paidOn != null) {
                    Spacer(modifier = Modifier.width(10.dp))
                }
                if (paidOn != null) {
                    Text(
                        text = paidOn,
                        style = TruckerType.caption,
                        color = colors.inkMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

/** "Advance" / "Balance" — the leg the payment settled. */
private fun paymentTitle(payment: TruckerPayment): String =
    when (payment.type.lowercase()) {
        "advance_to_trucker" -> "Advance"
        "balance_to_trucker" -> "Balance"
        else -> "Payment"
    }

/**
 * The wire sends snake_case rails (`bank_transfer`, `upi`). Spelled out for the
 * driver — "Bank transfer" is the wording a bank statement itself uses. UPI is
 * an acronym and stays capitalised.
 */
private fun paymentMethodLabel(method: String): String =
    when (method.lowercase()) {
        "upi" -> "UPI"
        else -> method.replace('_', ' ').replaceFirstChar { it.uppercase() }
    }

/** Small caps group label used inside the bill card. */
@Composable
private fun BillGroupLabel(
    text: String,
    colors: TruckerColors,
) {
    Text(
        text = text.uppercase(),
        style = TruckerType.caption.copy(letterSpacing = 1.2.sp),
        color = colors.inkMuted,
    )
}

// ---------------------------------------------------------------------------
// Trip log
// ---------------------------------------------------------------------------

@Composable
private fun TripLogSection(
    trip: TripDetail,
    colors: TruckerColors,
) {
    val stages = listOf("ASSIGNED", "LOADING", "IN_TRANSIT", "UNLOADED", "INVOICED", "PAID")

    val filteredEvents =
        trip.statusEvents
            .filter { !it.details.isNullOrBlank() }
            .filter { it.oldStatus != it.newStatus }

    val completedStatuses = filteredEvents.mapNotNull { it.newStatus }.distinct()
    val currentStageIndex = completedStatuses.maxOfOrNull { stages.indexOf(it.uppercase()) } ?: tripStageIndex(trip.status)

    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        FleetSectionHeader(title = "Trip Log", colors = colors)

        Spacer(modifier = Modifier.height(14.dp))

        if (filteredEvents.isEmpty()) {
            Text(
                text = "No checkpoints recorded yet.",
                style = TruckerType.body,
                color = colors.inkMuted,
            )
        } else {
            val rows =
                stages.mapIndexed { index, stage ->
                    val event = filteredEvents.firstOrNull { it.newStatus?.uppercase() == stage }
                    val state =
                        when {
                            index < currentStageIndex -> LogState.CLEARED
                            index == currentStageIndex -> LogState.CURRENT
                            else -> LogState.AHEAD
                        }
                    Triple(
                        event,
                        state,
                        Triple(
                            tripStageLabel(index),
                            when {
                                event != null && !event.details.isNullOrBlank() -> event.details!!
                                state == LogState.CURRENT -> "In progress"
                                state == LogState.CLEARED -> "Cleared"
                                else -> "Waiting for update"
                            },
                            when {
                                event != null -> DateTimeUtils.formatRelativeTime(event.eventAt)
                                index == 0 -> trip.createdAt
                                else -> ""
                            },
                        ),
                    )
                }

            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .background(colors.surface, FleetCardShape)
                        .border(1.5.dp, colors.line, FleetCardShape)
                        .padding(horizontal = 16.dp, vertical = 18.dp),
            ) {
                rows.forEachIndexed { index, (_, state, texts) ->
                    TripLogRow(
                        title = texts.first,
                        description = texts.second,
                        time = texts.third,
                        state = state,
                        colors = colors,
                        isLast = index == rows.lastIndex,
                        bubbleDescription = texts.second.contains("driver", ignoreCase = true),
                    )
                }
            }
        }
    }
}
