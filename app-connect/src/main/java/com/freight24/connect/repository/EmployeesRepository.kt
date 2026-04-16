package com.freight24.connect.repository

import com.freight24.rest.response.DashboardDataResponse.DashboardData
import com.freight24.rest.response.EmployeeBasicInfo

interface EmployeesRepository {
    suspend fun getDashboardData(): Result<DashboardData>
    suspend fun getEmployeeBasicInfo(userId: String): Result<EmployeeBasicInfo>
}