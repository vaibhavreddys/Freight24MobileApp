package com.freight24.connect.feature.verificationrequests.domain.usecase

import com.freight24.connect.feature.verificationrequests.domain.model.VerificationRequestStatus
import com.freight24.connect.repository.verificationrequests.VerificationRequestsRepository
import javax.inject.Inject

data class UpdateVerificationRequestStatusUseCaseParams(
    val id: String,
    val status: VerificationRequestStatus,
    val resolutionNotes: String?,
)

interface UpdateVerificationRequestStatusUseCase {
    suspend operator fun invoke(params: UpdateVerificationRequestStatusUseCaseParams): Result<Unit>
}

class UpdateVerificationRequestStatusUseCaseImpl @Inject constructor(
    private val repository: VerificationRequestsRepository,
) : UpdateVerificationRequestStatusUseCase {
    override suspend fun invoke(params: UpdateVerificationRequestStatusUseCaseParams): Result<Unit> {
        return repository.updateVerificationRequestStatus(
            id = params.id,
            status = params.status.apiValue,
            resolutionNotes = params.resolutionNotes?.trim().takeUnless { it.isNullOrBlank() },
        )
    }
}
