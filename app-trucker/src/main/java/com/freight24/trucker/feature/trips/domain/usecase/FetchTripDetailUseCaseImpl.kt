package com.freight24.trucker.feature.trips.domain.usecase

import com.freight24.common.model.TripDetail
import com.freight24.trucker.feature.trips.data.repository.TripRepository
import javax.inject.Inject

internal class FetchTripDetailUseCaseImpl
    @Inject
    constructor(
        private val repository: TripRepository,
    ) : FetchTripDetailUseCase {
        override suspend fun invoke(params: FetchTripDetailParams): Result<TripDetail> {
            return repository.fetchTripDetail(params.tripId, params.forceRefresh)
        }
    }
