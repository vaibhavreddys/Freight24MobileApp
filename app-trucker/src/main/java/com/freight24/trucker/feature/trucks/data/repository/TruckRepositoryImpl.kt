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
        private val documentApi: com.freight24.rest.api.DocumentApi,
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
                    null,
                    rcCard,
                    fcCertificate,
                    insurance,
                )
            }
        }

        override suspend fun updateTruckDocuments(
            truckId: String,
            rcCard: MultipartBody.Part?,
            fcCertificate: MultipartBody.Part?,
            insurance: MultipartBody.Part?,
        ): Result<AddTruckResponse> {
            return safeCallProvider.safeApiCall(ioDispatcher) {
                api.updateTruck(
                    id = truckId,
                    rc_card = rcCard,
                    fc_certificate = fcCertificate,
                    insurance = insurance,
                )
            }
        }

        override suspend fun updateTruck(
            id: String,
            registrationNumber: MultipartBody.Part,
            truckTypeId: MultipartBody.Part,
            make: MultipartBody.Part?,
            model: MultipartBody.Part?,
            year: MultipartBody.Part?,
            rcCard: MultipartBody.Part?,
            fcCertificate: MultipartBody.Part?,
            insurance: MultipartBody.Part?,
        ): Result<AddTruckResponse> {
            return safeCallProvider.safeApiCall(ioDispatcher) {
                api.updateTruck(
                    id = id,
                    registration_number = registrationNumber,
                    truck_type_id = truckTypeId,
                    make = make,
                    model = model,
                    year = year,
                    rc_card = rcCard,
                    fc_certificate = fcCertificate,
                    insurance = insurance,
                )
            }
        }

        override suspend fun getTruckDocumentSignedUrl(
            truckId: String,
            documentType: String,
        ): Result<String> {
            return safeCallProvider.safeApiCall(ioDispatcher) {
                documentApi.getTruckDocumentSignedUrl(truckId, documentType)
            }.map { it.data.signedUrl }
        }

        override suspend fun checkTruckAvailability(registrationNumber: String): Result<String?> {
            return safeCallProvider.safeApiCall(ioDispatcher) {
                api.checkTruckAvailability(registrationNumber)
            }.map { it.data }
        }
    }
