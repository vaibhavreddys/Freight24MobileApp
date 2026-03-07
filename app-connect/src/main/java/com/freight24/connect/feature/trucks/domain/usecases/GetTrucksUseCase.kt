package com.freight24.connect.feature.trucks.domain.usecases

import com.freight24.connect.repository.TruckRepository
import com.freight24.rest.response.TruckDto
import javax.inject.Inject

interface GetTrucksUseCase {
    suspend operator fun invoke(): Result<List<TruckDto>>
}

class GetTrucksUseCaseImpl @Inject constructor(
    private val repository: TruckRepository
) : GetTrucksUseCase {
    override suspend fun invoke(): Result<List<TruckDto>> {
        return repository.getTrucks()
    }
}
