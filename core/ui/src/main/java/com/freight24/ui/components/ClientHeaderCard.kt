package com.freight24.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.freight24.ui.theme.FreightThemeTokens

@Composable
fun ClientHeaderCard(
    companyName: String?,
    fullName: String,
    phone: String,
    status: String,
    showPhone: Boolean = true,
    modifier: Modifier = Modifier,
) {
    Card(
        shape = MaterialTheme.shapes.medium,
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ClientIcon(
                fullName = fullName,
                isActive = status.equals("active", ignoreCase = true),
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                if (fullName.isNotEmpty()) {
                    Text(
                        text = fullName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )

                    if (companyName != null && fullName != companyName) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = companyName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                } else {
                    companyName?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
                if (showPhone && phone.isNotBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = phone,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }
            }
            ClientStatusChip(status)
            Spacer(Modifier.width(8.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
fun ClientIcon(
    fullName: String,
    isActive: Boolean = true,
) {
    // Five M3 container/on-container pairs that read as distinct "clients"
    // in the list. Same pattern as OwnerAvatar in ManageTruckOwnersScreen:
    // hash the name to pick a stable palette so the same client always gets
    // the same color across screens/launches.
    val extended = FreightThemeTokens.colors
    val inactiveBackground = MaterialTheme.colorScheme.surfaceContainerHigh
    val inactiveText = MaterialTheme.colorScheme.onSurfaceVariant
    val avatarPalettes =
        listOf(
            MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer,
            extended.surfaceSuccess to extended.onSurfaceSuccess,
            extended.surfaceWarning to extended.onSurfaceWarning,
            extended.surfaceInfo to extended.onSurfaceInfo,
            MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer,
        )
    val palette =
        remember(fullName, isActive, inactiveBackground, inactiveText) {
            if (!isActive) {
                inactiveBackground to inactiveText
            } else {
                val hash = fullName.trim().ifBlank { fullName }.hashCode()
                avatarPalettes[kotlin.math.abs(hash) % avatarPalettes.size]
            }
        }
    val initials =
        fullName
            .trim()
            .split(Regex("\\s+"))
            .filter { it.isNotBlank() }
            .take(2)
            .joinToString("") { it.take(1).uppercase() }
            .ifBlank { null }

    Box(
        modifier =
            Modifier
                .size(44.dp)
                .background(palette.first, MaterialTheme.shapes.small),
        contentAlignment = Alignment.Center,
    ) {
        if (initials != null) {
            Text(
                text = initials,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = palette.second,
                ),
            )
        } else {
            Icon(
                imageVector = Icons.Default.Business,
                contentDescription = null,
                tint = palette.second,
            )
        }
    }
}

@Composable
fun ClientStatusChip(status: String) {
    val isActive = status.equals("active", ignoreCase = true)
    val extended = FreightThemeTokens.colors
    val (text, container, content) = if (isActive) {
        Triple("Active", extended.surfaceSuccess, extended.onSurfaceSuccess)
    } else {
        Triple(
            "Inactive",
            MaterialTheme.colorScheme.surfaceContainerHigh,
            MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }

    Surface(
        color = container,
        shape = MaterialTheme.shapes.small,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = content,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        )
    }
}
