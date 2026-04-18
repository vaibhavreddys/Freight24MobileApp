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
    val trucker_trip_cost: Double,
    val advance_amount: Double,
    val trucker_status: String? = null,
    val origin: TripLocationDto,
    val destination: TripLocationDto,
    val status_events: List<StatusEventDto> = emptyList(),
    val trip_documents: List<TripDocumentDto> = emptyList(),
)

@Serializable
data class StatusEventDto(
    val event_type: String,
    val event_by: String,
    val event_at: String,
    val details: String,
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
