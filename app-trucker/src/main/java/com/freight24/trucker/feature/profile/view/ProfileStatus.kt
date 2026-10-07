package com.freight24.trucker.feature.profile.view

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp
import com.freight24.trucker.feature.profile.domain.DocumentStatus
import com.freight24.trucker.feature.profile.domain.DocumentType
import com.freight24.trucker.feature.profile.util.ProfileDocumentTypeIds
import com.freight24.ui.theme.FreightThemeTokens

/**
 * The single status vocabulary for the Profile screen.
 *
 * Previously each call site invented its own chip: `VerificationChip` took an
 * arbitrary [Color] (callers passed `primary`, `warning`, `outline`, `tertiary`
 * for four different meanings) and `ExistingStatusBadge` used a different corner
 * radius. Colour was the only thing carrying meaning and it was inconsistent.
 *
 * A [StatusTone] maps to a documented container pair from
 * [FreightThemeTokens.colors], each guaranteed >= 4.5:1 against its container.
 */
enum class StatusTone {
    /** Requirement satisfied and checked by the backend. */
    VERIFIED,

    /**
     * Not yet satisfied: either nothing was submitted, or it was submitted and
     * is awaiting review. Both are "not done" — the label says which, the colour
     * says how worried to be.
     */
    PENDING,

    /** Required data has not been supplied yet. */
    MISSING,

    /** Attention is needed at section level, but no submission was rejected. */
    WARNING,

    /** Submitted and refused — user must correct and resubmit. */
    REJECTED,

    /** Informational only, nothing pending. */
    NEUTRAL,
}

@Composable
internal fun StatusTone.colors(): Pair<Color, Color> {
    val tokens = FreightThemeTokens.colors
    return when (this) {
        StatusTone.VERIFIED -> tokens.surfaceSuccess to tokens.onSurfaceSuccess
        StatusTone.PENDING ->
            if (isSystemInDarkTheme()) Color(0xFF24445D) to Color(0xFFD3E9FA)
            else Color(0xFFDCECF8) to Color(0xFF174A68)
        StatusTone.MISSING -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
        StatusTone.WARNING -> tokens.surfaceWarning to tokens.onSurfaceWarning
        StatusTone.REJECTED -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
        StatusTone.NEUTRAL -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
    }
}

/**
 * The one status chip used across the Profile screen.
 *
 * Shape, padding and type are fixed here so callers only ever choose a tone and
 * a label.
 */
@Composable
internal fun StatusChip(
    text: String,
    tone: StatusTone,
    modifier: Modifier = Modifier,
) {
    val (container, content) = tone.colors()
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = content,
        maxLines = 1,
        modifier =
            modifier
                .background(container, RoundedCornerShape(StatusShape))
                .padding(horizontal = 8.dp, vertical = 3.dp),
    )
}

private val StatusShape = 6.dp

/**
 * Per-document state, collapsed to the four states a user can act on.
 *
 * [DocumentUploadState.existingVerifyStatus] is the newer source and is cleared
 * the moment a replacement upload starts; [ProfileDocument.status] is the older
 * server copy. Preferring the former preserves current precedence rules while
 * keeping that decision in one place.
 */
internal enum class DocumentTone {
    VERIFIED,
    PENDING,
    MISSING,
    REJECTED,
}

/**
 * Convenience for field rows, which only need the tone for a single document
 * type and have no [ProfileDocument] list in scope.
 */
internal fun documentTone(
    type: DocumentType,
    documentStates: Map<String, DocumentUploadState>,
): DocumentTone {
    val state = documentStates[ProfileDocumentTypeIds.fieldKey(type)]
    return documentTone(state, fallback = null, hasFile = !state?.existingFileUrl.isNullOrBlank())
}

internal fun documentTone(
    state: DocumentUploadState?,
    fallback: DocumentStatus?,
    hasFile: Boolean,
): DocumentTone {
    val remote = state?.existingVerifyStatus?.lowercase()?.trim()
    return when {
        remote == "rejected" -> DocumentTone.REJECTED
        remote == "verified" -> DocumentTone.VERIFIED
        remote == "pending" -> DocumentTone.PENDING
        fallback == DocumentStatus.REJECTED -> DocumentTone.REJECTED
        fallback == DocumentStatus.VERIFIED -> DocumentTone.VERIFIED
        fallback == DocumentStatus.UPLOADED || fallback == DocumentStatus.PENDING -> DocumentTone.PENDING
        hasFile -> DocumentTone.PENDING
        else -> DocumentTone.MISSING
    }
}

internal fun DocumentTone.statusTone(): StatusTone =
    when (this) {
        DocumentTone.VERIFIED -> StatusTone.VERIFIED
        DocumentTone.PENDING -> StatusTone.PENDING
        DocumentTone.MISSING -> StatusTone.MISSING
        DocumentTone.REJECTED -> StatusTone.REJECTED
    }

/** Short label for a document row. */
internal fun DocumentTone.label(): String =
    when (this) {
        DocumentTone.VERIFIED -> "Verified"
        // "In Review", not "Pending": the document is submitted and a human is
        // looking at it. "Pending" read as an indefinite queue state and was
        // easily confused with the separate "Pending save" staged-upload badge.
        DocumentTone.PENDING -> "In Review"
        DocumentTone.MISSING -> "Not uploaded"
        DocumentTone.REJECTED -> "Rejected"
    }

/** True when the user has something to do about this document. */
internal fun DocumentTone.needsAction(): Boolean = this == DocumentTone.MISSING || this == DocumentTone.REJECTED

/**
 * Section-level verification, used only where the backend gives us a real
 * per-section signal.
 *
 * Business Details deliberately has none: the screen previously derived both
 * Business and Documents badges from `documentsVerificationStatus`, which made
 * "Documents: Verified" sit above two "Not Uploaded" rows.
 */
internal enum class SectionBadge {
    VERIFIED,
    UNVERIFIED,
}

internal fun SectionBadge.tone(): StatusTone = if (this == SectionBadge.VERIFIED) StatusTone.VERIFIED else StatusTone.WARNING

internal fun SectionBadge.label(): String = if (this == SectionBadge.VERIFIED) "Verified" else "Unverified"

/**
 * Decorative icon or spacer: hidden from accessibility services.
 *
 * The surrounding row already announces its own meaning, so leaving these
 * exposed produces duplicate, out-of-order announcements.
 */
internal fun Modifier.decorative(): Modifier = clearAndSetSemantics { }
