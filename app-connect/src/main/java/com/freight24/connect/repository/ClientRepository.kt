package com.freight24.connect.repository

import com.freight24.rest.request.UpdateClientRequest
import com.freight24.rest.response.ClientBranchV2Dto
import com.freight24.rest.response.ClientContactV2Dto
import com.freight24.rest.response.ClientDto
import com.freight24.rest.response.ClientProfileDto

interface ClientRepository {
    suspend fun getClients(): Result<List<ClientDto>>

    suspend fun searchClients(query: String): Result<List<ClientDto>>

    suspend fun getClientByUserId(userId: String): Result<ClientProfileDto>

    suspend fun createClient(request: UpdateClientRequest): Result<Boolean>

    suspend fun updateClient(
        userId: String,
        request: UpdateClientRequest,
    ): Result<ClientProfileDto>

    /**
     * Branches of a client, from `/api/v2/client/:clientId/branches`.
     * Returns active and inactive alike; callers filter on [ClientBranchV2Dto.status].
     */
    suspend fun getClientBranches(clientId: String): Result<List<ClientBranchV2Dto>>

    /**
     * Active contacts of a client, from `/api/v2/client/:clientId/contacts`.
     * These are the records an indent's contact roles point at.
     */
    suspend fun getClientContacts(clientId: String): Result<List<ClientContactV2Dto>>
}
