package com.freight24.connect.feature.clients.views
import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.freight24.common.model.DocumentUploadState
import com.freight24.common.model.UploadStatus
import com.freight24.connect.feature.clients.domain.viewmodel.ClientProfileViewModel
import com.freight24.rest.response.ClientDocumentDto
import com.freight24.rest.response.ClientProfileDto
import com.freight24.ui.UiState
import com.freight24.ui.components.FreightTopAppBar
import com.freight24.ui.components.formViewportInsets
import com.freight24.ui.theme.FreightThemeTokens
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientProfileScreen(
    onBackClick: () -> Unit,
    viewModel: ClientProfileViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val isEditing by viewModel.isEditing.collectAsState()
    val hasChanges by viewModel.hasChanges.collectAsState()
    val panDuplicateError by viewModel.panDuplicateError.collectAsState()
    val gstDuplicateError by viewModel.gstDuplicateError.collectAsState()
    val hasPendingTemporaryUploads by viewModel.hasPendingTemporaryUploads.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()
    var showCancelConfirmDialog by rememberSaveable { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        launch {
            viewModel.errorEvent.collect { message ->
                snackbarHostState.showSnackbar(
                    message = message,
                    duration = SnackbarDuration.Short,
                )
            }
        }
        launch {
            viewModel.saveEvent.collect { message ->
                snackbarHostState.showSnackbar(message)
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            FreightTopAppBar(
                title = {
                    Text(
                        text = "Client Profile",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (!isEditing) {
                        IconButton(onClick = viewModel::toggleEditMode) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit profile",
                            )
                        }
                    }
                },
            )
        },
        bottomBar = {
            if (isEditing) {
                EditActions(
                    hasChanges = hasChanges,
                    onSave = viewModel::saveProfile,
                    onCancel = {
                        if (hasPendingTemporaryUploads) {
                            showCancelConfirmDialog = true
                        } else {
                            viewModel.cancelEdit()
                        }
                    },
                    isSaving = isSaving,
                )
            }
        },
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = viewModel::onRefresh,
            modifier =
                Modifier
                    .fillMaxSize()
                    // Keyboard-aware viewport: without this the scroll bounds run behind the
                    // keyboard and bring-into-view leaves the focused field hidden. See FormInsets.kt.
                    .formViewportInsets(paddingValues),
        ) {
            if (showCancelConfirmDialog) {
                AlertDialog(
                    onDismissRequest = { showCancelConfirmDialog = false },
                    title = { Text("Discard uploads?") },
                    text = { Text("Uploaded documents will be discarded. Cancel anyway?") },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                showCancelConfirmDialog = false
                                viewModel.cancelEdit()
                            },
                        ) {
                            Text("Yes, Cancel")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showCancelConfirmDialog = false }) {
                            Text("Keep Editing")
                        }
                    },
                )
            }

            when (val state = uiState) {
                is UiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                is UiState.Error -> {
                    ClientProfileErrorState(
                        message = state.message,
                        onRetry = viewModel::retry,
                    )
                }

                is UiState.Success -> {
                    Box(modifier = Modifier.fillMaxSize()) {
                        ClientProfileContent(
                            client = state.data,
                            isEditing = isEditing,
                            viewModel = viewModel,
                            panDuplicateError = panDuplicateError,
                            gstDuplicateError = gstDuplicateError,
                        )

                        if (isSaving) {
                            Box(
                                modifier =
                                    Modifier
                                        .fillMaxSize()
                                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 3.dp)
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text("Saving profile...")
                                    }
                                }
                            }
                        }
                    }
                }

                UiState.Idle -> Unit
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ClientProfileContent(
    client: ClientProfileDto,
    isEditing: Boolean,
    viewModel: ClientProfileViewModel,
    panDuplicateError: String?,
    gstDuplicateError: String?,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val documentStates by viewModel.documentStates.collectAsState()

    var showSourcePicker by remember { mutableStateOf(false) }
    var currentTargetFieldKey by remember { mutableStateOf<String?>(null) }
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }
    val sheetState = androidx.compose.material3.rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var pendingUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var isPdfPending by rememberSaveable { mutableStateOf(false) }
    val selectedUploadStatus =
        currentTargetFieldKey
            ?.let { fieldKey -> documentStates[fieldKey]?.uploadStatus }
            ?: UploadStatus.IDLE

    val galleryLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.PickMultipleVisualMedia(),
        ) { uris ->
            if (uris.isNotEmpty()) {
                pendingUris = uris
                isPdfPending = false
            } else {
                currentTargetFieldKey = null
            }
        }

    val docLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.OpenDocument(),
        ) { uri ->
            if (uri != null) {
                pendingUris = listOf(uri)
                isPdfPending = true
            } else {
                currentTargetFieldKey = null
            }
        }

    val cameraLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.TakePicture(),
        ) { success ->
            if (success && tempCameraUri != null) {
                pendingUris = listOf(tempCameraUri!!)
                isPdfPending = false
            } else {
                currentTargetFieldKey = null
            }
            tempCameraUri = null
        }

    val cameraPermissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission(),
        ) { isGranted ->
            if (isGranted) {
                val uri = createTempImageUri(context)
                tempCameraUri = uri
                cameraLauncher.launch(uri)
            }
        }

    fun initiateCapture() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            val uri = createTempImageUri(context)
            tempCameraUri = uri
            cameraLauncher.launch(uri)
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }
    val fullName = viewModel.editableFullName.collectAsState().value
    val companyName = viewModel.editableCompanyName.collectAsState().value
    val email = viewModel.editableEmail.collectAsState().value
    val phone = viewModel.editablePhone.collectAsState().value
    val companyGst = viewModel.editableGstNumber.collectAsState().value
    val panCard = viewModel.editablePanCard.collectAsState().value
    val aadhaar = viewModel.editableAadhaarCard.collectAsState().value
    val branches = viewModel.editableBranches.collectAsState().value
    val tagCode = client.tag_code ?: ""

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 20.dp),
    ) {
        ProfileHeaderCard(
            fullName = fullName,
            companyName = companyName,
            tagCode = tagCode,
            initials = viewModel.getInitials(),
            status = client.status.orEmpty(),
        )

        Spacer(modifier = Modifier.height(24.dp))

        ProfileSection(
            title = "PERSONAL INFORMATION",
            icon = Icons.Default.Person,
            initiallyExpanded = true,
        ) {
            ProfileField(
                label = "DISPLAY NAME",
                value = fullName,
                isEditing = isEditing,
                leadingIcon = Icons.Default.Person,
                onValueChange = {
                    viewModel.editableFullName.value = it
                    viewModel.onFieldChanged()
                },
            )
            DividerLine()
            ProfileField(
                label = "EMAIL",
                value = email,
                isEditing = isEditing,
                leadingIcon = Icons.Default.Badge,
                keyboardType = KeyboardType.Email,
                capitalization = KeyboardCapitalization.None,
                onValueChange = {
                    viewModel.editableEmail.value = it.trim()
                    viewModel.onFieldChanged()
                },
            )
            DividerLine()
            ProfileField(
                label = "PHONE",
                value = phone,
                isEditing = isEditing,
                leadingIcon = Icons.Default.Call,
                onLeadingIconClick = { launchDialer(context, phone) },
                keyboardType = KeyboardType.Phone,
                capitalization = KeyboardCapitalization.None,
                isPhoneNumber = true,
                onValueChange = {
                    viewModel.editablePhone.value = it
                    viewModel.onFieldChanged()
                },
            )
            DividerLine()
            val altPhones =
                client.alt_phones
                    ?.filter { it.isNotBlank() && it != "[]" }
                    .orEmpty()
            if (altPhones.isNotEmpty()) {
                ProfileField(
                    label = "ALT PHONES",
                    value = altPhones.joinToString(", "),
                    isEditing = false,
                    isEditable = false,
                    leadingIcon = Icons.Default.Call,
                    onLeadingIconClick = {
                        launchDialer(context, altPhones.first())
                    },
                    placeholder = "Not added",
                    isPhoneNumber = true,
                )
                DividerLine()
            }
            ProfileField(
                label = "PAN NUMBER",
                value = panCard,
                isEditing = isEditing,
                leadingIcon = Icons.Default.Badge,
                keyboardType = KeyboardType.Ascii,
                capitalization = KeyboardCapitalization.Characters,
                onValueChange = {
                    viewModel.editablePanCard.value = it.uppercase()
                    viewModel.onFieldChanged()
                },
                onFocusLost = viewModel::onPanFocusLost,
                duplicateError = panDuplicateError,
            )
            DividerLine()
            ProfileField(
                label = "AADHAAR NUMBER",
                value = aadhaar,
                isEditing = isEditing,
                leadingIcon = Icons.Default.Fingerprint,
                keyboardType = KeyboardType.Number,
                capitalization = KeyboardCapitalization.None,
                onValueChange = {
                    viewModel.editableAadhaarCard.value = it.filter(Char::isDigit).take(12)
                    viewModel.onFieldChanged()
                },
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        ProfileSection(
            title = "BUSINESS DETAILS",
            icon = Icons.Default.Business,
            initiallyExpanded = true,
        ) {
            ProfileField(
                label = "COMPANY NAME",
                value = companyName,
                isEditing = isEditing,
                leadingIcon = Icons.Default.Business,
                onValueChange = {
                    viewModel.editableCompanyName.value = it
                    viewModel.onFieldChanged()
                },
            )
            DividerLine()
            ProfileField(
                label = "GST NUMBER",
                value = companyGst,
                isEditing = isEditing,
                leadingIcon = Icons.Default.Badge,
                keyboardType = KeyboardType.Ascii,
                capitalization = KeyboardCapitalization.Characters,
                onValueChange = {
                    viewModel.editableGstNumber.value = it.uppercase()
                    viewModel.onFieldChanged()
                },
                onFocusLost = viewModel::onGstFocusLost,
                duplicateError = gstDuplicateError,
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        ProfileSection(
            title = "DOCUMENTS",
            icon = Icons.AutoMirrored.Filled.Assignment,
            initiallyExpanded = true,
        ) {
            DocumentList(
                documents = client.documents,
                documentStates = documentStates,
                isEditing = isEditing,
                onDocumentView = { documentId ->
                    viewModel.getSignedUrl(documentId) { signedUrl ->
                        context.startActivity(com.freight24.ui.components.getPdfLaunchIntent(signedUrl, context))
                    }
                },
                onDocumentUpload = { fieldKey ->
                    currentTargetFieldKey = fieldKey
                    showSourcePicker = true
                },
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        ProfileSection(
            title = "BRANCHES",
            icon = Icons.Default.Business,
            initiallyExpanded = false,
        ) {
            BranchesSectionContent(
                branches = branches,
                isEditing = isEditing,
                onBranchNameChanged = viewModel::onBranchNameChanged,
                onBranchAddressChanged = viewModel::onBranchAddressChanged,
                onPodContactNameChanged = viewModel::onPodContactNameChanged,
                onPodContactNumberChanged = viewModel::onPodContactNumberChanged,
                onPaymentContactNameChanged = viewModel::onPaymentContactNameChanged,
                onPaymentContactNumberChanged = viewModel::onPaymentContactNumberChanged,
                onTrafficContactNameChanged = viewModel::onTrafficContactNameChanged,
                onTrafficContactNumberChanged = viewModel::onTrafficContactNumberChanged,
                onTrackingContactNameChanged = viewModel::onTrackingContactNameChanged,
                onTrackingContactNumberChanged = viewModel::onTrackingContactNumberChanged,
            )
        }

        com.freight24.ui.components.FileSourcePickerSheet(
            showSheet = showSourcePicker,
            onDismissRequest = {
                showSourcePicker = false
                currentTargetFieldKey = null
            },
            onCameraClick = {
                scope.launch { sheetState.hide() }.invokeOnCompletion {
                    showSourcePicker = false
                    initiateCapture()
                }
            },
            onGalleryClick = {
                scope.launch { sheetState.hide() }.invokeOnCompletion {
                    showSourcePicker = false
                    galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                }
            },
            onDocumentClick = {
                scope.launch { sheetState.hide() }.invokeOnCompletion {
                    showSourcePicker = false
                    docLauncher.launch(arrayOf("application/pdf"))
                }
            },
            sheetState = sheetState,
        )

        if (pendingUris.isNotEmpty()) {
            com.freight24.ui.components.FileConfirmationSheet(
                selectedUris = pendingUris,
                isPdf = isPdfPending,
                isUploading = selectedUploadStatus == UploadStatus.UPLOADING,
                uploadButtonText = "Confirm Upload",
                onUploadClick = {
                    currentTargetFieldKey?.let { fieldKey ->
                        viewModel.uploadDocument(
                            fieldKey = fieldKey,
                            uris = pendingUris,
                            isPdf = isPdfPending,
                        )
                    }
                    pendingUris = emptyList()
                    currentTargetFieldKey = null
                },
                onDismiss = {
                    pendingUris = emptyList()
                    currentTargetFieldKey = null
                },
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun BranchesSectionContent(
    branches: List<ClientProfileViewModel.EditableClientBranch>,
    isEditing: Boolean,
    onBranchNameChanged: (String, String) -> Unit,
    onBranchAddressChanged: (String, String) -> Unit,
    onPodContactNameChanged: (String, String) -> Unit,
    onPodContactNumberChanged: (String, String) -> Unit,
    onPaymentContactNameChanged: (String, String) -> Unit,
    onPaymentContactNumberChanged: (String, String) -> Unit,
    onTrafficContactNameChanged: (String, String) -> Unit,
    onTrafficContactNumberChanged: (String, String) -> Unit,
    onTrackingContactNameChanged: (String, String) -> Unit,
    onTrackingContactNumberChanged: (String, String) -> Unit,
) {
    if (branches.isEmpty()) {
        Text(
            text = "No branches available",
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        return
    }

    val successColor = FreightThemeTokens.colors.success

    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        branches.forEachIndexed { index, branch ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(18.dp)) {
                    // Branch header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = branch.branchName.ifBlank { "Unnamed Branch" },
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier =
                                    Modifier
                                        .size(8.dp)
                                        .background(successColor, CircleShape),
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Branch ${index + 1}",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = successColor,
                            )
                        }
                    }

                    if (branch.branchAddress.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(14.dp),
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = branch.branchAddress,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Operations (POD) sub-card
                    BranchContactSubCard(
                        icon = Icons.Default.Inventory,
                        title = "OPERATIONS (POD)",
                        contactNameLabel = "CONTACT NAME",
                        contactNameValue = branch.podContactName,
                        contactNumberLabel = "MOBILE NUMBER",
                        contactNumberValue = branch.podContactNumber,
                        isEditing = isEditing,
                        onNameChange = { onPodContactNameChanged(branch.id, it) },
                        onNumberChange = { onPodContactNumberChanged(branch.id, it.filter(Char::isDigit).take(15)) },
                        successColor = successColor,
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Finance & Payment sub-card
                    BranchContactSubCard(
                        icon = Icons.Default.CreditCard,
                        title = "FINANCE & PAYMENT",
                        contactNameLabel = "CONTACT NAME",
                        contactNameValue = branch.paymentContactName,
                        contactNumberLabel = "MOBILE NUMBER",
                        contactNumberValue = branch.paymentContactNumber,
                        isEditing = isEditing,
                        onNameChange = { onPaymentContactNameChanged(branch.id, it) },
                        onNumberChange = { onPaymentContactNumberChanged(branch.id, it.filter(Char::isDigit).take(15)) },
                        successColor = successColor,
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Logistics & Tracking sub-card
                    BranchLogisticsTrackingSubCard(
                        trafficName = branch.trafficContactName,
                        trafficNumber = branch.trafficContactNumber,
                        trackingName = branch.trackingContactName,
                        trackingNumber = branch.trackingContactNumber,
                        isEditing = isEditing,
                        onTrafficNameChange = { onTrafficContactNameChanged(branch.id, it) },
                        onTrafficNumberChange = { onTrafficContactNumberChanged(branch.id, it.filter(Char::isDigit).take(15)) },
                        onTrackingNameChange = { onTrackingContactNameChanged(branch.id, it) },
                        onTrackingNumberChange = { onTrackingContactNumberChanged(branch.id, it.filter(Char::isDigit).take(15)) },
                        successColor = successColor,
                    )
                }
            }
        }
    }
}

@Composable
private fun BranchContactSubCard(
    icon: ImageVector,
    title: String,
    contactNameLabel: String,
    contactNameValue: String,
    contactNumberLabel: String,
    contactNumberValue: String,
    isEditing: Boolean,
    onNameChange: (String) -> Unit,
    onNumberChange: (String) -> Unit,
    successColor: Color,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                BranchContactField(
                    modifier = Modifier.weight(1f),
                    label = contactNameLabel,
                    value = contactNameValue,
                    isEditing = isEditing,
                    onValueChange = onNameChange,
                )
                BranchContactField(
                    modifier = Modifier.weight(1f),
                    label = contactNumberLabel,
                    value = contactNumberValue,
                    isEditing = isEditing,
                    isPhoneNumber = true,
                    onValueChange = onNumberChange,
                    successColor = successColor,
                )
            }
        }
    }
}

@Composable
private fun BranchLogisticsTrackingSubCard(
    trafficName: String,
    trafficNumber: String,
    trackingName: String,
    trackingNumber: String,
    isEditing: Boolean,
    onTrafficNameChange: (String) -> Unit,
    onTrafficNumberChange: (String) -> Unit,
    onTrackingNameChange: (String) -> Unit,
    onTrackingNumberChange: (String) -> Unit,
    successColor: Color,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocalShipping,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "LOGISTICS & TRACKING",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Traffic row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                BranchContactField(
                    modifier = Modifier.weight(1f),
                    label = "TRAFFIC NAME",
                    value = trafficName,
                    isEditing = isEditing,
                    onValueChange = onTrafficNameChange,
                )
                BranchContactField(
                    modifier = Modifier.weight(1f),
                    label = "TRAFFIC NUM",
                    value = trafficNumber,
                    isEditing = isEditing,
                    isPhoneNumber = true,
                    onValueChange = onTrafficNumberChange,
                    successColor = successColor,
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                thickness = 1.dp,
            )
            Spacer(modifier = Modifier.height(10.dp))

            // Tracking row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                BranchContactField(
                    modifier = Modifier.weight(1f),
                    label = "TRACKING NAME",
                    value = trackingName,
                    isEditing = isEditing,
                    onValueChange = onTrackingNameChange,
                )
                BranchContactField(
                    modifier = Modifier.weight(1f),
                    label = "TRACKING NUM",
                    value = trackingNumber,
                    isEditing = isEditing,
                    isPhoneNumber = true,
                    onValueChange = onTrackingNumberChange,
                    successColor = successColor,
                )
            }
        }
    }
}

