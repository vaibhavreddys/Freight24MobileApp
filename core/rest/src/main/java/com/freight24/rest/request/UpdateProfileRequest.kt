package com.freight24.rest.request

import kotlinx.serialization.Serializable

@Serializable
data class UpdateProfileRequest(
    val full_name: String? = null,
    val email: String? = null,
    val alt_phones: List<String>? = null,
    val pan_card: String? = null,
    val aadhar_card: String? = null,
    val business_name: String? = null,
    val gst_number: String? = null,
    val is_tds_applicable: Boolean? = null,
    val account_holder_name: String? = null,
    val account_number: String? = null,
    val ifsc_code: String? = null,
    val upi_id: String? = null,
    val bank_name: String? = null,
    val branch_name: String? = null
)
