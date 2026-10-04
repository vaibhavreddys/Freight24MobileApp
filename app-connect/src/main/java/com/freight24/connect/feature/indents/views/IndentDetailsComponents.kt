package com.freight24.connect.feature.indents.views

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.freight24.connect.R
import com.freight24.ui.components.PanelShape
import com.freight24.ui.theme.FreightThemeTokens
import java.text.NumberFormat
import java.util.Locale

// ===========================================================================
// The lodged docket — the details page's clauses.
//
// Same paperwork as Create / Review / Edit, read back instead of written on:
// every section is a numbered clause with a spine down its left edge, and every
// fact inside it is a [LedgerLine] — label, dotted leader, value, one line.
//
// That last part is the point of this file. The page used to spend an icon
// badge, a label and a value on three stacked lines per fact; a ledger line says
// the same thing in one, which is what lets the page carry the branch, the call
// sheet and every specification without turning into a scroll. Nothing was
// dropped to buy the density — only the icons were.
// ===========================================================================

/**
 * One clause of a lodged docket: [ClauseDocket] with a neutral record spine.
 *
 * Not [ClauseState.EMPTY] — that would paint the spine the same colour as the
 * card border. A record clause passes [accent] explicitly; only an error state
 * overrides it.
 */
@Composable
fun IndentDetailSection(
    number: Int,
    title: String,
    testTag: String,
    hasError: Boolean = false,
    trailing: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val scheme = MaterialTheme.colorScheme

    ClauseDocket(
        number = number,
        title = title,
        state = if (hasError) ClauseState.ERROR else ClauseState.EMPTY,
        testTag = testTag,
        contentPadding = ReadoutClausePadding,
        accent = if (hasError) scheme.error else scheme.outline,
        trailing = trailing,
        content = content,
    )
}

// ---------------------------------------------------------------------------
// Metrics
// ---------------------------------------------------------------------------

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
        shape = MaterialTheme.shapes.medium,
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
    val contentColor = if (isVisible) extended.success else MaterialTheme.colorScheme.onSurfaceVariant
    val icon = if (isVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff
    val label = if (isVisible)
        stringResource(R.string.cost_visible)
    else
        stringResource(R.string.cost_hidden)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        modifier = Modifier.semantics { contentDescription = label }
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(15.dp),
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = contentColor,
        )
    }
}

// ---------------------------------------------------------------------------
// Clause 02 — route & financials
// ---------------------------------------------------------------------------

@Composable
fun RouteAndFinancialsSection(
    number: Int,
    originCity: String,
    destinationCity: String,
    clientCost: String,
    negotiatedCost: String,
    hideCost: Boolean,
    isCostVisible: Boolean,
    hasError: Boolean = false,
) {
    val marginAmount = calculateMargin(clientCost, negotiatedCost)
    val hasMargin = marginAmount > 0
    val hasCosts = clientCost.isNotBlank() || negotiatedCost.isNotBlank()

    IndentDetailSection(
        number = number,
        title = stringResource(R.string.route_and_financials),
        testTag = "indent_details_route",
        hasError = hasError,
        trailing = { CostVisibilityIndicator(isVisible = isCostVisible) },
    ) {
        LaneStrip(
            originLabel = stringResource(R.string.location_from),
            originValue = originCity,
            destinationLabel = stringResource(R.string.location_to),
            destinationValue = destinationCity,
            originTestTag = "indent_details_origin",
            destinationTestTag = "indent_details_destination",
        )

        if (hasCosts) {
            Spacer(modifier = Modifier.height(8.dp))
            LedgerLine(
                label = stringResource(R.string.client_cost),
                value = formatInr(clientCost),
            )
            LedgerLine(
                label = stringResource(R.string.target_cost),
                value = formatInr(negotiatedCost),
                emphasis = true,
            )
            if (hasMargin && isCostVisible) {
                LedgerLine(
                    label = stringResource(R.string.margin),
                    value = "₹${formatNumber(marginAmount)}",
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Clause 03 — specifications
// ---------------------------------------------------------------------------

@Composable
fun SpecificationsSection(
    number: Int,
    weight: String,
    weightUnit: String,
    materialType: String,
    truckType: String?,
    truckTypeUnit: String,
    tatRequired: Boolean,
    tatHours: String,
    tarpaulinRequired: Boolean,
) {
    IndentDetailSection(
        number = number,
        title = stringResource(R.string.specifications),
        testTag = "indent_details_specs",
    ) {
        LedgerLine(
            label = stringResource(R.string.truck_type),
            value = truckType?.let { "$it • $truckTypeUnit" },
        )
        LedgerLine(
            label = stringResource(R.string.weight),
            value = "$weight $weightUnit"
        )
        LedgerLine(
            label = stringResource(R.string.material),
            value = titleCase(materialType)
        )
        LedgerLine(
            label = stringResource(R.string.tat),
            value = if (tatRequired)
                "$tatHours hrs"
            else
                stringResource(R.string.not_applicable),
        )
        LedgerLine(
            label = stringResource(R.string.tarpaulin),
            value = if (tarpaulinRequired)
                stringResource(R.string.required)
            else
                stringResource(R.string.not_applicable),
        )
    }
}

// ---------------------------------------------------------------------------
// Clause 04 — remarks
// ---------------------------------------------------------------------------

@Composable
fun RemarksSection(
    number: Int,
    remarks: String,
) {
    val scheme = MaterialTheme.colorScheme

    IndentDetailSection(
        number = number,
        title = stringResource(R.string.remarks),
        testTag = "indent_details_remarks",
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = PanelShape,
            color = scheme.surfaceContainerHighest,
        ) {
            Text(
                text = remarks.takeIf { it.isNotBlank() }
                    ?: stringResource(R.string.no_special_requirements_specified),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (remarks.isNotBlank()) FontWeight.Medium else FontWeight.Normal,
                color = if (remarks.isNotBlank()) scheme.onSurface else scheme.onSurfaceVariant,
                modifier = Modifier.padding(10.dp),
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Formatters
// ---------------------------------------------------------------------------

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
