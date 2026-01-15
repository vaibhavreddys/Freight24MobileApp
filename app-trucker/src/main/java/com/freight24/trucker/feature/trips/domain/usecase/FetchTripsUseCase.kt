package com.freight24.trucker.feature.trips.domain.usecase

import com.freight24.common.model.PaginatedTrips
import com.freight24.common.usecase.UseCase
import com.freight24.common.usecase.UseCaseParams

internal interface FetchTripsUseCase : UseCase<FetchTripsUseCaseParams, PaginatedTrips>

data class FetchTripsUseCaseParams(
    val page: Int,
) : UseCaseParams