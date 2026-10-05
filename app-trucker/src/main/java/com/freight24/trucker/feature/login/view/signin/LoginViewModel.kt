package com.freight24.trucker.feature.login.view.signin

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freight24.notifications.AppNotification
import com.freight24.notifications.NotificationDispatcher
import com.freight24.trucker.feature.login.data.models.LoginResult
import com.freight24.trucker.feature.login.domain.AuthErrorMessage
import com.freight24.trucker.feature.login.domain.usecase.LoginUseCase
import com.freight24.trucker.feature.login.domain.usecase.LoginUseCaseParams
import com.freight24.ui.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class LoginViewModel
    @Inject
    constructor(
        private val loginUseCase: LoginUseCase,
        private val notificationDispatcher: NotificationDispatcher,
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

        @SuppressLint("MissingPermission")
        fun onSendOtpClick() {
            val phone = _phoneNumber.value
            if (phone.length != MAX_PHONE_LENGTH) {
                _uiState.value = UiState.Error("Please enter a valid 10-digit number")
                return
            }

            val fullPhone = "+91$phone"

            viewModelScope.launch {
                _uiState.value = UiState.Loading
                loginUseCase.invoke(LoginUseCaseParams(fullPhone)).fold(
                    onSuccess = { response ->
                        Log.e("LoginViewModel", "Response: $response")
                        // LoginUseCaseImpl wraps failures as LoginResult.Error
                        // inside a *successful* Result, so both branches of
                        // the sealed type must be handled here. Matching only
                        // Success left _uiState on Loading and the CTA spun
                        // forever whenever the backend was unreachable.
                        when (response) {
                            is LoginResult.Success -> {
                                notificationDispatcher.show(
                                    AppNotification.Critical(
                                        message = "OTP for $fullPhone is ${response.otp}",
                                    ),
                                )
                                Log.e("LoginViewModel", "Notification sent")
                                _uiState.value = UiState.Success(Unit)
                                _navigateToVerifyOtp.value = fullPhone
                            }
                            is LoginResult.Error ->
                                _uiState.value = UiState.Error(response.message)
                        }
                    },
                    onFailure = { exception ->
                        _uiState.value =
                            UiState.Error(
                                AuthErrorMessage.from(exception),
                            )
                    },
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
