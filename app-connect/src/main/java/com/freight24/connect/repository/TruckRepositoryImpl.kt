package com.freight24.connect.repository

import com.freight24.common.di.IoDispatcher
import com.freight24.rest.api.DocumentApi
import com.freight24.rest.api.TruckApi
import com.freight24.rest.response.AddTruckResponse
import com.freight24.rest.response.SearchTruckResponse
import com.freight24.rest.response.TruckDetailResponse
import com.freight24.rest.response.TruckDto
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import javax.inject.Inject

class TruckRepositoryImpl
    @Inject
    constructor(
        private val api: TruckApi,
        private val documentApi: DocumentApi,
        @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    ) : TruckRepository {
        override suspend fun searchTrucks(query: String): Result<List<SearchTruckResponse.Truck>> =
            withContext(ioDispatcher) {
                try {
                    val response = api.searchTrucks(query)
                    if (response.isSuccessful && response.body() != null) {
                        Result.success(response.body()!!.data)
                    } else {
                        Result.failure(Exception(response.message()))
                    }
                } catch (e: Exception) {
                    Result.failure(e)
                }
            }

        override suspend fun getTruckDocumentSignedUrl(
            truckId: String,
            documentType: String,
        ): Result<String> =
            withContext(ioDispatcher) {
                try {
                    val response = documentApi.getTruckDocumentSignedUrl(truckId, documentType)
                    if (response.isSuccessful && response.body() != null) {
                        Result.success(response.body()!!.data.signedUrl)
                    } else {
                        Result.failure(Exception(response.message()))
                    }
                } catch (e: Exception) {
                    Result.failure(e)
                }
            }

        override suspend fun getTrucks(): Result<List<TruckDto>> =
            withContext(ioDispatcher) {
                try {
                    val response = api.getTrucks()
                    if (response.isSuccessful && response.body() != null) {
                        Result.success(response.body()!!.trucks ?: emptyList())
                    } else {
                        Result.failure(Exception(response.message()))
                    }
                } catch (e: Exception) {
                    Result.failure(e)
                }
            }

        override suspend fun getTruckDetail(id: String): Result<TruckDetailResponse> =
            withContext(ioDispatcher) {
                try {
                    val response = api.getTruckDetail(id)
                    if (response.isSuccessful && response.body() != null) {
                        Result.success(response.body()!!)
                    } else {
                        Result.failure(Exception(response.message()))
                    }
                } catch (e: Exception) {
                    Result.failure(e)
                }
            }

        override suspend fun addTruck(
            registrationNumber: MultipartBody.Part,
            truckTypeId: MultipartBody.Part,
            truckOwnerId: MultipartBody.Part,
            make: MultipartBody.Part?,
            model: MultipartBody.Part?,
            year: MultipartBody.Part?,
            capacityMt: MultipartBody.Part?,
            rcCard: MultipartBody.Part?,
            fcCertificate: MultipartBody.Part?,
            insurance: MultipartBody.Part?,
        ): Result<AddTruckResponse> =
            withContext(ioDispatcher) {
                try {
                    val response =
                        api.addTruck(
                            registrationNumber,
                            truckTypeId,
                            truckOwnerId,
                            make,
                            model,
                            year,
                            capacityMt,
                            rcCard,
                            fcCertificate,
                            insurance,
                        )
                    if (response.isSuccessful && response.body() != null) {
                        Result.success(response.body()!!)
                    } else {
                        Result.failure(Exception(response.message()))
                    }
                } catch (e: Exception) {
                    Result.failure(e)
                }
            }

        override suspend fun updateTruck(
            id: String,
            registrationNumber: MultipartBody.Part,
            truckTypeId: MultipartBody.Part,
            make: MultipartBody.Part?,
            model: MultipartBody.Part?,
            year: MultipartBody.Part?,
            capacityMt: MultipartBody.Part?,
            rcCard: MultipartBody.Part?,
            fcCertificate: MultipartBody.Part?,
            insurance: MultipartBody.Part?,
        ): Result<AddTruckResponse> =
            withContext(ioDispatcher) {
                try {
                    val response =
                        api.updateTruck(
                            id,
                            registrationNumber,
                            truckTypeId,
                            make,
                            model,
                            year,
                            capacityMt,
                            rcCard,
                            fcCertificate,
                            insurance,
                        )
                    if (response.isSuccessful && response.body() != null) {
                        Result.success(response.body()!!)
                    } else {
                        Result.failure(Exception(response.message()))
                    }
                } catch (e: Exception) {
                    Result.failure(e)
                }
            }

        override suspend fun checkTruckAvailability(registrationNumber: String): Result<String?> =
            withContext(ioDispatcher) {
                try {
                    val response = api.checkTruckAvailability(registrationNumber)
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
