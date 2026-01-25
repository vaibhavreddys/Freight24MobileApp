package com.freight24.trucker.feature.profile.view

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val isEditing by viewModel.isEditing.collectAsState()
    val hasChanges by viewModel.hasChanges.collectAsState()


    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = { viewModel.onRefresh() },
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {
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
                    hasChanges = hasChanges
                )
            }
            UiState.Idle -> {}
        }
    }
}

@Composable
private fun ProfileMainContent(
    profile: Profile,
    viewModel: ProfileViewModel,
    isEditing: Boolean,
    hasChanges: Boolean
) {
    val scrollState = rememberScrollState()
    
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 24.dp)
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
                fullName = profile.fullName.orEmpty(),
                partnerId = profile.tagCode.orEmpty(),
                initials = viewModel.getUserInitials(),
                role = "Fleet Owner",
                isVerified = true,
                isEditing = isEditing,
                onEditClick = { viewModel.toggleEditMode() }
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Information Sections
            ProfileSection(title = "PERSONAL INFORMATION", icon = Icons.Default.Person) {
                ProfileField(
                    label = "FULL NAME",
                    value = viewModel.editableFullName.collectAsState().value,
                    isEditing = isEditing,
                    onValueChange = { viewModel.editableFullName.value = it; viewModel.onFieldChanged() }
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), thickness = 1.dp)
                ProfileField(
                    label = "EMAIL ADDRESS",
                    value = viewModel.editableEmailId.collectAsState().value,
                    isEditing = isEditing,
                    onValueChange = { viewModel.editableEmailId.value = it; viewModel.onFieldChanged() }
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), thickness = 1.dp)
                ProfileField(
                    label = "PHONE NUMBER",
                    value = viewModel.editablePhoneNo.collectAsState().value,
                    isEditing = isEditing,
                    onValueChange = { viewModel.editablePhoneNo.value = it; viewModel.onFieldChanged() }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            ProfileSection(title = "BUSINESS DETAILS", icon = Icons.Default.Business) {
                ProfileField(
                    label = "COMPANY NAME",
                    value = viewModel.editableBusinessName.collectAsState().value,
                    isEditing = isEditing,
                    onValueChange = { viewModel.editableBusinessName.value = it; viewModel.onFieldChanged() }
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), thickness = 1.dp)
                ProfileField(
                    label = "GST NUMBER",
                    value = viewModel.editableGstNumber.collectAsState().value,
                    isEditing = isEditing,
                    onValueChange = { viewModel.editableGstNumber.value = it; viewModel.onFieldChanged() }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            ProfileSection(title = "BANKING INFORMATION", icon = Icons.Default.AccountBalance) {
                ProfileField(
                    label = "ACCOUNT HOLDER",
                    value = viewModel.editableAccountHolderName.collectAsState().value,
                    isEditing = isEditing,
                    onValueChange = { viewModel.editableAccountHolderName.value = it; viewModel.onFieldChanged() }
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), thickness = 1.dp)
                ProfileField(
                    label = "ACCOUNT NUMBER",
                    value = viewModel.editableAccountNumber.collectAsState().value,
                    isEditing = isEditing,
                    isSensitive = true,
                    onValueChange = { viewModel.editableAccountNumber.value = it; viewModel.onFieldChanged() }
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), thickness = 1.dp)
                ProfileField(
                    label = "IFSC CODE",
                    value = viewModel.editableIfscCode.collectAsState().value,
                    isEditing = isEditing,
                    onValueChange = { viewModel.editableIfscCode.value = it; viewModel.onFieldChanged() }
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), thickness = 1.dp)
                ProfileField(
                    label = "UPI ID",
                    value = viewModel.editableUpiId.collectAsState().value,
                    isEditing = isEditing,
                    onValueChange = { viewModel.editableUpiId.value = it; viewModel.onFieldChanged() }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Documents Section
            ProfileSection(title = "DOCUMENTS", icon = Icons.AutoMirrored.Filled.Assignment) {
                DocumentList(documents = profile.documents)
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
            label = "ActionButtons"
        ) { editing ->
            if (editing) {
                EditActions(
                    hasChanges = hasChanges,
                    onSave = { viewModel.saveProfile() },
                    onCancel = { viewModel.cancelEdit() }
                )
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
    isVerified: Boolean,
    isEditing: Boolean,
    onEditClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar with Initials
            Box(contentAlignment = Alignment.BottomEnd) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(BrandGreen.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initials,
                        style = MaterialTheme.typography.headlineLarge,
                        color = BrandGreen,
                        fontWeight = FontWeight.Bold
                    )
                }
                if (isVerified) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Verified",
                        tint = BrandGreen,
                        modifier = Modifier
                            .size(22.dp)
                            .background(MaterialTheme.colorScheme.surface, CircleShape)
                            .padding(1.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(20.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = fullName.ifEmpty { "Member" },
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = role,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Partner ID: $partnerId",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            if (!isEditing) {
                IconButton(
                    onClick = onEditClick,
                    modifier = Modifier
                        .size(40.dp)
                        .background(MaterialTheme.colorScheme.surfaceContainer, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
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
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = BrandGreen,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = BrandGreen,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                content()
            }
        }
    }
}

@Composable
private fun ProfileField(
    label: String,
    value: String,
    isEditing: Boolean,
    onValueChange: (String) -> Unit = {},
    isSensitive: Boolean = false
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 14.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
        Spacer(modifier = Modifier.height(6.dp))
        
        if (isEditing) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                textStyle = MaterialTheme.typography.bodyLarge,
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BrandGreen,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                )
            )
        } else {
            val displayValue = if (isSensitive && value.isNotEmpty()) "•••• •••• •••• ${value.takeLast(4)}" else value.ifEmpty { "Not added" }
            Text(
                text = displayValue,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = if (value.isEmpty()) MaterialTheme.colorScheme.outlineVariant else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun DocumentList(documents: List<ProfileDocument>) {
    val types = listOf(
        DocumentType.PAN, DocumentType.AADHAR, 
        DocumentType.GST_CERTIFICATE, DocumentType.TDS_CERTIFICATE,
        DocumentType.CANCELLED_CHEQUE
    )
    
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        types.forEachIndexed { index, type ->
            val doc = documents.find { it.type == type }
            val status = doc?.status ?: DocumentStatus.PENDING
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = getDocIcon(type),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = type.displayName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.onSurface
                )
                StatusBadge(status)
            }
            if (index < types.lastIndex) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), thickness = 1.dp, modifier = Modifier.padding(horizontal = 20.dp))
            }
        }
    }
}

