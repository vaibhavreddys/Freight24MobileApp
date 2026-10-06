package com.freight24.connect.feature.clients.domain.usecases

import com.freight24.connect.repository.ClientRepository
import com.freight24.rest.response.ClientProfileDto
import javax.inject.Inject

interface GetClientByUserIdUseCase {
    suspend operator fun invoke(clientUserId: String): Result<ClientProfileDto>
}

class GetClientByUserIdUseCaseImpl
    @Inject
    constructor(
        private val repository: ClientRepository,
    ) : GetClientByUserIdUseCase {
        override suspend fun invoke(clientUserId: String): Result<ClientProfileDto> {
            return repository.getClientByUserId(clientUserId)
        }
    }
