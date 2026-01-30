package com.freight24.rest.api

import com.freight24.rest.request.RegisterDeviceRequest
import com.freight24.rest.response.BaseResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface NotificationServiceApi {
    @POST("notifications/register-device")
    suspend fun registerDevice(
        @Body request: RegisterDeviceRequest,
    ): BaseResponse
}
