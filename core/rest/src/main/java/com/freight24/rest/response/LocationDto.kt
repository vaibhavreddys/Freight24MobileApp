package com.freight24.rest.response

import kotlinx.serialization.Serializable

@Serializable
data class LocationResponse(
    val data: List<LocationDto>,
) : BaseResponse()

@Serializable
data class LocationDto(
    val id: String,
    val type: String,
    val name: String,
    val city: String? = null,
    val state: String? = null,
)
