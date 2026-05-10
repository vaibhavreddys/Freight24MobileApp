package com.freight24.rest.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class VerificationTruckOwnersResponse(
    val data: List<VerificationTruckOwnerDto> = emptyList(),
) : BaseResponse()

@Serializable
data class VerificationTruckOwnerDto(
    val id: String,
    @SerialName("reference_id") val referenceId: String,
    @SerialName("status") val status: String,
    @SerialName("changed_fields") val changedFields: VerificationBusinessProfileChangedFieldsDto? = null,
    @SerialName("old_snapshot") val oldSnapshot: VerificationBusinessProfileSnapshotDto? = null,
    @SerialName("new_snapshot") val newSnapshot: VerificationBusinessProfileSnapshotDto? = null,
    @SerialName("resolution_notes") val resolutionNotes: String? = null,
    @SerialName("updated_at") val updatedAt: String,
    @SerialName("entity") val entity: String? = null,
    @SerialName("source_table") val sourceTable: String? = null,
    @SerialName("full_name") val fullName: String? = null,
    @SerialName("phone") val phone: String? = null,
)

@Serializable
data class VerificationClientsResponse(
    val data: List<VerificationClientDto> = emptyList(),
) : BaseResponse()

@Serializable
data class VerificationClientDto(
    val id: String,
    @SerialName("reference_id") val referenceId: String,
    @SerialName("full_name") val fullName: String? = null,
    @SerialName("company_name") val companyName: String? = null,
    @SerialName("status") val status: String,
    @SerialName("changed_fields") val changedFields: VerificationBusinessProfileChangedFieldsDto? = null,
    @SerialName("old_snapshot") val oldSnapshot: VerificationBusinessProfileSnapshotDto? = null,
    @SerialName("new_snapshot") val newSnapshot: VerificationBusinessProfileSnapshotDto? = null,
    @SerialName("resolution_notes") val resolutionNotes: String? = null,
    @SerialName("updated_at") val updatedAt: String,
)

@Serializable
data class VerificationBusinessProfileChangedFieldsDto(
    @SerialName("business_profile") val businessProfile: List<String> = emptyList(),
)

@Serializable
data class VerificationBusinessProfileSnapshotDto(
    @SerialName("business_profile") val businessProfile: Map<String, JsonElement?> = emptyMap(),
)
