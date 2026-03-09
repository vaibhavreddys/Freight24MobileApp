package com.freight24.connect.repository

import com.freight24.rest.response.SearchTruckerResponse.TruckerData
import retrofit2.http.GET

interface FindTrucksRepository {

    @GET("search/matching/trucks")
    suspend fun searchTrucks(
        areaId: String?,
        cityId: String?,
        destinationCityId: String?,
        truckTypeId: String,
    ): Result<TruckerData>
}