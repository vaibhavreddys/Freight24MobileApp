package com.freight24.trucker.feature.profile.domain

/**
 * Domain model representing the trucker's profile.
 */
data class Profile(
    val id: String,
    val userId: String,
    val fullName: String?,
    val emailId: String?,
    val phoneNo: String?,
    val alternativePhoneNo: List<String>,
    val panCard: String?,
    val aadharCard: String?,
    val isTdsApplicable: Boolean,
    val tagCode: String?,
    val documents: List<ProfileDocument>,
    val bankAccountDetails: BankAccountDetails?,
    val businessName: String?,
    val gstNumber: String?
)

data class BankAccountDetails(
    val accountHolderName: String?,
    val accountNumber: String?,
    val ifscCode: String?,
    val upiId: String?,
    val bankName: String?,
    val branchName: String?
)

data class ProfileDocument(
    val type: DocumentType,
    val url: String?,
    val status: DocumentStatus
)

enum class DocumentType(val displayName: String) {
    PAN("PAN Card"),
    AADHAR("Aadhar Card"),
    GST_CERTIFICATE("GST Certificate"),
    TDS_CERTIFICATE("TDS Certificate"),
    CANCELLED_CHEQUE("Cancelled Cheque"),
    UNKNOWN("Unknown")
}

enum class DocumentStatus(val displayText: String) {
    UPLOADED("Uploaded"),
    PENDING("Pending upload"),
    UNKNOWN("Unknown")
}
