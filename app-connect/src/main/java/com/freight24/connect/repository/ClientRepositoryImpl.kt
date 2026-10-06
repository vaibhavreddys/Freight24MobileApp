package com.freight24.connect.repository

import com.freight24.common.di.IoDispatcher
import com.freight24.network.error.toApiException
import com.freight24.rest.api.ClientApi
import com.freight24.rest.api.ClientV2Api
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
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import retrofit2.Response
import javax.inject.Inject

class ClientRepositoryImpl
    @Inject
    constructor(
        private val api: ClientApi,
        private val v2Api: ClientV2Api,
        @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    ) : ClientRepository {
        override suspend fun getClients(): Result<List<ClientDto>> =
            withContext(ioDispatcher) {
                try {
                    val response = api.getClientList()
                    if (response.isSuccessful && response.body() != null) {
                        Result.success(response.body()!!.data)
                    } else {
                        Result.failure(response.toApiException())
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
                        Result.failure(response.toApiException())
                    }
                } catch (e: Exception) {
                    Result.failure(e)
                }
            }

        override suspend fun getClientByUserId(clientUserId: String): Result<ClientProfileDto> =
            withContext(ioDispatcher) {
                try {
                    val response = api.getClientByUserId(clientUserId)
                    val body = response.body()
                    if (response.isSuccessful && body != null) {
                        Result.success(body.data)
                    } else {
                        Result.failure(response.toApiException(bodyMessage = body?.message))
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
                        Result.failure(response.toApiException(bodyMessage = response.body()?.message))
                    }
                } catch (e: Exception) {
                    Result.failure(e)
                }
            }

        override suspend fun createClientV2(parts: List<MultipartBody.Part>) =
            execute { v2Api.createClient(parts) }

        override suspend fun saveClientLocations(
            clientId: String,
            parts: List<MultipartBody.Part>,
        ) = execute { v2Api.saveLocations(clientId, parts) }

        override suspend fun createClientContact(
            clientId: String,
            request: ClientContactCreateRequest,
        ): Result<Unit> =
            withContext(ioDispatcher) {
                try {
                    val response = v2Api.createContact(clientId, request)
                    val body = response.body()
                    if (response.isSuccessful && body?.success == true) {
                        Result.success(Unit)
                    } else {
                        Result.failure(response.toApiException(bodyMessage = body?.message))
                    }
                } catch (e: Exception) {
                    Result.failure(e)
                }
            }

        override suspend fun getStates() = loadList { v2Api.getStates() }

        override suspend fun getCities(stateId: Int) = loadList { v2Api.getCities(stateId) }

        override suspend fun getBranchTypes() = loadList { v2Api.getBranchTypes() }

        override suspend fun getContactRoles() = loadList { v2Api.getContactRoles() }

        private suspend fun execute(
            request: suspend () -> Response<ClientOnboardingV2Response>,
        ): Result<ClientOnboardingV2Response> =
            withContext(ioDispatcher) {
                try {
                    val response = request()
                    val body = response.body()
                    if (response.isSuccessful && body?.success == true) {
                        Result.success(body)
                    } else {
                        Result.failure(response.toApiException(bodyMessage = body?.message))
                    }
                } catch (e: Exception) {
                    Result.failure(e)
                }
            }

        private suspend fun <T> loadList(
            request: suspend () -> Response<com.freight24.rest.response.MasterDataListResponse<T>>,
        ): Result<List<T>> =
            withContext(ioDispatcher) {
                try {
                    val response = request()
                    val body = response.body()
                    if (response.isSuccessful && body?.success == true) {
                        Result.success(body.data)
                    } else {
                        Result.failure(response.toApiException())
                    }
                } catch (e: Exception) {
                    Result.failure(e)
                }
            }

        override suspend fun updateClient(
            clientUserId: String,
            request: UpdateClientRequest,
        ): Result<ClientProfileDto> =
            withContext(ioDispatcher) {
                try {
                    val response = api.updateClient(clientUserId, request)
                    val body = response.body()
                    if (response.isSuccessful && body != null) {
                        Result.success(body.data)
                    } else {
                        Result.failure(response.toApiException(bodyMessage = body?.message))
                    }
                } catch (e: Exception) {
                    Result.failure(e)
                }
            }

        override suspend fun getClientBranches(clientId: String): Result<List<ClientBranchV2Dto>> =
            withContext(ioDispatcher) {
                try {
                    val response = v2Api.getClientBranches(clientId)
                    val body = response.body()
                    if (response.isSuccessful && body != null) {
                        Result.success(body.data?.branches.orEmpty())
                    } else {
                        Result.failure(response.toApiException(bodyMessage = body?.message))
                    }
                } catch (e: Exception) {
                    Result.failure(e)
                }
            }

        override suspend fun getClientContacts(clientId: String): Result<List<ClientContactV2Dto>> =
            withContext(ioDispatcher) {
                try {
                    val response = v2Api.getClientContacts(clientId)
                    val body = response.body()
                    if (response.isSuccessful && body != null) {
                        Result.success(body.data?.data.orEmpty())
                    } else {
                        Result.failure(response.toApiException(bodyMessage = body?.message))
                    }
                } catch (e: Exception) {
                    Result.failure(e)
                }
            }

    }
