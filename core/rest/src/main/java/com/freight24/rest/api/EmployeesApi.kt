package com.freight24.rest.api

import com.freight24.rest.response.DashboardDataResponse
import retrofit2.Response
import retrofit2.http.GET

interface EmployeesApi {

    @GET("employees/dashboard")
    suspend fun getDashboardData(): Response<DashboardDataResponse>


}