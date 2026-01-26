package com.freight24.rest.api

import com.freight24.rest.response.LoadBoardResponse
import com.freight24.rest.response.PaginatedLoadBoardResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface IndentsApi {
    @GET("indents/trucker/load-board-summary")
    suspend fun getLoadBoardSummary(): Response<LoadBoardResponse>

    @GET("indents/trucker/load-board-paginated")
    suspend fun getPaginatedLoads(
        @Query("operational_city_id") cityId: Int,
        @Query("page") page: Int,
    ): Response<PaginatedLoadBoardResponse>
    @GET("indents/locations/search")
    suspend fun searchLocations(@Query("query") query: String): Response<List<com.freight24.rest.response.LocationDto>>

    @GET("indents/clients/search")
    suspend fun searchClients(@Query("query") query: String): Response<List<com.freight24.rest.response.ClientDto>>

    @GET("indents/truck-types")
    suspend fun getTruckTypes(): Response<List<com.freight24.rest.response.TruckTypeDto>>

    @retrofit2.http.POST("indents")
    suspend fun createIndent(@retrofit2.http.Body request: com.freight24.rest.request.CreateIndentRequest): Response<com.freight24.rest.response.BaseResponse>
}
