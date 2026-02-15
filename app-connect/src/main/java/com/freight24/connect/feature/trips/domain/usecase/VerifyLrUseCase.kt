package com.freight24.connect.feature.trips.domain.usecase

import com.freight24.common.usecase.UseCase
import com.freight24.common.usecase.UseCaseParams
import com.freight24.connect.repository.TripRepository
import com.freight24.rest.response.TripDataResponse
import javax.inject.Inject

data class VerifyLrUseCaseDataParams(
    val tripId : String,
    val documentId: String,
): UseCaseParams

internal interface VerifyLrUseCase : UseCase<VerifyLrUseCaseDataParams, TripDataResponse>


class VerifyLrUseCaseImpl @Inject constructor(
    private val repository: TripRepository
) : VerifyLrUseCase {

    override suspend fun invoke(params: VerifyLrUseCaseDataParams): Result<TripDataResponse> {
        return repository.verifyLr(
            params.tripId,
            params.documentId
        )
    }
}
