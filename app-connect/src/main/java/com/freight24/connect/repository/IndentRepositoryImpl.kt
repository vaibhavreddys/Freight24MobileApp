package com.freight24.connect.repository

import com.freight24.common.di.IoDispatcher
import com.freight24.rest.api.IndentsApi
import com.freight24.rest.request.CreateIndentRequest
import com.freight24.rest.response.BaseResponse
import com.freight24.rest.response.ClientDto
import com.freight24.rest.response.LocationDto
import com.freight24.rest.response.TruckTypeDto
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class IndentRepositoryImpl @Inject constructor(
    private val api: IndentsApi,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : IndentRepository {

    override suspend fun searchLocations(query: String): Result<List<LocationDto>> = withContext(ioDispatcher) {
        try {
            val response = api.searchLocations(query)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.message()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun searchClients(query: String): Result<List<ClientDto>> = withContext(ioDispatcher) {
        try {
            val response = api.searchClients(query)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.message()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getTruckTypes(): Result<List<TruckTypeDto>> = withContext(ioDispatcher) {
        try {
            val response = api.getTruckTypes()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.message()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createIndent(request: CreateIndentRequest): Result<BaseResponse> = withContext(ioDispatcher) {
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
}
