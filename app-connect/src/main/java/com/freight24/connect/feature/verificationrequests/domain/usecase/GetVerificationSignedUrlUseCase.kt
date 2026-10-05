package com.freight24.connect.feature.verificationrequests.domain.usecase

import com.freight24.connect.repository.verificationrequests.VerificationRequestsRepository
import javax.inject.Inject

interface GetVerificationSignedUrlUseCase {
    suspend operator fun invoke(
        entity: String,
        requestId: String,
        documentKey: String,
    ): Result<String>
}

class GetVerificationSignedUrlUseCaseImpl
    @Inject
    constructor(
        private val repository: VerificationRequestsRepository,
    ) : GetVerificationSignedUrlUseCase {
        override suspend fun invoke(
            entity: String,
            requestId: String,
            documentKey: String,
        ): Result<String> {
            return repository.getSignedUrl(entity, requestId, documentKey)
        }
    }
