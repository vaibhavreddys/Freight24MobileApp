package com.freight24.connect.repository

import com.freight24.common.di.IoDispatcher
import com.freight24.rest.api.ClientApi
import com.freight24.rest.response.ClientDto
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ClientRepositoryImpl
    @Inject
    constructor(
        private val api: ClientApi,
        @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    ) : ClientRepository {
        override suspend fun getClients(): Result<List<ClientDto>> =
            withContext(ioDispatcher) {
                try {
                    val response = api.getClientList()
                    if (response.isSuccessful && response.body() != null) {
                        Result.success(response.body()!!.data)
                    } else {
                        Result.failure(Exception(response.message()))
                    }
                } catch (e: Exception) {
                    Result.failure(e)
                }
            }

        override suspend fun searchClients(query: String): Result<List<ClientDto>> =
            withContext(ioDispatcher) {
                try {
                    val response = api.searchClients(query)
                    if (response.isSuccessful && response.body() != null) {
                        Result.success(response.body()!!.data)
                    } else {
                        Result.failure(Exception(response.message()))
                    }
                } catch (e: Exception) {
                    Result.failure(e)
                }
            }
    }
