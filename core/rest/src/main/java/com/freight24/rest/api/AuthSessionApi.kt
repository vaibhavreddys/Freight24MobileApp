package com.freight24.rest.api

import com.freight24.rest.request.LogoutRequest
import com.freight24.rest.request.RefreshSessionRequest
import com.freight24.rest.response.BaseResponse
import com.freight24.rest.response.LoginResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthSessionApi {
    @POST("auth/refresh")
    suspend fun refreshSession(
        @Body request: RefreshSessionRequest,
    ): LoginResponse

    @POST("auth/logout")
    suspend fun logout(
        @Body request: LogoutRequest,
    ): BaseResponse
}
