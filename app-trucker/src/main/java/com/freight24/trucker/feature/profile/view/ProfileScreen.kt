package com.freight24.trucker.feature.profile.view

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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
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
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SettingsApplications
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.material3.SheetState
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.freight24.trucker.R
import com.freight24.trucker.feature.profile.domain.DocumentStatus
import com.freight24.trucker.feature.profile.domain.DocumentType
import com.freight24.trucker.feature.profile.domain.Profile
import com.freight24.trucker.feature.profile.domain.ProfileDocument
import com.freight24.trucker.feature.profile.util.ProfileFieldValidators
import com.freight24.trucker.feature.profile.util.ValidationResult
import com.freight24.trucker.ui.components.rememberCallAction
import com.freight24.ui.UiState
import com.freight24.ui.components.AddRouteBottomSheet
import com.freight24.ui.components.CitySearchField
import com.freight24.ui.components.PODAddressCard
import com.freight24.ui.components.ProfileUtilityRow
import com.freight24.ui.components.RouteChip
import com.freight24.ui.theme.FreightThemeTokens
import kotlinx.coroutines.launch
import java.io.File

// Define theme-aware colors or use MaterialTheme directly

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
    paddingValues: PaddingValues,
    viewModel: ProfileViewModel = hiltViewModel(),
    onSettingsClick: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val isEditing by viewModel.isEditing.collectAsState()
    val hasChanges by viewModel.hasChanges.collectAsState()
    val panDuplicateError by viewModel.panDuplicateError.collectAsState()
    val gstDuplicateError by viewModel.gstDuplicateError.collectAsState()
    val showAddRouteSheet by viewModel.showAddRouteSheet.collectAsState()
    val showLogoutSheet by viewModel.showLogoutSheet.collectAsState()
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
            viewModel.uploadEvent.collect { result ->
                result.onSuccess {
                    snackbarHostState.showSnackbar("Document uploaded successfully")
                }.onFailure { error ->
                    snackbarHostState.showSnackbar(error.message ?: "Upload failed")
                }
            }
        }
        launch {
            viewModel.saveEvent.collect { result ->
                result.onSuccess {
                    snackbarHostState.showSnackbar("Profile saved successfully")
                }.onFailure { error ->
                    snackbarHostState.showSnackbar(error.message ?: "Save failed")
                }
            }
        }
    }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = { viewModel.onRefresh() },
        modifier =
            Modifier
                .fillMaxSize()
                .padding(paddingValues),
    ) {
        androidx.compose.material3.Scaffold(
            snackbarHost = { androidx.compose.material3.SnackbarHost(hostState = snackbarHostState) },
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
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
                            onSettingsClick = onSettingsClick,
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

                if (showLogoutSheet) {
                    LogoutConfirmationSheet(
                        onConfirm = {
                            viewModel.dismissLogoutSheet()
                            viewModel.logout()
                        },
                        onDismiss = { viewModel.dismissLogoutSheet() },
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
    onSettingsClick: () -> Unit = {},
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
    val profileStatus = profile.profileVerificationStatus?.lowercase() ?: "incomplete"
    val makeCall = rememberCallAction(stringResource(R.string.supportPhoneNumber))

    var pickingDocumentType by rememberSaveable { mutableStateOf<DocumentType?>(null) }
    var showSourcePicker by rememberSaveable { mutableStateOf(false) }
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }
    var pickingUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var isPdfPending by rememberSaveable { mutableStateOf(false) }
    var showCancelConfirmDialog by rememberSaveable { mutableStateOf(false) }

    val sourceSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val selectedUploadStatus = pickingDocumentType
        ?.let { type -> documentStates[com.freight24.trucker.feature.profile.util.ProfileDocumentTypeIds.fieldKey(type)]?.uploadStatus }
        ?: UploadStatus.IDLE

    val documentPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri != null) {
            pickingUris = listOf(uri)
            isPdfPending = true
        } else {
            pickingDocumentType = null
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(),
    ) { uris ->
        if (uris.isNotEmpty()) {
            pickingUris = uris
            isPdfPending = false
        } else {
            pickingDocumentType = null
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
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
                    .padding(horizontal = 20.dp, vertical = 20.dp),
        ) {
            ProfileHeaderCard(
                fullName = viewModel.editableFullName.collectAsState().value,
                partnerId = profile.tagCode.orEmpty(),
                initials = viewModel.getUserInitials(),
                role = "Truck Owner",
                verificationStatus = profileStatus,
                status = profile.status.orEmpty(),
            )

            Spacer(modifier = Modifier.height(32.dp))

            ProfileSection(title = "PERSONAL INFORMATION", icon = Icons.Default.Person, false) {
                ProfileField(
                    label = "FULL NAME",
                    value = viewModel.editableFullName.collectAsState().value,
                    isEditing = isEditing,
                    isEditable = true,
                    leadingIcon = Icons.Default.Person,
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
                    leadingIcon = Icons.Default.Badge,
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
                    isEditable = false,
                    showAsLabelOnly = true,
                    leadingIcon = Icons.Default.Call,
                    onLeadingIconClick = {
                        launchDialer(context, viewModel.editablePhoneNo.value)
                    },
                    onValueChange = { },
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), thickness = 1.dp)
                ProfileField(
                    label = "ALTERNATIVE PHONE",
                    value = viewModel.editableAlternativePhone.collectAsState().value,
                    isEditing = isEditing,
                    isEditable = true,
                    validationType = ValidationType.PHONE,
                    leadingIcon = Icons.Default.Call,
                    onLeadingIconClick = {
                        launchDialer(context, viewModel.editableAlternativePhone.value)
                    },
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
                    leadingIcon = Icons.Default.Fingerprint,
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
                    leadingIcon = Icons.Default.Badge,
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
            ) {
                ProfileField(
                    label = "COMPANY NAME",
                    value = viewModel.editableBusinessName.collectAsState().value,
                    isEditing = isEditing,
                    isEditable = !isVerified,
                    leadingIcon = Icons.Default.Business,
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
                    leadingIcon = Icons.Default.Badge,
                    onValueChange = {
                        viewModel.editableGstNumber.value = it
                        viewModel.onFieldChanged()
                    },
                    onFocusLost = viewModel::onGstFocusLost,
                    duplicateError = gstDuplicateError,
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), thickness = 1.dp)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Base location",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "BASE LOCATION",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp,
                        )
                    }
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
                            color = if (!locationLabel.isNullOrBlank()) {
                                MaterialTheme.colorScheme.onSurface
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), thickness = 1.dp)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Assignment,
                            contentDescription = "Preferred routes",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "PREFERRED ROUTES",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp,
                        )
                    }
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
                                onDelete = if (isEditing) {
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
                                colors = AssistChipDefaults.assistChipColors(
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
            ) {
                ProfileField(
                    label = "ACCOUNT HOLDER",
                    value = viewModel.editableAccountHolderName.collectAsState().value,
                    isEditing = isEditing,
                    isEditable = !isVerified,
                    leadingIcon = Icons.Default.Person,
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
                    leadingIcon = Icons.Default.CreditCard,
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
                    leadingIcon = Icons.Default.Badge,
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
                    leadingIcon = Icons.Default.Badge,
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
                    leadingIcon = Icons.Default.AccountBalance,
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
                    leadingIcon = Icons.AutoMirrored.Filled.Assignment,
                    onValueChange = {
                        viewModel.editableBranchName.value = it
                        viewModel.onFieldChanged()
                    },
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            ProfileSection(
                title = "DOCUMENTS",
                icon = Icons.AutoMirrored.Filled.Assignment,
                initiallyExpanded = false,
                isVerified = profile.documentsVerificationStatus,
            ) {
                DocumentList(
                    documents = profile.documents,
                    documentStates = documentStates,
                    isEditing = isEditing,
                    onDocumentView = { s3Url ->
                        viewModel.getSignedUrl(s3Url) { signedUrl ->
                            context.startActivity(com.freight24.ui.components.getPdfLaunchIntent(signedUrl, context))
                        }
                    },
                    onDocumentUpload = { type ->
                        pickingDocumentType = type
                        showSourcePicker = true
                    },
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            PODAddressCard(
                companyName = "Freight24",
                addressLines = listOf(
                    "4th Floor, #3, Srirampur Cross Road,",
                    "Jakkuru, Bengaluru,",
                    "Karnataka - 560064",
                ),
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .padding(vertical = 8.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.SettingsApplications,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "APP",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.weight(1f),
                )

                Spacer(modifier = Modifier.width(8.dp))
            }
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
            ) {
                ProfileUtilityRow(
                    title = "App Settings",
                    icon = Icons.Default.Settings,
                    onClick = onSettingsClick,
                    iconContainerColor = FreightThemeTokens.colors.settings.copy(alpha = 0.12f),
                    iconTint = FreightThemeTokens.colors.settings,
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), thickness = 1.dp)
                ProfileUtilityRow(
                    title = "Contact Support",
                    icon = Icons.Default.ChatBubble,
                    onClick = { makeCall() },
//                    subtitle = "Available Mon–Sat, 9am–6pm",
                    iconContainerColor = FreightThemeTokens.colors.info.copy(alpha = 0.12f),
                    iconTint = FreightThemeTokens.colors.info,
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            LogoutButton(onLogout = { viewModel.requestLogout() })

            Spacer(modifier = Modifier.height(60.dp))
        }

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
    val isVerified = verificationStatus == "verified"
    val verificationColor = when (verificationStatus) {
        "verified" -> MaterialTheme.colorScheme.primary
        "pending" -> FreightThemeTokens.colors.warning
        else -> MaterialTheme.colorScheme.error
    }
    val statusIcon = if (isVerified) Icons.Default.CheckCircle else Icons.Default.Warning

    val normalizedStatus = status.lowercase()
    val isActive = normalizedStatus == "active"
    val chipColor = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Avatar with Initials
            Box(contentAlignment = Alignment.BottomEnd) {
                Box(
                    modifier =
                        Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = initials,
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Icon(
                    imageVector = statusIcon,
                    contentDescription = verificationStatus,
                    tint = verificationColor,
                    modifier =
                        Modifier
                            .size(22.dp)
                            .background(MaterialTheme.colorScheme.surface, CircleShape)
                            .padding(1.dp),
                )
            }

            Spacer(modifier = Modifier.width(20.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = fullName.ifEmpty { "Member" },
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = role,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Partner ID: $partnerId",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.outline,
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

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
    isVerified: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    var isExpanded by rememberSaveable { mutableStateOf(initiallyExpanded) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .clickable { isExpanded = !isExpanded }
                .padding(vertical = 8.dp),
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
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                modifier = Modifier.weight(1f)
            )
            
            if (isVerified) {
                VerificationChip("Verified", MaterialTheme.colorScheme.primary)
            } else {
                VerificationChip("Unverified", FreightThemeTokens.colors.warning)
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = if (isExpanded) "Collapse" else "Expand",
                tint = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 8.dp),
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
    leadingIcon: ImageVector? = null,
    onLeadingIconClick: (() -> Unit)? = null,
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
        val filteredValue = when (validationType) {
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
        validationError = when (validationType) {
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

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
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
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp,
            )
            Spacer(modifier = Modifier.height(6.dp))

        if ((isEditing && isEditable) || showAsLabelOnly) {
            if (showAsLabelOnly) {
                // Display as read-only label (for phone number)
                Text(
                    text = value.ifEmpty { "Not added" },
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = if (value.isEmpty()) MaterialTheme.colorScheme.outlineVariant else MaterialTheme.colorScheme.onSurface,
                )
            } else {
                val keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = when (validationType) {
                        ValidationType.AADHAAR, ValidationType.PHONE, ValidationType.ACCOUNT_NUMBER -> KeyboardType.Number
                        ValidationType.EMAIL -> KeyboardType.Email
                        else -> KeyboardType.Text
                    },
                    capitalization = when (validationType) {
                        ValidationType.PAN, ValidationType.IFSC, ValidationType.GST -> KeyboardCapitalization.Characters
                        else -> KeyboardCapitalization.Words
                    },
                    imeAction = ImeAction.Next
                )

                var isFocused by remember { mutableStateOf(false) }

                val visualTransformation = when (validationType) {
                    ValidationType.AADHAAR -> VisualTransformation { text ->
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
                            }
                        )
                    }
                    ValidationType.PHONE -> VisualTransformation { text ->
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
                            }
                        )
                    }
                    else -> VisualTransformation.None
                }

                OutlinedTextField(
                    value = value,
                    onValueChange = handleValueChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { state ->
                            if (isFocused && !state.isFocused) {
                                onFocusLost?.invoke()
                            }
                            isFocused = state.isFocused
                        },
                    textStyle = MaterialTheme.typography.bodyLarge,
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    isError = validationError != null,
                    keyboardOptions = keyboardOptions,
                    visualTransformation = visualTransformation,
                    supportingText = {
                        val errorText = validationError ?: duplicateError
                        if (errorText != null) {
                            Text(
                                text = errorText,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    },
                    placeholder = when (validationType) {
                        ValidationType.AADHAAR -> { { Text("1234 5678 9012") } }
                        ValidationType.PAN -> { { Text("ABCDE1234F") } }
                        ValidationType.GST -> { { Text("27ABCDE1234F1Z5") } }
                        ValidationType.IFSC -> { { Text("HDFC0001234") } }
                        ValidationType.UPI -> { { Text("name@okhdfcbank") } }
                        ValidationType.EMAIL -> { { Text("email@example.com") } }
                        else -> null
                    },
                    colors =
                        OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = if (validationError != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = if (validationError != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outlineVariant,
                            errorBorderColor = MaterialTheme.colorScheme.error,
                            errorLabelColor = MaterialTheme.colorScheme.error,
                            errorSupportingTextColor = MaterialTheme.colorScheme.error
                        ),
                )
            }
        } else {
            val displayValue =
                if (isSensitive && value.isNotEmpty()) {
                    "•••• •••• •••• ${value.takeLast(
                        4,
                    )}"
                } else {
                    value.ifEmpty { "Not added" }
                }
            Text(
                text = displayValue,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = if (value.isEmpty()) MaterialTheme.colorScheme.outlineVariant else MaterialTheme.colorScheme.onSurface,
            )
        }
        }
    }
}

@Composable
private fun VerificationChip(text: String, color: Color) {
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = MaterialTheme.shapes.small,
    ) {
        Text(
            text = text,
            color = color,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
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
    val types = com.freight24.trucker.feature.profile.util.ProfileDocumentTypeIds.editableDocumentTypes

    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        types.forEachIndexed { index, type ->
            val fieldKey = com.freight24.trucker.feature.profile.util.ProfileDocumentTypeIds.fieldKey(type)
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
                            } else if (!existingUrl.isNullOrEmpty()) {
                                onDocumentView(existingUrl)
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
                                VerificationChip("Not Uploaded", MaterialTheme.colorScheme.outline)
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                        }
                    }

                    IconButton(
                        onClick = { onDocumentUpload(type) },
                        enabled = (uploadState?.uploadStatus ?: UploadStatus.IDLE) != UploadStatus.UPLOADING,
                    ) {
                        Icon(
                            imageVector = if ((uploadState?.uploadStatus ?: UploadStatus.IDLE) == UploadStatus.ERROR) {
                                Icons.Default.Refresh
                            } else {
                                Icons.Default.CloudUpload
                            },
                            contentDescription = "Upload",
                            tint = if ((uploadState?.uploadStatus ?: UploadStatus.IDLE) == UploadStatus.ERROR) {
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
private fun ExistingStatusBadge(existingVerifyStatus: String?, fallbackStatus: DocumentStatus?) {
    val normalizedStatus = existingVerifyStatus?.lowercase()?.trim()
    val (text, color) = when {
        normalizedStatus == "verified" -> "Verified" to MaterialTheme.colorScheme.primary
        normalizedStatus == "rejected" -> "Rejected" to MaterialTheme.colorScheme.error
        normalizedStatus == "pending" -> "Verification Pending" to MaterialTheme.colorScheme.tertiary
        fallbackStatus == DocumentStatus.VERIFIED -> "Verified" to MaterialTheme.colorScheme.primary
        fallbackStatus == DocumentStatus.REJECTED -> "Rejected" to MaterialTheme.colorScheme.error
        fallbackStatus == DocumentStatus.UPLOADED -> "Verification Pending" to MaterialTheme.colorScheme.tertiary
        fallbackStatus == DocumentStatus.PENDING -> "Pending" to MaterialTheme.colorScheme.tertiary
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
        color = Color.Transparent,
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




@Composable
private fun LogoutButton(onLogout: () -> Unit) {
    Button(
        onClick = onLogout,
        modifier =
            Modifier
                .fillMaxWidth()
                .height(56.dp)
                .semantics(mergeDescendants = true) { contentDescription = "profile_logout" },
        shape = RoundedCornerShape(28.dp),
        colors =
            ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.error,
            ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
    ) {
        Icon(imageVector = Icons.AutoMirrored.Filled.Logout, contentDescription = null, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = "LOGOUT", fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LogoutConfirmationSheet(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Icon badge
            Box(
                modifier =
                    Modifier
                        .size(56.dp)
                        .background(
                            color = MaterialTheme.colorScheme.error.copy(alpha = 0.18f),
                            shape = CircleShape,
                        ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Logout,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(28.dp),
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Logging out?",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Are you sure you want to log out of your account? You'll need to sign back in to access your loads.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Primary action: Logout
            Button(
                onClick = onConfirm,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .semantics(mergeDescendants = true) { contentDescription = "logout_confirm" },
                shape = RoundedCornerShape(28.dp),
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
            ) {
                Text(text = "Logout", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Secondary action: Stay Logged In
            OutlinedButton(
                onClick = onDismiss,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .semantics(mergeDescendants = true) { contentDescription = "logout_cancel" },
                shape = RoundedCornerShape(28.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            ) {
                Text(
                    text = "Stay Logged In",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun EditActions(
    hasChanges: Boolean,
    isSaving: Boolean,
    onSave: () -> Unit,
    onCancel: () -> Unit,
) {
    Surface(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
        tonalElevation = 8.dp,
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 12.dp,
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedButton(
                onClick = onCancel,
                enabled = !isSaving,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(width = 1.dp),
            ) {
                Text("Cancel", fontWeight = FontWeight.Bold)
            }
            Button(
                onClick = onSave,
                enabled = hasChanges && !isSaving,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            ) {
                Text("Save Changes", fontWeight = FontWeight.Bold)
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

private fun launchDialer(context: Context, rawPhone: String) {
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
