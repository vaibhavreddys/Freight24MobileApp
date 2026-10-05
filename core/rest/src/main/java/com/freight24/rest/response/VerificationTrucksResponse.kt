package com.freight24.rest.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive

@Serializable
data class VerificationTrucksResponse(
    val data: List<VerificationTruckDto> = emptyList(),
) : BaseResponse()

@Serializable
data class VerificationTruckDto(
    val id: String,
    @SerialName("reference_id") val referenceId: String,
    @SerialName("registration_number") val registrationNumber: String,
    @SerialName("changed_fields") val changedFields: VerificationTruckChangedFieldsDto? = null,
    @SerialName("old_snapshot") val oldSnapshot: VerificationTruckSnapshotDto? = null,
    @SerialName("new_snapshot") val newSnapshot: VerificationTruckSnapshotDto? = null,
    @SerialName("status") val status: String,
    @SerialName("resolution_notes") val resolutionNotes: String? = null,
    @SerialName("updated_at") val updatedAt: String,
    @SerialName("entity") val entity: String? = null,
    @SerialName("source_table") val sourceTable: String? = null,
)

@Serializable
data class VerificationTruckChangedFieldsDto(
    @SerialName("documents") val documents: List<String> = emptyList(),
    @SerialName("truck_details") val truckDetails: List<String> = emptyList(),
)

@Serializable
data class VerificationTruckSnapshotDto(
    @SerialName("documents") val documents: Map<String, VerificationDocumentDto> = emptyMap(),
    @SerialName("truck_details") val truckDetails: Map<String, JsonElement?> = emptyMap(),
)

fun JsonElement?.asString(): String? {
    return when (this) {
        null -> null
        is JsonPrimitive -> if (isString) content else content
        else -> toString()
    }
}
