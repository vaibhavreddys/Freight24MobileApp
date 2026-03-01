package com.freight24.trucker.feature.profile.view

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freight24.trucker.feature.profile.domain.Profile
import com.freight24.trucker.feature.profile.domain.usecase.FetchProfileUseCase
import com.freight24.ui.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import java.io.File
import java.io.FileOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import android.graphics.BitmapFactory
import android.graphics.pdf.PdfDocument
import android.net.Uri

@HiltViewModel
internal class ProfileViewModel
    @Inject
    constructor(
        private val fetchProfileUseCase: FetchProfileUseCase,
        private val updateProfileUseCase: com.freight24.trucker.feature.profile.domain.usecase.UpdateProfileUseCase,
        private val uploadIdentityDocumentUseCase: com.freight24.trucker.feature.profile.domain.usecase.UploadIdentityDocumentUseCase,
        private val updateIdentityDocumentUseCase: com.freight24.trucker.feature.profile.domain.usecase.UpdateIdentityDocumentUseCase,
        private val getProfileSignedUrlUseCase: com.freight24.trucker.feature.profile.domain.usecase.GetProfileSignedUrlUseCase,
        private val sessionManager: com.freight24.auth.SessionManager,
        @dagger.hilt.android.qualifiers.ApplicationContext private val context: android.content.Context,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow<UiState<Profile>>(UiState.Loading)
        val uiState: StateFlow<UiState<Profile>> = _uiState.asStateFlow()

        private val _isEditing = MutableStateFlow(false)
        val isEditing: StateFlow<Boolean> = _isEditing.asStateFlow()

        private val _isRefreshing = MutableStateFlow(false)
        val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

        // Editable fields - original values cached from API
        private var originalProfile: Profile? = null

        // Editable field state flows
        val editableFullName = MutableStateFlow("")
        val editableEmailId = MutableStateFlow("")
        val editablePhoneNo = MutableStateFlow("")
        val editableAadhaarNumber = MutableStateFlow("")
        val editablePanNumber = MutableStateFlow("")
        val editableAlternativePhone = MutableStateFlow("")
        val editableBusinessName = MutableStateFlow("")
        val editableGstNumber = MutableStateFlow("")
        val editableAccountHolderName = MutableStateFlow("")
        val editableAccountNumber = MutableStateFlow("")
        val editableIfscCode = MutableStateFlow("")
        val editableUpiId = MutableStateFlow("")
        val editableBankName = MutableStateFlow("")
        val editableBranchName = MutableStateFlow("")

        private val _hasChanges = MutableStateFlow(false)
        val hasChanges: StateFlow<Boolean> = _hasChanges.asStateFlow()

        private val _isUploading = MutableStateFlow(false)
        val isUploading: StateFlow<Boolean> = _isUploading.asStateFlow()

        private val _uploadEvent = kotlinx.coroutines.flow.MutableSharedFlow<Result<Unit>>()
        val uploadEvent = _uploadEvent.asSharedFlow()

        init {
            fetchProfile()
        }

        fun fetchProfile(forceRefresh: Boolean = false) {
            viewModelScope.launch {
                if (forceRefresh) {
                    _isRefreshing.value = true
                } else {
                    _uiState.value = UiState.Loading
                }

                fetchProfileUseCase.invoke(com.freight24.common.usecase.EmptyUseCaseParams)
                    .onSuccess { profile ->
                        originalProfile = profile
                        populateEditableFields(profile)
                        _uiState.value = UiState.Success(profile)
                    }
                    .onFailure { error ->
                        _uiState.value = UiState.Error(error.message ?: "Failed to fetch profile")
                    }

                _isRefreshing.value = false
            }
        }

        private fun populateEditableFields(profile: Profile) {
            editableFullName.value = profile.fullName.orEmpty()
            editableEmailId.value = profile.emailId.orEmpty()
            editablePhoneNo.value = profile.phoneNo.orEmpty()
            editableAadhaarNumber.value = profile.aadhaarNumber.orEmpty()
            editablePanNumber.value = profile.panNumber.orEmpty()
            editableAlternativePhone.value = profile.alternativePhoneNo.firstOrNull().orEmpty()
            editableBusinessName.value = profile.businessName.orEmpty()
            editableGstNumber.value = profile.gstNumber.orEmpty()
            editableAccountHolderName.value = profile.bankAccountDetails?.accountHolderName.orEmpty()
            editableAccountNumber.value = profile.bankAccountDetails?.accountNumber.orEmpty()
            editableIfscCode.value = profile.bankAccountDetails?.ifscCode.orEmpty()
            editableUpiId.value = profile.bankAccountDetails?.upiId.orEmpty()
            editableBankName.value = profile.bankAccountDetails?.bankName.orEmpty()
            editableBranchName.value = profile.bankAccountDetails?.branchName.orEmpty()
        }

        fun toggleEditMode() {
            _isEditing.value = !_isEditing.value
            if (!_isEditing.value) {
                // Exiting edit mode -> reset fields to original values
                originalProfile?.let { populateEditableFields(it) }
                _hasChanges.value = false
            }
        }

        fun onFieldChanged() {
            val original = originalProfile ?: return
            _hasChanges.value =
                editableFullName.value != (original.fullName.orEmpty()) ||
                editableEmailId.value != (original.emailId.orEmpty()) ||
                editablePhoneNo.value != (original.phoneNo.orEmpty()) ||
                editableAadhaarNumber.value != (original.aadhaarNumber.orEmpty()) ||
                editablePanNumber.value != (original.panNumber.orEmpty()) ||
                editableAlternativePhone.value != (original.alternativePhoneNo.firstOrNull().orEmpty()) ||
                editableBusinessName.value != (original.businessName.orEmpty()) ||
                editableGstNumber.value != (original.gstNumber.orEmpty()) ||
                editableAccountHolderName.value != (original.bankAccountDetails?.accountHolderName.orEmpty()) ||
                editableAccountNumber.value != (original.bankAccountDetails?.accountNumber.orEmpty()) ||
                editableIfscCode.value != (original.bankAccountDetails?.ifscCode.orEmpty()) ||
                editableUpiId.value != (original.bankAccountDetails?.upiId.orEmpty()) ||
                editableBankName.value != (original.bankAccountDetails?.bankName.orEmpty()) ||
                editableBranchName.value != (original.bankAccountDetails?.branchName.orEmpty())
        }

        fun cancelEdit() {
            _isEditing.value = false
            originalProfile?.let { populateEditableFields(it) }
            _hasChanges.value = false
        }

        fun getUserInitials(): String {
            val name = originalProfile?.fullName ?: return "U"
            return name.split(" ")
                .filter { it.isNotEmpty() }
                .take(2).joinToString("") { it.first().uppercase() }
        }

        fun saveProfile() {
            val original = originalProfile ?: return
            
            viewModelScope.launch {
                _isUploading.value = true
                val request = com.freight24.rest.request.UpdateProfileRequest(
                    full_name = editableFullName.value,
                    email = editableEmailId.value,
                    alt_phones = listOf(editableAlternativePhone.value),
                    pan_card = editablePanNumber.value,
                    aadhar_card = editableAadhaarNumber.value,
                    business_name = editableBusinessName.value,
                    gst_number = editableGstNumber.value,
                    is_tds_applicable = original.isTdsApplicable
                )
                
                updateProfileUseCase.invoke(
                    com.freight24.trucker.feature.profile.domain.usecase.UpdateProfileParams(original.userId, request)
                ).onSuccess {
                    _isEditing.value = false
                    _hasChanges.value = false
                    fetchProfile()
                }.onFailure {
                    // Handle failure (e.g., show error)
                }
                _isUploading.value = false
            }
        }

        fun uploadDocument(uris: List<Uri>, type: com.freight24.trucker.feature.profile.domain.DocumentType, isPdf: Boolean, docId: String? = null) {
            val original = originalProfile ?: return
            val docTypeId = when (type) {
                com.freight24.trucker.feature.profile.domain.DocumentType.AADHAAR -> 1
                com.freight24.trucker.feature.profile.domain.DocumentType.PAN -> 2
                com.freight24.trucker.feature.profile.domain.DocumentType.DRIVING_LICENSE -> 3
                com.freight24.trucker.feature.profile.domain.DocumentType.RC_BOOK -> 4
                com.freight24.trucker.feature.profile.domain.DocumentType.INSURANCE_CERTIFICATE -> 5
                com.freight24.trucker.feature.profile.domain.DocumentType.GST_CERTIFICATE -> 6
                com.freight24.trucker.feature.profile.domain.DocumentType.COMPANY_REGISTRATION -> 7
                com.freight24.trucker.feature.profile.domain.DocumentType.TDS_CERTIFICATE -> 8
                com.freight24.trucker.feature.profile.domain.DocumentType.CANCELLED_CHEQUE -> 9
                else -> return
            }

            viewModelScope.launch {
                _isUploading.value = true
                try {
                    val file = if (isPdf) {
                        getFileFromUri(uris.first(), type)
                    } else {
                        convertImagesToPdf(uris, type)
                    }
                    
                    if (file != null) {
                        val requestFile = file.asRequestBody("application/pdf".toMediaTypeOrNull())
                        val body = MultipartBody.Part.createFormData("file", file.name, requestFile)
                        
                        val result = if (docId != null) {
                            updateIdentityDocumentUseCase.invoke(
                                com.freight24.trucker.feature.profile.domain.usecase.UpdateIdentityDocumentParams(
                                    docId = docId,
                                    userId = original.userId,
                                    documentNumber = "DUMMY_DOC_NUMBER",
                                    file = body
                                )
                            )
                        } else {
                            uploadIdentityDocumentUseCase.invoke(
                                com.freight24.trucker.feature.profile.domain.usecase.UploadIdentityDocumentParams(
                                    userId = original.userId,
                                    documentTypeId = docTypeId,
                                    documentNumber = "DUMMY_DOC_NUMBER",
                                    file = body
                                )
                            )
                        }

                        result.onSuccess {
                            _uploadEvent.emit(Result.success(Unit))
                            fetchProfile(forceRefresh = true)
                        }.onFailure {
                            _uploadEvent.emit(Result.failure(it))
                        }
                    }
                } catch (e: Exception) {
                    _uploadEvent.emit(Result.failure(e))
                } finally {
                    _isUploading.value = false
                }
            }
        }

        private suspend fun convertImagesToPdf(uris: List<Uri>, type: com.freight24.trucker.feature.profile.domain.DocumentType): File? = withContext(Dispatchers.IO) {
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

                val file = File(context.cacheDir, "temp_${type.name}_images.pdf")
                val outputStream = FileOutputStream(file)
                pdfDocument.writeTo(outputStream)
                pdfDocument.close()
                outputStream.close()
                file
            } catch (e: Exception) {
                null
            }
        }

        private suspend fun getFileFromUri(uri: android.net.Uri, type: com.freight24.trucker.feature.profile.domain.DocumentType): File? = withContext(Dispatchers.IO) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val file = File(context.cacheDir, "temp_${type.name}.pdf")
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

        fun getSignedUrl(s3Url: String, onUrlReady: (String) -> Unit) {
            viewModelScope.launch {
                _isUploading.value = true
                getProfileSignedUrlUseCase.invoke(s3Url)
                    .onSuccess { signedUrl ->
                        onUrlReady(signedUrl)
                    }
                    .onFailure {
                        // Handle failure
                    }
                _isUploading.value = false
            }
        }

        fun logout() {
            viewModelScope.launch {
                sessionManager.logout()
            }
        }

        fun onRefresh() {
            fetchProfile(forceRefresh = true)
        }

        fun retry() {
            fetchProfile()
        }
    }
