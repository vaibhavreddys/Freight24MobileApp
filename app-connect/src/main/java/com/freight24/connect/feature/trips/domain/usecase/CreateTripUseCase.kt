package com.freight24.connect.feature.trips.domain.usecase

import com.freight24.common.usecase.UseCase
import com.freight24.common.usecase.UseCaseParams
import com.freight24.connect.repository.trips.TripRepository
import com.freight24.rest.response.CreatedTripResponse
import javax.inject.Inject

data class CreateTripUseCaseDataParams(
     val indentId: String,
     val truckId: String,
     val driverName: String?,
     val driverPhone: String?,
): UseCaseParams

internal interface CreateTripUseCase : UseCase<CreateTripUseCaseDataParams, CreatedTripResponse.TripData>


class CreateTripUseCaseImpl @Inject constructor(
    private val repository: TripRepository
) : CreateTripUseCase {

    override suspend fun invoke(params: CreateTripUseCaseDataParams): Result<CreatedTripResponse.TripData> {
        return repository.createTrip(
            params.indentId,
            params.truckId,
            params.driverName,
            params.driverPhone
        )
    }
}