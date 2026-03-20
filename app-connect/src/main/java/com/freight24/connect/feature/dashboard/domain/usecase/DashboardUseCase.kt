package com.freight24.connect.feature.dashboard.domain.usecase

import com.freight24.common.usecase.EmptyUseCaseParams
import com.freight24.common.usecase.UseCase
import com.freight24.connect.repository.EmployeesRepository
import com.freight24.rest.response.DashboardDataResponse
import javax.inject.Inject

interface DashboardUseCase : UseCase<EmptyUseCaseParams, DashboardDataResponse.DashboardData>

class DashboardUseCaseImpl @Inject constructor(
    private val repository: EmployeesRepository
) : DashboardUseCase {

    override suspend fun invoke(
        params: EmptyUseCaseParams
    ): Result<DashboardDataResponse.DashboardData> {
        return repository.getDashboardData()
    }

}