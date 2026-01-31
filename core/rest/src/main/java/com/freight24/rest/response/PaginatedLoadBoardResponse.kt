package com.freight24.rest.response

import kotlinx.serialization.SerialName
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
        @SerialName("total_loads")
        val total_loads: Int,
        @SerialName("has_more")
        val has_more: Boolean,
    )
}
