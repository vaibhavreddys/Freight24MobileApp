package com.freight24.connect.repository

import com.freight24.common.di.IoDispatcher
import com.freight24.rest.api.LocationApi
import com.freight24.rest.response.LocationDto
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class LocationRepositoryImpl
    @Inject
    constructor(
        private val api: LocationApi,
        @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    ) : LocationRepository {
        override suspend fun searchLocations(query: String): Result<List<LocationDto>> =
            withContext(ioDispatcher) {
                try {
                    val response = api.searchLocations(query)
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
