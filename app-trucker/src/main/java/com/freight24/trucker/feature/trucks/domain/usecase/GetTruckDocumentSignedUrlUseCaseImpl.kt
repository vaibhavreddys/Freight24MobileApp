package com.freight24.trucker.feature.trucks.domain.usecase

import com.freight24.trucker.feature.trucks.data.repository.TruckRepository
import javax.inject.Inject

internal class GetTruckDocumentSignedUrlUseCaseImpl @Inject constructor(
    private val repository: TruckRepository,
) : GetTruckDocumentSignedUrlUseCase {
    override suspend fun invoke(params: GetTruckDocumentSignedUrlParams): Result<String> =
        repository.getTruckDocumentSignedUrl(params.truckId, params.documentType)
}
