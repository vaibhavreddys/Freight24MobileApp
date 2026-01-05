package com.freight24.trucker.feature.login

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freight24.core.auth.AuthRepository
import com.freight24.core.ui.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VerifyOtpViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    companion object {
        private const val OTP_LENGTH = 6
        private const val RESEND_DELAY_SECONDS = 30
    }

    private val _otp = MutableStateFlow("")
    val otp: StateFlow<String> = _otp.asStateFlow()

    private val _showPasteHint = MutableStateFlow(false)
    val showPasteHint: StateFlow<Boolean> = _showPasteHint.asStateFlow()

    private val _shake = MutableStateFlow(false)
    val shake: StateFlow<Boolean> = _shake.asStateFlow()


    private val _otpDigits = MutableStateFlow(List(OTP_LENGTH) { "" })
    val otpDigits: StateFlow<List<String>> = _otpDigits.asStateFlow()

    private val _remainingSeconds = MutableStateFlow(RESEND_DELAY_SECONDS)
    val remainingSeconds: StateFlow<Int> = _remainingSeconds.asStateFlow()

    private val _isResendEnabled = MutableStateFlow(false)
    val isResendEnabled: StateFlow<Boolean> = _isResendEnabled.asStateFlow()

    private val _isVerifyEnabled = MutableStateFlow(false)
    val isVerifyEnabled: StateFlow<Boolean> = _isVerifyEnabled.asStateFlow()

    private val _uiState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val uiState: StateFlow<UiState<String>> = _uiState.asStateFlow()

    // One-time success navigation
    private val _navigateToDashboard = MutableStateFlow(false)
    val navigateToDashboard: StateFlow<Boolean> = _navigateToDashboard.asStateFlow()

    init {
        startResendTimer()
    }

    fun onOtpChange(value: String) {
        if (value.length > OTP_LENGTH || !value.all { it.isDigit() }) return

        _otp.value = value
        _isVerifyEnabled.value = value.length == OTP_LENGTH

        if (value.length > 1) {
            _showPasteHint.value = true
        }
    }

    private fun triggerShake() {
        viewModelScope.launch {
            _shake.value = true
            delay(400)
            _shake.value = false
        }
    }

    private fun updateVerifyEnabled() {
        _isVerifyEnabled.value = _otpDigits.value.all { it.isNotEmpty() }
    }

    private fun startResendTimer() {
        viewModelScope.launch {
            while (_remainingSeconds.value > 0) {
                delay(1000)
                _remainingSeconds.value -= 1
            }
            _isResendEnabled.value = true
        }
    }

    fun onResendClick() {
        if (_isResendEnabled.value) {
            // TODO: Call resend OTP API
            println("Resending OTP...")
            _remainingSeconds.value = RESEND_DELAY_SECONDS
            _isResendEnabled.value = false
            _otpDigits.value = List(OTP_LENGTH) { "" }
            startResendTimer()
        }
    }

    fun onVerifyClick(phoneNumber: String) {
        if (_otp.value.length != OTP_LENGTH) {
            triggerShake()
            return
        }
        // TODO: Call verify OTP API and navigate to Dashboard on success
        println("Verifying OTP: ${_otp.value}")
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            val result = authRepository.verifyOtp(phoneNumber, "${_otp.value}")
            result.fold(
                onSuccess = { token ->
                    _uiState.value = UiState.Success(token)
                    _navigateToDashboard.value = true
                },
                onFailure = { exception ->
                    _uiState.value = UiState.Error(exception.message ?: "Invalid OTP")
                    // Optional: shake animation
                    Log.e("VerifyOtpViewModel", "Error verifying OTP : " + exception.message)
                    triggerShake()
                }
            )
        }
    }

    fun onDashboardNavigated() {
        _navigateToDashboard.value = false
        _uiState.value = UiState.Idle
    }

    fun resetUiState() {
        _uiState.value = UiState.Idle
    }
}