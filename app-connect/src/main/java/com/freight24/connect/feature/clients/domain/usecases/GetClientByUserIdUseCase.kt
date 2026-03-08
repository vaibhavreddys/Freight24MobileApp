package com.freight24.connect.feature.clients.domain.usecases

import com.freight24.connect.repository.ClientRepository
import com.freight24.rest.response.ClientProfileDto
import javax.inject.Inject

interface GetClientByUserIdUseCase {
    suspend operator fun invoke(userId: String): Result<ClientProfileDto>
}

class GetClientByUserIdUseCaseImpl @Inject constructor(
    private val repository: ClientRepository,
) : GetClientByUserIdUseCase {
    override suspend fun invoke(userId: String): Result<ClientProfileDto> {
        return repository.getClientByUserId(userId)
    }
}
