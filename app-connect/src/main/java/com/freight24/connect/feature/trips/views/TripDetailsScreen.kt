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
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.BorderColor
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.freight24.common.util.DocumentUploadPolicy
import com.freight24.connect.R
import com.freight24.connect.feature.trips.domain.viewmodel.TripDetailsEvent
import com.freight24.connect.feature.trips.domain.viewmodel.TripDetailsViewModel
import com.freight24.connect.feature.trips.domain.viewmodel.TripDetailsViewModelImpl
import com.freight24.connect.feature.trips.views.components.TripCard
import com.freight24.connect.feature.trips.views.components.formatAmount
import com.freight24.connect.utils.toDateTime
import com.freight24.rest.response.TripDataResponse
import com.freight24.ui.components.FileConfirmationSheet
import com.freight24.ui.components.FileSourcePickerSheet
import com.freight24.ui.components.FreightTopAppBar
import com.freight24.ui.components.formViewportInsets
import com.freight24.ui.components.rememberCallAction
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripDetailsScreen(
    navController: androidx.navigation.NavController,
    tripId: String,
    onBackClick: () -> Unit,
    onEditClick: (String) -> Unit,
    onDocumentView: (String, String, String, String) -> Unit,
    onHaltingClick: (String, String, Int, Int, String?) -> Unit,
    onLuClick: (String, String, String?, String?, String?, String?, String?) -> Unit,
    onOtherDeductionsClick: (String, String?, String?) -> Unit,
    viewModel: TripDetailsViewModel = hiltViewModel<TripDetailsViewModelImpl>(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val colorScheme = MaterialTheme.colorScheme
    var menuExpanded by remember { mutableStateOf(false) }
    var showConfirmationDialog by remember { mutableStateOf(false) }
    var pendingStatus by remember { mutableStateOf("") }
    var pendingButtonText by remember { mutableStateOf("") }

    // Refresh trip details when returning from PDF viewer via navigation result
    val refreshTrigger =
        navController.currentBackStackEntry
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

    val shareTitle =
        stringResource(
            if (uiState.sharedDocumentType.equals("loading_memo", ignoreCase = true)) {
                R.string.trip_share_loading_memo
            } else {
                R.string.trip_share_lr
            },
        )

    val shareDocFailed = stringResource(R.string.trip_share_document_failed)

    LaunchedEffect(uiState.sharedDocumentFile, uiState.documentShareFailed) {
        val file = uiState.sharedDocumentFile
        if (file != null || uiState.documentShareFailed) {
            val failed =
                uiState.documentShareFailed ||
                    (file != null && shareTripDocumentPdf(context, file, shareTitle).isFailure)
            if (failed) {
                snackbarHostState.showSnackbar(shareDocFailed)
            }
            viewModel.onEvent(TripDetailsEvent.DocumentShareHandled)
        }
    }

    val sourceSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showSourcePicker by rememberSaveable { mutableStateOf(false) }
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }

    val imagePickerLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.PickMultipleVisualMedia(DocumentUploadPolicy.MAX_IMAGES),
            onResult = { uris ->
                if (uris.isNotEmpty()) {
                    viewModel.onEvent(TripDetailsEvent.OnFilesPicked(uris, isPdf = false))
                }
            },
        )

    val pdfPickerLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.OpenDocument(),
            onResult = { uri ->
                uri?.let {
                    viewModel.onEvent(TripDetailsEvent.OnFilesPicked(listOf(it), isPdf = true))
                }
            },
        )

    val cameraLauncher =
        rememberLauncherForActivityResult(
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
                    },
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
            shape = MaterialTheme.shapes.medium,
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
                        enabled = !uiState.isCanceling,
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.onEvent(TripDetailsEvent.ConfirmCancelTrip) },
                    enabled = !uiState.isCanceling && uiState.cancelReason.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = colorScheme.error),
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
            shape = MaterialTheme.shapes.medium,
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
            shape = MaterialTheme.shapes.medium,
        )
    }

    uiState.transactionUserType?.let { userType ->
        PaymentTransactionsSheet(
            userType = userType,
            transactions = uiState.transactions,
            isLoading = uiState.isTransactionsLoading,
            errorMessage = uiState.transactionsError,
            onRetry = { viewModel.onEvent(TripDetailsEvent.RetryPaymentTransactions) },
            onDismiss = { viewModel.onEvent(TripDetailsEvent.DismissPaymentTransactions) },
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
                            modifier = Modifier.background(colorScheme.surface),
                        ) {
                            DropdownMenuItem(
                                text = { Text("Edit") },
                                leadingIcon = {
                                    Icon(Icons.Outlined.BorderColor, contentDescription = "Edit", tint = colorScheme.onSurfaceVariant)
                                },
                                onClick = {
                                    menuExpanded = false
                                    onEditClick(tripId)
                                },
                            )
                            DropdownMenuItem(
                                text = { Text("Cancel", color = colorScheme.error) },
                                leadingIcon = {
                                    Icon(Icons.Outlined.Delete, contentDescription = "Cancel Trip", tint = colorScheme.error)
                                },
                                onClick = {
                                    menuExpanded = false
                                    viewModel.onEvent(TripDetailsEvent.ShowCancelDialog)
                                },
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
            val (buttonText, nextStatus) =
                when (truckerStatus.uppercase()) {
                    "ASSIGNED" -> stringResource(R.string.trip_update_to_loading) to "LOADING"
                    "LOADING" -> stringResource(R.string.trip_upload_lr) to "UPLOAD_LR"
                    "IN_TRANSIT" -> "Update to UNLOADED" to "UNLOADED"
                    else -> null to null
                }

            if (buttonText != null && nextStatus != null && showTripStatus) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shadowElevation = 8.dp,
                ) {
                    Column {
                        if (truckerStatus.equals("ASSIGNED", ignoreCase = true)) {
                            Text(
                                text = stringResource(R.string.trip_loading_lr_hint),
                                style = MaterialTheme.typography.bodySmall,
                                color = colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp),
                            )
                        }
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
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp)
                                    .height(52.dp),
                            shape = MaterialTheme.shapes.medium,
                            colors =
                                ButtonDefaults.buttonColors(
                                    containerColor = colorScheme.primary,
                                    contentColor = colorScheme.onPrimary,
                                ),
                        ) {
                            Text(buttonText, style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
            }
        },
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = { viewModel.onEvent(TripDetailsEvent.PullToRefresh) },
            modifier =
                Modifier
                    .fillMaxSize()
                    // Keyboard-aware viewport; see FormInsets.kt.
                    .formViewportInsets(paddingValues)
                    .background(colorScheme.background),
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                uiState.errorMessage != null -> {
                    Text(
                        text = uiState.errorMessage ?: "Unknown error",
                        color = colorScheme.error,
                        modifier = Modifier.align(Alignment.Center),
                    )
                }
                uiState.trip != null -> {
                    TripDetailsContent(
                        trip = uiState.trip!!,
                        onUploadLrClick = { showSourcePicker = true },
                        onDocClick = { docId, status, documentType ->
                            onDocumentView(tripId, docId, status, documentType)
                        },
                        onHaltingClick = { tripId, truckTypeId, loadingDays, unloadingDays, notes ->
                            onHaltingClick(tripId, truckTypeId, loadingDays, unloadingDays, notes)
                        },
                        onLuClick = { loading, unloading, loadingSlipUrl, unloadingSlipUrl, loadingId, unloadingId, notes ->
                            onLuClick(loading, unloading, loadingSlipUrl, unloadingSlipUrl, loadingId, unloadingId, notes)
                        },
                        onOtherDeductionsClick = onOtherDeductionsClick,
                        onShareDocumentClick = { viewModel.onEvent(TripDetailsEvent.ShareDocument(it)) },
                        onViewPaymentTransactions = {
                            viewModel.onEvent(TripDetailsEvent.ViewPaymentTransactions(it))
                        },
                        sharingDocumentId = uiState.sharingDocumentId,
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
    onDocClick: (String, String, String) -> Unit,
    onHaltingClick: (String, String, Int, Int, String?) -> Unit,
    onLuClick: (String, String, String?, String?, String?, String?, String?) -> Unit,
    onOtherDeductionsClick: (String, String?, String?) -> Unit,
    onShareDocumentClick: (String) -> Unit,
    onViewPaymentTransactions: (String) -> Unit,
    sharingDocumentId: String?,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Reused Trip Card
        TripCard(trip = trip)

        ExpandableSection(
            title = "Trip Contacts",
            icon = Icons.Outlined.Phone,
            iconBg = colorScheme.primaryContainer,
            iconTint = colorScheme.primary,
            initiallyExpanded = true,
        ) {
            TripContactsContent(trip = trip)
        }

        ExpandableSection(
            title = "Client Contacts",
            icon = Icons.Outlined.Phone,
            iconBg = colorScheme.primaryContainer,
            iconTint = colorScheme.primary,
        ) {
            ClientContactsContent(contacts = trip.client.contacts)
        }

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
                onDocClick = onDocClick,
                onShareDocumentClick = onShareDocumentClick,
                sharingDocumentId = sharingDocumentId,
            )
        }

        // Charges & Deductions
        ExpandableSection(
            title = "Charges & Deductions",
            icon = Icons.Outlined.AccountBalanceWallet,
            iconBg = colorScheme.primaryContainer,
            iconTint = colorScheme.primary,
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
                },
            )
        }

        // Payment Breakdown
        ExpandableSection(
            title = stringResource(R.string.trip_trucker_payment_breakdown),
            icon = Icons.Outlined.Payments,
            iconBg = colorScheme.primaryContainer,
            iconTint = colorScheme.primary,
        ) {
            PaymentBreakdownContent(
                financials = trip.financials,
                onViewTransactions = { onViewPaymentTransactions("trucker") },
            )
        }

        ExpandableSection(
            title = stringResource(R.string.trip_client_payment_breakdown),
            icon = Icons.Outlined.Payments,
            iconBg = colorScheme.primaryContainer,
            iconTint = colorScheme.primary,
        ) {
            ClientPaymentBreakdownContent(
                trip = trip,
                onViewTransactions = { onViewPaymentTransactions("client") },
            )
        }

        // Trip Timeline
        ExpandableSection(
            title = "Timeline",
            icon = Icons.Outlined.History,
            iconBg = colorScheme.primaryContainer,
            iconTint = colorScheme.primary,
        ) {
            TripTimelineContent(timeline = trip.timelines ?: emptyList())
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun TripContactsContent(trip: TripDataResponse) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        TripContactRow(
            label = "Truck owner",
            name = trip.trucker?.name,
            tagCode = trip.trucker?.tagCode,
            phone = trip.trucker?.phone,
        )
        HorizontalDivider(color = colorScheme.outlineVariant)
        TripContactRow(
            label = "Driver",
            name = trip.driverName,
            phone = trip.driverPhone,
        )
    }
}

