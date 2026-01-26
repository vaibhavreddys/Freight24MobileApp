package com.freight24.trucker.feature.trips.data.repository


import com.freight24.common.model.TripDetail
import com.freight24.rest.response.TripResponse

internal interface TripRepository {

    suspend fun fetchTrips(
        page: Int,
        forceRefresh: Boolean = false,
    ): Result<TripResponse>

    suspend fun fetchTripDetail(
        tripId: String,
        forceRefresh: Boolean = false,
    ): Result<TripDetail>

    suspend fun updateTripStatus(
        tripId: String,
        status: String,
    ): Result<TripDetail>

}