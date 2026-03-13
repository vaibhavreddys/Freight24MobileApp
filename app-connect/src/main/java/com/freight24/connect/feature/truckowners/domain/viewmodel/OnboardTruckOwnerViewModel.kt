package com.freight24.connect.feature.truckowners.domain.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freight24.connect.feature.trucks.domain.usecases.GetTruckOwnersUseCase
import com.freight24.connect.feature.truckowners.domain.usecases.CreateTruckOwnerParams
import com.freight24.connect.feature.truckowners.domain.usecases.CreateTruckOwnerUseCase
import com.freight24.connect.feature.truckowners.profile.util.ProfileFieldValidators
import com.freight24.connect.feature.truckowners.profile.util.ValidationResult
import com.freight24.rest.response.TruckOwnerDto
import com.freight24.rest.request.UpdateProfileRequest
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
class OnboardTruckOwnerViewModel @Inject constructor(
    private val createTruckOwnerUseCase: CreateTruckOwnerUseCase,
    private val getTruckOwnersUseCase: GetTruckOwnersUseCase
) : ViewModel() {
    private companion object {
        const val TAG = "OnboardTruckOwnerVM"
    }

    val fullName = MutableStateFlow("")
    val email = MutableStateFlow("")
    val phone = MutableStateFlow("")
    val alternativePhone = MutableStateFlow("")
    val panCard = MutableStateFlow("")
    val aadharCard = MutableStateFlow("")
    val businessName = MutableStateFlow("")
    val gstNumber = MutableStateFlow("")
    val isTdsApplicable = MutableStateFlow(true)
    val accountHolderName = MutableStateFlow("")
    val accountNumber = MutableStateFlow("")
    val ifscCode = MutableStateFlow("")
    val upiId = MutableStateFlow("")
    val bankName = MutableStateFlow("")
    val branchName = MutableStateFlow("")

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _messageEvent = MutableSharedFlow<String>()
    val messageEvent: SharedFlow<String> = _messageEvent.asSharedFlow()

    private val _createdTruckOwner = MutableStateFlow<CreatedTruckOwnerState?>(null)
    val createdTruckOwner: StateFlow<CreatedTruckOwnerState?> = _createdTruckOwner.asStateFlow()

    fun onFullNameChanged(input: String) {
        val filtered = input.filter { it.isLetter() || it == ' ' || it == '.' }
            .replace(Regex("\\s+"), " ")
            .take(60)
            .trimStart()
        fullName.value = filtered
    }

    fun onEmailChanged(input: String) {
        val filtered = input
            .lowercase()
            .filter { it.isLetterOrDigit() || it in "@._+-" }
            .take(80)

        if (filtered.count { it == '@' } <= 1) {
            email.value = filtered
        }
    }

    fun onPhoneChanged(input: String) {
        phone.value = input.filter { it.isDigit() }.take(10)
    }

    fun onAlternativePhoneChanged(input: String) {
        alternativePhone.value = input.filter { it.isDigit() }.take(10)
    }

    fun onAadhaarChanged(input: String) {
        aadharCard.value = input.filter { it.isDigit() }.take(12)
    }

    fun onPanChanged(input: String) {
        val candidate = input.uppercase().filter { it.isLetterOrDigit() }.take(10)
        if (isPanPrefix(candidate)) {
            panCard.value = candidate
        }
    }

    fun onBusinessNameChanged(input: String) {
        val filtered = input.filter { it.isLetterOrDigit() || it in " .,&()-/" }
            .replace(Regex("\\s+"), " ")
            .take(80)
            .trimStart()
        businessName.value = filtered
    }

    fun onGstChanged(input: String) {
        val candidate = input.uppercase().filter { it.isLetterOrDigit() }.take(15)
        if (isGstPrefix(candidate)) {
            gstNumber.value = candidate
        }
    }

    fun onTdsApplicableChanged(value: Boolean) {
        isTdsApplicable.value = value
    }

    fun onAccountHolderNameChanged(input: String) {
        val filtered = input.filter { it.isLetterOrDigit() || it in " .,&()-/" }
            .replace(Regex("\\s+"), " ")
            .take(80)
            .trimStart()
        accountHolderName.value = filtered
    }

    fun onAccountNumberChanged(input: String) {
        accountNumber.value = input.filter { it.isDigit() }.take(18)
    }

    fun onIfscChanged(input: String) {
        val candidate = input.uppercase().filter { it.isLetterOrDigit() }.take(11)
        if (isIfscPrefix(candidate)) {
            ifscCode.value = candidate
        }
    }

    fun onUpiChanged(input: String) {
        val candidate = input.lowercase().filter { it.isLetterOrDigit() || it in "._-@" }.take(80)
        if (isUpiPrefix(candidate)) {
            upiId.value = candidate
        }
    }

    fun onBankNameChanged(input: String) {
        val filtered = input.filter { it.isLetterOrDigit() || it in " .,&()-/" }
            .replace(Regex("\\s+"), " ")
            .take(80)
            .trimStart()
        bankName.value = filtered
    }

    fun onBranchNameChanged(input: String) {
        val filtered = input.filter { it.isLetterOrDigit() || it in " .,&()-/" }
            .replace(Regex("\\s+"), " ")
            .take(80)
            .trimStart()
        branchName.value = filtered
    }

    fun hasChanges(): Boolean {
        return fullName.value.isNotBlank() ||
            email.value.isNotBlank() ||
            phone.value.isNotBlank() ||
            alternativePhone.value.isNotBlank() ||
            panCard.value.isNotBlank() ||
            aadharCard.value.isNotBlank() ||
            businessName.value.isNotBlank() ||
            gstNumber.value.isNotBlank() ||
            accountHolderName.value.isNotBlank() ||
            accountNumber.value.isNotBlank() ||
            ifscCode.value.isNotBlank() ||
            upiId.value.isNotBlank() ||
            bankName.value.isNotBlank() ||
            branchName.value.isNotBlank() ||
            !isTdsApplicable.value
    }

    fun canSubmit(): Boolean {
        val reason = mandatoryValidationError()
        return reason == null
    }

    fun submitBlockReason(): String? = mandatoryValidationError()

    fun submit() {
        if (_isSaving.value) return

        val error = mandatoryValidationError()
        if (error != null) {
            Log.w(TAG, "Submit blocked: $error [phoneLen=${phone.value.length}, aadhaarLen=${aadharCard.value.length}, panLen=${panCard.value.length}, accLen=${accountNumber.value.length}, ifscLen=${ifscCode.value.length}]")
            viewModelScope.launch { _messageEvent.emit(error) }
            return
        }

        viewModelScope.launch {
            _isSaving.value = true

            val normalizedPhone = phone.value.trim()
            val normalizedAltPhone = alternativePhone.value.trim()
            val request = UpdateProfileRequest(
                full_name = fullName.value.trim(),
                email = email.value.trim().ifBlank { null },
                phone = if (normalizedPhone.length == 10) "+91$normalizedPhone" else normalizedPhone,
                alt_phones = normalizedAltPhone.ifBlank { null }?.let { listOf(it) },
                pan_card = panCard.value.trim().uppercase().ifBlank { null },
                aadhar_card = aadharCard.value.trim().ifBlank { null },
                business_name = businessName.value.trim().ifBlank { null },
                gst_number = gstNumber.value.trim().uppercase().ifBlank { null },
                is_tds_applicable = isTdsApplicable.value,
                account_holder_name = accountHolderName.value.trim().ifBlank { null },
                account_number = accountNumber.value.trim().ifBlank { null },
                ifsc_code = ifscCode.value.trim().uppercase().ifBlank { null },
                upi_id = upiId.value.trim().lowercase().ifBlank { null },
                bank_name = bankName.value.trim().ifBlank { null },
                branch_name = branchName.value.trim().ifBlank { null }
            )

            createTruckOwnerUseCase(CreateTruckOwnerParams(request))
                .onSuccess {
                    val matchedOwner = resolveCreatedTruckOwner(request.phone.orEmpty())
                    _createdTruckOwner.value = CreatedTruckOwnerState(
                        fullName = fullName.value.trim(),
                        userId = matchedOwner?.user_id,
                    )
                    _messageEvent.emit("Truck owner created successfully")
                }
                .onFailure { throwable ->
                    _messageEvent.emit(throwable.message ?: "Failed to create truck owner")
                }

            _isSaving.value = false
        }
    }

    private fun mandatoryValidationError(): String? {
        if (fullName.value.trim().isBlank()) return "Full name is required"
        if (phone.value.trim().isBlank()) return "Phone number is required"
        if (aadharCard.value.trim().isBlank()) return "Aadhaar number is required"
        if (panCard.value.trim().isBlank()) return "PAN number is required"
        if (accountHolderName.value.trim().isBlank()) return "Account holder name is required"
        if (accountNumber.value.trim().isBlank()) return "Account number is required"
        if (ifscCode.value.trim().isBlank()) return "IFSC code is required"

        when (ProfileFieldValidators.validateIndianPhone(phone.value.trim())) {
            is ValidationResult.Error -> return "Enter a valid primary phone number"
            ValidationResult.Success -> Unit
        }

        if (aadharCard.value.isNotBlank()) {
            when (val result = ProfileFieldValidators.validateAadhaar(aadharCard.value.trim())) {
                is ValidationResult.Error -> return result.message
                ValidationResult.Success -> Unit
            }
        }

        if (panCard.value.isNotBlank()) {
            when (val result = ProfileFieldValidators.validatePan(panCard.value.trim())) {
                is ValidationResult.Error -> return result.message
                ValidationResult.Success -> Unit
            }
        }

        if (gstNumber.value.isNotBlank()) {
            when (val result = ProfileFieldValidators.validateGst(gstNumber.value.trim())) {
                is ValidationResult.Error -> return result.message
                ValidationResult.Success -> Unit
            }
        }

        if (accountNumber.value.isNotBlank()) {
            when (val result = ProfileFieldValidators.validateAccountNumber(accountNumber.value.trim())) {
                is ValidationResult.Error -> return result.message
                ValidationResult.Success -> Unit
            }
        }

        if (ifscCode.value.isNotBlank()) {
            when (val result = ProfileFieldValidators.validateIfsc(ifscCode.value.trim())) {
                is ValidationResult.Error -> return result.message
                ValidationResult.Success -> Unit
            }
        }

        return null
    }

    private fun isPanPrefix(value: String): Boolean {
        return value.withIndex().all { (index, c) ->
            when (index) {
                0, 1, 2, 3, 4, 9 -> c.isLetter()
                5, 6, 7, 8 -> c.isDigit()
                else -> false
            }
        }
    }

    private fun isIfscPrefix(value: String): Boolean {
        return value.withIndex().all { (index, c) ->
            when {
                index in 0..3 -> c.isLetter()
                index == 4 -> c == '0'
                else -> c.isLetterOrDigit()
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

    private fun isUpiPrefix(value: String): Boolean {
        if (value.count { it == '@' } > 1) return false
        return Regex("^[a-z0-9._-]*(@[a-z0-9.]*)?$").matches(value)
    }

    private suspend fun resolveCreatedTruckOwner(phoneNumber: String): TruckOwnerDto? {
        val normalized = phoneNumber.trim()
        val local = normalized.removePrefix("+91")
        return getTruckOwnersUseCase()
            .getOrNull()
            ?.lastOrNull { owner ->
                owner.phone == normalized ||
                    owner.phone.removePrefix("+91") == local
            }
    }

    fun consumeCreatedTruckOwner() {
        _createdTruckOwner.value = null
    }

    data class CreatedTruckOwnerState(
        val fullName: String,
        val userId: String?,
    )
}
