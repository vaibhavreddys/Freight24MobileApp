package com.freight24.connect.feature.trips.domain.usecase

import com.freight24.common.usecase.UseCase
import com.freight24.common.usecase.UseCaseParams
import com.freight24.connect.repository.trips.TripRepository
import com.freight24.rest.response.MyTripsDataResponse
import javax.inject.Inject

enum class TripTypes(val filter: String, val displayText: String) {
    ONGOING("ONGOING", "ONGOING"),
    IN_TRANSIT("IN_TRANSIT", "IN-TRANSIT"),
    COMPLETED("COMPLETED", "COMPLETED");
}

data class GetMyTripsUseCaseDataParams(
    val tripTypes: TripTypes = TripTypes.ONGOING,
    val page: Int?,
    val limit: Int?,
): UseCaseParams

internal interface GetMyTripsUseCase : UseCase<GetMyTripsUseCaseDataParams, MyTripsDataResponse>


class GetMyTripsUseCaseImpl @Inject constructor(
    private val repository: TripRepository
) : GetMyTripsUseCase {

    override suspend fun invoke(params: GetMyTripsUseCaseDataParams): Result<MyTripsDataResponse> {
        return repository.getMyTrips(
            params.tripTypes.filter,
            params.page ?: 1,
            params.limit ?: 20,
        )
    }
}