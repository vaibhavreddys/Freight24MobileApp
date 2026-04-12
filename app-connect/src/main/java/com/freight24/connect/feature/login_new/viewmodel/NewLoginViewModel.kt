package com.freight24.connect.feature.login_new.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freight24.rest.api.LoginConnectApi
import com.freight24.rest.request.OtpRequest
import com.freight24.ui.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class NewLoginViewModel
@Inject
constructor(
    private val loginConnectApi: LoginConnectApi,
) : ViewModel() {
    companion object {
        private const val FULL_PHONE_PREFIX = "+91"
        val PHONE_REGEX: Regex = Regex("^[789]\\d{9}$")
    }

    // FIX #2: Start with empty string instead of space
    private val _phoneNumber = MutableStateFlow("")
    val phoneNumber: StateFlow<String> = _phoneNumber.asStateFlow()

    private val _isPhoneValid = MutableStateFlow(false)
    val isPhoneValid: StateFlow<Boolean> = _isPhoneValid.asStateFlow()

    private val _uiState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val uiState: StateFlow<UiState<String>> = _uiState.asStateFlow()

    fun onPhoneNumberChange(input: String) {
        // FIX #3: Strip country code prefixes from autofill
        val cleanedInput = stripCountryCode(input)
        val digitsOnly = cleanedInput.filter(Char::isDigit).take(10)
        _phoneNumber.value = digitsOnly
        _isPhoneValid.value = PHONE_REGEX.matches(digitsOnly)
    }

    // FIX #3: Helper function to remove country codes
    private fun stripCountryCode(input: String): String {
        var result = input.trim()

        // Remove "+91" or "91" prefix
        if (result.startsWith("+91")) {
            result = result.substring(3)
        } else if (result.startsWith("91") && result.length > 2) {
            result = result.substring(2)
        }

        // Remove "+91 " with space
        if (result.startsWith("+91 ")) {
            result = result.substring(4)
        }

        return result
    }

    fun onSendOtpClick() {
        if (!PHONE_REGEX.matches(_phoneNumber.value)) {
            _uiState.value = UiState.Error("Enter a valid 10-digit Indian mobile number")
            return
        }

        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val response =
                    loginConnectApi.requestOtp(
                        OtpRequest(phone_number = "$FULL_PHONE_PREFIX${_phoneNumber.value}"),
                    )

                _uiState.value =
                    if (response.success) {
                        UiState.Success(_phoneNumber.value)
                    } else {
                        UiState.Error(response.message.ifBlank { "Failed to send OTP" })
                    }
            } catch (exception: Exception) {
                _uiState.value = UiState.Error(exception.message ?: "Failed to send OTP")
            }
        }
    }

    fun resetUiState() {
        _uiState.value = UiState.Idle
    }
}