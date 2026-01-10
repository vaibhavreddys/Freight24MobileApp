package com.freight24.trucker.feature.login.data.repository

import com.freight24.rest.response.LoginResponse

internal interface LoginRepository {
    suspend fun requestOtp(phoneNumber: String): LoginResponse

    suspend fun verifyOtp(
        phoneNumber: String,
        otp: String,
    ): LoginResponse
}
