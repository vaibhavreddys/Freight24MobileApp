package com.freight24.connect.feature.truckowners.domain.usecases

import com.freight24.connect.repository.TruckOwnerRepository
import com.freight24.rest.request.UpdateProfileRequest
import javax.inject.Inject

data class CreateTruckOwnerParams(
    val request: UpdateProfileRequest
)

interface CreateTruckOwnerUseCase {
    suspend operator fun invoke(params: CreateTruckOwnerParams): Result<Boolean>
}

class CreateTruckOwnerUseCaseImpl @Inject constructor(
    private val repository: TruckOwnerRepository
) : CreateTruckOwnerUseCase {
    override suspend fun invoke(params: CreateTruckOwnerParams): Result<Boolean> {
        return repository.createTruckOwner(params.request)
    }
}
