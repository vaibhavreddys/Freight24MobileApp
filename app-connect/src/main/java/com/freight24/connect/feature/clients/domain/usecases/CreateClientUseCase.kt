package com.freight24.connect.feature.clients.domain.usecases

import com.freight24.connect.repository.ClientRepository
import com.freight24.rest.request.UpdateClientRequest
import javax.inject.Inject

data class CreateClientParams(
    val request: UpdateClientRequest,
)

interface CreateClientUseCase {
    suspend operator fun invoke(params: CreateClientParams): Result<Boolean>
}

class CreateClientUseCaseImpl @Inject constructor(
    private val repository: ClientRepository,
) : CreateClientUseCase {
    override suspend fun invoke(params: CreateClientParams): Result<Boolean> {
        return repository.createClient(params.request)
    }
}
