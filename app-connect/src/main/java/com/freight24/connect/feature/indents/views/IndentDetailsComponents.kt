package com.freight24.connect.feature.indents.views

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.outlined.Checkroom
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Scale
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.freight24.ui.components.RouteIndicatorWithCities
import com.freight24.ui.components.VisibilityChip
import com.freight24.ui.theme.FreightThemeTokens
import java.text.NumberFormat
import java.util.Locale

@Composable
fun IndentDetailSection(
    image: ImageVector? = null,
    title: String,
    onEditClick: (() -> Unit)? = null,
    hasError: Boolean = false,
    trailing: @Composable () -> Unit = {},
    content: @Composable () -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceContainer),
        border = if (hasError) BorderStroke(1.dp, colorScheme.error) else null,
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (image != null) {
                    Surface(
                        modifier = Modifier.size(24.dp),
                        shape = CircleShape,
                        color = colorScheme.primaryContainer,
                        contentColor = colorScheme.onPrimaryContainer,
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = image,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }

                Text(
                    text = title.uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    color = if (hasError) colorScheme.error else colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.8.sp,
                    modifier = Modifier.weight(1f),
                )

                trailing()

                if (onEditClick != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    FilledTonalIconButton(
                        onClick = onEditClick,
                        modifier = Modifier.size(48.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit $title",
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
            }

            content()
        }
    }
}

@Composable
fun GeneralInfoSection(
    clientName: String,
    clientPhone: String?,
    tagCode: String?,
    status: String? = null,
    onEditClick: (() -> Unit)? = null
) {
    val colorScheme = MaterialTheme.colorScheme
    val phone = clientPhone?.takeIf { it.isNotBlank() }
    val context = LocalContext.current

    IndentDetailSection(
        image = Icons.Default.Info,
        title = "CLIENT",
        onEditClick = onEditClick,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = clientName,
                    style = MaterialTheme.typography.titleLarge,
                    color = colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                )
                if (status != null) {
                    IndentStatusChip(status)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    DetailLabel(text = "PHONE")
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = if (phone != null) {
                            Modifier.clickable {
                                context.startActivity(
                                    Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                                )
                            }
                        } else {
                            Modifier
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = null,
                            tint = if (phone != null) colorScheme.primary else colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = phone ?: "Not available",
                            style = MaterialTheme.typography.titleMedium,
                            color = if (phone != null) colorScheme.primary else colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 6.dp)
                        )
                    }
                }

                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    DetailLabel(text = "TAG CODE")
                    Text(
                        text = tagCode?.takeIf { it.isNotBlank() } ?: "Not specified",
                        style = MaterialTheme.typography.titleMedium,
                        color = colorScheme.onSurface,
                        textAlign = TextAlign.End
                    )
                }
            }
        }
    }
}

@Composable
fun IndentMetricsSection(
    interestCount: Int,
    suitableTrucks: Int,
    onInterestClick: () -> Unit = {},
    onSuitableTrucksClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        MetricCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Outlined.People,
            value = interestCount.toString(),
            label = "Interest Expressed",
            onClick = onInterestClick
        )
        MetricCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Outlined.LocalShipping,
            value = suitableTrucks.toString(),
            label = "Suitable Trucks",
            onClick = onSuitableTrucksClick
        )
    }
}

