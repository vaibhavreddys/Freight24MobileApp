package com.freight24.trucker.feature.trips.domain.usecase

import com.freight24.common.usecase.UseCase
import com.freight24.common.usecase.UseCaseParams
import com.freight24.trucker.feature.trips.data.model.PaginatedTrips

data class FetchTripsUseCaseParams(val page: Int = 0) : UseCaseParams

internal interface FetchTripsUseCase: UseCase<FetchTripsUseCaseParams, PaginatedTrips>