@Composable
private fun ClientContactsContent(contacts: TripDataResponse.Contacts?) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        TripContactRow(
            label = "Placement",
            name = contacts?.placement?.name,
            phone = contacts?.placement?.phone,
        )
        HorizontalDivider(color = colorScheme.outlineVariant)
        TripContactRow(
            label = "POD",
            name = contacts?.pod?.name,
            phone = contacts?.pod?.phone,
        )
        HorizontalDivider(color = colorScheme.outlineVariant)
        TripContactRow(
            label = "Payment",
            name = contacts?.payment?.name,
            phone = contacts?.payment?.phone,
        )
        HorizontalDivider(color = colorScheme.outlineVariant)
        TripContactRow(
            label = "Operations",
            name = contacts?.operations?.name,
            phone = contacts?.operations?.phone,
        )
    }
}

@Composable
private fun TripContactRow(
    label: String,
    name: String?,
    phone: String?,
    tagCode: String? = null,
) {
    val hasName = !name.isNullOrBlank()
    val hasPhone = !phone.isNullOrBlank()

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = name?.takeIf { it.isNotBlank() } ?: "Not available",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = if (hasName) colorScheme.onSurface else colorScheme.onSurfaceVariant,
            )
            tagCode?.takeIf { it.isNotBlank() }?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.labelMedium,
                    color = colorScheme.primary,
                )
            }
            phone?.takeIf { it.isNotBlank() }?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorScheme.onSurfaceVariant,
                )
            }
        }

        if (hasPhone) {
            val call = rememberCallAction(phone!!)
            TextButton(onClick = call) {
                Icon(
                    imageVector = Icons.Outlined.Phone,
                    contentDescription = "Call $label",
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Call")
            }
        }
    }
}

