// core/network/src/main/java/com/freight24/core/network/api/AuthApi.kt

package com.freight24.core.network.api

import com.freight24.core.network.model.AuthResponse
import com.freight24.core.network.model.RequestOtpRequest
import com.freight24.core.network.model.VerifyOtpRequest
import retrofit2.http.Body
import retrofit2.http.POST

public interface AuthApi {

    @POST("auth/request-otp")
    suspend fun requestOtp(
        @Body request: RequestOtpRequest
    ): AuthResponse

    @POST("auth/verify-otp")
    suspend fun verifyOtp(
        @Body request: VerifyOtpRequest
    ): AuthResponse
}