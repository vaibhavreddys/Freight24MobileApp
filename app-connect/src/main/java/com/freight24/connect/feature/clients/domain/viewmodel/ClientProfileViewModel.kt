package com.freight24.connect.feature.clients.domain.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freight24.connect.feature.clients.domain.usecases.GetClientByUserIdUseCase
import com.freight24.connect.feature.clients.domain.usecases.UpdateClientParams
import com.freight24.connect.feature.clients.domain.usecases.UpdateClientUseCase
import com.freight24.connect.feature.truckowners.profile.domain.usecase.UploadIdentityDocumentParams
import com.freight24.connect.feature.truckowners.profile.domain.usecase.UploadIdentityDocumentUseCase
import com.freight24.rest.request.ClientBranchUpdateRequest
import com.freight24.rest.request.UpdateClientRequest
import com.freight24.rest.response.ClientBranchDto
import com.freight24.rest.response.ClientProfileDto
import com.freight24.ui.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import android.content.Context
import android.net.Uri
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
import java.io.File
import javax.inject.Inject

@HiltViewModel
class ClientProfileViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getClientByUserIdUseCase: GetClientByUserIdUseCase,
    private val updateClientUseCase: UpdateClientUseCase,
    private val uploadIdentityDocumentUseCase: UploadIdentityDocumentUseCase,
) : ViewModel() {

    private val userId: String = checkNotNull(savedStateHandle["userId"])

    private val _uiState = MutableStateFlow<UiState<ClientProfileDto>>(UiState.Loading)
    val uiState: StateFlow<UiState<ClientProfileDto>> = _uiState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _isEditing = MutableStateFlow(false)
    val isEditing: StateFlow<Boolean> = _isEditing.asStateFlow()

    private val _hasChanges = MutableStateFlow(false)
    val hasChanges: StateFlow<Boolean> = _hasChanges.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _messageEvent = MutableSharedFlow<String>()
    val messageEvent: SharedFlow<String> = _messageEvent.asSharedFlow()

    private var originalClient: ClientProfileDto? = null

    val editableFullName = MutableStateFlow("")
    val editableEmail = MutableStateFlow("")
    val editablePhone = MutableStateFlow("")
    val editableCompanyName = MutableStateFlow("")
    val editableGstNumber = MutableStateFlow("")
    val editablePanCard = MutableStateFlow("")
    val editableAadhaarCard = MutableStateFlow("")
    val editableBranches = MutableStateFlow<List<EditableClientBranch>>(emptyList())

    // Document State
    val aadhaarFile = MutableStateFlow<File?>(null)
    val panFile = MutableStateFlow<File?>(null)

    init {
        fetchClient()
    }

    fun fetchClient(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            if (forceRefresh) {
                _isRefreshing.value = true
            } else {
                _uiState.value = UiState.Loading
            }

            getClientByUserIdUseCase(userId)
                .onSuccess { client ->
                    originalClient = client
                    populateEditableFields(client)
                    _uiState.value = UiState.Success(client)
                    _hasChanges.value = false
                }
                .onFailure { error ->
                    _uiState.value = UiState.Error(error.message ?: "Failed to load client profile")
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

    fun toggleEditMode() {
        if (_isEditing.value) {
            cancelEdit()
        } else {
            _isEditing.value = true
        }
    }

    fun onFieldChanged() {
        val original = originalClient ?: return
        val topLevelChanged =
            editableFullName.value != original.full_name.orEmpty() ||
                editableEmail.value != original.email.orEmpty() ||
                editablePhone.value != original.phone.orEmpty() ||
                editableCompanyName.value != original.company_name.orEmpty() ||
                editableGstNumber.value != original.gst_number.orEmpty() ||
                editablePanCard.value != original.pan_card.orEmpty() ||
                editableAadhaarCard.value != original.aadhar_card.orEmpty()

        val documentsChanged = aadhaarFile.value != null || panFile.value != null
        val branchChanged = buildChangedBranchUpdates(original.client_branches, editableBranches.value).isNotEmpty()
        _hasChanges.value = topLevelChanged || branchChanged || documentsChanged
    }

    fun onFileSelected(
        context: Context,
        uri: Uri,
        type: String,
    ) {
        viewModelScope.launch {
            val file = fromUri(context, uri, type)
            if (file != null) {
                when (type) {
                    "AADHAAR" -> aadhaarFile.value = file
                    "PAN" -> panFile.value = file
                }
                onFieldChanged()
            }
        }
    }

    private fun fromUri(
        context: Context,
        uri: Uri,
        prefix: String,
    ): File? {
        return try {
            val contentResolver = context.contentResolver
            val extension = contentResolver.getType(uri)?.split("/")?.lastOrNull() ?: "jpg"
            val fileName = "${prefix}_${System.currentTimeMillis()}.$extension"
            val tempFile = File(context.cacheDir, fileName)
            contentResolver.openInputStream(uri)?.use { input ->
                tempFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            tempFile
        } catch (e: Exception) {
            null
        }
    }

    fun cancelEdit() {
        _isEditing.value = false
        originalClient?.let { populateEditableFields(it) }
        aadhaarFile.value = null
        panFile.value = null
        _hasChanges.value = false
    }

    fun getInitials(): String {
        val name = editableFullName.value.ifBlank { originalClient?.full_name.orEmpty() }
        val letters = name.split(" ").filter { it.isNotBlank() }.map { it.first().uppercaseChar() }
        return when {
            letters.isEmpty() -> "CL"
            letters.size == 1 -> letters[0].toString()
            else -> "${letters[0]}${letters[1]}"
        }
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

    fun saveProfile() {
        if (!_hasChanges.value || _isSaving.value) return

        val validationMessage = validateForm()
        if (validationMessage != null) {
            viewModelScope.launch {
                _messageEvent.emit(validationMessage)
            }
            return
        }

        val original = originalClient ?: return

        viewModelScope.launch {
            _isSaving.value = true

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

            updateClientUseCase(
                UpdateClientParams(
                    userId = original.user_id,
                    request = request,
                ),
            ).onSuccess { updatedClient ->
                originalClient = updatedClient
                populateEditableFields(updatedClient)

                // Upload documents if selected
                uploadDocuments(original.user_id)

                _uiState.value = UiState.Success(updatedClient)
                _isEditing.value = false
                _hasChanges.value = false
                aadhaarFile.value = null
                panFile.value = null
                _messageEvent.emit("Client profile updated successfully")
            }.onFailure { error ->
                _messageEvent.emit(error.message ?: "Failed to update client profile")
            }

            _isSaving.value = false
        }
    }

    private suspend fun uploadDocuments(userId: String) {
        aadhaarFile.value?.let { file ->
            uploadIdentityDocumentUseCase(
                UploadIdentityDocumentParams(
                    userId = userId,
                    documentTypeId = 1, // Aadhaar Card
                    documentNumber = "",
                    file = file.toMultipartPart("file")
                )
            )
        }

        panFile.value?.let { file ->
            uploadIdentityDocumentUseCase(
                UploadIdentityDocumentParams(
                    userId = userId,
                    documentTypeId = 2, // PAN Card
                    documentNumber = "",
                    file = file.toMultipartPart("file")
                )
            )
        }
    }

    private fun File.toMultipartPart(name: String): MultipartBody.Part {
        val requestBody = this.asRequestBody("application/octet-stream".toMediaTypeOrNull())
        return MultipartBody.Part.createFormData(name, this.name, requestBody)
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

    private companion object {
        val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")
        val PHONE_REGEX = Regex("^\\+?[0-9]{10,15}$")
        val PAN_REGEX = Regex("^[A-Z]{5}[0-9]{4}[A-Z]$")
        val AADHAAR_REGEX = Regex("^[0-9]{12}$")
        val GST_REGEX = Regex("^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z][0-9A-Z]Z[0-9A-Z]$")
    }
}
