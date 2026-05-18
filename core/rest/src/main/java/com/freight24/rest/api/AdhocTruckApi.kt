package com.freight24.rest.api

import com.freight24.rest.response.AdhocTruckOwnerSearchResponse
import com.freight24.rest.response.BaseResponse
import com.freight24.rest.request.CreateAdhocTruckRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface AdhocTruckApi {
    @GET("adhoc-truck-entries/truck-owners/search")
    suspend fun searchTruckOwnersByPhone(
        @Query("phone") phone: String,
    ): Response<AdhocTruckOwnerSearchResponse>

    @POST("adhoc-truck-entries")
    suspend fun createAdhocTruckEntry(
        @Body request: CreateAdhocTruckRequest,
    ): Response<BaseResponse>
}
