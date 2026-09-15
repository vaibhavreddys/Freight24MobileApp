package com.freight24.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.freight24.database.entity.CachedEmployeeProfileEntity

@Dao
interface EmployeeProfileDao {
    @Query("SELECT * FROM cached_employee_profiles WHERE userId = :userId LIMIT 1")
    suspend fun get(userId: String): CachedEmployeeProfileEntity?

    @Upsert
    suspend fun upsert(profile: CachedEmployeeProfileEntity)

    @Query("DELETE FROM cached_employee_profiles")
    suspend fun clearAll()

    @Query("DELETE FROM cached_employee_profiles WHERE userId = :userId")
    suspend fun clear(userId: String)
}