@Composable
private fun BranchContactField(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    isEditing: Boolean,
    isPhoneNumber: Boolean = false,
    onValueChange: (String) -> Unit = {},
    successColor: Color = Color.Unspecified,
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var showPhoneMenu by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(modifier = Modifier.height(4.dp))
        if (isEditing) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = MaterialTheme.shapes.small,
                colors =
                    OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (isPhoneNumber) successColor else MaterialTheme.colorScheme.primary,
                    ),
                textStyle =
                    MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = if (isPhoneNumber && value.isNotBlank()) successColor else MaterialTheme.colorScheme.onSurface,
                    ),
                keyboardOptions =
                    KeyboardOptions(
                        keyboardType = if (isPhoneNumber) KeyboardType.Phone else KeyboardType.Text,
                        capitalization = KeyboardCapitalization.None,
                    ),
            )
        } else {
            val displayValue = value.ifBlank { "—" }
            val isPhoneValid = isPhoneNumber && value.isNotBlank()
            Box {
                Row(
                    modifier =
                        Modifier
                            .then(
                                if (isPhoneValid) {
                                    Modifier.pointerInput(Unit) {
                                        detectTapGestures(
                                            onLongPress = { showPhoneMenu = true },
                                        )
                                    }
                                } else {
                                    Modifier
                                },
                            ),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = displayValue,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isPhoneValid) successColor else MaterialTheme.colorScheme.onSurface,
                    )
                }

                DropdownMenu(
                    expanded = showPhoneMenu,
                    onDismissRequest = { showPhoneMenu = false },
                ) {
                    DropdownMenuItem(
                        text = { Text("Call") },
                        leadingIcon = { Icon(Icons.Default.Call, contentDescription = null) },
                        onClick = {
                            showPhoneMenu = false
                            launchDialer(context, value)
                        },
                    )
                    HorizontalDivider()
                    DropdownMenuItem(
                        text = { Text("Copy Number") },
                        leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null) },
                        onClick = {
                            showPhoneMenu = false
                            clipboardManager.setText(AnnotatedString(value))
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileHeaderCard(
    fullName: String,
    companyName: String,
    tagCode: String,
    initials: String,
    status: String,
) {
    val normalizedStatus = status.lowercase()
    val isActive = normalizedStatus == "active"
    val chipColor = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error

    val titleText = fullName.ifBlank { companyName.ifBlank { "Client" } }
    val showCompanyLine = fullName.isNotBlank() || companyName.isBlank()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier =
                    Modifier
                        .size(72.dp)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = initials,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = titleText,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
                if (showCompanyLine) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = companyName.ifBlank { "Company not added" },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Tag Code: ${tagCode.ifBlank { "Not available" }}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline,
                )
            }
            VerificationChip(
                text = if (isActive) "Active" else "Inactive",
                color = chipColor,
            )
        }
    }
}

