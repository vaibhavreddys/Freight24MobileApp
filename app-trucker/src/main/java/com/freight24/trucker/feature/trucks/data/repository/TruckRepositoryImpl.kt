package com.freight24.trucker.feature.trucks.data.repository

import com.freight24.common.di.IoDispatcher
import com.freight24.network.provider.SafeCallProvider
import com.freight24.rest.api.TruckApi
import com.freight24.rest.response.AddTruckResponse
import com.freight24.rest.response.TruckDetailResponse
import com.freight24.rest.response.TruckResponse
import kotlinx.coroutines.CoroutineDispatcher
import okhttp3.MultipartBody
import javax.inject.Inject

internal class TruckRepositoryImpl
    @Inject
    constructor(
        private val api: TruckApi,
        private val safeCallProvider: SafeCallProvider,
        @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    ) : TruckRepository {
        override suspend fun fetchTrucks(): Result<TruckResponse> {
            return safeCallProvider.safeApiCall(ioDispatcher) {
                api.getTrucks()
            }
        }

        override suspend fun fetchTruckDetail(id: String): Result<TruckDetailResponse> {
            return safeCallProvider.safeApiCall(ioDispatcher) {
                api.getTruckDetail(id)
            }
        }

        override suspend fun addTruck(
            registrationNumber: MultipartBody.Part,
            truckTypeId: MultipartBody.Part,
            truckOwnerId: MultipartBody.Part,
            make: MultipartBody.Part?,
            model: MultipartBody.Part?,
            year: MultipartBody.Part?,
            rcCard: MultipartBody.Part?,
            fcCertificate: MultipartBody.Part?,
            insurance: MultipartBody.Part?,
        ): Result<AddTruckResponse> {
            return safeCallProvider.safeApiCall(ioDispatcher) {
                api.addTruck(
                    registrationNumber,
                    truckTypeId,
                    truckOwnerId,
                    make,
                    model,
                    year,
                    rcCard,
                    fcCertificate,
                    insurance,
                )
            }
        }
    }
