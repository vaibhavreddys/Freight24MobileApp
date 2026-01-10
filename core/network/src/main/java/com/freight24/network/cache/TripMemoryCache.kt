package com.freight24.network.cache

import com.freight24.common.model.Trip
import com.freight24.common.util.CacheConstants
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TripMemoryCache
    @Inject
    constructor() {
        // page -> List<Trip>
        private val tripsCache = ConcurrentHashMap<Int, List<Trip>>()

        // timestamp
        private var lastFetchedAt: Long = 0

        fun getTrips(page: Int): List<Trip>? {
            if (isExpired()) return null
            return tripsCache[page]
        }

        fun putTrips(
            page: Int,
            trips: List<Trip>,
        ) {
            tripsCache[page] = trips
            lastFetchedAt = System.currentTimeMillis()
        }

        fun clear() {
            tripsCache.clear()
            lastFetchedAt = 0
        }

        private fun isExpired(): Boolean {
            val age = System.currentTimeMillis() - lastFetchedAt
            return age > CacheConstants.LOADS_TTL_MS // Reuse loads TTL
        }
    }
