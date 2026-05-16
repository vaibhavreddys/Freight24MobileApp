package com.freight24.connect.feature.truckowners.profile.domain.usecase

import com.freight24.connect.feature.truckowners.profile.domain.ProfileRepository
import javax.inject.Inject

interface UpdateTruckOwnerStatusUseCase {
    suspend operator fun invoke(params: UpdateTruckOwnerStatusParams): Result<Boolean>
}

data class UpdateTruckOwnerStatusParams(
    val userId: String,
    val status: String
)

class UpdateTruckOwnerStatusUseCaseImpl @Inject constructor(
    private val repository: ProfileRepository
) : UpdateTruckOwnerStatusUseCase {
    override suspend fun invoke(params: UpdateTruckOwnerStatusParams): Result<Boolean> {
        return repository.updateTruckOwnerStatus(params.userId, params.status)
    }
}
