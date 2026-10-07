package com.freight24.connect.feature.truckowners.profile.view

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.freight24.common.util.DocumentUploadPolicy
import com.freight24.common.model.DocumentUploadState
import com.freight24.common.model.UploadStatus
import com.freight24.connect.feature.truckowners.profile.domain.DocumentStatus
import com.freight24.connect.feature.truckowners.profile.domain.DocumentType
import com.freight24.connect.feature.truckowners.profile.domain.Profile
import com.freight24.connect.feature.truckowners.profile.domain.ProfileDocument
import com.freight24.connect.feature.truckowners.profile.util.ProfileDocumentTypeIds
import com.freight24.connect.feature.truckowners.profile.util.ProfileFieldValidators
import com.freight24.connect.feature.truckowners.profile.util.ValidationResult
import com.freight24.ui.UiState
import com.freight24.ui.components.AccountStatusCard
import com.freight24.ui.components.AddRouteBottomSheet
import com.freight24.ui.components.CitySearchField
import com.freight24.ui.components.DocketCard
import com.freight24.ui.components.FreightTopAppBar
import com.freight24.ui.components.PanelShape
import com.freight24.ui.components.RouteChip
import com.freight24.ui.components.TagShape
import com.freight24.ui.components.formViewportInsets
import com.freight24.ui.theme.FreightThemeTokens
import kotlinx.coroutines.launch
import java.io.File

/**
 * Enum representing the type of validation for profile fields.
 * Used to apply Indian-specific validation rules.
 */
