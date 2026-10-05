package com.freight24.trucker.feature.profile.view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.unit.dp

/**
 * One collapsible section: a bordered card whose header and body share a surface.
 *
 * Previously the header was a bare row floating on the page background and the
 * body was a separate elevated card, so the two never read as one unit and the
 * collapsed state left ~82dp of dead space between every pair of headers.
 */
@Composable
internal fun ProfileSection(
    title: String,
    icon: ImageVector,
    badge: SectionBadge? = null,
    badgeText: String? = null,
    badgeTone: StatusTone? = null,
    initiallyExpanded: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    var isExpanded by rememberSaveable(title) { mutableStateOf(initiallyExpanded) }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clickable { isExpanded = !isExpanded }
                        // 48dp minimum target; the row is shorter than that visually.
                        .heightIn(min = 48.dp)
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                        .testTag("profile_section_${title.lowercase().replace(' ', '_')}")
                        // Without this the testTag is invisible to uiautomator and
                        // the row can only be driven by an ephemeral ref.
                        .semantics(mergeDescendants = true) {
                            testTagsAsResourceId = true
                        },
            ) {
                Box(
                    modifier =
                        Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(9.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(18.dp).decorative(),
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                )

                // Exactly one badge per section, from one token set. Badge text
                // and tone are passed separately because Documents reports a
                // count rather than a boolean.
                val resolvedText = badge?.label() ?: badgeText
                val resolvedTone = badge?.tone() ?: badgeTone
                if (resolvedText != null && resolvedTone != null) {
                    StatusChip(text = resolvedText, tone = resolvedTone)
                    Spacer(modifier = Modifier.width(6.dp))
                }

                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp).decorative(),
                )
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut(),
            ) {
                Column {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Column(modifier = Modifier.padding(vertical = 4.dp)) { content() }
                }
            }
        }
    }
}

/** Compact lock affordance explaining why a field cannot be edited. */
@Composable
internal fun LockedTag(reason: String?) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier =
            Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(horizontal = 6.dp, vertical = 2.dp),
    ) {
        Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(11.dp).decorative(),
        )
        Spacer(modifier = Modifier.width(3.dp))
        Text(
            text = reason ?: "Locked",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/**
 * One label/value row, optionally editable.
 *
 * The per-field leading icons are gone. They added 44dp of indent per row to
 * restate the label ("Branch name" carried a clipboard), five fields shared one
 * `Icons.Default.Badge` briefcase glyph, and four different icon sizes coexisted
 * inside a single card.
 *
 * [lockedReason] is shown instead of an input when a verified section makes the
 * field read-only, so the user can tell *why* rather than guessing from a
 * missing text box.
 */
