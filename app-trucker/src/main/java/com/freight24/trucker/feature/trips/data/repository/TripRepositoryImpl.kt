package com.freight24.trucker.feature.trips.data.repository

import com.freight24.common.di.IoDispatcher
import com.freight24.network.provider.SafeCallProvider
import com.freight24.rest.api.TripApi
import com.freight24.rest.response.TripResponse
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject

internal class TripRepositoryImpl @Inject constructor(
    private val safeCallProvider: SafeCallProvider,
    private val tripApi: TripApi,
    @param:IoDispatcher private val  ioDispatcher: CoroutineDispatcher
) : TripRepository {

    override suspend fun fetchTrips(
        page: Int,
        forceRefresh: Boolean
    ): Result<TripResponse> =
        safeCallProvider.safeApiCall(ioDispatcher) {
            tripApi.getTrips(page)
        }

}