package com.freight24.trucker.feature.profile.view

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Business
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
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.freight24.trucker.feature.profile.domain.DocumentStatus
import com.freight24.trucker.feature.profile.domain.DocumentType
import com.freight24.trucker.feature.profile.domain.Profile
import com.freight24.trucker.feature.profile.domain.ProfileDocument
import com.freight24.ui.UiState
import com.freight24.ui.theme.PrimaryGreen

// Define theme-aware colors or use MaterialTheme directly
// For BrandGreen, we can keep it as a constant for branding, but text colors should adapt
private val BrandGreen = PrimaryGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ProfileScreen(
    paddingValues: PaddingValues,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val isEditing by viewModel.isEditing.collectAsState()
    val hasChanges by viewModel.hasChanges.collectAsState()

    val snackbarHostState = androidx.compose.runtime.remember { androidx.compose.material3.SnackbarHostState() }

    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.uploadEvent.collect { result ->
            result.onSuccess {
                snackbarHostState.showSnackbar("Document uploaded successfully")
            }.onFailure { error ->
                snackbarHostState.showSnackbar(error.message ?: "Upload failed")
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
            containerColor = Color.Transparent
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                when (val state = uiState) {
                    is UiState.Loading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(strokeWidth = 3.dp, color = BrandGreen)
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
                            hasChanges = hasChanges,
                        )
                    }
                    UiState.Idle -> {}
                }
            }
        }
    }

}

