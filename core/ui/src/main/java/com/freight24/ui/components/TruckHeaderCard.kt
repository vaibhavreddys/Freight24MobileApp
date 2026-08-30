package com.freight24.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.freight24.ui.theme.FreightThemeTokens

@Composable
fun TruckHeaderCard(
    registrationNumber: String,
    truckType: String?,
    availability: String,
    isVerified: Boolean,
    isBlacklisted: Boolean,
    modifier: Modifier = Modifier,
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
            ),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                TruckIcon(availability, isBlacklisted)
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = registrationNumber,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                    )
                    Text(
                        text = truckType ?: "Unknown Truck",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                TruckStatusChip(availability, isBlacklisted)
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = MaterialTheme.colorScheme.outlineVariant,
            )

            TruckVerificationRow(isVerified)
        }
    }
}

@Composable
fun TruckIcon(
    status: String,
    isBlacklisted: Boolean,
) {
    val containerColor =
        when {
            isBlacklisted -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
            status.uppercase() == "ACTIVE" || status.uppercase() == "AVAILABLE" ->
                MaterialTheme.colorScheme.primaryContainer.copy(
                    alpha = 0.5f,
                )
            status.uppercase() == "ON_TRIP" -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
            else -> MaterialTheme.colorScheme.surfaceVariant
        }

    Box(
        modifier =
            Modifier
                .size(44.dp)
                .background(containerColor, RoundedCornerShape(10.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = if (isBlacklisted) Icons.Default.Warning else Icons.Default.LocalShipping,
            contentDescription = null,
            tint = if (isBlacklisted) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
fun TruckStatusChip(
    status: String,
    isBlacklisted: Boolean,
) {
    val (text, color) =
        when {
            isBlacklisted -> "Blacklisted" to MaterialTheme.colorScheme.error
            status.uppercase() == "ACTIVE" || status.uppercase() == "AVAILABLE" -> "Active" to MaterialTheme.colorScheme.primary
            status.uppercase() == "ON_TRIP" -> "On Trip" to MaterialTheme.colorScheme.secondary
            else -> "Inactive" to MaterialTheme.colorScheme.outline
        }

    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(8.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = color,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        )
    }
}

@Composable
fun TruckVerificationRow(isVerified: Boolean) {
    val (text, icon, color) =
        if (isVerified) {
            Triple("Document verification completed", Icons.Default.CheckCircle, FreightThemeTokens.colors.success)
        } else {
            Triple(
                "Verification Pending",
                Icons.Default.Error,
                FreightThemeTokens.colors.warning,
            )
        }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(16.dp),
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = color,
            fontWeight = FontWeight.Medium,
        )
    }
}
