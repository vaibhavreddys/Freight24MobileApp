package com.freight24.connect.feature.login.repository

import com.freight24.rest.response.LoginResponse

internal interface LoginConnectRepository {

    suspend fun requestOtp(phoneNumber: String): LoginResponse

    suspend fun verifyOtp(phoneNumber: String, otp: String): LoginResponse

}