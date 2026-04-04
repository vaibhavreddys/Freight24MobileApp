package com.freight24.connect.feature.verificationrequests.domain.model

import java.util.Locale

enum class VerificationRequestTab(
    val title: String,
) {
    TRIPS("Trips"),
    TRUCKS("Trucks"),
    TRUCK_OWNERS("Truck Owners"),
    CLIENTS("Clients"),
}

enum class VerificationRequestStatus(
    val apiValue: String,
    val displayText: String,
) {
    PENDING("PENDING", "Pending"),
    IN_REVIEW("IN_REVIEW", "In Review"),
    APPROVED("APPROVED", "Approved"),
    REJECTED("REJECTED", "Rejected"),
    COMPLETED("COMPLETED", "Completed");

    companion object {
        fun fromApiValue(value: String?): VerificationRequestStatus? {
            return entries.firstOrNull { it.apiValue.equals(value, ignoreCase = true) }
        }

        fun filterValues(tabType: String): List<VerificationRequestStatus> {
            return when (tabType) {
                "TRIPS" -> listOf(
                    PENDING, COMPLETED
                )
                else -> listOf(
                    PENDING, IN_REVIEW, COMPLETED
                )
            }
        }
    }
}

data class VerificationRequestLocation(
    val address: String? = null,
    val city: String? = null,
    val area: String? = null,
) {
    fun displayText(): String {
        return listOfNotNull(address, city, area)
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .joinToString(" • ")
            .ifBlank { "N/A" }
    }
}

data class VerificationRequestDocument(
    val name: String,
    val s3Url: String? = null,
) {
    val displayName: String
        get() = name.uppercase(Locale.getDefault())
}

data class VerificationRequest(
    val id: String,
    val tripId: String,
    val tripCode: String,
    val truckRegistrationNumber: String,
    val origin: VerificationRequestLocation?,
    val destination: VerificationRequestLocation?,
    val documents: List<VerificationRequestDocument>,
    val status: VerificationRequestStatus,
    val resolutionNotes: String? = null,
    val updatedAt: String,
)
