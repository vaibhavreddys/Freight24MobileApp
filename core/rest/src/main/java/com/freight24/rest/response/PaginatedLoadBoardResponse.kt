package com.freight24.rest.response

import com.freight24.rest.response.LoadBoardResponse.CityLoadSummary
import com.freight24.rest.response.LoadBoardResponse.LatestLoad
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PaginatedLoadBoardResponse(
    val success: Boolean,
    val data: LoadData?,
) {
    @Serializable
    data class LoadData(
        val loads: List<LatestLoad>,
        val pagination: Pagination,
    )

    @Serializable
    data class Pagination(
        val page: Int,
        val limit: Int,
        val hasMore: Boolean,
        val totalLoads: Int,
    )
}
