package com.freight24.trucker.feature.profile.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.freight24.ui.trucker.TruckerColors
import com.freight24.ui.trucker.rememberTruckerColors

/**
 * Identity header.
 *
 * Now an inverted (brand-filled) block rather than a white card on a near-white
 * background: the old card (#F7F9F4) sat three values off the page background
 * (#F4F6F1), so elevation shadow was doing all the separation work. Inverting
 * also gives the screen a single clear anchor and lets the name sit on one line.
 *
 * Filled with [TruckerColors.brand] rather than Material's `primary`, which is
 * what the Trips filter rail selects with — the two used to disagree, so the
 * header and the tab rail read as different brands.
 */
@Composable
internal fun ProfileHeaderCard(
    fullName: String,
    partnerId: String,
    initials: String,
    role: String,
    isVerified: Boolean,
    isActive: Boolean,
    businessName: String? = null,
) {
    val colors = rememberTruckerColors()
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = colors.brand,
        contentColor = colors.onBrand,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier =
                        Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(colors.onBrand.copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = initials,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = colors.onBrand,
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = profileHeaderName(businessName, fullName),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = colors.onBrand,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = role,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onBrand.copy(alpha = 0.75f),
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Partner ID is monospaced so it can be read aloud or transcribed
            // reliably — it is the reference support asks for.
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                HeaderPill(
                    text = if (isActive) "Active" else "Inactive",
                    container = colors.onBrand.copy(alpha = if (isActive) 0.18f else 0.10f),
                    content =
                        if (isActive) {
                            colors.onBrand
                        } else {
                            MaterialTheme.colorScheme.error
                        },
                )
                if (isVerified) {
                    HeaderPill(
                        text = "ID verified",
                        container = colors.onBrand.copy(alpha = 0.18f),
                        content = colors.onBrand,
                    )
                }
                if (partnerId.isNotBlank()) {
                    HeaderPill(
                        text = partnerId,
                        container = colors.onBrand.copy(alpha = 0.10f),
                        content = colors.onBrand.copy(alpha = 0.85f),
                    )
                }
            }
        }
    }
}

internal fun profileHeaderName(businessName: String?, fullName: String): String =
    businessName?.trim()?.takeIf { it.isNotEmpty() }
        ?: fullName.trim().ifBlank { "Member" }

@Composable
private fun HeaderPill(
    text: String,
    container: Color,
    content: Color,
) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = content,
        maxLines = 1,
        modifier =
            Modifier
                .background(container, RoundedCornerShape(6.dp))
                .padding(horizontal = 8.dp, vertical = 3.dp),
    )
}

/**
 * Document-completion meter.
 *
 * The single answer to "how much of my KYC is done?", which the previous screen
 * never gave: it showed four independent Verified/Unverified chips with no
 * denominator, and the Documents badge was derived from a flag that disagreed
 * with the rows underneath it.
 */
@Composable
internal fun ProfileCompletionMeter(completion: ProfileCompletion) {
    if (completion.total == 0) return

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Documents uploaded",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = "${completion.uploaded} of ${completion.total}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Neutral track. It was tinted from the warning surface, which in
            // dark mode read as a muddy olive stripe rather than "remaining".
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
            ) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth(completion.percent / 100f)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(MaterialTheme.colorScheme.primary),
                )
            }

            if (!completion.isComplete) {
                Spacer(modifier = Modifier.height(8.dp))
                // onSurfaceVariant, not onWarning: bare amber text on the plain
                // surface fell below a readable contrast in dark mode, and the
                // count is already carried by the meter.
                Text(
                    text = "${completion.missing} still needed to receive payouts",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
