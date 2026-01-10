package com.freight24.trucker.feature.loads.data.repository

import com.freight24.rest.response.LoadBoardResponse
import com.freight24.rest.response.PaginatedLoadBoardResponse

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
}
