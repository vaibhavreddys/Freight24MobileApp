package com.freight24.rest.api

import com.freight24.rest.response.VerificationTripsResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface VerificationApi {
    @GET("verifications/trips")
    suspend fun getVerificationTrips(
        @Query("status") status: String? = null,
    ): Response<VerificationTripsResponse>
}
