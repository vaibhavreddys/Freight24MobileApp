package com.freight24.trucker.feature.profile.domain.usecase

import com.freight24.common.usecase.EmptyUseCaseParams
import com.freight24.trucker.feature.profile.domain.Profile
import com.freight24.trucker.feature.profile.domain.ProfileRepository
import com.freight24.trucker.feature.profile.mapper.toDomain
import javax.inject.Inject

internal class FetchProfileUseCaseImpl @Inject constructor(
    private val repository: ProfileRepository
) : FetchProfileUseCase {
    override suspend fun invoke(params: EmptyUseCaseParams): Result<Profile> {
        return repository.getProfile()
    }
}
