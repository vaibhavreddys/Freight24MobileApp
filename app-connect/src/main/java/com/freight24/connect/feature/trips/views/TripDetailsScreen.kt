package com.freight24.connect.feature.trips.views
import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import com.freight24.ui.components.FreightTopAppBar

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.outlined.BorderColor
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.freight24.connect.feature.trips.domain.viewmodel.TripDetailsEvent
import com.freight24.connect.feature.trips.domain.viewmodel.TripDetailsViewModel
import com.freight24.connect.feature.trips.domain.viewmodel.TripDetailsViewModelImpl
import com.freight24.connect.feature.trips.views.components.TripCard
import com.freight24.connect.feature.trips.views.components.formatAmount
import com.freight24.ui.components.FileConfirmationSheet
import com.freight24.ui.components.FileSourcePickerSheet
import com.freight24.connect.utils.toDateTime
import com.freight24.rest.response.TripDataResponse
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripDetailsScreen(
    navController: androidx.navigation.NavController,
    tripId: String,
    onBackClick: () -> Unit,
    onEditClick: (String) -> Unit,
    onDocumentView : (String, String, String) -> Unit,
    onHaltingClick: (String, String, Int, Int, String?) -> Unit,
    onLuClick: (String, String, String?, String?, String?, String?, String?) -> Unit,
    onOtherDeductionsClick: (String, String?, String?) -> Unit,
    viewModel: TripDetailsViewModel = hiltViewModel<TripDetailsViewModelImpl>()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val colorScheme = MaterialTheme.colorScheme
    var menuExpanded by remember { mutableStateOf(false) }
    var showConfirmationDialog by remember { mutableStateOf(false) }
    var pendingStatus by remember { mutableStateOf("") }
    var pendingButtonText by remember { mutableStateOf("") }
    
    // Refresh trip details when returning from PDF viewer via navigation result
    val refreshTrigger = navController.currentBackStackEntry
        ?.savedStateHandle
        ?.getStateFlow("refresh", false)
        ?.collectAsStateWithLifecycle()

    LaunchedEffect(tripId, refreshTrigger?.value) {
        if (refreshTrigger?.value == true) {
            viewModel.onEvent(TripDetailsEvent.Refresh)
            navController.currentBackStackEntry?.savedStateHandle?.set("refresh", false)
        }
    }
    
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val sourceSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showSourcePicker by rememberSaveable { mutableStateOf(false) }
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(),
        onResult = { uris ->
            if (uris.isNotEmpty()) {
                viewModel.onEvent(TripDetailsEvent.OnFilesPicked(uris, isPdf = false))
            }
        },
    )

    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            uri?.let {
                viewModel.onEvent(TripDetailsEvent.OnFilesPicked(listOf(it), isPdf = true))
            }
        },
    )

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success) {
                tempCameraUri?.let {
                    viewModel.onEvent(TripDetailsEvent.OnFilesPicked(listOf(it), isPdf = false))
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
                    val result = snackbarHostState.showSnackbar(
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

    if (showConfirmationDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmationDialog = false },
            title = { Text("Confirm Status Update") },
            text = { Text("Are you sure you want to update the trip status to ${pendingButtonText.replace("Update to ", "")}?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.onEvent(TripDetailsEvent.UpdateStatus(newStatus = pendingStatus))
                        showConfirmationDialog = false
                    }
                ) {
                    Text("Confirm", color = colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmationDialog = false }) {
                    Text("Cancel", color = colorScheme.onSurfaceVariant)
                }
            },
            containerColor = colorScheme.surface,
            shape = MaterialTheme.shapes.medium
        )
    }

    // Cancel Trip - Confirmation dialog (for ASSIGNED / LOADING)
    if (uiState.isCancelDialogVisible) {
        AlertDialog(
            onDismissRequest = { viewModel.onEvent(TripDetailsEvent.HideCancelDialog) },
            title = { Text("Cancel Trip") },
            text = {
                Column {
                    Text("Please provide a reason for cancelling this trip.")
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = uiState.cancelReason,
                        onValueChange = { viewModel.onEvent(TripDetailsEvent.UpdateCancelReason(it)) },
                        label = { Text("Reason") },
                        placeholder = { Text("Enter cancellation reason…") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = false,
                        minLines = 3,
                        shape = RoundedCornerShape(8.dp),
                        enabled = !uiState.isCanceling
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.onEvent(TripDetailsEvent.ConfirmCancelTrip) },
                    enabled = !uiState.isCanceling && uiState.cancelReason.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = colorScheme.error)
                ) {
                    if (uiState.isCanceling) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = colorScheme.onError, strokeWidth = 2.dp)
                        Spacer(Modifier.width(8.dp))
                    }
                    Text("Confirm Cancel")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onEvent(TripDetailsEvent.HideCancelDialog) }) {
                    Text("No", color = colorScheme.onSurfaceVariant)
                }
            },
            containerColor = colorScheme.surface,
            shape = MaterialTheme.shapes.medium
        )
    }

    // Cancel not allowed dialog
    uiState.cancelNotAllowedMessage?.let { status ->
        AlertDialog(
            onDismissRequest = { viewModel.onEvent(TripDetailsEvent.DismissCancelNotAllowed) },
            title = { Text("Cannot Cancel Trip") },
            text = { Text("You are not allowed to cancel the trip while it is in \"$status\" state.") },
            confirmButton = {
                TextButton(onClick = { viewModel.onEvent(TripDetailsEvent.DismissCancelNotAllowed) }) {
                    Text("OK", color = MaterialTheme.colorScheme.primary)
                }
            },
            containerColor = colorScheme.surface,
            shape = MaterialTheme.shapes.medium
        )
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

    FileConfirmationSheet(
        selectedUris = uiState.selectedUris,
        isPdf = uiState.isPdf,
        isUploading = uiState.isUploading,
        uploadButtonText = "Confirm Upload",
        onUploadClick = {
            viewModel.onEvent(TripDetailsEvent.UploadLR)
        },
        onDismiss = {
            viewModel.onEvent(TripDetailsEvent.OnClearSelection)
        },
    )

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            FreightTopAppBar(
                title = { Text("Trip Details", style = MaterialTheme.typography.titleMedium) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More options")
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false },
                            modifier = Modifier.background(colorScheme.surface)
                        ) {
                            DropdownMenuItem(
                                text = { Text("Edit") },
                                leadingIcon = {
                                    Icon(Icons.Outlined.BorderColor, contentDescription = "Edit", tint = colorScheme.onSurfaceVariant)
                                },
                                onClick = {
                                    menuExpanded = false
                                    onEditClick(tripId)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Cancel", color = colorScheme.error) },
                                leadingIcon = {
                                    Icon(Icons.Outlined.Delete, contentDescription = "Cancel Trip", tint = colorScheme.error)
                                },
                                onClick = {
                                    menuExpanded = false
                                    viewModel.onEvent(TripDetailsEvent.ShowCancelDialog)
                                }
                            )
                        }
                    }
                },
            )
        },
        bottomBar = {
            val trip = uiState.trip
            val truckerStatus = trip?.truckerStatus ?: ""
            val showTripStatus = (trip?.status ?: "").equals("active", true)
            val (buttonText, nextStatus) = when (truckerStatus.uppercase()) {
                "ASSIGNED" -> "Update to LOADING" to "LOADING"
                "LOADING" -> "Upload LR" to "UPLOAD_LR"
                "IN_TRANSIT" -> "Update to UNLOADED" to "UNLOADED"
                else -> null to null
            }

            if (buttonText != null && nextStatus != null && showTripStatus) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shadowElevation = 8.dp,
//                    color = colorScheme.onPrimary
                ) {
                    Button(
                        onClick = {
                            if (nextStatus == "UPLOAD_LR") {
                                showSourcePicker = true
                            } else {
                                pendingStatus = nextStatus
                                pendingButtonText = buttonText
                                showConfirmationDialog = true
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (nextStatus == "UPLOAD_LR") colorScheme.primary else colorScheme.primary
                        )
                    ) {
                        Text(buttonText, color = colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(colorScheme.background)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                uiState.errorMessage != null -> {
                    Text(
                        text = uiState.errorMessage ?: "Unknown error",
                        color = colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                uiState.trip != null -> {
                    TripDetailsContent(
                        trip = uiState.trip!!,
                        onUploadLrClick = { showSourcePicker = true },
                        onDocClick = { docId, status ->
                            onDocumentView(tripId, docId, status)
                        },
                        onHaltingClick = { tripId, truckTypeId, loadingDays, unloadingDays, notes ->
                            onHaltingClick(tripId, truckTypeId, loadingDays, unloadingDays, notes)
                        },
                        onLuClick = { loading, unloading, loadingSlipUrl, unloadingSlipUrl, loadingId, unloadingId, notes ->
                            onLuClick(loading, unloading, loadingSlipUrl, unloadingSlipUrl, loadingId, unloadingId, notes)
                        },
                        onOtherDeductionsClick = onOtherDeductionsClick
                    )
                }
            }
        }
    }
}

@Composable
fun TripDetailsContent(
    trip: TripDataResponse,
    onUploadLrClick: () -> Unit,
    onDocClick: (String, String) -> Unit,
    onHaltingClick: (String, String, Int, Int, String?) -> Unit,
    onLuClick: (String, String, String?, String?, String?, String?, String?) -> Unit,
    onOtherDeductionsClick: (String, String?, String?) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Reused Trip Card
        TripCard(trip = trip)


        // Trip Documents — auto-expand to cue the user when LR needs attention
        val lrDoc = (trip.documents ?: emptyList()).find { it.type.equals("lr", ignoreCase = true) }
        val isLrMissing = lrDoc == null
        val isLrPending = lrDoc?.status?.lowercase() == "pending"
        val isLrRejected = lrDoc?.status?.lowercase() == "rejected"
        ExpandableSection(
            title = "Documents",
            icon = Icons.Outlined.Description,
            iconBg = colorScheme.primaryContainer,
            iconTint = colorScheme.primary,
            initiallyExpanded = isLrMissing || isLrPending || isLrRejected,
        ) {
            TripDocumentsSection(
                truckerStatus = trip.truckerStatus,
                documents = trip.documents ?: emptyList(),
                onUploadLrClick = onUploadLrClick,
                onDocClick = onDocClick
            )
        }

        // Charges & Deductions
        ExpandableSection(
            title = "Charges & Deductions",
            icon = Icons.Outlined.AccountBalanceWallet,
            iconBg = colorScheme.primaryContainer,
            iconTint = colorScheme.primary
        ) {
            ChargesAndDeductionsContent(
                financials = trip.financials,
                onHaltingClick = {
                    val truckTypeId = trip.indentDetails.truckTypeId
                    val loadingDays = trip.financials?.halting?.loading?.days ?: 0
                    val unloadingDays = trip.financials?.halting?.unloading?.days ?: 0
                    val notes = trip.financials?.halting?.loading?.notes
                    onHaltingClick(trip.id, truckTypeId, loadingDays, unloadingDays, notes)
                },
                onLuClick = onLuClick,
                onOtherDeductionsClick = { amount, notes ->
                    onOtherDeductionsClick(trip.id, amount.toInt().toString(), notes)
                }
            )
        }

        // Payment Breakdown
        ExpandableSection(
            title = "Payment Breakdown",
            icon = Icons.Outlined.Payments,
            iconBg = colorScheme.primaryContainer,
            iconTint = colorScheme.primary
        ) {
            PaymentBreakdownContent(trip.financials)
        }

        // Trip Timeline
        ExpandableSection(
            title = "Timeline",
            icon = Icons.Outlined.History
        ) {
            TripTimelineContent(timeline = trip.timelines ?: emptyList())
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}


@Composable
fun TripDocumentsSection(
    truckerStatus: String,
    documents: List<TripDataResponse.Document>,
    onUploadLrClick: () -> Unit,
    onDocClick: (String, String) -> Unit
) {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        val lrDoc = documents.find { it.type.lowercase() == "lr" }
        val memoDoc = documents.find { it.type.lowercase() == "loading_memo" }

        DocumentCard(
            title = "Lorry Receipt (LR)",
            doc = lrDoc,
            showUploadButton = true,
            isUploadEnabled = truckerStatus == "LOADING" || (lrDoc != null && (lrDoc.status.lowercase() == "pending" || lrDoc.status.lowercase() == "rejected")),
            onUploadClick = onUploadLrClick,
            onCardClick = { docId, status -> onDocClick(docId, status) }
        )

        DocumentCard(
            title = "Trip Memo",
            doc = memoDoc,
            showUploadButton = false
        )
    }
}

@Composable
fun DocumentCard(
    title: String,
    doc: TripDataResponse.Document?,
    showUploadButton: Boolean = false,
    isUploadEnabled: Boolean = false,
    onUploadClick: () -> Unit = {},
    onCardClick: (String, String) -> Unit = { _, _ -> }
) {

    fun onCardClick() {
        doc?.let {
            val status = it.status.lowercase()
            if (status == "verified" || status == "rejected" || status == "pending") {
                onCardClick(it.id, it.status)
            }
        }
    }

    Card(
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth(),
        onClick = {
            onCardClick()
        }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = colorScheme.primaryContainer,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Outlined.Description,
                        contentDescription = null,
                        tint = colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface
                )
                
                if (doc == null) {
                    Text(
                        text = "Not generated yet",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colorScheme.onSurfaceVariant
                    )
                }
            }

            val docStatus = doc?.status?.lowercase()
            val isPending = docStatus == "pending"
            val isRejected = docStatus == "rejected"
            val showUpload = showUploadButton && (doc == null || isPending || isRejected)

            if (showUpload) {
                val buttonText = if (isPending) "Verify" else "Upload LR"
                val handleButtonClick = if (isPending) { { onCardClick() } } else onUploadClick

                Button(
                    onClick = handleButtonClick,
                    enabled = isUploadEnabled,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorScheme.primaryContainer,
                        contentColor = colorScheme.primary,
                        disabledContainerColor = colorScheme.primaryContainer.copy(alpha = 0.5f),
                        disabledContentColor = colorScheme.primary.copy(alpha = 0.5f)
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text(
                        text = buttonText,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else if (doc != null) {
                StatusBadgeSmall(status = doc.status)
            }
        }
    }
}

@Composable
fun StatusBadgeSmall(status: String) {
    val (color, ticketColor) = when (status.lowercase()) {
        "verified", "approved" -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.primary
        "pending" -> MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.tertiary
        "rejected" -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        color = color,
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            text = status.replaceFirstChar { it.uppercase() },
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = ticketColor)
        )
    }
}

