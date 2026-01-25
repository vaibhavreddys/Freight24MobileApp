package com.freight24.connect.feature.login.repository

import com.freight24.common.di.IoDispatcher
import com.freight24.rest.api.LoginConnectApi
import com.freight24.rest.request.OtpRequest
import com.freight24.rest.request.VerifyOtpRequest
import com.freight24.rest.response.LoginResponse
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

internal class LoginConnectRepositoryImpl
@Inject
constructor(
    private val loginConnectApi: LoginConnectApi,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : LoginConnectRepository {

    override suspend fun requestOtp(phoneNumber: String): LoginResponse {
       return withContext(ioDispatcher) {
           loginConnectApi.requestOtp(OtpRequest(phoneNumber))
       }
    }

    override suspend fun verifyOtp(
        phoneNumber: String,
        otp: String
    ): LoginResponse {
        return withContext(ioDispatcher) {
            loginConnectApi.verifyOtp(
                VerifyOtpRequest(
                    phone_number = phoneNumber,
                    otp = otp,
                )
            )
        }
    }
}