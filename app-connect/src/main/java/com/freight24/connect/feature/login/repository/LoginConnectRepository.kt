package com.freight24.connect.feature.login.repository

import com.freight24.connect.feature.login.domain.model.AuthInfo

internal interface LoginConnectRepository {

    suspend fun requestOtp(phoneNumber: String): Result<Unit>

    suspend fun verifyOtp(phoneNumber: String, otp: String): Result<AuthInfo>

}