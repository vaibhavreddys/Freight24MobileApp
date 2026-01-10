package com.freight24.trucker.feature.login.data.models

sealed class VerifyOtpResult {
    data class Success(
        val isNewUser: Boolean,
        val token: String,
    ) : VerifyOtpResult()

    data class OtpExpired(
        val message: String,
    ) : VerifyOtpResult()

    data class InvalidOtp(
        val message: String,
    ) : VerifyOtpResult()

    data class Error(
        val message: String,
    ) : VerifyOtpResult()
}
