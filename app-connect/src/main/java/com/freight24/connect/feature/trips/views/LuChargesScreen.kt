package com.freight24.connect.feature.trips.views

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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import com.freight24.connect.feature.trips.domain.viewmodel.LuChargesEvent
import com.freight24.connect.feature.trips.domain.viewmodel.LuChargesViewModel
import com.freight24.connect.feature.trips.domain.viewmodel.LuChargesViewModelImpl
import com.freight24.connect.feature.trips.domain.viewmodel.LuDocType
import com.freight24.ui.components.FileConfirmationSheet
import com.freight24.ui.components.FileSourcePickerSheet
import com.freight24.ui.theme.FreightThemeTokens
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LuChargesScreen(
    tripId: String,
    onBackClick: () -> Unit,
    onSuccess: () -> Unit,
    onPdfPreview: (String, String?, String?) -> Unit,
    viewModel: LuChargesViewModel = hiltViewModel<LuChargesViewModelImpl>()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    val colorScheme = MaterialTheme.colorScheme
    val extendedColors = FreightThemeTokens.colors
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var showSourcePicker by remember { mutableStateOf(false) }
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }
    val sourceSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(uiState.shouldNavigateBack) {
        if (uiState.shouldNavigateBack) {
            onSuccess()
        }
    }

    LaunchedEffect(uiState.navigateToSignedUrl) {
        uiState.navigateToSignedUrl?.let { url ->
            onPdfPreview("Loading & Unloading Proof", url, null)
            viewModel.onEvent(LuChargesEvent.OnSignedUrlConsumed)
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(),
        onResult = { uris ->
            if (uris.isNotEmpty()) {
                viewModel.onEvent(LuChargesEvent.OnFilePicked(uris, isPdf = false))
            }
        }
    )

    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            uri?.let {
                viewModel.onEvent(LuChargesEvent.OnFilePicked(listOf(it), isPdf = true))
            }
        }
    )

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success) {
                tempCameraUri?.let {
                    viewModel.onEvent(LuChargesEvent.OnFilePicked(listOf(it), isPdf = false))
                }
            }
            tempCameraUri = null
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
                    val result = snackbarHostState.showSnackbar(
                        message = "Camera permission denied. Grant it from app settings.",
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
                pdfPickerLauncher.launch("application/pdf")
            }
        },
        sheetState = sourceSheetState,
    )

    if (uiState.showFileConfirmation) {
        FileConfirmationSheet(
            selectedUris = uiState.selectedUris,
            isPdf = uiState.isPdf,
            isUploading = false,
            uploadButtonText = "Confirm Upload",
            onUploadClick = {
                viewModel.onEvent(LuChargesEvent.OnConfirmUpload)
            },
            onDismiss = {
                viewModel.onEvent(LuChargesEvent.OnClearSelection)
            },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Loading & Unloading", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colorScheme.surface)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            Button(
                onClick = { viewModel.onEvent(LuChargesEvent.OnSubmit) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = extendedColors.successColor),
                enabled = !uiState.isSubmitting && uiState.hasChanges
            ) {
                if (uiState.isSubmitting) {
                    CircularProgressIndicator(color = colorScheme.onPrimary, modifier = Modifier.size(24.dp))
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Update Charges", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
        },
        containerColor = colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Info Message
            Surface(
                color = extendedColors.successColor.copy(alpha = 0.12f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = extendedColors.successColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "All submitted charges and documents are subject to administrative verification and internal audit. Please ensure all details are accurate.",
                        style = MaterialTheme.typography.bodySmall,
                        color = extendedColors.successColor,
                        lineHeight = 18.sp
                    )
                }
            }

            // Amounts Section
            Card(
                colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    LuAmountInput(
                        label = "Loading Amount (₹)",
                        value = uiState.loadingAmount,
                        onValueChange = { viewModel.onEvent(LuChargesEvent.OnLoadingAmountChange(it)) }
                    )

                    LuAmountInput(
                        label = "Unloading Amount (₹)",
                        value = uiState.unloadingAmount,
                        onValueChange = { viewModel.onEvent(LuChargesEvent.OnUnloadingAmountChange(it)) }
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "Notes",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = colorScheme.onSurfaceVariant
                        )
                        OutlinedTextField(
                            value = uiState.notes,
                            onValueChange = { viewModel.onEvent(LuChargesEvent.OnNotesChange(it)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            placeholder = { Text("Enter any additional details or remarks here...", color = colorScheme.onSurfaceVariant) },
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = extendedColors.successColor,
                                unfocusedBorderColor = colorScheme.outlineVariant
                            )
                        )
                    }
                }
            }

            // Documents Section
            Card(
                colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "Supporting Documents",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    val loadingHasDoc = uiState.loadingDocument != null || uiState.loadingSlipUrl != null
                    val loadingFileSize = uiState.loadingDocument?.length()
                        ?.let { formatBytes(it) } ?: ""

                    LuDocumentRow(
                        title = when {
                            uiState.loadingProcessing -> "Loading Charges Proof"
                            loadingHasDoc && !uiState.loadingShowReupload -> "View Loading Charges Proof"
                            uiState.loadingShowReupload -> "Reupload Loading Charges Proof"
                            else -> "Upload Loading Charges Proof"
                        },
                        subtitle = when {
                            uiState.loadingProcessing -> "Processing file..."
                            uiState.loadingSlipUrl != null -> "Uploaded on server"
                            uiState.loadingDocument != null -> "Ready to upload ($loadingFileSize)"
                            else -> "Tap to upload"
                        },
                        isProcessing = uiState.loadingProcessing,
                        hasDocument = loadingHasDoc,
                        onUploadClick = {
                            viewModel.onEvent(LuChargesEvent.OnShowSourcePicker(LuDocType.LOADING))
                            showSourcePicker = true
                        },
                        onDeleteClick = {
                            viewModel.onEvent(LuChargesEvent.OnDeleteDoc(LuDocType.LOADING))
                        },
                        onViewClick = {
                            if (uiState.loadingSlipUrl != null) {
                                viewModel.onEvent(LuChargesEvent.OnRemoteDocClick("LOADING_SLIP"))
                            } else {
                                uiState.loadingDocument?.let {
                                    onPdfPreview("Loading Proof", null, it.absolutePath)
                                }
                            }
                        },
                    )

                    HorizontalDivider(color = colorScheme.outlineVariant.copy(alpha = 0.5f))

                    val unloadingHasDoc = uiState.unloadingDocument != null || uiState.unloadingSlipUrl != null
                    val unloadingFileSize = uiState.unloadingDocument?.length()
                        ?.let { formatBytes(it) } ?: ""

                    LuDocumentRow(
                        title = when {
                            uiState.unloadingProcessing -> "Unloading Charges Proof"
                            unloadingHasDoc && !uiState.unloadingShowReupload -> "View Unloading Charges Proof"
                            uiState.unloadingShowReupload -> "Reupload Unloading Charges Proof"
                            else -> "Upload Unloading Charges Proof"
                        },
                        subtitle = when {
                            uiState.unloadingProcessing -> "Processing file..."
                            uiState.unloadingSlipUrl != null -> "Uploaded on server"
                            uiState.unloadingDocument != null -> "Ready to upload ($unloadingFileSize)"
                            else -> "Tap to upload"
                        },
                        isProcessing = uiState.unloadingProcessing,
                        hasDocument = unloadingHasDoc,
                        onUploadClick = {
                            viewModel.onEvent(LuChargesEvent.OnShowSourcePicker(LuDocType.UNLOADING))
                            showSourcePicker = true
                        },
                        onDeleteClick = {
                            viewModel.onEvent(LuChargesEvent.OnDeleteDoc(LuDocType.UNLOADING))
                        },
                        onViewClick = {
                            if (uiState.unloadingSlipUrl != null) {
                                viewModel.onEvent(LuChargesEvent.OnRemoteDocClick("UNLOADING_SLIP"))
                            } else {
                                uiState.unloadingDocument?.let {
                                    onPdfPreview("Unloading Proof", null, it.absolutePath)
                                }
                            }
                        },
                    )
                }
            }
        }
    }
}

