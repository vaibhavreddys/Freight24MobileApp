package com.freight24.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.freight24.ui.theme.FreightThemeTokens
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
    // Tonal layering: surfaceContainerHigh outer over the screen surface gives
    // depth without shadow; the footer drops to surfaceContainerLowest as an
    // inset action well. All roles resolve through colorScheme — no hex.
    val extended = FreightThemeTokens.colors
    Card(
        modifier =
            modifier
                .fillMaxWidth()
                .clickable { onClick() },
        shape = MaterialTheme.shapes.large,
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        // Box to allow overlaying tooltip
        Box(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                // ───── Route + Price ─────
                // Price is the dominant region (truckers scan earnings first):
                // a primaryContainer chip anchors the top-right while the route
                // keeps the left. Posted recency sits above it as a quiet pill.
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
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceContainerHighest,
                            shape = MaterialTheme.shapes.small,
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            ) {
                                Box(
                                    modifier =
                                        Modifier
                                            .size(6.dp)
                                            .background(
                                                color = extended.success,
                                                shape = CircleShape,
                                            ),
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = postedTime,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                        // A backend amount that formats to ₹0 (blank, "--", or
                        // numeric 0) means "no usable price" — hide the chip
                        // entirely. The detail sheet carries the agent note.
                        val formattedPrice = remember(price) { formatCurrency(price) }
                        if (formattedPrice != "₹0") {
                            Spacer(Modifier.height(8.dp))
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = MaterialTheme.shapes.medium,
                            ) {
                                Text(
                                    text = formattedPrice,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
                    thickness = 1.dp,
                )

                // ───── Meta Info ─────
                // Dot-separated single row scans faster than pipes and stays
                // legible at body scale; truck spec leads, commodity trails
                // with ellipsis so long names never push the row off-screen.
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.LocalShipping,
                        contentDescription = "Delivery truck",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = truckInfo,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(Modifier.width(8.dp))
                    MetaDot()
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = loadWeight,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(Modifier.width(8.dp))
                    MetaDot()
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = commodity,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                }

                // ───── Footer ─────
                // Inset action well: agent identity (initial avatar + name)
                // left, bookmark + Call right — primary action stays
                // thumb-reachable in the card's lower half.
                Spacer(Modifier.height(12.dp))

                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainerLowest,
                    shape = MaterialTheme.shapes.medium,
                    border =
                        BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                        ),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier =
                            Modifier
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                                .fillMaxWidth(),
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier =
                                Modifier
                                    .size(36.dp)
                                    .background(
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        shape = CircleShape,
                                    ),
                        ) {
                            Text(
                                text = f24AgentName.trim().firstOrNull()?.uppercase() ?: "–",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                        }
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = f24AgentName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f),
                        )

                        // Bookmark button with click handler that shows tooltip
                        FooterIconButton(
                            icon = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Mark as interested",
                            highlighted = isBookmarked,
                            enabled = !isBookmarkLoading,
                            modifier =
                                Modifier
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
                            modifier =
                                Modifier
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
                modifier =
                    Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = (-20).dp, y = (-70).dp)
                        .pointerInput(Unit) {},
            )
        }
    }
}

@Composable
private fun MetaDot() {
    Box(
        modifier =
            Modifier
                .size(4.dp)
                .background(
                    color = MaterialTheme.colorScheme.outline,
                    shape = CircleShape,
                ),
    )
}

fun formatCurrency(amount: String?): String {
    if (amount.isNullOrBlank() || amount == "null") return "₹0"
    return try {
        // Clean the string: remove common currency prefixes and separators
        val cleaned =
            amount.replace("Rs.", "", ignoreCase = true)
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
