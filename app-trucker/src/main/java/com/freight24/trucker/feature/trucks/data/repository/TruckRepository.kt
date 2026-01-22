package com.freight24.trucker.feature.trucks.data.repository

import com.freight24.rest.response.TruckDetailResponse
import com.freight24.rest.response.TruckResponse

interface TruckRepository {
    suspend fun fetchTrucks(): Result<TruckResponse>
    suspend fun fetchTruckDetail(id: String): Result<TruckDetailResponse>
}
