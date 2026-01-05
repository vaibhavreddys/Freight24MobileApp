package com.freight24.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "city_pagination")
data class CityPaginationEntity(
    @PrimaryKey val cityId: Int,
    val currentPage: Int,
    val totalPages: Int,
    val hasMore: Boolean,
    val totalLoads: Int,
    val lastFetchedAt: Long = System.currentTimeMillis()
)
