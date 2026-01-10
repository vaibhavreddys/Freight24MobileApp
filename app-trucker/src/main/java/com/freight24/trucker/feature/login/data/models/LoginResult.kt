package com.freight24.trucker.feature.login.data.models

sealed class LoginResult {
    data class Success(val otp: String) : LoginResult()

    data class Error(val message: String) : LoginResult()
}
