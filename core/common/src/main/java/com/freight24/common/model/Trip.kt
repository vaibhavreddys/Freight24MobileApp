package com.freight24.common.model

data class Trip(
    val id: String,
    val registrationNumber: String,
    val truckType: String,
    val status: String,
    val createdAt: String,
    val tripCost: Double,
    val advanceAmount: Double,
    val origin: String,
    val destination: String,
)

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
    val status: String,
    val statusEvents: List<StatusEvent>,
    val tripDocuments: List<TripDocument>,
)

data class TripLocation(
    val city: String?,
    val area: String?,
    val address: String?,
) {
    fun toDisplayString(): String {
        return listOfNotNull(area, city).joinToString(", ").ifEmpty { address ?: "Unknown" }
    }
}

data class StatusEvent(
    val eventType: String,
    val eventBy: String,
    val eventAt: String,
    val details: String,
)

data class TripDocument(
    val id: String? = null,
    val documentType: String,
    val originalFilename: String? = null,
    val verificationStatus: String,
    val uploadedAt: String? = null,
    val podType: String? = null,
    val podStatus: String? = null,
)
