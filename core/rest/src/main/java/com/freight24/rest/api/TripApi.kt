package com.freight24.rest.api

import com.freight24.rest.response.TripDetailResponse
import com.freight24.rest.response.TripResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path
import retrofit2.http.Query

interface TripApi {

    @GET("trips/trucker/list")
    suspend fun getTrips(
        @Query("page") page: Int,
        @Query("limit") limit: Int = 20,
    ): Response<TripResponse>

    @GET("trips/trucker/trip/{tripId}")
    suspend fun getTripDetail(
        @Path("tripId") tripId: String,
    ): Response<TripDetailResponse>

    @PATCH("trips/{tripId}/trucker-status")
    suspend fun updateTripStatus(
        @Path("tripId") tripId: String,
        @Body request: com.freight24.rest.request.UpdateTripStatusRequest,
    ): Response<TripDetailResponse>

}