package com.freight24.connect.feature.login.domain.model


sealed class LoginResult {
    data object Success : LoginResult()
    data class Error(val message: String): LoginResult()
}