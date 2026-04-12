package com.freight24.connect.feature.trips.views.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.outlined.Handshake
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.freight24.connect.utils.toDate
import com.freight24.rest.response.TripDataResponse

@Composable
fun TripCard(
    trip: TripDataResponse,
    onClick: (() -> Unit)? = null
) {
    val colorScheme = MaterialTheme.colorScheme
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
    ) {
        Column(
            modifier = Modifier
                .padding(
                    start = 16.dp,
                    end = 16.dp,
                    top = 16.dp,
                    bottom = 12.dp
                )
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = trip.tripCode,
                    style = MaterialTheme.typography.bodySmall,
                    color = colorScheme.onSurfaceVariant
                )
                StatusBadge(status = trip.truckerStatus)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Truck Info
            Row(verticalAlignment = Alignment.CenterVertically) {

                Icon(
                    imageVector = Icons.Default.LocalShipping,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = trip.truck.registrationNumber,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "|",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 16.sp
                    ),
                    color = colorScheme.onSurfaceVariant,
                )

                Spacer(modifier = Modifier.width(4.dp))

                Text(
                    text = "${trip.truck.truckType.displayName} • ${trip.indentDetails.loadWeight} MT • ${trip.indentDetails.material}",
                    style = MaterialTheme.typography.bodySmall,
                    color = colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Route
            Row(verticalAlignment = Alignment.CenterVertically) {
                RouteDot(color = colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = trip.loadingLocation.city ?: trip.loadingLocation.area ?: "Unknown",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
            Box(
                modifier = Modifier
                    .padding(start = 3.dp)
                    .width(2.dp)
                    .height(12.dp)
                    .background(colorScheme.outlineVariant)
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                RouteDot(color = colorScheme.secondary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = trip.unLoadingLocation.city ?: trip.unLoadingLocation.area ?: "Unknown",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            HorizontalDivider(
                thickness = 0.5.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(0.5.dp)
                    .background(colorScheme.outlineVariant)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Financials
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                FinancialItem(
                    icon = Icons.Outlined.Payments,
                    amount = trip.clientTripCost.orEmpty()
                )

                VerticalDivider(
                    modifier = Modifier
                        .width(0.5.dp)
                        .height(22.dp)
                        .background(colorScheme.outlineVariant)
                )

                FinancialItem(
                    icon = Icons.Outlined.Handshake,
                    amount = trip.truckerTripCost.orEmpty()
                )
            }
        }

        Column {
            // Footer: Client & Date
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = colorScheme.surfaceVariant
                    )
                    .padding(
                        horizontal = 16.dp,
                        vertical = 8.dp
                    ),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = trip.client.name.uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = colorScheme.onSurfaceVariant
                )
                Text(
                    text = trip.createdAt.toDate(),
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun StatusBadge(status: String) {
    val colorScheme = MaterialTheme.colorScheme
    val (color, ticketColor, text) = when (status) {
        "ASSIGNED" -> Triple(colorScheme.primaryContainer, colorScheme.primary, "ASSIGNED")
        "IN_TRANSIT" -> Triple(colorScheme.tertiaryContainer, colorScheme.tertiary, "IN-TRANSIT")
        "LOADING" -> Triple(colorScheme.secondaryContainer, colorScheme.secondary, "LOADING")
        "UNLOADING" -> Triple(colorScheme.primaryContainer, colorScheme.primary, "UNLOADING")
        else -> Triple(colorScheme.surfaceVariant, colorScheme.onSurfaceVariant, status)
    }

    Surface(
        color = color,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = ticketColor
            ),
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

@Composable
fun RouteDot(color: Color) {
    Box(
        modifier = Modifier
            .size(8.dp)
            .background(color, CircleShape)
            .padding(1.dp)
            .background(MaterialTheme.colorScheme.surface, CircleShape)
            .background(color, CircleShape)
    )
}

@Composable
fun FinancialItem(icon: ImageVector, amount: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "₹${formatAmount(amount)}",
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

fun formatAmount(amount: String): String {
    return try {
        "%,.0f".format(amount.toDouble())
    } catch (e: Exception) {
        amount
    }
}
