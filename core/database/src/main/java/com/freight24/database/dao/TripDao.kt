package com.freight24.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.freight24.database.entity.TripEntity
import com.freight24.database.entity.TripsPaginationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TripDao {
    @Query("SELECT * FROM trips ORDER BY createdAt DESC")
    fun observeTrips(): Flow<List<TripEntity>>

    @Query("SELECT * FROM trips WHERE page = :page ORDER BY createdAt DESC")
    suspend fun getTripsByPage(page: Int): List<TripEntity>

    @Query("SELECT * FROM trips ORDER BY createdAt DESC")
    suspend fun getAllTrips(): List<TripEntity>

    @Transaction
    suspend fun refreshTrips(
        page: Int,
        trips: List<TripEntity>,
    ) {
        if (page == 1) {
            clearTrips()
        }
        insertTrips(trips)
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrips(trips: List<TripEntity>)

    @Query("DELETE FROM trips")
    suspend fun clearTrips()

    @Query("SELECT * FROM trips_pagination LIMIT 1")
    suspend fun getPagination(): TripsPaginationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPagination(pagination: TripsPaginationEntity)
}