@Composable
private fun ProfileMainContent(
    profile: Profile,
    viewModel: ProfileViewModel,
    isEditing: Boolean,
    hasChanges: Boolean,
) {
    val isUploading by viewModel.isUploading.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current

    val isVerified = profile.profileVerificationStatus?.lowercase() == "verified"
    val profileStatus = profile.profileVerificationStatus?.lowercase() ?: "incomplete"
    
    var pickingDocumentType by rememberSaveable { mutableStateOf<DocumentType?>(null) }
    var pickingDocumentId by rememberSaveable { mutableStateOf<String?>(null) }
    var showSelectionDialog by rememberSaveable { mutableStateOf(false) }

    val documentPickerLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            pickingDocumentType?.let { type ->
                viewModel.uploadDocument(listOf(it), type, isPdf = true, docId = pickingDocumentId)
            }
        }
    }

    val imagePickerLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        if (uris.isNotEmpty()) {
            pickingDocumentType?.let { type ->
                viewModel.uploadDocument(uris, type, isPdf = false, docId = pickingDocumentId)
            }
        }
    }

    if (showSelectionDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showSelectionDialog = false },
            title = { Text("Select Document Source") },
            text = { Text("How would you like to upload your document?") },
            confirmButton = {
                TextButton(onClick = {
                    showSelectionDialog = false
                    documentPickerLauncher.launch("application/pdf")
                }) {
                    Text("PDF File")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showSelectionDialog = false
                    imagePickerLauncher.launch("image/*")
                }) {
                    Text("Images")
                }
            }
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
                isEditing = isEditing,
                onEditClick = { viewModel.toggleEditMode() },
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Information Sections
            ProfileSection(title = "PERSONAL INFORMATION", icon = Icons.Default.Person, true) {
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
                    isEditable = !isVerified,
                    onValueChange = {
                        viewModel.editablePhoneNo.value = it
                        viewModel.onFieldChanged()
                    },
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), thickness = 1.dp)
                ProfileField(
                    label = "ALTERNATIVE PHONE",
                    value = viewModel.editableAlternativePhone.collectAsState().value,
                    isEditing = isEditing,
                    isEditable = true,
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
                    onValueChange = {
                        viewModel.editableAadhaarNumber.value = it
                        viewModel.onFieldChanged()
                    },
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            ProfileSection(
                title = "BUSINESS DETAILS", 
                icon = Icons.Default.Business, 
                initiallyExpanded = false,
                isVerified = profile.documentsVerificationStatus
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
                    onValueChange = {
                        viewModel.editableGstNumber.value = it
                        viewModel.onFieldChanged()
                    },
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            ProfileSection(
                title = "BANKING INFORMATION", 
                icon = Icons.Default.AccountBalance, 
                initiallyExpanded = false,
                isVerified = profile.bankAccountVerificationStatus
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
                    onValueChange = {
                        viewModel.editableIfscCode.value = it
                        viewModel.onFieldChanged()
                    },
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), thickness = 1.dp)
                ProfileField(
                    label = "UPI ID",
                    value = viewModel.editableUpiId.collectAsState().value,
                    isEditing = isEditing,
                    isEditable = !isVerified,
                    onValueChange = {
                        viewModel.editableUpiId.value = it
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
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), thickness = 1.dp)
                ProfileField(
                    label = "PAN NUMBER",
                    value = viewModel.editablePanNumber.collectAsState().value,
                    isEditing = isEditing,
                    isEditable = !isVerified,
                    onValueChange = {
                        viewModel.editablePanNumber.value = it
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
                isVerified = profile.documentsVerificationStatus
            ) {
                DocumentList(
                    documents = profile.documents,
                    isEditing = isEditing,
                    onDocumentView = { s3Url ->
                        viewModel.getSignedUrl(s3Url) { signedUrl ->
                            context.startActivity(com.freight24.ui.components.getPdfLaunchIntent(signedUrl, context))
                        }
                    },
                    onDocumentUpload = { type, docId ->
                        pickingDocumentType = type
                        pickingDocumentId = docId
                        showSelectionDialog = true
                    }
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Logout Button
            LogoutButton(onLogout = { viewModel.logout() })

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
                    onSave = { viewModel.saveProfile() },
                    onCancel = { viewModel.cancelEdit() },
                )
            }
        }

        // Uploading Overlay
        AnimatedVisibility(
            visible = isUploading,
            enter = androidx.compose.animation.fadeIn(),
            exit = androidx.compose.animation.fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
                    .clickable(enabled = false) {},
                contentAlignment = Alignment.Center
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = BrandGreen)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Processing...",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
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
    isEditing: Boolean,
    onEditClick: () -> Unit,
) {
    val isVerified = verificationStatus == "verified"
    val statusColor = when (verificationStatus) {
        "verified" -> BrandGreen
        "pending" -> Color(0xFFFFA000) // Darker variant of Yellow for better visibility
        else -> MaterialTheme.colorScheme.error 
    }
    val statusIcon = if (isVerified) Icons.Default.CheckCircle else Icons.Default.Warning
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
                            .background(BrandGreen.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = initials,
                        style = MaterialTheme.typography.headlineLarge,
                        color = BrandGreen,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Icon(
                    imageVector = statusIcon,
                    contentDescription = verificationStatus,
                    tint = statusColor,
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

            if (!isEditing) {
                IconButton(
                    onClick = onEditClick,
                    modifier =
                        Modifier
                            .size(40.dp)
                            .background(MaterialTheme.colorScheme.surfaceContainer, CircleShape),
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
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
                tint = BrandGreen,
                modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = BrandGreen,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                modifier = Modifier.weight(1f)
            )
            
            if (isVerified) {
                VerificationChip("Verified", BrandGreen)
            } else {
                VerificationChip("Unverified", Color(0xFFFFA000))
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = if (isExpanded) "Collapse" else "Expand",
                tint = BrandGreen
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
    onValueChange: (String) -> Unit = {},
    isSensitive: Boolean = false,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp,
        )
        Spacer(modifier = Modifier.height(6.dp))

        if (isEditing && isEditable) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                textStyle = MaterialTheme.typography.bodyLarge,
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors =
                    OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BrandGreen,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    ),
            )
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

@Composable
private fun VerificationChip(text: String, color: Color) {
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(12.dp),
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
    isEditing: Boolean,
    onDocumentView: (String) -> Unit,
    onDocumentUpload: (DocumentType, String?) -> Unit,
) {
    val types =
        listOf(
            DocumentType.AADHAAR,
            DocumentType.PAN,
            DocumentType.GST_CERTIFICATE,
            DocumentType.COMPANY_REGISTRATION,
            DocumentType.TDS_CERTIFICATE,
            DocumentType.CANCELLED_CHEQUE,
        )

    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        types.forEachIndexed { index, type ->
            val doc = documents.find { it.type == type }

            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clickable { 
                            if (isEditing) {
                                onDocumentUpload(type, doc?.docId)
                            } else if (!doc?.url.isNullOrEmpty()) {
                                onDocumentView(doc!!.url!!)
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
                    if (doc != null && !doc.url.isNullOrEmpty()) {
                        StatusBadge(doc.status)
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Icon(
                        imageVector = Icons.Default.CloudUpload,
                        contentDescription = "Upload",
                        tint = BrandGreen,
                        modifier = Modifier.size(24.dp)
                    )
                } else if (doc != null && !doc.url.isNullOrEmpty()) {
                    StatusBadge(doc.status)
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "View",
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(20.dp)
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
private fun StatusBadge(status: DocumentStatus) {
    val (text, color) =
        when (status) {
            DocumentStatus.VERIFIED -> "Verified" to BrandGreen
            DocumentStatus.REJECTED -> "Rejected" to MaterialTheme.colorScheme.error
            DocumentStatus.UPLOADED -> "Verification Pending" to Color(0xFFFFA000)
            DocumentStatus.PENDING -> "Pending" to MaterialTheme.colorScheme.error
            DocumentStatus.UNKNOWN -> "N/A" to MaterialTheme.colorScheme.onSurfaceVariant
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
private fun LogoutButton(onLogout: () -> Unit) {
    Button(
        onClick = onLogout,
        modifier =
            Modifier
                .fillMaxWidth()
                .height(56.dp),
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

@Composable
private fun EditActions(
    hasChanges: Boolean,
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
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(width = 1.dp),
            ) {
                Text("Cancel", fontWeight = FontWeight.Bold)
            }
            Button(
                onClick = onSave,
                enabled = hasChanges,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandGreen),
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
        TextButton(onClick = onRetry, colors = ButtonDefaults.textButtonColors(contentColor = BrandGreen)) {
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
