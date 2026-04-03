package com.freight24.connect.feature.verificationrequests.domain.usecase

import com.freight24.connect.repository.verificationrequests.VerificationRequestsRepository
import javax.inject.Inject

interface GetVerificationSignedUrlUseCase {
    suspend operator fun invoke(s3Url: String): Result<String>
}

class GetVerificationSignedUrlUseCaseImpl @Inject constructor(
    private val repository: VerificationRequestsRepository,
) : GetVerificationSignedUrlUseCase {
    override suspend fun invoke(s3Url: String): Result<String> {
        return repository.getSignedUrl(s3Url)
    }
}
