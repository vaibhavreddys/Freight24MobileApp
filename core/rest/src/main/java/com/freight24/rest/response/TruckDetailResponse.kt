package com.freight24.rest.response

import kotlinx.serialization.Serializable

@Serializable
data class TruckDetailResponse(
    val success: Boolean,
    val data: TruckDetailDto?,
)

@Serializable
data class TruckDetailDto(
    val truckId: String,
    val registrationNumber: String,
    val isVerified: Boolean,
    val truckType: String?,
    val bodyType: String?,
    val displayName: String?,
    val make: String?,
    val model: String?,
    val year: String?,
    val capacityMt: Double? = null,
    val rcCard: DocumentDto?,
    val fcCertificate: DocumentDto?,
    val insurance: DocumentDto?,
    val isBlacklisted: Boolean,
)

@Serializable
data class DocumentDto(
    val url: String?,
    val status: String,
)
