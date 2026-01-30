package com.freight24.trucker.feature.trips.domain.usecase

import com.freight24.common.model.PaginatedTrips
import com.freight24.trucker.feature.trips.data.repository.TripRepository
import com.freight24.trucker.feature.trips.mapper.toDomain
import javax.inject.Inject

internal class FetchTripsUseCaseImpl
    @Inject
    constructor(
        private val repository: TripRepository,
    ) : FetchTripsUseCase {
        override suspend fun invoke(params: FetchTripsUseCaseParams): Result<PaginatedTrips> {
            return repository.fetchTrips(params.page).mapCatching { response ->
                PaginatedTrips(
                    trips = response.data?.map { it.toDomain() } ?: emptyList(),
                    page = response.pagination?.page ?: 1,
                    hasMore = response.pagination?.let { it.page < it.pages } ?: false,
                    totalTrips = response.pagination?.total ?: 0,
                )
            }
        }
    }
