package com.freight24.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "trips",
    indices = [
        Index(value = ["page"]),
        Index(value = ["status"]),
    ],
)
data class TripEntity(
    @PrimaryKey val id: String,
    val registrationNumber: String,
    val truckType: String,
    val status: String,
    val createdAt: String,
    val tripCost: Double,
    val advanceAmount: Double,
    val origin: String,
    val destination: String,
    val cachedAt: Long = System.currentTimeMillis(),
    val page: Int = 1,
)

@Entity(tableName = "trips_pagination")
data class TripsPaginationEntity(
    @PrimaryKey val id: String = "trips_sync",
    val lastPage: Int,
    val hasMore: Boolean,
    val totalTrips: Int,
    val lastFetchedAt: Long = System.currentTimeMillis(),
)
