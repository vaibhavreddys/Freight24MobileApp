package com.freight24.trucker.feature.profile.domain.usecase

import com.freight24.rest.request.UpdateProfileRequest
import com.freight24.trucker.feature.profile.domain.ProfileRepository
import javax.inject.Inject

interface UpdateProfileUseCase {
    suspend operator fun invoke(params: UpdateProfileParams): Result<Boolean>
}

data class UpdateProfileParams(
    val id: String,
    val request: UpdateProfileRequest
)

class UpdateProfileUseCaseImpl @Inject constructor(
    private val repository: ProfileRepository
) : UpdateProfileUseCase {
    override suspend fun invoke(params: UpdateProfileParams): Result<Boolean> {
        return repository.updateProfile(params.id, params.request)
    }
}
