package com.freight24.connect.feature.trips.domain.usecase

import com.freight24.common.usecase.UseCase
import com.freight24.common.usecase.UseCaseParams
import com.freight24.connect.repository.trips.TripRepository
import com.freight24.rest.response.TripDataResponse
import javax.inject.Inject

data class UpdateTruckerUseCaseDataParams(
    val tripId: String,
    val truckerStatus: String,
    val remarks: String?,
) : UseCaseParams

internal interface UpdateTruckerUseCase : UseCase<UpdateTruckerUseCaseDataParams, TripDataResponse>

class UpdateTruckerUseCaseImpl
    @Inject
    constructor(
        private val repository: TripRepository,
    ) : UpdateTruckerUseCase {
        override suspend fun invoke(params: UpdateTruckerUseCaseDataParams): Result<TripDataResponse> {
            return repository.updateTruckerStatus(
                params.tripId,
                params.truckerStatus,
                params.remarks,
            )
        }
    }
