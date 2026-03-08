package com.freight24.connect.feature.clients.domain.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freight24.connect.feature.clients.domain.usecases.GetClientByUserIdUseCase
import com.freight24.connect.feature.clients.domain.usecases.UpdateClientParams
import com.freight24.connect.feature.clients.domain.usecases.UpdateClientUseCase
import com.freight24.rest.request.UpdateClientRequest
import com.freight24.rest.response.ClientProfileDto
import com.freight24.ui.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ClientProfileViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getClientByUserIdUseCase: GetClientByUserIdUseCase,
    private val updateClientUseCase: UpdateClientUseCase,
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
        _hasChanges.value =
            editableFullName.value != original.full_name.orEmpty() ||
            editableEmail.value != original.email.orEmpty() ||
            editablePhone.value != original.phone.orEmpty() ||
            editableCompanyName.value != original.company_name.orEmpty() ||
            editableGstNumber.value != original.gst_number.orEmpty() ||
            editablePanCard.value != original.pan_card.orEmpty() ||
            editableAadhaarCard.value != original.aadhar_card.orEmpty()
    }

    fun cancelEdit() {
        _isEditing.value = false
        originalClient?.let { populateEditableFields(it) }
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

            val request = UpdateClientRequest(
                full_name = editableFullName.value.trim(),
                email = editableEmail.value.trim().ifBlank { null },
                company_name = editableCompanyName.value.trim().ifBlank { null },
                gst_number = editableGstNumber.value.trim().uppercase().ifBlank { null },
                phone_number = editablePhone.value.trim().ifBlank { null },
                aadhar_card = editableAadhaarCard.value.trim().ifBlank { null },
                pan_card = editablePanCard.value.trim().uppercase().ifBlank { null },
            )

            updateClientUseCase(
                UpdateClientParams(
                    userId = original.user_id,
                    request = request,
                ),
            ).onSuccess { updatedClient ->
                originalClient = updatedClient
                populateEditableFields(updatedClient)
                _uiState.value = UiState.Success(updatedClient)
                _isEditing.value = false
                _hasChanges.value = false
                _messageEvent.emit("Client profile updated successfully")
            }.onFailure { error ->
                _messageEvent.emit(error.message ?: "Failed to update client profile")
            }

            _isSaving.value = false
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

        return null
    }

    private companion object {
        val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")
        val PHONE_REGEX = Regex("^\\+?[0-9]{10,15}$")
        val PAN_REGEX = Regex("^[A-Z]{5}[0-9]{4}[A-Z]$")
        val AADHAAR_REGEX = Regex("^[0-9]{12}$")
        val GST_REGEX = Regex("^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z][0-9A-Z]Z[0-9A-Z]$")
    }
}
