package com.freight24.trucker.feature.trips.data.model

data class PaginatedTrips(
    val trips: List<Trip>,
    val page: Int,
    val hasMore: Boolean,
    val totalTrips: Int,
)
