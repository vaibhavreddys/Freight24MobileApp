package com.freight24.trucker.feature.trucks.domain.usecase

import com.freight24.common.usecase.UseCase
import com.freight24.common.usecase.UseCaseParams
import com.freight24.rest.response.AddTruckResponse

data class UpdateTruckParams(
    val truckId: String,
    val registrationNumber: String,
    val truckTypeId: Int,
    val make: String? = null,
    val model: String? = null,
    val year: String? = null,
    val rcCard: java.io.File? = null,
    val fcCertificate: java.io.File? = null,
    val insurance: java.io.File? = null,
) : UseCaseParams

interface UpdateTruckUseCase : UseCase<UpdateTruckParams, AddTruckResponse>
