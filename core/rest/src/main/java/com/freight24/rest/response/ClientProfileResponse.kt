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
    val tag_code: String? = null,
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
    val documents: List<ClientDocumentDto> = emptyList(),
)

@Serializable
data class ClientDocumentDto(
    val documentId: String,
    val documentName: String,
    val documentUrl: String,
    val documentStatus: String,
)

@Serializable
data class ClientBranchDto(
    val id: String? = null,
    val client_id: String? = null,
    val branch_name: String? = null,
    val branch_address: String? = null,
    val pod_contact_name: String? = null,
    val pod_contact_number: String? = null,
    val payment_contact_name: String? = null,
    val traffic_contact_name: String? = null,
    val tracking_contact_name: String? = null,
    val payment_contact_number: String? = null,
    val traffic_contact_number: String? = null,
    val tracking_contact_number: String? = null,
    // Backward compatibility with older API payloads.
    val name: String? = null,
    val address: String? = null,
)
