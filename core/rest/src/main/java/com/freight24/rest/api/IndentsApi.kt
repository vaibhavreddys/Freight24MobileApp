package com.freight24.rest.api

import com.freight24.rest.request.CloneIndentRequest
import com.freight24.rest.request.CreateIndentRequest
import com.freight24.rest.request.IndentInterestRequest
import com.freight24.rest.request.IndentLifecycleRequest
import com.freight24.rest.request.UpdateIndentGroupRequest
import com.freight24.rest.request.UpdateIndentRequest
import com.freight24.rest.response.BaseResponse
import com.freight24.rest.response.GetIndentsResponse
import com.freight24.rest.response.IndentInterestResponse
import com.freight24.rest.response.IndentResponse
import com.freight24.rest.response.LoadBoardResponse
import com.freight24.rest.response.MaterialsResponse
import com.freight24.rest.response.PaginatedLoadBoardResponse
import com.freight24.rest.response.TruckTypeResponse
import com.freight24.rest.response.TruckerIndentDetailResponse
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

    @POST("indent-interests/interested")
    suspend fun expressInterest(
        @Body request: IndentInterestRequest,
    ): Response<IndentInterestResponse>

    @POST("indent-interests/withdraw")
    suspend fun withdrawInterest(
        @Body request: IndentInterestRequest,
    ): Response<IndentInterestResponse>

    @GET("truck-types")
    suspend fun getTruckTypes(): Response<TruckTypeResponse>

    /**
     * Cargo categories for the indent's material. Served from a constant on the
     * backend for now, so the list is short enough to hand to a picker whole —
     * no paging, no search endpoint.
     */
    @GET("materials")
    suspend fun getMaterials(): Response<MaterialsResponse>

    @POST("indents/groups")
    suspend fun createIndent(
        @Body request: CreateIndentRequest,
    ): Response<IndentResponse>

    @GET("indents/my-indents")
    suspend fun getMyIndents(
        @Query("status") status: String,
        @Query("page") page: Int? = null,
        @Query("limit") limit: Int? = null,
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

    @POST("indents/{id}/cancel")
    suspend fun cancelIndent(
        @Path("id") id: String,
        @Body request: IndentLifecycleRequest,
    ): Response<BaseResponse>

    @POST("indents/{id}/reopen")
    suspend fun reopenIndent(
        @Path("id") id: String,
        @Body request: IndentLifecycleRequest,
    ): Response<BaseResponse>

    /**
     * Group-level update — the only write path that can change an indent's
     * contact roles. Backend accepts it while the group is `OPEN` and only from
     * the creator or an admin / operations manager.
     *
     * The body is a write confirmation, not an indent projection: unlike
     * `POST indents/groups` (which maps through `mapIndentResponse`) this route
     * returns the raw `indent_groups` row, snake_case and without the child-indent
     * fields (`indentCode`, `assignedTo`, `negotiatedTripCost`, …). Nothing here
     * reads it, so it is decoded as [BaseResponse] and the payload is ignored —
     * see [com.freight24.connect.repository.IndentRepository.updateIndentGroup].
     * Callers that need the updated state re-read `GET indents/child/{id}`.
     */
    @PUT("indents/groups/{groupId}")
    suspend fun updateIndentGroup(
        @Path("groupId") groupId: String,
        @Body request: UpdateIndentGroupRequest,
    ): Response<BaseResponse>

    @GET("indents/groups")
    suspend fun getAllIndents(
        @Query("page") page: Int,
        @Query("limit") limit: Int,
        @Query("status") status: String? = null,
    ): Response<GetIndentsResponse>

    @GET("indents/groups/inactive")
    suspend fun getInactiveIndents(
        @Query("page") page: Int,
        @Query("limit") limit: Int,
    ): Response<GetIndentsResponse>

    @POST("indents/groups/{groupId}/clone")
    suspend fun cloneIndent(
        @Path("groupId") indentGroupId: String,
        @Body request: CloneIndentRequest,
    ): Response<IndentResponse>

    @GET("indents/search")
    suspend fun searchIndents(
        @Query("q") query: String,
        @Query("priority") priority: String?,
        @Query("client_id") clientId: String?,
        @Query("assigned_to_me") assignedToMe: Boolean?,
    ): Response<GetIndentsResponse>

    @GET("indents/trucker/child/{id}")
    suspend fun getTruckerIndentDetail(
        @Path("id") id: String,
    ): Response<TruckerIndentDetailResponse>
}
