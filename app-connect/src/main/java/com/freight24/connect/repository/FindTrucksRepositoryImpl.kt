package com.freight24.connect.repository

import com.freight24.common.di.IoDispatcher
import com.freight24.rest.api.FindTrucksApi
import com.freight24.rest.response.SearchTruckerResponse.Trucker
import com.freight24.rest.response.SearchTruckerResponse.TruckerData
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class FindTrucksRepositoryImpl@Inject constructor(
    private val api: FindTrucksApi,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : FindTrucksRepository {

    override suspend fun searchTrucks(
        areaId: String?,
        cityId: String?,
        destinationCityId: String?,
        truckTypeId: String
    ): Result<TruckerData> = withContext(ioDispatcher) {
        try {
            if(areaId == null && cityId == null) {
               return@withContext Result.failure(
                   IllegalStateException("Either areaId or cityId must be provided")
               )
            }

            val response = api.searchTrucks(
                areaId = areaId,
                cityId = cityId,
                destinationCityId = destinationCityId,
                truckTypeId = truckTypeId
            )
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