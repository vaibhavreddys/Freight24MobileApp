package com.freight24.trucker.feature.login.view.verify

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freight24.notifications.AppNotification
import com.freight24.notifications.NotificationDispatcher
import com.freight24.trucker.feature.login.data.models.LoginResult
import com.freight24.trucker.feature.login.data.models.VerifyOtpResult
import com.freight24.trucker.feature.login.domain.AuthErrorMessage
import com.freight24.trucker.feature.login.domain.usecase.LoginUseCase
import com.freight24.trucker.feature.login.domain.usecase.LoginUseCaseParams
import com.freight24.trucker.feature.login.domain.usecase.VerifyOtpUseCase
import com.freight24.trucker.feature.login.domain.usecase.VerifyOtpUseCaseParams
import com.freight24.ui.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class ButtonState {
    Idle,
    Loading,
    Success,
    Failure,
}

@HiltViewModel
internal class VerifyOtpViewModel
    @Inject
    constructor(
        private val verifyOtpUseCase: VerifyOtpUseCase,
        private val loginUseCase: LoginUseCase,
        private val notificationDispatcher: NotificationDispatcher,
    ) : ViewModel() {
        companion object {
            private const val OTP_LENGTH = 6
            private const val RESEND_DELAY_SECONDS = 30
            private const val VERIFY_WINDOW_SECONDS = OTP_VERIFY_WINDOW_SECONDS
            private val ATTEMPTS_REGEX = Regex("""(\d+)\s*attempts?\s*remaining""", RegexOption.IGNORE_CASE)
        }

        private val _otp = MutableStateFlow("")
        val otp: StateFlow<String> = _otp.asStateFlow()

        private val _shake = MutableStateFlow(false)
        val shake: StateFlow<Boolean> = _shake.asStateFlow()

        private val _remainingSeconds = MutableStateFlow(RESEND_DELAY_SECONDS)
        val remainingSeconds: StateFlow<Int> = _remainingSeconds.asStateFlow()

        /** 5-minute window from when the OTP was sent. Drives the expiry
         *  meter — once it hits zero the user has to resend to continue. */
        private val _verificationSecondsRemaining = MutableStateFlow(VERIFY_WINDOW_SECONDS)
        val verificationSecondsRemaining: StateFlow<Int> = _verificationSecondsRemaining.asStateFlow()

        private val _isResendEnabled = MutableStateFlow(false)
        val isResendEnabled: StateFlow<Boolean> = _isResendEnabled.asStateFlow()

        private val _isVerifyEnabled = MutableStateFlow(false)
        val isVerifyEnabled: StateFlow<Boolean> = _isVerifyEnabled.asStateFlow()

        val buttonState: StateFlow<ButtonState> get() = _buttonState
        private val _buttonState = MutableStateFlow(ButtonState.Idle)

        val errorMessage: StateFlow<String?> get() = _errorMessage
        private val _errorMessage = MutableStateFlow<String?>(null)

        val attemptsRemaining: StateFlow<Int?> get() = _attemptsRemaining
        private val _attemptsRemaining = MutableStateFlow<Int?>(null)

        private val _uiState = MutableStateFlow<UiState<String>>(UiState.Idle)
        val uiState: StateFlow<UiState<String>> = _uiState.asStateFlow()

        private val _navigateToDashboard = MutableStateFlow(false)
        val navigateToDashboard: StateFlow<Boolean> = _navigateToDashboard.asStateFlow()

        private val _isResending = MutableStateFlow(false)
        val isResending: StateFlow<Boolean> = _isResending.asStateFlow()

        // Timer jobs are held so a resend cancels the previous countdowns
        // before starting new ones. Without this the old and new loops both
        // decrement the same StateFlow and the timer runs at double speed.
        private var resendTimerJob: Job? = null
        private var verifyWindowJob: Job? = null

        init {
            startResendTimer()
            startVerifyWindowTimer()
        }

        fun onOtpChange(value: String) {
            if (value.length > OTP_LENGTH || !value.all { it.isDigit() }) return

            _otp.value = value
            _isVerifyEnabled.value = value.length == OTP_LENGTH
            _buttonState.value = ButtonState.Idle
            _errorMessage.value = null
            _attemptsRemaining.value = null
        }

        private fun triggerShake() {
            viewModelScope.launch {
                _shake.value = true
                delay(400)
                _shake.value = false
            }
        }

        private fun startResendTimer() {
            resendTimerJob?.cancel()
            resendTimerJob = viewModelScope.launch {
                while (_remainingSeconds.value > 0) {
                    delay(1000)
                    _remainingSeconds.value -= 1
                }
                _isResendEnabled.value = true
            }
        }

        private fun startVerifyWindowTimer() {
            verifyWindowJob?.cancel()
            verifyWindowJob = viewModelScope.launch {
                while (_verificationSecondsRemaining.value > 0) {
                    delay(1000)
                    _verificationSecondsRemaining.value -= 1
                }
            }
        }

        @SuppressLint("MissingPermission")
        fun onResendClick(phoneNumber: String) {
            if (!_isResendEnabled.value || _isResending.value) return

            viewModelScope.launch {
                _isResending.value = true
                _errorMessage.value = null
                _attemptsRemaining.value = null

                val result = loginUseCase.invoke(LoginUseCaseParams(phoneNumber))
                result.fold(
                    onSuccess = { loginResult ->
                        when (loginResult) {
                            is LoginResult.Success -> {
                                notificationDispatcher.show(
                                    AppNotification.Critical(
                                        message = "OTP for $phoneNumber is ${loginResult.otp}",
                                    ),
                                )
                                Log.d("VerifyOtpViewModel", "Resend OTP: ${loginResult.otp}")
                                _otp.value = ""
                                _buttonState.value = ButtonState.Idle
                                _remainingSeconds.value = RESEND_DELAY_SECONDS
                                _verificationSecondsRemaining.value = VERIFY_WINDOW_SECONDS
                                _isResendEnabled.value = false
                                startResendTimer()
                                startVerifyWindowTimer()
                            }
                            is LoginResult.Error -> {
                                _errorMessage.value = loginResult.message
                                _isResendEnabled.value = true
                            }
                        }
                    },
                    onFailure = { exception ->
                        Log.e("VerifyOtpViewModel", "Error resending OTP: ${exception.message}")
                        _errorMessage.value = AuthErrorMessage.from(exception)
                        _isResendEnabled.value = true
                    },
                )
                _isResending.value = false
            }
        }

        fun onVerifyClick(phoneNumber: String) {
            if (_otp.value.length != OTP_LENGTH) {
                triggerShake()
                return
            }
            if (_buttonState.value != ButtonState.Idle) return

            viewModelScope.launch {
                _buttonState.value = ButtonState.Loading
                _errorMessage.value = null
                _attemptsRemaining.value = null

                val result = verifyOtpUseCase.invoke(VerifyOtpUseCaseParams(phoneNumber, _otp.value))
                result.fold(
                    onSuccess = { verifyOtpResult ->
                        when (verifyOtpResult) {
                            is VerifyOtpResult.InvalidOtp -> {
                                _attemptsRemaining.value = parseAttempts(verifyOtpResult.message)
                                _errorMessage.value = "Wrong OTP"
                                _buttonState.value = ButtonState.Failure
                                triggerShake()
                            }
                            is VerifyOtpResult.OtpExpired -> {
                                _errorMessage.value = verifyOtpResult.message
                                _buttonState.value = ButtonState.Failure
                                triggerShake()
                            }
                            is VerifyOtpResult.Error -> {
                                _errorMessage.value = verifyOtpResult.message
                                _buttonState.value = ButtonState.Failure
                                triggerShake()
                            }
                            is VerifyOtpResult.Success -> {
                                _errorMessage.value = null
                                _buttonState.value = ButtonState.Success
                                delay(600)
                                _navigateToDashboard.value = true
                            }
                        }
                    },
                    onFailure = { exception ->
                        Log.e("VerifyOtpViewModel", "Error verifying OTP : " + exception.message)
                        _errorMessage.value = AuthErrorMessage.from(exception)
                        _buttonState.value = ButtonState.Failure
                        triggerShake()
                    },
                )
            }
        }

        private fun parseAttempts(message: String): Int? {
            return ATTEMPTS_REGEX.find(message)?.groupValues?.get(1)?.toIntOrNull()
        }

        fun onDashboardNavigated() {
            _navigateToDashboard.value = false
            _uiState.value = UiState.Idle
            _buttonState.value = ButtonState.Idle
        }
    }
