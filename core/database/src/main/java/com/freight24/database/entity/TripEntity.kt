package com.freight24.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Cached copy of one row from `GET /api/v1/trips/trucker/list`.
 *
 * The Trips tab paints from here first and refreshes from the network in
 * parallel, so the manifest is on screen before the round trip finishes.
 *
 * Keyed by [ownerId] as well as [id]: trips belong to one truck owner, so a
 * second account signing in on the same device can never read the previous
 * owner's trips out of the cache.
 */
@Entity(
    tableName = "trips",
    indices = [
        // Serves both the owner's filter and the newest-first ordering.
        Index(value = ["ownerId", "createdAt"]),
    ],
)
data class TripEntity(
    @PrimaryKey val id: String,
    val ownerId: String,
    val registrationNumber: String,
    val truckType: String,
    val status: String,
    val createdAt: String,
    val tripCost: Double,
    val totalPaidAmount: Double,
    val origin: String,
    val destination: String,
    val cachedAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "trips_pagination")
data class TripsPaginationEntity(
    @PrimaryKey val id: String = "trips_sync",
    val lastPage: Int,
    val hasMore: Boolean,
    val totalTrips: Int,
    val lastFetchedAt: Long = System.currentTimeMillis(),
)
