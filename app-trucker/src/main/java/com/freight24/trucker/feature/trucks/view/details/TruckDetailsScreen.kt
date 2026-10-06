package com.freight24.trucker.feature.trucks.view.details

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.freight24.common.model.TruckDetail
import com.freight24.common.model.TruckDocument
import com.freight24.ui.UiState
import com.freight24.ui.components.FileConfirmationSheet
import com.freight24.ui.components.FileSourcePickerSheet
import com.freight24.ui.components.getPdfLaunchIntent
import com.freight24.ui.trucker.DocWalletRow
import com.freight24.ui.trucker.FleetCardShape
import com.freight24.ui.trucker.FleetEmptyState
import com.freight24.ui.trucker.FleetRule
import com.freight24.ui.trucker.FleetSectionHeader
import com.freight24.ui.trucker.FleetTone
import com.freight24.ui.trucker.FleetTopBar
import com.freight24.ui.trucker.PlateBadgeLarge
import com.freight24.ui.trucker.RigShape
import com.freight24.ui.trucker.RigSilhouette
import com.freight24.ui.trucker.SpecCell
import com.freight24.ui.trucker.StampSeal
import com.freight24.ui.trucker.TruckerColors
import com.freight24.ui.trucker.TruckerPrimaryButton
import com.freight24.ui.trucker.TruckerSecondaryButton
import com.freight24.ui.trucker.TruckerType
import com.freight24.ui.trucker.VerificationLine
import com.freight24.ui.trucker.rememberTruckerColors
import com.freight24.ui.trucker.rigShapeFor
import com.freight24.ui.trucker.truckSidingLabel
import com.freight24.ui.trucker.truckSidingTone
import kotlinx.coroutines.launch
import java.io.File

// ---------------------------------------------------------------------------
// Truck detail — the data plate.
//
// The truck is the subject here, so this is the one screen that gets a
// drawing: the rig, shaped by its body type, sitting beside the plate. Past
// that it holds the same line as the trips dossier — identity, a hairline,
// specifications as stamped fields, paperwork as wallet rows. No collapsing
// sections, no nested cards.
// ---------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TruckDetailsScreen(
    onBackClick: () -> Unit,
    onEditClick: (String) -> Unit = {},
    viewModel: TruckDetailsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val selectedUris by viewModel.selectedUris.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val isUploading by viewModel.isUploading.collectAsState()
    val colors = rememberTruckerColors()

    LaunchedEffect(Unit) {
        viewModel.uploadSuccess.collect {
            snackbarHostState.showSnackbar("Document uploaded successfully")
        }
    }

    LaunchedEffect(Unit) {
        viewModel.errorEvent.collect { error ->
            snackbarHostState.showSnackbar(error)
        }
    }

    var showSourcePicker by rememberSaveable { mutableStateOf(false) }
    var currentDocumentType by rememberSaveable { mutableStateOf<String?>(null) }
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }
    val sourceSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val photoPickerLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.PickMultipleVisualMedia(),
            onResult = { uris ->
                if (uris.isNotEmpty()) {
                    viewModel.onFilesPicked(uris, false, currentDocumentType ?: "")
                    currentDocumentType = null
                }
            },
        )

    val documentPickerLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.OpenDocument(),
            onResult = { uri ->
                uri?.let {
                    viewModel.onFilesPicked(listOf(it), true, currentDocumentType ?: "")
                    currentDocumentType = null
                }
            },
        )

    val cameraLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.TakePicture(),
            onResult = { success ->
                if (success) {
                    tempCameraUri?.let {
                        viewModel.onFilesPicked(listOf(it), false, currentDocumentType ?: "")
                    }
                }
                tempCameraUri = null
                currentDocumentType = null
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
        onDismissRequest = {
            showSourcePicker = false
            currentDocumentType = null
        },
        onCameraClick = {
            scope.launch { sourceSheetState.hide() }.invokeOnCompletion {
                showSourcePicker = false
                startCameraCapture()
            }
        },
        onGalleryClick = {
            scope.launch { sourceSheetState.hide() }.invokeOnCompletion {
                showSourcePicker = false
                photoPickerLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                )
            }
        },
        onDocumentClick = {
            scope.launch { sourceSheetState.hide() }.invokeOnCompletion {
                showSourcePicker = false
                documentPickerLauncher.launch(arrayOf("application/pdf"))
            }
        },
        sheetState = sourceSheetState,
    )

    if (selectedUris.isNotEmpty()) {
        FileConfirmationSheet(
            selectedUris = selectedUris,
            isPdf = viewModel.isPdf.value,
            isUploading = isUploading,
            uploadButtonText = "Upload Now",
            onUploadClick = { viewModel.uploadDocuments() },
            onDismiss = { viewModel.clearSelection() },
        )
    }

    Scaffold(
        containerColor = colors.canvas,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            FleetTopBar(
                title = "Truck Details",
                subtitle = (uiState as? UiState.Success<TruckDetail>)?.data?.registrationNumber,
                colors = colors,
                onBackClick = onBackClick,
                backTestTag = "truck_details_back_btn",
            )
        },
        bottomBar = {
            val state = uiState
            if (state is UiState.Success && !state.data.isVerified) {
                Column(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .background(colors.canvas)
                            .padding(horizontal = 16.dp)
                            .padding(top = 12.dp, bottom = 24.dp),
                ) {
                    TruckerPrimaryButton(
                        text = "Edit Truck",
                        enabled = true,
                        colors = colors,
                        onClick = { onEditClick(state.data.truckId) },
                        testTag = "truck_edit_button",
                    )
                }
            }
        },
    ) { paddingValues ->
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
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
                    Column(
                        modifier =
                            Modifier
                                .align(Alignment.Center)
                                .padding(horizontal = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        FleetEmptyState(
                            title = "Could not load this truck",
                            subtitle = state.message,
                            colors = colors,
                            shape = RigShape.OPEN,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        TruckerSecondaryButton(
                            text = "Retry",
                            enabled = true,
                            colors = colors,
                            onClick = { viewModel.fetchTruckDetail(true) },
                        )
                    }
                }
                is UiState.Success -> {
                    TruckDossier(
                        truck = state.data,
                        colors = colors,
                        onViewClick = { s3Url ->
                            val documentType =
                                when (s3Url) {
                                    state.data.rcCard?.url -> "rc"
                                    state.data.fcCertificate?.url -> "fc"
                                    state.data.insurance?.url -> "insurance"
                                    else -> null
                                }
                            if (documentType != null) {
                                viewModel.fetchSignedUrl(documentType) { signedUrl ->
                                    context.startActivity(getPdfLaunchIntent(signedUrl, context))
                                }
                            }
                        },
                        onUploadClick = { type ->
                            currentDocumentType = type
                            showSourcePicker = true
                        },
                    )
                }
                else -> Unit
            }

            if (isUploading) {
                UploadingVeil(colors = colors)
            }
        }
    }
}

