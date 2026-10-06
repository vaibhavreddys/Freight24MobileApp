package com.freight24.rest.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ClientOnboardingV2Response(
    val success: Boolean,
    val message: String? = null,
    val nextStep: String? = null,
    val data: ClientOnboardingV2Data? = null,
)

@Serializable
data class ClientOnboardingV2Data(
    val clientId: String,
    val userId: String? = null,
    val clientType: String? = null,
    val companyName: String? = null,
    val status: String? = null,
    val operatingStructure: String? = null,
    val branchCount: Int? = null,
    val skipped: Boolean? = null,
    val branches: List<ClientOnboardingBranchDto> = emptyList(),
)

@Serializable
data class ClientOnboardingBranchDto(
    val id: String,
    val branchName: String,
    val branchCode: String? = null,
    val branchTypeId: String? = null,
    val status: String? = null,
    val gstin: String? = null,
)

@Serializable
data class ClientContactCreateRequest(
    @SerialName("full_name") val fullName: String,
    val designation: String? = null,
    val phone: String,
    @SerialName("alternate_phone") val alternatePhone: String? = null,
    val email: String? = null,
    @SerialName("is_primary") val isPrimary: Boolean = false,
    @SerialName("access_assignments") val accessAssignments: List<ClientContactAssignmentRequest>,
)

@Serializable
data class ClientContactAssignmentRequest(
    @SerialName("scope_type") val scopeType: String,
    @SerialName("branch_id") val branchId: String? = null,
    @SerialName("role_ids") val roleIds: List<String>,
)

@Serializable
data class ClientContactCreateResponse(
    val success: Boolean,
    val message: String? = null,
)

@Serializable
data class MasterDataListResponse<T>(
    val success: Boolean,
    val data: List<T> = emptyList(),
)

@Serializable
data class StateOptionDto(val id: Int, val name: String, val code: String? = null)

@Serializable
data class CityOptionDto(val id: Int, val name: String, val state: StateOptionDto)

@Serializable
data class BranchTypeOptionDto(
    val id: String,
    val name: String,
    val code: String,
    @SerialName("is_active") val isActive: Boolean = true,
)

@Serializable
data class ContactRoleOptionDto(
    val id: String,
    val name: String,
    val code: String,
    @SerialName("applicable_to") val applicableTo: String,
    @SerialName("is_active") val isActive: Boolean = true,
)
