package com.freight24.connect.repository

import com.freight24.common.di.IoDispatcher
import com.freight24.rest.api.AdhocTruckApi
import com.freight24.rest.response.AdhocTruckOwnerDto
import com.freight24.rest.request.CreateAdhocTruckRequest
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AdhocTruckRepositoryImpl @Inject constructor(
    private val api: AdhocTruckApi,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : AdhocTruckRepository {

    override suspend fun searchTruckOwnersByPhone(phone: String): Result<List<AdhocTruckOwnerDto>> =
        withContext(ioDispatcher) {
            try {
                val response = api.searchTruckOwnersByPhone(phone)
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    if (body.success) {
                        Result.success(body.data)
                    } else {
                        Result.failure(Exception("Search failed"))
                    }
                } else {
                    Result.failure(Exception(response.message()))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun createAdhocTruckEntry(request: CreateAdhocTruckRequest): Result<Unit> =
        withContext(ioDispatcher) {
            try {
                val response = api.createAdhocTruckEntry(request)
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    if (body.success) {
                        Result.success(Unit)
                    } else {
                        Result.failure(Exception(body.message))
                    }
                } else {
                    Result.failure(Exception(response.message()))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
}
