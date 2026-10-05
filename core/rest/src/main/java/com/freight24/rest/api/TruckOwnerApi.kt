package com.freight24.rest.api

import com.freight24.rest.request.UpdateProfileRequest
import com.freight24.rest.request.UpdateTruckOwnerStatusRequest
import com.freight24.rest.response.BaseResponse
import com.freight24.rest.response.CheckFieldResponse
import com.freight24.rest.response.ProfileResponse
import com.freight24.rest.response.TruckOwnerResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface TruckOwnerApi {
    @GET("truck-owners/list")
    suspend fun getTruckOwners(): Response<TruckOwnerResponse>

    @GET("truck-owners/search")
    suspend fun searchTruckOwners(
        @Query("q") query: String,
    ): Response<TruckOwnerResponse>

    @GET("truck-owners/{userId}")
    suspend fun getTruckOwnerProfile(
        @Path("userId") userId: String,
    ): Response<ProfileResponse>

    @POST("truck-owners/")
    suspend fun createTruckOwner(
        @Body request: UpdateProfileRequest,
    ): Response<BaseResponse>

    @PATCH("truck-owners/{userId}/status")
    suspend fun updateTruckOwnerStatus(
        @Path("userId") userId: String,
        @Body request: UpdateTruckOwnerStatusRequest,
    ): Response<BaseResponse>

    @GET("truck-owners/check-field")
    suspend fun checkTruckOwnerField(
        @Query("field") field: String,
        @Query("value") value: String,
        @Query("exclude_id") excludeId: String? = null,
    ): Response<CheckFieldResponse>
}
