package com.freight24.rest.response

import kotlinx.serialization.Serializable

@Serializable
data class ClientProfileResponse(
    val data: ClientProfileDto,
) : BaseResponse()

@Serializable
data class ClientProfileDto(
    val id: String,
    val user_id: String,
    val full_name: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val alt_phones: List<String>? = null,
    val pan_card: String? = null,
    val aadhar_card: String? = null,
    val company_name: String? = null,
    val gst_number: String? = null,
    val created_at: String? = null,
    val status: String? = null,
    val client_branches: List<ClientBranchDto> = emptyList(),
)

@Serializable
data class ClientBranchDto(
    val id: String? = null,
    val name: String? = null,
    val address: String? = null,
)
