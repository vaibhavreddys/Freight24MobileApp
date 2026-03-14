package com.freight24.rest.request

import kotlinx.serialization.Serializable

@Serializable
data class UpdateClientRequest(
    val full_name: String? = null,
    val email: String? = null,
    val company_name: String? = null,
    val gst_number: String? = null,
    val phone_number: String? = null,
    val aadhar_card: String? = null,
    val pan_card: String? = null,
    val branches: Map<String, ClientBranchRequest>? = null,
    val client_branches: List<ClientBranchUpdateRequest>? = null,
    val branch_name: String? = null,
    val branch_address: String? = null,
    val payment_contact_name: String? = null,
    val payment_contact_number: String? = null,
    val traffic_contact_name: String? = null,
    val traffic_contact_number: String? = null,
    val pod_contact_name: String? = null,
    val pod_contact_number: String? = null,
    val tracking_contact_name: String? = null,
    val tracking_contact_number: String? = null,
)

@Serializable
data class ClientBranchRequest(
    val branch_name: String? = null,
    val branch_address: String? = null,
    val payment_contact_name: String? = null,
    val payment_contact_number: String? = null,
    val traffic_contact_name: String? = null,
    val traffic_contact_number: String? = null,
    val pod_contact_name: String? = null,
    val pod_contact_number: String? = null,
    val tracking_contact_name: String? = null,
    val tracking_contact_number: String? = null,
)

@Serializable
data class ClientBranchUpdateRequest(
    val id: String? = null,
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
)
