package com.freight24.core.common.model

// core/common/model/PaginatedLoads.kt
data class PaginatedLoads(
    val loads: List<Load>,
    val page: Int,
    val hasMore: Boolean,
    val totalLoads: Int
)
