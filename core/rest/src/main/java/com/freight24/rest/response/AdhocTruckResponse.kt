package com.freight24.rest.response

import kotlinx.serialization.Serializable

@Serializable
data class AdhocTruckOwnerSearchResponse(
    val success: Boolean,
    val data: List<AdhocTruckOwnerDto> = emptyList(),
)

@Serializable
data class AdhocTruckOwnerDto(
    val id: String,
    val fullName: String,
    val phone: String,
    val tagCode: String? = null,
)
