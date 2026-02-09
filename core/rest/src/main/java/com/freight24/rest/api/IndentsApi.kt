package com.freight24.rest.api

import com.freight24.rest.request.CloneIndentRequest
import com.freight24.rest.request.CreateIndentRequest
import com.freight24.rest.request.UpdateIndentRequest
import com.freight24.rest.response.GetIndentsResponse
import com.freight24.rest.response.IndentResponse
import com.freight24.rest.response.IndentsListResponse
import com.freight24.rest.response.LoadBoardResponse
import com.freight24.rest.response.PaginatedLoadBoardResponse
import com.freight24.rest.response.TruckTypeResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface IndentsApi {
    @GET("indents/trucker/load-board-summary")
    suspend fun getLoadBoardSummary(): Response<LoadBoardResponse>

    @GET("indents/trucker/load-board-paginated")
    suspend fun getPaginatedLoads(
        @Query("operational_city_id") cityId: Int,
        @Query("page") page: Int,
    ): Response<PaginatedLoadBoardResponse>

    @GET("truck-types")
    suspend fun getTruckTypes(): Response<TruckTypeResponse>

    @POST("indents/groups")
    suspend fun createIndent(
        @Body request: CreateIndentRequest,
    ): Response<IndentResponse>

    @GET("indents/my-indents")
    suspend fun getMyIndents(
        @Query("status") status: String,
        @Query("page") page: Int ?= null,
        @Query("limit") limit: Int ?= null,
    ): Response<GetIndentsResponse>

    @GET("indents/child/{id}")
    suspend fun getIndentById(
        @Path("id") id: String,
    ): Response<IndentResponse>

    @PUT("indents/{id}")
    suspend fun updateIndent(
        @Path("id") id: String,
        @Body request: UpdateIndentRequest,
    ): Response<IndentResponse>

    @GET("indents/groups")
    suspend fun getAllIndents(
        @Query("page") page: Int,
        @Query("limit") limit: Int,
    ): Response<GetIndentsResponse>

    @POST("indents/groups/{groupId}/clone")
    suspend fun cloneIndent(
        @Path("groupId") indentGroupId: String,
        @Body request: CloneIndentRequest,
    ): Response<IndentResponse>
}
