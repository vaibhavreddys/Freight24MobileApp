package com.freight24.rest.api

import com.freight24.rest.request.WithdrawInterestRequest
import com.freight24.rest.response.BaseResponse
import com.freight24.rest.response.IndentInterestInboxResponse
import com.freight24.rest.response.TruckerInboxInterestResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface IndentInterestApi {
    @GET("indent-interests/inbox")
    suspend fun getIndentInterestInbox(
        @Query("indent_id") indentId: String? = null,
    ): Response<IndentInterestInboxResponse>

    @POST("indent-interests/employee-withdraw")
    suspend fun withdrawInterest(
        @Body request: WithdrawInterestRequest,
    ): Response<BaseResponse>

    @GET("indent-interests/inbox/truckers")
    suspend fun getTruckersInterestInbox(
        @Query("indent_id") indentId: String? = null,
    ): Response<TruckerInboxInterestResponse>
}
