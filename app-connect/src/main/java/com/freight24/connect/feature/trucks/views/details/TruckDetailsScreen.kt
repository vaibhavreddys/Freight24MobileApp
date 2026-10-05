package com.freight24.connect.feature.trucks.views.details
import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.freight24.common.model.TruckDetail
import com.freight24.common.model.TruckDocument
import com.freight24.connect.feature.trucks.views.TruckDocketCard
import com.freight24.ui.UiState
import com.freight24.ui.components.FreightTopAppBar
import com.freight24.ui.components.PanelShape
import com.freight24.ui.components.TagShape
import com.freight24.ui.components.getPdfLaunchIntent
import com.freight24.ui.theme.FreightThemeTokens
import kotlinx.coroutines.launch
import java.io.File

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
    val colorScheme = MaterialTheme.colorScheme

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

    com.freight24.ui.components.FileSourcePickerSheet(
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
        com.freight24.ui.components.FileConfirmationSheet(
            selectedUris = selectedUris,
            isPdf = viewModel.isPdf.value,
            isUploading = isUploading,
            uploadButtonText = "Upload Now",
            onUploadClick = { viewModel.uploadDocuments() },
            onDismiss = { viewModel.clearSelection() },
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            FreightTopAppBar(
                title = {
                    Text(
                        text = "Truck Details",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = colorScheme.surface,
                shadowElevation = 8.dp,
            ) {
                Button(
                    onClick = {
                        val state = uiState
                        if (state is UiState.Success) {
                            onEditClick(state.data.truckId)
                        }
                    },
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            .height(50.dp)
                            .testTag("truck_edit_button")
                            .semantics { testTagsAsResourceId = true },
                    shape = RoundedCornerShape(25.dp),
                    enabled = uiState is UiState.Success,
                ) {
                    Text("Edit Truck", fontWeight = FontWeight.SemiBold)
                }
            }
        },
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { viewModel.onRefresh() },
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.background),
        ) {
            when (val state = uiState) {
                is UiState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is UiState.Error -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(state.message, color = MaterialTheme.colorScheme.error)
                            Button(onClick = { viewModel.fetchTruckDetail(true) }) {
                                Text("Retry")
                            }
                        }
                    }
                }
                is UiState.Success -> {
                    Box(modifier = Modifier.fillMaxSize()) {
                        TruckDetailsContent(
                            truck = state.data,
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
                            onEditClick = onEditClick,
                        )

                        if (isUploading) {
                            Surface(
                                color = colorScheme.onSurface.copy(alpha = 0.5f),
                                modifier = Modifier.fillMaxSize(),
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        CircularProgressIndicator(color = colorScheme.onPrimary)
                                        Spacer(Modifier.height(16.dp))
                                        Text("Uploading Document...", color = colorScheme.onPrimary)
                                    }
                                }
                            }
                        }
                    }
                }
                else -> {}
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
private fun TruckDetailsContent(
    truck: TruckDetail,
    onViewClick: (String?) -> Unit,
    onUploadClick: (String) -> Unit,
    onEditClick: (String) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            TruckDocketCard(
                registrationNumber = truck.registrationNumber,
                truckType = truck.truckType,
                availability = null, // detail payload carries no availability
                isVerified = truck.isVerified,
                isBlacklisted = truck.isBlacklisted,
                testTag = "truck_detail_card",
            )
        }

        item {
            SpecificationsSection(truck)
        }

        item {
            DocumentsSection(truck, onViewClick, onUploadClick)
        }

        item {
            Spacer(Modifier.height(4.dp))
        }
    }
}

@Composable
private fun SpecificationsSection(truck: TruckDetail) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = PanelShape,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
    ) {
        Column(modifier = Modifier.padding(vertical = 10.dp)) {
            Text(
                text = "SPECIFICATIONS",
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.1.sp),
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
            )
            LedgerRow("BODY TYPE", truck.bodyType)
            LedgerDivider()
            LedgerRow("MAKE", truck.make)
            LedgerDivider()
            LedgerRow("MODEL", truck.model)
            LedgerDivider()
            LedgerRow("TRUCK TYPE", truck.truckType)
        }
    }
}

