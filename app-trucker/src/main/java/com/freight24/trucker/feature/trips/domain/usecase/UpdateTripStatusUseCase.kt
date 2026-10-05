package com.freight24.trucker.feature.trips.domain.usecase

import com.freight24.common.model.TripDetail
import com.freight24.common.usecase.UseCase
import com.freight24.common.usecase.UseCaseParams

data class UpdateTripStatusParams(val tripId: String, val status: String) : UseCaseParams

interface UpdateTripStatusUseCase : UseCase<UpdateTripStatusParams, TripDetail>
