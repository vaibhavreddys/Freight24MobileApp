package com.freight24.connect.feature.findTrucks.domain.usecases

import com.freight24.common.usecase.UseCase
import com.freight24.common.usecase.UseCaseParams
import com.freight24.connect.repository.FindTrucksRepository
import com.freight24.rest.response.SearchTruckerResponse.TruckerData
import javax.inject.Inject

data class FindTrucksUseCaseDataParams(
    val areaId: String?,
    val cityId: String?,
    val destinationCityId: String?,
    val truckTypeIds: String,
): UseCaseParams

interface FindTrucksUseCase : UseCase<FindTrucksUseCaseDataParams, TruckerData>


class FindTrucksUseCaseImpl @Inject constructor(
    private val repository: FindTrucksRepository
) : FindTrucksUseCase {

    override suspend fun invoke(params: FindTrucksUseCaseDataParams): Result<TruckerData> {
        return repository.searchTrucks(
            params.areaId,
            params.cityId,
            params.destinationCityId,
            params.truckTypeIds
        )
    }
}