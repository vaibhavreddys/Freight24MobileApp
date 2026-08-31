package com.freight24.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * The four tones for [BoundaryBadge] — each maps to a Material color pair.
 *
 *  - [Info]:    `tertiary` / `onTertiary`   (non-urgent counts)
 *  - [Urgent]:  `error`   / `onError`       (action-needed counts)
 *  - [Muted]:   `surfaceVariant` / `onSurfaceVariant` (zero state)
 */
enum class BoundaryBadgeTone { Info, Urgent, Muted }

/**
 * How to render the numeric label inside [BoundaryBadge].
 *
 *  - [Plain]:  plain decimal (0‑99 → "0".."99", >99 → "99+").
 *  - [Padded]: two-digit "jersey" style (0‑9 → "00".."09").
 */
enum class BadgeFormatStyle { Plain, Padded }

/**
 * M3 boundary-breaking corner badge: a compact stadium pill that sits on top
 * of the top-end corner of a card via a parent [Box] +
 * `Modifier.align(TopEnd).offset(x = 12.dp, y = -12.dp)`.
 *
 * Tokens (colors, elevation, ring, typography) are fully encapsulated —
 * callers only choose the [tone] and [formatStyle].
 *
 * @param count  The integer to display (0..99 verbatim, >99 → "99+").
 * @param modifier  Applied to the badge root (caller typically adds
 *   `Modifier.align(Alignment.TopEnd).offset(x = 12.dp, y = (-12).dp)`).
 * @param tone  Colour scheme: [Info], [Urgent], or [Muted]. A count of 0
 *   always renders as [Muted] — a quiet zero badge instead of hiding.
 * @param minSize  Minimum badge diameter (default 24.dp; grows with text).
 * @param formatStyle  [Plain] decimal or [Padded] two-digit style.
 */
@Composable
fun BoundaryBadge(
    count: Int,
    modifier: Modifier = Modifier,
    tone: BoundaryBadgeTone = BoundaryBadgeTone.Info,
    minSize: Dp = 24.dp,
    formatStyle: BadgeFormatStyle = BadgeFormatStyle.Plain,
) {
    val colorScheme = MaterialTheme.colorScheme
    // Zero state: a muted pill reads as "nothing to act on" rather than a
    // loud Info/Urgent count — so count 0 always downgrades to Muted.
    val effectiveTone = if (count == 0) BoundaryBadgeTone.Muted else tone
    val (containerColor, contentColor) = when (effectiveTone) {
        BoundaryBadgeTone.Info -> colorScheme.tertiary to colorScheme.onTertiary
        BoundaryBadgeTone.Urgent -> colorScheme.error to colorScheme.onError
        BoundaryBadgeTone.Muted -> colorScheme.surfaceVariant to colorScheme.onSurfaceVariant
    }
    val stadium = RoundedCornerShape(100.dp)

    Surface(
        // Border first = outermost, so the ring sits between the badge fill and
        // the card underneath, creating the "cutout" boundary-breaking illusion.
        modifier = modifier
            .border(2.dp, colorScheme.surface, stadium)
            .semantics { contentDescription = "$count notifications" },
        shape = stadium,
        color = containerColor,
        contentColor = contentColor,
        shadowElevation = 3.dp,
    ) {
        Box(
            modifier = Modifier
                .widthIn(min = minSize)
                .heightIn(min = minSize)
                .padding(horizontal = 6.dp),
            contentAlignment = Alignment.Center,
        ) {
            AnimatedContent(
                targetState = count,
                transitionSpec = {
                    (fadeIn(animationSpec = tween(160)) togetherWith fadeOut(animationSpec = tween(120)))
                        .using(SizeTransform(clip = false))
                },
                label = "BoundaryBadgeCount",
            ) { c ->
                Text(
                    text = formatCount(c, formatStyle),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

private fun formatCount(count: Int, formatStyle: BadgeFormatStyle): String = when {
    count > 99 -> "99+"
    formatStyle == BadgeFormatStyle.Padded -> count.toString().padStart(2, '0')
    else -> count.toString()
}