package com.freight24.rest.api

import com.freight24.rest.response.SearchTruckerResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface FindTrucksApi {

    @GET("search/matching/trucks")
    suspend fun searchTrucks(
        @Query("sourceAreaId") areaId: String?,
        @Query("sourceCityId") cityId: String?,
        @Query("destinationCityId") destinationCityId: String?,
        @Query("truckTypeId") truckTypeIds: String,
    ): Response<SearchTruckerResponse>

}