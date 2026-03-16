package com.freight24.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileSourcePickerSheet(
    showSheet: Boolean,
    onDismissRequest: () -> Unit,
    onCameraClick: () -> Unit,
    onGalleryClick: () -> Unit,
    onDocumentClick: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState()
) {
    if (!showSheet) return

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp),
        ) {
            Text(
                text = "Select Document Source",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(16.dp),
                fontWeight = FontWeight.Bold,
            )

            ListItem(
                headlineContent = { Text("Camera") },
                leadingContent = {
                    Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = CircleShape) {
                        Icon(Icons.Default.CameraAlt, null, modifier = Modifier.padding(8.dp).size(20.dp))
                    }
                },
                modifier = Modifier.clickable { onCameraClick() },
            )
            ListItem(
                headlineContent = { Text("Gallery (Images)") },
                leadingContent = {
                    Surface(color = MaterialTheme.colorScheme.secondaryContainer, shape = CircleShape) {
                        Icon(Icons.Default.PhotoLibrary, null, modifier = Modifier.padding(8.dp).size(20.dp))
                    }
                },
                modifier = Modifier.clickable { onGalleryClick() },
            )
            ListItem(
                headlineContent = { Text("Documents (PDF)") },
                leadingContent = {
                    Surface(color = MaterialTheme.colorScheme.tertiaryContainer, shape = CircleShape) {
                        Icon(Icons.AutoMirrored.Filled.InsertDriveFile, null, modifier = Modifier.padding(8.dp).size(20.dp))
                    }
                },
                modifier = Modifier.clickable { onDocumentClick() },
            )
        }
    }
}
