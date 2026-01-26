package com.freight24.trucker.feature.trips.domain.usecase

import com.freight24.common.model.TripDetail
import com.freight24.trucker.feature.trips.data.repository.TripRepository
import javax.inject.Inject

internal class UpdateTripStatusUseCaseImpl @Inject constructor(
    private val repository: TripRepository
) : UpdateTripStatusUseCase {
    override suspend fun invoke(params: UpdateTripStatusParams): Result<TripDetail> {
        return repository.updateTripStatus(params.tripId, params.status)
    }
}
