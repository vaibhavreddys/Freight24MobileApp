package com.freight24.rest.response

import kotlinx.serialization.Serializable

@Serializable
data class TruckTypeDto(
    val id: String,
    val truckType: String,
    val displayName: String,
    val description: String? = null,
    val bodyType: String? = null,
)
