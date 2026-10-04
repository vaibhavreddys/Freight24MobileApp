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
    @Query("SELECT * FROM trips WHERE ownerId = :ownerId ORDER BY createdAt DESC")
    fun observeTrips(ownerId: String): Flow<List<TripEntity>>

    /**
     * Page 1 is the driver's whole manifest, so it replaces the owner's cached
     * rows; later pages append.
     */
    @Transaction
    suspend fun refreshTrips(
        ownerId: String,
        page: Int,
        trips: List<TripEntity>,
    ) {
        if (page == 1) {
            clearTrips(ownerId)
        }
        insertTrips(trips)
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrips(trips: List<TripEntity>)

    @Query("DELETE FROM trips WHERE ownerId = :ownerId")
    suspend fun clearTrips(ownerId: String)

    @Query("SELECT * FROM trips_pagination LIMIT 1")
    suspend fun getPagination(): TripsPaginationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPagination(pagination: TripsPaginationEntity)
}
