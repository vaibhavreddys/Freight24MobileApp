package com.freight24.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.freight24.database.entity.LeadCityEntity
import com.freight24.database.entity.LeadTruckTypeEntity

@Dao
interface LeadsCacheDao {
    @Query("SELECT * FROM lead_truck_types WHERE cachedAt >= :minCachedAt ORDER BY displayName")
    suspend fun getTruckTypes(minCachedAt: Long): List<LeadTruckTypeEntity>

    @Upsert
    suspend fun upsertTruckTypes(types: List<LeadTruckTypeEntity>)

    @Query("SELECT * FROM lead_cities WHERE cachedAt >= :minCachedAt ORDER BY name")
    suspend fun getCities(minCachedAt: Long): List<LeadCityEntity>

    @Query("SELECT * FROM lead_cities WHERE name LIKE '%' || :query || '%' COLLATE NOCASE ORDER BY name LIMIT 20")
    suspend fun searchCachedCities(query: String): List<LeadCityEntity>

    @Upsert
    suspend fun upsertCities(cities: List<LeadCityEntity>)
}
