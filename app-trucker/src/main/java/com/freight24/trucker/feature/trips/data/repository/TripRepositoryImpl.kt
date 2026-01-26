package com.freight24.trucker.feature.trips.data.repository

import com.freight24.common.di.IoDispatcher
import com.freight24.common.model.TripDetail
import com.freight24.network.provider.SafeCallProvider
import com.freight24.rest.api.TripApi
import com.freight24.rest.response.TripResponse
import com.freight24.trucker.feature.trips.mapper.toDomain
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject

internal class TripRepositoryImpl @Inject constructor(
    private val safeCallProvider: SafeCallProvider,
    private val tripApi: TripApi,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : TripRepository {

    override suspend fun fetchTrips(
        page: Int,
        forceRefresh: Boolean
    ): Result<TripResponse> =
        safeCallProvider.safeApiCall(ioDispatcher) {
            tripApi.getTrips(page)
        }

    override suspend fun fetchTripDetail(
        tripId: String,
        forceRefresh: Boolean
    ): Result<TripDetail> =
        safeCallProvider.safeApiCall(ioDispatcher) {
            tripApi.getTripDetail(tripId)
        }.mapCatching { response ->
            response.data?.toDomain() ?: throw IllegalStateException("Trip detail data is null")
        }

    override suspend fun updateTripStatus(
        tripId: String,
        status: String
    ): Result<TripDetail> {
        val updateResult = safeCallProvider.safeApiCall(ioDispatcher) {
            tripApi.updateTripStatus(
                tripId,
                com.freight24.rest.request.UpdateTripStatusRequest(status)
            )
        }

        return if (updateResult.isSuccess) {
            fetchTripDetail(tripId, forceRefresh = true)
        } else {
            Result.failure(updateResult.exceptionOrNull() ?: Exception("Failed to update status"))
        }
    }

}