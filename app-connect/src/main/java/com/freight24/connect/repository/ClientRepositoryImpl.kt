package com.freight24.connect.repository

import com.freight24.common.di.IoDispatcher
import com.freight24.network.error.toApiException
import com.freight24.rest.api.ClientV2Api
import com.freight24.rest.api.DocumentApi
import com.freight24.rest.api.ReferenceDataApi
import com.freight24.rest.response.ClientBranchV2Dto
import com.freight24.rest.response.ClientContactV2Dto
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
import com.freight24.connect.repository.mapper.toDomain
import com.freight24.connect.repository.mapper.toManagementPage
import com.freight24.connect.repository.mapper.toPickerPage
import javax.inject.Inject

class ClientRepositoryImpl
    @Inject
    constructor(
        private val v2Api: ClientV2Api,
        private val documentApi: DocumentApi,
        private val referenceDataApi: ReferenceDataApi,
        @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    ) : ClientRepository {
        override suspend fun getManagementPage(query: String?, status: String?, page: Int, limit: Int) =
            loadClientPage(query, status, page, limit) { it.toManagementPage() }

        override suspend fun searchPickerOptions(query: String, page: Int, limit: Int) =
            loadClientPage(query, "active", page, limit) { it.toPickerPage() }

        override suspend fun getDetails(clientId: String) =
            withContext(ioDispatcher) {
                try {
                    val response = v2Api.getClientDetail(clientId)
                    val body = response.body()
                    val data = body?.data
                    if (response.isSuccessful && body?.success == true && data != null) {
                        Result.success(data.toDomain())
                    } else {
                        Result.failure(response.toApiException(bodyMessage = body?.message))
                    }
                } catch (e: Exception) {
                    Result.failure(e)
                }
            }

        override suspend fun getClientDocumentSignedUrl(clientId: String, documentId: String) =
            loadSignedUrl { documentApi.getClientDocumentSignedUrl(clientId, documentId) }

        override suspend fun getBranchDocumentSignedUrl(clientId: String, branchId: String, documentId: String) =
            loadSignedUrl { documentApi.getClientBranchDocumentSignedUrl(clientId, branchId, documentId) }

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

        override suspend fun getStates() = loadList { referenceDataApi.getStates() }

        override suspend fun getCities(stateId: Int) = loadList { referenceDataApi.getCities(stateId) }

        override suspend fun getBranchTypes() = loadList { referenceDataApi.getBranchTypes() }

        override suspend fun getContactRoles() = loadList { referenceDataApi.getContactRoles() }

        override suspend fun isClientFieldRegistered(field: String, value: String): Result<Boolean> =
            withContext(ioDispatcher) {
                try {
                    val response =
                        when (field) {
                            "pan_card" -> v2Api.validateClient(panCard = value)
                            "gst_number" -> v2Api.validateClient(gstNumber = value)
                            else -> return@withContext Result.failure(IllegalArgumentException("Unsupported client field: $field"))
                        }
                    val body = response.body()
                    val validation =
                        when (field) {
                            "pan_card" -> body?.data?.pan_card
                            else -> body?.data?.gst_number
                        }
                    if (response.isSuccessful && body?.success == true && validation != null) {
                        Result.success(validation.exists)
                    } else {
                        Result.failure(response.toApiException())
                    }
                } catch (e: Exception) {
                    Result.failure(e)
                }
            }

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

        private suspend fun <T> loadClientPage(
            query: String?,
            status: String?,
            page: Int,
            limit: Int,
            mapper: (com.freight24.rest.response.ClientV2PageResponse) -> T,
        ): Result<T> =
            withContext(ioDispatcher) {
                try {
                    val response = v2Api.getClients(status = status, search = query, page = page, limit = limit)
                    val body = response.body()
                    if (response.isSuccessful && body?.success == true) {
                        Result.success(mapper(body))
                    } else {
                        Result.failure(response.toApiException())
                    }
                } catch (e: Exception) {
                    Result.failure(e)
                }
            }

        private suspend fun loadSignedUrl(
            request: suspend () -> Response<com.freight24.rest.response.SignedDocument>,
        ): Result<String> =
            withContext(ioDispatcher) {
                try {
                    val response = request()
                    val body = response.body()
                    if (response.isSuccessful && body != null) {
                        Result.success(body.data.signedUrl)
                    } else {
                        Result.failure(response.toApiException())
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
