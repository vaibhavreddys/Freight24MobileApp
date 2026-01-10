package com.freight24.common.repository

import com.freight24.common.model.PaginatedTrips
import kotlinx.coroutines.flow.Flow

interface TripRepository {
    /**
     * Fetch paginated trips for the trucker.
     * @param page Page number (1-indexed)
     * @param forceRefresh If true, bypasses cache and fetches from network
     */
    suspend fun fetchTrips(
        page: Int,
        forceRefresh: Boolean = false,
    ): Flow<PaginatedTrips>
}
