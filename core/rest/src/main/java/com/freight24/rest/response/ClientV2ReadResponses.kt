package com.freight24.rest.response

import kotlinx.serialization.Serializable

@Serializable
data class ClientV2PageResponse(
    val success: Boolean,
    val page: Int,
    val limit: Int,
    val counts: ClientV2Counts = ClientV2Counts(),
    val data: List<ClientV2SummaryDto> = emptyList(),
)

@Serializable
data class ClientV2Counts(
    val total: Int = 0,
    val active: Int = 0,
    val inactive: Int = 0,
)

@Serializable
data class ClientV2SummaryDto(
    val id: String,
    val userId: String,
    val clientType: String? = null,
    val companyName: String,
    val displayName: String? = null,
    val companyEmail: String? = null,
    val companyPhone: String? = null,
    val companyAltPhone: String? = null,
    val panNumber: String? = null,
    val gstNumber: String? = null,
    val companyType: String? = null,
    val website: String? = null,
    val registeredAddress: ClientV2AddressDto? = null,
    val billingSameAsRegistered: Boolean = false,
    val billingAddress: ClientV2AddressDto? = null,
    val podSameAsRegistered: Boolean = false,
    val podAddress: ClientV2AddressDto? = null,
    val primaryContact: ClientV2PrimaryContactDto? = null,
    val branchCount: Int = 0,
    val activeBranchCount: Int = 0,
    val inactiveBranchCount: Int = 0,
    val contactCount: Int = 0,
    val activeContactCount: Int = 0,
    val inactiveContactCount: Int = 0,
    val status: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
)

@Serializable
data class ClientV2AddressDto(
    val addressLine1: String? = null,
    val addressLine2: String? = null,
    val line1: String? = null,
    val line2: String? = null,
    val area: V2NamedIdDto? = null,
    val city: V2NamedIdDto? = null,
    val state: V2NamedIdDto? = null,
    val pinCode: String? = null,
    val sameAsRegistered: Boolean? = null,
)

@Serializable
data class ClientV2PrimaryContactDto(
    val id: String,
    val fullName: String? = null,
    val designation: String? = null,
    val phone: String? = null,
    val alternatePhone: String? = null,
    val email: String? = null,
)

@Serializable
data class ClientV2DetailResponse(
    val success: Boolean,
    val message: String? = null,
    val data: ClientV2DetailDto? = null,
)

@Serializable
data class ClientV2DetailDto(
    val id: String,
    val clientId: String,
    val tagCode: String? = null,
    val clientType: String? = null,
    val company: ClientV2CompanyDto,
    val registeredAddress: ClientV2AddressDto? = null,
    val billingAddress: ClientV2AddressDto? = null,
    val podAddress: ClientV2AddressDto? = null,
    val contacts: List<ClientV2DetailContactDto> = emptyList(),
    val contactCount: Int = 0,
    val branches: List<ClientV2DetailBranchDto> = emptyList(),
    val branchCount: Int = 0,
    val documents: List<ClientV2DocumentDto> = emptyList(),
    val documentSummary: ClientV2DocumentSummaryDto? = null,
    val documentsVerified: Boolean = false,
    val profileVerificationStatus: String? = null,
    val status: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
)

@Serializable
data class ClientV2CompanyDto(
    val legalName: String,
    val displayName: String? = null,
    val pan: String? = null,
    val gstNumber: String? = null,
    val companyType: String? = null,
    val website: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val alternatePhone: String? = null,
)

@Serializable
data class ClientV2DetailContactDto(
    val id: String,
    val fullName: String,
    val designation: String? = null,
    val phone: String? = null,
    val alternatePhone: String? = null,
    val email: String? = null,
    val isPrimary: Boolean = false,
    val isActive: Boolean = true,
    val clientRoles: List<ClientV2RoleDto> = emptyList(),
    val branchAssignments: List<ClientV2BranchAssignmentDto> = emptyList(),
)

@Serializable
data class ClientV2RoleDto(
    val id: String? = null,
    val code: String? = null,
    val name: String? = null,
    val description: String? = null,
    val applicableTo: String? = null,
)

@Serializable
data class ClientV2BranchAssignmentDto(
    val branchId: String,
    val role: ClientV2RoleDto? = null,
)

@Serializable
data class ClientV2DetailBranchDto(
    val id: String,
    val branchName: String,
    val branchCode: String? = null,
    val branchType: ClientBranchTypeV2Dto? = null,
    val status: String? = null,
    val contacts: List<ClientV2DetailContactDto> = emptyList(),
    val documents: List<ClientV2DocumentDto> = emptyList(),
)

@Serializable
data class ClientV2DocumentDto(
    val id: String? = null,
    val type: String,
    val url: String? = null,
    val status: String,
    val verifiedBy: String? = null,
    val verifiedAt: String? = null,
    val rejectionReason: String? = null,
)

@Serializable
data class ClientV2DocumentSummaryDto(
    val total: Int = 0,
    val uploaded: Int = 0,
    val pending: Int = 0,
    val verified: Int = 0,
    val rejected: Int = 0,
)

@Serializable
data class ClientV2ValidationResponse(
    val success: Boolean,
    val data: ClientV2ValidationData = ClientV2ValidationData(),
)

@Serializable
data class ClientV2ValidationData(
    val company_phone: ClientV2FieldValidationDto? = null,
    val company_alt_phone: ClientV2FieldValidationDto? = null,
    val company_email: ClientV2FieldValidationDto? = null,
    val pan_card: ClientV2FieldValidationDto? = null,
    val gst_number: ClientV2FieldValidationDto? = null,
)

@Serializable
data class ClientV2FieldValidationDto(
    val exists: Boolean,
    val foundIn: String? = null,
    val client: ClientV2ValidationClientDto? = null,
    val branch: ClientV2ValidationBranchDto? = null,
)

@Serializable
data class ClientV2ValidationClientDto(
    val id: String,
    val companyName: String? = null,
    val displayName: String? = null,
    val companyPhone: String? = null,
    val companyAltPhone: String? = null,
    val companyEmail: String? = null,
    val panCard: String? = null,
    val gstNumber: String? = null,
    val status: String? = null,
)

@Serializable
data class ClientV2ValidationBranchDto(
    val id: String,
    val clientId: String,
    val branchName: String? = null,
    val branchCode: String? = null,
    val gstNumber: String? = null,
    val status: String? = null,
)
