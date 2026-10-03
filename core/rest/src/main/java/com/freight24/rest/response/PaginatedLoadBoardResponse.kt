package com.freight24.rest.response

import kotlinx.serialization.Serializable

/**
 * `GET /api/v1/indents/trucker/load-board-paginated` — the expanded-city feed
 * behind the load board's "Show More". Returns the same [LoadBoardLoad] rows as
 * `load-board-summary`, but paged.
 */
@Serializable
data class PaginatedLoadBoardResponse(
    val success: Boolean,
    val data: LoadData? = null,
) {
    @Serializable
    data class LoadData(
        val loads: List<LoadBoardLoad> = emptyList(),
        val pagination: Pagination = Pagination(),
    )

    @Serializable
    data class Pagination(
        val page: Int = 0,
        val limit: Int = 0,
        val hasMore: Boolean = false,
        val totalLoads: Int = 0,
    )
}
