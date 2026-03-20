package com.freight24.connect.feature.truckowners.profile.view

import android.content.Context
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freight24.common.model.DocumentUploadState
import com.freight24.common.model.UploadMode
import com.freight24.common.model.UploadStatus
import com.freight24.common.util.FileUtils
import com.freight24.connect.feature.truckowners.profile.domain.DocumentType
import com.freight24.connect.feature.truckowners.profile.domain.Profile
import com.freight24.connect.feature.truckowners.profile.domain.usecase.FetchProfileParams
import com.freight24.connect.feature.truckowners.profile.domain.usecase.FetchProfileUseCase
import com.freight24.connect.feature.truckowners.profile.domain.usecase.FinalizeIdentityDocumentsUseCase
import com.freight24.connect.feature.truckowners.profile.domain.usecase.UpdateIdentityDocumentParams
import com.freight24.connect.feature.truckowners.profile.domain.usecase.UpdateIdentityDocumentUseCase
import com.freight24.connect.feature.truckowners.profile.domain.usecase.UpdateProfileParams
import com.freight24.connect.feature.truckowners.profile.domain.usecase.UpdateProfileUseCase
import com.freight24.connect.feature.truckowners.profile.domain.usecase.UploadIdentityDocumentParams
import com.freight24.connect.feature.truckowners.profile.domain.usecase.UploadIdentityDocumentUseCase
import com.freight24.connect.feature.truckowners.profile.util.ProfileDocumentTypeIds
import com.freight24.rest.request.FinalSaveFileRequest
import com.freight24.rest.request.FinalSaveRequest
import com.freight24.ui.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody

