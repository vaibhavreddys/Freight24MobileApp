package com.freight24.trucker.feature.trips.domain.usecase

import com.freight24.common.model.PaginatedTrips
import com.freight24.common.model.Trip
import com.freight24.common.usecase.UseCase
import com.freight24.common.usecase.UseCaseParams
import kotlinx.coroutines.flow.Flow

internal interface FetchTripsUseCase : UseCase<FetchTripsUseCaseParams, PaginatedTrips> {
    /** Locally cached manifest, painted before the network answers. */
    fun observeTrips(): Flow<List<Trip>>
}

data class FetchTripsUseCaseParams(
    val page: Int,
) : UseCaseParams
