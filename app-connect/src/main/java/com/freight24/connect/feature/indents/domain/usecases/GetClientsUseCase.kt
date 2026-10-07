package com.freight24.connect.feature.indents.domain.usecases

import com.freight24.connect.repository.ClientRepository
import com.freight24.connect.domain.model.ClientPickerOption
import javax.inject.Inject

interface GetClientsUseCase {
    suspend fun invoke(query: String): Result<List<ClientPickerOption>>
}

class GetClientsUseCaseImpl
    @Inject
    constructor(
        private val repository: ClientRepository,
    ) : GetClientsUseCase {
        override suspend fun invoke(query: String): Result<List<ClientPickerOption>> {
            return repository.searchPickerOptions(query).map { it.items }
        }
    }
