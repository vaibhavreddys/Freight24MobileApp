package com.freight24.core.common.util

/**
 * Centralized cache configuration constants.
 * 
 * These values control the behavior of the three-tier caching system:
 * - L1: In-memory cache (fastest, session-scoped)
 * - L2: Room database (persistent, offline-capable)
 * - L3: Network API (source of truth)
 */
object CacheConstants {
    
    /**
     * Time-to-live for city summary cache (5 minutes).
     * After this duration, cache is considered stale and will be refreshed from network.
     */
    const val CITY_SUMMARY_TTL_MS = 5 * 60 * 1000L // 5 minutes
    
    /**
     * Time-to-live for load data cache (5 minutes).
     * Balances freshness with API call reduction.
     */
    const val LOADS_TTL_MS = 5 * 60 * 1000L // 5 minutes
    
    /**
     * Maximum number of cities to keep in memory cache.
     * Uses LRU eviction when limit is reached.
     */
    const val MEMORY_CACHE_MAX_CITIES = 100
    
    /**
     * Maximum number of load items to keep in memory cache.
     * Prevents excessive memory usage.
     */
    const val MEMORY_CACHE_MAX_LOADS = 1000
    
    /**
     * Page size for paginated load requests.
     */
    const val LOADS_PAGE_SIZE = 10
}