@Composable
private fun ProfileSection(
    title: String,
    icon: ImageVector,
    initiallyExpanded: Boolean,
    content: @Composable ColumnScope.() -> Unit,
) {
    var isExpanded by rememberSaveable { mutableStateOf(initiallyExpanded) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded }
                    .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f),
            )
            Icon(
                imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = if (isExpanded) "Collapse section" else "Expand section",
                tint = MaterialTheme.colorScheme.primary,
            )
        }

        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically(),
            exit = shrinkVertically(),
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            ) {
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    content()
                }
            }
        }
    }
}

@Composable
private fun ProfileField(
    label: String,
    value: String,
    isEditing: Boolean,
    isEditable: Boolean = true,
    leadingIcon: ImageVector? = null,
    onLeadingIconClick: (() -> Unit)? = null,
    onValueChange: (String) -> Unit = {},
    keyboardType: KeyboardType = KeyboardType.Text,
    capitalization: KeyboardCapitalization = KeyboardCapitalization.Words,
    placeholder: String = "Not added",
    isPhoneNumber: Boolean = false,
    onFocusLost: (() -> Unit)? = null,
    duplicateError: String? = null,
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var showPhoneMenu by remember { mutableStateOf(false) }

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        if (leadingIcon != null) {
            val iconModifier =
                if (onLeadingIconClick != null) {
                    Modifier.clickable(onClick = onLeadingIconClick)
                } else {
                    Modifier
                }
            Icon(
                imageVector = leadingIcon,
                contentDescription = label,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = iconModifier.size(24.dp),
            )
            Spacer(modifier = Modifier.width(20.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 0.6.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(6.dp))
            if (isEditing && isEditable) {
                var isFocused by remember { mutableStateOf(false) }
                OutlinedTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .onFocusChanged { state ->
                                if (isFocused && !state.isFocused) {
                                    onFocusLost?.invoke()
                                }
                                isFocused = state.isFocused
                            },
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium,
                    colors =
                        OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                        ),
                    keyboardOptions =
                        KeyboardOptions(
                            keyboardType = keyboardType,
                            capitalization = capitalization,
                        ),
                    supportingText =
                        if (duplicateError != null) {
                            {
                                Text(
                                    text = duplicateError,
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall,
                                )
                            }
                        } else {
                            null
                        },
                )
            } else {
                val displayValue = value.ifBlank { placeholder }
                val isPhoneValid = isPhoneNumber && value.isNotBlank()
                Box {
                    Row(
                        modifier =
                            Modifier
                                .then(
                                    if (isPhoneValid && !isEditing) {
                                        Modifier.pointerInput(Unit) {
                                            detectTapGestures(onLongPress = { showPhoneMenu = true })
                                        }
                                    } else {
                                        Modifier
                                    },
                                ),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = displayValue,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = if (value.isBlank()) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurface,
                        )
                    }

                    DropdownMenu(
                        expanded = showPhoneMenu,
                        onDismissRequest = { showPhoneMenu = false },
                    ) {
                        DropdownMenuItem(
                            text = { Text("Call") },
                            leadingIcon = { Icon(Icons.Default.Call, contentDescription = null) },
                            onClick = {
                                showPhoneMenu = false
                                launchDialer(context, value)
                            },
                        )
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { Text("Copy Number") },
                            leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null) },
                            onClick = {
                                showPhoneMenu = false
                                clipboardManager.setText(AnnotatedString(value))
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DividerLine() {
    HorizontalDivider(
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
        thickness = 1.dp,
        modifier = Modifier.padding(horizontal = 18.dp),
    )
}

@Composable
private fun VerificationChip(
    text: String,
    color: Color,
) {
    Surface(
        color = color.copy(alpha = 0.12f),
        shape = MaterialTheme.shapes.medium,
    ) {
        Text(
            text = text,
            color = color,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        )
    }
}

@Composable
private fun EditActions(
    hasChanges: Boolean,
    onSave: () -> Unit,
    onCancel: () -> Unit,
    isSaving: Boolean,
) {
    Surface(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 4.dp,
        shadowElevation = 10.dp,
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f),
                shape = MaterialTheme.shapes.medium,
                enabled = !isSaving,
            ) {
                Text("Cancel", fontWeight = FontWeight.Bold)
            }
            Button(
                onClick = onSave,
                modifier = Modifier.weight(1f),
                shape = MaterialTheme.shapes.medium,
                enabled = hasChanges && !isSaving,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                } else {
                    Text("Save Changes", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun ClientProfileErrorState(
    message: String,
    onRetry: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Default.ErrorOutline,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(48.dp),
        )
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedButton(onClick = onRetry) {
            Text("Retry")
        }
    }
}

@Composable
private fun DocumentList(
    documents: List<ClientDocumentDto>,
    documentStates: Map<String, DocumentUploadState>,
    isEditing: Boolean,
    onDocumentView: (String) -> Unit,
    onDocumentUpload: (String) -> Unit,
) {
    val types =
        listOf(
            ClientDocumentTypeUi("aadhaar", "Aadhaar Card"),
            ClientDocumentTypeUi("pan", "PAN Card"),
        )

    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        types.forEachIndexed { index, type ->
            val uploadState = documentStates[type.fieldKey]
            val doc = documents.find { it.documentName.equals(type.displayName, ignoreCase = true) }
            val existingUrl = uploadState?.existingFileUrl ?: doc?.documentUrl

            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (isEditing) {
                                onDocumentUpload(type.fieldKey)
                            } else if (!existingUrl.isNullOrEmpty() && !doc?.documentId.isNullOrBlank()) {
                                onDocumentView(doc!!.documentId)
                            }
                        }
                        .padding(horizontal = 20.dp, vertical = 18.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector =
                        when (type.displayName) {
                            "PAN Card" -> Icons.Default.CreditCard
                            "Aadhaar Card" -> Icons.Default.Fingerprint
                            else -> Icons.Default.Description
                        },
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = type.displayName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.onSurface,
                )

                if (isEditing) {
                    when (uploadState?.uploadStatus ?: UploadStatus.IDLE) {
                        UploadStatus.UPLOADING -> {
                            LinearProgressIndicator(
                                modifier = Modifier.width(88.dp),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }

                        UploadStatus.DONE -> {
                            PendingSaveBadge()
                            Spacer(modifier = Modifier.width(8.dp))
                        }

                        UploadStatus.ERROR -> {
                            UploadFailedBadge()
                            Spacer(modifier = Modifier.width(8.dp))
                        }

                        UploadStatus.IDLE -> {
                            if (!existingUrl.isNullOrEmpty()) {
                                ExistingStatusBadge(uploadState?.existingVerifyStatus, doc?.documentStatus)
                                Spacer(modifier = Modifier.width(8.dp))
                            } else {
                                VerificationChip("Not Uploaded", MaterialTheme.colorScheme.outline)
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                        }
                    }

                    IconButton(
                        onClick = { onDocumentUpload(type.fieldKey) },
                        enabled = (uploadState?.uploadStatus ?: UploadStatus.IDLE) != UploadStatus.UPLOADING,
                    ) {
                        Icon(
                            imageVector =
                                if ((uploadState?.uploadStatus ?: UploadStatus.IDLE) == UploadStatus.ERROR) {
                                    Icons.Default.Refresh
                                } else {
                                    Icons.Default.CloudUpload
                                },
                            contentDescription = "Upload",
                            tint =
                                if ((uploadState?.uploadStatus ?: UploadStatus.IDLE) == UploadStatus.ERROR) {
                                    MaterialTheme.colorScheme.error
                                } else {
                                    FreightThemeTokens.colors.success
                                },
                            modifier = Modifier.size(24.dp),
                        )
                    }
                } else if (!existingUrl.isNullOrEmpty()) {
                    ExistingStatusBadge(uploadState?.existingVerifyStatus, doc?.documentStatus)
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "View",
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(20.dp),
                    )
                } else {
                    VerificationChip("Not Uploaded", MaterialTheme.colorScheme.outline)
                }
            }
            if (index < types.lastIndex) {
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    thickness = 1.dp,
                    modifier = Modifier.padding(horizontal = 20.dp),
                )
            }
        }
    }
}

