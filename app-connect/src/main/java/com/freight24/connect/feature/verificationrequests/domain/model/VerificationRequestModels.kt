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
    VERIFIED("VERIFIED", "Verified"),
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
    val entity: String,
    val requestId: String,
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

data class TruckDetailChange(
    val fieldName: String,
    val displayFieldName: String,
    val beforeValue: String?,
    val afterValue: String?,
)

data class TruckVerificationRequest(
    val id: String,
    val referenceId: String,
    val registrationNumber: String,
    val changedFields: List<String>,
    val changedDocuments: List<VerificationRequestDocument>,
    val changedTruckDetails: List<TruckDetailChange>,
    val status: VerificationRequestStatus,
    val resolutionNotes: String? = null,
    val updatedAt: String,
)

data class BusinessProfileChange(
    val fieldName: String,
    val displayFieldName: String,
    val beforeValue: String?,
    val afterValue: String?,
)

data class TruckOwnerVerificationRequest(
    val id: String,
    val referenceId: String,
    val fullName: String,
    val phone: String?,
    val changedFields: List<String>,
    val changedBusinessProfile: List<BusinessProfileChange>,
    val status: VerificationRequestStatus,
    val resolutionNotes: String? = null,
    val updatedAt: String,
)

data class ClientVerificationRequest(
    val id: String,
    val referenceId: String,
    val fullName: String,
    val companyName: String,
    val changedFields: List<String>,
    val changedBusinessProfile: List<BusinessProfileChange>,
    val status: VerificationRequestStatus,
    val resolutionNotes: String? = null,
    val updatedAt: String,
)
