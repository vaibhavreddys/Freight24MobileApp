package com.freight24.connect.feature.trucks.di

import com.freight24.connect.feature.trucks.domain.usecases.GetTruckByIdUseCase
import com.freight24.connect.feature.trucks.domain.usecases.GetTruckByIdUseCaseImpl
import com.freight24.connect.feature.trucks.domain.usecases.SearchTruckUseCase
import com.freight24.connect.feature.trucks.domain.usecases.SearchTruckUseCaseImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class TrucksDomainModule {
    @Binds
    abstract fun bindSearchTruckUseCase(useCaseImpl: SearchTruckUseCaseImpl): SearchTruckUseCase

    @Binds
    abstract fun bindGetTruckByIdUseCase(useCaseImpl: GetTruckByIdUseCaseImpl): GetTruckByIdUseCase
}