package com.freight24.trucker.feature.profile.data

import com.freight24.common.di.IoDispatcher
import com.freight24.network.provider.SafeCallProvider
import com.freight24.rest.api.LocationApi
import com.freight24.rest.repository.LocationRepository
import com.freight24.rest.response.CityResult
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject

internal class LocationRepositoryImpl
    @Inject
    constructor(
        private val api: LocationApi,
        private val safeCallProvider: SafeCallProvider,
        @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    ) : LocationRepository {
        override suspend fun searchCities(
            query: String,
            type: String?,
        ): Result<List<CityResult>> {
            return safeCallProvider.safeApiCall(ioDispatcher) {
                api.searchCities(query, type)
            }.map { response ->
                if (response.success) response.data else throw IllegalStateException("Location search failed")
            }
        }
    }
