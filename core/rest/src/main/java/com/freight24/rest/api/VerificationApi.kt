package com.freight24.rest.api

import com.freight24.rest.request.UpdateVerificationRequestStatusRequest
import com.freight24.rest.response.BaseResponse
import com.freight24.rest.response.VerificationTripsResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path
import retrofit2.http.Query

interface VerificationApi {
    @GET("verifications/trips")
    suspend fun getVerificationTrips(
        @Query("status") status: String? = null,
    ): Response<VerificationTripsResponse>

    @PATCH("verifications/trips/{id}")
    suspend fun updateVerificationRequestStatus(
        @Path("id") id: String,
        @Body request: UpdateVerificationRequestStatusRequest,
    ): Response<BaseResponse>
}