@HiltViewModel
internal class ProfileViewModel
    @Inject
    constructor(
        savedStateHandle: SavedStateHandle,
        private val fetchProfileUseCase: FetchProfileUseCase,
        private val updateProfileUseCase: UpdateProfileUseCase,
        private val uploadIdentityDocumentUseCase: UploadIdentityDocumentUseCase,
        private val updateIdentityDocumentUseCase: UpdateIdentityDocumentUseCase,
        private val finalizeIdentityDocumentsUseCase: FinalizeIdentityDocumentsUseCase,
        private val getProfileSignedUrlUseCase: com.freight24.connect.feature.truckowners.profile.domain.usecase.GetProfileSignedUrlUseCase,
        @param:ApplicationContext private val context: Context,
    ) : ViewModel() {
        private val userId: String = checkNotNull(savedStateHandle["userId"])
        val sessionId: String = UUID.randomUUID().toString()

        private val _uiState = MutableStateFlow<UiState<Profile>>(UiState.Loading)
        val uiState: StateFlow<UiState<Profile>> = _uiState.asStateFlow()

        private val _isEditing = MutableStateFlow(false)
        val isEditing: StateFlow<Boolean> = _isEditing.asStateFlow()

        private val _isRefreshing = MutableStateFlow(false)
        val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

        private var originalProfile: Profile? = null

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

        private val _documentStates = MutableStateFlow<Map<String, DocumentUploadState>>(emptyMap())
        val documentStates: StateFlow<Map<String, DocumentUploadState>> = _documentStates.asStateFlow()

        private val _hasChanges = MutableStateFlow(false)
        val hasChanges: StateFlow<Boolean> = _hasChanges.asStateFlow()

        private val _hasPendingTemporaryUploads = MutableStateFlow(false)
        val hasPendingTemporaryUploads: StateFlow<Boolean> = _hasPendingTemporaryUploads.asStateFlow()

        private val _isSaving = MutableStateFlow(false)
        val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

        private val _errorEvent = MutableSharedFlow<String>()
        val errorEvent = _errorEvent.asSharedFlow()

        private val _saveEvent = MutableSharedFlow<Result<Unit>>()
        val saveEvent = _saveEvent.asSharedFlow()

        init {
            fetchProfile()
        }

        fun fetchProfile(forceRefresh: Boolean = false) {
            viewModelScope.launch(Dispatchers.IO) {
                if (forceRefresh) {
                    _isRefreshing.value = true
                } else {
                    _uiState.value = UiState.Loading
                }

                fetchProfileUseCase.invoke(FetchProfileParams(userId))
                    .onSuccess { profile ->
                        originalProfile = profile
                        populateEditableFields(profile)
                        initializeDocumentStates(profile, preserveTemporary = _isEditing.value)
                        _uiState.value = UiState.Success(profile)
                        recomputeHasChanges()
                    }
                    .onFailure { error ->
                        val message = error.message ?: "Failed to fetch profile"
                        _uiState.value = UiState.Error(message)
                        _errorEvent.emit(message)
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

        private fun initializeDocumentStates(profile: Profile, preserveTemporary: Boolean) {
            val previous = _documentStates.value
            val docsByType = profile.documents.associateBy { it.type }

            _documentStates.value = ProfileDocumentTypeIds.editableDocumentTypes.associate { type ->
                val fieldKey = ProfileDocumentTypeIds.fieldKey(type)
                val existing = docsByType[type]
                val oldState = previous[fieldKey]
                val documentTypeId = ProfileDocumentTypeIds.documentTypeId(type) ?: 0

                fieldKey to DocumentUploadState(
                    fieldKey = fieldKey,
                    documentTypeId = documentTypeId,
                    existingDocId = existing?.docId,
                    existingFileUrl = existing?.url,
                    existingVerifyStatus = existing?.let { ProfileDocumentTypeIds.verifyStatus(it.status) },
                    temporaryFileKey = if (preserveTemporary) oldState?.temporaryFileKey else null,
                    uploadStatus = if (preserveTemporary && oldState?.temporaryFileKey != null) {
                        if (oldState.uploadStatus == UploadStatus.ERROR) UploadStatus.ERROR else UploadStatus.DONE
                    } else {
                        UploadStatus.IDLE
                    },
                )
            }

            _hasPendingTemporaryUploads.value = hasTemporaryUploads()
        }

        fun toggleEditMode() {
            if (_isEditing.value) {
                cancelEdit()
            } else {
                _isEditing.value = true
                recomputeHasChanges()
            }
        }

        fun onFieldChanged() {
            recomputeHasChanges()
        }

        fun cancelEdit() {
            _isEditing.value = false
            originalProfile?.let {
                populateEditableFields(it)
                initializeDocumentStates(it, preserveTemporary = false)
            }
            recomputeHasChanges()
        }

        fun getUserInitials(): String {
            val name = originalProfile?.fullName ?: return "U"
            return name
                .split(" ")
                .filter { it.isNotEmpty() }
                .take(2)
                .joinToString("") { it.first().uppercase() }
        }

        fun saveProfile() {
            val original = originalProfile ?: return
            if (_isSaving.value) return

            viewModelScope.launch(Dispatchers.IO) {
                _isSaving.value = true

                val targetUserId = original.userId

                val hasProfileChanges = hasProfileFieldChanges(original)
                if (hasProfileChanges) {
                    val request = com.freight24.rest.request.UpdateProfileRequest(
                        full_name = editableFullName.value,
                        email = editableEmailId.value,
                        alt_phones = listOf(editableAlternativePhone.value),
                        pan_card = editablePanNumber.value,
                        aadhar_card = editableAadhaarNumber.value,
                        business_name = editableBusinessName.value,
                        gst_number = editableGstNumber.value,
                        is_tds_applicable = original.isTdsApplicable,
                        account_holder_name = editableAccountHolderName.value,
                        account_number = editableAccountNumber.value,
                        ifsc_code = editableIfscCode.value,
                        upi_id = editableUpiId.value,
                        bank_name = editableBankName.value,
                        branch_name = editableBranchName.value,
                    )

                    val profileSaveResult = updateProfileUseCase.invoke(UpdateProfileParams(targetUserId, request))
                    if (profileSaveResult.isFailure) {
                        _isSaving.value = false
                        val message = profileSaveResult.exceptionOrNull()?.message ?: "Failed to save profile"
                        _errorEvent.emit(message)
                        _saveEvent.emit(Result.failure(IllegalStateException(message)))
                        return@launch
                    }
                }

                val finalSaveRequest = buildFinalSaveRequest(targetUserId)
                if (finalSaveRequest.files.isNotEmpty()) {
                    val finalSaveResult = finalizeIdentityDocumentsUseCase.invoke(finalSaveRequest)
                    if (finalSaveResult.isFailure) {
                        _isSaving.value = false
                        val message = finalSaveResult.exceptionOrNull()?.message ?: "Failed to finalize documents"
                        _errorEvent.emit(message)
                        _saveEvent.emit(Result.failure(IllegalStateException(message)))
                        recomputeHasChanges()
                        return@launch
                    }
                    clearTemporaryFileKeys()
                }

                _isEditing.value = false
                fetchProfile(forceRefresh = true)
                recomputeHasChanges()
                _saveEvent.emit(Result.success(Unit))
                _isSaving.value = false
            }
        }

        fun buildFinalSaveRequest(targetUserId: String): FinalSaveRequest {
            return FinalSaveRequest(
                user_id = targetUserId,
                files = documentStates.value.values
                    .filter { it.temporaryFileKey != null }
                    .map { doc ->
                        FinalSaveFileRequest(
                            temporaryKey = doc.temporaryFileKey!!,
                            document_type_id = doc.documentTypeId,
                        )
                    },
            )
        }

        fun uploadDocument(uris: List<Uri>, type: DocumentType, isPdf: Boolean) {
            if (uris.isEmpty()) return
            val original = originalProfile ?: return
            val fieldKey = ProfileDocumentTypeIds.fieldKey(type)
            val currentState = _documentStates.value[fieldKey] ?: return

            viewModelScope.launch(Dispatchers.IO) {
                updateDocumentState(fieldKey) { state ->
                    state.copy(uploadStatus = UploadStatus.UPLOADING, existingVerifyStatus = null)
                }
                recomputeHasChanges()

                try {
                    val file = createPdfFile(uris = uris, isPdf = isPdf)
                        ?: throw IllegalStateException("Unable to process selected file")

                    val requestFile = file.asRequestBody("application/pdf".toMediaTypeOrNull())
                    val body = MultipartBody.Part.createFormData("file", file.name, requestFile)

                    val result = when (val uploadMode = resolveUploadMode(currentState)) {
                        UploadMode.ADD -> {
                            uploadIdentityDocumentUseCase.invoke(
                                UploadIdentityDocumentParams(
                                    userId = original.userId,
                                    documentTypeId = currentState.documentTypeId,
                                    documentNumber = "SESSION_$sessionId",
                                    file = body,
                                ),
                            )
                        }

                        is UploadMode.UPDATE -> {
                            updateIdentityDocumentUseCase.invoke(
                                UpdateIdentityDocumentParams(
                                    docId = uploadMode.existingDocId,
                                    userId = original.userId,
                                    documentNumber = "SESSION_$sessionId",
                                    file = body,
                                ),
                            )
                        }
                    }

                    result.onSuccess { temporaryKey ->
                        updateDocumentState(fieldKey) { state ->
                            state.copy(
                                temporaryFileKey = temporaryKey,
                                existingVerifyStatus = null,
                                uploadStatus = UploadStatus.DONE,
                            )
                        }
                    }.onFailure { error ->
                        updateDocumentState(fieldKey) { state ->
                            state.copy(uploadStatus = UploadStatus.ERROR)
                        }
                        _errorEvent.emit(error.message ?: "Upload failed")
                    }
                } catch (e: Exception) {
                    updateDocumentState(fieldKey) { state ->
                        state.copy(uploadStatus = UploadStatus.ERROR)
                    }
                    _errorEvent.emit(e.message ?: "An error occurred during upload")
                }

                recomputeHasChanges()
            }
        }

        private fun resolveUploadMode(currentDoc: DocumentUploadState): UploadMode {
            val existingDocId = currentDoc.existingDocId?.takeIf { it.isNotBlank() }
            return if (existingDocId == null) {
                UploadMode.ADD
            } else {
                UploadMode.UPDATE(existingDocId = existingDocId)
            }
        }

        private suspend fun createPdfFile(uris: List<Uri>, isPdf: Boolean): File? {
            return if (isPdf) {
                FileUtils.getFileFromUri(context, uris.first())
            } else {
                FileUtils.convertImageToPdf(context, uris)
            }
        }

        private fun updateDocumentState(fieldKey: String, transformer: (DocumentUploadState) -> DocumentUploadState) {
            _documentStates.value = _documentStates.value.toMutableMap().apply {
                val current = this[fieldKey] ?: return@apply
                this[fieldKey] = transformer(current)
            }
            _hasPendingTemporaryUploads.value = hasTemporaryUploads()
        }

        private fun clearTemporaryFileKeys() {
            _documentStates.value = _documentStates.value.mapValues { (_, state) ->
                state.copy(temporaryFileKey = null, uploadStatus = UploadStatus.IDLE)
            }
            _hasPendingTemporaryUploads.value = false
        }

        private fun recomputeHasChanges() {
            val original = originalProfile
            val textChanged = if (original == null) false else hasProfileFieldChanges(original)

            val hasTemporaryUploads = hasTemporaryUploads()
            _hasPendingTemporaryUploads.value = hasTemporaryUploads
            _hasChanges.value = textChanged || hasTemporaryUploads
        }

        private fun hasProfileFieldChanges(original: Profile): Boolean {
            return editableFullName.value != original.fullName.orEmpty() ||
                editableEmailId.value != original.emailId.orEmpty() ||
                editablePhoneNo.value != original.phoneNo.orEmpty() ||
                editableAadhaarNumber.value != original.aadhaarNumber.orEmpty() ||
                editablePanNumber.value != original.panNumber.orEmpty() ||
                editableAlternativePhone.value != original.alternativePhoneNo.firstOrNull().orEmpty() ||
                editableBusinessName.value != original.businessName.orEmpty() ||
                editableGstNumber.value != original.gstNumber.orEmpty() ||
                editableAccountHolderName.value != original.bankAccountDetails?.accountHolderName.orEmpty() ||
                editableAccountNumber.value != original.bankAccountDetails?.accountNumber.orEmpty() ||
                editableIfscCode.value != original.bankAccountDetails?.ifscCode.orEmpty() ||
                editableUpiId.value != original.bankAccountDetails?.upiId.orEmpty() ||
                editableBankName.value != original.bankAccountDetails?.bankName.orEmpty() ||
                editableBranchName.value != original.bankAccountDetails?.branchName.orEmpty()
        }

        private fun hasTemporaryUploads(): Boolean {
            return _documentStates.value.values.any { !it.temporaryFileKey.isNullOrBlank() }
        }

        fun getSignedUrl(s3Url: String, onUrlReady: (String) -> Unit) {
            viewModelScope.launch(Dispatchers.IO) {
                getProfileSignedUrlUseCase.invoke(s3Url)
                    .onSuccess { signedUrl ->
                        onUrlReady(signedUrl)
                    }
                    .onFailure { error ->
                        _errorEvent.emit(error.message ?: "Failed to get document link")
                    }
            }
        }

        fun onRefresh() {
            fetchProfile(forceRefresh = true)
        }

        fun retry() {
            fetchProfile()
        }
    }
