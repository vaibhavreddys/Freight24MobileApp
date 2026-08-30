package com.freight24.trucker.feature.trucks.view.details
import com.freight24.ui.components.FreightTopAppBar

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil3.compose.rememberAsyncImagePainter
import com.freight24.common.model.TruckDetail
import com.freight24.common.model.TruckDocument
import com.freight24.ui.UiState
import com.freight24.ui.components.DocumentStatus
import com.freight24.ui.components.DocumentUploadItem
import com.freight24.ui.components.TruckHeaderCard
import com.freight24.ui.components.getPdfLaunchIntent
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

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(),
        onResult = { uris ->
            if (uris.isNotEmpty()) {
                viewModel.onFilesPicked(uris, false, currentDocumentType ?: "")
                currentDocumentType = null
            }
        }
    )

    val documentPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            uri?.let {
                viewModel.onFilesPicked(listOf(it), true, currentDocumentType ?: "")
                currentDocumentType = null
            }
        }
    )

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success) {
                tempCameraUri?.let {
                    viewModel.onFilesPicked(listOf(it), false, currentDocumentType ?: "")
                }
            }
            tempCameraUri = null
            currentDocumentType = null
        }
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
        }
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
        sheetState = sourceSheetState
    )

    if (selectedUris.isNotEmpty()) {
        com.freight24.ui.components.FileConfirmationSheet(
            selectedUris = selectedUris,
            isPdf = viewModel.isPdf.value,
            isUploading = isUploading,
            uploadButtonText = "Upload Now",
            onUploadClick = { viewModel.uploadDocuments() },
            onDismiss = { viewModel.clearSelection() }
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
                    IconButton(
                        onClick = onBackClick,
                        modifier =
                            Modifier
                                .testTag("truck_details_back_btn")
                                .semantics { testTagsAsResourceId = true },
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
        bottomBar = {
            val state = uiState
            if (state is UiState.Success && !state.data.isVerified) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(top = 12.dp, bottom = 24.dp),
                    color = Color.Transparent
                ) {
                    Button(
                        onClick = { onEditClick(state.data.truckId) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .semantics(mergeDescendants = true) { contentDescription = "truck_edit_button" },
                        shape = RoundedCornerShape(50.dp),
                    ) {
                        Text("Edit Truck", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
        }
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { viewModel.onRefresh() },
            modifier = Modifier
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
                                if (!s3Url.isNullOrEmpty()) {
                                    viewModel.fetchSignedUrl(s3Url) { signedUrl ->
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
                                color = Color.Black.copy(alpha = 0.5f),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        CircularProgressIndicator(color = Color.White)
                                        Spacer(Modifier.height(16.dp))
                                        Text("Uploading Document...", color = Color.White)
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
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            TruckHeaderCard(
                registrationNumber = truck.registrationNumber,
                truckType = truck.truckType,
                availability = "AVAILABLE",
                isVerified = truck.isVerified,
                isBlacklisted = truck.isBlacklisted,
            )
        }

        item {
            SpecificationsSection(truck)
        }

        item {
            DocumentsSection(truck, onViewClick, onUploadClick)
        }
    }
}

@Composable
private fun SpecificationsSection(truck: TruckDetail) {
    Card(
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Specifications",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
            Spacer(Modifier.height(16.dp))

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    SpecItem(Modifier.weight(1f), "BODY TYPE", truck.bodyType ?: "NA")
                    SpecItem(Modifier.weight(1f), "MAKE", truck.make ?: "NA")
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    SpecItem(Modifier.weight(1f), "MODEL", truck.model ?: "NA")
                    SpecItem(Modifier.weight(1f), "TRUCK TYPE", truck.truckType ?: "NA")
                }
            }
        }
    }
}

@Composable
private fun SpecItem(
    modifier: Modifier,
    label: String,
    value: String,
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(8.dp),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                fontSize = 10.sp,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun DocumentsSection(
    truck: TruckDetail,
    onViewClick: (String?) -> Unit,
    onUploadClick: (String) -> Unit,
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.FolderOpen,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Documents",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
            Spacer(Modifier.height(16.dp))

            DocumentItem("RC Book", truck.rcCard, onViewClick, onUploadClick)
            DocumentItem("Insurance Policy", truck.insurance, onViewClick, onUploadClick)
            DocumentItem("Fitness Certificate", truck.fcCertificate, onViewClick, onUploadClick)
        }
    }
}

@Composable
private fun DocumentItem(
    title: String,
    doc: TruckDocument?,
    onViewClick: (String?) -> Unit,
    onUploadClick: (String) -> Unit,
) {
    val hasUrl = !doc?.url.isNullOrEmpty()
    val statusText = doc?.status?.uppercase() ?: "NOT UPLOADED"

    val subtitle = if (!hasUrl) "Not Uploaded" else
        when (statusText) {
        "VERIFIED", "COMPLETED" -> "Verified"
        "PENDING" -> "Verification Pending"
        "REJECTED" -> "Rejected. Please upload again."
        else -> ""
    }

    println("")

    val docStatus = if (hasUrl) {
        DocumentStatus.Uploaded(
            id = doc!!.url!!,
            label = statusText,
            backgroundColor = Color.Transparent, // Not used when chip is hidden
            textColor = Color.Transparent,
            canReupload = statusText == "REJECTED" || statusText == "NOT UPLOADED"
        )
    } else {
        DocumentStatus.Pending
    }

    DocumentUploadItem(
        title = title,
        subtitle = subtitle,
        status = docStatus,
        onUploadClick = { onUploadClick(title) },
        onViewClick = { onViewClick(it) },
        showStatusChip = false,
        trailingIcon = if (hasUrl) Icons.AutoMirrored.Filled.KeyboardArrowRight else null
    )
}
