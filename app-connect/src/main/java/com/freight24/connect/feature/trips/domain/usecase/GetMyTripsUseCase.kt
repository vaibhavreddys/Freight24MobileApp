package com.freight24.connect.feature.trips.domain.usecase

import com.freight24.common.usecase.UseCase
import com.freight24.common.usecase.UseCaseParams
import com.freight24.connect.repository.trips.TripRepository
import com.freight24.rest.response.MyTripsDataResponse
import javax.inject.Inject

enum class TripTypes(val filter: String, val displayText: String) {
    ONGOING("ONGOING", "ONGOING"),
    IN_TRANSIT("IN_TRANSIT", "IN-TRANSIT"),
    COMPLETED("COMPLETED", "COMPLETED"),
    LR_NOT_UPLOADED("LR_NOT_UPLOADED", "Upload LR Pending"),
    LR_PENDING_REVIEW("LR_PENDING_REVIEW", "Review LR"),
    WAITING_FOR_POD("WAITING_FOR_POD", "POD Pending");
}

data class GetMyTripsUseCaseDataParams(
    val tripTypes: TripTypes = TripTypes.ONGOING,
    val page: Int?,
    val limit: Int?,
): UseCaseParams

interface GetMyTripsUseCase : UseCase<GetMyTripsUseCaseDataParams, MyTripsDataResponse>


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