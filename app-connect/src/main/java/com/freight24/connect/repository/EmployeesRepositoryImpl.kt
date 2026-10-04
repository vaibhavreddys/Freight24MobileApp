package com.freight24.connect.repository

import com.freight24.common.di.IoDispatcher
import com.freight24.database.dao.EmployeeProfileDao
import com.freight24.database.entity.CachedEmployeeProfileEntity
import com.freight24.rest.api.EmployeesApi
import com.freight24.rest.response.DashboardDataResponse
import com.freight24.rest.response.EmployeeBasicInfo
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class EmployeesRepositoryImpl @Inject constructor(
    private val api: EmployeesApi,
    private val employeeProfileDao: EmployeeProfileDao,
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

    override suspend fun getEmployeeBasicInfo(userId: String): Result<EmployeeBasicInfo> =
        withContext(ioDispatcher) {
            try {
                val response = api.getEmployeeBasicInfo(userId)
                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!.data)
                } else {
                    Result.failure(Exception(response.message()))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun getEmployeeProfile(userId: String): Result<EmployeeBasicInfo> =
        withContext(ioDispatcher) {
            readCachedProfile(userId)?.let { return@withContext Result.success(it) }

            val fetched = getEmployeeBasicInfo(userId)
            fetched.onSuccess { info -> writeCachedProfile(userId, info) }
            fetched
        }

    override suspend fun clearEmployeeProfileCache() {
        withContext(ioDispatcher) {
            runCatching { employeeProfileDao.clearAll() }
        }
    }

    private suspend fun readCachedProfile(userId: String): EmployeeBasicInfo? =
        runCatching { employeeProfileDao.get(userId) }
            .getOrNull()
            ?.let {
                EmployeeBasicInfo(
                    userId = it.userId,
                    fullName = it.fullName,
                    designation = it.designation,
                    status = it.status,
                    tagCode = it.tagCode,
                    department = it.department,
                    phone = it.phone,
                    email = it.email,
                )
            }

    private suspend fun writeCachedProfile(userId: String, info: EmployeeBasicInfo) {
        runCatching {
            employeeProfileDao.upsert(
                CachedEmployeeProfileEntity(
                    userId = userId,
                    fullName = info.fullName,
                    designation = info.designation,
                    status = info.status,
                    tagCode = info.tagCode,
                    department = info.department,
                    phone = info.phone,
                    email = info.email,
                    cachedAt = System.currentTimeMillis(),
                )
            )
        }
    }
}
