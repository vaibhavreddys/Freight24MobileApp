package com.freight24.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IndentCard(
    companyName: String,
    indentCode: String?,
    postedTime: String,
    fromCity: String,
    toCity: String,
    clientCostFormatted: String,
    negotiatedCostFormatted: String,
    truckDisplay: String,
    weightDisplay: String,
    materialDisplay: String,
    showAdmin: Boolean,
    adminName: String?,
    onCallClick: (() -> Unit)? = null,
    onClick: () -> Unit,
    onCloneClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Header(
                companyName = companyName,
                indentCode = indentCode,
                postedTime = postedTime,
            )

            Spacer(modifier = Modifier.height(16.dp))

            RouteAndPrices(
                fromCity = fromCity,
                toCity = toCity,
                clientCostFormatted = clientCostFormatted,
                negotiatedCostFormatted = negotiatedCostFormatted,
            )

            CargoChips(
                truckDisplay = truckDisplay,
                weightDisplay = weightDisplay,
                materialDisplay = materialDisplay,
                onCallClick = onCallClick,
            )

            Footer(
                showAdmin = showAdmin,
                adminName = adminName,
                onCloneClick = onCloneClick,
            )
        }
    }
}

@Composable
private fun Header(
    companyName: String,
    indentCode: String?,
    postedTime: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f),
        ) {
            Text(
                text = companyName,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                ),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (indentCode != null) {
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "($indentCode)",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Medium,
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                )
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHighest,
        ) {
            Text(
                text = postedTime,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            )
        }
    }
}

@Composable
private fun RouteAndPrices(
    fromCity: String,
    toCity: String,
    clientCostFormatted: String,
    negotiatedCostFormatted: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        Row(
            verticalAlignment = Alignment.Top,
            modifier = Modifier.weight(1f),
        ) {
            RouteIndicatorWithCities(
                fromCity = fromCity,
                toCity = toCity,
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier.padding(top = 2.dp),
        ) {
            Text(
                text = "CLIENT COST",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = clientCostFormatted,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                ),
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "NEGOTIATED COST",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = negotiatedCostFormatted,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                ),
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun CargoChips(
    truckDisplay: String,
    weightDisplay: String,
    materialDisplay: String,
    onCallClick: (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            AssistChip(
                onClick = { },
                label = {
                    Text(
                        text = truckDisplay,
                        style = MaterialTheme.typography.labelMedium,
                    )
                },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                ),
                border = AssistChipDefaults.assistChipBorder(
                    enabled = false,
                    borderColor = MaterialTheme.colorScheme.outlineVariant,
                ),
                shape = RoundedCornerShape(8.dp),
            )

            AssistChip(
                onClick = { },
                label = {
                    Text(
                        text = weightDisplay,
                        style = MaterialTheme.typography.labelMedium,
                    )
                },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                ),
                border = AssistChipDefaults.assistChipBorder(
                    enabled = false,
                    borderColor = MaterialTheme.colorScheme.outlineVariant,
                ),
                shape = RoundedCornerShape(8.dp),
            )

            if (materialDisplay.isNotBlank()) {
                AssistChip(
                    onClick = { },
                    label = {
                        Text(
                            text = materialDisplay,
                            style = MaterialTheme.typography.labelMedium,
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                    ),
                    border = AssistChipDefaults.assistChipBorder(
                        enabled = false,
                        borderColor = MaterialTheme.colorScheme.outlineVariant,
                    ),
                    shape = RoundedCornerShape(8.dp),
                )
            }
        }

        if (onCallClick != null) {
            Spacer(modifier = Modifier.weight(1f))

            // LIGHTWEIGHT CUSTOM CHIP: 100% predictable sizing, no hidden framework padding
            Row(
                modifier = Modifier
                    .height(32.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(MaterialTheme.colorScheme.primary)
                    .clickable(
                        onClick = onCallClick,
                    )
                    .padding(horizontal = 8.dp), // STRICT: Exact same horizontal padding as AssistChip
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Call,
                    contentDescription = "Call",
                    modifier = Modifier.size(18.dp), // Exact same icon size as AssistChip
                    tint = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Call",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = MaterialTheme.colorScheme.onPrimary,
                        // CRITICAL: Prevents the Text composable from adding hidden vertical padding
                        lineHeight = 16.sp
                    )
                )
            }
        }
    }
}

@Composable
private fun Footer(
    showAdmin: Boolean,
    adminName: String?,
    onCloneClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (showAdmin && adminName != null) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = adminName,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium,
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        if (showAdmin) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilledTonalButton(
                    onClick = onCloneClick,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    ),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ContentCopy,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Clone",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                        ),
                    )
                }
            }
        }
    }
}
