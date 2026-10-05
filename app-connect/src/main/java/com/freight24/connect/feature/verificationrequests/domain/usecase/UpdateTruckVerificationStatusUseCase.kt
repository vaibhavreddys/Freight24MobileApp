package com.freight24.connect.feature.verificationrequests.domain.usecase

import com.freight24.connect.repository.verificationrequests.VerificationRequestsRepository
import javax.inject.Inject

data class UpdateTruckVerificationStatusUseCaseParams(
    val id: String,
    val status: String,
    val resolutionNotes: String? = null,
)

interface UpdateTruckVerificationStatusUseCase {
    suspend operator fun invoke(params: UpdateTruckVerificationStatusUseCaseParams): Result<Unit>
}

class UpdateTruckVerificationStatusUseCaseImpl
    @Inject
    constructor(
        private val repository: VerificationRequestsRepository,
    ) : UpdateTruckVerificationStatusUseCase {
        override suspend fun invoke(params: UpdateTruckVerificationStatusUseCaseParams): Result<Unit> {
            return repository.updateTruckVerificationStatus(
                id = params.id,
                status = params.status,
                resolutionNotes = params.resolutionNotes?.trim()?.takeIf { it.isNotBlank() },
            )
        }
    }
