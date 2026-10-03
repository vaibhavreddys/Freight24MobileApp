package com.freight24.trucker.feature.trucks.view.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.content.Context
import kotlinx.coroutines.flow.asSharedFlow
import android.graphics.BitmapFactory
import android.graphics.pdf.PdfDocument
import android.net.Uri
import com.freight24.common.model.TruckDetail
import com.freight24.trucker.feature.trucks.domain.usecase.FetchTruckDetailParams
import com.freight24.trucker.feature.trucks.domain.usecase.FetchTruckDetailUseCase
import com.freight24.trucker.feature.trucks.domain.usecase.GetTruckDocumentSignedUrlParams
import com.freight24.trucker.feature.trucks.domain.usecase.GetTruckDocumentSignedUrlUseCase
import com.freight24.trucker.feature.trucks.domain.usecase.UpdateTruckDocumentsParams
import com.freight24.trucker.feature.trucks.domain.usecase.UpdateTruckDocumentsUseCase
import com.freight24.ui.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

@HiltViewModel
internal class TruckDetailsViewModel
    @Inject
    constructor(
        private val fetchTruckDetailUseCase: FetchTruckDetailUseCase,
        private val updateTruckDocumentsUseCase: UpdateTruckDocumentsUseCase,
        private val getTruckDocumentSignedUrlUseCase: GetTruckDocumentSignedUrlUseCase,
        @ApplicationContext private val context: Context,
        savedStateHandle: SavedStateHandle,
    ) : ViewModel() {
        private val truckId: String = checkNotNull(savedStateHandle["truckId"])

        private val _uiState = MutableStateFlow<UiState<TruckDetail>>(UiState.Loading)
        val uiState: StateFlow<UiState<TruckDetail>> = _uiState.asStateFlow()

        private val _isRefreshing = MutableStateFlow(false)
        val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

        private val _isUploading = MutableStateFlow(false)
        val isUploading: StateFlow<Boolean> = _isUploading.asStateFlow()

        private val _selectedUris = MutableStateFlow<List<Uri>>(emptyList())
        val selectedUris: StateFlow<List<Uri>> = _selectedUris.asStateFlow()

        private val _isPdf = MutableStateFlow(false)
        val isPdf: StateFlow<Boolean> = _isPdf.asStateFlow()

        private val _currentDocumentType = MutableStateFlow<String?>(null)
        val currentDocumentType: StateFlow<String?> = _currentDocumentType.asStateFlow()

        private val _uploadSuccess = kotlinx.coroutines.flow.MutableSharedFlow<Unit>()
        val uploadSuccess = _uploadSuccess.asSharedFlow()

        private val _errorEvent = kotlinx.coroutines.flow.MutableSharedFlow<String>()
        val errorEvent = _errorEvent.asSharedFlow()

        init {
            fetchTruckDetail()
        }

        fun fetchTruckDetail(forceRefresh: Boolean = false) {
            viewModelScope.launch {
                if (forceRefresh) {
                    _isRefreshing.value = true
                } else {
                    _uiState.value = UiState.Loading
                }

                fetchTruckDetailUseCase(FetchTruckDetailParams(truckId))
                    .onSuccess { detail ->
                        _uiState.value = UiState.Success(detail)
                    }
                    .onFailure { error ->
                        _uiState.value = UiState.Error(error.message ?: "Failed to fetch truck details")
                    }

                _isRefreshing.value = false
            }
        }

        fun onRefresh() {
            fetchTruckDetail(forceRefresh = true)
        }

        fun onFilesPicked(uris: List<Uri>, pdf: Boolean, documentType: String) {
            _selectedUris.value = uris
            _isPdf.value = pdf
            _currentDocumentType.value = documentType
        }

        fun clearSelection() {
            _selectedUris.value = emptyList()
            _isPdf.value = false
            _currentDocumentType.value = null
        }

        fun uploadDocuments() {
            val uris = _selectedUris.value
            val docType = _currentDocumentType.value ?: return
            if (uris.isEmpty()) return

            viewModelScope.launch {
                _isUploading.value = true
                try {
                    val file = if (_isPdf.value) {
                        getFileFromUri(uris.first(), docType)
                    } else {
                        convertImagesToPdf(uris, docType)
                    }

                    if (file != null) {
                        val requestFile = file.asRequestBody("application/pdf".toMediaTypeOrNull())
                        val partName = when (docType.lowercase()) {
                            "rc book" -> "rc_card"
                            "insurance policy" -> "insurance"
                            "fitness certificate" -> "fc_certificate"
                            else -> "document"
                        }
                        val body = MultipartBody.Part.createFormData(partName, file.name, requestFile)

                        val params = when (partName) {
                            "rc_card" -> UpdateTruckDocumentsParams(truckId, rcCard = body)
                            "insurance" -> UpdateTruckDocumentsParams(truckId, insurance = body)
                            "fc_certificate" -> UpdateTruckDocumentsParams(truckId, fcCertificate = body)
                            else -> UpdateTruckDocumentsParams(truckId)
                        }

                        updateTruckDocumentsUseCase(params)
                            .onSuccess {
                                fetchTruckDetail(forceRefresh = true)
                                clearSelection()
                                _uploadSuccess.emit(Unit)
                            }
                            .onFailure { error ->
                                _errorEvent.emit(error.message ?: "Upload failed")
                            }
                    }
                } catch (e: Exception) {
                    _errorEvent.emit(e.message ?: "Upload error")
                } finally {
                    _isUploading.value = false
                }
            }
        }

        private suspend fun getFileFromUri(uri: Uri, docType: String): File? = withContext(Dispatchers.IO) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val file = File(context.cacheDir, "temp_${docType.replace(" ", "_")}.pdf")
                val outputStream = FileOutputStream(file)
                inputStream?.use { input ->
                    outputStream.use { output ->
                        input.copyTo(output)
                    }
                }
                file
            } catch (e: Exception) {
                null
            }
        }

        private suspend fun convertImagesToPdf(uris: List<Uri>, docType: String): File? = withContext(Dispatchers.IO) {
            try {
                val pdfDocument = PdfDocument()
                uris.forEachIndexed { index, uri ->
                    val inputStream = context.contentResolver.openInputStream(uri)
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    if (bitmap != null) {
                        val pageInfo = PdfDocument.PageInfo.Builder(bitmap.width, bitmap.height, index + 1).create()
                        val page = pdfDocument.startPage(pageInfo)
                        page.canvas.drawBitmap(bitmap, 0f, 0f, null)
                        pdfDocument.finishPage(page)
                        bitmap.recycle()
                    }
                    inputStream?.close()
                }

                val file = File(context.cacheDir, "temp_${docType.replace(" ", "_")}_converted.pdf")
                val outputStream = FileOutputStream(file)
                pdfDocument.writeTo(outputStream)
                pdfDocument.close()
                outputStream.close()
                file
            } catch (e: Exception) {
                null
            }
        }

        fun fetchSignedUrl(documentType: String, onUrlReady: (String) -> Unit) {
            viewModelScope.launch {
                getTruckDocumentSignedUrlUseCase(GetTruckDocumentSignedUrlParams(truckId, documentType))
                    .onSuccess { url ->
                        onUrlReady(url)
                    }
                    .onFailure { error ->
                        // Show error or handle fail
                    }
            }
        }
    }
