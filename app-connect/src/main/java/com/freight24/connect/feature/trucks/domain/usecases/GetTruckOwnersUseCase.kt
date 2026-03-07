package com.freight24.connect.feature.trucks.domain.usecases

import com.freight24.connect.repository.TruckOwnerRepository
import com.freight24.rest.response.TruckOwnerDto
import javax.inject.Inject

interface GetTruckOwnersUseCase {
    suspend operator fun invoke(): Result<List<TruckOwnerDto>>
}

class GetTruckOwnersUseCaseImpl @Inject constructor(
    private val repository: TruckOwnerRepository
) : GetTruckOwnersUseCase {
    override suspend fun invoke(): Result<List<TruckOwnerDto>> {
        return repository.getTruckOwners()
    }
}
