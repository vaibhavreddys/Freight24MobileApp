package com.freight24.connect.repository.verificationrequests

import com.freight24.rest.response.VerificationTripsResponse

interface VerificationRequestsRepository {
    suspend fun getVerificationTrips(status: String? = null): Result<VerificationTripsResponse>

    suspend fun updateVerificationRequestStatus(
        id: String,
        status: String,
        resolutionNotes: String? = null,
    ): Result<Unit>

    suspend fun getSignedUrl(s3Url: String): Result<String>
}
