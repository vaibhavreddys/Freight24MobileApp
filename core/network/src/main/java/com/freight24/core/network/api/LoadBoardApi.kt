package com.freight24.core.network.api

import com.freight24.core.network.model.LoadBoardResponse
import com.freight24.core.network.model.LoadBoardSummaryResponse
import com.freight24.core.network.model.PaginatedLoadResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface LoadBoardApi {

    @GET("indents/trucker/load-board")
    suspend fun getLoadBoard(): LoadBoardResponse

    @GET("indents/trucker/load-board-summary")
    suspend fun getLoadBoardSummary(): LoadBoardSummaryResponse

    @GET("indents/trucker/load-board-paginated")
    suspend fun getPaginatedLoads(
        @Query("operational_city_id") cityId: Int,
        @Query("page") page: Int
    ): PaginatedLoadResponse

}
