package com.freight24.connect.repository

import com.freight24.rest.response.CreatedTripResponse

interface TripRepository {

    suspend fun createTrip(
        indentId: String,
        truckId: String,
        driverName: String?,
        driverPhone: String?): Result<CreatedTripResponse.TripData>
}