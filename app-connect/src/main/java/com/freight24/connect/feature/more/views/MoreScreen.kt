package com.freight24.connect.feature.more.views
import com.freight24.ui.components.FreightTopAppBar

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PeopleAlt
import androidx.compose.material.icons.filled.PeopleOutline
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.freight24.connect.feature.more.MoreViewModel
import com.freight24.ui.components.QuickActionCard
import com.freight24.ui.components.QuickActionListItem
import com.freight24.ui.components.SectionContainer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreScreen(
    onBackClick: () -> Unit,
    onManageClients: () -> Unit,
    onManageTrucks: () -> Unit,
    onManageTruckOwners: () -> Unit,
    onMyProfile: () -> Unit,
    onSettings: () -> Unit,
    moreViewModel: MoreViewModel = hiltViewModel(),
) {
    val employeeInfo = moreViewModel.employeeInfo.collectAsStateWithLifecycle().value
    val showLogoutSheet = moreViewModel.showLogoutSheet.collectAsStateWithLifecycle().value
    val colorScheme = MaterialTheme.colorScheme
    val brandGreen = colorScheme.primary
    val softGreen = colorScheme.surfaceVariant
    val tileStroke = colorScheme.outlineVariant
    val iconContainerColor = colorScheme.primaryContainer

    Scaffold(
        topBar = {
            FreightTopAppBar(
                title = {
                    Text(
                        text = "More",
                        style = MaterialTheme.typography.titleLarge,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(bottom = 32.dp),
        ) {

            Spacer(modifier = Modifier.height(12.dp))
            // Profile Header
            ProfileHeader(
                name = employeeInfo.fullName,
                role = employeeInfo.designation,
                tagCode = employeeInfo.tagCode,
                onProfileClick = onMyProfile,
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Quick Actions Section
            SectionContainer(title = "Fleet & Partners") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    val tileContentPadding = PaddingValues(vertical = 14.dp, horizontal = 4.dp)

                    QuickActionCard(
                        title = "Manage\nClients",
                        icon = Icons.Default.Person,
                        onClick = onManageClients,
                        modifier = Modifier.weight(1f),
                        containerColor = softGreen,
                        border = BorderStroke(1.dp, tileStroke),
                        contentPadding = tileContentPadding,
                        iconContainerColor = colorScheme.primaryContainer,
                        iconTint = colorScheme.primary,
                        textColor = colorScheme.onSurface,
                    )
                    QuickActionCard(
                        title = "Manage\nTrucks",
                        icon = Icons.Default.LocalShipping,
                        onClick = onManageTrucks,
                        modifier = Modifier.weight(1f),
                        containerColor = softGreen,
                        border = BorderStroke(1.dp, tileStroke),
                        contentPadding = tileContentPadding,
                        iconContainerColor = iconContainerColor,
                        iconTint = brandGreen,
                        textColor = colorScheme.onSurface,
                    )
                    QuickActionCard(
                        title = "Manage\nTruck Owners",
                        icon = Icons.Default.PeopleAlt,
                        onClick = onManageTruckOwners,
                        modifier = Modifier.weight(1f),
                        containerColor = softGreen,
                        border = BorderStroke(1.dp, tileStroke),
                        contentPadding = tileContentPadding,
                        iconContainerColor = iconContainerColor,
                        iconTint = brandGreen,
                        textColor = colorScheme.onSurface,
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // App Settings Section
            SectionContainer(title = "App Settings") {
                QuickActionListItem(
                    title = "Settings",
                    icon = Icons.Outlined.Settings,
                    onClick = onSettings,
                    iconContainerColor = iconContainerColor,
                    iconTint = brandGreen,
                    showDivider = true,
                )

                QuickActionListItem(
                    title = "Log Out",
                    icon = Icons.AutoMirrored.Filled.ExitToApp,
                    onClick = moreViewModel::requestLogout,
                    iconContainerColor = colorScheme.errorContainer,
                    iconContainerBorder = BorderStroke(1.dp, colorScheme.error.copy(alpha = 0.2f)),
                    iconTint = colorScheme.error,
                    textColor = colorScheme.error,
                )
            }
        }
    }

    if (showLogoutSheet) {
        LogoutConfirmationSheet(
            onConfirm = {
                moreViewModel.dismissLogoutSheet()
                moreViewModel.logout()
            },
            onDismiss = { moreViewModel.dismissLogoutSheet() },
        )
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
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Icon badge
            Box(
                modifier = Modifier
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
                text = "Are you sure you want to log out of your account? You'll need to sign back in to manage your indents and trips.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Primary action: Logout
            Button(
                onClick = onConfirm,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .semantics(mergeDescendants = true) { contentDescription = "logout_confirm" },
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
            ) {
                Text(text = "Logout", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Secondary action: Stay Logged In
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .semantics(mergeDescendants = true) { contentDescription = "logout_cancel" },
                shape = RoundedCornerShape(28.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
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
