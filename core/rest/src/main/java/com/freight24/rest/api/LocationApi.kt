package com.freight24.rest.api

import com.freight24.rest.response.LocationResponse
import com.freight24.rest.response.LocationSearchResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface LocationApi {
    @GET("locations/search")
    suspend fun searchLocations(
        @Query("q") query: String,
    ): Response<LocationResponse>

    @GET("locations/search")
    suspend fun searchCities(
        @Query("q") query: String,
        @Query("type") type: String? = null,
    ): Response<LocationSearchResponse>
}
