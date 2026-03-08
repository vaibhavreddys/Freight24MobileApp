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
)
