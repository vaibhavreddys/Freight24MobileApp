package com.freight24.common.model

data class PaginatedLoads(
    val loads: List<Load>,
    val page: Int,
    val hasMore: Boolean,
    val totalLoads: Int,
)
