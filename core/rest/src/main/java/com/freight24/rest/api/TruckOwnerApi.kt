package com.freight24.rest.api

import com.freight24.rest.response.TruckOwnerResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface TruckOwnerApi {
    @GET("truck-owners")
    suspend fun getTruckOwners(): Response<TruckOwnerResponse>

    @GET("truck-owners/search")
    suspend fun searchTruckOwners(
        @Query("q") query: String
    ): Response<TruckOwnerResponse>
}
