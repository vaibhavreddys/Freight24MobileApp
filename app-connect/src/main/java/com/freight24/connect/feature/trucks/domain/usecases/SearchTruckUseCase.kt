package com.freight24.connect.feature.trucks.domain.usecases

import com.freight24.common.usecase.UseCase
import com.freight24.common.usecase.UseCaseParams
import com.freight24.connect.repository.TruckRepository
import com.freight24.rest.response.SearchTruckResponse
import javax.inject.Inject

data class SearchTrucksUseCaseParams(
    val query: String,
): UseCaseParams


interface SearchTruckUseCase : UseCase<SearchTrucksUseCaseParams, List<SearchTruckResponse.Truck>>

class SearchTruckUseCaseImpl @Inject constructor(
    private val repository: TruckRepository
) : SearchTruckUseCase {
    override suspend fun invoke(
        params: SearchTrucksUseCaseParams
    ): Result<List<SearchTruckResponse.Truck>> {
        return repository.searchTrucks(
            params.query
        )
    }
}