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
}
