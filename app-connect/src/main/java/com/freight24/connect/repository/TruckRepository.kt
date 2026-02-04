package com.freight24.connect.repository

import com.freight24.rest.response.SearchTruckResponse
import com.freight24.rest.response.TruckDetailResponse

interface TruckRepository {
    suspend fun searchTrucks(query: String): Result<List<SearchTruckResponse.Truck>>

    suspend fun getTruckDetail(id: String): Result<TruckDetailResponse>
}