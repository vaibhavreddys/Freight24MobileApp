package com.freight24.ui.components

// import androidx.compose.material.icons.default.LocalShipping
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.freight24.ui.theme.monoSpace

@Composable
fun LoadCard(
    sourceCity: String,
    destinationCity: String,
    postedTime: String,
    price: String,
    loadWeight: String,
    truckInfo: String,
    commodity: String,
    f24AgentName: String,
    isBookmarked: Boolean = false,
    isBookmarkLoading: Boolean = false,
    onCallClick: () -> Unit,
    onBookmarkClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // ───── Route + Price ─────
            Row(
                verticalAlignment = Alignment.Top,
                modifier = Modifier.fillMaxWidth(),
            ) {
                RouteIndicatorWithCities(
                    fromCity = sourceCity,
                    toCity = destinationCity,
                )

                Spacer(modifier = Modifier.weight(1f))

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = postedTime,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = formatCurrency(price),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            Divider(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                thickness = 1.dp,
            )

            // ───── Meta Info ─────
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.LocalShipping,
                    contentDescription = "Delivery truck",
                    tint = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = truckInfo,
                    style = MaterialTheme.typography.bodyMedium.copy(fontFamily = monoSpace),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "|",
                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = monoSpace),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = loadWeight,
                    style = MaterialTheme.typography.bodyMedium.copy(fontFamily = monoSpace),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "|",
                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = monoSpace),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = commodity,
                    style = MaterialTheme.typography.bodyMedium.copy(fontFamily = monoSpace),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            // ───── Footer ─────
            Spacer(Modifier.height(6.dp))

            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier =
                        Modifier
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                            .fillMaxWidth(),
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Delivery truck",
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = f24AgentName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )

                    Spacer(Modifier.weight(1f))

                    FooterIconButton(
                        icon = Icons.Default.Call,
                        tooltipText = "Call $f24AgentName",
                        contentDescription = "Call $f24AgentName",
                        onClick = onCallClick,
                    )

                    Spacer(Modifier.width(8.dp))

                    FooterIconButton(
                        icon = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        tooltipText = "Interested",
                        contentDescription = "Mark as interested",
                        highlighted = isBookmarked,
                        enabled = !isBookmarkLoading,
                        onClick = onBookmarkClick,
                    )
                }
            }
        }
    }
}

fun formatCurrency(amount: String?): String {
    if (amount.isNullOrBlank() || amount == "null") return "₹ 0"

    return try {
        // Remove any existing currency symbols/spaces and parse
        val numericValue = amount.replace("[₹,\\s]".toRegex(), "").toDoubleOrNull()

        if (numericValue != null) {
            "₹ ${"%,.0f".format(numericValue)}"
        } else {
            "₹ 0" // Fallback for invalid input
        }
    } catch (e: Exception) {
        "₹ 0" // Fallback for any parsing errors
    }
}

