package com.freight24.common.model

data class TruckDetail(
    val truckId: String,
    val registrationNumber: String,
    val isVerified: Boolean,
    val truckType: String?,
    val bodyType: String?,
    val displayName: String?,
    val make: String?,
    val model: String?,
    val year: String?,
    val capacityMt: Double?,
    val rcCard: TruckDocument?,
    val fcCertificate: TruckDocument?,
    val insurance: TruckDocument?,
    val isBlacklisted: Boolean,
)
