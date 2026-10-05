package com.freight24.connect.repository

import com.freight24.common.di.IoDispatcher
import com.freight24.rest.api.LocationApi
import com.freight24.rest.response.CityResult
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
                    val response = api.searchCities(query)
                    if (response.isSuccessful && response.body() != null) {
                        val body = response.body()!!
                        if (body.success) {
                            Result.success(
                                body.data.map { it.toLocationDto() },
                            )
                        } else {
                            Result.failure(IllegalStateException("Location search failed"))
                        }
                    } else {
                        Result.failure(Exception(response.message()))
                    }
                } catch (e: Exception) {
                    Result.failure(e)
                }
            }

        override suspend fun searchCities(
            query: String,
            type: String?,
        ): Result<List<CityResult>> =
            withContext(ioDispatcher) {
                try {
                    val response = api.searchCities(query, type)
                    if (response.isSuccessful && response.body() != null) {
                        val body = response.body()!!
                        if (body.success) {
                            Result.success(body.data)
                        } else {
                            Result.failure(IllegalStateException("Location search failed"))
                        }
                    } else {
                        Result.failure(Exception(response.message()))
                    }
                } catch (e: Exception) {
                    Result.failure(e)
                }
            }

        private fun CityResult.toLocationDto(): LocationDto =
            LocationDto(
                id = id.toString(),
                type = type,
                name = name,
                city = city,
                state = state,
            )
    }
