package com.freight24.rest.response

import com.google.gson.annotations.SerializedName
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
        @SerializedName("total_loads")
        val total_loads: Int,
        @SerializedName("has_more")
        val has_more: Boolean,
    )
}
