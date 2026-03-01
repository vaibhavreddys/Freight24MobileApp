package com.freight24.trucker.feature.profile.domain.usecase

import com.freight24.trucker.feature.profile.domain.ProfileRepository
import javax.inject.Inject

interface GetProfileSignedUrlUseCase {
    suspend operator fun invoke(s3Url: String): Result<String>
}

class GetProfileSignedUrlUseCaseImpl @Inject constructor(
    private val repository: ProfileRepository
) : GetProfileSignedUrlUseCase {
    override suspend fun invoke(s3Url: String): Result<String> {
        return repository.getGenericSignedUrl(s3Url)
    }
}
