package com.freight24.trucker.feature.trips.domain.usecase

import com.freight24.trucker.feature.trips.data.model.PaginatedTrips
import com.freight24.trucker.feature.trips.data.repository.TripRepository
import com.freight24.trucker.feature.trips.mapper.toDomain
import javax.inject.Inject

internal class FetchTripsUseCaseImpl @Inject constructor(
    private val repository: TripRepository,
) : FetchTripsUseCase {

    override suspend fun invoke(params: FetchTripsUseCaseParams): Result<PaginatedTrips> =
        runCatching {
           val response = getTrips(params.page)
           val domainTrips = response.data?.map { it.toDomain() } ?: emptyList()
            PaginatedTrips(
                trips = domainTrips,
                page = params.page,
                hasMore = (response.pagination?.pages ?: 0) > params.page,
                totalTrips = response.pagination?.total ?: 0,
            )
        }


    private suspend fun getTrips(page: Int) = repository.fetchTrips(page).getOrThrow()

}