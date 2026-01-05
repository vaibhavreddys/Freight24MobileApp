package com.freight24.core.network.repository

import android.util.Log
import com.freight24.core.common.model.CityLoadSummary
import com.freight24.core.common.model.PaginatedLoads
import com.freight24.core.common.repository.LoadBoardRepository
import com.freight24.core.common.repository.toDomain
import com.freight24.core.common.repository.toEntity
import com.freight24.core.common.util.CacheConstants
import com.freight24.core.database.dao.LoadBoardDao
import com.freight24.core.database.entity.CityEntity
import com.freight24.core.database.entity.CityPaginationEntity
import com.freight24.core.network.api.LoadBoardApi
import com.freight24.core.network.cache.LoadMemoryCache
import com.freight24.core.network.mapper.toDomain
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Production-grade repository with three-tier caching strategy.
 * 
 * Cache Hierarchy:
 * - L1 (Memory): Instant access (0ms), session-scoped
 * - L2 (Room): Fast access (10ms), persistent
 * - L3 (Network): Slow access (500ms), source of truth
 * 
 * Strategy:
 * 1. Check L1 → emit if hit
 * 2. Check L2 → emit if fresh
 * 3. Fetch L3 → update L1 + L2
 */
@Singleton
class LoadBoardRepositoryCachedImpl @Inject constructor(
    private val api: LoadBoardApi,
    private val dao: LoadBoardDao,
    private val memoryCache: LoadMemoryCache,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : LoadBoardRepository {

    /**
     * Fetch load board summary (cities with load counts).
     * 
     * Flow:
     * 1. L1 (Memory): Return immediately if cached
     * 2. L2 (Room): Return if fresh (TTL not expired)
     * 3. L3 (Network): Fetch and update both caches
     */
    override suspend fun fetchLoadBoardSummary(
        forceRefresh: Boolean
    ): Flow<List<CityLoadSummary>> = flow {
        
        // === L1: In-Memory Cache ===
        if (!forceRefresh) {
            memoryCache.getCitySummary()?.let { cached ->
                Log.d(TAG, "✅ L1 HIT: City summary from memory cache")
                emit(cached)
                return@flow // Hot path: instant return
            }
        }
        
        // === L2: Room Database ===
        val dbCities = dao.getCities()
        val hasFreshCache = !forceRefresh && dao.hasFreshCities()
        
        if (hasFreshCache && dbCities.isNotEmpty()) {
            val summary = dbCities.map { city ->
                val loads = dao.getLoadsForCity(city.cityId)
                CityLoadSummary(
                    cityId = city.cityId,
                    cityName = city.cityName,
                    totalLoads = city.totalLoads,
                    initialLoads = loads.map { it.toDomain() }
                )
            }
            
            // Update L1 cache
            memoryCache.putCitySummary(summary)
            
            Log.d(TAG, "✅ L2 HIT: City summary from Room (${summary.size} cities)")
            emit(summary)
            return@flow // Warm path: fast return
        }
        
        // === L3: Network API ===
        try {
            Log.d(TAG, "🌐 L3 FETCH: Fetching city summary from network")
            val response = api.getLoadBoardSummary()
            
            if (response.success && response.data != null) {
                val cities = response.data
                
                // Update L2 (Room) - atomic transaction
                val cityEntities = cities.map {
                    CityEntity(
                        cityId = it.operationalCityId,
                        cityName = it.operationalCity,
                        totalLoads = it.totalLoads
                    )
                }
                dao.refreshCities(cityEntities)
                
                // Store initial loads for each city
                cities.forEach { city ->
                    val domainLoads = city.latestLoads.map {
                        it.toDomain(city.operationalCity)
                    }
                    
                    if (domainLoads.isNotEmpty()) {
                        dao.refreshLoadsForCity(
                            cityId = city.operationalCityId,
                            page = 1,
                            loads = domainLoads.map { it.toEntity(city.operationalCityId, page = 1) }
                        )
                        
                        dao.upsertPagination(
                            CityPaginationEntity(
                                cityId = city.operationalCityId,
                                currentPage = 1,
                                totalPages = (city.totalLoads + CacheConstants.LOADS_PAGE_SIZE - 1) / CacheConstants.LOADS_PAGE_SIZE,
                                hasMore = city.totalLoads > domainLoads.size,
                                totalLoads = city.totalLoads
                            )
                        )
                    }
                }
                
                // Build summary from fresh data
                val summary = cityEntities.map { city ->
                    val loads = dao.getLoadsForCity(city.cityId)
                    CityLoadSummary(
                        cityId = city.cityId,
                        cityName = city.cityName,
                        totalLoads = city.totalLoads,
                        initialLoads = loads.map { it.toDomain() }
                    )
                }
                
                // Update L1 (Memory)
                memoryCache.putCitySummary(summary)
                
                Log.d(TAG, "✅ L3 SUCCESS: Fetched ${summary.size} cities from network")
                emit(summary)
            } else {
                Log.w(TAG, "⚠️ Network response unsuccessful")
                // Emit stale cache if available
                if (dbCities.isNotEmpty()) {
                    val staleSummary = dbCities.map { it.toDomainSummary() }
                    emit(staleSummary)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Network error: ${e.message}")
            // Graceful degradation: emit stale cache
            if (dbCities.isNotEmpty()) {
                val staleSummary = dbCities.map { it.toDomainSummary() }
                Log.d(TAG, "📦 Fallback: Emitting stale cache (${staleSummary.size} cities)")
                emit(staleSummary)
            } else {
                // No cache available, propagate error
                throw e
            }
        }
    }.flowOn(ioDispatcher)

    /**
     * Fetch paginated loads for a specific city.
     * 
     * Flow:
     * 1. L1 (Memory): Return immediately if cached (page 1 only)
     * 2. L2 (Room): Return if available and fresh
     * 3. L3 (Network): Fetch and update both caches
     */
    override suspend fun fetchLoadsForCity(
        cityId: Int,
        page: Int,
        forceRefresh: Boolean
    ): Flow<PaginatedLoads> = flow {
        
        // === L1: In-Memory Cache (page 1 only) ===
        if (!forceRefresh && page == 1) {
            memoryCache.getLoadsForCity(cityId, page)?.let { cached ->
                val pagination = dao.getPagination(cityId)
                val result = PaginatedLoads(
                    loads = cached,
                    page = page,
                    hasMore = pagination?.hasMore ?: false,
                    totalLoads = pagination?.totalLoads ?: cached.size
                )
                Log.d(TAG, "✅ L1 HIT: Loads for city $cityId page $page from memory")
                emit(result)
                return@flow // Hot path: instant return
            }
        }
        
        // === L2: Room Database ===
        val dbLoads = dao.getLoadsForCityPage(cityId, page)
        val dbPagination = dao.getPagination(cityId)
        
        // Check if cache is fresh
        val isCacheFresh = dbPagination?.let {
            System.currentTimeMillis() - it.lastFetchedAt < CacheConstants.LOADS_TTL_MS
        } ?: false
        
        if (!forceRefresh && dbLoads.isNotEmpty() && isCacheFresh) {
            val loads = dbLoads.map { it.toDomain() }
            val result = PaginatedLoads(
                loads = loads,
                page = page,
                hasMore = dbPagination?.hasMore ?: false,
                totalLoads = dbPagination?.totalLoads ?: loads.size
            )
            
            // Update L1 cache
            memoryCache.putLoadsForCity(cityId, page, loads)
            
            Log.d(TAG, "✅ L2 HIT: ${loads.size} loads for city $cityId page $page from Room")
            emit(result)
            return@flow // Warm path: fast return
        }
        
        // Emit stale cache first if available (better UX)
        if (dbLoads.isNotEmpty()) {
            val loads = dbLoads.map { it.toDomain() }
            val staleResult = PaginatedLoads(
                loads = loads,
                page = page,
                hasMore = dbPagination?.hasMore ?: false,
                totalLoads = dbPagination?.totalLoads ?: loads.size
            )
            Log.d(TAG, "📦 Emitting stale cache while fetching fresh data")
            emit(staleResult)
        }
        
        // === L3: Network API ===
        try {
            Log.d(TAG, "🌐 L3 FETCH: Fetching loads for city $cityId page $page from network")
            val response = api.getPaginatedLoads(cityId, page)
            
            if (response.success && response.data != null) {
                val data = response.data
                val fallbackCity = data.loads.firstOrNull()?.loadingCity
                    ?: data.loads.firstOrNull()?.loadingArea
                    ?: "Unknown"
                
                val domainLoads = data.loads.map { it.toDomain(fallbackCity) }
                
                // Update L2 (Room) - atomic transaction
                dao.refreshLoadsForCity(
                    cityId = cityId,
                    page = page,
                    loads = domainLoads.map { it.toEntity(cityId, page) }
                )
                
                dao.upsertPagination(
                    CityPaginationEntity(
                        cityId = cityId,
                        currentPage = page,
                        totalPages = (data.pagination.total_loads + CacheConstants.LOADS_PAGE_SIZE - 1) / CacheConstants.LOADS_PAGE_SIZE,
                        hasMore = data.pagination.has_more,
                        totalLoads = data.pagination.total_loads
                    )
                )
                
                val result = PaginatedLoads(
                    loads = domainLoads,
                    page = page,
                    hasMore = data.pagination.has_more,
                    totalLoads = data.pagination.total_loads
                )
                
                // Update L1 (Memory)
                memoryCache.putLoadsForCity(cityId, page, domainLoads)
                
                Log.d(TAG, "✅ L3 SUCCESS: Fetched ${domainLoads.size} loads for city $cityId page $page")
                emit(result)
            } else {
                Log.w(TAG, "⚠️ Network response unsuccessful for city $cityId page $page")
                // Already emitted stale cache above if available
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Network error for city $cityId page $page: ${e.message}")
            // Already emitted stale cache above if available
            if (dbLoads.isEmpty()) {
                // No cache available, propagate error
                throw e
            }
        }
    }.flowOn(ioDispatcher)
    
    /**
     * Helper to convert CityEntity to domain summary without loads.
     */
    private suspend fun CityEntity.toDomainSummary(): CityLoadSummary {
        val loads = dao.getLoadsForCity(cityId)
        return CityLoadSummary(
            cityId = cityId,
            cityName = cityName,
            totalLoads = totalLoads,
            initialLoads = loads.map { it.toDomain() }
        )
    }
    
    companion object {
        private const val TAG = "LoadBoardRepo"
    }
}
