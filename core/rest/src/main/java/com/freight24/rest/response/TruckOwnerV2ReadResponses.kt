package com.freight24.rest.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TruckOwnerV2PageResponse(
    val success: Boolean,
    val page: Int,
    val limit: Int,
    val count: Int? = null,
    val counts: TruckOwnerV2Counts? = null,
    val data: List<TruckOwnerV2SummaryDto> = emptyList(),
)

@Serializable
data class TruckOwnerV2Counts(
    val total: Int = 0,
    val active: Int = 0,
    val inactive: Int = 0,
    @SerialName("pending_docs") val pendingDocs: Int = 0,
)

@Serializable
data class TruckOwnerV2SummaryDto(
    val id: String,
    val userId: String? = null,
    val profileId: String? = null,
    val tagCode: String? = null,
    val fullName: String,
    val email: String? = null,
    val phone: String? = null,
    val businessName: String? = null,
    val gstNumber: String? = null,
    val panNumber: String? = null,
    val status: String? = null,
    val baseCity: V2NamedIdDto? = null,
    val baseArea: V2NamedIdDto? = null,
    val preferredRoutesCount: Int = 0,
    val trucksCount: Int = 0,
    val createdAt: String? = null,
    val updatedAt: String? = null,
)

@Serializable
data class V2NamedIdDto(
    val id: Int? = null,
    val name: String? = null,
)

@Serializable
data class TruckOwnerV2DetailResponse(
    val success: Boolean,
    val message: String? = null,
    val data: TruckOwnerV2DetailDto? = null,
)

@Serializable
data class TruckOwnerV2DetailDto(
    val id: String,
    val profileId: String,
    val tagCode: String? = null,
    val fullName: String,
    val email: String? = null,
    val phone: String? = null,
    val altPhones: List<String> = emptyList(),
    val businessName: String? = null,
    val gstNumber: String? = null,
    val status: String,
    val baseLocation: String? = null,
    val baseCity: V2NamedIdDto? = null,
    val baseArea: V2NamedIdDto? = null,
    val baseCityState: String? = null,
    val panCard: String? = null,
    val aadharCard: String? = null,
    val isTdsApplicable: Boolean? = null,
    val trucksCount: Int = 0,
    val truckStats: TruckOwnerV2TruckStatsDto? = null,
    val documents: TruckOwnerV2DocumentsDto? = null,
    val bank: TruckOwnerV2BankDto? = null,
    val bankVerified: Boolean = false,
    val documentsVerified: Boolean = false,
    val activationEligibility: V2ActivationEligibilityDto? = null,
    val preferredRoutes: List<TruckOwnerV2RouteDto> = emptyList(),
    val profileVerificationStatus: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
)

@Serializable
data class TruckOwnerV2TruckStatsDto(
    val total: Int = 0,
    val active: Int = 0,
    val inactive: Int = 0,
    val blacklisted: Int = 0,
)

@Serializable
data class TruckOwnerV2DocumentsDto(
    val mandatory: List<TruckOwnerV2DocumentDto> = emptyList(),
    val optional: List<TruckOwnerV2DocumentDto> = emptyList(),
)

@Serializable
data class TruckOwnerV2DocumentDto(
    val id: String? = null,
    val type: String,
    val status: String,
    val url: String? = null,
)

@Serializable
data class TruckOwnerV2BankDto(
    val id: String,
    val accountHolderName: String? = null,
    val accountNumber: String? = null,
    val ifscCode: String? = null,
    val upiId: String? = null,
    val bankName: String? = null,
    val branchName: String? = null,
    val chequeImageUrl: String? = null,
    val verificationStatus: String? = null,
)

@Serializable
data class TruckOwnerV2RouteDto(
    val id: String,
    val priority: Int? = null,
    val source: String? = null,
    val sourceCity: V2NamedIdDto? = null,
    val destinationCity: V2NamedIdDto? = null,
)

@Serializable
data class TruckOwnerV2ValidationResponse(
    val success: Boolean,
    val data: TruckOwnerV2ValidationData = TruckOwnerV2ValidationData(),
)

@Serializable
data class TruckOwnerV2ValidationData(
    val phone: TruckOwnerV2FieldValidationDto? = null,
    val pan: TruckOwnerV2FieldValidationDto? = null,
    val aadhar: TruckOwnerV2FieldValidationDto? = null,
    val gst: TruckOwnerV2FieldValidationDto? = null,
)

@Serializable
data class TruckOwnerV2FieldValidationDto(
    val exists: Boolean,
    val trucker: TruckOwnerV2ValidationOwnerDto? = null,
)

@Serializable
data class TruckOwnerV2ValidationOwnerDto(
    val id: String,
    val profileId: String,
    val fullName: String? = null,
    val status: String? = null,
    val tagCode: String? = null,
)
