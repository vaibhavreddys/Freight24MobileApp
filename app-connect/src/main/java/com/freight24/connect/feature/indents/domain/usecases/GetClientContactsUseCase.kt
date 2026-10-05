package com.freight24.connect.feature.indents.domain.usecases

import com.freight24.connect.repository.ClientRepository
import com.freight24.rest.response.ClientContactV2Dto
import javax.inject.Inject

/**
 * Loads the selected client's active contacts. These back the four contact
 * roles on an indent (placement / payment / operations / POD).
 */
interface GetClientContactsUseCase {
    suspend fun invoke(clientId: String): Result<List<ClientContactV2Dto>>
}

class GetClientContactsUseCaseImpl
    @Inject
    constructor(
        private val repository: ClientRepository,
    ) : GetClientContactsUseCase {
        override suspend fun invoke(clientId: String): Result<List<ClientContactV2Dto>> = repository.getClientContacts(clientId)
    }
