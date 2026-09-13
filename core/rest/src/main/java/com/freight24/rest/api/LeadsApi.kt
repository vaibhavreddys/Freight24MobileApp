package com.freight24.rest.api

import com.freight24.rest.request.CallOutcomeRequest
import com.freight24.rest.request.ConvertLeadRequest
import com.freight24.rest.request.UpdateLeadDetailsRequest
import com.freight24.rest.response.BaseResponse
import com.freight24.rest.response.CallOutcomeResponse
import com.freight24.rest.response.ConvertLeadResponse
import com.freight24.rest.response.LeadDetailResponse
import com.freight24.rest.response.LeadListResponse
import com.freight24.rest.response.LeadSummaryResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface LeadsApi {
    @GET("leads")
    suspend fun getLeads(
        @Query("status") status: String? = null,
        @Query("q") search: String? = null,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
    ): Response<LeadListResponse>

    @GET("leads/summary")
    suspend fun getLeadSummary(): Response<LeadSummaryResponse>

    @GET("leads/{leadId}")
    suspend fun getLeadDetail(
        @Path("leadId") leadId: String,
    ): Response<LeadDetailResponse>

    @PATCH("leads/{leadId}")
    suspend fun updateLeadDetails(
        @Path("leadId") leadId: String,
        @Body request: UpdateLeadDetailsRequest,
    ): Response<LeadDetailResponse>

    @POST("leads/{leadId}/call-outcome")
    suspend fun recordCallOutcome(
        @Path("leadId") leadId: String,
        @Body request: CallOutcomeRequest,
    ): Response<CallOutcomeResponse>

    @POST("leads/{leadId}/reopen")
    suspend fun reopenLead(
        @Path("leadId") leadId: String,
    ): Response<BaseResponse>

    @POST("leads/{leadId}/convert")
    suspend fun convertLead(
        @Path("leadId") leadId: String,
        @Body request: ConvertLeadRequest,
    ): Response<ConvertLeadResponse>
}
