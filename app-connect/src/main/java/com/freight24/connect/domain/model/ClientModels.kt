package com.freight24.connect.domain.model

data class ClientManagementItem(
    val clientId: String,
    val clientUserId: String,
    val clientType: String?,
    val companyName: String,
    val displayName: String?,
    val companyEmail: String?,
    val companyPhone: String?,
    val companyAlternatePhone: String?,
    val panNumber: String?,
    val gstNumber: String?,
    val companyType: String?,
    val website: String?,
    val registeredAddress: ClientAddress?,
    val primaryContact: ClientPrimaryContact?,
    val branchCount: Int,
    val activeBranchCount: Int,
    val inactiveBranchCount: Int,
    val contactCount: Int,
    val activeContactCount: Int,
    val inactiveContactCount: Int,
    val status: String?,
    val createdAt: String?,
    val updatedAt: String?,
)

data class ClientPickerOption(
    val clientId: String,
    val clientUserId: String,
    val companyName: String,
    val displayName: String?,
    val companyPhone: String?,
    val status: String?,
    val primaryContactName: String?,
    val primaryContactPhone: String?,
)

data class ClientDetails(
    val clientId: String,
    val clientUserId: String,
    val tagCode: String?,
    val clientType: String?,
    val company: ClientCompany,
    val registeredAddress: ClientAddress?,
    val billingAddress: ClientAddress?,
    val podAddress: ClientAddress?,
    val contacts: List<ClientContact>,
    val branches: List<ClientBranch>,
    val documents: List<VerificationDocument>,
    val documentSummary: ClientDocumentSummary?,
    val documentsVerified: Boolean,
    val profileVerificationStatus: String?,
    val status: String?,
    val createdAt: String?,
    val updatedAt: String?,
)

data class ClientCompany(
    val legalName: String,
    val displayName: String?,
    val panNumber: String?,
    val gstNumber: String?,
    val companyType: String?,
    val website: String?,
    val email: String?,
    val phone: String?,
    val alternatePhone: String?,
)

data class ClientAddress(
    val line1: String?,
    val line2: String?,
    val area: NamedLocation?,
    val city: NamedLocation?,
    val state: NamedLocation?,
    val pinCode: String?,
    val sameAsRegistered: Boolean?,
)

data class ClientPrimaryContact(
    val contactId: String,
    val fullName: String?,
    val designation: String?,
    val phone: String?,
    val alternatePhone: String?,
    val email: String?,
)

data class ClientContact(
    val contactId: String,
    val fullName: String,
    val designation: String?,
    val phone: String?,
    val alternatePhone: String?,
    val email: String?,
    val isPrimary: Boolean,
    val isActive: Boolean,
    val clientRoles: List<ClientContactRole>,
    val branchAssignments: List<ClientBranchAssignment>,
)

data class ClientContactRole(
    val roleId: String?,
    val code: String?,
    val name: String?,
    val description: String?,
    val applicableTo: String?,
)

data class ClientBranchAssignment(
    val branchId: String,
    val role: ClientContactRole?,
)

data class ClientBranch(
    val branchId: String,
    val branchName: String,
    val branchCode: String?,
    val branchTypeId: String?,
    val branchTypeCode: String?,
    val branchTypeName: String?,
    val status: String?,
    val contacts: List<ClientContact>,
    val documents: List<VerificationDocument>,
)

data class ClientDocumentSummary(
    val total: Int,
    val uploaded: Int,
    val pending: Int,
    val verified: Int,
    val rejected: Int,
)
