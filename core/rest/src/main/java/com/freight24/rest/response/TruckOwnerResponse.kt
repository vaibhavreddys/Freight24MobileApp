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
    val full_name: String,
    val phone: String,
    val tag_code: String? = null
)
