package com.freight24.connect.feature.trucks.domain.usecases

import com.freight24.common.usecase.UseCase
import com.freight24.common.usecase.UseCaseParams
import com.freight24.connect.repository.TruckRepository
import com.freight24.connect.domain.model.TruckDetails
import javax.inject.Inject

data class GetTruckByIdUseCaseParams(
    val truckId: String,
) : UseCaseParams

interface GetTruckByIdUseCase : UseCase<GetTruckByIdUseCaseParams, TruckDetails>

class GetTruckByIdUseCaseImpl
    @Inject
    constructor(
        private val repository: TruckRepository,
    ) : GetTruckByIdUseCase {
        override suspend fun invoke(params: GetTruckByIdUseCaseParams): Result<TruckDetails> {
            return repository.getTruckDetails(params.truckId)
        }
    }

data class GetTruckDocumentSignedUrlParams(
    val truckId: String,
    val documentType: String,
) : UseCaseParams

interface GetTruckDocumentSignedUrlUseCase : UseCase<GetTruckDocumentSignedUrlParams, String>

class GetTruckDocumentSignedUrlUseCaseImpl
    @Inject
    constructor(
        private val repository: TruckRepository,
    ) : GetTruckDocumentSignedUrlUseCase {
        override suspend fun invoke(params: GetTruckDocumentSignedUrlParams): Result<String> =
            repository.getTruckDocumentSignedUrl(params.truckId, params.documentType)
    }
