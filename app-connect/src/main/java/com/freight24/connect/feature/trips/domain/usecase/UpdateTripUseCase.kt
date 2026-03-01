package com.freight24.connect.feature.trips.domain.usecase

import com.freight24.common.usecase.UseCase
import com.freight24.common.usecase.UseCaseParams
import com.freight24.connect.repository.trips.TripRepository
import com.freight24.rest.response.TripDataResponse
import javax.inject.Inject

data class UpdateTripUseCaseDataParams(
    val tripId: String,
    val driverName: String?,
    val driverPhone: String?,
    val clientTripCost: String?,
    val truckerTripCost: String?,
    val luIncluded: Boolean?,
    val truckId: String?,
    val reason: String?
): UseCaseParams

internal interface UpdateTripUseCase : UseCase<UpdateTripUseCaseDataParams, TripDataResponse>


class UpdateTripUseCaseImpl @Inject constructor(
    private val repository: TripRepository
) : UpdateTripUseCase {

    override suspend fun invoke(params: UpdateTripUseCaseDataParams): Result<TripDataResponse> {
        return repository.updateTrip(
            tripId = params.tripId,
            driverName = params.driverName,
            driverPhone = params.driverPhone,
            clientTripCost = params.clientTripCost,
            truckerTripCost = params.truckerTripCost,
            luIncluded = params.luIncluded,
            truckId = params.truckId,
            reason = params.reason,
        )
    }
}