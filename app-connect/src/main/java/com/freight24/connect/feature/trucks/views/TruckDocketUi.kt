package com.freight24.connect.feature.trucks.views

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.freight24.ui.components.DocketCard
import com.freight24.ui.components.PerforationRule
import com.freight24.ui.components.TagShape
import com.freight24.ui.theme.FreightThemeTokens

// ===========================================================================
// Truck docket language — shared by the Manage Trucks list and the truck
// details page. The spine is keyed to the truck's paper status:
//
//   blacklisted            → error red
//   documents unverified   → warning amber
//   verified               → success green
//
// Availability ("on trip") is a separate fact and gets its own stamp only
// when the truck is not plainly available — an all-green tray of identical
// ACTIVE pills is noise, not information.
// ===========================================================================

@Composable
fun truckStatusAccent(
    isVerified: Boolean,
    isBlacklisted: Boolean,
): Color {
    val ext = FreightThemeTokens.colors
    val scheme = MaterialTheme.colorScheme
    return when {
        isBlacklisted -> scheme.error
        !isVerified -> ext.warning
        else -> ext.success
    }
}

private fun truckAvailabilityLabel(availability: String?): String? {
    val value = availability?.trim()?.uppercase() ?: return null
    return when {
        value == "BLACKLISTED" -> null // spine + blacklist stamp already say it
        value in setOf("ACTIVE", "AVAILABLE", "") -> null // the quiet default
        value == "ON_TRIP" -> "ON TRIP"
        else -> value.replace('_', ' ')
    }
}

/** Container/content pair for a truck stamp, guaranteed-contrast tokens. */
@Composable
private fun truckStampColors(kind: TruckStampKind): Pair<Color, Color> {
    val ext = FreightThemeTokens.colors
    val scheme = MaterialTheme.colorScheme
    return when (kind) {
        TruckStampKind.VERIFIED -> ext.surfaceSuccess to ext.onSurfaceSuccess
        TruckStampKind.VERIFICATION_PENDING -> ext.surfaceWarning to ext.onSurfaceWarning
        TruckStampKind.BLACKLISTED -> scheme.errorContainer to scheme.onErrorContainer
        TruckStampKind.ON_TRIP -> ext.surfaceInfo to ext.onSurfaceInfo
    }
}

private enum class TruckStampKind { VERIFIED, VERIFICATION_PENDING, BLACKLISTED, ON_TRIP }

/** Rubber-stamp mark with a Check / Close glyph where the verdict is binary. */
@Composable
private fun TruckStamp(
    kind: TruckStampKind,
    modifier: Modifier = Modifier,
) {
    val (container, content) = truckStampColors(kind)
    val glyph =
        when (kind) {
            TruckStampKind.VERIFIED -> Icons.Default.Check
            TruckStampKind.BLACKLISTED -> Icons.Default.Close
            else -> null
        }
    val text =
        when (kind) {
            TruckStampKind.VERIFIED -> "VERIFIED"
            TruckStampKind.VERIFICATION_PENDING -> "VERIFICATION PENDING"
            TruckStampKind.BLACKLISTED -> "BLACKLISTED"
            TruckStampKind.ON_TRIP -> "ON TRIP"
        }

    Surface(
        modifier = modifier,
        shape = TagShape,
        color = container,
    ) {
        Row(
            modifier =
                Modifier
                    .height(22.dp)
                    .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (glyph != null) {
                Icon(
                    imageVector = glyph,
                    contentDescription = null,
                    modifier = Modifier.size(12.dp),
                    tint = content,
                )
                Spacer(Modifier.width(4.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                fontWeight = FontWeight.Bold,
                color = content,
            )
        }
    }
}

/**
 * One truck as a docket. In the tray the whole card opens the truck; on the
 * details page it is the static header ([onClick] = null, no Review cue).
 */
@Composable
fun TruckDocketCard(
    registrationNumber: String,
    truckType: String?,
    availability: String?,
    isVerified: Boolean,
    isBlacklisted: Boolean,
    testTag: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    val scheme = MaterialTheme.colorScheme

    DocketCard(
        accent = truckStatusAccent(isVerified, isBlacklisted),
        testTag = testTag,
        onClick = onClick,
    ) {
        Text(
            text = registrationNumber,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = scheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = truckType?.takeIf { it.isNotBlank() } ?: "Type not set",
            style = MaterialTheme.typography.bodyMedium,
            color = scheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        Spacer(Modifier.height(12.dp))
        PerforationRule()
        Spacer(Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val availabilityStamp = truckAvailabilityLabel(availability)
            if (isBlacklisted) {
                TruckStamp(kind = TruckStampKind.BLACKLISTED)
                Spacer(Modifier.width(8.dp))
            }
            if (availabilityStamp != null) {
                TruckStamp(kind = TruckStampKind.ON_TRIP)
                Spacer(Modifier.width(8.dp))
            }
            TruckStamp(
                kind = if (isVerified) TruckStampKind.VERIFIED else TruckStampKind.VERIFICATION_PENDING,
            )
            if (onClick != null) {
                Spacer(Modifier.weight(1f))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Review",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = scheme.primary,
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = scheme.primary,
                    )
                }
            }
        }
    }
}
