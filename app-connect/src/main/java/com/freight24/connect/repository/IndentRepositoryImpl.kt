package com.freight24.connect.repository

import com.freight24.common.di.IoDispatcher
import com.freight24.rest.api.IndentsApi
import com.freight24.rest.request.CloneIndentRequest
import com.freight24.rest.request.CreateIndentRequest
import com.freight24.rest.request.UpdateIndentRequest
import com.freight24.rest.response.GetIndentsResponse
import com.freight24.rest.response.IndentResponse
import com.freight24.rest.response.IndentsListResponse
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
        limit: Int?
    ): Result<GetIndentsResponse> =
        withContext(ioDispatcher) {
            try {
                val response = api.getMyIndents(status)
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
    override suspend fun updateIndent(id: String, request: UpdateIndentRequest): Result<IndentResponse> =
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

    override suspend fun getAllIndents(
        page: Int,
        limit: Int
    ): Result<GetIndentsResponse> =
        withContext(ioDispatcher) {
            try {
                val response = api.getAllIndents(page, limit)
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
        notes: String?
    ): Result<IndentResponse> =
        withContext(ioDispatcher) {
            try {
                val response = api.cloneIndent(
                    indentGroupId,
                    CloneIndentRequest(
                        assignedTo,
                        notes
                    )
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
        assignedToMe: Boolean?
    ): Result<GetIndentsResponse> =
        withContext(ioDispatcher) {
            try {
                val response = api.searchIndents(
                    query,
                    priority,
                    clientId,
                    assignedToMe
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
