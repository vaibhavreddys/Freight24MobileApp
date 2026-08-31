package com.freight24.connect.feature.clients.domain.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freight24.common.model.DocumentUploadState
import com.freight24.common.model.UploadMode
import com.freight24.common.model.UploadStatus
import com.freight24.common.util.FileUtils
import com.freight24.connect.feature.clients.domain.usecases.GetClientByUserIdUseCase
import com.freight24.connect.feature.clients.domain.usecases.UpdateClientParams
import com.freight24.connect.feature.clients.domain.usecases.UpdateClientUseCase
import com.freight24.connect.feature.truckowners.profile.domain.usecase.FinalizeIdentityDocumentsUseCase
import com.freight24.connect.feature.truckowners.profile.domain.usecase.GetProfileSignedUrlUseCase
import com.freight24.connect.feature.truckowners.profile.domain.usecase.UpdateIdentityDocumentParams
import com.freight24.connect.feature.truckowners.profile.domain.usecase.UpdateIdentityDocumentUseCase
import com.freight24.connect.feature.truckowners.profile.domain.usecase.UploadIdentityDocumentParams
import com.freight24.connect.feature.truckowners.profile.domain.usecase.UploadIdentityDocumentUseCase
import com.freight24.rest.api.ClientApi
import com.freight24.rest.request.ClientBranchUpdateRequest
import com.freight24.rest.request.FinalSaveFileRequest
import com.freight24.rest.request.FinalSaveRequest
import com.freight24.rest.request.UpdateClientRequest
import com.freight24.rest.response.ClientBranchDto
import com.freight24.rest.response.ClientDocumentDto
import com.freight24.rest.response.ClientProfileDto
import com.freight24.ui.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody

