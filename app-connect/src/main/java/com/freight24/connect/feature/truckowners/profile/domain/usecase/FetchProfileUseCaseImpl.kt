package com.freight24.connect.feature.truckowners.profile.domain.usecase

import com.freight24.connect.feature.truckowners.profile.domain.Profile
import com.freight24.connect.feature.truckowners.profile.domain.ProfileRepository
import javax.inject.Inject

internal class FetchProfileUseCaseImpl
    @Inject
    constructor(
        private val repository: ProfileRepository,
    ) : FetchProfileUseCase {
        override suspend fun invoke(params: FetchProfileParams): Result<Profile> {
            return repository.getProfile(params.userId)
        }
    }
