package com.freight24.core.common.model

/**
 * Domain model representing a Trip in the system.
 */
data class Trip(
    val id: String,
    val registrationNumber: String,
    val truckType: String,
    val status: String,
    val createdAt: String,
    val tripCost: Double,
    val advanceAmount: Double,
    val origin: String,
    val destination: String
)

/**
 * Data class for paginated trip responses.
 */
data class PaginatedTrips(
    val trips: List<Trip>,
    val page: Int,
    val hasMore: Boolean,
    val totalTrips: Int
)
