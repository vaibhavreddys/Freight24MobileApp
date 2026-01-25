package com.freight24.trucker.feature.trucks.di

import com.freight24.trucker.feature.trucks.data.repository.TruckRepository
import com.freight24.trucker.feature.trucks.data.repository.TruckRepositoryImpl
import com.freight24.trucker.feature.trucks.domain.usecase.AddTruckUseCase
import com.freight24.trucker.feature.trucks.domain.usecase.AddTruckUseCaseImpl
import com.freight24.trucker.feature.trucks.domain.usecase.FetchTruckDetailUseCase
import com.freight24.trucker.feature.trucks.domain.usecase.FetchTruckDetailUseCaseImpl
import com.freight24.trucker.feature.trucks.domain.usecase.FetchTrucksUseCase
import com.freight24.trucker.feature.trucks.domain.usecase.FetchTrucksUseCaseImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal interface TruckBinderModule {
    @Binds
    @Singleton
    fun bindTruckRepository(impl: TruckRepositoryImpl): TruckRepository

    @Binds
    @Singleton
    fun bindFetchTrucksUseCase(impl: FetchTrucksUseCaseImpl): FetchTrucksUseCase

    @Binds
    @Singleton
    fun bindFetchTruckDetailUseCase(impl: FetchTruckDetailUseCaseImpl): FetchTruckDetailUseCase

    @Binds
    @Singleton
    fun bindAddTruckUseCase(impl: AddTruckUseCaseImpl): AddTruckUseCase
}
