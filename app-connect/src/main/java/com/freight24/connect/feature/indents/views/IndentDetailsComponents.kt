package com.freight24.connect.feature.indents.views

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.outlined.Payments
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
import com.freight24.ui.components.VisibilityChip

@Composable
fun IndentDetailSection(
    image: ImageVector? = null,
    title: String,
    onEditClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (image != null) {
                    Image(
                        image,
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(
                            color = Color.Gray,
                            blendMode = BlendMode.SrcIn
                        ),
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.size(8.dp))

                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.Gray,
                    fontWeight = FontWeight.Bold,
                )

                Spacer(modifier = Modifier.weight(1f))

                if (onEditClick != null) {
                    IconButton(onClick = onEditClick, modifier = Modifier.size(24.dp)) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            content()
        }
    }
}

@Composable
fun IndentDetailItem(
    label: String,
    value: String,
    valueColor: Color = Color.Black,
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.Gray,
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
    valueColor: Color = Color.Black,
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.Gray,
        )

        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = valueColor,
        )
    }
}

@Composable
fun GeneralInfoSection(
    clientName: String,
    tagCode: String?,
    onEditClick: (() -> Unit)? = null
) {
    IndentDetailSection(
        image = Icons.Default.Info,
        title = "GENERAL INFO",
        onEditClick = onEditClick,
    ) {
        IndentDetailItem(label = "INDENT TYPE", value = "New Indent")

        Spacer(modifier = Modifier.size(8.dp))

        IndentDetailItem(
            label = "CLIENT NAME",
            value = clientName
        )

        Spacer(modifier = Modifier.size(8.dp))

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
        image = Icons.Default.Route,
        title = "ROUTE DETAILS",
        onEditClick = onEditClick,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                Icons.Default.LocationOn,
                contentDescription = null,
                tint = Color(0xFF4CAF50),
                modifier = Modifier.size(20.dp),
            )
            Column {
                Text(
                    text = "ORIGIN CITY",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray,
                )
                Text(
                    text = originCity,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                Icons.Default.LocationOn,
                contentDescription = null,
                tint = Color(0xFFF44336),
                modifier = Modifier.size(20.dp),
            )
            Column {
                Text(
                    text = "DESTINATION CITY",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray,
                )
                Text(
                    text = destinationCity,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
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
    IndentDetailSection(
        Icons.Outlined.Payments,
        "FINANCIAL DETAILS",
        onEditClick = onEditClick
    ) {
        Row(
            modifier = Modifier.padding(vertical = 16.dp)
        ) {
            IndentDetailItemColumn(
                label = "CLIENT COST",
                value = "₹ $clientCost"
            )

            Spacer(modifier = Modifier.weight(1f))

            VerticalDivider(
                thickness = 0.5.dp,
                color = Color(0xFFe2e8f0),
                modifier = Modifier.height(32.dp)
            )

            Spacer(modifier = Modifier.weight(1f))

            IndentDetailItemColumn(
                label = "NEGOTIATED COST",
                value = "₹ $negotiatedCost"
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
                color = Color.Gray,
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
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "WEIGHT (MT)",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray,
                )
                Text(
                    text = weight,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "MATERIAL TYPE",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray,
                )
                Text(
                    text = materialType,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        HorizontalDivider(
            thickness = 0.5.dp,
            color = Color(0xFFe2e8f0)
        )

        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "VEHICLE TYPE",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray,
            )

            Spacer(Modifier.weight(1f))

            if (truckType != null) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF4CAF50),
                ) {
                    Text(
                        text = truckType,
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    )
                }
            } else {
                Text(
                    text = "Not selected",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        IndentDetailItem(
            label = "TAT",
            value = if (tatRequired) "$tatHours hrs" else "No",
            valueColor = if (tatRequired) Color.Black else Color.Gray,
        )

        Spacer(Modifier.height(8.dp))

        IndentDetailItem(
            label = "TARPAULIN",
            value = if (tarpaulinRequired) "Yes" else "No",
            valueColor = if (tarpaulinRequired) Color(0xFF4CAF50) else Color.Gray,
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
