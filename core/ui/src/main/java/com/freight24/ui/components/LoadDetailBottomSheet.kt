package com.freight24.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Notes
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Height
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.freight24.common.util.RelativeTimeFormatter
import com.freight24.ui.theme.FreightThemeTokens
import com.freight24.ui.theme.monoSpace

// ──────────────────────────────────────────────────────────────────────────────
//  UI model
// ──────────────────────────────────────────────────────────────────────────────

data class LoadDetailLocation(
    val address: String,
    val areaName: String?,
    val cityName: String?,
    val contactPerson: String?,
    val contactPhone: String?,
    val sequenceOrder: Int,
)

data class LoadDetailTruckType(
    val bodyType: String,
    val truckType: String,
    val displayName: String,
)

data class LoadDetailClient(
    val companyName: String?,
    val tagCode: String?,
)

data class LoadDetailF24Agent(
    val name: String,
    val phone: String,
)

data class LoadDetail(
    val indentCode: String,
    val priority: String,
    val createdAt: String,
    val tripPrice: String,
    val hideCostInfo: Boolean,
    val loadWeight: String?,
    val material: String?,
    val commodity: String,
    val truckType: LoadDetailTruckType?,
    val flooringType: String?,
    val vehicleHeight: String?,
    val tarpaulingRequired: Boolean,
    val tatEnabled: Boolean,
    val tatHours: Int?,
    val luIncludedInTripCost: Boolean?,
    val specialRequirements: String?,
    val expiresAt: String,
    val client: LoadDetailClient?,
    val loadingLocation: LoadDetailLocation?,
    val unloadingLocation: LoadDetailLocation?,
    val interestCount: Int,
    val agent: LoadDetailF24Agent?,
    val isInterested: Boolean,
    val isInterestLoading: Boolean,
)

sealed interface LoadDetailState {
    data object Loading : LoadDetailState
    data class Success(val detail: LoadDetail) : LoadDetailState
    data class Error(val message: String) : LoadDetailState
}

// ──────────────────────────────────────────────────────────────────────────────
//  Sheet
// ──────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoadDetailBottomSheet(
    state: LoadDetailState,
    onDismiss: () -> Unit,
    onCallAgent: (phone: String) -> Unit,
    onToggleInterest: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    modifier: Modifier = Modifier,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        dragHandle = { DragHandle() },
        modifier = modifier,
    ) {
        when (state) {
            is LoadDetailState.Loading -> LoadDetailLoading()
            is LoadDetailState.Error -> LoadDetailError(
                message = state.message,
                onClose = onDismiss,
            )
            is LoadDetailState.Success -> LoadDetailContent(
                detail = state.detail,
                onClose = onDismiss,
                onCallAgent = onCallAgent,
                onToggleInterest = onToggleInterest,
            )
        }
    }
}

@Composable
private fun DragHandle() {
    Box(
        modifier = Modifier
            .padding(top = 12.dp, bottom = 4.dp)
            .size(width = 40.dp, height = 4.dp)
            .background(
                color = MaterialTheme.colorScheme.surfaceDim,
                shape = RoundedCornerShape(2.dp),
            ),
    )
}

// ──────────────────────────────────────────────────────────────────────────────
//  Scrollable content
// ──────────────────────────────────────────────────────────────────────────────

@Composable
private fun LoadDetailContent(
    detail: LoadDetail,
    onClose: () -> Unit,
    onCallAgent: (String) -> Unit,
    onToggleInterest: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Header(code = detail.indentCode, onClose = onClose)
        RouteCard(detail = detail)
        SpecsChips(detail = detail)
        if (detail.client?.companyName != null) {
            ShipperCard(companyName = detail.client.companyName)
        }
        if (detail.agent != null) {
            DispatchAgentCard(agent = detail.agent, onCallAgent = onCallAgent)
        }
        EngagementLine(detail = detail)
        ExpressInterestButton(
            isInterested = detail.isInterested,
            isLoading = detail.isInterestLoading,
            onToggle = onToggleInterest,
        )
    }
}

// ──────────────────────────────────────────────────────────────────────────────
//  Header — indent ID chip (mono) + close icon
// ──────────────────────────────────────────────────────────────────────────────