@Composable
fun TripDocumentsSection(
    truckerStatus: String,
    documents: List<TripDataResponse.Document>,
    onUploadLrClick: () -> Unit,
    onDocClick: (String, String, String) -> Unit,
    onShareDocumentClick: (String) -> Unit,
    sharingDocumentId: String?,
) {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        val lrDoc = documents.find { it.type.lowercase() == "lr" }
        val memoDoc = documents.find { it.type.lowercase() == "loading_memo" }
        val epodDoc = documents.find {
            it.type.equals("pod", ignoreCase = true) && it.podType.equals("epod", ignoreCase = true)
        }
        val isLrUploadEnabled =
            truckerStatus.equals("LOADING", ignoreCase = true) ||
                (
                    lrDoc != null && (
                        lrDoc.status.equals("pending", ignoreCase = true) ||
                            lrDoc.status.equals("rejected", ignoreCase = true)
                    )
                )

        DocumentCard(
            title = stringResource(R.string.trip_lorry_receipt),
            doc = lrDoc,
            showUploadButton = true,
            showEmptyStatus = false,
            isUploadEnabled = isLrUploadEnabled,
            uploadDisabledReason =
                if (truckerStatus.equals("ASSIGNED", ignoreCase = true) &&
                    !isLrUploadEnabled && lrDoc == null
                ) {
                    stringResource(R.string.trip_lr_upload_disabled_reason)
                } else {
                    null
                },
            onUploadClick = onUploadLrClick,
            onCardClick = { docId, status -> onDocClick(docId, status, "lr") },
            onShareClick = lrDoc?.let { { onShareDocumentClick(it.id) } },
            isPreparingShare = lrDoc != null && sharingDocumentId == lrDoc.id,
            isShareEnabled = sharingDocumentId == null,
        )

        DocumentCard(
            title = stringResource(R.string.trip_loading_memo),
            doc = memoDoc,
            showUploadButton = false,
            onCardClick = { docId, status -> onDocClick(docId, status, "loading_memo") },
            onShareClick = memoDoc?.let { { onShareDocumentClick(it.id) } },
            isPreparingShare = memoDoc != null && sharingDocumentId == memoDoc.id,
            isShareEnabled = sharingDocumentId == null,
            shareLabel = stringResource(R.string.trip_share_loading_memo),
        )

        epodDoc?.let { document ->
            DocumentCard(
                title = stringResource(R.string.trip_epod),
                doc = document,
                onCardClick = { docId, status -> onDocClick(docId, status, "epod") },
            )
        }
    }
}

