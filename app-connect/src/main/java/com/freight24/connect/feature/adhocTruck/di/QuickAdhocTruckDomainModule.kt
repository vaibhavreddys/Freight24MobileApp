package com.freight24.connect.feature.adhocTruck.di

import com.freight24.connect.feature.adhocTruck.domain.usecase.QuickAdhocTruckUseCase
import com.freight24.connect.feature.adhocTruck.domain.usecase.QuickAdhocTruckUseCaseImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal abstract class QuickAdhocTruckDomainModule {
    @Binds
    abstract fun bindQuickAdhocTruckUseCase(useCaseImpl: QuickAdhocTruckUseCaseImpl): QuickAdhocTruckUseCase
}
