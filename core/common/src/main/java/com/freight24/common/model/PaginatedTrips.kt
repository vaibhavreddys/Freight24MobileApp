package com.freight24.common.model

data class PaginatedTrips(
    val trips: List<Trip>,
    val page: Int,
    val hasMore: Boolean,
    val totalTrips: Int,
)
