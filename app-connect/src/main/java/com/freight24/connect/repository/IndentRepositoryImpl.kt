package com.freight24.connect.repository

import com.freight24.common.di.IoDispatcher
import com.freight24.rest.api.IndentsApi
import com.freight24.rest.request.CloneIndentRequest
import com.freight24.rest.request.CreateIndentRequest
import com.freight24.rest.request.UpdateIndentGroupRequest
import com.freight24.rest.request.UpdateIndentRequest
import com.freight24.rest.request.IndentLifecycleRequest
import com.freight24.network.error.toApiException
import com.freight24.rest.response.GetIndentsResponse
import com.freight24.rest.response.IndentResponse
import com.freight24.rest.response.MaterialDto
import com.freight24.rest.response.TruckCategoryDto
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class IndentRepositoryImpl
    @Inject
    constructor(
        private val api: IndentsApi,
        @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    ) : IndentRepository {
        override suspend fun getTruckTypes(): Result<TruckCategoryDto> =
            withContext(ioDispatcher) {
                try {
                    val response = api.getTruckTypes()
                    if (response.isSuccessful && response.body() != null) {
                        Result.success(response.body()!!.data)
                    } else {
                        Result.failure(Exception(response.message()))
                    }
                } catch (e: Exception) {
                    Result.failure(e)
                }
            }

        override suspend fun createIndent(request: CreateIndentRequest): Result<IndentResponse> =
            withContext(ioDispatcher) {
                try {
                    val response = api.createIndent(request)
                    if (response.isSuccessful && response.body() != null) {
                        Result.success(response.body()!!)
                    } else {
                        Result.failure(Exception(response.message()))
                    }
                } catch (e: Exception) {
                    Result.failure(e)
                }
            }

        override suspend fun getMyIndents(
            status: String,
            page: Int?,
            limit: Int?,
        ): Result<GetIndentsResponse> =
            withContext(ioDispatcher) {
                try {
                    val response = api.getMyIndents(status, page, limit)
                    if (response.isSuccessful && response.body() != null) {
                        Result.success(response.body()!!)
                    } else {
                        Result.failure(Exception(response.message()))
                    }
                } catch (e: Exception) {
                    Result.failure(e)
                }
            }

        override suspend fun getIndentById(id: String): Result<IndentResponse> =
            withContext(ioDispatcher) {
                try {
                    val response = api.getIndentById(id)
                    if (response.isSuccessful && response.body() != null) {
                        Result.success(response.body()!!)
                    } else {
                        Result.failure(Exception(response.message()))
                    }
                } catch (e: Exception) {
                    Result.failure(e)
                }
            }

        override suspend fun updateIndent(
            id: String,
            request: UpdateIndentRequest,
        ): Result<IndentResponse> =
            withContext(ioDispatcher) {
                try {
                    val response = api.updateIndent(id, request)
                    if (response.isSuccessful && response.body() != null) {
                        Result.success(response.body()!!)
                    } else {
                        Result.failure(Exception(response.message()))
                    }
                } catch (e: Exception) {
                    Result.failure(e)
                }
            }

        override suspend fun cancelIndent(
            id: String,
            request: IndentLifecycleRequest,
        ): Result<Unit> = executeLifecycleRequest { api.cancelIndent(id, request) }

        override suspend fun reopenIndent(
            id: String,
            request: IndentLifecycleRequest,
        ): Result<Unit> = executeLifecycleRequest { api.reopenIndent(id, request) }

        private suspend fun executeLifecycleRequest(
            request: suspend () -> retrofit2.Response<com.freight24.rest.response.BaseResponse>,
        ): Result<Unit> =
            withContext(ioDispatcher) {
                try {
                    val response = request()
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

        override suspend fun updateIndentGroup(
            groupId: String,
            request: UpdateIndentGroupRequest,
        ): Result<Unit> =
            withContext(ioDispatcher) {
                try {
                    val response = api.updateIndentGroup(groupId, request)
                    if (response.isSuccessful) {
                        Result.success(Unit)
                    } else {
                        Result.failure(Exception(response.message()))
                    }
                } catch (e: Exception) {
                    Result.failure(e)
                }
            }

        override suspend fun getMaterials(): Result<List<MaterialDto>> =
            withContext(ioDispatcher) {
                try {
                    val response = api.getMaterials()
                    if (response.isSuccessful && response.body() != null) {
                        Result.success(response.body()!!.data)
                    } else {
                        Result.failure(Exception(response.message()))
                    }
                } catch (e: Exception) {
                    Result.failure(e)
                }
            }

        override suspend fun getAllIndents(
            page: Int,
            limit: Int,
            status: String?,
        ): Result<GetIndentsResponse> =
            withContext(ioDispatcher) {
                try {
                    val response = api.getAllIndents(page, limit, status)
                    if (response.isSuccessful && response.body() != null) {
                        Result.success(response.body()!!)
                    } else {
                        Result.failure(Exception(response.message()))
                    }
                } catch (e: Exception) {
                    Result.failure(e)
                }
            }

        override suspend fun getInactiveIndents(
            page: Int,
            limit: Int,
        ): Result<GetIndentsResponse> =
            withContext(ioDispatcher) {
                try {
                    val response = api.getInactiveIndents(page, limit)
                    if (response.isSuccessful && response.body() != null) {
                        Result.success(response.body()!!)
                    } else {
                        Result.failure(Exception(response.message()))
                    }
                } catch (e: Exception) {
                    Result.failure(e)
                }
            }

        override suspend fun cloneIndent(
            indentGroupId: String,
            assignedTo: String?,
            notes: String?,
        ): Result<IndentResponse> =
            withContext(ioDispatcher) {
                try {
                    val response =
                        api.cloneIndent(
                            indentGroupId,
                            CloneIndentRequest(
                                assignedTo,
                                notes,
                            ),
                        )
                    if (response.isSuccessful && response.body() != null) {
                        Result.success(response.body()!!)
                    } else {
                        Result.failure(Exception(response.message()))
                    }
                } catch (e: Exception) {
                    Result.failure(e)
                }
            }

        override suspend fun searchIndents(
            query: String,
            priority: String?,
            clientId: String?,
            assignedToMe: Boolean?,
        ): Result<GetIndentsResponse> =
            withContext(ioDispatcher) {
                try {
                    val response =
                        api.searchIndents(
                            query,
                            priority,
                            clientId,
                            assignedToMe,
                        )
                    if (response.isSuccessful && response.body() != null) {
                        Result.success(response.body()!!)
                    } else {
                        Result.failure(Exception(response.message()))
                    }
                } catch (e: Exception) {
                    Result.failure(e)
                }
            }
    }
