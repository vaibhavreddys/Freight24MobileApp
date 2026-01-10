package com.freight24.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.freight24.database.entity.CityEntity
import com.freight24.database.entity.CityPaginationEntity
import com.freight24.database.entity.LoadEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LoadBoardDao {
    // ---------- Cities ----------

    /**
     * Observe cities that haven't expired yet (reactive).
     * Automatically updates UI when data changes.
     */
    @Query("SELECT * FROM cities WHERE expiresAt > :now ORDER BY cityName")
    fun observeCities(now: Long = System.currentTimeMillis()): Flow<List<CityEntity>>

    /**
     * Get all cities (one-shot, includes expired).
     */
    @Query("SELECT * FROM cities ORDER BY cityName")
    suspend fun getCities(): List<CityEntity>

    /**
     * Check if we have fresh (non-expired) cities in cache.
     */
    @Query("SELECT COUNT(*) > 0 FROM cities WHERE expiresAt > :now")
    suspend fun hasFreshCities(now: Long = System.currentTimeMillis()): Boolean

    /**
     * Atomic refresh: clear old cities and insert new ones.
     */
    @Transaction
    suspend fun refreshCities(cities: List<CityEntity>) {
        clearCities()
        upsertCities(cities)
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCities(cities: List<CityEntity>): List<Long>

    @Query("DELETE FROM cities")
    suspend fun clearCities()

    // ---------- Loads ----------

    /**
     * Observe all loads for a city (reactive).
     */
    @Query(
        """
        SELECT * FROM loads 
        WHERE cityId = :cityId 
        ORDER BY createdTime DESC
    """,
    )
    fun observeLoadsForCity(cityId: Int): Flow<List<LoadEntity>>

    /**
     * Get loads for a specific city and page.
     */
    @Query(
        """
        SELECT * FROM loads 
        WHERE cityId = :cityId AND page = :page
        ORDER BY createdTime DESC
    """,
    )
    suspend fun getLoadsForCityPage(
        cityId: Int,
        page: Int,
    ): List<LoadEntity>

    /**
     * Get all loads for a city (all pages).
     */
    @Query(
        """
        SELECT * FROM loads 
        WHERE cityId = :cityId 
        ORDER BY createdTime DESC
    """,
    )
    suspend fun getLoadsForCity(cityId: Int): List<LoadEntity>

    /**
     * Atomic refresh for a city's loads.
     * If page 1, clears all existing loads for that city.
     */
    @Transaction
    suspend fun refreshLoadsForCity(
        cityId: Int,
        page: Int,
        loads: List<LoadEntity>,
    ) {
        if (page == 1) {
            clearLoadsForCity(cityId)
        }
        upsertLoads(loads)
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertLoads(loads: List<LoadEntity>): List<Long>

    @Query("DELETE FROM loads WHERE cityId = :cityId")
    suspend fun clearLoadsForCity(cityId: Int): Int

    // ---------- Pagination ----------

    @Query("SELECT * FROM city_pagination WHERE cityId = :cityId")
    suspend fun getPagination(cityId: Int): CityPaginationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPagination(pagination: CityPaginationEntity): Long
}
