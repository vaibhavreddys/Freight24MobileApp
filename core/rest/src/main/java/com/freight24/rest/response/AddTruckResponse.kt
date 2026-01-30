package com.freight24.rest.response

import kotlinx.serialization.Serializable

@Serializable
data class AddTruckResponse(
    val success: Boolean,
    val message: String,
    val data: AddTruckDto?,
)

@Serializable
data class AddTruckDto(
    val id: String,
    val truck_owner_id: String,
    val truck_type_id: Int,
    val registration_number: String,
    val make: String?,
    val model: String?,
    val year: String?,
    val rc_card_url: String?,
    val fc_certificate_url: String?,
    val insurance_url: String?,
    val rc_verify_status: String?,
    val fc_verify_status: String?,
    val insurance_verify_status: String?,
    val verified: Boolean,
    val status: String,
    val created_at: String,
    val updated_at: String,
)
