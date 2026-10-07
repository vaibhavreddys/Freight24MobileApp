package com.freight24.ui.components

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.rememberAsyncImagePainter
import com.freight24.common.util.DocumentUploadPolicy
import com.freight24.ui.theme.FreightThemeTokens
import java.util.Locale

private const val LARGE_FILE_THRESHOLD_BYTES = DocumentUploadPolicy.MAX_FILE_SIZE_BYTES

private data class FileMeta(
    val name: String?,
    val sizeBytes: Long?,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileConfirmationSheet(
    selectedUris: List<Uri>,
    isPdf: Boolean,
    isUploading: Boolean = false,
    uploadButtonText: String = "Upload",
    onUploadClick: () -> Unit,
    onDismiss: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(),
    progressText: String? = null,
) {
    if (selectedUris.isEmpty()) return

    val context = LocalContext.current
    val firstFileMeta =
        remember(selectedUris) {
            selectedUris.firstOrNull()?.let { uri -> resolveFileMeta(context, uri) }
        }

    val tooManyImages = !isPdf && selectedUris.size > DocumentUploadPolicy.MAX_IMAGES
    val oversizedPdf = isPdf && (firstFileMeta?.sizeBytes ?: 0L) > LARGE_FILE_THRESHOLD_BYTES

    ModalBottomSheet(
        onDismissRequest = { if (!isUploading) onDismiss() },
        sheetState = sheetState,
        sheetGesturesEnabled = !isUploading,
        properties = ModalBottomSheetProperties(shouldDismissOnBackPress = !isUploading),
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = if (isPdf) "PDF Selected" else "Images Selected (${selectedUris.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (isPdf) {
                val fileName = firstFileMeta?.name ?: "document.pdf"
                val sizeBytes = firstFileMeta?.sizeBytes
                val isLarge = (sizeBytes ?: 0L) > LARGE_FILE_THRESHOLD_BYTES

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer,
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Description,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.size(16.dp),
                            )
                            Text(
                                text = fileName,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                maxLines = 1,
                            )
                        }
                    }

                    sizeBytes?.let {
                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = if (isLarge) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.tertiaryContainer,
                        ) {
                            Text(
                                text = formatFileSize(it),
                                style = MaterialTheme.typography.labelMedium,
                                color = if (isLarge) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            )
                        }
                    }
                }

                if (isLarge) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(com.freight24.ui.R.string.upload_pdf_too_large),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    items(selectedUris) { uri ->
                        Box {
                            Image(
                                painter = rememberAsyncImagePainter(uri),
                                contentDescription = null,
                                modifier =
                                    Modifier
                                        .size(100.dp)
                                        .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop,
                            )
                        }
                    }
                }
                if (selectedUris.size > 1) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Will be combined into a PDF (${selectedUris.size} pages)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            if (tooManyImages) {
                Text(
                    text = stringResource(com.freight24.ui.R.string.upload_too_many_images, DocumentUploadPolicy.MAX_IMAGES),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (isUploading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                Text(
                    text = progressText ?: stringResource(com.freight24.ui.R.string.upload_processing),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp, bottom = 16.dp),
                )
            }

            Button(
                onClick = onUploadClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = FreightThemeTokens.colors.success),
                enabled = !isUploading && !tooManyImages && !oversizedPdf,
            ) {
                if (isUploading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                    Text(
                        text = stringResource(com.freight24.ui.R.string.upload_please_wait),
                        modifier = Modifier.padding(start = 8.dp),
                    )
                } else {
                    Text(uploadButtonText, color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

private fun resolveFileMeta(
    context: Context,
    uri: Uri,
): FileMeta {
    var name: String? = null
    var size: Long? = null

    context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE), null, null, null)
        ?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIdx = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (nameIdx >= 0) name = cursor.getString(nameIdx)
                if (sizeIdx >= 0 && !cursor.isNull(sizeIdx)) size = cursor.getLong(sizeIdx)
            }
        }

    return FileMeta(name = name, sizeBytes = size)
}

private fun formatFileSize(bytes: Long): String {
    val kb = bytes / 1024.0
    if (kb < 1024) return String.format(Locale.US, "%.0f KB", kb)
    val mb = kb / 1024.0
    return String.format(Locale.US, "%.1f MB", mb)
}
