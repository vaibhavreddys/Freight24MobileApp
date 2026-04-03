package com.freight24.connect.repository.verificationrequests

import com.freight24.rest.response.VerificationTripsResponse

interface VerificationRequestsRepository {
    suspend fun getVerificationTrips(status: String? = null): Result<VerificationTripsResponse>

    suspend fun getSignedUrl(s3Url: String): Result<String>
}
