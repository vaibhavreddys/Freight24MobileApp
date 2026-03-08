package com.freight24.connect.repository

import com.freight24.rest.response.ClientDto

interface ClientRepository {
    suspend fun getClients(): Result<List<ClientDto>>
    suspend fun searchClients(query: String): Result<List<ClientDto>>
}
