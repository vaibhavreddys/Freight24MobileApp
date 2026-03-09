package com.freight24.rest.api

import com.freight24.rest.response.TruckOwnerResponse
import com.freight24.rest.response.ProfileResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface TruckOwnerApi {
    @GET("truck-owners/list")
    suspend fun getTruckOwners(): Response<TruckOwnerResponse>

    @GET("truck-owners/search")
    suspend fun searchTruckOwners(
        @Query("q") query: String
    ): Response<TruckOwnerResponse>

    @GET("truck-owners/{userId}")
    suspend fun getTruckOwnerProfile(
        @Path("userId") userId: String
    ): Response<ProfileResponse>
}
