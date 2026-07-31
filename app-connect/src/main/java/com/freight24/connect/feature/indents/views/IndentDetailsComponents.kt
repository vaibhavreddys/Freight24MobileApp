package com.freight24.connect.feature.indents.views

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.freight24.ui.components.VisibilityChip
import java.text.NumberFormat
import java.util.Locale

@Composable
fun IndentDetailSection(
    image: ImageVector? = null,
    title: String,
    onEditClick: (() -> Unit)? = null,
    hasError: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceContainer),
        border = BorderStroke(
            1.dp,
            if (hasError) colorScheme.error else colorScheme.outlineVariant,
        ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            if (hasError) colorScheme.errorContainer.copy(alpha = 0.3f)
                            else colorScheme.primaryContainer.copy(alpha = 0.42f),
                            colorScheme.surfaceContainer,
                            colorScheme.surfaceContainerHigh,
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (image != null) {
                        Surface(
                            modifier = Modifier.size(28.dp),
                            shape = CircleShape,
                            color = colorScheme.primaryContainer,
                            contentColor = colorScheme.onPrimaryContainer,
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = image,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    Text(
                        text = title.uppercase(),
                        style = MaterialTheme.typography.labelMedium,
                        color = if (hasError) colorScheme.error else colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                        modifier = Modifier.weight(1f),
                    )

                    if (onEditClick != null) {
                        FilledTonalIconButton(
                            onClick = onEditClick,
                            modifier = Modifier.size(40.dp),
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

                if (hasError) {
                    Text(
                        text = "Client trip cost is required",
                        color = colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }
    }
}

@Composable
fun IndentDetailItem(
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.Top
    ) {
        DetailLabel(text = label, modifier = Modifier.weight(0.9f))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            color = valueColor,
            modifier = Modifier.weight(1.1f),
            textAlign = TextAlign.End
        )
    }
}

@Composable
fun IndentDetailItemColumn(
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        DetailLabel(text = label)
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = valueColor,
        )
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
        colors = CardDefaults.cardColors(containerColor = colorScheme.secondaryContainer),
        border = BorderStroke(1.dp, colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Surface(
                modifier = Modifier.size(36.dp),
                shape = CircleShape,
                color = colorScheme.surface.copy(alpha = 0.72f),
                contentColor = colorScheme.onSecondaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onSecondaryContainer
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = colorScheme.onSecondaryContainer.copy(alpha = 0.72f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun GeneralInfoSection(
    clientName: String,
    tagCode: String?,
    indentId: String? = null,
    onEditClick: (() -> Unit)? = null
) {
    IndentDetailSection(
        image = Icons.Default.Info,
        title = "CLIENT",
        onEditClick = onEditClick,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (indentId != null) {
//                IndentDetailItem(label = "INDENT TYPE", value = "New Indent")
//            } else {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    DetailLabel(text = "INDENT ID")
                    Text(
                        text = "#$indentId",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                IndentDetailItemColumn(
                    label = "CLIENT NAME",
                    value = clientName,
                    modifier = Modifier.weight(1f)
                )
                IndentDetailItemColumn(
                    label = "TAG CODE",
                    value = tagCode?.takeIf { it.isNotBlank() } ?: "Not specified",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun RouteDetailsSection(
    originCity: String,
    destinationCity: String,
    onEditClick: (() -> Unit)? = null
) {
    IndentDetailSection(
        image = Icons.Default.Route,
        title = "ROUTE DETAILS",
        onEditClick = onEditClick,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            RouteTimeline()
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                RoutePoint(label = "ORIGIN CITY", value = originCity)
                RoutePoint(label = "DESTINATION CITY", value = destinationCity)
            }
        }
    }
}

@Composable
fun FinancialDetailsSection(
    clientCost: String,
    negotiatedCost: String,
    hideCost: Boolean,
    onEditClick: (() -> Unit)? = null,
    hasError: Boolean = false,
) {
    IndentDetailSection(
        image = Icons.Outlined.Payments,
        title = "FINANCIAL DETAILS",
        onEditClick = onEditClick,
        hasError = hasError,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
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

            HorizontalDivider(
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                DetailLabel(text = "COST VISIBILITY")
                Spacer(modifier = Modifier.weight(1f))
                VisibilityChip(isVisible = !hideCost)
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
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                SpecificationTile(
                    label = "WEIGHT (MT)",
                    value = weight,
                    modifier = Modifier.weight(1f)
                )
                SpecificationTile(
                    label = "MATERIAL TYPE",
                    value = materialType,
                    modifier = Modifier.weight(1f)
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                DetailLabel(text = "VEHICLE TYPE")
                if (truckType != null) {
                    AssistChip(
                        onClick = {},
                        label = {
                            Text(
                                text = truckType,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.LocalShipping,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            labelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            leadingIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        border = AssistChipDefaults.assistChipBorder(
                            enabled = true,
                            borderColor = MaterialTheme.colorScheme.outlineVariant
                        )
                    )
                } else {
                    Text(
                        text = "Not selected",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                SpecificationTile(
                    label = "TAT",
                    value = if (tatRequired) "$tatHours hrs" else "No",
                    subdued = !tatRequired,
                    modifier = Modifier.weight(1f)
                )
                SpecificationTile(
                    label = "TARPAULIN",
                    value = if (tarpaulinRequired) "Yes" else "No",
                    emphasized = tarpaulinRequired,
                    subdued = !tarpaulinRequired,
                    modifier = Modifier.weight(1f)
                )
            }
        }
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
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Text(
                    text = remarks,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(12.dp)
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
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp,
        modifier = modifier
    )
}

@Composable
private fun RouteTimeline() {
    val colorScheme = MaterialTheme.colorScheme

    Column(
        modifier = Modifier.padding(top = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        RouteDot(isStart = true)
        Box(
            modifier = Modifier
                .height(64.dp)
                .width(4.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(colorScheme.outlineVariant)
        )
        RouteDot(isStart = false)
    }
}

@Composable
private fun TimelineDot(
    containerColor: Color,
    dotColor: Color
) {
    Surface(
        modifier = Modifier.size(28.dp),
        shape = CircleShape,
        color = containerColor
    ) {
        Box(contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(dotColor, CircleShape)
            )
        }
    }
}

@Composable
private fun RoutePoint(
    label: String,
    value: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        DetailLabel(text = label)
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun CostTile(
    label: String,
    value: String,
    emphasized: Boolean = false,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    val containerColor = if (emphasized) colorScheme.surfaceContainerHighest else colorScheme.surfaceContainerHighest
    val contentColor = if (emphasized) colorScheme.primary else colorScheme.onSurface

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = containerColor,
        border = BorderStroke(1.dp, colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            DetailLabel(text = label)
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
        }
    }
}

@Composable
private fun SpecificationTile(
    label: String,
    value: String,
    emphasized: Boolean = false,
    subdued: Boolean = false,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    val containerColor = if (emphasized) colorScheme.tertiaryContainer else colorScheme.surfaceContainerHighest
    val valueColor = when {
        emphasized -> colorScheme.onTertiaryContainer
        subdued -> colorScheme.onSurfaceVariant
        else -> colorScheme.onSurface
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = containerColor,
        border = BorderStroke(1.dp, colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            DetailLabel(text = label)
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = valueColor
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
