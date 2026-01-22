package com.freight24.rest.response

import kotlinx.serialization.Serializable

@Serializable
data class TruckResponse(
    val success: Boolean,
    val trucks: List<TruckDto>?
)

@Serializable
data class TruckDto(
    val truckId: String,
    val registrationNumber: String,
    val isVerified: Boolean,
    val isBlacklisted: Boolean,
    val availability: String,
    val truckType: String?,
    val bodyType: String?
)
