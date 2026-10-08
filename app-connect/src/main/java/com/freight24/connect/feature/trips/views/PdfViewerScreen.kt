package com.freight24.connect.feature.trips.views
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.freight24.connect.R
import com.freight24.connect.feature.trips.domain.viewmodel.PdfViewerEvent
import com.freight24.connect.feature.trips.domain.viewmodel.PdfViewerViewModel
import com.freight24.ui.components.FreightTopAppBar
import com.freight24.ui.components.PdfRendererView
import com.freight24.ui.theme.FreightThemeTokens
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PdfViewerScreen(
    onBackClick: () -> Unit,
    onSuccess: () -> Unit,
    viewModel: PdfViewerViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val colorScheme = MaterialTheme.colorScheme
    val extendedColors = FreightThemeTokens.colors
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val isLrDocument = uiState.documentType.equals("lr", ignoreCase = true)
    val isEpodDocument = uiState.documentType.equals("epod", ignoreCase = true)
    val shareTitle =
        stringResource(
            when {
                isLrDocument -> R.string.trip_share_lr
                isEpodDocument -> R.string.trip_share_epod
                else -> R.string.trip_share_loading_memo
            },
        )

    // Navigate back when approval/rejection is successful
    LaunchedEffect(uiState.shouldNavigateBack) {
        if (uiState.shouldNavigateBack) {
            onSuccess()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Column {
                FreightTopAppBar(
                    title = {
                        Text(
                            stringResource(
                                when {
                                    isLrDocument -> R.string.trip_lr_preview_title
                                    isEpodDocument -> R.string.trip_epod_preview_title
                                    else -> R.string.trip_loading_memo
                                },
                            ),
                            fontWeight = FontWeight.Bold,
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        TooltipBox(
                            positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
                            tooltip = { PlainTooltip { Text(shareTitle) } },
                            state = rememberTooltipState(),
                        ) {
                            IconButton(
                                enabled = uiState.pdfFile != null && !uiState.isLoading,
                                onClick = {
                                    uiState.pdfFile?.let { file ->
                                        if (shareTripDocumentPdf(context, file, shareTitle).isFailure) {
                                            scope.launch {
                                                snackbarHostState.showSnackbar(context.getString(R.string.trip_share_document_failed))
                                            }
                                        }
                                    }
                                },
                            ) {
                                Icon(Icons.Outlined.Share, contentDescription = shareTitle)
                            }
                        }
                        // Show approval status if already approved/verified
                        if (uiState.isApproved || uiState.documentStatus?.lowercase() == "approved" || uiState.documentStatus?.lowercase() == "verified") {
                            Text(
                                "Approved",
                                modifier = Modifier.padding(end = 16.dp),
                                color = extendedColors.success,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                            )
                        } else if (uiState.documentStatus?.lowercase() == "rejected") {
                            Text(
                                "Rejected",
                                modifier = Modifier.padding(end = 16.dp),
                                color = colorScheme.error,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    },
                )
                Surface(color = colorScheme.surfaceContainerLow) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.client),
                            style = MaterialTheme.typography.labelMedium,
                            color = colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text =
                                uiState.clientName ?: stringResource(
                                    if (uiState.isLoadingClient) {
                                        R.string.trip_review_client_loading
                                    } else {
                                        R.string.trip_review_client_unavailable
                                    },
                                ),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = colorScheme.onSurface,
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            // Show FABs only if document is pending and not yet approved/rejected
            if (isLrDocument && uiState.documentStatus?.lowercase() == "pending" && !uiState.isApproved) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    // Reject FAB
                    FloatingActionButton(
                        onClick = { viewModel.onEvent(PdfViewerEvent.ShowRejectDialog) },
                        containerColor = colorScheme.error,
                        contentColor = colorScheme.onError,
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(Icons.Filled.Close, contentDescription = "Reject")
                            Text("Reject", fontWeight = FontWeight.Bold)
                        }
                    }

                    // Approve FAB
                    FloatingActionButton(
                        onClick = { viewModel.onEvent(PdfViewerEvent.ShowApproveDialog) },
                        containerColor = extendedColors.success,
                        contentColor = colorScheme.onPrimary,
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(Icons.Default.Check, contentDescription = "Approve")
                            Text("Approve", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        },
        floatingActionButtonPosition = FabPosition.End,
        containerColor = colorScheme.background,
    ) { padding ->
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding),
            contentAlignment = Alignment.Center,
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator()
                }
                uiState.errorMessage != null -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(uiState.errorMessage!!, color = colorScheme.error)
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
        if (isLrDocument && uiState.showApproveDialog) {
            RemarksDialog(
                title = "Approve LR",
                remarksValue = uiState.remarksInput,
                onRemarksChange = { viewModel.onEvent(PdfViewerEvent.UpdateRemarks(it)) },
                onDismiss = { viewModel.onEvent(PdfViewerEvent.DismissDialog) },
                onSubmit = { viewModel.onEvent(PdfViewerEvent.ApproveLr) },
                isLoading = uiState.isApproving,
            )
        }

        // Reject Dialog
        if (isLrDocument && uiState.showRejectDialog) {
            RemarksDialog(
                title = "Reject LR",
                remarksValue = uiState.remarksInput,
                onRemarksChange = { viewModel.onEvent(PdfViewerEvent.UpdateRemarks(it)) },
                onDismiss = { viewModel.onEvent(PdfViewerEvent.DismissDialog) },
                onSubmit = { viewModel.onEvent(PdfViewerEvent.RejectLr) },
                isLoading = uiState.isApproving,
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
    isLoading: Boolean,
) {
    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    "Enter remarks: *",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                    supportingText =
                        if (remarksValue.isBlank()) {
                            { Text("Remarks are required", color = MaterialTheme.colorScheme.error) }
                        } else {
                            null
                        },
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onSubmit,
                enabled = !isLoading && remarksValue.isNotBlank(),
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp,
                    )
                    Spacer(Modifier.width(8.dp))
                }
                Text("Submit")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isLoading,
            ) {
                Text("Cancel")
            }
        },
    )
}