@Composable
private fun LedgerRow(
    label: String,
    value: String?,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.1.sp),
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(3.dp))
        Text(
            text = value?.takeIf { it.isNotBlank() } ?: "—",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            color = if (value.isNullOrBlank()) MaterialTheme.colorScheme.outlineVariant else MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun LedgerDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 16.dp),
        thickness = 1.dp,
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
    )
}

@Composable
private fun DocumentsSection(
    truck: TruckDetail,
    onViewClick: (String?) -> Unit,
    onUploadClick: (String) -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = PanelShape,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
    ) {
        Column(modifier = Modifier.padding(vertical = 10.dp)) {
            Text(
                text = "DOCUMENTS",
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.1.sp),
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
            )
            TruckDocumentRow("RC Book", truck.rcCard, onViewClick, onUploadClick)
            LedgerDivider()
            TruckDocumentRow("Insurance Policy", truck.insurance, onViewClick, onUploadClick)
            LedgerDivider()
            TruckDocumentRow("Fitness Certificate", truck.fcCertificate, onViewClick, onUploadClick)
        }
    }
}

@Composable
private fun TruckDocumentRow(
    title: String,
    doc: TruckDocument?,
    onViewClick: (String?) -> Unit,
    onUploadClick: (String) -> Unit,
) {
    val hasUrl = !doc?.url.isNullOrEmpty()
    val statusText = doc?.status?.uppercase() ?: "NOT UPLOADED"

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .then(
                    if (hasUrl) {
                        Modifier.clickable { onViewClick(doc?.url) }
                    } else {
                        Modifier
                    },
                )
                .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text =
                    when {
                        !hasUrl -> "Not uploaded"
                        statusText == "VERIFIED" || statusText == "COMPLETED" -> "On file"
                        statusText == "PENDING" -> "Awaiting verification"
                        statusText == "REJECTED" -> "Rejected — upload a fresh copy"
                        else -> "On file"
                    },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        when {
            !hasUrl ->
                DocumentActionIconButton(
                    icon = Icons.Default.CloudUpload,
                    description = "Upload $title",
                    tint = MaterialTheme.colorScheme.primary,
                    onClick = { onUploadClick(title) },
                    testTag = "truck_doc_upload_${title.lowercase().replace(' ', '_')}",
                )
            statusText == "REJECTED" ->
                DocumentActionIconButton(
                    icon = Icons.Default.Refresh,
                    description = "Re-upload $title",
                    tint = MaterialTheme.colorScheme.error,
                    onClick = { onUploadClick(title) },
                    testTag = "truck_doc_upload_${title.lowercase().replace(' ', '_')}",
                )
            else -> {
                TruckDocumentStamp(statusText)
                Spacer(Modifier.width(6.dp))
                DocumentActionIconButton(
                    icon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    description = "View $title",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    onClick = { onViewClick(doc?.url) },
                    testTag = "truck_doc_view_${title.lowercase().replace(' ', '_')}",
                )
            }
        }
    }
}

@Composable
private fun TruckDocumentStamp(statusText: String) {
    val ext = FreightThemeTokens.colors
    val scheme = MaterialTheme.colorScheme
    val (container, content, label) =
        when (statusText) {
            "VERIFIED", "COMPLETED" -> Triple(ext.surfaceSuccess, ext.onSurfaceSuccess, "VERIFIED")
            "REJECTED" -> Triple(scheme.errorContainer, scheme.onErrorContainer, "REJECTED")
            "PENDING" -> Triple(ext.surfaceWarning, ext.onSurfaceWarning, "PENDING")
            else -> Triple(scheme.surfaceContainerHighest, scheme.onSurfaceVariant, "UPLOADED")
        }
    Surface(
        shape = TagShape,
        color = container,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
            fontWeight = FontWeight.Bold,
            color = content,
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
        )
    }
}

@Composable
private fun DocumentActionIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    description: String,
    tint: Color,
    onClick: () -> Unit,
    testTag: String,
) {
    IconButton(
        onClick = onClick,
        modifier =
            Modifier
                .size(36.dp)
                .testTag(testTag),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = description,
            tint = tint,
            modifier = Modifier.size(20.dp),
        )
    }
}
