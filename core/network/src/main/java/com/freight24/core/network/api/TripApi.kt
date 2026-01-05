package com.freight24.core.network.api

import com.freight24.core.network.model.TripsResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface TripApi {
    @GET("trips/trucker/list")
    suspend fun getTrips(
        @Query("page") page: Int,
        @Query("limit") limit: Int = 20
    ): TripsResponse
}
