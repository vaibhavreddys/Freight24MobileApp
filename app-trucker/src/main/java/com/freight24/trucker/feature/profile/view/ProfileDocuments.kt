package com.freight24.trucker.feature.profile.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.freight24.trucker.feature.profile.domain.DocumentType
import com.freight24.trucker.feature.profile.domain.ProfileDocument
import com.freight24.trucker.feature.profile.util.ProfileDocumentTypeIds

/**
 * The evidence tray: every document the partner is expected to supply.
 *
 * This stays a dedicated section rather than being folded into the field rows
 * because it is a checklist with a per-document state machine (upload progress,
 * retry, rejection) and a single aggregate count. Field rows show the value and
 * a read-only status hint; they are not upload surfaces.
 *
 * In edit mode a row can still be *viewed* — previously the row click and the
 * trailing icon both opened the picker, making preview unreachable while editing.
 */
@Composable
internal fun DocumentList(
    documentTypes: List<DocumentType>,
    documents: List<ProfileDocument>,
    documentStates: Map<String, DocumentUploadState>,
    isEditing: Boolean,
    onDocumentView: (String) -> Unit,
    onDocumentUpload: (DocumentType) -> Unit,
    previewingDocumentId: String? = null,
) {
    Column {
        documentTypes.forEachIndexed { index, type ->
            val fieldKey = ProfileDocumentTypeIds.fieldKey(type)
            val uploadState = documentStates[fieldKey]
            val doc = documents.find { it.type == type }
            val existingUrl = uploadState?.existingFileUrl ?: doc?.url
            val uploadStatus = uploadState?.uploadStatus ?: UploadStatus.IDLE
            val tone = documentTone(uploadState, doc?.status, !existingUrl.isNullOrEmpty())
            val canView = !doc?.docId.isNullOrBlank() && !existingUrl.isNullOrEmpty()

            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clickable(enabled = previewingDocumentId == null) {
                            when {
                                isEditing -> onDocumentUpload(type)
                                canView -> onDocumentView(doc!!.docId!!)
                                else -> onDocumentUpload(type)
                            }
                        }
                        .heightIn(min = 48.dp)
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier =
                        Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(9.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = getDocIcon(type),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(17.dp).decorative(),
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = type.displayName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )

                Spacer(modifier = Modifier.width(8.dp))

                when {
                    uploadStatus == UploadStatus.UPLOADING -> {
                        androidx.compose.material3.LinearProgressIndicator(
                            modifier = Modifier.width(72.dp),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        )
                    }
                    // A staged file is not on the server yet — saying "Verified"
                    // here would claim a state the backend has not granted.
                    uploadStatus == UploadStatus.DONE -> StatusChip("Pending save", StatusTone.PENDING)
                    uploadStatus == UploadStatus.ERROR -> StatusChip("Upload failed", StatusTone.REJECTED)
                    else -> StatusChip(tone.label(), tone.statusTone())
                }

                Spacer(modifier = Modifier.width(6.dp))

                if (previewingDocumentId != null && previewingDocumentId == doc?.docId) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                    )
                } else {
                    Icon(
                        imageVector =
                            when {
                                uploadStatus == UploadStatus.ERROR -> Icons.Default.Refresh
                                canView -> Icons.AutoMirrored.Filled.KeyboardArrowRight
                                // Outlined variants only: the filled cloud sat next to
                                // outlined chevrons at double the visual weight, which
                                // made the action look more important than the row.
                                else -> Icons.Outlined.CloudUpload
                            },
                        contentDescription =
                            when {
                                uploadStatus == UploadStatus.ERROR -> "Retry upload"
                                canView -> "View ${type.displayName}"
                                else -> "Upload ${type.displayName}"
                            },
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }

            if (index < documentTypes.lastIndex) {
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant,
                    modifier = Modifier.padding(start = 56.dp),
                )
            }
        }
    }
}

private fun getDocIcon(type: DocumentType) =
    when (type) {
        DocumentType.PAN -> Icons.Default.CreditCard
        DocumentType.AADHAAR -> Icons.Default.Fingerprint
        DocumentType.GST_CERTIFICATE -> Icons.Default.Description
        DocumentType.COMPANY_REGISTRATION -> Icons.Default.Business
        DocumentType.TDS_CERTIFICATE -> Icons.Default.Assignment
        DocumentType.CANCELLED_CHEQUE -> Icons.Default.AccountBalance
        else -> Icons.Default.Description
    }

/**
 * Section badge for Documents.
 *
 * Says "uploaded", never "verified": [ProfileCompletion] counts files, not
 * review outcomes, so claiming verification here would be the same class of lie
 * as the old `documentsVerificationStatus` badge.
 */
internal fun documentsBadge(completion: ProfileCompletion): String =
    when {
        completion.total == 0 -> "None required"
        completion.missing > 0 -> "${completion.missing} of ${completion.total} missing"
        else -> "${completion.total} uploaded"
    }
