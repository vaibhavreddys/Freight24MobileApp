package com.freight24.core.common.repository

import com.freight24.core.common.model.CityLoadSummary
import com.freight24.core.common.model.PaginatedLoads
import kotlinx.coroutines.flow.Flow

interface LoadBoardRepository {
    /**
     * Fetch load board summary (cities with load counts).
     * @param forceRefresh If true, bypasses cache and fetches from network
     */
    suspend fun fetchLoadBoardSummary(forceRefresh: Boolean = false): Flow<List<CityLoadSummary>>

    /**
     * Fetch paginated loads for a specific city.
     * @param cityId The city to fetch loads for
     * @param page Page number (1-indexed)
     * @param forceRefresh If true, bypasses cache and fetches from network
     */
    suspend fun fetchLoadsForCity(
        cityId: Int,
        page: Int,
        forceRefresh: Boolean = false
    ): Flow<PaginatedLoads>

}
