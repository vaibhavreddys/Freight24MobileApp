package com.freight24.connect.feature.clients.domain.usecases

import com.freight24.connect.repository.ClientRepository
import com.freight24.rest.request.UpdateClientRequest
import com.freight24.rest.response.ClientProfileDto
import javax.inject.Inject

data class UpdateClientParams(
    val clientUserId: String,
    val request: UpdateClientRequest,
)

interface UpdateClientUseCase {
    suspend operator fun invoke(params: UpdateClientParams): Result<ClientProfileDto>
}

class UpdateClientUseCaseImpl
    @Inject
    constructor(
        private val repository: ClientRepository,
    ) : UpdateClientUseCase {
        override suspend fun invoke(params: UpdateClientParams): Result<ClientProfileDto> {
            return repository.updateClient(
                clientUserId = params.clientUserId,
                request = params.request,
            )
        }
    }
