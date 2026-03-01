package com.freight24.connect.feature.trips.views

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
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.freight24.connect.feature.trips.domain.viewmodel.LuChargesEvent
import com.freight24.connect.feature.trips.domain.viewmodel.LuChargesViewModel
import com.freight24.connect.feature.trips.domain.viewmodel.LuChargesViewModelImpl
import com.freight24.connect.feature.trips.domain.viewmodel.LuDocType
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

    var showFileSourceDialog by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
        }
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
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
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                enabled = !uiState.isSubmitting && (uiState.loadingAmount.isNotBlank() || uiState.unloadingAmount.isNotBlank())
            ) {
                if (uiState.isSubmitting) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Update Charges", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
        },
        containerColor = Color(0xFFF8FAFC)
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
                color = Color(0xFFECFDF5),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "All submitted charges and documents are subject to administrative verification and internal audit. Please ensure all details are accurate.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF065F46),
                        lineHeight = 18.sp
                    )
                }
            }

            // Amounts Section
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
//                    Row(verticalAlignment = Alignment.CenterVertically) {
//                        Box(
//                            modifier = Modifier
//                                .width(4.dp)
//                                .height(24.dp)
//                                .background(Color(0xFF10B981), RoundedCornerShape(2.dp))
//                        )
//
//                        Spacer(modifier = Modifier.width(12.dp))
//
//                        Text(
//                            "Loading/Unloading Charges",
//                            fontWeight = FontWeight.Bold,
//                            fontSize = 18.sp,
//                            color = Color(0xFF1E293B)
//                        )
//                    }

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
                            color = Color(0xFF64748B)
                        )
                        OutlinedTextField(
                            value = uiState.notes,
                            onValueChange = { viewModel.onEvent(LuChargesEvent.OnNotesChange(it)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            placeholder = { Text("Enter any additional details or remarks here...", color = Color(0xFF94A3B8)) },
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF10B981),
                                unfocusedBorderColor = Color(0xFFE2E8F0)
                            )
                        )
                    }
                }
            }

            // Documents Section
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        "Supporting Documents",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color(0xFF1E293B)
                    )

                    if (uiState.loadingDocument != null || uiState.loadingSlipUrl != null) {
                        DocumentItem(
                            file = uiState.loadingDocument,
                            remoteUrl = uiState.loadingSlipUrl,
                            label = "Loading Proof",
                            onDelete = { viewModel.onEvent(LuChargesEvent.OnDeleteDoc(LuDocType.LOADING)) },
                            onPreview = { 
                                if (uiState.loadingDocument != null) {
                                    onPdfPreview("Loading Proof", null, uiState.loadingDocument!!.absolutePath)
                                } else if (uiState.loadingSlipUrl != null) {
                                    viewModel.onEvent(LuChargesEvent.OnRemoteDocClick("LOADING_SLIP"))
                                }
                            }
                        )
                    }

                    if (uiState.unloadingDocument != null || uiState.unloadingSlipUrl != null) {
                        DocumentItem(
                            file = uiState.unloadingDocument,
                            remoteUrl = uiState.unloadingSlipUrl,
                            label = "Unloading Proof",
                            onDelete = { viewModel.onEvent(LuChargesEvent.OnDeleteDoc(LuDocType.UNLOADING)) },
                            onPreview = { 
                                if (uiState.unloadingDocument != null) {
                                    onPdfPreview("Unloading Proof", null, uiState.unloadingDocument!!.absolutePath)
                                } else if (uiState.unloadingSlipUrl != null) {
                                    viewModel.onEvent(LuChargesEvent.OnRemoteDocClick("UNLOADING_SLIP"))
                                }
                            }
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .border(
                                width = 1.dp,
                                color = Color(0xFFCBD5E1),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { viewModel.onEvent(LuChargesEvent.OnUploadClick) },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.Upload,
                                contentDescription = null,
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Tap to upload proof",
                                color = Color(0xFF64748B),
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }

    // Document Type Selection Sheet
    if (uiState.showDocTypeSheet) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.onEvent(LuChargesEvent.OnDismissSheet) },
            sheetState = rememberModalBottomSheetState(),
            containerColor = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp, bottom = 48.dp, top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Text(
                    "Select Document Type",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color(0xFF1E293B),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                DocTypeSelectionItem(
                    title = "Loading Charge Proof",
                    icon = Icons.Outlined.FileUpload,
                    selected = uiState.selectedDocType == LuDocType.LOADING,
                    onClick = { viewModel.onEvent(LuChargesEvent.OnDocTypeSelected(LuDocType.LOADING)) }
                )

                DocTypeSelectionItem(
                    title = "Unloading Charge Proof",
                    icon = Icons.Outlined.FileDownload,
                    selected = uiState.selectedDocType == LuDocType.UNLOADING,
                    onClick = { viewModel.onEvent(LuChargesEvent.OnDocTypeSelected(LuDocType.UNLOADING)) }
                )

                Button(
                    onClick = { /* Already handled in DocTypeSelectionItem but following mock button presence */ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                ) {
                    Text("Select", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }

    // File Source Selection Dialog (Image vs PDF)
    if (uiState.showFilePicker) {
        AlertDialog(
            onDismissRequest = { viewModel.onEvent(LuChargesEvent.OnDismissSheet) },
            title = { Text("Select File Type") },
            text = { Text("Choose how you want to pick the document proof.") },
            confirmButton = {
                TextButton(onClick = { imagePickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }) {
                    Text("Images", color = Color(0xFF10B981))
                }
            },
            dismissButton = {
                TextButton(onClick = { pdfPickerLauncher.launch("application/pdf") }) {
                    Text("PDF", color = Color(0xFF10B981))
                }
            }
        )
    }
}

@Composable
fun LuAmountInput(
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF64748B)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = {
                Text("₹", color = Color(0xFF64748B), fontWeight = FontWeight.Bold, fontSize = 18.sp)
            },
            placeholder = { Text("0.00", color = Color(0xFF94A3B8)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF10B981),
                unfocusedBorderColor = Color(0xFFE2E8F0)
            ),
            singleLine = true
        )
    }
}

@Composable
fun DocumentItem(
    file: File? = null,
    remoteUrl: String? = null,
    label: String,
    onDelete: () -> Unit,
    onPreview: () -> Unit
) {
    Surface(
        onClick = onPreview,
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFF1F5F9),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color.White,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (remoteUrl != null) Icons.Default.CloudDone
                                      else if (file?.extension == "pdf") Icons.Outlined.PictureAsPdf 
                                      else Icons.Outlined.Image,
                        contentDescription = null,
                        tint = if (remoteUrl != null) Color(0xFF10B981) else Color(0xFF94A3B8),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (remoteUrl != null) "$label (Existing)" else "${label}_${file?.name?.takeLast(8)}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1E293B)
                )
                Text(
                    text = if (remoteUrl != null) "Uploaded on server" else "${(file?.length() ?: 0) / 1024} KB",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF64748B)
                )
            }
            
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFEF4444), modifier = Modifier.size(20.dp))
            }
        }
    }
}
@Composable
fun DocTypeSelectionItem(
    title: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = if (selected) Color(0xFFECFDF5) else Color(0xFFF8FAFC),
        border = if (selected) border(1.dp, Color(0xFF10B981)) else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = Color.White,
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color(0xFF1E293B),
                modifier = Modifier.weight(1f)
            )
            
            RadioButton(
                selected = selected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF10B981))
            )
        }
    }
}

// Extension to avoid repetitive border definition
@Composable
fun border(width: androidx.compose.ui.unit.Dp, color: Color) =
    androidx.compose.foundation.BorderStroke(width, color)
