package com.freight24.connect.domain.model

data class TruckOwnerManagementItem(
    val ownerUserId: String,
    val ownerProfileId: String?,
    val tagCode: String?,
    val fullName: String,
    val email: String?,
    val phone: String?,
    val businessName: String?,
    val gstNumber: String?,
    val panNumber: String?,
    val status: String?,
    val baseCity: NamedLocation?,
    val baseArea: NamedLocation?,
    val preferredRoutesCount: Int,
    val trucksCount: Int,
    val createdAt: String?,
    val updatedAt: String?,
)

data class TruckOwnerPickerOption(
    val ownerUserId: String,
    val ownerProfileId: String,
    val tagCode: String?,
    val fullName: String,
    val phone: String?,
    val businessName: String?,
    val status: String?,
)

data class TruckOwnerDetails(
    val ownerUserId: String,
    val ownerProfileId: String,
    val tagCode: String?,
    val fullName: String,
    val email: String?,
    val phone: String?,
    val alternatePhones: List<String>,
    val businessName: String?,
    val gstNumber: String?,
    val status: String,
    val baseLocation: String?,
    val panNumber: String?,
    val aadhaarNumber: String?,
    val isTdsApplicable: Boolean?,
    val truckStats: TruckOwnerTruckStats,
    val mandatoryDocuments: List<VerificationDocument>,
    val optionalDocuments: List<VerificationDocument>,
    val bankAccount: TruckOwnerBankAccount?,
    val bankVerified: Boolean,
    val documentsVerified: Boolean,
    val activationEligibility: ActivationEligibility?,
    val preferredRoutes: List<TruckOwnerPreferredRoute>,
    val profileVerificationStatus: String?,
    val createdAt: String?,
    val updatedAt: String?,
)

data class TruckOwnerTruckStats(
    val total: Int,
    val active: Int,
    val inactive: Int,
    val blacklisted: Int,
)

data class TruckOwnerBankAccount(
    val bankAccountId: String,
    val accountHolderName: String?,
    val accountNumber: String?,
    val ifscCode: String?,
    val upiId: String?,
    val bankName: String?,
    val branchName: String?,
    val cancelledChequeUrl: String?,
    val verificationStatus: String?,
)

data class TruckOwnerPreferredRoute(
    val routeId: String,
    val priority: Int?,
    val source: String?,
    val sourceCity: NamedLocation?,
    val destinationCity: NamedLocation?,
)
