package com.freight24.connect.feature.truckowners.profile.domain.usecase

import com.freight24.connect.feature.truckowners.profile.domain.ProfileRepository
import javax.inject.Inject

interface GetProfileSignedUrlUseCase {
    suspend operator fun invoke(documentId: String): Result<String>
}

class GetProfileSignedUrlUseCaseImpl
    @Inject
    constructor(
        private val repository: ProfileRepository,
    ) : GetProfileSignedUrlUseCase {
        override suspend fun invoke(documentId: String): Result<String> {
            return repository.getIdentityDocumentSignedUrl(documentId)
        }
    }
