package com.freight24.connect.repository

import com.freight24.common.di.IoDispatcher
import com.freight24.rest.api.TripApi
import com.freight24.rest.request.CreateTripRequest
import com.freight24.rest.response.CreatedTripResponse
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class TripRepositoryImpl @Inject constructor(
    private val api: TripApi,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : TripRepository {

    override suspend fun createTrip(
        indentId: String,
        truckId: String,
        driverName: String?,
        driverPhone: String?
    ): Result<CreatedTripResponse.TripData> =
        withContext(ioDispatcher) {
            try {
                val response = api.createTrip(
                    CreateTripRequest(
                        indentId,
                        truckId,
                        driverName,
                        driverPhone
                    )
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