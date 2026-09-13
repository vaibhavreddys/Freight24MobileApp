package com.freight24.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "lead_truck_types")
data class LeadTruckTypeEntity(
    @PrimaryKey val id: Int,
    val truckType: String,
    val displayName: String,
    val cachedAt: Long,
)

@Entity(tableName = "lead_cities")
data class LeadCityEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val cachedAt: Long,
)
