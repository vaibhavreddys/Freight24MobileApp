package com.freight24.connect.feature.trips.views.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.outlined.Handshake
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material3.MaterialTheme
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
import com.freight24.common.util.formatLoadWeight
import com.freight24.connect.utils.toDate
import com.freight24.rest.response.TripDataResponse

/**
 * Client label for a trip. [TripDataResponse.Client.name] is null for
 * organisation-only clients, so fall back to the company name.
 */
private val TripDataResponse.Client.label: String
    get() = name ?: companyName.orEmpty()

@Composable
fun TripCard(
    trip: TripDataResponse,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    Card(
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
        border = BorderStroke(1.dp, colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
    ) {
        Column(
            modifier = Modifier
                .padding(
                    start = 16.dp,
                    end = 16.dp,
                    top = 16.dp,
                    bottom = 16.dp
                )
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = trip.tripCode,
                    style = MaterialTheme.typography.labelMedium,
                    color = colorScheme.onSurfaceVariant
                )
                StatusBadge(status = trip.truckerStatus)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Truck — the hero line
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocalShipping,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = trip.truck.registrationNumber,
                    style = MaterialTheme.typography.titleLarge,
                    color = colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "${trip.truck.truckType.displayName} • ${formatLoadWeight(trip.indentDetails.loadWeight, "MT")} • ${trip.indentDetails.material}",
                style = MaterialTheme.typography.bodyMedium,
                color = colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Route — connected dot-line-dot, city names aligned to dots
            Row(verticalAlignment = Alignment.Top) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    RouteDot(color = colorScheme.primary)
                    Box(
                        modifier = Modifier
                            .width(2.dp)
                            .height(20.dp)
                            .background(colorScheme.outlineVariant)
                    )
                    RouteDot(color = colorScheme.secondary)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(top = 0.dp)
                ) {
                    Text(
                        text = trip.loadingLocation.city ?: trip.loadingLocation.area ?: "Unknown",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = colorScheme.onSurface
                    )
                    Text(
                        text = trip.unLoadingLocation.city ?: trip.unLoadingLocation.area ?: "Unknown",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            HorizontalDivider(
                color = colorScheme.outlineVariant,
                thickness = 1.dp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Financials — labelled pair, negotiated cost emphasized
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "CLIENT COST",
                        style = MaterialTheme.typography.labelSmall,
                        color = colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    FinancialItem(
                        icon = Icons.Outlined.Payments,
                        amount = trip.clientTripCost.orEmpty()
                    )
                }

                VerticalDivider(
                    modifier = Modifier
                        .width(1.dp)
                        .height(32.dp)
                        .background(colorScheme.outlineVariant)
                )

                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "NEGOTIATED",
                        style = MaterialTheme.typography.labelSmall,
                        color = colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    FinancialItem(
                        icon = Icons.Outlined.Handshake,
                        amount = trip.truckerTripCost.orEmpty(),
                        emphasized = true
                    )
                }
            }
        }

        Column {
            // Footer: Client & Date
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = colorScheme.surfaceContainerLow
                    )
                    .padding(
                        horizontal = 16.dp,
                        vertical = 8.dp
                    ),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = trip.client.label.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = colorScheme.onSurfaceVariant
                )
                Text(
                    text = trip.createdAt.toDate(),
                    style = MaterialTheme.typography.labelSmall,
                    color = colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Compact list card for the Trips tab — optimized for viewport density:
 * single-line route (origin → destination), no stacked connector, and a
 * brand-tinted financial band instead of a divider + row pair.
 * The rich [TripCard] remains the hero header on Trip Details.
 */
@Composable
fun TripListItem(
    trip: TripDataResponse,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    val colorScheme = MaterialTheme.colorScheme
    Card(
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
        border = BorderStroke(1.dp, colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
    ) {
        Column(
            modifier = Modifier
                .padding(start = 16.dp, end = 16.dp, top = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = trip.tripCode,
                        style = MaterialTheme.typography.labelMedium,
                        color = colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.labelMedium,
                        color = colorScheme.outline
                    )
                    Text(
                        text = trip.client.label,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                StatusBadge(status = trip.truckerStatus)
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocalShipping,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = trip.truck.registrationNumber,
                    style = MaterialTheme.typography.titleMedium,
                    color = colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${trip.truck.truckType.displayName} • ${formatLoadWeight(trip.indentDetails.loadWeight, "MT")} • ${trip.indentDetails.material}",
                    style = MaterialTheme.typography.bodySmall,
                    color = colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Route on one line — origin → destination
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = trip.loadingLocation.city ?: trip.loadingLocation.area ?: "Unknown",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = colorScheme.onSurface
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "to",
                    modifier = Modifier.size(16.dp),
                    tint = colorScheme.primary
                )
                Text(
                    text = trip.unLoadingLocation.city ?: trip.unLoadingLocation.area ?: "Unknown",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
        }

        // Brand-tinted financial band — no icons needed, labels carry meaning
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(colorScheme.primaryContainer)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "CLIENT  ₹${formatAmount(trip.clientTripCost.orEmpty())}",
                style = MaterialTheme.typography.labelMedium,
                color = colorScheme.onPrimaryContainer
            )
            Text(
                text = "NEGOTIATED  ₹${formatAmount(trip.truckerTripCost.orEmpty())}",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = colorScheme.primary
            )
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
            .size(12.dp)
            .background(color, CircleShape)
            .padding(3.dp)
            .background(MaterialTheme.colorScheme.surface, CircleShape)
    )
}

@Composable
fun FinancialItem(
    icon: ImageVector,
    amount: String,
    emphasized: Boolean = false
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = if (emphasized) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "₹${formatAmount(amount)}",
            style = MaterialTheme.typography.titleMedium,
            color = if (emphasized) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurface
            }
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
