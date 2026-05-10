package com.freight24.connect.feature.verificationrequests.domain.usecase

import com.freight24.connect.repository.verificationrequests.VerificationRequestsRepository
import javax.inject.Inject

data class UpdateTruckOwnerVerificationStatusUseCaseParams(
    val id: String,
    val status: String,
    val resolutionNotes: String? = null,
)

interface UpdateTruckOwnerVerificationStatusUseCase {
    suspend operator fun invoke(params: UpdateTruckOwnerVerificationStatusUseCaseParams): Result<Unit>
}

class UpdateTruckOwnerVerificationStatusUseCaseImpl @Inject constructor(
    private val repository: VerificationRequestsRepository,
) : UpdateTruckOwnerVerificationStatusUseCase {
    override suspend fun invoke(params: UpdateTruckOwnerVerificationStatusUseCaseParams): Result<Unit> {
        return repository.updateTruckOwnerVerificationStatus(
            id = params.id,
            status = params.status,
            resolutionNotes = params.resolutionNotes,
        )
    }
}

data class UpdateClientVerificationStatusUseCaseParams(
    val id: String,
    val status: String,
    val resolutionNotes: String? = null,
)

interface UpdateClientVerificationStatusUseCase {
    suspend operator fun invoke(params: UpdateClientVerificationStatusUseCaseParams): Result<Unit>
}

class UpdateClientVerificationStatusUseCaseImpl @Inject constructor(
    private val repository: VerificationRequestsRepository,
) : UpdateClientVerificationStatusUseCase {
    override suspend fun invoke(params: UpdateClientVerificationStatusUseCaseParams): Result<Unit> {
        return repository.updateClientVerificationStatus(
            id = params.id,
            status = params.status,
            resolutionNotes = params.resolutionNotes,
        )
    }
}
