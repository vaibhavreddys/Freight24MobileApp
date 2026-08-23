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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Route

import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Scale
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.TextButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.freight24.ui.components.RouteIndicatorWithCities
import com.freight24.ui.theme.FreightThemeTokens
import java.text.NumberFormat
import java.util.Locale

@Composable
fun IndentDetailSection(
    image: ImageVector? = null,
    title: String,
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
    onViewProfileClick: (() -> Unit)? = null,
) {
    val colorScheme = MaterialTheme.colorScheme
    val extended = FreightThemeTokens.colors
    val phone = clientPhone?.takeIf { it.isNotBlank() }
    val context = LocalContext.current
    val displayTagCode = tagCode?.takeIf { it.isNotBlank() } ?: "—"

    IndentDetailSection(
        image = Icons.Default.Person,
        title = "CLIENT",
        trailing = {
            Surface(
                shape = RoundedCornerShape(50),
                color = colorScheme.surfaceVariant.copy(alpha = 0.6f),
            ) {
                Text(
                    text = displayTagCode,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                )
            }
        },
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = clientName,
                style = MaterialTheme.typography.titleLarge,
                color = colorScheme.onSurface,
                modifier = Modifier.fillMaxWidth(),
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (phone != null) {
                    val formattedNumber = formatPhoneNumber(phone)
                    Button(
                        onClick = {
                            context.startActivity(
                                Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                            )
                        },
                        modifier = Modifier.semantics { contentDescription = "Call $formattedNumber" },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = extended.success,
                            contentColor = extended.onSuccess,
                        ),
                        shape = RoundedCornerShape(32.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Call",
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }
                } else {
                    Text(
                        text = "Not available",
                        style = MaterialTheme.typography.titleMedium,
                        color = colorScheme.onSurfaceVariant,
                    )
                }

                if (onViewProfileClick != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Spacer(modifier = Modifier.width(32.dp))
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.clickable { onViewProfileClick() },
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Text(
                                    text = "View profile",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = colorScheme.onSurfaceVariant,
                                )
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
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
fun IndentStatusChip(status: String) {
    val colorScheme = MaterialTheme.colorScheme
    val extended = FreightThemeTokens.colors
    val (backgroundColor, contentColor) =
        when (status.uppercase()) {
            "OPEN" -> extended.surfaceSuccess to extended.onSurfaceSuccess
            "READY_FOR_ASSIGNMENT" -> colorScheme.primaryContainer to colorScheme.onPrimaryContainer
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
fun CostVisibilityIndicator(
    isVisible: Boolean,
) {
    val extended = FreightThemeTokens.colors
    val backgroundColor = if (isVisible) extended.surfaceSuccess else MaterialTheme.colorScheme.surfaceVariant
    val contentColor = if (isVisible) extended.onSurfaceSuccess else MaterialTheme.colorScheme.onSurfaceVariant
    val icon = if (isVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff
    val label = if (isVisible) "Cost visible" else "Cost hidden"

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.semantics { contentDescription = label }
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(16.dp),
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            color = contentColor,
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
    isCostVisible: Boolean,
    hasError: Boolean = false,
) {
    val extended = FreightThemeTokens.colors
    val marginAmount = calculateMargin(clientCost, negotiatedCost)
    val hasMargin = marginAmount > 0

    IndentDetailSection(
        image = Icons.Default.Route,
        title = "ROUTE & FINANCIALS",
        hasError = hasError,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            RouteIndicatorWithCities(
                fromCity = originCity,
                toCity = destinationCity,
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
                        label = "CLIENT BUDGET",
                        value = if (isCostVisible) formatInr(clientCost) else "₹ ••••",
                        modifier = Modifier.weight(1f)
                    )
                    CostTile(
                        label = "NEGOTIATED COST",
                        value = if (isCostVisible) formatInr(negotiatedCost) else "₹ ••••",
                        emphasized = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CostVisibilityIndicator(isVisible = isCostVisible)

                    if (hasMargin && isCostVisible) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = extended.surfaceSuccess,
                        ) {
                            Text(
                                text = "Margin ₹${formatNumber(marginAmount)}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = extended.onSurfaceSuccess,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SpecificationsSection(
    weight: String,
    weightUnit: String,
    materialType: String,
    truckType: String?,
    truckTypeUnit: String,
    tatRequired: Boolean,
    tatHours: String,
    tarpaulinRequired: Boolean,
) {
    val colorScheme = MaterialTheme.colorScheme

    IndentDetailSection(
        image = Icons.Outlined.Tune,
        title = "SPECIFICATIONS",
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
            val rows = listOf(
                SpecRowData(
                    icon = Icons.Outlined.LocalShipping,
                    label = "Truck Type",
                    value = if (truckType != null) titleCase(truckType) + " • $truckTypeUnit" else "Not selected",
                    iconTint = FreightThemeTokens.colors.info,
                    subdued = truckType == null,
                ),
                SpecRowData(
                    icon = Icons.Outlined.Scale,
                    label = "Weight",
                    value = "$weight $weightUnit",
                    iconTint = FreightThemeTokens.colors.success,
                ),
                SpecRowData(
                    icon = Icons.Outlined.Inventory2,
                    label = "Material",
                    value = titleCase(materialType),
                    iconTint = FreightThemeTokens.colors.warning,
                ),
                BooleanSpecRowData(
                    label = "TAT mentioned",
                    description = if (tatRequired) "$tatHours hrs" else null,
                    isEnabled = tatRequired,
                ),
                BooleanSpecRowData(
                    label = "Tarpaulin required",
                    description = null,
                    isEnabled = tarpaulinRequired,
                ),
            )

            rows.forEachIndexed { index, rowData ->
                when (rowData) {
                    is SpecRowData -> SpecRow(
                        icon = rowData.icon,
                        label = rowData.label,
                        value = rowData.value,
                        iconTint = rowData.iconTint,
                        subdued = rowData.subdued,
                    )
                    is BooleanSpecRowData -> BooleanSpecRow(
                        label = rowData.label,
                        description = rowData.description,
                        isEnabled = rowData.isEnabled,
                    )
                }

                if (index < rows.lastIndex) {
                    HorizontalDivider(
                        thickness = 0.5.dp,
                        color = colorScheme.outlineVariant,
                        modifier = Modifier.padding(vertical = 10.dp),
                    )
                }
            }
        }
    }
}

private data class SpecRowData(
    val icon: ImageVector,
    val label: String,
    val value: String,
    val iconTint: Color = Color.Unspecified,
    val subdued: Boolean = false,
)

private data class BooleanSpecRowData(
    val label: String,
    val description: String?,
    val isEnabled: Boolean,
)

@Composable
fun RemarksSection(
    remarks: String,
) {
    IndentDetailSection(
        image = Icons.Outlined.Tune,
        title = "REMARKS",
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHighest,
        ) {
            Text(
                text = remarks.takeIf { it.isNotBlank() } ?: "No special requirements specified.",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = if (remarks.isNotBlank()) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(10.dp)
            )
        }
    }
}

@Composable
private fun SpecRow(
    icon: ImageVector,
    label: String,
    value: String,
    iconTint: Color = Color.Unspecified,
    subdued: Boolean = false,
) {
    val colorScheme = MaterialTheme.colorScheme
    val extended = FreightThemeTokens.colors
    val resolvedTint = if (iconTint != Color.Unspecified) iconTint else extended.success
    val valueColor = if (subdued) colorScheme.onSurfaceVariant else colorScheme.onSurface

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            modifier = Modifier.size(28.dp),
            shape = CircleShape,
            color = resolvedTint.copy(alpha = 0.12f),
            contentColor = resolvedTint,
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
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium,
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
private fun BooleanSpecRow(
    label: String,
    description: String?,
    isEnabled: Boolean,
) {
    val colorScheme = MaterialTheme.colorScheme
    val extended = FreightThemeTokens.colors
    val tint = if (isEnabled) extended.success else colorScheme.onSurfaceVariant

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            modifier = Modifier.size(28.dp),
            shape = CircleShape,
            color = tint.copy(alpha = 0.12f),
            contentColor = tint,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = if (isEnabled) Icons.Default.Check else Icons.Default.Close,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f),
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End,
        ) {
            if (description != null) {
                Text(
                    text = description,
                    style = MaterialTheme.typography.titleMedium,
                    color = colorScheme.onSurface,
                    modifier = Modifier.padding(end = 4.dp),
                )
            }
            Surface(
                shape = RoundedCornerShape(50),
                color = tint.copy(alpha = 0.12f),
            ) {
                Text(
                    text = if (isEnabled) "Yes" else "No",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = tint,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
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
        format.format(number)
    } else {
        "₹$amount"
    }
}

private fun formatNumber(amount: Double): String {
    return "%,.0f".format(amount)
}

private fun calculateMargin(clientCost: String, negotiatedCost: String): Double {
    val client = clientCost.replace(",", "").toDoubleOrNull() ?: 0.0
    val negotiated = negotiatedCost.replace(",", "").toDoubleOrNull() ?: 0.0
    return if (client > 0 && negotiated > 0) client - negotiated else 0.0
}

private fun titleCase(text: String): String {
    return text.lowercase()
        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
}

private fun formatPhoneNumber(phone: String): String {
    val digits = phone.replace(Regex("[^0-9]"), "")
    return when {
        digits.length == 10 -> "+91 ${digits.substring(0, 5)} ${digits.substring(5)}"
        digits.length == 12 && digits.startsWith("91") -> "+91 ${digits.substring(2, 7)} ${digits.substring(7)}"
        digits.length == 13 && digits.startsWith("91") -> "+91 ${digits.substring(3, 8)} ${digits.substring(8)}"
        else -> phone
    }
}

private fun formatCreatedAt(isoTimestamp: String): String {
    return try {
        val digits = isoTimestamp.replace(Regex("[^0-9]"), "")
        if (digits.length >= 14) {
            val year = digits.substring(0, 4)
            val month = digits.substring(4, 6).toIntOrNull() ?: 1
            val day = digits.substring(6, 8).toIntOrNull() ?: 1
            val hour = digits.substring(8, 10).toIntOrNull() ?: 0
            val minute = digits.substring(10, 12).toIntOrNull() ?: 0
            val amPm = if (hour < 12) "AM" else "PM"
            val displayHour = when {
                hour == 0 -> 12
                hour > 12 -> hour - 12
                else -> hour
            }
            val monthNames = arrayOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
            "${monthNames[month - 1]} $day, $year at $displayHour:${"%02d".format(minute)} $amPm"
        } else {
            isoTimestamp
        }
    } catch (e: Exception) {
        isoTimestamp
    }
}
