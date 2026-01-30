package com.freight24.trucker.feature.trucks.domain.usecase

import java.io.File

data class AddTruckParams(
    val registrationNumber: String,
    val truckTypeId: Int,
    val make: String?,
    val model: String?,
    val year: String?,
    val rcCard: File?,
    val fcCertificate: File?,
    val insurance: File?,
)
