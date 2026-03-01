package com.freight24.connect.feature.trips.domain.usecase

import com.freight24.common.usecase.UseCase
import com.freight24.common.usecase.UseCaseParams
import com.freight24.connect.repository.trips.TripHaltingRepository
import com.freight24.rest.response.UpdateHaltingRatesResponse
import javax.inject.Inject

data class UpdateHaltingRateUseCaseDataParams(
    val tripId: String,
    val loadingDays: Int?,
    val unloadingDays: Int?,
    val ratePerDay: Int? = null,
    val notes: String?
): UseCaseParams

internal interface UpdateHaltingRateUseCase : UseCase<UpdateHaltingRateUseCaseDataParams, UpdateHaltingRatesResponse.UpdateHaltingRatesData>


class UpdateHaltingRateUseCaseImpl @Inject constructor(
    private val repository: TripHaltingRepository
) : UpdateHaltingRateUseCase {

    override suspend fun invoke(params: UpdateHaltingRateUseCaseDataParams): Result<UpdateHaltingRatesResponse.UpdateHaltingRatesData> {
        return repository.addHaltingCharges(
            params.tripId,
            params.loadingDays,
            params.unloadingDays,
            params.ratePerDay,
            params.notes
        )
    }
}