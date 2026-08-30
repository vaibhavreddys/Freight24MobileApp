package com.freight24.ui.components

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.freight24.ui.theme.monoSpace
import kotlinx.coroutines.delay

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
    onClick: () -> Unit = {},
) {
    var showThankYouTooltip by remember { mutableStateOf(false) }
    val haptics = LocalHapticFeedback.current

    // Auto-dismiss tooltip
    LaunchedEffect(showThankYouTooltip) {
        if (showThankYouTooltip) {
            delay(1500)
            showThankYouTooltip = false
        }
    }

    // Outer Card receives taps that fall outside the inner action buttons.
    // The FooterIconButton and PrimaryCallButton consume their own touches
    // first (they are themselves clickable), so the Card's onClick only
    // fires for taps on the route, divider, and meta rows.
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
//            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        // Box to allow overlaying tooltip
        Box(modifier = Modifier.fillMaxWidth()) {
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
                        if (price.isNotBlank() && price != "null" && price != "--") {
                            Spacer(Modifier.height(16.dp))
                            Text(
                                text = formatCurrency(price),
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                            )
                        }
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
                    color = MaterialTheme.colorScheme.surface,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                            .fillMaxWidth(),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Agent",
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = f24AgentName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )

                        Spacer(Modifier.weight(1f))

                        // Bookmark button with click handler that shows tooltip
                        FooterIconButton(
                            icon = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Mark as interested",
                            highlighted = isBookmarked,
                            enabled = !isBookmarkLoading,
                            modifier = Modifier
                                .testTag("load_bookmark_btn")
                                .semantics { testTagsAsResourceId = true },
                            onClick = {
                                // Only show tooltip when bookmarking (not unbookmarking)
                                if (!isBookmarked) {
                                    showThankYouTooltip = true
                                }
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                onBookmarkClick()
                            },
                        )

                        Spacer(Modifier.width(8.dp))
                        PrimaryCallButton(
                            label = "Call",
                            contentDescription = "Call $f24AgentName",
                            modifier = Modifier
                                .testTag("load_call_btn")
                                .semantics { testTagsAsResourceId = true },
                            onClick = onCallClick,
                        )
                    }
                }
            }
            // Floating tooltip overlay
            BookmarkThankYouTooltip(
                visible = showThankYouTooltip,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = (-20).dp, y = (-70).dp)
                    .pointerInput(Unit) {}
            )
        }
    }
}

fun formatCurrency(amount: String?): String {
    if (amount.isNullOrBlank() || amount == "null") return "₹0"
    return try {
        // Clean the string: remove common currency prefixes and separators
        val cleaned = amount.replace("Rs.", "", ignoreCase = true)
            .replace("Rs", "", ignoreCase = true)
            .replace("[₹,\\s]".toRegex(), "")

        // Extract the numeric part (digits and at most one decimal point)
        val numericRegex = Regex("[0-9]+(?:\\.[0-9]+)?")
        val match = numericRegex.find(cleaned)

        if (match != null) {
            val numericValue = match.value.toDouble()
            val formattedValue = "%,.0f".format(numericValue)
            // Preserve any suffix (e.g., "/ton") that was in the cleaned string
            val suffix = cleaned.substringAfter(match.value)
            "₹$formattedValue$suffix"
        } else {
            "₹0"
        }
    } catch (_: Exception) {
        "₹0"
    }
}

