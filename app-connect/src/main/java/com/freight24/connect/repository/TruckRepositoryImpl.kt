package com.freight24.connect.repository

import com.freight24.common.di.IoDispatcher
import com.freight24.rest.api.TruckApi
import com.freight24.rest.response.SearchTruckResponse
import com.freight24.rest.response.TruckDetailResponse
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class TruckRepositoryImpl
@Inject
constructor(
    private val api: TruckApi,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : TruckRepository {

    override suspend fun searchTrucks(query: String): Result<List<SearchTruckResponse.Truck>> =
        withContext(ioDispatcher) {
            try {
                val response = api.searchTrucks(query)
                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!.data)
                } else {
                    Result.failure(Exception(response.message()))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }

        }

    override suspend fun getTruckDetail(id: String): Result<TruckDetailResponse> =
        withContext(ioDispatcher) {
             try {
                val response = api.getTruckDetail(id)
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