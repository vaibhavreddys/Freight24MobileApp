package com.freight24.rest.response

import kotlinx.serialization.Serializable

@Serializable
data class TripDetailResponse(
    val success: Boolean,
    val data: TripDetailDto?,
    val message: String? = null,
)

@Serializable
data class TripDetailDto(
    val id: String,
    val trip_code: String,
    val registration_number: String,
    val truck_type: String,
    val created_at: String,
    val trucker_trip_cost: Double = 0.0,
    val advance_amount: Double = 0.0,
    val trucker_status: String? = null,
    val origin: TripLocationDto,
    val destination: TripLocationDto,
    val status_events: List<StatusEventDto> = emptyList(),
    val trip_documents: List<TripDocumentDto> = emptyList(),
    val financials: TripFinancialsDto? = null,
)

@Serializable
data class StatusEventDto(
    val event_type: String,
    val event_by: String,
    val event_at: String,
    val details: String? = null,
    val old_status: String? = null,
    val new_status: String? = null,
)

@Serializable
data class TripFinancialsDto(
    val client_trip_cost: Double = 0.0,
    val trucker_trip_cost: Double = 0.0,
    val balance_amount: Double = 0.0,
    val platform_fee_amount: Double = 0.0,
    val tds_amount: Double = 0.0,
    val tds_applicable: Boolean = false,
    val other_deductions: Double = 0.0,
    val deduction_remarks: String? = null,
    val trucker_net_amount: Double = 0.0,
    val halting_amount: Double = 0.0,
    val lu_amount_total: Double = 0.0,
    val lu_amount_reimbursable: Double = 0.0,
    val lu_included: Boolean? = null,
    val advance_paid: Boolean = false,
    val advance_paid_amount: Double = 0.0,
    val balance_paid: Boolean = false,
    val balance_paid_amount: Double = 0.0,
    val total_deductions: Double = 0.0,
)

@Serializable
data class TripDocumentDto(
    val document_type: String,
    val original_filename: String? = null,
    val verification_status: String,
    val verification_remarks: String? = null,
    val uploaded_at: String? = null,
    val pod_type: String? = null,
    val pod_status: String? = null,
    val id: String
)