@Composable
fun ExpandableSection(
    title: String,
    icon: ImageVector,
    iconBg: Color = MaterialTheme.colorScheme.surfaceVariant,
    iconTint: Color = MaterialTheme.colorScheme.primary,
    initiallyExpanded: Boolean = false,
    content: @Composable () -> Unit
) {
    var expanded by remember { mutableStateOf(initiallyExpanded) }
    
    Card(
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Row(
                modifier = Modifier
                    .clickable { expanded = !expanded }
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = iconBg,
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp), tint = iconTint)
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            AnimatedVisibility(visible = expanded) {
                content()
            }
        }
    }
}

@Composable
fun ChargesAndDeductionsContent(
    financials: TripDataResponse.FinanceDetails?,
    onHaltingClick: () -> Unit,
    onLuClick: (String, String, String?, String?, String?, String?, String?) -> Unit,
    onOtherDeductionsClick: (Double, String?) -> Unit
) {
    if (financials == null) {
        Text("No data", modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.bodySmall)
        return
    }
    
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        val luTotal = (financials.luCharges.loading.total + financials.luCharges.unloading.total).toDouble()
        val haltingTotal = (financials.halting.loading.totalAmount + financials.halting.unloading.totalAmount)
        
        ChargesSubRow("Loading & Unloading", luTotal, onClick = {
            onLuClick(
                financials.luCharges.loading.total.toString(),
                financials.luCharges.unloading.total.toString(),
                financials.luCharges.loading.slipUrl,
                financials.luCharges.unloading.slipUrl,
                financials.luCharges.loading.id,
                financials.luCharges.unloading.id,
                financials.luCharges.loading.notes,
            )
        })
        ChargesSubRow("Halting", haltingTotal, onClick = onHaltingClick)
        val otherDeductionsNotes = financials.deductionRemarks
        ChargesSubRow("Other Deductions", financials.otherDeductions, isDeduction = true, onClick = {
            onOtherDeductionsClick(financials.otherDeductions, otherDeductionsNotes)
        })
    }
}

