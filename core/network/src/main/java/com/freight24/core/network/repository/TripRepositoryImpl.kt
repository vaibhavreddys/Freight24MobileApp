package com.freight24.core.network.repository

import android.util.Log
import com.freight24.core.common.model.PaginatedTrips
import com.freight24.core.common.repository.TripRepository
import com.freight24.core.common.repository.toDomain
import com.freight24.core.common.repository.toEntity
import com.freight24.core.common.util.CacheConstants
import com.freight24.core.database.dao.TripDao
import com.freight24.core.database.entity.TripsPaginationEntity
import com.freight24.core.network.api.TripApi
import com.freight24.core.network.cache.TripMemoryCache
import com.freight24.core.network.mapper.toDomain
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TripRepositoryImpl @Inject constructor(
    private val api: TripApi,
    private val dao: TripDao,
    private val memoryCache: TripMemoryCache,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : TripRepository {

    override suspend fun fetchTrips(
        page: Int,
        forceRefresh: Boolean
    ): Flow<PaginatedTrips> = flow {

        // L1: Memory Cache
        if (!forceRefresh) {
            memoryCache.getTrips(page)?.let { cached ->
                val pagination = dao.getPagination()
                emit(
                    PaginatedTrips(
                        trips = cached,
                        page = page,
                        hasMore = pagination?.hasMore ?: false,
                        totalTrips = pagination?.totalTrips ?: cached.size
                    )
                )
                Log.d(TAG, "✅ L1 HIT: Trips page $page from memory")
                return@flow
            }
        }

        // L2: Room Database
        val dbTrips = dao.getTripsByPage(page)
        val pagination = dao.getPagination()
        val isCacheFresh = pagination?.let {
            System.currentTimeMillis() - it.lastFetchedAt < CacheConstants.LOADS_TTL_MS
        } ?: false

        if (!forceRefresh && dbTrips.isNotEmpty() && isCacheFresh) {
            val trips = dbTrips.map { it.toDomain() }
            emit(
                PaginatedTrips(
                    trips = trips,
                    page = page,
                    hasMore = pagination?.hasMore ?: false,
                    totalTrips = pagination?.totalTrips ?: trips.size
                )
            )
            memoryCache.putTrips(page, trips)
            Log.d(TAG, "✅ L2 HIT: Trips page $page from Room")
            return@flow
        }

        // Emit stale cache if available
        if (dbTrips.isNotEmpty()) {
            val trips = dbTrips.map { it.toDomain() }
            emit(
                PaginatedTrips(
                    trips = trips,
                    page = page,
                    hasMore = pagination?.hasMore ?: false,
                    totalTrips = pagination?.totalTrips ?: trips.size
                )
            )
            Log.d(TAG, "📦 Emitting stale Trips cache")
        }

        // L3: Network API
        try {
            val response = api.getTrips(page)
            if (response.success && response.data != null) {
                val domainTrips = response.data.map { it.toDomain() }
                
                // Update Room
                dao.refreshTrips(page, domainTrips.map { it.toEntity(page) })
                dao.upsertPagination(
                    TripsPaginationEntity(
                        lastPage = page,
                        hasMore = response.pagination?.pages ?: 0 > page,
                        totalTrips = response.pagination?.total ?: 0
                    )
                )

                val result = PaginatedTrips(
                    trips = domainTrips,
                    page = page,
                    hasMore = response.pagination?.pages ?: 0 > page,
                    totalTrips = response.pagination?.total ?: 0
                )

                // Update Memory
                memoryCache.putTrips(page, domainTrips)

                emit(result)
                Log.d(TAG, "✅ L3 SUCCESS: Fetched Trips page $page from network")
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Network error: ${e.message}")
            if (dbTrips.isEmpty()) throw e
        }
    }.flowOn(ioDispatcher)

    companion object {
        private const val TAG = "TripRepo"
    }
}
