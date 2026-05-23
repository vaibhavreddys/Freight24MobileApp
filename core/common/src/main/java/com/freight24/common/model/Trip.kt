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
    val financials: TripFinancials? = null,
)

data class TripFinancials(
    val clientTripCost: Double = 0.0,
    val truckerTripCost: Double = 0.0,
    val balanceAmount: Double = 0.0,
    val platformFeeAmount: Double = 0.0,
    val tdsAmount: Double = 0.0,
    val tdsApplicable: Boolean = false,
    val otherDeductions: Double = 0.0,
    val deductionRemarks: String? = null,
    val truckerNetAmount: Double = 0.0,
    val haltingAmount: Double = 0.0,
    val luAmountTotal: Double = 0.0,
    val luAmountReimbursable: Double = 0.0,
    val luIncluded: Boolean? = null,
    val advancePaid: Boolean = false,
    val advancePaidAmount: Double = 0.0,
    val balancePaid: Boolean = false,
    val balancePaidAmount: Double = 0.0,
    val totalDeductions: Double = 0.0,
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
    val details: String? = null,
    val oldStatus: String? = null,
    val newStatus: String? = null,
)

data class TripDocument(
    val id: String?,
    val documentType: String,
    val originalFilename: String? = null,
    val verificationStatus: String,
    val verificationRemarks: String? = null,
    val uploadedAt: String? = null,
    val podType: String? = null,
    val podStatus: String? = null,
)
