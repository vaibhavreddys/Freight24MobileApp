package com.freight24.trucker.feature.trucks.domain.usecase

import com.freight24.rest.response.AddTruckResponse
import com.freight24.trucker.feature.trucks.data.repository.TruckRepository
import javax.inject.Inject

internal class UpdateTruckDocumentsUseCaseImpl
    @Inject
    constructor(
        private val repository: TruckRepository,
    ) : UpdateTruckDocumentsUseCase {
        override suspend fun invoke(params: UpdateTruckDocumentsParams): Result<AddTruckResponse> {
            return repository.updateTruckDocuments(
                params.truckId,
                params.rcCard,
                params.fcCertificate,
                params.insurance,
            )
        }
    }
