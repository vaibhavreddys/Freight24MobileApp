package com.freight24.trucker.feature.trips.domain.usecase

import com.freight24.common.model.TripDetail
import com.freight24.common.usecase.UseCase
import com.freight24.common.usecase.UseCaseParams

internal interface FetchTripDetailUseCase : UseCase<FetchTripDetailParams, TripDetail>

data class FetchTripDetailParams(
    val tripId: String,
    val forceRefresh: Boolean = false
) : UseCaseParams
