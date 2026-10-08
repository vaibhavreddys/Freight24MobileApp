package com.freight24.rest.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TruckV2PageResponse(
    val success: Boolean,
    val page: Int,
    val limit: Int,
    val counts: TruckV2Counts = TruckV2Counts(),
    val data: List<TruckV2SummaryDto> = emptyList(),
)

@Serializable
data class TruckV2Counts(
    val total: Int = 0,
    val active: Int = 0,
    val inactive: Int = 0,
    val blacklisted: Int = 0,
    @SerialName("pending_docs") val pendingDocs: Int = 0,
)

@Serializable
data class TruckV2SummaryDto(
    val truckId: String,
    val registrationNumber: String,
    val status: String,
    val isVerified: Boolean = false,
    val isBlacklisted: Boolean = false,
    val availability: String? = null,
    val owner: TruckV2OwnerDto? = null,
    val vehicle: TruckV2VehicleDto? = null,
    val truckType: String? = null,
    val bodyType: String? = null,
    val displayName: String? = null,
    val fitnessCertificate: TruckV2ExpiryDto? = null,
    val insurance: TruckV2ExpiryDto? = null,
)

@Serializable
data class TruckV2OwnerDto(
    val name: String? = null,
    val phone: String? = null,
    val tagCode: String? = null,
    val status: String? = null,
)

@Serializable
data class TruckV2VehicleDto(
    val make: String? = null,
    val model: String? = null,
    val year: Int? = null,
    val capacityMt: Double? = null,
)

@Serializable
data class TruckV2ExpiryDto(
    val expiryDate: String? = null,
    val expiryStatus: String? = null,
)

@Serializable
data class TruckV2DetailResponse(
    val success: Boolean,
    val message: String? = null,
    val data: TruckV2DetailDto? = null,
)

@Serializable
data class TruckV2DetailDto(
    val truckId: String,
    val truckTypeId: Int? = null,
    val registrationNumber: String,
    val status: String,
    val isVerified: Boolean = false,
    val isBlacklisted: Boolean = false,
    val activationEligibility: V2ActivationEligibilityDto? = null,
    val availability: String? = null,
    val owner: TruckV2OwnerDto? = null,
    val vehicle: TruckV2VehicleDto? = null,
    val truckType: String? = null,
    val bodyType: String? = null,
    val displayName: String? = null,
    val documents: TruckV2DocumentsDto? = null,
    val detached: TruckV2DetachedDto? = null,
    val blacklist: TruckV2BlacklistDto? = null,
)

@Serializable
data class TruckV2DocumentsDto(
    val rcCard: TruckV2DocumentDto? = null,
    val fcCertificate: TruckV2DocumentDto? = null,
    val insurance: TruckV2DocumentDto? = null,
)

@Serializable
data class TruckV2DocumentDto(
    val url: String? = null,
    val status: String? = null,
    val expiryDate: String? = null,
    val expiryStatus: String? = null,
)

@Serializable
data class TruckV2DetachedDto(
    val isDetached: Boolean,
    val reason: String? = null,
    val detachedAt: String? = null,
    val detachedBy: V2ActorDto? = null,
)

@Serializable
data class TruckV2BlacklistDto(
    val reason: String? = null,
    val blacklistedAt: String? = null,
    val blacklistedBy: V2ActorDto? = null,
)

@Serializable
data class V2ActorDto(
    val id: String? = null,
    val tagCode: String? = null,
    val name: String? = null,
    val phone: String? = null,
)

@Serializable
data class V2ActivationEligibilityDto(
    val eligible: Boolean,
    val missingRequirements: List<V2MissingRequirementDto> = emptyList(),
)

@Serializable
data class V2MissingRequirementDto(
    val field: String? = null,
    val code: String? = null,
    val label: String? = null,
    val message: String? = null,
)

@Serializable
data class TruckV2ValidationResponse(
    val success: Boolean,
    val data: TruckV2ValidationData,
)

@Serializable
data class TruckV2ValidationData(
    val exists: Boolean,
    val truck: TruckV2ValidationTruckDto? = null,
)

@Serializable
data class TruckV2ValidationTruckDto(
    val id: String,
    val registrationNumber: String,
    val owner: TruckV2ValidationOwnerDto? = null,
    val make: String? = null,
    val model: String? = null,
    val year: Int? = null,
    val capacityMt: Double? = null,
    val status: String? = null,
)

@Serializable
data class TruckV2ValidationOwnerDto(
    val id: String,
    val profileId: String,
    val fullName: String? = null,
    val tagCode: String? = null,
)

@Serializable
data class V2ActivationFailureResponse(
    val success: Boolean,
    val message: String,
    val data: V2ActivationFailureData? = null,
)

@Serializable
data class V2ActivationFailureData(
    val status: String? = null,
    val activationEligibility: V2ActivationEligibilityDto? = null,
)
