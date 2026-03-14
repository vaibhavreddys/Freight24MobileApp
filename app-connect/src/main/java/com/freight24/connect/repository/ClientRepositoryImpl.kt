package com.freight24.connect.repository

import com.freight24.common.di.IoDispatcher
import com.freight24.rest.api.ClientApi
import com.freight24.rest.request.UpdateClientRequest
import com.freight24.rest.response.ClientDto
import com.freight24.rest.response.ClientProfileDto
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.json.JSONObject
import retrofit2.Response
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
                        Result.failure(Exception(extractErrorMessage(response)))
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
                        Result.failure(Exception(extractErrorMessage(response)))
                    }
                } catch (e: Exception) {
                    Result.failure(e)
                }
            }

        override suspend fun getClientByUserId(userId: String): Result<ClientProfileDto> =
            withContext(ioDispatcher) {
                try {
                    val response = api.getClientByUserId(userId)
                    val body = response.body()
                    if (response.isSuccessful && body != null) {
                        Result.success(body.data)
                    } else {
                        Result.failure(Exception(extractErrorMessage(response, body?.message)))
                    }
                } catch (e: Exception) {
                    Result.failure(e)
                }
            }

        override suspend fun createClient(request: UpdateClientRequest): Result<Boolean> =
            withContext(ioDispatcher) {
                try {
                    val response = api.createClient(request)
                    if (response.isSuccessful && response.body() != null) {
                        Result.success(true)
                    } else {
                        Result.failure(Exception(extractErrorMessage(response, response.body()?.message)))
                    }
                } catch (e: Exception) {
                    Result.failure(e)
                }
            }

        override suspend fun updateClient(
            userId: String,
            request: UpdateClientRequest,
        ): Result<ClientProfileDto> =
            withContext(ioDispatcher) {
                try {
                    val response = api.updateClient(userId, request)
                    val body = response.body()
                    if (response.isSuccessful && body != null) {
                        Result.success(body.data)
                    } else {
                        Result.failure(Exception(extractErrorMessage(response, body?.message)))
                    }
                } catch (e: Exception) {
                    Result.failure(e)
                }
            }

    private fun extractErrorMessage(response: Response<*>, bodyMessage: String? = null): String {
        bodyMessage?.takeIf { it.isNotBlank() }?.let { return it }

        val rawError = response.errorBody()?.string().orEmpty()
        if (rawError.isNotBlank()) {
            runCatching {
                JSONObject(rawError).optString("message")
            }.getOrNull()?.takeIf { it.isNotBlank() }?.let { return it }
        }

        return response.message().ifBlank { "Request failed" }
    }
    }
