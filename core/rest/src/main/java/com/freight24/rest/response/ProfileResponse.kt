package com.freight24.rest.response

import kotlinx.serialization.Serializable

@Serializable
data class ProfileResponse(
    val success: Boolean,
    val data: ProfileDto?
)

@Serializable
data class ProfileDto(
    val id: String,
    val userId: String,
    val fullName: String?,
    val emailId: String?,
    val phoneNo: String?,
    val alternativePhoneNo: List<String>?,
    val panCard: String?,
    val aadharCard: String?,
    val isTdsApplicable: Boolean?,
    val tagCode: String?,
    val documents: List<ProfileDocumentDto>?,
    val bankAccountDetails: BankAccountDetailsDto?,
    val businessName: String?,
    val gstNumber: String?
)

@Serializable
data class BankAccountDetailsDto(
    val accountHolderName: String?,
    val accountNumber: String?,
    val ifscCode: String?,
    val upiId: String?,
    val bankName: String?,
    val branchName: String?
)

@Serializable
data class ProfileDocumentDto(
    val type: String?,
    val url: String?,
    val status: String?
)