@Composable
fun DocumentCard(
    title: String,
    doc: TripDataResponse.Document?,
    showUploadButton: Boolean = false,
    isUploadEnabled: Boolean = false,
    onUploadClick: () -> Unit = {},
    onCardClick: (String, String) -> Unit = { _, _ -> },
    uploadDisabledReason: String? = null,
    showEmptyStatus: Boolean = true,
    onShareClick: (() -> Unit)? = null,
    isPreparingShare: Boolean = false,
    isShareEnabled: Boolean = true,
    shareLabel: String? = null,
) {
    fun onCardClick() {
        doc?.let {
            onCardClick(it.id, it.status)
        }
    }

    Card(
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth(),
        onClick = {
            onCardClick()
        },
    ) {
        Column {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = colorScheme.primaryContainer,
                    modifier = Modifier.size(40.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Outlined.Description,
                            contentDescription = null,
                            tint = colorScheme.primary,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurface,
                    )
                    if (doc == null && showEmptyStatus) {
                        Text(
                            text = stringResource(R.string.trip_document_not_generated),
                            style = MaterialTheme.typography.bodyMedium,
                            color = colorScheme.onSurfaceVariant,
                        )
                    }
                }

                val docStatus = doc?.status?.lowercase()
                val isPending = docStatus == "pending"
                val isRejected = docStatus == "rejected"
                val showUpload = showUploadButton && (doc == null || isPending || isRejected)

                if (showUpload) {
                    val buttonText =
                        stringResource(
                            if (isPending) R.string.trip_verify_document else R.string.trip_upload_lr,
                        )
                    val handleButtonClick =
                        if (isPending) {
                            { onCardClick() }
                        } else {
                            onUploadClick
                        }

                    Button(
                        onClick = handleButtonClick,
                        enabled = isUploadEnabled,
                        shape = MaterialTheme.shapes.small,
                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor = colorScheme.primaryContainer,
                                contentColor = colorScheme.onPrimaryContainer,
                                disabledContainerColor = colorScheme.primaryContainer.copy(alpha = 0.5f),
                                disabledContentColor = colorScheme.onPrimaryContainer.copy(alpha = 0.5f),
                            ),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        modifier = Modifier.height(36.dp),
                    ) {
                        Text(
                            text = buttonText,
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                } else if (doc != null) {
                    StatusBadgeSmall(status = doc.status)
                }
            }
            if (doc != null && onShareClick != null) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 8.dp),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(onClick = onShareClick, enabled = isShareEnabled && !isPreparingShare) {
                        if (isPreparingShare) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Outlined.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                        }
                        Spacer(Modifier.width(8.dp))
                        Text(shareLabel ?: stringResource(R.string.trip_share_lr))
                    }
                }
            }
            if (uploadDisabledReason != null) {
                Row(
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = null,
                        tint = colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp),
                    )
                    Text(
                        text = uploadDisabledReason,
                        style = MaterialTheme.typography.bodySmall,
                        color = colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
fun StatusBadgeSmall(status: String) {
    val (color, ticketColor) =
        when (status.lowercase()) {
            "verified", "approved" -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.primary
            "pending" -> MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.tertiary
            "rejected" -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.error
            else -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
        }
    val label =
        when (status.lowercase()) {
            "pending" -> "In Review"
            else -> status.replaceFirstChar { it.uppercase() }
        }

    Surface(
        color = color,
        shape = MaterialTheme.shapes.small,
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = ticketColor),
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
    content: @Composable () -> Unit,
) {
    var expanded by rememberSaveable { mutableStateOf(initiallyExpanded) }

    Card(
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column {
            Row(
                modifier =
                    Modifier
                        .clickable { expanded = !expanded }
                        .padding(16.dp)
                        .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = iconBg,
                    modifier = Modifier.size(40.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp), tint = iconTint)
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f),
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant,
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                    content()
                }
            }
        }
    }
}

