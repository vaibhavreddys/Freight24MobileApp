package com.freight24.connect.repository

import com.freight24.common.di.IoDispatcher
import com.freight24.rest.api.TruckOwnerApi
import com.freight24.rest.response.TruckOwnerDto
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class TruckOwnerRepositoryImpl @Inject constructor(
    private val api: TruckOwnerApi,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : TruckOwnerRepository {

    override suspend fun getTruckOwners(): Result<List<TruckOwnerDto>> =
        withContext(ioDispatcher) {
            try {
                val response = api.getTruckOwners()
                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!.data)
                } else {
                    Result.failure(Exception(response.message()))
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
                    Result.failure(Exception(response.message()))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
}
