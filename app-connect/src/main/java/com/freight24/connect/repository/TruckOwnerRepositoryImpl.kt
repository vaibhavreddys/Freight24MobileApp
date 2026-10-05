package com.freight24.connect.repository

import com.freight24.common.di.IoDispatcher
import com.freight24.rest.api.TruckOwnerApi
import com.freight24.rest.request.UpdateProfileRequest
import com.freight24.rest.response.TruckOwnerDto
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.json.JSONObject
import retrofit2.Response
import javax.inject.Inject

class TruckOwnerRepositoryImpl
    @Inject
    constructor(
        private val api: TruckOwnerApi,
        @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    ) : TruckOwnerRepository {
        override suspend fun getTruckOwners(): Result<List<TruckOwnerDto>> =
            withContext(ioDispatcher) {
                try {
                    val response = api.getTruckOwners()
                    if (response.isSuccessful && response.body() != null) {
                        Result.success(response.body()!!.data)
                    } else {
                        Result.failure(Exception(extractErrorMessage(response, response.body()?.message)))
                    }
                } catch (e: Exception) {
                    Result.failure(e)
                }
            }

        override suspend fun searchTruckOwners(query: String): Result<List<TruckOwnerDto>> =
            withContext(ioDispatcher) {
                try {
                    val response = api.searchTruckOwners(query)
                    if (response.isSuccessful && response.body() != null) {
                        Result.success(response.body()!!.data)
                    } else {
                        Result.failure(Exception(extractErrorMessage(response, response.body()?.message)))
                    }
                } catch (e: Exception) {
                    Result.failure(e)
                }
            }

        override suspend fun createTruckOwner(request: UpdateProfileRequest): Result<Boolean> =
            withContext(ioDispatcher) {
                try {
                    val response = api.createTruckOwner(request)
                    if (response.isSuccessful && response.body() != null) {
                        Result.success(response.body()!!.success)
                    } else {
                        Result.failure(Exception(extractErrorMessage(response, response.body()?.message)))
                    }
                } catch (e: Exception) {
                    Result.failure(e)
                }
            }

        private fun extractErrorMessage(
            response: Response<*>,
            bodyMessage: String? = null,
        ): String {
            bodyMessage?.takeIf { it.isNotBlank() }?.let { return it }

            val rawError = response.errorBody()?.string().orEmpty()
            if (rawError.isNotBlank()) {
                runCatching { JSONObject(rawError).optString("message") }
                    .getOrNull()
                    ?.takeIf { it.isNotBlank() }
                    ?.let { return it }
            }

            return response.message().ifBlank { "Request failed" }
        }
    }
