package com.freight24.connect.feature.clients.domain.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freight24.connect.feature.clients.domain.usecases.CreateClientParams
import com.freight24.connect.feature.clients.domain.usecases.CreateClientUseCase
import com.freight24.connect.feature.clients.domain.usecases.GetClientsUseCase
import com.freight24.connect.feature.truckowners.profile.util.ProfileFieldValidators
import com.freight24.connect.feature.truckowners.profile.util.ValidationResult
import com.freight24.rest.request.ClientBranchRequest
import com.freight24.rest.request.UpdateClientRequest
import com.freight24.rest.response.ClientDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardClientViewModel @Inject constructor(
    private val createClientUseCase: CreateClientUseCase,
    private val getClientsUseCase: GetClientsUseCase,
) : ViewModel() {

    val fullName = MutableStateFlow("")
    val phoneNumber = MutableStateFlow("")
    val panNumber = MutableStateFlow("")
    val email = MutableStateFlow("")
    val companyName = MutableStateFlow("")
    val gstNumber = MutableStateFlow("")
    val branches = MutableStateFlow<List<BranchFormState>>(emptyList())

    private var nextBranchId = 1

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _messageEvent = MutableSharedFlow<String>()
    val messageEvent: SharedFlow<String> = _messageEvent.asSharedFlow()

    private val _createdClient = MutableStateFlow<CreatedClientState?>(null)
    val createdClient: StateFlow<CreatedClientState?> = _createdClient.asStateFlow()

    fun onFullNameChanged(input: String) {
        fullName.value = sanitizePersonName(input)
    }

    fun onPhoneNumberChanged(input: String) {
        phoneNumber.value = input.filter(Char::isDigit).take(10)
    }

    fun onPanNumberChanged(input: String) {
        val candidate = input.uppercase().filter(Char::isLetterOrDigit).take(10)
        if (isPanPrefix(candidate)) panNumber.value = candidate
    }

    fun onEmailChanged(input: String) {
        val filtered = input.lowercase().filter { it.isLetterOrDigit() || it in "@._+-" }.take(80)
        if (filtered.count { it == '@' } <= 1) {
            email.value = filtered
        }
    }

    fun onCompanyNameChanged(input: String) {
        companyName.value = sanitizeText(input, maxLength = 80)
    }

    fun onGstNumberChanged(input: String) {
        val candidate = input.uppercase().filter(Char::isLetterOrDigit).take(15)
        if (isGstPrefix(candidate)) gstNumber.value = candidate
    }

    fun addBranch() {
        branches.update { current ->
            current + BranchFormState(id = nextBranchId++)
        }
    }

    fun onBranchNameChanged(branchId: Int, input: String) {
        updateBranch(branchId) { it.copy(branchName = sanitizeText(input, maxLength = 80)) }
    }

    fun onBranchAddressChanged(branchId: Int, input: String) {
        updateBranch(branchId) { it.copy(branchAddress = sanitizeAddress(input)) }
    }

    fun onPaymentContactNameChanged(branchId: Int, input: String) {
        updateBranch(branchId) { it.copy(paymentContactName = sanitizePersonName(input)) }
    }

    fun onPaymentContactNumberChanged(branchId: Int, input: String) {
        updateBranch(branchId) { it.copy(paymentContactNumber = input.filter(Char::isDigit).take(10)) }
    }

    fun onTrafficContactNameChanged(branchId: Int, input: String) {
        updateBranch(branchId) { it.copy(trafficContactName = sanitizePersonName(input)) }
    }

    fun onTrafficContactNumberChanged(branchId: Int, input: String) {
        updateBranch(branchId) { it.copy(trafficContactNumber = input.filter(Char::isDigit).take(10)) }
    }

    fun onPodContactNameChanged(branchId: Int, input: String) {
        updateBranch(branchId) { it.copy(podContactName = sanitizePersonName(input)) }
    }

    fun onPodContactNumberChanged(branchId: Int, input: String) {
        updateBranch(branchId) { it.copy(podContactNumber = input.filter(Char::isDigit).take(10)) }
    }

    fun onTrackingContactNameChanged(branchId: Int, input: String) {
        updateBranch(branchId) { it.copy(trackingContactName = sanitizePersonName(input)) }
    }

    fun onTrackingContactNumberChanged(branchId: Int, input: String) {
        updateBranch(branchId) { it.copy(trackingContactNumber = input.filter(Char::isDigit).take(10)) }
    }

    fun copyPaymentFromPrimary(branchId: Int) {
        updateBranch(branchId) {
            it.copy(
                paymentContactName = fullName.value.trim(),
                paymentContactNumber = phoneNumber.value.trim(),
            )
        }
    }

    fun copyTrafficFromPrimary(branchId: Int) {
        updateBranch(branchId) {
            it.copy(
                trafficContactName = fullName.value.trim(),
                trafficContactNumber = phoneNumber.value.trim(),
            )
        }
    }

    fun copyPodFromPrimary(branchId: Int) {
        updateBranch(branchId) {
            it.copy(
                podContactName = fullName.value.trim(),
                podContactNumber = phoneNumber.value.trim(),
            )
        }
    }

    fun copyTrackingFromPrimary(branchId: Int) {
        updateBranch(branchId) {
            it.copy(
                trackingContactName = fullName.value.trim(),
                trackingContactNumber = phoneNumber.value.trim(),
            )
        }
    }

    fun hasRequiredFields(): Boolean = requiredValidationMessage() == null

    fun canSubmit(): Boolean = !_isSaving.value && hasRequiredFields()

    fun submitBlockReason(): String? = requiredValidationMessage()

    fun validationMessage(): String? {
        requiredValidationMessage()?.let { return it }

        if (email.value.isNotBlank()) {
            when (val result = ProfileFieldValidators.validateEmail(email.value.trim())) {
                is ValidationResult.Error -> return result.message
                ValidationResult.Success -> Unit
            }
        }

        branches.value.forEachIndexed { index, branch ->
            contactPhoneValues(branch).forEach { phone ->
                if (phone.isNotBlank()) {
                    val error = phoneValidation(phone, showIncompleteError = true)
                    if (error != null) {
                        return "Branch ${index + 1}: $error"
                    }
                }
            }
        }

        return null
    }

    fun submit() {
        if (_isSaving.value) return
        val blockReason = validationMessage()
        if (blockReason != null) {
            viewModelScope.launch { _messageEvent.emit(blockReason) }
            return
        }

        viewModelScope.launch {
            _isSaving.value = true

            val request =
                UpdateClientRequest(
                    full_name = fullName.value.trim(),
                    phone_number = phoneNumber.value.trim(),
                    pan_card = panNumber.value.trim().uppercase(),
                    email = email.value.trim().ifBlank { null },
                    company_name = companyName.value.trim().ifBlank { null },
                    gst_number = gstNumber.value.trim().uppercase(),
                    branches = buildBranchesPayload(),
                )

            createClientUseCase(CreateClientParams(request))
                .onSuccess {
                    val created = resolveCreatedClient(phoneNumber.value.trim())
                    _createdClient.value =
                        CreatedClientState(
                            fullName = fullName.value.trim(),
                            userId = created?.userId,
                        )
                    _messageEvent.emit("Client created successfully")
                }
                .onFailure { error ->
                    _messageEvent.emit(error.message ?: "Failed to create client")
                }

            _isSaving.value = false
        }
    }

    fun consumeCreatedClient() {
        _createdClient.value = null
    }

    private fun updateBranch(
        branchId: Int,
        updater: (BranchFormState) -> BranchFormState,
    ) {
        branches.update { list ->
            list.map { branch ->
                if (branch.id == branchId) updater(branch) else branch
            }
        }
    }

    private fun requiredValidationMessage(): String? {
        if (fullName.value.trim().isBlank()) return "Full Name is required"
        if (phoneNumber.value.trim().isBlank()) return "Phone Number is required"
        if (panNumber.value.trim().isBlank()) return "PAN Number is required"
        if (gstNumber.value.trim().isBlank()) return "GST Number is required"

        phoneValidation(phoneNumber.value, showIncompleteError = true)?.let { return it }

        when (val result = ProfileFieldValidators.validatePan(panNumber.value.trim())) {
            is ValidationResult.Error -> return result.message
            ValidationResult.Success -> Unit
        }

        when (val result = ProfileFieldValidators.validateGst(gstNumber.value.trim())) {
            is ValidationResult.Error -> return result.message
            ValidationResult.Success -> Unit
        }

        if (branches.value.isEmpty()) return "At least one branch is required"

        branches.value.forEachIndexed { index, branch ->
            if (branch.branchName.trim().isBlank()) return "Branch ${index + 1}: Branch Name is required"
            if (branch.branchAddress.trim().isBlank()) return "Branch ${index + 1}: Branch Address is required"
        }

        return null
    }

    private fun buildBranchesPayload(): Map<String, ClientBranchRequest> {
        val payload = LinkedHashMap<String, ClientBranchRequest>()
        branches.value.forEachIndexed { index, branch ->
            payload[(index + 1).toString()] =
                ClientBranchRequest(
                    branch_name = branch.branchName.trim(),
                    branch_address = branch.branchAddress.trim(),
                    payment_contact_name = branch.paymentContactName.trim().ifBlank { null },
                    payment_contact_number = branch.paymentContactNumber.trim().ifBlank { null },
                    traffic_contact_name = branch.trafficContactName.trim().ifBlank { null },
                    traffic_contact_number = branch.trafficContactNumber.trim().ifBlank { null },
                    pod_contact_name = branch.podContactName.trim().ifBlank { null },
                    pod_contact_number = branch.podContactNumber.trim().ifBlank { null },
                    tracking_contact_name = branch.trackingContactName.trim().ifBlank { null },
                    tracking_contact_number = branch.trackingContactNumber.trim().ifBlank { null },
                )
        }
        return payload
    }

    private suspend fun resolveCreatedClient(phone: String): ClientDto? {
        val normalized = phone.removePrefix("+91")
        return getClientsUseCase()
            .getOrNull()
            ?.lastOrNull { client ->
                client.phoneNumber.removePrefix("+91") == normalized
            }
    }

    private fun contactPhoneValues(branch: BranchFormState): List<String> {
        return listOf(
            branch.paymentContactNumber.trim(),
            branch.trafficContactNumber.trim(),
            branch.podContactNumber.trim(),
            branch.trackingContactNumber.trim(),
        )
    }

    private fun phoneValidation(value: String, showIncompleteError: Boolean): String? {
        if (value.isBlank()) return null
        return when {
            value.length < 10 && showIncompleteError -> "Phone must be 10 digits"
            value.length == 10 -> {
                when (val result = ProfileFieldValidators.validateIndianPhone(value)) {
                    is ValidationResult.Error -> result.message
                    ValidationResult.Success -> null
                }
            }
            else -> null
        }
    }

    private fun sanitizePersonName(input: String): String {
        return input
            .filter { it.isLetter() || it == ' ' || it == '.' }
            .replace(Regex("\\s+"), " ")
            .take(60)
            .trimStart()
    }

    private fun sanitizeText(input: String, maxLength: Int): String {
        return input
            .filter { it.isLetterOrDigit() || it in " .,&()-/" }
            .replace(Regex("\\s+"), " ")
            .take(maxLength)
            .trimStart()
    }

    private fun sanitizeAddress(input: String): String {
        return input
            .filter { it.isLetterOrDigit() || it in " .,&()/#-," }
            .replace(Regex("[ ]{2,}"), " ")
            .take(160)
            .trimStart()
    }

    private fun isPanPrefix(value: String): Boolean {
        return value.withIndex().all { (index, c) ->
            when {
                index in 0..4 -> c.isLetter()
                index in 5..8 -> c.isDigit()
                index == 9 -> c.isLetter()
                else -> false
            }
        }
    }

    private fun isGstPrefix(value: String): Boolean {
        return value.withIndex().all { (index, c) ->
            when {
                index in 0..1 -> c.isDigit()
                index in 2..6 -> c.isLetter()
                index in 7..10 -> c.isDigit()
                index == 11 -> c.isLetter()
                index == 12 -> c.isLetterOrDigit() && c != '0'
                index == 13 -> c == 'Z'
                index == 14 -> c.isLetterOrDigit()
                else -> false
            }
        }
    }

    data class BranchFormState(
        val id: Int,
        val branchName: String = "",
        val branchAddress: String = "",
        val paymentContactName: String = "",
        val paymentContactNumber: String = "",
        val trafficContactName: String = "",
        val trafficContactNumber: String = "",
        val podContactName: String = "",
        val podContactNumber: String = "",
        val trackingContactName: String = "",
        val trackingContactNumber: String = "",
    )

    data class CreatedClientState(
        val fullName: String,
        val userId: String?,
    )
}
