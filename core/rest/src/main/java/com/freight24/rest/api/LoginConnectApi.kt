package com.freight24.rest.api

import com.freight24.rest.request.OtpRequest
import com.freight24.rest.request.VerifyOtpRequest
import com.freight24.rest.response.LoginResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface LoginConnectApi {

    @POST("auth/request-otp")
    suspend fun requestOtp(
        @Body request: OtpRequest,
    ): LoginResponse

    @POST("auth/employee-verify-otp")
    suspend fun verifyOtp(
        @Body request: VerifyOtpRequest,
    ): LoginResponse
}