@Composable
fun ChargesAndDeductionsContent(
    financials: TripDataResponse.FinanceDetails?,
    onHaltingClick: () -> Unit,
    onLuClick: (String, String, String?, String?, String?, String?, String?) -> Unit,
    onOtherDeductionsClick: (Double, String?) -> Unit,
) {
    if (financials == null) {
        Text("No data", modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.bodySmall)
        return
    }

    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        val luTotal = (financials.luCharges.loading.total + financials.luCharges.unloading.total).toDouble()
        val haltingTotal =
            (financials.halting.loading.totalAmount?.toDouble() ?: 0.0) +
                (financials.halting.unloading.totalAmount?.toDouble() ?: 0.0)

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
        ChargesSubRow("Other Deductions", financials.otherDeductions?.toDouble() ?: 0.0, isDeduction = true, onClick = {
            onOtherDeductionsClick(financials.otherDeductions?.toDouble() ?: 0.0, otherDeductionsNotes)
        })
    }
}

@Composable
fun ChargesSubRow(
    label: String,
    amount: Double,
    isDeduction: Boolean = false,
    onClick: (() -> Unit)? = null,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .then(
                    if (onClick != null) {
                        Modifier.clickable { onClick() }
                    } else {
                        Modifier
                    },
                )
                .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            val hasAmount = amount > 0.0
            Text(
                text =
                    if (hasAmount) {
                        (if (isDeduction) "- " else "") + "₹${formatAmount(amount.toString())}"
                    } else {
                        "—"
                    },
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (hasAmount) FontWeight.Bold else FontWeight.Normal,
                color =
                    when {
                        !hasAmount -> MaterialTheme.colorScheme.onSurfaceVariant
                        isDeduction -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.onSurface
                    },
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
fun PaymentBreakdownContent(
    financials: TripDataResponse.FinanceDetails?,
    onViewTransactions: () -> Unit,
) {
    if (financials == null) {
        Text("Financial data not available", modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.bodySmall)
        return
    }

    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(15.dp),
    ) {
        PaymentDetailRow("Trip Cost", financials.truckerTripCost)

        // Halting Loading
        PaymentDetailRow(
            label = "Loading",
            amount = financials.halting.loading.totalAmount?.toDouble() ?: 0.0,
            isAddition = true,
            detailText = "(${financials.halting.loading.days} Days x ₹${formatAmount(
                financials.halting.loading.perDayAmount?.toPlainString() ?: "0",
            )})",
            addHeader = true,
            headerName = "Halting Charges",
        )

        // Halting Unloading
        PaymentDetailRow(
            label = "Unloading",
            amount = financials.halting.unloading.totalAmount?.toDouble() ?: 0.0,
            isAddition = true,
            detailText = "(${financials.halting.unloading.days} Days x ₹${formatAmount(
                financials.halting.unloading.perDayAmount?.toPlainString() ?: "0",
            )})",
        )

        // Loading Charges (from LU)
        PaymentDetailRow(
            label = "Loading Charges",
            amount = financials.luCharges.loading.total.toDouble(),
            isAddition = true,
        )

        // Unloading Charges (from LU)
        PaymentDetailRow(
            label = "Unloading Charges",
            amount = financials.luCharges.unloading.total.toDouble(),
            isAddition = true,
        )

        PaymentDetailRow("Other Deductions", financials.otherDeductions?.toDouble() ?: 0.0, isDeduction = true)
        PaymentDetailRow("Advance Paid", financials.advance.paidAmount)
        PaymentDetailRow("Platform Fee", financials.platformFee.amount, isDeduction = true)
        PaymentDetailRow("TDS", financials.tds.amount, isDeduction = true)

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(vertical = 4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "Net Payable",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                "₹${formatAmount(financials.netPayable.toString())}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "Paid Amount",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                "₹${formatAmount(financials.totalPaidAmount.toString())}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Balance Due Box
        val balanceDue = financials.netPayable - financials.totalPaidAmount
        Surface(
            color = MaterialTheme.colorScheme.tertiaryContainer,
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "Balance Due",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    "₹${formatAmount(balanceDue.toString())}",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                    style = MaterialTheme.typography.titleMedium,
                )
            }
        }
        TextButton(onClick = onViewTransactions, modifier = Modifier.align(Alignment.End)) {
            Text(stringResource(R.string.trip_view_transactions))
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
    headerName: String = "",
) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        if (addHeader) {
            Text(
                text = headerName,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 4.dp),
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                text =
                    when {
                        amount <= 0.0 -> "₹0"
                        isDeduction -> "- ₹${formatAmount(amount.toString())}"
                        isAddition -> "+ ₹${formatAmount(amount.toString())}"
                        else -> "₹${formatAmount(amount.toString())}"
                    },
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (amount > 0.0) FontWeight.Bold else FontWeight.Normal,
                color =
                    when {
                        amount <= 0.0 -> MaterialTheme.colorScheme.onSurfaceVariant
                        isDeduction -> MaterialTheme.colorScheme.error
                        isAddition -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.onSurface
                    },
            )
        }
    }
}