@Composable
private fun UploadingVeil(colors: TruckerColors) {
    Surface(
        color = colors.canvas.copy(alpha = 0.82f),
        modifier = Modifier.fillMaxSize(),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Column(
                modifier =
                    Modifier
                        .background(colors.surface, FleetCardShape)
                        .border(1.5.dp, colors.line, FleetCardShape)
                        .padding(horizontal = 28.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                CircularProgressIndicator(color = colors.brand, strokeWidth = 2.5.dp)
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "Uploading document…",
                    style = TruckerType.body,
                    color = colors.ink,
                )
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

// ---------------------------------------------------------------------------
// The dossier
// ---------------------------------------------------------------------------

@Composable
internal fun TruckDossier(
    truck: TruckDetail,
    colors: TruckerColors,
    onViewClick: (String?) -> Unit,
    onUploadClick: (String) -> Unit,
) {
    val siding = truckSidingTone(availability = "AVAILABLE", isBlacklisted = truck.isBlacklisted)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 4.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        item {
            TruckHero(truck = truck, siding = siding, colors = colors)
        }

        item {
            Spacer(modifier = Modifier.height(26.dp))
            SpecificationsSection(truck = truck, colors = colors)
        }

        item {
            Spacer(modifier = Modifier.height(26.dp))
            TruckDocumentsSection(
                truck = truck,
                colors = colors,
                onViewClick = onViewClick,
                onUploadClick = onUploadClick,
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun TruckHero(
    truck: TruckDetail,
    siding: FleetTone,
    colors: TruckerColors,
) {
    Column(
        modifier =
            Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .fillMaxWidth()
                .background(colors.surface, FleetCardShape)
                .border(1.5.dp, colors.line, FleetCardShape)
                .padding(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                PlateBadgeLarge(
                    registration = truck.registrationNumber,
                    colors = colors,
                )
                Spacer(modifier = Modifier.height(10.dp))
                truck.displayName?.takeIf { it.isNotBlank() }?.let {
                    Text(
                        text = it,
                        style = TruckerType.body,
                        color = colors.inkMuted,
                        maxLines = 1,
                    )
                }
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(horizontalAlignment = Alignment.End) {
                StampSeal(
                    text = truckSidingLabel("AVAILABLE", truck.isBlacklisted),
                    tone = siding,
                    colors = colors,
                )
                Spacer(modifier = Modifier.height(12.dp))
                RigSilhouette(
                    shape = rigShapeFor(truckType = truck.truckType, bodyType = truck.bodyType),
                    tint = colors.brand,
                    modifier = Modifier.size(width = 78.dp, height = 44.dp),
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))
        FleetRule(colors = colors)
        Spacer(modifier = Modifier.height(14.dp))

        VerificationLine(isVerified = truck.isVerified, colors = colors)
    }
}

@Composable
private fun SpecificationsSection(
    truck: TruckDetail,
    colors: TruckerColors,
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        FleetSectionHeader(title = "Specifications", colors = colors)

        Spacer(modifier = Modifier.height(14.dp))

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SpecCell(
                    label = "Body",
                    value = truck.bodyType?.takeIf { it.isNotBlank() } ?: "—",
                    colors = colors,
                    modifier = Modifier.weight(1f),
                )
                SpecCell(
                    label = "Capacity",
                    value = truck.capacityMt?.let { "${it.toString().removeSuffix(".0")} MT" } ?: "—",
                    colors = colors,
                    modifier = Modifier.weight(1f),
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SpecCell(
                    label = "Make",
                    value = truck.make?.takeIf { it.isNotBlank() } ?: "—",
                    colors = colors,
                    modifier = Modifier.weight(1f),
                )
                SpecCell(
                    label = "Model",
                    value = truck.model?.takeIf { it.isNotBlank() } ?: "—",
                    colors = colors,
                    modifier = Modifier.weight(1f),
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SpecCell(
                    label = "Year",
                    value = truck.year?.takeIf { it.isNotBlank() } ?: "—",
                    colors = colors,
                    modifier = Modifier.weight(1f),
                )
                SpecCell(
                    label = "Class",
                    value = truck.truckType?.takeIf { it.isNotBlank() } ?: "—",
                    colors = colors,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun TruckDocumentsSection(
    truck: TruckDetail,
    colors: TruckerColors,
    onViewClick: (String?) -> Unit,
    onUploadClick: (String) -> Unit,
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        FleetSectionHeader(
            title = "Documents",
            colors = colors,
            trailing = {
                Text(
                    text = "${truckDocuments(truck).count { it.second != null }} of 3 lodged",
                    style = TruckerType.caption,
                    color = colors.inkMuted,
                )
            },
        )

        Spacer(modifier = Modifier.height(14.dp))

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            truckDocuments(truck).forEach { (title, doc) ->
                TruckDocumentRow(
                    title = title,
                    doc = doc,
                    colors = colors,
                    onViewClick = onViewClick,
                    onUploadClick = onUploadClick,
                )
            }
        }
    }
}

private fun truckDocuments(truck: TruckDetail): List<Pair<String, TruckDocument?>> =
    listOf(
        "RC Book" to truck.rcCard,
        "Insurance Policy" to truck.insurance,
        "Fitness Certificate" to truck.fcCertificate,
    )

@Composable
private fun TruckDocumentRow(
    title: String,
    doc: TruckDocument?,
    colors: TruckerColors,
    onViewClick: (String?) -> Unit,
    onUploadClick: (String) -> Unit,
) {
    val url = doc?.url?.takeIf { it.isNotBlank() }
    val status = doc?.status?.uppercase()

    val (sealText, tone, subtitle) =
        when {
            url == null ->
                Triple("Required", FleetTone.ACCENT, "Not uploaded yet.")
            status == "VERIFIED" || status == "COMPLETED" ->
                Triple("Verified", FleetTone.BRAND, "Cleared by the Freight24 desk.")
            status == "REJECTED" ->
                Triple("Rejected", FleetTone.ERROR, "Rejected. Upload a fresh copy.")
            status == "PENDING" ->
                Triple("In Review", FleetTone.ACCENT, "Lodged. Waiting on verification.")
            else ->
                Triple("Lodged", FleetTone.MUTED, "On file.")
        }

    DocWalletRow(
        title = title,
        subtitle = subtitle,
        icon = Icons.AutoMirrored.Filled.InsertDriveFile,
        colors = colors,
        sealText = sealText,
        sealTone = tone,
        actionIcon = if (url == null || status == "REJECTED") Icons.Filled.CloudUpload else null,
        actionContentDescription = if (url == null) "Upload $title" else "Upload $title again",
        onActionClick = { onUploadClick(title) },
        onRowClick =
            if (url != null) {
                { onViewClick(url) }
            } else {
                null
            },
    )
}
