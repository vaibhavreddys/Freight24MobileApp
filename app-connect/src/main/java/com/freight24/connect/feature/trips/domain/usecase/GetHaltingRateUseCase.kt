package com.freight24.connect.feature.trips.domain.usecase

import com.freight24.common.usecase.UseCase
import com.freight24.common.usecase.UseCaseParams
import com.freight24.connect.repository.trips.TripHaltingRepository
import com.freight24.rest.response.TruckHaltingRate
import javax.inject.Inject

data class GetHaltingRateUseCaseDataParams(
    val truckTypeId: String,
) : UseCaseParams

internal interface GetHaltingRateUseCase : UseCase<GetHaltingRateUseCaseDataParams, TruckHaltingRate>

class GetHaltingRateUseCaseImpl
    @Inject
    constructor(
        private val repository: TripHaltingRepository,
    ) : GetHaltingRateUseCase {
        override suspend fun invoke(params: GetHaltingRateUseCaseDataParams): Result<TruckHaltingRate> {
            return repository.getHaltingRate(params.truckTypeId)
        }
    }
