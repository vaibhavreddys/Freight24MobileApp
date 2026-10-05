package com.freight24.connect.feature.login_new.viewmodel

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freight24.auth.SessionManager
import com.freight24.connect.navigation.ConnectDestination
import com.freight24.notifications.AppNotification
import com.freight24.notifications.NotificationDispatcher
import com.freight24.rest.api.LoginConnectApi
import com.freight24.rest.request.OtpRequest
import com.freight24.rest.request.VerifyOtpRequest
import com.freight24.ui.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.HttpException
import javax.inject.Inject

@HiltViewModel
internal class NewOtpViewModel
    @Inject
    constructor(
        private val loginConnectApi: LoginConnectApi,
        private val sessionManager: SessionManager,
        @ApplicationContext private val context: Context,
        savedStateHandle: SavedStateHandle,
    ) : ViewModel() {
        companion object {
            private const val OTP_LENGTH = 6
            private const val VERIFY_TIMER_SECONDS = 300
            private const val RESEND_TIMER_SECONDS = 30
            private const val MAX_ATTEMPTS = 3
            private const val FULL_PHONE_PREFIX = "+91"
        }

        private val phoneNumber = savedStateHandle.get<String>(ConnectDestination.NewOtp.PHONE_ARG).orEmpty()

        private val _otp = MutableStateFlow("")
        val otp: StateFlow<String> = _otp.asStateFlow()

        private val _shake = MutableStateFlow(false)
        val shake: StateFlow<Boolean> = _shake.asStateFlow()

        private val _verificationSecondsRemaining = MutableStateFlow(VERIFY_TIMER_SECONDS)
        val verificationSecondsRemaining: StateFlow<Int> = _verificationSecondsRemaining.asStateFlow()

        private val _resendSecondsRemaining = MutableStateFlow(RESEND_TIMER_SECONDS)
        val resendSecondsRemaining: StateFlow<Int> = _resendSecondsRemaining.asStateFlow()

        private val _attemptsRemaining = MutableStateFlow(MAX_ATTEMPTS)
        val attemptsRemaining: StateFlow<Int> = _attemptsRemaining.asStateFlow()

        private val _isVerifyEnabled = MutableStateFlow(false)
        val isVerifyEnabled: StateFlow<Boolean> = _isVerifyEnabled.asStateFlow()

        private val _isResendEnabled = MutableStateFlow(false)
        val isResendEnabled: StateFlow<Boolean> = _isResendEnabled.asStateFlow()

        private val _isResending = MutableStateFlow(false)
        val isResending: StateFlow<Boolean> = _isResending.asStateFlow()

        private val _uiState = MutableStateFlow<UiState<String>>(UiState.Idle)
        val uiState: StateFlow<UiState<String>> = _uiState.asStateFlow()

        private var verificationTimerJob: Job? = null
        private var resendTimerJob: Job? = null

        init {
            startVerificationTimer()
            startResendTimer()
            refreshActionState()
        }

        fun onOtpChange(input: String) {
            val digitsOnly = input.filter(Char::isDigit).take(OTP_LENGTH)
            _otp.value = digitsOnly
            refreshActionState()
        }

        fun onVerifyClick() {
            if (!isVerifyEnabled.value) {
                triggerShake()
                return
            }

            viewModelScope.launch {
                _uiState.value = UiState.Loading
                try {
                    val installationId = sessionManager.ensureInstallationId()
                    val response =
                        loginConnectApi.verifyOtp(
                            VerifyOtpRequest(
                                phone_number = "$FULL_PHONE_PREFIX$phoneNumber",
                                otp = _otp.value,
                                installation_id = installationId,
                            ),
                        )

                    if (!response.success) {
                        // Use the message from the API response
                        val errorMessage = response.message.ifBlank { "Invalid OTP" }
                        registerFailedAttempt(errorMessage)
                        return@launch
                    }

                    val data = response.data
                    val token = data?.access_token ?: data?.token.orEmpty()
                    val userId = data?.user?.id.orEmpty()

                    if (token.isBlank() || userId.isBlank()) {
                        registerFailedAttempt("Invalid server response")
                        return@launch
                    }

                    if (data != null) {
                        sessionManager.saveSession(
                            phone = "$FULL_PHONE_PREFIX$phoneNumber",
                            userId = userId,
                            authData = data,
                            installationId = installationId,
                        )
                    } else {
                        sessionManager.saveSession(
                            token = token,
                            phone = "$FULL_PHONE_PREFIX$phoneNumber",
                            userId = userId,
                        )
                    }

                    _uiState.value = UiState.Success(token)
                    refreshActionState()
                } catch (httpException: HttpException) {
                    // Handle HTTP 400 error and extract message from response body
                    val errorMessage = extractErrorMessageFromHttpException(httpException)
                    _uiState.value = UiState.Error(errorMessage)
                    triggerShake()
                } catch (exception: Exception) {
                    _uiState.value = UiState.Error(exception.message ?: "Failed to verify OTP")
                    triggerShake()
                }
            }
        }

        // Helper function to extract error message from HTTP exception
        private fun extractErrorMessageFromHttpException(exception: HttpException): String {
            return try {
                val errorBody = exception.response()?.errorBody()?.string()
                if (!errorBody.isNullOrEmpty()) {
                    val jsonObject = JSONObject(errorBody)
                    val message = jsonObject.optString("message")
                    if (message.isNotEmpty()) {
                        return message
                    }
                }
                "Invalid OTP. Please try again."
            } catch (e: Exception) {
                "Invalid OTP. Please try again."
            }
        }

        fun onResendClick() {
            if (!isResendEnabled.value || _isResending.value) return

            viewModelScope.launch {
                _isResending.value = true
                try {
                    val response =
                        loginConnectApi.requestOtp(
                            OtpRequest(phone_number = "$FULL_PHONE_PREFIX$phoneNumber"),
                        )

                    if (response.success) {
                        val otpCode = response.data?.otp_code
                        if (!otpCode.isNullOrBlank()) {
                            val fullPhone = "$FULL_PHONE_PREFIX$phoneNumber"
                            NotificationDispatcher(context).show(
                                AppNotification.Critical(message = "OTP for $fullPhone is $otpCode"),
                            )
                        }
                        resetForNewOtp()
                    } else {
                        _uiState.value = UiState.Error(response.message.ifBlank { "Failed to resend OTP" })
                    }
                } catch (exception: Exception) {
                    _uiState.value = UiState.Error(exception.message ?: "Failed to resend OTP")
                } finally {
                    _isResending.value = false
                }
            }
        }

        fun resetUiState() {
            _uiState.value = UiState.Idle
        }

        fun markNavigationHandled() {
            _uiState.value = UiState.Idle
        }

        private fun resetForNewOtp() {
            _otp.value = "" // Fixed: removed space
            _attemptsRemaining.value = MAX_ATTEMPTS
            _verificationSecondsRemaining.value = VERIFY_TIMER_SECONDS
            _resendSecondsRemaining.value = RESEND_TIMER_SECONDS
            startVerificationTimer()
            startResendTimer()
            refreshActionState()
        }

        private fun registerFailedAttempt(message: String) {
            _attemptsRemaining.value = (_attemptsRemaining.value - 1).coerceAtLeast(0)
            _uiState.value = UiState.Error(message)
            triggerShake()
            refreshActionState()
        }

        private fun refreshActionState() {
            _isVerifyEnabled.value =
                _otp.value.length == OTP_LENGTH &&
                _verificationSecondsRemaining.value > 0 &&
                _attemptsRemaining.value > 0 &&
                !_isResending.value

            _isResendEnabled.value = _resendSecondsRemaining.value == 0 && !_isResending.value
        }

        private fun startVerificationTimer() {
            verificationTimerJob?.cancel()
            verificationTimerJob =
                viewModelScope.launch {
                    while (isActive && _verificationSecondsRemaining.value > 0) {
                        delay(1000)
                        _verificationSecondsRemaining.value = (_verificationSecondsRemaining.value - 1).coerceAtLeast(0)
                        refreshActionState()
                    }
                    refreshActionState()
                }
        }

        private fun startResendTimer() {
            resendTimerJob?.cancel()
            resendTimerJob =
                viewModelScope.launch {
                    while (isActive && _resendSecondsRemaining.value > 0) {
                        delay(1000)
                        _resendSecondsRemaining.value = (_resendSecondsRemaining.value - 1).coerceAtLeast(0)
                        refreshActionState()
                    }
                    refreshActionState()
                }
        }

        private fun triggerShake() {
            viewModelScope.launch {
                _shake.value = true
                delay(350)
                _shake.value = false
            }
        }
    }