@Composable
fun ChargesSubRow(
    label: String, 
    amount: Double, 
    isDeduction: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label, 
            style = MaterialTheme.typography.bodyMedium, 
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            val hasAmount = amount > 0.0
            Text(
                text = if (hasAmount) {
                    (if (isDeduction) "- " else "") + "₹${formatAmount(amount.toString())}"
                } else {
                    "—"
                },
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (hasAmount) FontWeight.Bold else FontWeight.Normal,
                color = when {
                    !hasAmount -> MaterialTheme.colorScheme.onSurfaceVariant
                    isDeduction -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.onSurface
                }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun PaymentBreakdownContent(financials: TripDataResponse.FinanceDetails?) {
    if (financials == null) {
        Text("Financial data not available", modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.bodySmall)
        return
    }

    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(15.dp)
    ) {
        PaymentDetailRow("Trip Cost", financials.truckerTripCost)
        
        // Halting Loading
        PaymentDetailRow(
            label = "Loading",
            amount = financials.halting.loading.totalAmount,
            isAddition = true,
            detailText = "(${financials.halting.loading.days} Days x ₹${formatAmount(financials.halting.loading.perDayAmount.toString())})",
            addHeader = true,
            headerName = "Halting Charges"
        )

        // Halting Unloading
        PaymentDetailRow(
            label = "Unloading",
            amount = financials.halting.unloading.totalAmount,
            isAddition = true,
            detailText = "(${financials.halting.unloading.days} Days x ₹${formatAmount(financials.halting.unloading.perDayAmount.toString())})"
        )

        // Loading Charges (from LU)
        PaymentDetailRow(
            label = "Loading Charges",
            amount = financials.luCharges.loading.total.toDouble(),
            isAddition = true
        )

        // Unloading Charges (from LU)
        PaymentDetailRow(
            label = "Unloading Charges",
            amount = financials.luCharges.unloading.total.toDouble(),
            isAddition = true
        )

        PaymentDetailRow("Other Deductions", financials.otherDeductions, isDeduction = true)
        PaymentDetailRow("Advance Paid", financials.advance.paidAmount)
        PaymentDetailRow("Platform Fee", financials.platformFee.amount, isDeduction = true)
        PaymentDetailRow("TDS", financials.tds.amount, isDeduction = true)
        
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(vertical = 4.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Net Payable", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Text("₹${formatAmount(financials.netPayable.toString())}", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Paid Amount", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Text("₹${formatAmount(financials.totalPaidAmount.toString())}", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Balance Due Box
        val balanceDue = financials.netPayable - financials.totalPaidAmount
        Surface(
            color = MaterialTheme.colorScheme.tertiaryContainer,
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Balance Due", 
                    fontWeight = FontWeight.Bold, 
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    "₹${formatAmount(balanceDue.toString())}", 
                    fontWeight = FontWeight.Bold, 
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

@Composable
fun PaymentDetailRow(
    label: String,
    amount: Double,
    isDeduction: Boolean = false,
    isAddition: Boolean = false,
    detailText: String? = null,
    addHeader: Boolean = false,
    headerName: String = ""
) {
    Column(modifier = Modifier.padding(vertical = 2.dp)) {
        if (addHeader) {
            Text(
                text = headerName, 
                style = MaterialTheme.typography.bodySmall, 
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label, 
                    style = MaterialTheme.typography.bodyMedium, 
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (!detailText.isNullOrBlank()) {
                    Text(
                        text = detailText,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            )
                }
            }
            Text(
                text = when {
                    amount <= 0.0 -> "₹0"
                    isDeduction -> "- ₹${formatAmount(amount.toString())}"
                    isAddition -> "+ ₹${formatAmount(amount.toString())}"
                    else -> "₹${formatAmount(amount.toString())}"
                },
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (amount > 0.0) FontWeight.Bold else FontWeight.Normal,
                color = when {
                    amount <= 0.0 -> MaterialTheme.colorScheme.onSurfaceVariant
                    isDeduction -> MaterialTheme.colorScheme.error
                    isAddition -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.onSurface
                }
            )
        }
    }
}

@Composable
fun TripTimelineContent(timeline: List<TripDataResponse.Timeline>) {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        timeline.forEachIndexed { index, item ->
            TimelineItemRow(
                item = item,
                isLast = index == timeline.size - 1
            )
        }
    }
}

@Composable
fun TimelineItemRow(
    item: TripDataResponse.Timeline,
    isLast: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
        verticalAlignment = Alignment.Top
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .width(28.dp)
                .fillMaxHeight()
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(
                        color = if (item.isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                        shape = CircleShape
                    )
                    .then(
                        if (item.isCurrent && !item.isCompleted) {
                            Modifier.background(MaterialTheme.colorScheme.surface, CircleShape)
                                .then(Modifier.size(12.dp).background(MaterialTheme.colorScheme.primary, CircleShape))
                        } else Modifier
                    )
            )

            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .weight(1f)
                        .padding(bottom = 12.dp)
                        .background(
                            if (item.isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                        )
                )
            }
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.padding(bottom = if (isLast) 16.dp else 24.dp)) {
            Text(
                text = mapStatusToTitle(item.statusType),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = if (item.isCompleted || item.isCurrent) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            val subText = if (item.isCurrent && !item.isCompleted) {
                "In Progress"
            } else {
                item.updatedAt?.let { 
                    try {
                        it.toDateTime()
                    } catch (e: Exception) {
                        it
                    }
                } ?: ""
            }

            if (subText.isNotEmpty()) {
                Text(
                    text = subText,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (item.isCurrent && !item.isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

fun mapStatusToTitle(statusType: String): String {
    val normalized = statusType.uppercase().replace("-", "_")
    return when (normalized) {
        "ASSIGNED" -> "Trip Confirmed"
        "LOADING" -> "Loading"
        "IN_TRANSIT" -> "In Transit"
        "UNLOADED" -> "Unloaded"
        "INVOICED" -> "Invoiced"
        "POD_DISPATCHED" -> "POD Dispatched"
        "PAID" -> "Paid"
        "CLOSED" -> "Closed"
        else -> normalized.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }
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
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", context.packageName, null)
    }
    context.startActivity(intent)
}
