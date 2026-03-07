package com.freight24.connect.feature.trucks.di

import com.freight24.connect.feature.trucks.domain.usecases.*
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

    @Binds
    abstract fun bindGetTrucksUseCase(useCaseImpl: GetTrucksUseCaseImpl): GetTrucksUseCase

    @Binds
    abstract fun bindAddTruckUseCase(useCaseImpl: AddTruckUseCaseImpl): AddTruckUseCase

    @Binds
    abstract fun bindGetTruckOwnersUseCase(useCaseImpl: GetTruckOwnersUseCaseImpl): GetTruckOwnersUseCase

    @Binds
    abstract fun bindUpdateTruckUseCase(useCaseImpl: UpdateTruckUseCaseImpl): UpdateTruckUseCase
}