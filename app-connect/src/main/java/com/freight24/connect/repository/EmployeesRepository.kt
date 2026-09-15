package com.freight24.connect.repository

import com.freight24.rest.response.DashboardDataResponse.DashboardData
import com.freight24.rest.response.EmployeeBasicInfo

interface EmployeesRepository {
    suspend fun getDashboardData(): Result<DashboardData>

    /** Always hits the network. Used when the caller explicitly wants fresh data. */
    suspend fun getEmployeeBasicInfo(userId: String): Result<EmployeeBasicInfo>

    /**
     * Cache-first employee profile for the More desk.
     *
     * Basic info does not change within a session, so this reads the Room copy and only
     * calls `/employees/{id}/basic-info` the first time (or after the cache was cleared on
     * logout). A network failure still resolves if a cached row exists.
     */
    suspend fun getEmployeeProfile(userId: String): Result<EmployeeBasicInfo>

    /** Drops the cached profile so the next login fetches it again. */
    suspend fun clearEmployeeProfileCache()
}
