package com.freight24.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "loads",
    indices = [
        Index(value = ["cityId", "page"]),
        Index(value = ["createdTime"])
    ]
)
data class LoadEntity(
    @PrimaryKey val id: String,
    val cityId: Int,

    val sourceCity: String,
    val destinationCity: String,
    val material: String,
    val weight: String,
    val truckInfo: String,
    val price: String,
    val createdTime: String,

    val agentName: String,
    val agentPhone: String,
    
    val cachedAt: Long = System.currentTimeMillis(),
    val page: Int = 1
)