@Composable
fun TripTimelineContent(timeline: List<TripDataResponse.Timeline>) {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        timeline.forEachIndexed { index, item ->
            TimelineItemRow(
                item = item,
                isLast = index == timeline.size - 1,
            )
        }
    }
}

@Composable
fun TimelineItemRow(
    item: TripDataResponse.Timeline,
    isLast: Boolean,
) {
    val lineColor =
        if (item.isCompleted) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.outlineVariant
        }
    val isCurrentStop = item.isCurrent && !item.isCompleted

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
        verticalAlignment = Alignment.Top,
    ) {
        // Indicator column: dot centered over the connector line
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(24.dp),
        ) {
            Box(
                modifier =
                    Modifier
                        .size(16.dp)
                        .border(
                            width = 2.dp,
                            color =
                                if (item.isCompleted || isCurrentStop) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.outlineVariant
                                },
                            shape = CircleShape,
                        )
                        .padding(4.dp)
                        .background(
                            color =
                                if (item.isCompleted || isCurrentStop) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.outlineVariant
                                },
                            shape = CircleShape,
                        ),
            )

            if (!isLast) {
                Box(
                    modifier =
                        Modifier
                            .width(2.dp)
                            .weight(1f)
                            .background(lineColor),
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.padding(bottom = if (isLast) 16.dp else 24.dp)) {
            Text(
                text = mapStatusToTitle(item.statusType),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = if (item.isCompleted || isCurrentStop) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
            )

            val subText =
                if (isCurrentStop) {
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
                    color = if (isCurrentStop) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
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
    val intent =
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
        }
    context.startActivity(intent)
}
