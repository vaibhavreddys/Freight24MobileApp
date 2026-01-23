package com.freight24.trucker.feature.trucks.domain.usecase

import com.freight24.common.model.Truck
import com.freight24.common.usecase.EmptyUseCaseParams
import com.freight24.trucker.feature.trucks.data.repository.TruckRepository
import com.freight24.trucker.feature.trucks.mapper.toDomain
import javax.inject.Inject

internal class FetchTrucksUseCaseImpl @Inject constructor(
    private val repository: TruckRepository
) : FetchTrucksUseCase {
    override suspend fun invoke(params: EmptyUseCaseParams): Result<List<Truck>> {
        return repository.fetchTrucks().map { response ->
            response.trucks?.map { it.toDomain() }.orEmpty()
        }
    }
}