@Composable
fun LuAmountInput(
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val extendedColors = FreightThemeTokens.colors
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = colorScheme.onSurfaceVariant
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = {
                Text("₹", color = colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            },
            placeholder = { Text("0.00", color = colorScheme.onSurfaceVariant) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = extendedColors.successColor,
                unfocusedBorderColor = colorScheme.outlineVariant
            ),
            singleLine = true
        )
    }
}

@Composable
private fun LuDocumentRow(
    title: String,
    subtitle: String = "",
    isProcessing: Boolean,
    hasDocument: Boolean,
    onUploadClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onViewClick: () -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = hasDocument && !isProcessing) { onViewClick() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        when {
            isProcessing -> {
                LinearProgressIndicator(
                    modifier = Modifier.width(88.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.CloudUpload,
                    contentDescription = "Uploading",
                    tint = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(20.dp),
                )
            }
            hasDocument -> {
                IconButton(onClick = onDeleteClick) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error,
                    )
                }
            }
            else -> {
                IconButton(onClick = onUploadClick) {
                    Icon(
                        imageVector = Icons.Default.CloudUpload,
                        contentDescription = "Upload",
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
    }
}

private fun formatBytes(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "${bytes / 1024} KB"
        else -> "%.1f MB".format(bytes / (1024.0 * 1024.0))
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

@Composable
fun border(width: androidx.compose.ui.unit.Dp, color: Color) =
    androidx.compose.foundation.BorderStroke(width, color)

private fun openAppSettings(context: Context) {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", context.packageName, null)
    }
    context.startActivity(intent)
}
