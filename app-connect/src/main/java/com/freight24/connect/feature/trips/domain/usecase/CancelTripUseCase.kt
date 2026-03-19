package com.freight24.connect.feature.trips.domain.usecase

import com.freight24.common.usecase.UseCase
import com.freight24.common.usecase.UseCaseParams
import com.freight24.connect.repository.trips.TripRepository
import com.freight24.rest.response.BaseResponse
import javax.inject.Inject

data class CancelTripUseCaseDataParams(
    val tripId: String,
    val reason: String,
): UseCaseParams

internal interface CancelTripUseCase : UseCase<CancelTripUseCaseDataParams, BaseResponse>


class CancelTripUseCaseImpl @Inject constructor(
    private val repository: TripRepository
) : CancelTripUseCase {

    override suspend fun invoke(params: CancelTripUseCaseDataParams): Result<BaseResponse> {
        return repository.cancelTrip(
            params.tripId,
            params.reason,
        )
    }
}