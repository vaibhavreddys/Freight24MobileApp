package com.freight24.connect.feature.trips.domain.usecase

import com.freight24.common.usecase.UseCase
import com.freight24.common.usecase.UseCaseParams
import com.freight24.connect.repository.trips.TripRepository
import com.freight24.rest.response.TripDataResponse
import javax.inject.Inject

data class TripDetailsUseCaseDataParams(
    val tripId: String,
) : UseCaseParams

internal interface TripDetailsUseCase : UseCase<TripDetailsUseCaseDataParams, TripDataResponse>

class TripDetailsUseCaseImpl
    @Inject
    constructor(
        private val repository: TripRepository,
    ) : TripDetailsUseCase {
        override suspend fun invoke(params: TripDetailsUseCaseDataParams): Result<TripDataResponse> {
            return repository.getTripDetails(
                params.tripId,
            )
        }
    }
