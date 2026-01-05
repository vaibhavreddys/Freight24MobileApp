package com.freight24.trucker.feature.login

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freight24.core.auth.AuthRepository
import com.freight24.core.ui.UiState
import com.freight24.core.notifications.AppNotification
import com.freight24.core.notifications.NotificationDispatcher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val notificationDispatcher: NotificationDispatcher
) : ViewModel() {

    private val _phoneNumber = MutableStateFlow("")
    val phoneNumber: StateFlow<String> = _phoneNumber.asStateFlow()

    // One-time navigation event
    private val _uiState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val uiState: StateFlow<UiState<Unit>> = _uiState.asStateFlow()

    // One-time navigation event
    private val _navigateToVerifyOtp = MutableStateFlow<String?>(null)
    val navigateToVerifyOtp: StateFlow<String?> = _navigateToVerifyOtp.asStateFlow()

    companion object {
        const val MAX_PHONE_LENGTH = 10
    }

    fun onPhoneNumberChange(newValue: String) {
        val digitsOnly = newValue.filter { it.isDigit() }
        if (digitsOnly.length <= MAX_PHONE_LENGTH) {
            _phoneNumber.value = digitsOnly
        }
    }

    fun onSendOtpClick() {
        val phone = _phoneNumber.value
        if (phone.length != MAX_PHONE_LENGTH) {
            _uiState.value = UiState.Error("Please enter a valid 10-digit number")
            return
        }

        val fullPhone = "+91$phone"

        viewModelScope.launch {
            _uiState.value = UiState.Loading
            val result = authRepository.requestOtp(fullPhone)
            result.fold(
                onSuccess = { response ->
                    Log.e("LoginViewModel", "Response: $response")
                    notificationDispatcher.show(
                        AppNotification.Critical(
                            message = "OTP for $fullPhone is ${response.message?.otp_code}"
                        )
                    )
                    Log.e("LoginViewModel", "Notification sent")
                    _uiState.value = UiState.Success(Unit)
                    _navigateToVerifyOtp.value = fullPhone
                },
                onFailure = { exception ->
                    _uiState.value = UiState.Error(exception.message ?: "Failed to send OTP")
                }
            )
        }
    }

    // Call this after UI consumes the event
    fun onNavigationHandled() {
        _navigateToVerifyOtp.value = null
        _uiState.value = UiState.Idle
    }

    fun resetUiState() {
        _uiState.value = UiState.Idle
    }
}