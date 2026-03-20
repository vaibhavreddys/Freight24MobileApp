package com.freight24.connect.feature.truckowners.profile.domain.usecase

import com.freight24.connect.feature.truckowners.profile.domain.ProfileRepository
import com.freight24.rest.request.FinalSaveRequest
import javax.inject.Inject

interface FinalizeIdentityDocumentsUseCase {
    suspend operator fun invoke(request: FinalSaveRequest): Result<Boolean>
}

class FinalizeIdentityDocumentsUseCaseImpl
    @Inject
    constructor(
        private val repository: ProfileRepository,
    ) : FinalizeIdentityDocumentsUseCase {
        override suspend fun invoke(request: FinalSaveRequest): Result<Boolean> {
            return repository.finalizeIdentityDocuments(request)
        }
    }
