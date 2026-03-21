package com.freight24.rest.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProfileResponse(
    val success: Boolean,
    val data: ProfileDto?,
)

@Serializable
data class ProfileDto(
    val id: String,
    val userId: String,
    @SerialName("base_city_id")
    val baseCityId: Int? = null,
    @SerialName("base_city_name")
    val baseCityName: String? = null,
    @SerialName("base_city_state")
    val baseCityState: String? = null,
    @SerialName("baseLocation")
    val baseLocation: String? = null,
    val fullName: String?,
    val emailId: String?,
    val phoneNo: String?,
    val alternativePhoneNo: List<String>?,
    
    @SerialName("panNumber")
    val panNumber: String? = null,
    
    @SerialName("panCard")
    val panCard: String? = null,
    
    @SerialName("aadhaarNumber")
    val aadhaarNumber: String? = null,
    
    @SerialName("aadharCard") // API uses "aadharCard" (single 'a' after 'd')
    val aadharCard: String? = null,
    
    val isTdsApplicable: Boolean?,
    val tagCode: String?,
    val documents: List<ProfileDocumentDto>?,
    val bankAccountDetails: BankAccountDetailsDto?,
    val businessName: String?,
    val gstNumber: String?,
    val bankAccountVerificationStatus: Boolean? = null,
    val documentsVerificationStatus: Boolean? = null,
    val profileVerificationStatus: String? = null,
    val preferredRoutes: List<PreferredRouteDto>? = null,
)

@Serializable
data class BankAccountDetailsDto(
    val accountHolderName: String?,
    val accountNumber: String?,
    val ifscCode: String?,
    val upiId: String?,
    val bankName: String?,
    val branchName: String?,
)

@Serializable
data class ProfileDocumentDto(
    val documentId: String? = null,
    val documentType: String?,
    val url: String?,
    val status: String?,
)

@Serializable
data class PreferredRouteDto(
    val id: String,
    val priority: Int,
    val source: String? = null,
    val sourceCity: PreferredRouteCityDto? = null,
    val destinationCity: PreferredRouteCityDto? = null,
)

@Serializable
data class PreferredRouteCityDto(
    val id: Int,
    val name: String?,
)
