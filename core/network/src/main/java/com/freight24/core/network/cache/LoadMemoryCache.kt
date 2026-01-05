package com.freight24.core.network.cache

import android.util.LruCache
import com.freight24.core.common.model.CityLoadSummary
import com.freight24.core.common.model.Load
import com.freight24.core.common.util.CacheConstants
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * L1 In-Memory Cache for ultra-fast data access.
 * 
 * This cache provides instant (0ms) access to frequently accessed data during the user's session.
 * It's cleared on logout and uses LRU eviction to prevent excessive memory usage.
 * 
 * Cache Hierarchy:
 * - L1 (this class): In-memory, fastest, session-scoped
 * - L2 (Room): Persistent, offline-capable
 * - L3 (Network): Source of truth
 */
@Singleton
class LoadMemoryCache @Inject constructor() {
    
    // City summary cache with LRU eviction
    private val citySummaryCache = LruCache<Int, CityLoadSummary>(CacheConstants.MEMORY_CACHE_MAX_CITIES)
    
    // Loads cache: cityId -> (page -> List<Load>)
    private val loadsCache = ConcurrentHashMap<Int, MutableMap<Int, List<Load>>>()
    
    // Timestamps for TTL tracking
    private val timestamps = ConcurrentHashMap<String, Long>()
    
    /* ---------- City Summary Cache ---------- */
    
    /**
     * Get cached city summary if fresh.
     * @return List of cities or null if cache miss/expired
     */
    fun getCitySummary(): List<CityLoadSummary>? {
        if (isExpired(CITY_SUMMARY_KEY)) return null
        
        val snapshot = citySummaryCache.snapshot()
        return if (snapshot.isEmpty()) null else snapshot.values.toList()
    }
    
    /**
     * Cache city summary data.
     */
    fun putCitySummary(data: List<CityLoadSummary>) {
        data.forEach { city ->
            citySummaryCache.put(city.cityId, city)
        }
        timestamps[CITY_SUMMARY_KEY] = System.currentTimeMillis()
    }
    
    /* ---------- Loads Cache ---------- */
    
    /**
     * Get cached loads for a specific city and page.
     * @return List of loads or null if cache miss/expired
     */
    fun getLoadsForCity(cityId: Int, page: Int): List<Load>? {
        if (isExpired(getLoadsCacheKey(cityId))) return null
        return loadsCache[cityId]?.get(page)
    }
    
    /**
     * Cache loads for a specific city and page.
     */
    fun putLoadsForCity(cityId: Int, page: Int, loads: List<Load>) {
        loadsCache.getOrPut(cityId) { mutableMapOf() }[page] = loads
        timestamps[getLoadsCacheKey(cityId)] = System.currentTimeMillis()
    }
    
    /**
     * Get all cached loads for a city (all pages).
     */
    fun getAllLoadsForCity(cityId: Int): List<Load>? {
        if (isExpired(getLoadsCacheKey(cityId))) return null
        
        return loadsCache[cityId]?.values?.flatten()
    }
    
    /* ---------- Cache Management ---------- */
    
    /**
     * Clear all caches (call on logout).
     */
    fun clear() {
        citySummaryCache.evictAll()
        loadsCache.clear()
        timestamps.clear()
    }
    
    /**
     * Clear cache for a specific city.
     */
    fun clearCity(cityId: Int) {
        citySummaryCache.remove(cityId)
        loadsCache.remove(cityId)
        timestamps.remove(getLoadsCacheKey(cityId))
    }
    
    /* ---------- Private Helpers ---------- */
    
    private fun isExpired(key: String): Boolean {
        val timestamp = timestamps[key] ?: return true
        val age = System.currentTimeMillis() - timestamp
        return age > CacheConstants.LOADS_TTL_MS
    }
    
    private fun getLoadsCacheKey(cityId: Int) = "loads_$cityId"
    
    companion object {
        private const val CITY_SUMMARY_KEY = "city_summary"
    }
}
