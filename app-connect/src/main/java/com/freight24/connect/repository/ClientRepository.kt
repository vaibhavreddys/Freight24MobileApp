package com.freight24.connect.repository

import com.freight24.rest.request.UpdateClientRequest
import com.freight24.rest.response.ClientDto
import com.freight24.rest.response.ClientProfileDto

interface ClientRepository {
    suspend fun getClients(): Result<List<ClientDto>>
    suspend fun searchClients(query: String): Result<List<ClientDto>>
    suspend fun getClientByUserId(userId: String): Result<ClientProfileDto>
    suspend fun createClient(request: UpdateClientRequest): Result<Boolean>
    suspend fun updateClient(userId: String, request: UpdateClientRequest): Result<ClientProfileDto>
}
