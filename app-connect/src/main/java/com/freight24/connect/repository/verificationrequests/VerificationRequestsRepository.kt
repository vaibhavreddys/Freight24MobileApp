package com.freight24.connect.repository.verificationrequests

import com.freight24.rest.response.VerificationTripsResponse
import com.freight24.rest.response.VerificationClientsResponse
import com.freight24.rest.response.VerificationTruckOwnersResponse
import com.freight24.rest.response.VerificationTrucksResponse

interface VerificationRequestsRepository {
    suspend fun getVerificationTrips(status: String? = null): Result<VerificationTripsResponse>

    suspend fun getVerificationTrucks(status: String? = null): Result<VerificationTrucksResponse>

    suspend fun getVerificationTruckOwners(status: String? = null): Result<VerificationTruckOwnersResponse>

    suspend fun getVerificationClients(status: String? = null): Result<VerificationClientsResponse>

    suspend fun updateVerificationRequestStatus(
        id: String,
        status: String,
        resolutionNotes: String? = null,
    ): Result<Unit>

    suspend fun updateTruckVerificationStatus(
        id: String,
        status: String,
        resolutionNotes: String? = null,
    ): Result<Unit>

    suspend fun updateTruckOwnerVerificationStatus(
        id: String,
        status: String,
        resolutionNotes: String? = null,
    ): Result<Unit>

    suspend fun updateClientVerificationStatus(
        id: String,
        status: String,
        resolutionNotes: String? = null,
    ): Result<Unit>

    suspend fun getSignedUrl(s3Url: String): Result<String>
}
