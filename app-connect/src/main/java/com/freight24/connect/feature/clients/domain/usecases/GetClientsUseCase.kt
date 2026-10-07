package com.freight24.connect.feature.clients.domain.usecases

import com.freight24.connect.repository.ClientRepository
import com.freight24.connect.domain.model.ClientCounts
import com.freight24.connect.domain.model.ClientManagementItem
import com.freight24.connect.domain.model.EntityPage
import javax.inject.Inject

data class GetClientsParams(
    val query: String? = null,
    val status: String? = null,
    val page: Int = 1,
    val limit: Int = 30,
)

interface GetClientsUseCase {
    suspend operator fun invoke(params: GetClientsParams = GetClientsParams()): Result<EntityPage<ClientManagementItem, ClientCounts>>
}

class GetClientsUseCaseImpl
    @Inject
    constructor(
        private val repository: ClientRepository,
    ) : GetClientsUseCase {
        override suspend fun invoke(params: GetClientsParams) =
            repository.getManagementPage(params.query, params.status, params.page, params.limit)
    }
