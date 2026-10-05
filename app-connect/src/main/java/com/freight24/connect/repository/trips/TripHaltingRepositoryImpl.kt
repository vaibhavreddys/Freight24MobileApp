package com.freight24.connect.repository.trips

import com.freight24.common.di.IoDispatcher
import com.freight24.rest.api.TripHaltingApi
import com.freight24.rest.request.HaltingRequest
import com.freight24.rest.response.TruckHaltingRate
import com.freight24.rest.response.UpdateHaltingRatesResponse
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class TripHaltingRepositoryImpl
    @Inject
    constructor(
        private val api: TripHaltingApi,
        @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    ) : TripHaltingRepository {
        override suspend fun getHaltingRate(truckTypeId: String): Result<TruckHaltingRate> =
            withContext(ioDispatcher) {
                try {
                    val response =
                        api.getHaltingRate(
                            truckTypeId,
                        )
                    if (response.isSuccessful && response.body() != null && response.body()?.data != null) {
                        Result.success(response.body()!!.data!!)
                    } else {
                        Result.failure(Exception(response.message()))
                    }
                } catch (e: Exception) {
                    Result.failure(e)
                }
            }

        override suspend fun addHaltingCharges(
            tripId: String,
            loadingDays: Int?,
            unloadingDays: Int?,
            ratePerDay: Int?,
            notes: String?,
        ): Result<UpdateHaltingRatesResponse.UpdateHaltingRatesData> =
            withContext(ioDispatcher) {
                try {
                    val response =
                        api.addHaltingCharges(
                            tripId,
                            HaltingRequest(
                                loadingDays,
                                unloadingDays,
                                ratePerDay,
                                notes,
                            ),
                        )
                    if (response.isSuccessful && response.body() != null && response.body()?.data != null) {
                        Result.success(response.body()!!.data!!)
                    } else {
                        Result.failure(Exception(response.message()))
                    }
                } catch (e: Exception) {
                    Result.failure(e)
                }
            }
    }
