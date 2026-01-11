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
 * Data class representing a detailed Trip view.
 */
data class TripDetail(
    val id: String,
    val tripCode: String,
    val registrationNumber: String,
    val truckType: String,
    val createdAt: String,
    val tripCost: Double,
    val advanceAmount: Double,
    val origin: TripLocation,
    val destination: TripLocation,
    val statusEvents: List<StatusEvent>,
    val tripDocuments: List<TripDocument>
)

data class TripDocument(
    val id: String?,
    val documentType: String,
    val originalFilename: String?,
    val verificationStatus: String,
    val uploadedAt: String?,
    val podType: String? = null,
    val podStatus: String? = null
)

data class TripLocation(
    val city: String?,
    val area: String?,
    val address: String?
)

data class StatusEvent(
    val eventType: String,
    val eventBy: String,
    val eventAt: String,
    val details: String
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
