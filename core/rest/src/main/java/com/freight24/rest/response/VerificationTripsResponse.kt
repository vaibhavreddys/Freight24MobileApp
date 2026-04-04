package com.freight24.rest.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VerificationTripsResponse(
    val data: List<VerificationTripDto> = emptyList(),
) : BaseResponse()

@Serializable
data class VerificationTripDto(
    val id: String,
    @SerialName("trip_id") val tripId: String,
    @SerialName("trip_code") val tripCode: String,
    @SerialName("truck_registration_number") val truckRegistrationNumber: String,
    @SerialName("origin") val origin: VerificationLocationDto? = null,
    @SerialName("destination") val destination: VerificationLocationDto? = null,
    @SerialName("changed_fields") val changedFields: VerificationChangedFieldsDto? = null,
    @SerialName("old_snapshot") val oldSnapshot: VerificationSnapshotDto? = null,
    @SerialName("new_snapshot") val newSnapshot: VerificationSnapshotDto? = null,
    @SerialName("status") val status: String,
    @SerialName("resolution_notes") val resolutionNotes: String? = null,
    @SerialName("updated_at") val updatedAt: String,
)

@Serializable
data class VerificationLocationDto(
    @SerialName("address") val address: String? = null,
    @SerialName("city") val city: String? = null,
    @SerialName("area") val area: String? = null,
)

@Serializable
data class VerificationChangedFieldsDto(
    @SerialName("documents") val documents: List<String> = emptyList(),
)

@Serializable
data class VerificationSnapshotDto(
    @SerialName("documents") val documents: Map<String, VerificationDocumentDto> = emptyMap(),
)

@Serializable
data class VerificationDocumentDto(
    @SerialName("_ref") val ref: String? = null,
    @SerialName("file_url") val fileUrl: String? = null,
)
