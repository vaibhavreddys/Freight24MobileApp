package com.freight24.connect.feature.more.views

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.freight24.ui.components.QuickActionCard
import com.freight24.ui.components.QuickActionListItem
import com.freight24.ui.components.SectionContainer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreBottomSheet(
    onDismiss: () -> Unit,
    onOnboardClient: () -> Unit,
    onOnboardTruck: () -> Unit,
    onOnboardTruckOwner: () -> Unit,
    onMyProfile: () -> Unit,
    onSettings: () -> Unit,
    onLogout: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val brandGreen = Color(0xFF16A34A)
    val softGreen = if (isDark) Color(0xFF1E2722) else Color(0xFFF1FDF6)
    val tileStroke = if (isDark) Color(0xFF2C3530) else Color(0xFFD1FAE5)
    val iconContainerColor = if (isDark) Color(0xFF2C3530) else Color(0xFFDCFCE7)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        dragHandle = {
            BottomSheetDefaults.DragHandle(
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
            )
        },
        containerColor = if (isDark) MaterialTheme.colorScheme.surface else Color(0xFFF9FAFB),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
        ) {

            // Profile Header
            ProfileHeader(
                name = "Alex Mercer", // TODO: Get from ViewModel/Context
                role = "Logistics Coordinator", // TODO: Get from ViewModel/Context
                onProfileClick = {
                    onMyProfile()
                    onDismiss()
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Quick Actions Section
            SectionContainer(title = "Quick Actions") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val tileContentPadding = PaddingValues(vertical = 14.dp, horizontal = 4.dp)

                    QuickActionCard(
                        title = "Manage\nClients",
                        icon = Icons.Default.Person,
                        onClick = {
                            onOnboardClient()
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f),
                        containerColor = softGreen,
                        border = BorderStroke(1.dp, tileStroke),
                        contentPadding = tileContentPadding,
                        iconContainerColor = if (isDark) iconContainerColor else Color(0xFFE0F2FE),
                        iconTint = Color(0xFF0284C7),
                        textColor = if (isDark) Color.White else Color(0xFF374151)
                    )
                    QuickActionCard(
                        title = "Manage\nTrucks",
                        icon = Icons.Default.LocalShipping,
                        onClick = {
                            onOnboardTruck()
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f),
                        containerColor = softGreen,
                        border = BorderStroke(1.dp, tileStroke),
                        contentPadding = tileContentPadding,
                        iconContainerColor = iconContainerColor,
                        iconTint = brandGreen,
                        textColor = if (isDark) Color.White else Color(0xFF374151)
                    )
                    QuickActionCard(
                        title = "Manage\nTruck Owners",
                        icon = Icons.Default.Group,
                        onClick = {
                            onOnboardTruckOwner()
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f),
                        containerColor = softGreen,
                        border = BorderStroke(1.dp, tileStroke),
                        contentPadding = tileContentPadding,
                        iconContainerColor = if (isDark) iconContainerColor else Color(0xFFFEF3C7),
                        iconTint = Color(0xFFD97706),
                        textColor = if (isDark) Color.White else Color(0xFF374151)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // App Settings Section
            SectionContainer(title = "App Settings") {
                QuickActionListItem(
                    title = "Settings",
                    icon = Icons.Outlined.Settings,
                    onClick = {
                        onSettings()
                        onDismiss()
                    },
                    iconContainerColor = iconContainerColor,
                    iconTint = brandGreen,
                    showDivider = true
                )

                QuickActionListItem(
                    title = "Log Out",
                    icon = Icons.AutoMirrored.Filled.ExitToApp,
                    onClick = {
                        onLogout()
                        onDismiss()
                    },
                    iconContainerColor = Color(0x18FF4D6D),
                    iconContainerBorder = BorderStroke(1.dp, Color(0x33FF4D6D)),
                    iconTint = Color(0xFFFF4D6D),
                    textColor = Color(0xFFFF4D6D)
                )
            }
        }
    }
}
