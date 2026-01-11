package com.freight24.rest.api

import com.freight24.rest.response.TripResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface TripApi {

    @GET("trips/trucker/list")
    suspend fun getTrips(
        @Query("page") page: Int,
        @Query("limit") limit: Int = 20,
    ): Response<TripResponse>

}