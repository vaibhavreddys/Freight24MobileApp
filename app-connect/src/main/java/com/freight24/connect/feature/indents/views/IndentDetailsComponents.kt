package com.freight24.connect.feature.indents.views

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.CircleShape
import com.freight24.ui.components.VisibilityChip
import java.text.NumberFormat
import java.util.Locale

private val TextGrey = Color(0xFF64748B)
private val TextDark = Color(0xFF0F172A)
private val BorderLight = Color(0xFFF1F5F9)
private val GreenBrand = Color(0xFF10B981)

@Composable
fun IndentDetailSection(
    image: ImageVector? = null,
    title: String,
    onEditClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, BorderLight),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (image != null) {
                    val hasBg = title == "GENERAL INFO"
                    Box(
                        modifier = if (hasBg) Modifier
                            .background(Color(0xFFF1F5F9), CircleShape)
                            .padding(4.dp) else Modifier,
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            image,
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(
                                color = TextGrey,
                                blendMode = BlendMode.SrcIn
                            ),
                            modifier = Modifier.size(if (hasBg) 14.dp else 18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }

                Text(
                    text = title.uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    color = TextGrey,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )

                Spacer(modifier = Modifier.weight(1f))

                if (onEditClick != null) {
                    IconButton(
                        onClick = onEditClick,
                        modifier = Modifier
                            .size(32.dp)
                            .background(Color(0xFFECFDF5), CircleShape)
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = GreenBrand,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            content()
        }
    }
}

@Composable
fun IndentDetailItem(
    label: String,
    value: String,
    valueColor: Color = TextDark,
) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = TextGrey,
        )

        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = valueColor,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.End
        )
    }
}

@Composable
fun IndentDetailItemColumn(
    label: String,
    value: String,
    valueColor: Color = TextDark,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = TextGrey,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
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
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        MetricCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Outlined.People,
            value = interestCount.toString(),
            label = "Interest Expressed",
            iconBackgroundColor = Color(0xFFECFDF5),
            iconTintColor = GreenBrand,
            onClick = onInterestClick
        )
        MetricCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Outlined.LocalShipping,
            value = suitableTrucks.toString(),
            label = "Suitable Trucks",
            iconBackgroundColor = Color(0xFFF0FDF4),
            iconTintColor = GreenBrand,
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
    iconBackgroundColor: Color,
    iconTintColor: Color,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = if (onClick != null) modifier.clickable { onClick() } else modifier,
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, BorderLight)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                color = iconBackgroundColor
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTintColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = TextGrey,
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
        title = "GENERAL INFO",
        onEditClick = onEditClick,
    ) {
        if(indentId == null) {
            IndentDetailItem(label = "INDENT TYPE", value = "New Indent")
        } else {
            IndentDetailItem(label = "INDENT ID", value = "#${indentId}")
        }

        IndentDetailItem(
            label = "CLIENT NAME",
            value = clientName
        )

        IndentDetailItem(
            label = "TAG CODE",
            value = tagCode ?: "Not specified"
        )
    }
}

@Composable
fun RouteDetailsSection(
    originCity: String,
    destinationCity: String,
    onEditClick: (() -> Unit)? = null
) {
    IndentDetailSection(
        image = Icons.Default.LocationOn,
        title = "ROUTE DETAILS",
        onEditClick = onEditClick,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(GreenBrand, CircleShape)
            )
            Column {
                Text(
                    text = "ORIGIN CITY",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextGrey,
                )
                Text(
                    text = originCity,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = TextDark
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(Color(0xFFEF4444), CircleShape)
            )
            Column {
                Text(
                    text = "DESTINATION CITY",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextGrey,
                )
                Text(
                    text = destinationCity,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = TextDark
                )
            }
        }
    }
}

@Composable
fun FinancialDetailsSection(
    clientCost: String,
    negotiatedCost: String,
    hideCost: Boolean,
    onEditClick: (() -> Unit)? = null
) {
    val formatInr = { amount: String ->
        val number = amount.replace(",", "").toDoubleOrNull()
        if (number != null) {
            val format = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
            format.maximumFractionDigits = 0
            format.format(number).replace("₹", "₹ ")
        } else {
            "₹ $amount"
        }
    }

    IndentDetailSection(
        image = Icons.Outlined.Payments,
        title = "FINANCIAL DETAILS",
        onEditClick = onEditClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(32.dp),
        ) {
            IndentDetailItemColumn(
                label = "CLIENT COST",
                value = formatInr(clientCost),
                modifier = Modifier.weight(1f)
            )

            IndentDetailItemColumn(
                label = "NEGOTIATED COST",
                value = formatInr(negotiatedCost),
                modifier = Modifier.weight(1f)
            )
        }

        HorizontalDivider(
            thickness = 0.5.dp,
            color = Color(0xFFe2e8f0),
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "COST VISIBILITY",
                style = MaterialTheme.typography.labelSmall,
                color = TextGrey,
            )

            Spacer(modifier = Modifier.weight(1f))

            VisibilityChip(
                isVisible = !hideCost
            )
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
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(32.dp),
        ) {
            IndentDetailItemColumn(
                label = "WEIGHT (MT)",
                value = weight,
                modifier = Modifier.weight(1f)
            )

            IndentDetailItemColumn(
                label = "MATERIAL TYPE",
                value = materialType,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(20.dp))

        Text(
            text = "VEHICLE TYPE",
            style = MaterialTheme.typography.labelSmall,
            color = TextGrey,
        )
        Spacer(Modifier.height(6.dp))

        if (truckType != null) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = GreenBrand,
            ) {
                Text(
                    text = truckType,
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                )
            }
        } else {
            Text(
                text = "Not selected",
                style = MaterialTheme.typography.bodyMedium,
                color = TextDark
            )
        }

        Spacer(Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(32.dp),
        ) {
            IndentDetailItemColumn(
                label = "TAT",
                value = if (tatRequired) "$tatHours hrs" else "No",
                valueColor = if (tatRequired) TextDark else TextGrey,
                modifier = Modifier.weight(1f)
            )
            IndentDetailItemColumn(
                label = "TARPAULIN",
                value = if (tarpaulinRequired) "Yes" else "No",
                valueColor = if (tarpaulinRequired) GreenBrand else TextGrey,
                modifier = Modifier.weight(1f)
            )
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
            image = Icons.Default.Info, // Or another icon
            title = "REMARKS",
            onEditClick = onEditClick
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = Color(0xFFF3F3F3),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(16.dp)
            ) {
                Text(
                    text = remarks,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Gray,
                    textAlign = TextAlign.Start
                )
            }
        }
    }
}
