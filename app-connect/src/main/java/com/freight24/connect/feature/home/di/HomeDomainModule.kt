package com.freight24.connect.feature.home.di

import com.freight24.connect.feature.dashboard.domain.usecase.DashboardUseCase
import com.freight24.connect.feature.dashboard.domain.usecase.DashboardUseCaseImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal abstract class HomeDomainModule {
    @Binds
    abstract fun bindDashboardUseCase(dashboardUseCaseImpl: DashboardUseCaseImpl): DashboardUseCase
}
