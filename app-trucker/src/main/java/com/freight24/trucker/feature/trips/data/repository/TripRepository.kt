package com.freight24.trucker.feature.trips.data.repository

import com.freight24.common.model.Trip
import com.freight24.common.model.TripDetail
import com.freight24.rest.response.TripResponse
import kotlinx.coroutines.flow.Flow

internal interface TripRepository {
    /**
     * The driver's last known manifest, straight from the local cache. Emits
     * again whenever a fetch writes fresher rows.
     */
    fun observeCachedTrips(): Flow<List<Trip>>

    suspend fun fetchTrips(
        page: Int,
        forceRefresh: Boolean = false,
    ): Result<TripResponse>

    suspend fun fetchTripDetail(
        tripId: String,
        forceRefresh: Boolean = false,
    ): Result<TripDetail>

    suspend fun updateTripStatus(
        tripId: String,
        status: String,
    ): Result<TripDetail>

    suspend fun uploadLr(
        tripId: String,
        filePart: okhttp3.MultipartBody.Part,
        notes: okhttp3.RequestBody? = null,
    ): Result<TripDetail>

    suspend fun uploadPod(
        tripId: String,
        filePart: okhttp3.MultipartBody.Part,
        podType: okhttp3.RequestBody,
        notes: okhttp3.RequestBody? = null,
        replaceDocumentId: String? = null,
    ): Result<TripDetail>

    suspend fun getSignedDocument(
        tripId: String,
        documentId: String,
    ): Result<String>
}
