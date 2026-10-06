package com.freight24.rest.response

import kotlinx.serialization.Serializable

@Serializable
data class CreateTruckV2Response(
    val success: Boolean,
    val message: String,
    val data: CreateTruckV2Dto? = null,
)

@Serializable
data class CreateTruckV2Dto(
    val truckId: String,
    val registrationNumber: String,
    val status: String,
    val isVerified: Boolean,
    val isBlacklisted: Boolean,
    val availability: String,
    val owner: CreateTruckV2OwnerDto? = null,
    val vehicle: CreateTruckV2VehicleDto? = null,
    val truckType: String? = null,
    val bodyType: String? = null,
    val displayName: String? = null,
)

@Serializable
data class CreateTruckV2OwnerDto(
    val name: String? = null,
    val phone: String? = null,
    val tagCode: String? = null,
)

@Serializable
data class CreateTruckV2VehicleDto(
    val make: String? = null,
    val model: String? = null,
    val year: Int? = null,
    val capacityMt: Double? = null,
)
