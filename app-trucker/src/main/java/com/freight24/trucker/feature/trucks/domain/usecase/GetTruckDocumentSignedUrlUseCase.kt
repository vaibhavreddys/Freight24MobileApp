package com.freight24.trucker.feature.trucks.domain.usecase

import com.freight24.common.usecase.UseCase
import com.freight24.common.usecase.UseCaseParams

data class GetTruckDocumentSignedUrlParams(
    val truckId: String,
    val documentType: String,
) : UseCaseParams

interface GetTruckDocumentSignedUrlUseCase : UseCase<GetTruckDocumentSignedUrlParams, String>
