package com.freight24.connect.feature.more.views

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PeopleAlt
import androidx.compose.material.icons.filled.PeopleOutline
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
    onLogout: () -> Unit,
    moreViewModel: MoreViewModel = hiltViewModel(),
) {
    val employeeInfo = moreViewModel.employeeInfo.collectAsStateWithLifecycle().value
    val colorScheme = MaterialTheme.colorScheme
    val brandGreen = colorScheme.primary
    val softGreen = colorScheme.surfaceVariant
    val tileStroke = colorScheme.outlineVariant
    val iconContainerColor = colorScheme.primaryContainer

    Scaffold(
        topBar = {
            TopAppBar(
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
                onProfileClick = onMyProfile,
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Quick Actions Section
            SectionContainer(title = "Quick Actions") {
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
                    onClick = onLogout,
                    iconContainerColor = colorScheme.errorContainer,
                    iconContainerBorder = BorderStroke(1.dp, colorScheme.error.copy(alpha = 0.2f)),
                    iconTint = colorScheme.error,
                    textColor = colorScheme.error,
                )
            }
        }
    }
}
