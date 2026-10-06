package com.freight24.connect.repository

import com.freight24.rest.request.UpdateClientRequest
import com.freight24.rest.response.ClientBranchV2Dto
import com.freight24.rest.response.ClientContactV2Dto
import com.freight24.rest.response.ClientDto
import com.freight24.rest.response.ClientProfileDto
import com.freight24.rest.response.BranchTypeOptionDto
import com.freight24.rest.response.CityOptionDto
import com.freight24.rest.response.ClientContactCreateRequest
import com.freight24.rest.response.ClientOnboardingV2Response
import com.freight24.rest.response.ContactRoleOptionDto
import com.freight24.rest.response.StateOptionDto
import okhttp3.MultipartBody

interface ClientRepository {
    suspend fun getClients(): Result<List<ClientDto>>

    suspend fun searchClients(query: String): Result<List<ClientDto>>

    suspend fun getClientByUserId(clientUserId: String): Result<ClientProfileDto>

    suspend fun createClient(request: UpdateClientRequest): Result<Boolean>

    suspend fun createClientV2(parts: List<MultipartBody.Part>): Result<ClientOnboardingV2Response>

    suspend fun saveClientLocations(
        clientId: String,
        parts: List<MultipartBody.Part>,
    ): Result<ClientOnboardingV2Response>

    suspend fun createClientContact(
        clientId: String,
        request: ClientContactCreateRequest,
    ): Result<Unit>

    suspend fun getStates(): Result<List<StateOptionDto>>

    suspend fun getCities(stateId: Int): Result<List<CityOptionDto>>

    suspend fun getBranchTypes(): Result<List<BranchTypeOptionDto>>

    suspend fun getContactRoles(): Result<List<ContactRoleOptionDto>>

    suspend fun updateClient(
        clientUserId: String,
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
