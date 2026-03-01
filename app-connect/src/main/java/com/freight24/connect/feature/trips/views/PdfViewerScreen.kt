package com.freight24.connect.feature.trips.views

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.freight24.connect.feature.trips.domain.viewmodel.PdfViewerEvent
import com.freight24.connect.feature.trips.domain.viewmodel.PdfViewerViewModel
import com.freight24.ui.components.PdfRendererView
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PdfViewerScreen(
    onBackClick: () -> Unit,
    onSuccess: () -> Unit,
    viewModel: PdfViewerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // Navigate back when approval/rejection is successful
    LaunchedEffect(uiState.shouldNavigateBack) {
        if (uiState.shouldNavigateBack) {
            onSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lorry Receipt", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Show approval status if already approved/verified
                    if (uiState.isApproved || uiState.documentStatus?.lowercase() == "approved" || uiState.documentStatus?.lowercase() == "verified") {
                        Text(
                            "Approved",
                            modifier = Modifier.padding(end = 16.dp),
                            color = Color(0xFF16A34A),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    } else if (uiState.documentStatus?.lowercase() == "rejected") {
                        Text(
                            "Rejected",
                            modifier = Modifier.padding(end = 16.dp),
                            color = Color(0xFFDC2626),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        floatingActionButton = {
            // Show FABs only if document is pending and not yet approved/rejected
            if (uiState.documentStatus?.lowercase() == "pending" && !uiState.isApproved && uiState.documentStatus?.lowercase() != "rejected") {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Reject FAB
                    FloatingActionButton(
                        onClick = { viewModel.onEvent(PdfViewerEvent.ShowRejectDialog) },
                        containerColor = Color(0xFFDC2626),
                        contentColor = Color.White
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.Close, contentDescription = "Reject")
                            Text("Reject", fontWeight = FontWeight.Bold)
                        }
                    }
                    
                    // Approve FAB
                    FloatingActionButton(
                        onClick = { viewModel.onEvent(PdfViewerEvent.ShowApproveDialog) },
                        containerColor = Color(0xFF16A34A),
                        contentColor = Color.White
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Check, contentDescription = "Approve")
                            Text("Approve", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        },
        floatingActionButtonPosition = FabPosition.End,
        containerColor = Color.White
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator()
                }
                uiState.errorMessage != null -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(uiState.errorMessage!!, color = Color.Red)
                        Button(onClick = { viewModel.onEvent(PdfViewerEvent.LoadPdf) }) {
                            Text("Retry")
                        }
                    }
                }
                uiState.pdfFile != null -> {
                    PdfRendererView(source = com.rajat.pdfviewer.util.PdfSource.LocalFile(uiState.pdfFile!!))
                }
            }
        }
        
        // Approve Dialog
        if (uiState.showApproveDialog) {
            RemarksDialog(
                title = "Approve LR",
                remarksValue = uiState.remarksInput,
                onRemarksChange = { viewModel.onEvent(PdfViewerEvent.UpdateRemarks(it)) },
                onDismiss = { viewModel.onEvent(PdfViewerEvent.DismissDialog) },
                onSubmit = { viewModel.onEvent(PdfViewerEvent.ApproveLr) },
                isLoading = uiState.isApproving
            )
        }
        
        // Reject Dialog
        if (uiState.showRejectDialog) {
            RemarksDialog(
                title = "Reject LR",
                remarksValue = uiState.remarksInput,
                onRemarksChange = { viewModel.onEvent(PdfViewerEvent.UpdateRemarks(it)) },
                onDismiss = { viewModel.onEvent(PdfViewerEvent.DismissDialog) },
                onSubmit = { viewModel.onEvent(PdfViewerEvent.RejectLr) },
                isLoading = uiState.isApproving
            )
        }
    }
}

@Composable
fun RemarksDialog(
    title: String,
    remarksValue: String,
    onRemarksChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSubmit: () -> Unit,
    isLoading: Boolean
) {
    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "Enter remarks: *",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = remarksValue,
                    onValueChange = onRemarksChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Enter your remarks here...") },
                    minLines = 3,
                    maxLines = 5,
                    enabled = !isLoading,
                    isError = remarksValue.isBlank(),
                    supportingText = if (remarksValue.isBlank()) {
                        { Text("Remarks are required", color = MaterialTheme.colorScheme.error) }
                    } else null
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onSubmit,
                enabled = !isLoading && remarksValue.isNotBlank()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(Modifier.width(8.dp))
                }
                Text("Submit")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isLoading
            ) {
                Text("Cancel")
            }
        }
    )
}
