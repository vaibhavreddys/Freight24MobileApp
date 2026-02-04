package com.freight24.connect.feature.trucks.domain.usecases

import com.freight24.common.usecase.UseCase
import com.freight24.common.usecase.UseCaseParams
import com.freight24.connect.repository.TruckRepository
import com.freight24.rest.response.TruckDetailResponse
import javax.inject.Inject

data class GetTruckByIdUseCaseParams(
    val truckId: String
) : UseCaseParams

interface GetTruckByIdUseCase : UseCase<GetTruckByIdUseCaseParams, TruckDetailResponse>

class GetTruckByIdUseCaseImpl @Inject constructor(
    private val repository: TruckRepository
) : GetTruckByIdUseCase {
    override suspend fun invoke(input: GetTruckByIdUseCaseParams): Result<TruckDetailResponse> {
        return repository.getTruckDetail(input.truckId)
    }
}
