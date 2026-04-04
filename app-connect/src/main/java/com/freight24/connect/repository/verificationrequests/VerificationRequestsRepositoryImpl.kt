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

    override suspend fun getSignedUrl(s3Url: String): Result<String> =
        withContext(ioDispatcher) {
            try {
                val response = documentApi.getGenericSignedUrl(s3Url)
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
