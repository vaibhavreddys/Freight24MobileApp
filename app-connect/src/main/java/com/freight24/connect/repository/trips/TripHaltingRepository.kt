package com.freight24.connect.repository.trips

import com.freight24.rest.response.TruckHaltingRate
import com.freight24.rest.response.UpdateHaltingRatesResponse

interface TripHaltingRepository {

    suspend fun getHaltingRate(
        truckTypeId: String
    ): Result<TruckHaltingRate>

    suspend fun addHaltingCharges(
        tripId: String,
        loadingDays: Int?,
        unloadingDays: Int?,
        ratePerDay: Int?,
        notes: String?
    ): Result<UpdateHaltingRatesResponse.UpdateHaltingRatesData>

}