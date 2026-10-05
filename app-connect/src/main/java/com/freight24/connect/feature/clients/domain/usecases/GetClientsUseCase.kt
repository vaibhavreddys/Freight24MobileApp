package com.freight24.connect.feature.clients.domain.usecases

import com.freight24.connect.repository.ClientRepository
import com.freight24.rest.response.ClientDto
import javax.inject.Inject

interface GetClientsUseCase {
    suspend operator fun invoke(): Result<List<ClientDto>>
}

class GetClientsUseCaseImpl
    @Inject
    constructor(
        private val repository: ClientRepository,
    ) : GetClientsUseCase {
        override suspend fun invoke(): Result<List<ClientDto>> {
            return repository.getClients()
        }
    }
