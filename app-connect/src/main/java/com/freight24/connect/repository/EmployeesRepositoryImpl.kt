package com.freight24.connect.repository

import com.freight24.common.di.IoDispatcher
import com.freight24.rest.api.EmployeesApi
import com.freight24.rest.response.DashboardDataResponse
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class EmployeesRepositoryImpl @Inject constructor(
    private val api: EmployeesApi,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : EmployeesRepository {

    override suspend fun getDashboardData(): Result<DashboardDataResponse.DashboardData> =
        withContext(ioDispatcher) {
            try {
                val response = api.getDashboardData()
                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!.data)
                } else {
                    Result.failure(Exception(response.message()))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

}