@Composable
private fun MetricCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    value: String,
    label: String,
    onClick: (() -> Unit)? = null
) {
    val colorScheme = MaterialTheme.colorScheme

    Card(
        modifier = if (onClick != null) modifier.clickable { onClick() } else modifier,
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceContainer),
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Surface(
                modifier = Modifier.size(28.dp),
                shape = CircleShape,
                color = colorScheme.primaryContainer,
                contentColor = colorScheme.onPrimaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = colorScheme.onSurface
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun IndentStatusChip(status: String) {
    val colorScheme = MaterialTheme.colorScheme
    val extended = FreightThemeTokens.colors
    val (backgroundColor, contentColor) =
        when (status.uppercase()) {
            "OPEN" -> colorScheme.primaryContainer to colorScheme.onPrimaryContainer
            "READY_FOR_ASSIGNMENT" -> extended.surfaceSuccess to extended.onSurfaceSuccess
            "CANCELLED" -> colorScheme.errorContainer to colorScheme.onErrorContainer
            "COMPLETED" -> extended.surfaceSuccess to extended.onSurfaceSuccess
            else -> colorScheme.surfaceVariant to colorScheme.onSurfaceVariant
        }

    Surface(
        shape = RoundedCornerShape(50),
        color = backgroundColor,
    ) {
        Text(
            text = status
                .split("_")
                .joinToString(" ") { word -> word.lowercase().replaceFirstChar { it.uppercase() } },
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = contentColor,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
        )
    }
}

@Composable
fun RouteAndFinancialsSection(
    originCity: String,
    destinationCity: String,
    clientCost: String,
    negotiatedCost: String,
    hideCost: Boolean,
    onEditClick: (() -> Unit)? = null,
    hasError: Boolean = false,
) {
    IndentDetailSection(
        image = Icons.Default.Route,
        title = "ROUTE & FINANCIALS",
        onEditClick = onEditClick,
        hasError = hasError,
        trailing = {
            VisibilityChip(isVisible = !hideCost)
        },
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            RouteIndicatorWithCities(
                fromCity = originCity,
                toCity = destinationCity,
//                fromLabel = "From",
//                toLabel = "To",
            )

            if (clientCost.isNotBlank() || negotiatedCost.isNotBlank()) {
                HorizontalDivider(
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant,
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    CostTile(
                        label = "CLIENT COST",
                        value = formatInr(clientCost),
                        modifier = Modifier.weight(1f)
                    )
                    CostTile(
                        label = "NEGOTIATED COST",
                        value = formatInr(negotiatedCost),
                        emphasized = true,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun SpecificationsSection(
    weight: String,
    materialType: String,
    truckType: String?,
    tatRequired: Boolean,
    tatHours: String,
    tarpaulinRequired: Boolean,
    onEditClick: (() -> Unit)? = null
) {
    IndentDetailSection(
        image = Icons.Outlined.Tune,
        title = "SPECIFICATIONS",
        onEditClick = onEditClick,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            SpecRow(
                icon = Icons.Outlined.Scale,
                label = "WEIGHT (MT)",
                value = weight,
            )
            SpecRow(
                icon = Icons.Outlined.Inventory2,
                label = "MATERIAL TYPE",
                value = materialType,
            )
            SpecRow(
                icon = Icons.Outlined.LocalShipping,
                label = "VEHICLE TYPE",
                value = truckType ?: "Not selected",
                subdued = truckType == null,
            )
            SpecRow(
                icon = Icons.Outlined.Schedule,
                label = "TAT",
                value = if (tatRequired) "$tatHours hrs" else "No",
                subdued = !tatRequired,
            )
            SpecRow(
                icon = Icons.Outlined.Checkroom,
                label = "TARPAULIN",
                value = if (tarpaulinRequired) "Yes" else "No",
                emphasized = tarpaulinRequired,
                subdued = !tarpaulinRequired,
            )
        }
    }
}

@Composable
private fun SpecRow(
    icon: ImageVector,
    label: String,
    value: String,
    emphasized: Boolean = false,
    subdued: Boolean = false,
) {
    val colorScheme = MaterialTheme.colorScheme
    val valueColor = when {
        emphasized -> colorScheme.primary
        subdued -> colorScheme.onSurfaceVariant
        else -> colorScheme.onSurface
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            modifier = Modifier.size(28.dp),
            shape = CircleShape,
            color = colorScheme.primaryContainer,
            contentColor = colorScheme.onPrimaryContainer,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        DetailLabel(
            text = label,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = valueColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
fun RemarksSection(
    remarks: String,
    onEditClick: (() -> Unit)? = null
) {
    if (remarks.isNotEmpty()) {
        IndentDetailSection(
            image = Icons.Default.Info,
            title = "REMARKS",
            onEditClick = onEditClick
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHighest,
            ) {
                Text(
                    text = remarks,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(10.dp)
                )
            }
        }
    }
}

@Composable
private fun DetailLabel(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.6.sp,
        modifier = modifier
    )
}

@Composable
private fun CostTile(
    label: String,
    value: String,
    emphasized: Boolean = false,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = colorScheme.surfaceContainerHighest,
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            DetailLabel(text = label)
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = if (emphasized) colorScheme.primary else colorScheme.onSurface
            )
        }
    }
}

private fun formatInr(amount: String): String {
    val number = amount.replace(",", "").toDoubleOrNull()
    return if (number != null) {
        val format = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("en-IN"))
        format.maximumFractionDigits = 0
        format.format(number).replace("₹", "₹ ")
    } else {
        "₹ $amount"
    }
}
