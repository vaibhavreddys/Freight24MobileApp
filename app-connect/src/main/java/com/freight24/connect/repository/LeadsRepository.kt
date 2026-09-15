package com.freight24.connect.repository

import com.freight24.rest.request.CallOutcomeRequest
import com.freight24.rest.request.ConvertLeadRequest
import com.freight24.rest.request.UpdateLeadDetailsRequest
import com.freight24.rest.response.CallOutcomeData
import com.freight24.rest.response.CityResult
import com.freight24.rest.response.ConvertLeadData
import com.freight24.rest.response.LeadDetailDto
import com.freight24.rest.response.LeadSummaryData
import com.freight24.rest.response.LeadTruckTypeDto

data class LeadsPage(
    val leads: List<com.freight24.rest.response.LeadDto>,
    val page: Int,
    val totalPages: Int,
    val total: Int,
)

interface LeadsRepository {
    suspend fun getLeads(
        status: String? = null,
        search: String? = null,
        page: Int = 1,
        limit: Int = 20,
    ): Result<LeadsPage>

    suspend fun getLeadSummary(): Result<LeadSummaryData>

    suspend fun getLeadDetail(leadId: String): Result<LeadDetailDto>

    suspend fun updateLeadDetails(leadId: String, request: UpdateLeadDetailsRequest): Result<LeadDetailDto>

    suspend fun recordCallOutcome(leadId: String, request: CallOutcomeRequest): Result<CallOutcomeData>

    suspend fun reopenLead(leadId: String): Result<Boolean>

    suspend fun convertLead(leadId: String, request: ConvertLeadRequest): Result<ConvertLeadData>

    /** Truck types with 24h Room caching for instant offline form loading. */
    suspend fun getTruckTypesCached(): Result<List<LeadTruckTypeDto>>

    /**
     * Live city search; results are merged into the Room cache.
     *
     * Returns the full [CityResult] rather than the trimmed `LeadCityDto` so the shared
     * city field can render its second line (state / parent city). Collapsing to
     * `LeadCityDto` threw that away and made the lead form's picker strictly worse than
     * the one the truck-owner profile already uses.
     */
    suspend fun searchCities(query: String): Result<List<CityResult>>

    /** Cached cities matching [query] — instant results while offline. */
    suspend fun searchCachedCities(query: String): List<CityResult>
}
