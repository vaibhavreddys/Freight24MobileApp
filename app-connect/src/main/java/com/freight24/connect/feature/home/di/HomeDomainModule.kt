package com.freight24.connect.feature.home.di

import com.freight24.connect.feature.home.usecase.DashboardUseCase
import com.freight24.connect.feature.home.usecase.DashboardUseCaseImpl
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