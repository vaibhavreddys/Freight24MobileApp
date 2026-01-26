package com.freight24.connect.feature.indents.domain

import com.freight24.connect.repository.IndentRepository
import com.freight24.rest.response.ClientDto
import javax.inject.Inject

interface GetClientsUseCase {
    suspend fun invoke(query: String): Result<List<ClientDto>>
}

class GetClientsUseCaseImpl @Inject constructor(
    private val repository: IndentRepository
) : GetClientsUseCase {
    override suspend fun invoke(query: String): Result<List<ClientDto>> {
        return repository.searchClients(query)
    }
}
