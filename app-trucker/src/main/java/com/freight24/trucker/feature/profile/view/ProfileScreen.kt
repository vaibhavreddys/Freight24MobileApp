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
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.SnackbarDuration
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.freight24.common.util.DocumentUploadPolicy
import com.freight24.trucker.BuildConfig
import com.freight24.trucker.R
import com.freight24.trucker.feature.profile.domain.DocumentType
import com.freight24.trucker.feature.profile.domain.Profile
import com.freight24.trucker.feature.profile.util.ProfileDocumentTypeIds
import com.freight24.trucker.ui.components.rememberCallAction
import com.freight24.ui.UiState
import com.freight24.ui.components.AddRouteBottomSheet
import com.freight24.ui.components.CitySearchField
import com.freight24.ui.components.ProfileUtilityRow
import com.freight24.ui.components.RouteChip
import com.freight24.ui.components.formViewportInsets
import com.freight24.ui.components.getPdfLaunchIntent
import com.freight24.ui.theme.FreightThemeTokens
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

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
                    snackbarHostState.showSnackbar("Document staged. Save changes to finish.")
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
                // Keyboard-aware viewport; see FormInsets.kt.
                .formViewportInsets(paddingValues),
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
    val documentTypes by viewModel.documentTypes.collectAsState()
    val previewingDocumentId by viewModel.previewingDocumentId.collectAsState()
    val completion by viewModel.completion.collectAsState()
    val hasPendingTemporaryUploads by viewModel.hasPendingTemporaryUploads.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()
    val bankValidationErrors by viewModel.bankValidationErrors.collectAsState()
    val isIfscLookupInProgress by viewModel.isIfscLookupInProgress.collectAsState()
    val changeCount by viewModel.changeCount.collectAsState()
    val baseCity by viewModel.baseCity.collectAsState()
    val baseCityQuery by viewModel.baseCityQuery.collectAsState()
    val baseCitySuggestions by viewModel.baseCitySuggestions.collectAsState()
    val baseCityLoading by viewModel.baseCitySearchLoading.collectAsState()
    val baseLocation by viewModel.baseLocation.collectAsState()
    val visibleRoutes by viewModel.visibleRoutes.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val documentPreviewLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        viewModel.onDocumentPreviewClosed()
    }

    val isVerified = profile.profileVerificationStatus?.equals("verified", ignoreCase = true) == true
    val makeCall = rememberCallAction(stringResource(R.string.supportPhoneNumber))

    var pickingDocumentType by rememberSaveable { mutableStateOf<DocumentType?>(null) }
    var showSourcePicker by rememberSaveable { mutableStateOf(false) }
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }
    var pickingUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var isPdfPending by rememberSaveable { mutableStateOf(false) }
    var uploadRequested by remember { mutableStateOf(false) }
    var showCancelConfirmDialog by rememberSaveable { mutableStateOf(false) }

    val sourceSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val selectedUploadStatus =
        pickingDocumentType
            ?.let { type -> documentStates[com.freight24.trucker.feature.profile.util.ProfileDocumentTypeIds.fieldKey(type)]?.uploadStatus }
            ?: UploadStatus.IDLE

    val isPreparingDocument = pickingDocumentType
        ?.let { type -> documentStates[com.freight24.trucker.feature.profile.util.ProfileDocumentTypeIds.fieldKey(type)]?.isPreparingDocument }
        ?: false
    LaunchedEffect(selectedUploadStatus, uploadRequested) {
        if (uploadRequested && (selectedUploadStatus == UploadStatus.DONE || selectedUploadStatus == UploadStatus.ERROR)) {
            pickingUris = emptyList()
            pickingDocumentType = null
            uploadRequested = false
        }
    }

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
            progressText = stringResource(
                if (isPreparingDocument) com.freight24.ui.R.string.upload_preparing else com.freight24.ui.R.string.upload_in_progress,
            ),
            uploadButtonText = "Confirm Upload",
            onUploadClick = {
                pickingDocumentType?.let { type ->
                    uploadRequested = true
                    viewModel.uploadDocument(pickingUris, type, isPdf = isPdfPending)
                }
            },
            onDismiss = {
                pickingUris = emptyList()
                pickingDocumentType = null
            },
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
                    .padding(top = 16.dp, bottom = 24.dp),
        ) {
            ProfileHeaderCard(
                fullName = viewModel.editableFullName.collectAsState().value,
                partnerId = profile.tagCode.orEmpty(),
                initials = viewModel.getUserInitials(),
                role = "Truck Owner",
                isVerified = isVerified,
                isActive = profile.status.equals("active", ignoreCase = true),
            )

            Spacer(modifier = Modifier.height(12.dp))

            ProfileCompletionMeter(completion)

            Spacer(modifier = Modifier.height(12.dp))

            ProfileSection(
                title = "Personal information",
                icon = Icons.Default.Person,
                badge = if (isVerified) SectionBadge.VERIFIED else SectionBadge.UNVERIFIED,
                expandForEditing = isEditing,
                accent = ProfileSectionAccent.PERSONAL,
            ) {
                ProfileField(
                    label = "Full name",
                    value = viewModel.editableFullName.collectAsState().value,
                    isEditing = isEditing,
                    isEditable = true,
                    onValueChange = {
                        viewModel.editableFullName.value = it
                        viewModel.onFieldChanged()
                    },
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                ProfileField(
                    label = "Email address",
                    value = viewModel.editableEmailId.collectAsState().value,
                    isEditing = isEditing,
                    isEditable = !isVerified,
                    isLocked = isVerified,
                    lockedReason = "Verified",
                    validationType = ValidationType.EMAIL,
                    onValueChange = {
                        viewModel.editableEmailId.value = it
                        viewModel.onFieldChanged()
                    },
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                ProfileField(
                    label = "Phone number",
                    value = viewModel.editablePhoneNo.collectAsState().value,
                    isEditing = isEditing,
                    isEditable = false,
                    showAsLabelOnly = true,
                    isLocked = true,
                    lockedReason = "Primary",
                    onValueChange = { },
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                ProfileField(
                    label = "Alternative phone",
                    value = viewModel.editableAlternativePhone.collectAsState().value,
                    isEditing = isEditing,
                    isEditable = true,
                    validationType = ValidationType.PHONE,
                    onValueChange = {
                        viewModel.editableAlternativePhone.value = it
                        viewModel.onFieldChanged()
                    },
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                ProfileField(
                    label = "Aadhaar number",
                    value = viewModel.editableAadhaarNumber.collectAsState().value,
                    isEditing = isEditing,
                    isEditable = documentStates[ProfileDocumentTypeIds.fieldKey(DocumentType.AADHAAR)]?.existingVerifyStatus != "verified",
                    isLocked = documentStates[ProfileDocumentTypeIds.fieldKey(DocumentType.AADHAAR)]?.existingVerifyStatus == "verified",
                    lockedReason = "Verified",
                    validationType = ValidationType.AADHAAR,
                    documentTone = documentTone(DocumentType.AADHAAR, documentStates),
                    onValueChange = {
                        viewModel.editableAadhaarNumber.value = it
                        viewModel.onFieldChanged()
                    },
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                ProfileField(
                    label = "PAN number",
                    value = viewModel.editablePanNumber.collectAsState().value,
                    isEditing = isEditing,
                    isEditable = documentStates[ProfileDocumentTypeIds.fieldKey(DocumentType.PAN)]?.existingVerifyStatus != "verified",
                    isLocked = documentStates[ProfileDocumentTypeIds.fieldKey(DocumentType.PAN)]?.existingVerifyStatus == "verified",
                    lockedReason = "Verified",
                    validationType = ValidationType.PAN,
                    documentTone = documentTone(DocumentType.PAN, documentStates),
                    onValueChange = {
                        viewModel.editablePanNumber.value = it.uppercase()
                        viewModel.onFieldChanged()
                    },
                    onFocusLost = viewModel::onPanFocusLost,
                    duplicateError = panDuplicateError,
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // No badge here on purpose. Business and Documents previously both
            // read `documentsVerificationStatus`, so "Documents: Verified" could
            // sit directly above two "Not uploaded" rows.
            ProfileSection(
                title = "Business details",
                icon = Icons.Default.Business,
                expandForEditing = isEditing,
                accent = ProfileSectionAccent.BUSINESS,
            ) {
                ProfileField(
                    label = "Company name",
                    value = viewModel.editableBusinessName.collectAsState().value,
                    isEditing = isEditing,
                    isEditable = !isVerified,
                    isLocked = isVerified,
                    lockedReason = "Verified",
                    onValueChange = {
                        viewModel.editableBusinessName.value = it
                        viewModel.onFieldChanged()
                    },
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), thickness = 1.dp)
                ProfileField(
                    label = "GST number",
                    value = viewModel.editableGstNumber.collectAsState().value,
                    isEditing = isEditing,
                    isEditable = !isVerified,
                    isLocked = isVerified,
                    lockedReason = "Verified",
                    validationType = ValidationType.GST,
                    documentTone = documentTone(DocumentType.GST_CERTIFICATE, documentStates),
                    onValueChange = {
                        viewModel.editableGstNumber.value = it
                        viewModel.onFieldChanged()
                    },
                    onFocusLost = viewModel::onGstFocusLost,
                    duplicateError = gstDuplicateError,
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                ProfileField(
                    label = "Base location",
                    value = baseLocation?.takeIf { it.isNotBlank() } ?: baseCity?.city.orEmpty(),
                    isEditing = isEditing,
                    isEditable = true,
                    onValueChange = { },
                    valueContent = {
                        // Read mode must fall back to the plain value. Returning
                        // nothing here left the row blank, because valueContent
                        // replaces the default value rendering entirely.
                        if (isEditing) {
                            CitySearchField(
                                query = baseCityQuery,
                                onQueryChange = viewModel::onBaseCityQueryChanged,
                                suggestions = baseCitySuggestions,
                                onCitySelected = viewModel::onBaseCitySelected,
                                isLoading = baseCityLoading,
                                label = "Base location",
                                selectAllOnFocus = true,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        } else {
                            Text(
                                text =
                                    baseLocation?.takeIf { it.isNotBlank() }
                                        ?: baseCity?.city
                                        ?: "Not added",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    },
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                ProfileField(
                    label = "Preferred routes",
                    value = "",
                    isEditing = false,
                    isEditable = false,
                    onValueChange = { },
                    valueContent = {
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
                                color = MaterialTheme.colorScheme.onSurface,
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
                                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                                            labelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                        ),
                                )
                            }
                        }
                    },
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            ProfileSection(
                title = "Banking",
                icon = Icons.Default.AccountBalance,
                badge = if (profile.bankAccountVerificationStatus) SectionBadge.VERIFIED else SectionBadge.UNVERIFIED,
                expandForEditing = isEditing,
                accent = ProfileSectionAccent.BANKING,
            ) {
                ProfileField(
                    label = "Account holder",
                    value = viewModel.editableAccountHolderName.collectAsState().value,
                    isEditing = isEditing,
                    isEditable = !profile.bankAccountVerificationStatus,
                    isLocked = profile.bankAccountVerificationStatus,
                    lockedReason = "Locked",
                    isRequired = isEditing && !profile.bankAccountVerificationStatus,
                    duplicateError = bankValidationErrors["accountHolder"],
                    onValueChange = {
                        viewModel.editableAccountHolderName.value = it
                        viewModel.onBankFieldChanged()
                    },
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                ProfileField(
                    label = "Account number",
                    value = viewModel.editableAccountNumber.collectAsState().value,
                    isEditing = isEditing,
                    isSensitive = true,
                    isEditable = !profile.bankAccountVerificationStatus,
                    isLocked = profile.bankAccountVerificationStatus,
                    lockedReason = "Locked",
                    validationType = ValidationType.ACCOUNT_NUMBER,
                    documentTone = documentTone(DocumentType.CANCELLED_CHEQUE, documentStates),
                    isRequired = isEditing && !profile.bankAccountVerificationStatus,
                    duplicateError = bankValidationErrors["accountNumber"],
                    onValueChange = {
                        viewModel.editableAccountNumber.value = it
                        viewModel.onBankFieldChanged()
                    },
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                ProfileField(
                    label = "IFSC code",
                    value = viewModel.editableIfscCode.collectAsState().value,
                    isEditing = isEditing,
                    isEditable = !profile.bankAccountVerificationStatus,
                    isLocked = profile.bankAccountVerificationStatus,
                    lockedReason = "Locked",
                    validationType = ValidationType.IFSC,
                    isRequired = isEditing && !profile.bankAccountVerificationStatus,
                    duplicateError = bankValidationErrors["ifsc"],
                    onValueChange = viewModel::onIfscChanged,
                    onFocusLost = viewModel::onIfscFocusLost,
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                ProfileField(
                    label = "UPI ID",
                    value = viewModel.editableUpiId.collectAsState().value,
                    isEditing = isEditing,
                    isEditable = !profile.bankAccountVerificationStatus,
                    isLocked = profile.bankAccountVerificationStatus,
                    lockedReason = "Locked",
                    validationType = ValidationType.UPI,
                    onValueChange = {
                        viewModel.editableUpiId.value = it.lowercase()
                        viewModel.onBankFieldChanged()
                    },
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                ProfileField(
                    label = "Bank name",
                    value = if (isIfscLookupInProgress) "Looking up bank…" else viewModel.editableBankName.collectAsState().value,
                    isEditing = false,
                    isEditable = false,
                    isRequired = isEditing && !profile.bankAccountVerificationStatus,
                    duplicateError = bankValidationErrors["bankName"],
                    onValueChange = { },
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                ProfileField(
                    label = "Branch name",
                    value = if (isIfscLookupInProgress) "Looking up branch…" else viewModel.editableBranchName.collectAsState().value,
                    isEditing = false,
                    isEditable = false,
                    isRequired = isEditing && !profile.bankAccountVerificationStatus,
                    duplicateError = bankValidationErrors["branchName"],
                    onValueChange = { },
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                val message = stringResource(R.string.profile_document_preview_failed)
                DocumentList(
                    documentTypes = listOf(DocumentType.CANCELLED_CHEQUE),
                    documents = profile.documents,
                    documentStates = documentStates,
                    isEditing = isEditing,
                    previewingDocumentId = previewingDocumentId,
                    mandatoryTypes = setOf(DocumentType.CANCELLED_CHEQUE),
                    lockedTypes = if (profile.bankAccountVerificationStatus) setOf(DocumentType.CANCELLED_CHEQUE) else emptySet(),
                    onDocumentView = { documentId ->
                        viewModel.getSignedUrl(
                            documentId = documentId,
                            onUrlReady = { signedUrl -> documentPreviewLauncher.launch(getPdfLaunchIntent(signedUrl, context)) },
                            onError = { scope.launch { snackbarHostState.showSnackbar(message) } },
                        )
                    },
                    onDocumentUpload = { type ->
                        pickingDocumentType = type
                        showSourcePicker = true
                    },
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            ProfileSection(
                title = "Documents",
                icon = Icons.AutoMirrored.Filled.Assignment,
                expandForEditing = isEditing,
                accent = ProfileSectionAccent.DOCUMENTS,
                // Derived from the document list itself. The old badge came from
                // `documentsVerificationStatus`, a single flag shared with
                // Business Details, so it could read "Verified" above rows that
                // said "Not uploaded".
                badgeText = documentsBadge(completion),
                // Outstanding documents take the warning tone. An earlier pass
                // used the primary-green tone here, which read as a positive
                // state — the opposite of what "2 of 6 missing" means.
                badgeTone =
                    if (completion.isComplete) StatusTone.VERIFIED else StatusTone.WARNING,
            ) {
                val message = stringResource(R.string.profile_document_preview_failed)
                DocumentList(
                    documentTypes = documentTypes.filterNot { it == DocumentType.CANCELLED_CHEQUE },
                    documents = profile.documents,
                    documentStates = documentStates,
                    isEditing = isEditing,
                    previewingDocumentId = previewingDocumentId,
                    mandatoryTypes = ProfileDocumentTypeIds.mandatoryIdentityDocumentTypes,
                    onDocumentView = { documentId ->
                        viewModel.getSignedUrl(
                            documentId = documentId,
                            onUrlReady = { signedUrl ->
                                documentPreviewLauncher.launch(
                                    getPdfLaunchIntent(signedUrl, context)
                                )
                            },
                            onError = {
                                scope.launch {
                                    snackbarHostState.showSnackbar(message)
                                }
                            }
                        )
                    },
                    onDocumentUpload = { type ->
                        pickingDocumentType = type
                        showSourcePicker = true
                    },
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            CourierAddressCard(
                companyName = "Freight24 Logistics Pvt Ltd",
                addressLines =
                    listOf(
                        "Adarsh Building,",
                        "#313, 1st Floor, 7th Cross road,",
                        "Dr.Shivaram Karanth Nagar",
                        "RK Hegde Nagar, Bengaluru",
                        "Karnataka - 560064",
                        "Phone: 9620111667"
                    ),
            )

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            ) {
                Column {
                    ProfileUtilityRow(
                        title = "Settings",
                        icon = Icons.Default.Settings,
                        onClick = onSettingsClick,
                        testTag = "profile_settings",
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    ProfileUtilityRow(
                        title = "Contact support",
                        // Phone, not chat: this dials the support line. It used a
                        // ChatBubble glyph, which promised a messenger.
                        icon = Icons.Default.Call,
                        subtitle = "Mon–Sat, 9am–6pm",
                        onClick = { makeCall() },
                        iconContainerColor = FreightThemeTokens.colors.surfaceInfo,
                        iconTint = FreightThemeTokens.colors.onSurfaceInfo,
                        testTag = "profile_support",
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            LogoutButton(onLogout = { viewModel.requestLogout() })

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Freight24 Partner · v${BuildConfig.VERSION_NAME}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        // Sibling of the scroll area, not an overlay. The previous version was
        // an absolutely-positioned translucent Surface, so rows scrolled visibly
        // underneath it and got clipped mid-chip.
        AnimatedVisibility(
            visible = isEditing,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut(),
        ) {
            EditActions(
                hasChanges = hasChanges,
                changeCount = changeCount,
                isSaving = isSaving,
                bankingValid = bankValidationErrors.isEmpty() && !isIfscLookupInProgress,
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

/**
 * Courier address card.
 *
 * Replaces a generic titled-card wrapper wrapping a second card component, which
 * produced a header, a subtitle and four address lines as one undifferentiated
 * run of text with a decorative green circle floating in it.
 *
 * Now it follows the same anatomy as every other card on this screen: icon tile
 * + title on top, hairline divider, body. The copy action is a standard trailing
 * icon button rather than a pill with a circle inside it.
 */
@Composable
private fun CourierAddressCard(
    companyName: String,
    addressLines: List<String>,
) {
    val clipboardManager = LocalClipboardManager.current
    var isCopied by remember { mutableStateOf(false) }

    val fullAddress =
        remember(companyName, addressLines) {
            "$companyName\n${addressLines.joinToString("\n")}"
        }

    LaunchedEffect(isCopied) {
        if (isCopied) {
            delay(2000)
            isCopied = false
        }
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 14.dp, end = 6.dp, top = 10.dp, bottom = 10.dp),
            ) {
                Box(
                    modifier =
                        Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(9.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(18.dp).decorative(),
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "POD address",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                )
                if (isCopied) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Address copied",
                        style = MaterialTheme.typography.labelSmall,
                        color = FreightThemeTokens.colors.onSurfaceSuccess,
                    )
                }
                IconButton(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(fullAddress))
                        isCopied = true
                    },
                    modifier =
                        Modifier
                            .size(40.dp)
                            .testTag("profile_copy_address")
                            .semantics(mergeDescendants = true) {
                                testTagsAsResourceId = true
                                contentDescription = "Copy courier address"
                            },
                ) {
                    Icon(
                        imageVector = if (isCopied) Icons.Default.Check else Icons.Default.ContentCopy,
                        contentDescription = null,
                        tint =
                            if (isCopied) {
                                FreightThemeTokens.colors.onSurfaceSuccess
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                        modifier = Modifier.size(18.dp),
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
                Text(
                    text = companyName,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.height(2.dp))
                addressLines.forEach { line ->
                    Text(
                        text = line,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

/**
 * Outlined rather than a filled error-container pill. It was previously the
 * loudest element on the page — a 56dp fully-saturated pink lozenge with
 * letter-spaced caps — for an action most users never take.
 */
@Composable
private fun LogoutButton(onLogout: () -> Unit) {
    OutlinedButton(
        onClick = onLogout,
        modifier =
            Modifier
                .fillMaxWidth()
                .height(48.dp)
                .semantics(mergeDescendants = true) { contentDescription = "profile_logout" },
        shape = RoundedCornerShape(24.dp),
        colors =
            ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.error,
            ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.errorContainer),
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.Logout,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = "Log out", fontWeight = FontWeight.SemiBold)
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

/**
 * Bottom action bar for edit mode.
 *
 * Docked as a sibling of the scroll area with an opaque surface and a hairline
 * top border, so nothing can slide underneath it. The change count is stated in
 * words because a silently-disabled Save button gives no reason.
 */
@Composable
private fun EditActions(
    hasChanges: Boolean,
    changeCount: Int,
    isSaving: Boolean,
    bankingValid: Boolean,
    onSave: () -> Unit,
    onCancel: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column {
            // Explains *why* Save is greyed out, and what it will do once it is
            // not. Previously this was a constant string, so it kept claiming
            // "No changes to save yet" while Save was already enabled.
            Text(
                text =
                    when {
                        isSaving -> "Saving your changes…"
                        !bankingValid -> "Complete the required banking fields to save"
                        changeCount > 0 ->
                            "$changeCount ${if (changeCount == 1) "change" else "changes"} to save"
                        else -> "No changes to save yet"
                    },
                style = MaterialTheme.typography.labelMedium,
                color =
                    if (changeCount > 0 && !isSaving) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 10.dp),
            )
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedButton(
                    onClick = onCancel,
                    enabled = !isSaving,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                ) {
                    Text("Discard", fontWeight = FontWeight.SemiBold)
                }
                Button(
                    onClick = onSave,
                    enabled = hasChanges && bankingValid && !isSaving,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                ) {
                    if (isSaving) {
                        androidx.compose.material3.CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Saving", fontWeight = FontWeight.SemiBold)
                    } else {
                        Text("Save changes", fontWeight = FontWeight.SemiBold)
                    }
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
