package com.freight24.connect.repository

import com.freight24.common.di.IoDispatcher
import com.freight24.network.error.toApiException
import com.freight24.connect.domain.model.EntityPage
import com.freight24.connect.domain.model.TruckAssignmentOption
import com.freight24.connect.domain.model.TruckCounts
import com.freight24.connect.domain.model.TruckDetails
import com.freight24.connect.domain.model.TruckManagementItem
import com.freight24.connect.domain.model.TruckRegistrationValidation
import com.freight24.connect.repository.mapper.toAssignmentPage
import com.freight24.connect.repository.mapper.toDomain
import com.freight24.connect.repository.mapper.toManagementPage
import com.freight24.rest.api.DocumentApi
import com.freight24.rest.api.TruckV2Api
import com.freight24.rest.response.CreateTruckV2Response
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import javax.inject.Inject

class TruckRepositoryImpl
    @Inject
    constructor(
        private val v2Api: TruckV2Api,
        private val documentApi: DocumentApi,
        @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    ) : TruckRepository {
        override suspend fun getTruckManagementPage(
            query: String?,
            status: String?,
            documentStatus: String?,
            page: Int,
            limit: Int,
        ): Result<EntityPage<TruckManagementItem, TruckCounts>> =
            withContext(ioDispatcher) {
                try {
                    val response =
                        if (query.isNullOrBlank()) {
                            v2Api.getTrucks(status, documentStatus, null, page, limit)
                        } else {
                            v2Api.searchTrucks(query.trim(), status, documentStatus, page, limit)
                        }
                    val body = response.body()
                    if (response.isSuccessful && body?.success == true) {
                        Result.success(body.toManagementPage())
                    } else {
                        Result.failure(response.toApiException(fallbackMessage = "Failed to load trucks"))
                    }
                } catch (error: Exception) {
                    Result.failure(error)
                }
            }

        override suspend fun searchAssignmentTrucks(
            query: String,
            page: Int,
            limit: Int,
        ): Result<EntityPage<TruckAssignmentOption, TruckCounts>> =
            withContext(ioDispatcher) {
                try {
                    val response = v2Api.searchTrucks(query.trim(), "active", null, page, limit)
                    val body = response.body()
                    if (response.isSuccessful && body?.success == true) {
                        Result.success(body.toAssignmentPage())
                    } else {
                        Result.failure(response.toApiException(fallbackMessage = "Failed to search trucks"))
                    }
                } catch (error: Exception) {
                    Result.failure(error)
                }
            }

        override suspend fun getTruckDetails(truckId: String): Result<TruckDetails> =
            withContext(ioDispatcher) {
                try {
                    val response = v2Api.getTruckDetail(truckId)
                    val body = response.body()
                    val data = body?.data
                    if (response.isSuccessful && body?.success == true && data != null) {
                        Result.success(data.toDomain())
                    } else {
                        Result.failure(response.toApiException(bodyMessage = body?.message, fallbackMessage = "Failed to load truck"))
                    }
                } catch (error: Exception) {
                    Result.failure(error)
                }
            }

        override suspend fun validateRegistration(
            registrationNumber: String,
            excludeTruckId: String?,
        ): Result<TruckRegistrationValidation> =
            withContext(ioDispatcher) {
                try {
                    val response = v2Api.validateRegistration(registrationNumber, excludeTruckId)
                    val body = response.body()
                    if (response.isSuccessful && body?.success == true) {
                        Result.success(
                            TruckRegistrationValidation(
                                exists = body.data.exists,
                                truckId = body.data.truck?.id,
                            ),
                        )
                    } else {
                        Result.failure(response.toApiException(fallbackMessage = "Failed to validate registration number"))
                    }
                } catch (error: Exception) {
                    Result.failure(error)
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

        override suspend fun addTruck(
            registrationNumber: MultipartBody.Part,
            truckTypeId: MultipartBody.Part,
            ownerProfileId: MultipartBody.Part,
            make: MultipartBody.Part?,
            model: MultipartBody.Part?,
            year: MultipartBody.Part?,
            capacityMt: MultipartBody.Part?,
            rcCard: MultipartBody.Part?,
            fcCertificate: MultipartBody.Part?,
            insurance: MultipartBody.Part?,
        ): Result<CreateTruckV2Response> =
            withContext(ioDispatcher) {
                try {
                    val response =
                        v2Api.createTruck(
                            registrationNumber,
                            truckTypeId,
                            ownerProfileId,
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
                        Result.failure(response.toApiException(fallbackMessage = "Failed to create truck"))
                    }
                } catch (e: Exception) {
                    Result.failure(e)
                }
            }

        override suspend fun updateTruck(
            truckId: String,
            registrationNumber: MultipartBody.Part,
            truckTypeId: MultipartBody.Part,
            make: MultipartBody.Part?,
            model: MultipartBody.Part?,
            year: MultipartBody.Part?,
            capacityMt: MultipartBody.Part?,
            rcCard: MultipartBody.Part?,
            fcCertificate: MultipartBody.Part?,
            insurance: MultipartBody.Part?,
        ): Result<TruckDetails> =
            withContext(ioDispatcher) {
                try {
                    val response =
                        v2Api.updateTruck(
                            truckId,
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
                    val body = response.body()
                    val data = body?.data
                    if (response.isSuccessful && body?.success == true && data != null) {
                        Result.success(data.toDomain())
                    } else {
                        Result.failure(response.toApiException(bodyMessage = body?.message, fallbackMessage = "Failed to update truck"))
                    }
                } catch (error: Exception) {
                    Result.failure(error)
                }
            }

    }
