package com.freight24.connect.feature.login.view.signin

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freight24.connect.feature.login.domain.usecase.LoginConnectUseCase
import com.freight24.connect.feature.login.domain.usecase.LoginConnectUseCaseParams
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
        private val loginConnectUseCase: LoginConnectUseCase,
    ) : ViewModel() {
        private val _phoneNumber = MutableStateFlow("")
        val phoneNumber: StateFlow<String> = _phoneNumber.asStateFlow()

        private val _uiState = MutableStateFlow<UiState<String>>(UiState.Idle)
        val uiState: StateFlow<UiState<String>> = _uiState.asStateFlow()

        fun onPhoneNumberChange(newValue: String) {
            // Simple validation to allow only digits and max length if needed
            if (newValue.all { it.isDigit() } && newValue.length <= 10) {
                _phoneNumber.value = newValue
            }
        }

        fun onSendOtpClick() {
            viewModelScope.launch {
                val phone = _phoneNumber.value
                if (phone.length == 10) {
                    loginConnectUseCase.invoke(
                        LoginConnectUseCaseParams("+91$phone"),
                    ).fold(
                        onSuccess = {
                            Log.e("LoginViewModel", "Notification sent")
                            _uiState.value = UiState.Success(phone)
                            // _navigateToVerifyOtp.value = fullPhone
                        },
                        onFailure = { exception ->
                            _uiState.value =
                                UiState.Error(
                                    exception.message ?: "Failed to send OTP",
                                )
                        },
                    )
                } else {
                    // TODO: Show error state
                }
            }
        }
    }
