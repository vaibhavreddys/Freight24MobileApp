package com.freight24.connect.feature.trips.views
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.freight24.connect.feature.trips.domain.viewmodel.CommonPdfViewModel
import com.freight24.ui.components.FreightTopAppBar
import com.freight24.ui.components.PdfRendererView
import com.rajat.pdfviewer.util.PdfSource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommonPdfViewerScreen(
    onBackClick: () -> Unit,
    viewModel: CommonPdfViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val colorScheme = MaterialTheme.colorScheme

    Scaffold(
        topBar = {
            FreightTopAppBar(
                title = { Text(uiState.title, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
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
                        Text(uiState.errorMessage!!, color = colorScheme.error, modifier = Modifier.padding(16.dp))
                        Button(onClick = { viewModel.retry() }) {
                            Text("Retry")
                        }
                    }
                }
                uiState.pdfFile != null -> {
                    PdfRendererView(source = PdfSource.LocalFile(uiState.pdfFile!!))
                }
            }
        }
    }
}