@Composable
private fun Header(
    code: String,
    onClose: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IndentIdChip(code = code)
        Spacer(Modifier.weight(1f))
        IconButton(
            onClick = onClose,
            modifier = Modifier.size(40.dp),
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close details",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun IndentIdChip(code: String) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        shape = RoundedCornerShape(999.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
        ) {
            Text(
                text = code,
                style = MaterialTheme.typography.labelLarge.copy(fontFamily = monoSpace),
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

// ──────────────────────────────────────────────────────────────────────────────
//  Route card — surface-bright, column-style route (LoadCard pattern) + amount chip
// ──────────────────────────────────────────────────────────────────────────────

@Composable
private fun RouteCard(detail: LoadDetail) {
    val fromCity = bestCityLabel(detail.loadingLocation, fallback = "Pickup")
    val toCity = bestCityLabel(detail.unloadingLocation, fallback = "Drop")

    Surface(
        color = MaterialTheme.colorScheme.surfaceBright,
        contentColor = MaterialTheme.colorScheme.onSurface,
        shape = RoundedCornerShape(24.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Route — column-style with indicator on the left, cities stacked.
            ColumnRouteIndicator(
                fromCity = fromCity,
                toCity = toCity,
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 1f))
            if (detail.hideCostInfo) {
                HideCostInline()
            } else {
                AmountChip(priceText = formatCurrency(detail.tripPrice))
            }
        }
    }
}

/**
 * Column-style route indicator that mirrors the LoadCard layout:
 * a vertical "dot-line-dot" on the left, two city names stacked on the right.
 * Long city names wrap onto multiple lines instead of overflowing the row.
 */
@Composable
private fun ColumnRouteIndicator(
    fromCity: String,
    toCity: String,
) {
    Row(verticalAlignment = Alignment.Top) {
        // Indicator column: top dot, gradient line, bottom dot
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 6.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = CircleShape,
                    ),
            )
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(28.dp)
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.error,
                            ),
                        ),
                    ),
            )
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(
                        color = MaterialTheme.colorScheme.error,
                        shape = CircleShape,
                    ),
            )
        }
        Spacer(Modifier.width(12.dp))
        // Stacked city names — wrap on overflow, never truncate.
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(top = 2.dp),
        ) {
            Text(
                text = fromCity,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = toCity,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun AmountChip(priceText: String) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        shape = RoundedCornerShape(999.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
        ) {
            Icon(
                imageVector = Icons.Filled.Payments,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = priceText,
                style = MaterialTheme.typography.titleLarge,
                color = FreightThemeTokens.colors.successColor,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun HideCostInline() {
    Surface(
        color = MaterialTheme.colorScheme.tertiaryContainer,
        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
        shape = RoundedCornerShape(999.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = "Price details not available, contact our agent",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

private fun bestCityLabel(
    location: LoadDetailLocation?,
    fallback: String,
): String = when {
    location == null -> fallback
    !location.cityName.isNullOrBlank() -> location.cityName
    !location.areaName.isNullOrBlank() -> location.areaName
    else -> fallback
}

// ──────────────────────────────────────────────────────────────────────────────
//  Spec chips — surface-variant pills with icons
// ──────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SpecsChips(detail: LoadDetail) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        detail.truckType?.let {
            SpecChip(
                text = it.displayName.ifBlank { it.truckType },
                icon = Icons.Filled.LocalShipping,
            )
        }
        detail.loadWeight?.let {
            SpecChip(text = detail.loadWeight, icon = Icons.Filled.Scale)
        }
        detail.material?.let {
            SpecChip(text = detail.material, icon = Icons.Filled.Category)
        }
        // We can also surface specific items from truckType meta (e.g. body type)
        detail.flooringType?.takeIf { it.isNotBlank() }?.let {
            SpecChip(text = "Flooring ${it.replaceFirstChar(Char::uppercase)}")
        }
        detail.vehicleHeight?.takeIf { it.isNotBlank() }?.let {
            SpecChip(text = "Height $it", icon = Icons.Filled.Height)
        }
    }
}

// ──────────────────────────────────────────────────────────────────────────────
//  Shipper card — primaryContainer avatar + name
// ──────────────────────────────────────────────────────────────────────────────

@Composable
private fun ShipperCard(companyName: String) {
    SectionColumn(label = "Shipper Account") {
        PartnerCard(
            name = companyName,
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            onContainerColor = MaterialTheme.colorScheme.onPrimaryContainer,
            icon = Icons.Filled.Business,
        )
    }
}

@Composable
private fun PartnerCard(
    name: String,
    containerColor: androidx.compose.ui.graphics.Color,
    onContainerColor: androidx.compose.ui.graphics.Color,
    icon: ImageVector,
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceBright,
        contentColor = MaterialTheme.colorScheme.onSurface,
        shape = RoundedCornerShape(20.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AvatarCircle(
                color = containerColor,
                onColor = onContainerColor,
                icon = icon,
            )
            Spacer(Modifier.width(14.dp))
            Text(
                text = name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun AvatarCircle(
    color: androidx.compose.ui.graphics.Color,
    onColor: androidx.compose.ui.graphics.Color,
    icon: ImageVector,
) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .background(color = color, shape = CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = onColor,
            modifier = Modifier.size(22.dp),
        )
    }
}

// ──────────────────────────────────────────────────────────────────────────────
//  F24 Dispatch Agent card — secondaryContainer avatar + name + Call pill
// ──────────────────────────────────────────────────────────────────────────────

@Composable
private fun DispatchAgentCard(
    agent: LoadDetailF24Agent,
    onCallAgent: (String) -> Unit,
) {
    SectionColumn(label = "Freight24 Agent") {
        Surface(
            color = MaterialTheme.colorScheme.surfaceBright,
            contentColor = MaterialTheme.colorScheme.onSurface,
            shape = RoundedCornerShape(20.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AvatarCircle(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    onColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    icon = Icons.Filled.Person,
                )
                Spacer(Modifier.width(14.dp))
                Text(
                    text = agent.name.ifBlank { "Freight24" },
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                Spacer(Modifier.width(12.dp))
                CallPillButton(
                    enabled = agent.phone.isNotBlank() && agent.phone != "--",
                    onClick = { onCallAgent(agent.phone) },
                )
            }
        }
    }
}

@Composable
private fun CallPillButton(
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val container = if (enabled) {
        MaterialTheme.colorScheme.secondaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceContainerHigh
    }
    val onContainer = if (enabled) {
        MaterialTheme.colorScheme.onSecondaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    Surface(
        onClick = onClick,
        enabled = enabled,
        color = container,
        contentColor = onContainer,
        shape = RoundedCornerShape(999.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Filled.Call,
                contentDescription = "Call dispatch agent",
                modifier = Modifier.size(18.dp),
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = "Call",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

// ──────────────────────────────────────────────────────────────────────────────
//  Engagement line — "Expires in" left-aligned, "truckers interested" right-aligned
// ──────────────────────────────────────────────────────────────────────────────

@Composable
private fun EngagementLine(detail: LoadDetail) {
    val expiryText = RelativeTimeFormatter.formatExpiry(detail.expiresAt)
    val isExpired = expiryText == "Expired"
    val hasInterest = detail.interestCount > 0
    val interestText = if (hasInterest) {
        "${detail.interestCount} trucker${if (detail.interestCount == 1) "" else "s"} interested"
    } else {
        "Be the first to express interest"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Left: clock + relative expiry
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.AccessTime,
                contentDescription = null,
                tint = if (isExpired) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                modifier = Modifier.size(16.dp),
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = expiryText,
                style = MaterialTheme.typography.labelLarge,
                color = if (isExpired) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
            )
        }

        Spacer(Modifier.weight(1f))

        // Right: status dot + interest count
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End,
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(
                        color = if (hasInterest) {
                            FreightThemeTokens.colors.successColor
                        } else {
                            MaterialTheme.colorScheme.outlineVariant
                        },
                        shape = CircleShape,
                    ),
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = interestText,
                style = MaterialTheme.typography.labelLarge,
                color = if (hasInterest) {
                    FreightThemeTokens.colors.successColor
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

// ──────────────────────────────────────────────────────────────────────────────
//  Express interest CTA — primaryContainer in both states
// ──────────────────────────────────────────────────────────────────────────────

@Composable
private fun ExpressInterestButton(
    isInterested: Boolean,
    isLoading: Boolean,
    onToggle: () -> Unit,
) {
    val haptics = LocalHapticFeedback.current
    Button(
        onClick = {
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            onToggle()
        },
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp),
        shape = RoundedCornerShape(28.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ),
        enabled = !isLoading,
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                strokeWidth = 2.dp,
            )
        } else {
            Icon(
                imageVector = if (isInterested) Icons.Filled.CheckCircle else Icons.Filled.Verified,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = if (isInterested) "Interest expressed" else "Express interest",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

// ──────────────────────────────────────────────────────────────────────────────
//  Section column (label + content)
// ──────────────────────────────────────────────────────────────────────────────

@Composable
private fun SectionColumn(
    label: String,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.4.sp,
        )
        content()
    }
}

// ──────────────────────────────────────────────────────────────────────────────
//  Spec chip (pill with optional leading icon)
// ──────────────────────────────────────────────────────────────────────────────

@Composable
private fun SpecChip(
    text: String,
    icon: ImageVector? = null,
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        shape = RoundedCornerShape(999.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(Modifier.width(8.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

// ──────────────────────────────────────────────────────────────────────────────
//  States: loading, error
// ──────────────────────────────────────────────────────────────────────────────

@Composable
private fun LoadDetailLoading() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 240.dp)
            .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(12.dp))
            Text(
                text = "Loading indent details…",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun LoadDetailError(
    message: String,
    onClose: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = Icons.Filled.Info,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(40.dp),
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = "Unable to load details",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = message.ifBlank { "Please try again later." },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(20.dp))
        Button(onClick = onClose, shape = MaterialTheme.shapes.large) {
            Text("Close")
        }
    }
}

@Suppress("unused")
private val LoadDetailBottomSheetDefaults = object {
    val ContentPadding = PaddingValues(horizontal = 0.dp, vertical = 0.dp)
}