private enum class ValidationType {
    NONE,
    AADHAAR,
    PAN,
    GST,
    IFSC,
    UPI,
    PHONE,
    EMAIL,
    ACCOUNT_NUMBER,
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
internal fun ProfileScreen(
    onBackClick: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val isEditing by viewModel.isEditing.collectAsState()
    val hasChanges by viewModel.hasChanges.collectAsState()
    val panDuplicateError by viewModel.panDuplicateError.collectAsState()
    val gstDuplicateError by viewModel.gstDuplicateError.collectAsState()
    val showAddRouteSheet by viewModel.showAddRouteSheet.collectAsState()
    val fromQuery by viewModel.fromCityQuery.collectAsState()
    val toQuery by viewModel.toCityQuery.collectAsState()
    val fromSuggestions by viewModel.fromCitySuggestions.collectAsState()
    val toSuggestions by viewModel.toCitySuggestions.collectAsState()
    val fromLoading by viewModel.fromCityLoading.collectAsState()
    val toLoading by viewModel.toCityLoading.collectAsState()
    val selectedFromCity by viewModel.selectedFromCity.collectAsState()
    val selectedToCity by viewModel.selectedToCity.collectAsState()
    val priority by viewModel.selectedPriority.collectAsState()
    val routeSheetError by viewModel.routeSheetError.collectAsState()

    val snackbarHostState = androidx.compose.runtime.remember { androidx.compose.material3.SnackbarHostState() }

    androidx.compose.runtime.LaunchedEffect(Unit) {
        launch {
            viewModel.errorEvent.collect { message ->
                snackbarHostState.showSnackbar(
                    message = message,
                    duration = SnackbarDuration.Short,
                )
            }
        }
        launch {
            viewModel.saveEvent.collect { result ->
                result.onSuccess {
                    snackbarHostState.showSnackbar("Profile saved successfully")
                }
            }
        }
    }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = { viewModel.onRefresh() },
        modifier = Modifier.fillMaxSize(),
    ) {
        androidx.compose.material3.Scaffold(
            snackbarHost = { androidx.compose.material3.SnackbarHost(hostState = snackbarHostState) },
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                FreightTopAppBar(
                    title = {
                        Text(
                            text = "Truck Owner Profile",
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
                            IconButton(
                                onClick = { viewModel.toggleEditMode() },
                                modifier =
                                    Modifier
                                        .testTag("owner_edit_button")
                                        .semantics { testTagsAsResourceId = true },
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit profile",
                                )
                            }
                        }
                    },
                )
            },
        ) { innerPadding ->
            // Keyboard-aware viewport; see FormInsets.kt.
            Box(modifier = Modifier.formViewportInsets(innerPadding)) {
                when (val state = uiState) {
                    is UiState.Loading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(strokeWidth = 3.dp, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                    is UiState.Error -> {
                        ProfileErrorState(message = state.message, onRetry = { viewModel.retry() })
                    }
                    is UiState.Success -> {
                        ProfileMainContent(
                            profile = state.data,
                            viewModel = viewModel,
                            isEditing = isEditing,
                            panDuplicateError = panDuplicateError,
                            gstDuplicateError = gstDuplicateError,
                            hasChanges = hasChanges,
                            snackbarHostState = snackbarHostState,
                        )
                    }
                    UiState.Idle -> {}
                }
                if (showAddRouteSheet) {
                    AddRouteBottomSheet(
                        fromQuery = fromQuery,
                        onFromQueryChange = viewModel::onFromCityQueryChanged,
                        fromSuggestions = fromSuggestions,
                        onFromCitySelected = viewModel::onFromCitySelected,
                        isFromLoading = fromLoading,
                        selectedFromCity = selectedFromCity,
                        isFromPreFilled = selectedFromCity?.id == viewModel.baseCity.value?.id,
                        toQuery = toQuery,
                        onToQueryChange = viewModel::onToCityQueryChanged,
                        toSuggestions = toSuggestions,
                        onToCitySelected = viewModel::onToCitySelected,
                        isToLoading = toLoading,
                        selectedToCity = selectedToCity,
                        selectedPriority = priority,
                        onPrioritySelected = { viewModel.selectedPriority.value = it },
                        onConfirm = viewModel::onStageRouteAdd,
                        onDismiss = viewModel::dismissAddRouteSheet,
                        errorMessage = routeSheetError,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun ProfileMainContent(
    profile: Profile,
    isEditing: Boolean,
    viewModel: ProfileViewModel,
    panDuplicateError: String?,
    gstDuplicateError: String?,
    hasChanges: Boolean,
    snackbarHostState: SnackbarHostState,
) {
    val documentStates by viewModel.documentStates.collectAsState()
    val hasPendingTemporaryUploads by viewModel.hasPendingTemporaryUploads.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()
    val baseCity by viewModel.baseCity.collectAsState()
    val baseCityQuery by viewModel.baseCityQuery.collectAsState()
    val baseCitySuggestions by viewModel.baseCitySuggestions.collectAsState()
    val baseCityLoading by viewModel.baseCitySearchLoading.collectAsState()
    val baseLocation by viewModel.baseLocation.collectAsState()
    val visibleRoutes by viewModel.visibleRoutes.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val isVerified = profile.profileVerificationStatus?.lowercase() == "verified"
    val profileStatus = profile.status?.lowercase() ?: "inactive"

    var pickingDocumentType by rememberSaveable { mutableStateOf<DocumentType?>(null) }
    var showSourcePicker by rememberSaveable { mutableStateOf(false) }
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }
    var pickingUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var isPdfPending by rememberSaveable { mutableStateOf(false) }
    var showCancelConfirmDialog by rememberSaveable { mutableStateOf(false) }
    val sourceSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val selectedUploadStatus =
        pickingDocumentType
            ?.let { type -> documentStates[ProfileDocumentTypeIds.fieldKey(type)]?.uploadStatus }
            ?: UploadStatus.IDLE

    val documentPickerLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.OpenDocument(),
        ) { uri ->
            if (uri != null) {
                pickingUris = listOf(uri)
                isPdfPending = true
            } else {
                pickingDocumentType = null
            }
        }

    val imagePickerLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.PickMultipleVisualMedia(DocumentUploadPolicy.MAX_IMAGES),
        ) { uris ->
            if (uris.isNotEmpty()) {
                pickingUris = uris
                isPdfPending = false
            } else {
                pickingDocumentType = null
            }
        }

    val cameraLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.TakePicture(),
            onResult = { success ->
                if (success && tempCameraUri != null) {
                    pickingUris = listOf(tempCameraUri!!)
                    isPdfPending = false
                } else {
                    pickingDocumentType = null
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

    com.freight24.ui.components.FileSourcePickerSheet(
        showSheet = showSourcePicker,
        onDismissRequest = {
            showSourcePicker = false
            pickingDocumentType = null
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
                imagePickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
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

    if (pickingUris.isNotEmpty()) {
        com.freight24.ui.components.FileConfirmationSheet(
            selectedUris = pickingUris,
            isPdf = isPdfPending,
            isUploading = selectedUploadStatus == UploadStatus.UPLOADING,
            uploadButtonText = "Confirm Upload",
            onUploadClick = {
                pickingDocumentType?.let { type ->
                    viewModel.uploadDocument(pickingUris, type, isPdf = isPdfPending)
                }
                pickingUris = emptyList()
                pickingDocumentType = null
            },
            onDismiss = {
                pickingUris = emptyList()
                pickingDocumentType = null
            },
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 24.dp),
        ) {
//            // Top Title & Settings Row
//            Row(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(vertical = 24.dp),
//                horizontalArrangement = Arrangement.SpaceBetween,
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                Text(
//                    text = "Profile",
//                    style = MaterialTheme.typography.headlineMedium,
//                    fontWeight = FontWeight.Bold,
//                    color = MaterialTheme.colorScheme.onBackground
//                )
//                IconButton(onClick = { /* TODO */ }) {
//                    Icon(
//                        imageVector = Icons.Default.Settings,
//                        contentDescription = "Settings",
//                        modifier = Modifier.size(24.dp)
//                    )
//                }
//            }

            // Profile Header Card
            ProfileHeaderCard(
                fullName = viewModel.editableFullName.collectAsState().value,
                partnerId = profile.tagCode.orEmpty(),
                initials = viewModel.getUserInitials(),
                role = "Truck Owner",
                verificationStatus = profileStatus,
                status = profile.status.orEmpty(),
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (isEditing) {
                AccountStatusCard(
                    isActive = viewModel.isAccountActive.collectAsState().value,
                    isToggling = viewModel.isTogglingStatus.collectAsState().value,
                    onToggle = { viewModel.toggleAccountStatus() },
                )
                Spacer(modifier = Modifier.height(32.dp))
            } else {
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Information Sections
            ProfileSection(title = "PERSONAL INFORMATION", icon = Icons.Default.Person, true, testTag = "owner_section_personal") {
                ProfileField(
                    label = "FULL NAME",
                    value = viewModel.editableFullName.collectAsState().value,
                    isEditing = isEditing,
                    isEditable = true,
                    onValueChange = {
                        viewModel.editableFullName.value = it
                        viewModel.onFieldChanged()
                    },
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), thickness = 1.dp)
                ProfileField(
                    label = "EMAIL ADDRESS",
                    value = viewModel.editableEmailId.collectAsState().value,
                    isEditing = isEditing,
                    isEditable = !isVerified,
                    validationType = ValidationType.EMAIL,
                    onValueChange = {
                        viewModel.editableEmailId.value = it
                        viewModel.onFieldChanged()
                    },
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), thickness = 1.dp)
                ProfileField(
                    label = "PHONE NUMBER",
                    value = viewModel.editablePhoneNo.collectAsState().value,
                    isEditing = isEditing,
                    isEditable = false, // Phone number is never editable - primary key factor
                    showAsLabelOnly = true,
                    onCallClick = { launchDialer(context, viewModel.editablePhoneNo.value) },
                    onValueChange = { },
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), thickness = 1.dp)
                ProfileField(
                    label = "ALTERNATIVE PHONE",
                    value = viewModel.editableAlternativePhone.collectAsState().value,
                    isEditing = isEditing,
                    isEditable = true,
                    validationType = ValidationType.PHONE,
                    onCallClick = { launchDialer(context, viewModel.editableAlternativePhone.value) },
                    onValueChange = {
                        viewModel.editableAlternativePhone.value = it
                        viewModel.onFieldChanged()
                    },
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), thickness = 1.dp)
                ProfileField(
                    label = "AADHAAR NUMBER",
                    value = viewModel.editableAadhaarNumber.collectAsState().value,
                    isEditing = isEditing,
                    isEditable = !isVerified,
                    validationType = ValidationType.AADHAAR,
                    onValueChange = {
                        viewModel.editableAadhaarNumber.value = it
                        viewModel.onFieldChanged()
                    },
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), thickness = 1.dp)
                ProfileField(
                    label = "PAN NUMBER",
                    value = viewModel.editablePanNumber.collectAsState().value,
                    isEditing = isEditing,
                    isEditable = !isVerified,
                    validationType = ValidationType.PAN,
                    onValueChange = {
                        viewModel.editablePanNumber.value = it.uppercase()
                        viewModel.onFieldChanged()
                    },
                    onFocusLost = viewModel::onPanFocusLost,
                    duplicateError = panDuplicateError,
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            ProfileSection(
                title = "BUSINESS DETAILS",
                icon = Icons.Default.Business,
                initiallyExpanded = false,
                isVerified = profile.documentsVerificationStatus,
                testTag = "owner_section_business",
            ) {
                ProfileField(
                    label = "COMPANY NAME",
                    value = viewModel.editableBusinessName.collectAsState().value,
                    isEditing = isEditing,
                    isEditable = !isVerified,
                    onValueChange = {
                        viewModel.editableBusinessName.value = it
                        viewModel.onFieldChanged()
                    },
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), thickness = 1.dp)
                ProfileField(
                    label = "GST NUMBER",
                    value = viewModel.editableGstNumber.collectAsState().value,
                    isEditing = isEditing,
                    isEditable = !isVerified,
                    validationType = ValidationType.GST,
                    onValueChange = {
                        viewModel.editableGstNumber.value = it
                        viewModel.onFieldChanged()
                    },
                    onFocusLost = viewModel::onGstFocusLost,
                    duplicateError = gstDuplicateError,
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), thickness = 1.dp)
                Column(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                ) {
                    Text(
                        text = "BASE LOCATION",
                        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.1.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    if (isEditing) {
                        CitySearchField(
                            query = baseCityQuery,
                            onQueryChange = viewModel::onBaseCityQueryChanged,
                            suggestions = baseCitySuggestions,
                            onCitySelected = viewModel::onBaseCitySelected,
                            isLoading = baseCityLoading,
                            label = "Base Location",
                            selectAllOnFocus = true,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    } else {
                        val locationLabel = baseLocation?.takeIf { it.isNotBlank() } ?: baseCity?.city
                        Text(
                            text = locationLabel ?: "—",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color =
                                if (!locationLabel.isNullOrBlank()) {
                                    MaterialTheme.colorScheme.onSurface
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), thickness = 1.dp)
                Column(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                ) {
                    Text(
                        text = "PREFERRED ROUTES",
                        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.1.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        visibleRoutes.forEach { route ->
                            RouteChip(
                                fromCity = route.sourceCityName,
                                toCity = route.destCityName,
                                isStaged = route.isStaged,
                                onDelete =
                                    if (isEditing) {
                                        { viewModel.onStageRouteDelete(route.id, route.isStaged) }
                                    } else {
                                        null
                                    },
                            )
                        }
                    }
                    if (visibleRoutes.isEmpty() && !isEditing) {
                        Text(
                            "No preferred routes added",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    if (isEditing) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start,
                        ) {
                            AssistChip(
                                onClick = viewModel::openAddRouteSheet,
                                label = { Text("+ Add route", style = MaterialTheme.typography.labelMedium) },
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
                                colors =
                                    AssistChipDefaults.assistChipColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f),
                                        labelColor = MaterialTheme.colorScheme.primary,
                                    ),
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            ProfileSection(
                title = "BANKING INFORMATION",
                icon = Icons.Default.AccountBalance,
                initiallyExpanded = false,
                isVerified = profile.bankAccountVerificationStatus,
                testTag = "owner_section_banking",
            ) {
                ProfileField(
                    label = "ACCOUNT HOLDER",
                    value = viewModel.editableAccountHolderName.collectAsState().value,
                    isEditing = isEditing,
                    isEditable = !isVerified,
                    onValueChange = {
                        viewModel.editableAccountHolderName.value = it
                        viewModel.onFieldChanged()
                    },
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), thickness = 1.dp)
                ProfileField(
                    label = "ACCOUNT NUMBER",
                    value = viewModel.editableAccountNumber.collectAsState().value,
                    isEditing = isEditing,
                    isSensitive = true,
                    isEditable = !isVerified,
                    validationType = ValidationType.ACCOUNT_NUMBER,
                    onValueChange = {
                        viewModel.editableAccountNumber.value = it
                        viewModel.onFieldChanged()
                    },
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), thickness = 1.dp)
                ProfileField(
                    label = "IFSC CODE",
                    value = viewModel.editableIfscCode.collectAsState().value,
                    isEditing = isEditing,
                    isEditable = !isVerified,
                    validationType = ValidationType.IFSC,
                    onValueChange = {
                        viewModel.editableIfscCode.value = it.uppercase()
                        viewModel.onFieldChanged()
                    },
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), thickness = 1.dp)
                ProfileField(
                    label = "UPI ID",
                    value = viewModel.editableUpiId.collectAsState().value,
                    isEditing = isEditing,
                    isEditable = !isVerified,
                    validationType = ValidationType.UPI,
                    onValueChange = {
                        viewModel.editableUpiId.value = it.lowercase()
                        viewModel.onFieldChanged()
                    },
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), thickness = 1.dp)
                ProfileField(
                    label = "BANK NAME",
                    value = viewModel.editableBankName.collectAsState().value,
                    isEditing = isEditing,
                    isEditable = !isVerified,
                    onValueChange = {
                        viewModel.editableBankName.value = it
                        viewModel.onFieldChanged()
                    },
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), thickness = 1.dp)
                ProfileField(
                    label = "BRANCH NAME",
                    value = viewModel.editableBranchName.collectAsState().value,
                    isEditing = isEditing,
                    isEditable = !isVerified,
                    onValueChange = {
                        viewModel.editableBranchName.value = it
                        viewModel.onFieldChanged()
                    },
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Documents Section
            ProfileSection(
                title = "DOCUMENTS",
                icon = Icons.AutoMirrored.Filled.Assignment,
                initiallyExpanded = false,
                isVerified = profile.documentsVerificationStatus,
                testTag = "owner_section_documents",
            ) {
                DocumentList(
                    documents = profile.documents,
                    documentStates = documentStates,
                    isEditing = isEditing,
                    onDocumentView = { documentId ->
                        viewModel.getSignedUrl(documentId) { signedUrl ->
                            context.startActivity(com.freight24.ui.components.getPdfLaunchIntent(signedUrl, context))
                        }
                    },
                    onDocumentUpload = { type ->
                        pickingDocumentType = type
                        showSourcePicker = true
                    },
                )
            }

            Spacer(modifier = Modifier.height(48.dp))
            Spacer(modifier = Modifier.height(120.dp))
        }

        // Action Buttons at Bottom in Edit Mode
        AnimatedContent(
            targetState = isEditing,
            transitionSpec = { slideInVertically { it } togetherWith slideOutVertically { it } },
            modifier = Modifier.align(Alignment.BottomCenter),
            label = "ActionButtons",
        ) { editing ->
            if (editing) {
                EditActions(
                    hasChanges = hasChanges,
                    isSaving = isSaving,
                    onSave = { viewModel.saveProfile() },
                    onCancel = {
                        if (hasPendingTemporaryUploads) {
                            showCancelConfirmDialog = true
                        } else {
                            viewModel.cancelEdit()
                        }
                    },
                )
            }
        }

        if (showCancelConfirmDialog) {
            androidx.compose.material3.AlertDialog(
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
    }
}

@Composable
private fun ProfileHeaderCard(
    fullName: String,
    partnerId: String,
    initials: String,
    role: String,
    verificationStatus: String,
    status: String,
) {
    val scheme = MaterialTheme.colorScheme
    val ext = FreightThemeTokens.colors
    val normalizedStatus = status.lowercase()
    val isActive = normalizedStatus == "active"
    val isVerified = verificationStatus == "verified" || verificationStatus == "active"

    DocketCard(
        // Spine keyed to the account state: open for business or shut.
        accent = if (isActive) ext.success else scheme.error,
        testTag = "owner_header_card",
        onClick = null,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(contentAlignment = Alignment.BottomEnd) {
                Surface(
                    modifier = Modifier.size(52.dp),
                    shape = CircleShape,
                    color = scheme.primaryContainer,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = initials,
                            style = MaterialTheme.typography.titleMedium,
                            color = scheme.onPrimaryContainer,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
                if (isVerified) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = verificationStatus,
                        tint = ext.success,
                        modifier =
                            Modifier
                                .size(18.dp)
                                .background(scheme.surface, CircleShape)
                                .padding(1.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = role.uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.4.sp),
                    fontWeight = FontWeight.SemiBold,
                    color = scheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = fullName.ifEmpty { "Member" },
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = scheme.onSurface,
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = partnerId,
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.1.sp),
                    fontWeight = FontWeight.SemiBold,
                    color = scheme.onSurfaceVariant.copy(alpha = 0.7f),
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            ProfileStamp(
                text = if (isActive) "ACTIVE" else "INACTIVE",
                container = if (isActive) ext.surfaceSuccess else scheme.errorContainer,
                content = if (isActive) ext.onSurfaceSuccess else scheme.onErrorContainer,
            )
        }
    }
}

/** Small tracked-caps rubber stamp used across the owner profile. */
@Composable
private fun ProfileStamp(
    text: String,
    container: Color,
    content: Color,
    modifier: Modifier = Modifier,
    bordered: Boolean = false,
) {
    Surface(
        modifier = modifier,
        shape = TagShape,
        color = if (bordered) Color.Transparent else container,
        border = if (bordered) BorderStroke(1.dp, container) else null,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
            fontWeight = FontWeight.Bold,
            color = content,
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
        )
    }
}

@Composable
private fun ProfileSection(
    title: String,
    icon: ImageVector,
    initiallyExpanded: Boolean,
    isVerified: Boolean = false,
    testTag: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    var isExpanded by rememberSaveable { mutableStateOf(initiallyExpanded) }
    val scheme = MaterialTheme.colorScheme
    val ext = FreightThemeTokens.colors

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .testTag(testTag)
                    .semantics { testTagsAsResourceId = true }
                    .clickable { isExpanded = !isExpanded }
                    .padding(vertical = 8.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp),
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 1.sp),
                color = scheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
            )

            if (isVerified) {
                ProfileStamp(
                    text = "VERIFIED",
                    container = ext.surfaceSuccess,
                    content = ext.onSurfaceSuccess,
                )
            } else {
                ProfileStamp(
                    text = "UNVERIFIED",
                    container = ext.surfaceWarning,
                    content = ext.onSurfaceWarning,
                )
            }

            Spacer(modifier = Modifier.width(10.dp))
            Icon(
                imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = if (isExpanded) "Collapse" else "Expand",
                tint = scheme.onSurfaceVariant,
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically(),
            exit = shrinkVertically(),
        ) {
            // Flat paper panel — hairline border, no elevation shadow, the
            // same treatment as the verification sheet's ledger panels.
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = PanelShape,
                color = scheme.surface,
                border = BorderStroke(1.dp, scheme.outlineVariant.copy(alpha = 0.5f)),
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 6.dp),
                ) {
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
    onCallClick: (() -> Unit)? = null,
    onValueChange: (String) -> Unit = {},
    isSensitive: Boolean = false,
    validationType: ValidationType = ValidationType.NONE,
    showAsLabelOnly: Boolean = false,
    onFocusLost: (() -> Unit)? = null,
    duplicateError: String? = null,
) {
    // State for validation error
    var validationError by rememberSaveable { mutableStateOf<String?>(null) }

    // Validate on value change
    val handleValueChange: (String) -> Unit = { newValue ->
        // Apply input filtering based on validation type
        val filteredValue =
            when (validationType) {
                ValidationType.AADHAAR -> newValue.filter { it.isDigit() }.take(12)
                ValidationType.PHONE -> newValue.filter { it.isDigit() }.take(10)
                ValidationType.ACCOUNT_NUMBER -> newValue.filter { it.isDigit() }.take(18)
                ValidationType.IFSC -> newValue.uppercase().filter { it.isLetterOrDigit() }.take(11)
                ValidationType.PAN -> newValue.uppercase().filter { it.isLetterOrDigit() }.take(10)
                ValidationType.GST -> newValue.uppercase().filter { it.isLetterOrDigit() }.take(15)
                else -> newValue
            }

        onValueChange(filteredValue)

        // Validate and show error
        validationError =
            when (validationType) {
                ValidationType.AADHAAR -> {
                    val result = ProfileFieldValidators.validateAadhaar(filteredValue)
                    if (result is ValidationResult.Error) result.message else null
                }
                ValidationType.PAN -> {
                    val result = ProfileFieldValidators.validatePan(filteredValue)
                    if (result is ValidationResult.Error) result.message else null
                }
                ValidationType.GST -> {
                    val result = ProfileFieldValidators.validateGst(filteredValue)
                    if (result is ValidationResult.Error) result.message else null
                }
                ValidationType.IFSC -> {
                    val result = ProfileFieldValidators.validateIfsc(filteredValue)
                    if (result is ValidationResult.Error) result.message else null
                }
                ValidationType.UPI -> {
                    val result = ProfileFieldValidators.validateUpiId(filteredValue)
                    if (result is ValidationResult.Error) result.message else null
                }
                ValidationType.EMAIL -> {
                    val result = ProfileFieldValidators.validateEmail(filteredValue)
                    if (result is ValidationResult.Error) result.message else null
                }
                ValidationType.ACCOUNT_NUMBER -> {
                    val result = ProfileFieldValidators.validateAccountNumber(filteredValue)
                    if (result is ValidationResult.Error) result.message else null
                }
                else -> null
            }
    }

    // One field, one ledger row: tracked-caps label above the value, all
    // values on a shared left edge. Edit mode swaps the value line for the
    // app's standard outlined field.
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.1.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(modifier = Modifier.height(6.dp))

        if (isEditing && isEditable && !showAsLabelOnly) {
            val keyboardOptions =
                androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType =
                        when (validationType) {
                            ValidationType.AADHAAR, ValidationType.PHONE, ValidationType.ACCOUNT_NUMBER -> KeyboardType.Number
                            ValidationType.EMAIL -> KeyboardType.Email
                            else -> KeyboardType.Text
                        },
                    capitalization =
                        when (validationType) {
                            ValidationType.PAN, ValidationType.IFSC, ValidationType.GST -> KeyboardCapitalization.Characters
                            else -> KeyboardCapitalization.Words
                        },
                    imeAction = ImeAction.Next,
                )

            val visualTransformation =
                when (validationType) {
                    ValidationType.AADHAAR ->
                        VisualTransformation { text ->
                            val transformed = ProfileFieldValidators.formatAadhaar(text.text)
                            TransformedText(
                                AnnotatedString(transformed),
                                object : OffsetMapping {
                                    override fun originalToTransformed(offset: Int): Int {
                                        if (offset <= 4) return offset
                                        if (offset <= 8) return offset + 1
                                        return offset + 2
                                    }

                                    override fun transformedToOriginal(offset: Int): Int {
                                        if (offset <= 4) return offset
                                        if (offset <= 9) return offset - 1
                                        return offset - 2
                                    }
                                },
                            )
                        }
                    ValidationType.PHONE ->
                        VisualTransformation { text ->
                            val transformed = ProfileFieldValidators.formatPhone(text.text)
                            TransformedText(
                                AnnotatedString(transformed),
                                object : OffsetMapping {
                                    override fun originalToTransformed(offset: Int): Int {
                                        if (offset <= 5) return offset
                                        return offset + 1
                                    }

                                    override fun transformedToOriginal(offset: Int): Int {
                                        if (offset <= 5) return offset
                                        return offset - 1
                                    }
                                },
                            )
                        }
                    else -> VisualTransformation.None
                }

            var isFocused by remember { mutableStateOf(false) }
            OutlinedTextField(
                value = value,
                onValueChange = handleValueChange,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .testTag("owner_field_${label.lowercase().replace(' ', '_')}")
                        .semantics { testTagsAsResourceId = true }
                        .onFocusChanged { state ->
                            if (isFocused && !state.isFocused) {
                                onFocusLost?.invoke()
                            }
                            isFocused = state.isFocused
                        },
                textStyle = MaterialTheme.typography.bodyLarge,
                singleLine = true,
                shape = MaterialTheme.shapes.medium,
                isError = validationError != null,
                keyboardOptions = keyboardOptions,
                visualTransformation = visualTransformation,
                supportingText = {
                    val errorText = validationError ?: duplicateError
                    if (errorText != null) {
                        Text(
                            text = errorText,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                },
                placeholder =
                    when (validationType) {
                        ValidationType.AADHAAR -> {
                            { Text("1234 5678 9012") }
                        }
                        ValidationType.PAN -> {
                            { Text("ABCDE1234F") }
                        }
                        ValidationType.GST -> {
                            { Text("27ABCDE1234F1Z5") }
                        }
                        ValidationType.IFSC -> {
                            { Text("HDFC0001234") }
                        }
                        ValidationType.UPI -> {
                            { Text("name@okhdfcbank") }
                        }
                        ValidationType.EMAIL -> {
                            { Text("email@example.com") }
                        }
                        else -> null
                    },
                colors =
                    OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (validationError != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = if (validationError != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outlineVariant,
                        errorBorderColor = MaterialTheme.colorScheme.error,
                        errorLabelColor = MaterialTheme.colorScheme.error,
                        errorSupportingTextColor = MaterialTheme.colorScheme.error,
                    ),
            )
        } else {
            val displayValue =
                if (isSensitive && value.isNotEmpty()) {
                    "•••• •••• •••• ${value.takeLast(4)}"
                } else {
                    value.ifEmpty { "Not added" }
                }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = displayValue,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = if (value.isEmpty()) MaterialTheme.colorScheme.outlineVariant else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                )
                // The only row-level affordance worth keeping: tap-to-dial.
                if (onCallClick != null && value.isNotBlank()) {
                    IconButton(
                        onClick = onCallClick,
                        modifier =
                            Modifier
                                .size(36.dp)
                                .testTag("owner_call_${label.lowercase().replace(' ', '_')}")
                                .semantics { testTagsAsResourceId = true },
                    ) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = "Call $label",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DocumentList(
    documents: List<ProfileDocument>,
    documentStates: Map<String, DocumentUploadState>,
    isEditing: Boolean,
    onDocumentView: (String) -> Unit,
    onDocumentUpload: (DocumentType) -> Unit,
) {
    val types = ProfileDocumentTypeIds.editableDocumentTypes

    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        types.forEachIndexed { index, type ->
            val fieldKey = ProfileDocumentTypeIds.fieldKey(type)
            val uploadState = documentStates[fieldKey]
            val doc = documents.find { it.type == type }
            val existingUrl = uploadState?.existingFileUrl ?: doc?.url

            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (isEditing) {
                                onDocumentUpload(type)
                            } else if (!existingUrl.isNullOrEmpty() && !doc?.docId.isNullOrBlank()) {
                                onDocumentView(doc!!.docId!!)
                            }
                        }
                        .padding(horizontal = 20.dp, vertical = 18.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = getDocIcon(type),
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
                            androidx.compose.material3.LinearProgressIndicator(
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
                                ExistingStatusBadge(uploadState?.existingVerifyStatus, doc?.status)
                                Spacer(modifier = Modifier.width(8.dp))
                            } else {
                                ProfileStamp(
                                    text = "NOT UPLOADED",
                                    container = MaterialTheme.colorScheme.surfaceContainerHighest,
                                    content = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                        }
                    }

                    IconButton(
                        onClick = { onDocumentUpload(type) },
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
                                    MaterialTheme.colorScheme.primary
                                },
                            modifier = Modifier.size(24.dp),
                        )
                    }
                } else if (!existingUrl.isNullOrEmpty()) {
                    ExistingStatusBadge(uploadState?.existingVerifyStatus, doc?.status)
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "View",
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(20.dp),
                    )
                } else {
                    ProfileStamp(
                        text = "NOT UPLOADED",
                        container = MaterialTheme.colorScheme.surfaceContainerHighest,
                        content = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
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
    fallbackStatus: DocumentStatus?,
) {
    val scheme = MaterialTheme.colorScheme
    val ext = FreightThemeTokens.colors
    val normalizedStatus = existingVerifyStatus?.lowercase()?.trim()
    val (text, container, content) =
        when {
            normalizedStatus == "verified" -> Triple("VERIFIED", ext.surfaceSuccess, ext.onSurfaceSuccess)
            normalizedStatus == "rejected" -> Triple("REJECTED", scheme.errorContainer, scheme.onErrorContainer)
            normalizedStatus == "pending" -> Triple("VERIFICATION PENDING", ext.surfaceWarning, ext.onSurfaceWarning)
            fallbackStatus == DocumentStatus.VERIFIED -> Triple("VERIFIED", ext.surfaceSuccess, ext.onSurfaceSuccess)
            fallbackStatus == DocumentStatus.REJECTED -> Triple("REJECTED", scheme.errorContainer, scheme.onErrorContainer)
            fallbackStatus == DocumentStatus.UPLOADED -> Triple("VERIFICATION PENDING", ext.surfaceWarning, ext.onSurfaceWarning)
            fallbackStatus == DocumentStatus.PENDING -> Triple("PENDING", ext.surfaceWarning, ext.onSurfaceWarning)
            else -> Triple("N/A", scheme.surfaceContainerHighest, scheme.onSurfaceVariant)
        }

    ProfileStamp(text = text, container = container, content = content)
}

@Composable
private fun PendingSaveBadge() {
    ProfileStamp(
        text = "PENDING SAVE",
        container = MaterialTheme.colorScheme.onSurfaceVariant,
        content = MaterialTheme.colorScheme.onSurfaceVariant,
        bordered = true,
    )
}

@Composable
private fun UploadFailedBadge() {
    ProfileStamp(
        text = "UPLOAD FAILED",
        container = MaterialTheme.colorScheme.errorContainer,
        content = MaterialTheme.colorScheme.onErrorContainer,
    )
}

@Composable
private fun EditActions(
    hasChanges: Boolean,
    isSaving: Boolean,
    onSave: () -> Unit,
    onCancel: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp,
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier =
                    Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("owner_cancel_button")
                        .semantics { testTagsAsResourceId = true },
                shape = RoundedCornerShape(25.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                enabled = !isSaving,
            ) {
                Text("Cancel", fontWeight = FontWeight.SemiBold)
            }
            Button(
                onClick = onSave,
                enabled = hasChanges && !isSaving,
                modifier =
                    Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("owner_save_button")
                        .semantics { testTagsAsResourceId = true },
                shape = RoundedCornerShape(25.dp),
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                } else {
                    Text("Save Changes", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun ProfileErrorState(
    message: String,
    onRetry: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(Icons.Default.ErrorOutline, null, Modifier.size(48.dp), tint = MaterialTheme.colorScheme.error)
        Spacer(Modifier.height(16.dp))
        Text(
            message,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground,
        )
        TextButton(onClick = onRetry, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary)) {
            Text("Retry", fontWeight = FontWeight.Bold)
        }
    }
}

private fun getDocIcon(type: DocumentType) =
    when (type) {
        DocumentType.PAN -> Icons.Default.CreditCard
        DocumentType.AADHAAR -> Icons.Default.Fingerprint
        DocumentType.GST_CERTIFICATE -> Icons.Default.Description
        DocumentType.COMPANY_REGISTRATION -> Icons.Default.Business
        DocumentType.TDS_CERTIFICATE -> Icons.Default.Badge
        DocumentType.CANCELLED_CHEQUE -> Icons.Default.AccountBalance
        else -> Icons.Default.Description
    }

private fun launchDialer(
    context: Context,
    rawPhone: String,
) {
    val phone = rawPhone.trim()
    if (phone.isBlank()) return
    val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${Uri.encode(phone)}"))
    context.startActivity(dialIntent)
}

private fun openAppSettings(context: Context) {
    val intent =
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
        }
    context.startActivity(intent)
}

private fun createTempImageUri(context: Context): Uri {
    val tempFile = File(context.cacheDir, "camera_capture_${System.currentTimeMillis()}.jpg")
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        tempFile,
    )
}
