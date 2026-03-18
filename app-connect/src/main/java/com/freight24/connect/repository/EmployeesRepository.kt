package com.freight24.connect.repository

import com.freight24.rest.response.DashboardDataResponse.DashboardData

interface EmployeesRepository {
    suspend fun getDashboardData(): Result<DashboardData>
}