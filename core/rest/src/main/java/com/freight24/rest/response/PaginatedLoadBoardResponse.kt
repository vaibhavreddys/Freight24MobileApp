package com.freight24.rest.response

import kotlinx.serialization.Serializable

@Serializable
data class PaginatedLoadBoardResponse(
    val success: Boolean,
    val data: LoadData?,
) {
    @Serializable
    data class LoadData(
        val loads: List<LoadBoardResponse.LatestLoad>,
        val pagination: Pagination,
    )

    @Serializable
    data class Pagination(
        val page: Int,
        val limit: Int,
        val total_loads: Int,
        val has_more: Boolean,
    )
}
