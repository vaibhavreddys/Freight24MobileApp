package com.freight24.connect.repository.verificationrequests

import com.freight24.common.di.IoDispatcher
import com.freight24.rest.api.DocumentApi
import com.freight24.rest.api.VerificationApi
import com.freight24.rest.request.UpdateVerificationRequestStatusRequest
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class VerificationRequestsRepositoryImpl @Inject constructor(
    private val verificationApi: VerificationApi,
    private val documentApi: DocumentApi,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : VerificationRequestsRepository {

    override suspend fun getVerificationTrips(status: String?): Result<com.freight24.rest.response.VerificationTripsResponse> =
        withContext(ioDispatcher) {
            try {
                val response = verificationApi.getVerificationTrips(status)
                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!)
                } else {
                    Result.failure(Exception(response.message()))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun getVerificationTrucks(status: String?): Result<com.freight24.rest.response.VerificationTrucksResponse> =
        withContext(ioDispatcher) {
            try {
                val response = verificationApi.getVerificationTrucks(status)
                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!)
                } else {
                    Result.failure(Exception(response.message()))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun getVerificationTruckOwners(status: String?): Result<com.freight24.rest.response.VerificationTruckOwnersResponse> =
        withContext(ioDispatcher) {
            try {
                val response = verificationApi.getVerificationTruckOwners(status)
                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!)
                } else {
                    Result.failure(Exception(response.message()))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun getVerificationClients(status: String?): Result<com.freight24.rest.response.VerificationClientsResponse> =
        withContext(ioDispatcher) {
            try {
                val response = verificationApi.getVerificationClients(status)
                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!)
                } else {
                    Result.failure(Exception(response.message()))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun updateVerificationRequestStatus(
        id: String,
        status: String,
        resolutionNotes: String?,
    ): Result<Unit> =
        withContext(ioDispatcher) {
            try {
                val response = verificationApi.updateVerificationRequestStatus(
                    id = id,
                    request = UpdateVerificationRequestStatusRequest(
                        status = status,
                        resolutionNotes = resolutionNotes,
                    ),
                )
                if (response.isSuccessful) {
                    Result.success(Unit)
                } else {
                    Result.failure(Exception(response.message()))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun updateTruckVerificationStatus(
        id: String,
        status: String,
        resolutionNotes: String?,
    ): Result<Unit> =
        withContext(ioDispatcher) {
            try {
                val response = verificationApi.updateTruckVerificationStatus(
                    id = id,
                    request = UpdateVerificationRequestStatusRequest(
                        status = status,
                        resolutionNotes = resolutionNotes,
                    ),
                )
                if (response.isSuccessful) {
                    Result.success(Unit)
                } else {
                    Result.failure(Exception(response.message()))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun updateTruckOwnerVerificationStatus(
        id: String,
        status: String,
        resolutionNotes: String?,
    ): Result<Unit> =
        withContext(ioDispatcher) {
            try {
                val response = verificationApi.updateTruckOwnerVerificationStatus(
                    id = id,
                    request = UpdateVerificationRequestStatusRequest(
                        status = status,
                        resolutionNotes = resolutionNotes,
                    ),
                )
                if (response.isSuccessful) {
                    Result.success(Unit)
                } else {
                    Result.failure(Exception(response.message()))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun updateClientVerificationStatus(
        id: String,
        status: String,
        resolutionNotes: String?,
    ): Result<Unit> =
        withContext(ioDispatcher) {
            try {
                val response = verificationApi.updateClientVerificationStatus(
                    id = id,
                    request = UpdateVerificationRequestStatusRequest(
                        status = status,
                        resolutionNotes = resolutionNotes,
                    ),
                )
                if (response.isSuccessful) {
                    Result.success(Unit)
                } else {
                    Result.failure(Exception(response.message()))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun getSignedUrl(entity: String, requestId: String, documentKey: String): Result<String> =
        withContext(ioDispatcher) {
            try {
                val response = documentApi.getVerificationRequestDocumentSignedUrl(
                    entity = entity,
                    requestId = requestId,
                    documentKey = documentKey,
                )
                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!.data.signedUrl)
                } else {
                    Result.failure(Exception(response.message()))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
}