@Composable
private fun StatusBadge(status: DocumentStatus) {
    val (text, color) = when (status) {
        DocumentStatus.UPLOADED -> "Verified" to BrandGreen
        DocumentStatus.PENDING -> "Pending" to MaterialTheme.colorScheme.error
        DocumentStatus.UNKNOWN -> "N/A" to MaterialTheme.colorScheme.onSurfaceVariant
    }
    
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = text,
            color = color,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun LogoutButton(onLogout: () -> Unit) {
    Button(
        onClick = onLogout,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(28.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.error
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
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
    onCancel: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        tonalElevation = 8.dp,
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 12.dp
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                border = ButtonDefaults.outlinedButtonBorder.copy(width = 1.dp)
            ) {
                Text("Cancel", fontWeight = FontWeight.Bold)
            }
            Button(
                onClick = onSave,
                enabled = hasChanges,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandGreen)
            ) {
                Text("Save Changes", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun ProfileErrorState(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.ErrorOutline, null, Modifier.size(48.dp), tint = MaterialTheme.colorScheme.error)
        Spacer(Modifier.height(16.dp))
        Text(message, style = MaterialTheme.typography.bodyLarge, textAlign = androidx.compose.ui.text.style.TextAlign.Center, color = MaterialTheme.colorScheme.onBackground)
        TextButton(onClick = onRetry, colors = ButtonDefaults.textButtonColors(contentColor = BrandGreen)) { 
            Text("Retry", fontWeight = FontWeight.Bold) 
        }
    }
}

private fun getDocIcon(type: DocumentType) = when (type) {
    DocumentType.PAN -> Icons.Default.CreditCard
    DocumentType.AADHAR -> Icons.Default.Fingerprint
    DocumentType.GST_CERTIFICATE -> Icons.Default.Description
    DocumentType.TDS_CERTIFICATE -> Icons.Default.Badge
    DocumentType.CANCELLED_CHEQUE -> Icons.Default.AccountBalance
    else -> Icons.Default.Description
}
