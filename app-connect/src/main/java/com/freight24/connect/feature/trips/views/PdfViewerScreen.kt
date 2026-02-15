package com.freight24.connect.feature.trips.views

import android.annotation.SuppressLint
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.freight24.connect.feature.trips.domain.viewmodel.PdfViewerEvent
import com.freight24.connect.feature.trips.domain.viewmodel.PdfViewerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PdfViewerScreen(
    onBackClick: () -> Unit,
    viewModel: PdfViewerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

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
                    if (uiState.documentStatus?.lowercase() == "pending" && !uiState.isApproved) {
                        if (uiState.isApproving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp).padding(end = 16.dp),
                                color = MaterialTheme.colorScheme.primary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            TextButton(
                                onClick = { viewModel.onEvent(PdfViewerEvent.ApproveLr) },
                                colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF16A34A))
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Approve", fontWeight = FontWeight.Bold)
                            }
                        }
                    } else if (uiState.isApproved || uiState.documentStatus?.lowercase() == "approved" || uiState.documentStatus?.lowercase() == "verified") {
                        Text(
                            "Approved",
                            modifier = Modifier.padding(end = 16.dp),
                            color = Color(0xFF16A34A),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
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
                uiState.pdfUrl != null -> {
                    PdfWebView(url = uiState.pdfUrl!!)
                }
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun PdfWebView(url: String) {
    // Basic Google Docs Viewer for PDF viewing in WebView
    val viewerUrl = "https://docs.google.com/viewer?url=$url&embedded=true"
    
    AndroidView(
        factory = { context ->
            WebView(context).apply {
                webViewClient = WebViewClient()
                settings.javaScriptEnabled = true
                loadUrl(viewerUrl)
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}
