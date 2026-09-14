package com.freight24.connect.feature.leads.views

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.freight24.ui.components.TagShape
import com.freight24.ui.theme.FreightThemeTokens

// ===========================================================================
// Lead status language.
//
// Every lead status resolves through MaterialTheme / FreightThemeTokens — the
// same rule the verification queue follows (no hardcoded hex). The accent
// colours the card spine, the tally badges on the queue rail and the stamps:
//
//   new                → info teal      (fresh intake, not yet worked)
//   contact_in_progress→ warning amber  (active work in flight)
//   follow_up          → tertiary bronze(scheduled callback)
//   soft_onboarded     → success green  (paperwork gathered)
//   converted          → brand primary  (fully done — the deepest green)
//   not_interested /
//   invalid /
//   do_not_contact     → error          (the lead is closed)
// ===========================================================================

fun leadStatusLabel(status: String): String = when (status) {
    "new" -> "New"
    "contact_in_progress" -> "In Progress"
    "follow_up" -> "Follow Up"
    "soft_onboarded" -> "Soft Onboarded"
    "not_interested" -> "Not Interested"
    "invalid" -> "Invalid"
    "converted" -> "Converted"
    "do_not_contact" -> "DND"
    else -> status.replace('_', ' ')
}

@Composable
fun LeadStatusAccent(status: String): Color {
    val ext = FreightThemeTokens.colors
    val scheme = MaterialTheme.colorScheme
    return when (status) {
        "new" -> ext.info
        "contact_in_progress" -> ext.warning
        "follow_up" -> scheme.tertiary
        "soft_onboarded" -> ext.success
        "converted" -> scheme.primary
        "not_interested", "invalid", "do_not_contact" -> scheme.error
        else -> scheme.onSurfaceVariant
    }
}

/** Container/content pair for the status stamp, guaranteed-contrast tokens. */
@Composable
fun LeadStatusStampColors(status: String): Pair<Color, Color> {
    val ext = FreightThemeTokens.colors
    val scheme = MaterialTheme.colorScheme
    return when (status) {
        "new" -> ext.surfaceInfo to ext.onSurfaceInfo
        "contact_in_progress" -> ext.surfaceWarning to ext.onSurfaceWarning
        "follow_up" -> scheme.tertiaryContainer to scheme.onTertiaryContainer
        "soft_onboarded" -> ext.surfaceSuccess to ext.onSurfaceSuccess
        "converted" -> scheme.primaryContainer to scheme.onPrimaryContainer
        "not_interested", "invalid", "do_not_contact" -> scheme.errorContainer to scheme.onErrorContainer
        else -> scheme.surfaceContainerHighest to scheme.onSurfaceVariant
    }
}

/**
 * Rubber-stamp verdict for a lead. Shown on list cards only when the active
 * tab mixes statuses (same rule as the verification queue: the tab chip
 * already states the state for single-status tabs).
 */
@Composable
fun LeadStatusStamp(status: String, modifier: Modifier = Modifier) {
    val (container, content) = LeadStatusStampColors(status)
    val glyph = when (status) {
        "converted" -> Icons.Default.Check
        "not_interested", "invalid", "do_not_contact" -> Icons.Default.Close
        else -> null
    }

    Surface(
        modifier = modifier,
        shape = TagShape,
        color = container,
    ) {
        Row(
            modifier = Modifier
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
                text = leadStatusLabel(status).uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                fontWeight = FontWeight.Bold,
                color = content,
            )
        }
    }
}

/** Accent for an activity-timeline entry, keyed to the action type. */
@Composable
fun LeadActivityAccent(actionType: String): Color {
    val ext = FreightThemeTokens.colors
    val scheme = MaterialTheme.colorScheme
    return when (actionType) {
        "converted" -> ext.success
        "call_outcome" -> ext.warning
        "marked_invalid" -> scheme.error
        "assigned" -> ext.info
        "imported", "details_updated" -> scheme.primary
        "reopened" -> scheme.tertiary
        else -> scheme.onSurfaceVariant
    }
}
