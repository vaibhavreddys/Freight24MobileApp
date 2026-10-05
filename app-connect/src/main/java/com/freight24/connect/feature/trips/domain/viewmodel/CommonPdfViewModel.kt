package com.freight24.connect.feature.trips.domain.viewmodel

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject

data class CommonPdfUiState(
    val isLoading: Boolean = false,
    val pdfFile: File? = null,
    val errorMessage: String? = null,
    val title: String = "Document",
)

@HiltViewModel
class CommonPdfViewModel
    @Inject
    constructor(
        @param:ApplicationContext private val context: Context,
        savedStateHandle: SavedStateHandle,
    ) : ViewModel() {
        private val url: String? = savedStateHandle["url"]
        private val localPath: String? = savedStateHandle["localPath"]
        private val title: String = savedStateHandle["title"] ?: "Document"

        private val _uiState = MutableStateFlow(CommonPdfUiState(title = title))
        val uiState: StateFlow<CommonPdfUiState> = _uiState.asStateFlow()

        init {
            loadPdf()
        }

        private fun loadPdf() {
            if (!localPath.isNullOrBlank() && localPath != "null") {
                val file = File(localPath)
                if (file.exists()) {
                    _uiState.update { it.copy(pdfFile = file, isLoading = false) }
                } else if (!url.isNullOrBlank() && url != "null") {
                    downloadPdf(url)
                } else {
                    _uiState.update { it.copy(errorMessage = "File not found and no URL provided") }
                }
            } else if (!url.isNullOrBlank() && url != "null") {
                downloadPdf(url)
            } else {
                _uiState.update { it.copy(errorMessage = "No valid PDF source provided") }
            }
        }

        private fun downloadPdf(pdfUrl: String) {
            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }
                try {
                    withContext(Dispatchers.IO) {
                        val connection = URL(pdfUrl).openConnection() as HttpURLConnection
                        connection.connect()

                        if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                            throw Exception("Server returned HTTP ${connection.responseCode}")
                        }

                        val inputStream = connection.inputStream
                        val file = File(context.cacheDir, "temp_common_pdf_${System.currentTimeMillis()}.pdf")
                        val outputStream = FileOutputStream(file)

                        inputStream.use { input ->
                            outputStream.use { output ->
                                input.copyTo(output)
                            }
                        }

                        _uiState.update { it.copy(isLoading = false, pdfFile = file) }
                    }
                } catch (e: Exception) {
                    _uiState.update { it.copy(isLoading = false, errorMessage = "Failed to download PDF: ${e.message}") }
                }
            }
        }

        fun retry() = loadPdf()
    }
