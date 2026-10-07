package com.freight24.connect.feature.clients.domain.usecases

import com.freight24.connect.domain.model.ClientDetails
import com.freight24.connect.repository.ClientRepository
import javax.inject.Inject

interface GetClientDetailsUseCase {
    suspend operator fun invoke(clientId: String): Result<ClientDetails>
}

class GetClientDetailsUseCaseImpl
    @Inject
    constructor(
        private val repository: ClientRepository,
    ) : GetClientDetailsUseCase {
        override suspend fun invoke(clientId: String): Result<ClientDetails> =
            repository.getDetails(clientId)
    }
