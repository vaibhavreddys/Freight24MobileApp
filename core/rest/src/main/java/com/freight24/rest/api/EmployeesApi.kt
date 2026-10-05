package com.freight24.rest.api

import com.freight24.rest.response.DashboardDataResponse
import com.freight24.rest.response.EmployeeBasicInfoResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface EmployeesApi {
    @GET("employees/dashboard")
    suspend fun getDashboardData(): Response<DashboardDataResponse>

    @GET("employees/{userId}/basic-info")
    suspend fun getEmployeeBasicInfo(
        @Path("userId") userId: String,
    ): Response<EmployeeBasicInfoResponse>
}
