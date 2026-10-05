package com.freight24.connect.feature.dashboard.di

import com.freight24.connect.feature.dashboard.domain.viewmodel.DashboardViewModel
import com.freight24.connect.feature.dashboard.domain.viewmodel.DashboardViewModelImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
abstract class DashboardDomainModule {
    @Binds
    abstract fun bindDashboardViewModel(impl: DashboardViewModelImpl): DashboardViewModel
}
