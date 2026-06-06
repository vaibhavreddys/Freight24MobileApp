package com.freight24.trucker.feature.loads.data.repository

import com.freight24.rest.response.LoadBoardResponse
import com.freight24.rest.response.IndentInterestResponse
import com.freight24.rest.response.PaginatedLoadBoardResponse
import com.freight24.rest.response.TruckerIndentDetailResponse

interface LoadBoardRepository {
    /**
     * Fetch load board summary (cities with load counts).
     * @param forceRefresh If true, bypasses cache and fetches from network
     */
    suspend fun fetchLoadBoardSummary(forceRefresh: Boolean = false): Result<LoadBoardResponse>

    /**
     * Fetch paginated loads for a specific city.
     * @param cityId The city to fetch loads for
     * @param page Page number (1-indexed)
     * @param forceRefresh If true, bypasses cache and fetches from network
     */
    suspend fun fetchLoadsForCity(
        cityId: Int,
        page: Int,
        forceRefresh: Boolean = false,
    ): Result<PaginatedLoadBoardResponse>

    suspend fun expressInterest(indentId: String): Result<IndentInterestResponse>

    suspend fun withdrawInterest(indentId: String): Result<IndentInterestResponse>

    /**
     * Fetch the trucker-safe indent detail (no client cost details).
     */
    suspend fun fetchTruckerIndentDetail(indentId: String): Result<TruckerIndentDetailResponse>
}
