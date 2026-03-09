package com.freight24.rest.response

import kotlinx.serialization.Serializable

@Serializable
data class TruckOwnerResponse(
    val success: Boolean,
    val message: String? = null,
    val data: List<TruckOwnerDto> = emptyList()
)

@Serializable
data class TruckOwnerDto(
    val id: String,
    val user_id: String? = null,
    val full_name: String,
    val phone: String,
    val business_name: String? = null,
    val status: String? = null,
    val tag_code: String? = null
)
