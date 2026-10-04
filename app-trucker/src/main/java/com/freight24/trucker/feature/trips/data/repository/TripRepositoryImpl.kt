package com.freight24.trucker.feature.trips.data.repository

import com.freight24.auth.AuthDataStore
import com.freight24.common.di.IoDispatcher
import com.freight24.common.model.Trip
import com.freight24.common.model.TripDetail
import com.freight24.database.dao.TripDao
import com.freight24.network.provider.SafeCallProvider
import com.freight24.rest.api.TripApi
import com.freight24.rest.request.UpdateTruckerStatus
import com.freight24.rest.response.TripResponse
import com.freight24.trucker.feature.trips.mapper.toDomain
import com.freight24.trucker.feature.trips.mapper.toEntity
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import javax.inject.Inject

internal class TripRepositoryImpl @Inject constructor(
    private val safeCallProvider: SafeCallProvider,
    private val tripApi: TripApi,
    private val tripDao: TripDao,
    private val authDataStore: AuthDataStore,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : TripRepository {

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeCachedTrips(): Flow<List<Trip>> =
        authDataStore.userId
            .filterNotNull()
            .flatMapLatest { ownerId -> tripDao.observeTrips(ownerId) }
            .map { entities -> entities.map { it.toDomain() } }

    override suspend fun fetchTrips(
        page: Int,
        forceRefresh: Boolean
    ): Result<TripResponse> =
        safeCallProvider.safeApiCall(ioDispatcher) {
            tripApi.getTrips(page)
        }.onSuccess { response ->
            cacheTrips(page, response)
        }

    /**
     * Writes straight from the DTO rather than the mapped list — the mapper that
     * reads these rows back already exists, and formatting a page twice to reach
     * the same rows buys nothing.
     */
    private suspend fun cacheTrips(page: Int, response: TripResponse) {
        val ownerId = authDataStore.userId.first().orEmpty()
        if (ownerId.isBlank()) return
        runCatching {
            tripDao.refreshTrips(
                ownerId = ownerId,
                page = page,
                trips = response.data.orEmpty().map { it.toEntity(ownerId) },
            )
        }
    }

    override suspend fun fetchTripDetail(
        tripId: String,
        forceRefresh: Boolean
    ): Result<TripDetail> =
        safeCallProvider.safeApiCall(ioDispatcher) {
            tripApi.getTripDetail(tripId)
        }.mapCatching { response ->
            response.data?.toDomain() ?: throw IllegalStateException("Trip detail data is null")
        }

    override suspend fun updateTripStatus(
        tripId: String,
        status: String
    ): Result<TripDetail> {
        val updateResult = safeCallProvider.safeApiCall(ioDispatcher) {
            tripApi.updateTripStatus(
                tripId,
                UpdateTruckerStatus(truckerStatus = status, remarks = null)
            )
        }

        return if (updateResult.isSuccess) {
            fetchTripDetail(tripId, forceRefresh = true)
        } else {
            Result.failure(updateResult.exceptionOrNull() ?: Exception("Failed to update status"))
        }
    }

    override suspend fun uploadLr(
        tripId: String,
        filePart: okhttp3.MultipartBody.Part,
        notes: okhttp3.RequestBody?
    ): Result<TripDetail> {
        val uploadResult = safeCallProvider.safeApiCall(ioDispatcher) {
            tripApi.uploadLr(tripId, filePart, notes)
        }

        return if (uploadResult.isSuccess) {
            fetchTripDetail(tripId, forceRefresh = true)
        } else {
            Result.failure(uploadResult.exceptionOrNull() ?: Exception("Failed to upload LR"))
        }
    }

    override suspend fun getSignedDocument(
        tripId: String,
        documentId: String,
    ): Result<String> = safeCallProvider.safeApiCall(ioDispatcher) {
        tripApi.getSignedDocument(tripId, documentId)
    }.mapCatching { response ->
        response.data?.signedUrl ?: throw IllegalStateException("Signed URL not found.")
    }

}