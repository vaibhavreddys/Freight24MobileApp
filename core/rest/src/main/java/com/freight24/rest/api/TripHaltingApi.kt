package com.freight24.rest.api

import com.freight24.rest.request.HaltingRequest
import com.freight24.rest.response.TruckHaltingRateResponse
import com.freight24.rest.response.UpdateHaltingRatesResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface TripHaltingApi {
    @GET("trips/halting-rate/{truck_type_id}")
    suspend fun getHaltingRate(
        @Path("truck_type_id") truckTypeId: String,
    ): Response<TruckHaltingRateResponse>

    @POST("trips/{trip_id}/halting/bulk")
    suspend fun addHaltingCharges(
        @Path("trip_id") tripId: String,
        @Body request: HaltingRequest,
    ): Response<UpdateHaltingRatesResponse>
}
