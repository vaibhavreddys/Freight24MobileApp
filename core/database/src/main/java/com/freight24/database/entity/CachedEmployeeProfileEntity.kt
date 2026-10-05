package com.freight24.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Cached copy of `GET /api/v1/employees/{id}/basic-info`.
 *
 * The More desk is opened repeatedly and this payload (name, designation, tag code)
 * does not change within a session, so it is fetched once per login and read from
 * here afterwards.
 *
 * Keyed by `userId` rather than being a singleton row: a different account can never
 * read another account's profile even if the row outlives a logout.
 */
@Entity(tableName = "cached_employee_profiles")
data class CachedEmployeeProfileEntity(
    @PrimaryKey val userId: String,
    val fullName: String,
    val designation: String?,
    val status: String,
    val tagCode: String,
    val department: String?,
    val phone: String?,
    val email: String?,
    val cachedAt: Long,
)
