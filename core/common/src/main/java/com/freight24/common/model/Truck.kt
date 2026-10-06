package com.freight24.common.model

data class Truck(
    val id: String,
    val registrationNumber: String,
    val isVerified: Boolean,
    val isBlacklisted: Boolean,
    val availability: String,
    val truckType: String?,
    val bodyType: String?,
    val capacityMt: Double? = null,
)