@HiltViewModel
class ClientProfileViewModel
    @Inject
    constructor(
        savedStateHandle: SavedStateHandle,
        private val getClientByUserIdUseCase: GetClientByUserIdUseCase,
        private val updateClientUseCase: UpdateClientUseCase,
        private val uploadIdentityDocumentUseCase: UploadIdentityDocumentUseCase,
        private val updateIdentityDocumentUseCase: UpdateIdentityDocumentUseCase,
        private val finalizeIdentityDocumentsUseCase: FinalizeIdentityDocumentsUseCase,
        private val getProfileSignedUrlUseCase: GetProfileSignedUrlUseCase,
        private val clientApi: ClientApi,
        @param:ApplicationContext private val context: Context,
    ) : ViewModel() {

        private val userId: String = checkNotNull(savedStateHandle["userId"])
        val sessionId: String = UUID.randomUUID().toString()

        private val _uiState = MutableStateFlow<UiState<ClientProfileDto>>(UiState.Loading)
        val uiState: StateFlow<UiState<ClientProfileDto>> = _uiState.asStateFlow()

        private val _isRefreshing = MutableStateFlow(false)
        val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

        private val _isEditing = MutableStateFlow(false)
        val isEditing: StateFlow<Boolean> = _isEditing.asStateFlow()

        private val _hasChanges = MutableStateFlow(false)
        val hasChanges: StateFlow<Boolean> = _hasChanges.asStateFlow()

        private val _hasPendingTemporaryUploads = MutableStateFlow(false)
        val hasPendingTemporaryUploads: StateFlow<Boolean> = _hasPendingTemporaryUploads.asStateFlow()

        private val _isSaving = MutableStateFlow(false)
        val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

        private val _documentStates = MutableStateFlow<Map<String, DocumentUploadState>>(emptyMap())
        val documentStates: StateFlow<Map<String, DocumentUploadState>> = _documentStates.asStateFlow()

        private val _errorEvent = MutableSharedFlow<String>()
        val errorEvent: SharedFlow<String> = _errorEvent.asSharedFlow()

        private val _saveEvent = MutableSharedFlow<String>()
        val saveEvent: SharedFlow<String> = _saveEvent.asSharedFlow()

        private var originalClient: ClientProfileDto? = null

        private val _panDuplicateError = MutableStateFlow<String?>(null)
        val panDuplicateError: StateFlow<String?> = _panDuplicateError.asStateFlow()

        private val _gstDuplicateError = MutableStateFlow<String?>(null)
        val gstDuplicateError: StateFlow<String?> = _gstDuplicateError.asStateFlow()

        val editableFullName = MutableStateFlow("")
        val editableEmail = MutableStateFlow("")
        val editablePhone = MutableStateFlow("")
        val editableCompanyName = MutableStateFlow("")
        val editableGstNumber = MutableStateFlow("")
        val editablePanCard = MutableStateFlow("")
        val editableAadhaarCard = MutableStateFlow("")
        val editableBranches = MutableStateFlow<List<EditableClientBranch>>(emptyList())

        init {
            fetchClient()
        }

        fun fetchClient(forceRefresh: Boolean = false) {
            viewModelScope.launch(Dispatchers.IO) {
                if (forceRefresh) {
                    _isRefreshing.value = true
                } else {
                    _uiState.value = UiState.Loading
                }

                getClientByUserIdUseCase(userId)
                    .onSuccess { client ->
                        originalClient = client
                        populateEditableFields(client)
                        initializeDocumentStates(client, preserveTemporary = _isEditing.value)
                        _uiState.value = UiState.Success(client)
                        recomputeHasChanges()
                    }
                    .onFailure { error ->
                        val message = error.message ?: "Failed to load client profile"
                        _uiState.value = UiState.Error(message)
                        _errorEvent.emit(message)
                    }

                _isRefreshing.value = false
            }
        }

        private fun populateEditableFields(client: ClientProfileDto) {
            editableFullName.value = client.full_name.orEmpty()
            editableEmail.value = client.email.orEmpty()
            editablePhone.value = client.phone.orEmpty()
            editableCompanyName.value = client.company_name.orEmpty()
            editableGstNumber.value = client.gst_number.orEmpty()
            editablePanCard.value = client.pan_card.orEmpty()
            editableAadhaarCard.value = client.aadhar_card.orEmpty()
            editableBranches.value = client.client_branches.mapIndexed { index, branch ->
                branch.toEditableBranch(fallbackId = "branch_$index")
            }
        }

        private fun initializeDocumentStates(client: ClientProfileDto, preserveTemporary: Boolean) {
            val previous = _documentStates.value

            _documentStates.value = documentConfigs.associate { config ->
                val existing = client.documents.findDocument(config.documentName)
                val oldState = previous[config.fieldKey]

                config.fieldKey to DocumentUploadState(
                    fieldKey = config.fieldKey,
                    documentTypeId = config.documentTypeId,
                    existingDocId = existing?.documentId?.takeIf { it.isNotBlank() },
                    existingFileUrl = existing?.documentUrl?.takeIf { it.isNotBlank() },
                    existingVerifyStatus = existing?.documentStatus?.lowercase()?.trim(),
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

        fun onPanFocusLost() {
            val pan = editablePanCard.value.trim().uppercase()
            if (pan.length != 10) return
            val original = originalClient
            if (original != null && pan == original.pan_card.orEmpty().trim().uppercase()) return
            viewModelScope.launch(Dispatchers.IO) {
                val response = clientApi.checkClientField("pan_card", pan, excludeId = userId)
                if (response.isSuccessful && response.body()?.exists == true) {
                    _panDuplicateError.value = "PAN already registered with another client"
                } else if (response.isSuccessful) {
                    _panDuplicateError.value = null
                }
            }
        }

        fun onGstFocusLost() {
            val gst = editableGstNumber.value.trim().uppercase()
            if (gst.length != 15) return
            val original = originalClient
            if (original != null && gst == original.gst_number.orEmpty().trim().uppercase()) return
            viewModelScope.launch(Dispatchers.IO) {
                val response = clientApi.checkClientField("gst_number", gst, excludeId = userId)
                if (response.isSuccessful && response.body()?.exists == true) {
                    _gstDuplicateError.value = "GST already registered with another client"
                } else if (response.isSuccessful) {
                    _gstDuplicateError.value = null
                }
            }
        }

        fun clearFocusErrors() {
            _panDuplicateError.value = null
            _gstDuplicateError.value = null
        }

        fun cancelEdit() {
            clearFocusErrors()
            _isEditing.value = false
            originalClient?.let {
                populateEditableFields(it)
                initializeDocumentStates(it, preserveTemporary = false)
            }
            recomputeHasChanges()
        }

        fun getInitials(): String {
            val name = editableFullName.value
                .ifBlank { originalClient?.full_name.orEmpty() }
                .ifBlank { editableCompanyName.value.ifBlank { originalClient?.company_name.orEmpty() } }
            val letters = name.split(" ").filter { it.isNotBlank() }.map { it.first().uppercaseChar() }
            return when {
                letters.isEmpty() -> "CL"
                letters.size == 1 -> letters[0].toString()
                else -> "${letters[0]}${letters[1]}"
            }
        }

        fun uploadDocument(fieldKey: String, uris: List<Uri>, isPdf: Boolean) {
            if (uris.isEmpty()) return
            val original = originalClient ?: return
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
                                    userId = original.user_id,
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
                                    userId = original.user_id,
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

        fun saveProfile() {
            if (!_hasChanges.value || _isSaving.value) return

            val validationMessage = validateForm()
            if (validationMessage != null) {
                viewModelScope.launch(Dispatchers.IO) {
                    _errorEvent.emit(validationMessage)
                }
                return
            }

            val original = originalClient ?: return

            viewModelScope.launch(Dispatchers.IO) {
                _isSaving.value = true
                val targetUserId = original.user_id
                val hasProfileChanges = hasProfileFieldChanges(original)

                if (hasProfileChanges) {
                    val branchUpdates = buildChangedBranchUpdates(original.client_branches, editableBranches.value)
                    val request =
                        UpdateClientRequest(
                            full_name = editableFullName.value.trim(),
                            email = editableEmail.value.trim().ifBlank { null },
                            company_name = editableCompanyName.value.trim().ifBlank { null },
                            gst_number = editableGstNumber.value.trim().uppercase().ifBlank { null },
                            phone_number = editablePhone.value.trim().ifBlank { null },
                            aadhar_card = editableAadhaarCard.value.trim().ifBlank { null },
                            pan_card = editablePanCard.value.trim().uppercase().ifBlank { null },
                            client_branches = branchUpdates.ifEmpty { null },
                        )

                    val profileResult =
                        updateClientUseCase(
                            UpdateClientParams(
                                userId = targetUserId,
                                request = request,
                            ),
                        )

                    if (profileResult.isFailure) {
                        _isSaving.value = false
                        _errorEvent.emit(profileResult.exceptionOrNull()?.message ?: "Failed to update client profile")
                        return@launch
                    }
                }

                val finalSaveRequest = buildFinalSaveRequest(targetUserId)
                if (finalSaveRequest.files.isNotEmpty()) {
                    val finalSaveResult = finalizeIdentityDocumentsUseCase.invoke(finalSaveRequest)
                    if (finalSaveResult.isFailure) {
                        _isSaving.value = false
                        _errorEvent.emit(finalSaveResult.exceptionOrNull()?.message ?: "Failed to finalize documents")
                        recomputeHasChanges()
                        return@launch
                    }
                    clearTemporaryFileKeys()
                }

                _isEditing.value = false
                fetchClient(forceRefresh = true)
                recomputeHasChanges()
                _saveEvent.emit("Client profile updated successfully")
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

        fun onRefresh() {
            fetchClient(forceRefresh = true)
        }

        fun retry() {
            fetchClient(forceRefresh = false)
        }

        fun onBranchNameChanged(branchId: String, value: String) {
            updateBranch(branchId) { it.copy(branchName = value) }
        }

        fun onBranchAddressChanged(branchId: String, value: String) {
            updateBranch(branchId) { it.copy(branchAddress = value) }
        }

        fun onPodContactNameChanged(branchId: String, value: String) {
            updateBranch(branchId) { it.copy(podContactName = value) }
        }

        fun onPodContactNumberChanged(branchId: String, value: String) {
            updateBranch(branchId) { it.copy(podContactNumber = value) }
        }

        fun onPaymentContactNameChanged(branchId: String, value: String) {
            updateBranch(branchId) { it.copy(paymentContactName = value) }
        }

        fun onPaymentContactNumberChanged(branchId: String, value: String) {
            updateBranch(branchId) { it.copy(paymentContactNumber = value) }
        }

        fun onTrafficContactNameChanged(branchId: String, value: String) {
            updateBranch(branchId) { it.copy(trafficContactName = value) }
        }

        fun onTrafficContactNumberChanged(branchId: String, value: String) {
            updateBranch(branchId) { it.copy(trafficContactNumber = value) }
        }

        fun onTrackingContactNameChanged(branchId: String, value: String) {
            updateBranch(branchId) { it.copy(trackingContactName = value) }
        }

        fun onTrackingContactNumberChanged(branchId: String, value: String) {
            updateBranch(branchId) { it.copy(trackingContactNumber = value) }
        }

        fun getSignedUrl(s3Url: String, onUrlReady: (String) -> Unit) {
            viewModelScope.launch(Dispatchers.IO) {
                getProfileSignedUrlUseCase(s3Url)
                    .onSuccess { signedUrl ->
                        onUrlReady(signedUrl)
                    }
                    .onFailure { error ->
                        _errorEvent.emit(error.message ?: "Failed to get document link")
                    }
            }
        }

        private suspend fun createPdfFile(uris: List<Uri>, isPdf: Boolean): File? {
            return if (isPdf) {
                FileUtils.getFileFromUri(context, uris.first())
            } else {
                FileUtils.convertImageToPdf(context, uris)
            }
        }

        private fun updateBranch(
            branchId: String,
            updater: (EditableClientBranch) -> EditableClientBranch,
        ) {
            editableBranches.update { branches ->
                branches.map { branch ->
                    if (branch.id == branchId) updater(branch) else branch
                }
            }
            onFieldChanged()
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
            val original = originalClient
            val profileChanged = if (original == null) false else hasProfileFieldChanges(original)
            val hasTemporaryUploads = hasTemporaryUploads()
            _hasPendingTemporaryUploads.value = hasTemporaryUploads
            _hasChanges.value = profileChanged || hasTemporaryUploads
        }

        private fun hasProfileFieldChanges(original: ClientProfileDto): Boolean {
            val topLevelChanged =
                editableFullName.value != original.full_name.orEmpty() ||
                    editableEmail.value != original.email.orEmpty() ||
                    editablePhone.value != original.phone.orEmpty() ||
                    editableCompanyName.value != original.company_name.orEmpty() ||
                    editableGstNumber.value != original.gst_number.orEmpty() ||
                    editablePanCard.value != original.pan_card.orEmpty() ||
                    editableAadhaarCard.value != original.aadhar_card.orEmpty()

            val branchChanged = buildChangedBranchUpdates(original.client_branches, editableBranches.value).isNotEmpty()
            return topLevelChanged || branchChanged
        }

        private fun hasTemporaryUploads(): Boolean {
            return _documentStates.value.values.any { !it.temporaryFileKey.isNullOrBlank() }
        }

        private fun buildChangedBranchUpdates(
            original: List<ClientBranchDto>,
            edited: List<EditableClientBranch>,
        ): List<ClientBranchUpdateRequest> {
            val originalById = original.associateBy { it.id.orEmpty() }
            return edited.mapNotNull { branch ->
                if (branch.id.isBlank()) return@mapNotNull null
                val originalBranch = originalById[branch.id] ?: return@mapNotNull null
                var hasAnyChange = false

                var branchName: String? = null
                var branchAddress: String? = null
                var podContactName: String? = null
                var podContactNumber: String? = null
                var paymentContactName: String? = null
                var trafficContactName: String? = null
                var trackingContactName: String? = null
                var paymentContactNumber: String? = null
                var trafficContactNumber: String? = null
                var trackingContactNumber: String? = null

                fun applyChange(currentValue: String, originalValue: String?, assign: (String?) -> Unit) {
                    val current = currentValue.trim()
                    val old = originalValue.orEmpty().trim()
                    if (current != old) {
                        assign(current.ifBlank { null })
                        hasAnyChange = true
                    }
                }

                applyChange(branch.branchName, originalBranch.branch_name ?: originalBranch.name) { branchName = it }
                applyChange(branch.branchAddress, originalBranch.branch_address ?: originalBranch.address) { branchAddress = it }
                applyChange(branch.podContactName, originalBranch.pod_contact_name) { podContactName = it }
                applyChange(branch.podContactNumber, originalBranch.pod_contact_number) { podContactNumber = it }
                applyChange(branch.paymentContactName, originalBranch.payment_contact_name) { paymentContactName = it }
                applyChange(branch.trafficContactName, originalBranch.traffic_contact_name) { trafficContactName = it }
                applyChange(branch.trackingContactName, originalBranch.tracking_contact_name) { trackingContactName = it }
                applyChange(branch.paymentContactNumber, originalBranch.payment_contact_number) { paymentContactNumber = it }
                applyChange(branch.trafficContactNumber, originalBranch.traffic_contact_number) { trafficContactNumber = it }
                applyChange(branch.trackingContactNumber, originalBranch.tracking_contact_number) { trackingContactNumber = it }

                if (!hasAnyChange) return@mapNotNull null

                ClientBranchUpdateRequest(
                    id = branch.id,
                    branch_name = branchName,
                    branch_address = branchAddress,
                    pod_contact_name = podContactName,
                    pod_contact_number = podContactNumber,
                    payment_contact_name = paymentContactName,
                    traffic_contact_name = trafficContactName,
                    tracking_contact_name = trackingContactName,
                    payment_contact_number = paymentContactNumber,
                    traffic_contact_number = trafficContactNumber,
                    tracking_contact_number = trackingContactNumber,
                )
            }
        }

        private fun validateForm(): String? {
            val fullName = editableFullName.value.trim()
            if (fullName.isBlank()) return "Full name is required"

            val email = editableEmail.value.trim()
            if (email.isNotBlank() && !EMAIL_REGEX.matches(email)) {
                return "Enter a valid email address"
            }

            val phone = editablePhone.value.trim()
            if (phone.isBlank()) return "Phone number is required"
            if (!PHONE_REGEX.matches(phone)) return "Enter a valid phone number"

            val pan = editablePanCard.value.trim()
            if (pan.isNotBlank() && !PAN_REGEX.matches(pan.uppercase())) {
                return "Enter a valid PAN number"
            }

            val aadhaar = editableAadhaarCard.value.trim()
            if (aadhaar.isNotBlank() && !AADHAAR_REGEX.matches(aadhaar)) {
                return "Enter a valid Aadhaar number"
            }

            val gst = editableGstNumber.value.trim()
            if (gst.isNotBlank() && !GST_REGEX.matches(gst.uppercase())) {
                return "Enter a valid GST number"
            }

            editableBranches.value.forEachIndexed { index, branch ->
                if (branch.branchName.trim().isBlank()) return "Branch ${index + 1}: Branch name is required"
                if (branch.branchAddress.trim().isBlank()) return "Branch ${index + 1}: Branch address is required"

                val branchPhones =
                    listOf(
                        "POD contact number" to branch.podContactNumber,
                        "Payment contact number" to branch.paymentContactNumber,
                        "Traffic contact number" to branch.trafficContactNumber,
                        "Tracking contact number" to branch.trackingContactNumber,
                    )
                branchPhones.forEach { (label, value) ->
                    if (value.isNotBlank() && !PHONE_REGEX.matches(value.trim())) {
                        return "Branch ${index + 1}: Enter a valid $label"
                    }
                }
            }

            return null
        }

        private fun ClientBranchDto.toEditableBranch(fallbackId: String): EditableClientBranch {
            return EditableClientBranch(
                id = id?.takeIf { it.isNotBlank() } ?: fallbackId,
                branchName = branch_name ?: name.orEmpty(),
                branchAddress = branch_address ?: address.orEmpty(),
                podContactName = pod_contact_name.orEmpty(),
                podContactNumber = pod_contact_number.orEmpty(),
                paymentContactName = payment_contact_name.orEmpty(),
                trafficContactName = traffic_contact_name.orEmpty(),
                trackingContactName = tracking_contact_name.orEmpty(),
                paymentContactNumber = payment_contact_number.orEmpty(),
                trafficContactNumber = traffic_contact_number.orEmpty(),
                trackingContactNumber = tracking_contact_number.orEmpty(),
            )
        }

        private fun List<ClientDocumentDto>.findDocument(documentName: String): ClientDocumentDto? {
            return firstOrNull { it.documentName.equals(documentName, ignoreCase = true) }
        }

        data class EditableClientBranch(
            val id: String,
            val branchName: String = "",
            val branchAddress: String = "",
            val podContactName: String = "",
            val podContactNumber: String = "",
            val paymentContactName: String = "",
            val trafficContactName: String = "",
            val trackingContactName: String = "",
            val paymentContactNumber: String = "",
            val trafficContactNumber: String = "",
            val trackingContactNumber: String = "",
        )

        private data class ClientDocumentConfig(
            val fieldKey: String,
            val documentTypeId: Int,
            val documentName: String,
        )

        private companion object {
            val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")
            val PHONE_REGEX = Regex("^\\+?[0-9]{10,15}$")
            val PAN_REGEX = Regex("^[A-Z]{5}[0-9]{4}[A-Z]$")
            val AADHAAR_REGEX = Regex("^[0-9]{12}$")
            val GST_REGEX = Regex("^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z][0-9A-Z]Z[0-9A-Z]$")

            val documentConfigs = listOf(
                ClientDocumentConfig(fieldKey = "aadhaar", documentTypeId = 1, documentName = "Aadhaar Card"),
                ClientDocumentConfig(fieldKey = "pan", documentTypeId = 2, documentName = "PAN Card"),
            )
        }
    }