@Composable
private fun ExistingStatusBadge(
    existingVerifyStatus: String?,
    fallbackStatus: String?,
) {
    val status = existingVerifyStatus?.lowercase()?.trim() ?: fallbackStatus?.lowercase()?.trim().orEmpty()
    val (text, color) =
        when (status) {
            "verified" -> "Verified" to FreightThemeTokens.colors.success
            "rejected" -> "Rejected" to MaterialTheme.colorScheme.error
            "pending" -> "Verification Pending" to MaterialTheme.colorScheme.tertiary
            else -> "N/A" to MaterialTheme.colorScheme.onSurfaceVariant
        }

    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(8.dp),
    ) {
        Text(
            text = text,
            color = color,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
        )
    }
}

@Composable
private fun PendingSaveBadge() {
    Surface(
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0f),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Text(
            text = "Pending Save",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
        )
    }
}

@Composable
private fun UploadFailedBadge() {
    Surface(
        color = MaterialTheme.colorScheme.errorContainer,
        shape = RoundedCornerShape(8.dp),
    ) {
        Text(
            text = "Upload Failed",
            color = MaterialTheme.colorScheme.onErrorContainer,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
        )
    }
}

private data class ClientDocumentTypeUi(
    val fieldKey: String,
    val displayName: String,
)

private fun launchDialer(
    context: Context,
    rawPhone: String,
) {
    val phone = rawPhone.substringBefore(",").trim()
    if (phone.isBlank()) return
    val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${Uri.encode(phone)}"))
    context.startActivity(dialIntent)
}

private fun createTempImageUri(context: Context): Uri {
    val tempFile = File(context.cacheDir, "camera_capture_${System.currentTimeMillis()}.jpg")
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        tempFile,
    )
}
