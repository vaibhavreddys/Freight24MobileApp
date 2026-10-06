package com.freight24.connect.feature.truckowners.di

import com.freight24.connect.feature.truckowners.domain.usecases.CreateTruckOwnerUseCase
import com.freight24.connect.feature.truckowners.domain.usecases.CreateTruckOwnerUseCaseImpl
import com.freight24.connect.feature.trucks.domain.usecases.SearchTruckOwnerOptionsUseCase
import com.freight24.connect.feature.trucks.domain.usecases.SearchTruckOwnerOptionsUseCaseImpl
import com.freight24.connect.feature.trucks.domain.usecases.ValidateTruckOwnerUseCase
import com.freight24.connect.feature.trucks.domain.usecases.ValidateTruckOwnerUseCaseImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class TruckOwnersDomainModule {
    @Binds
    abstract fun bindCreateTruckOwnerUseCase(useCaseImpl: CreateTruckOwnerUseCaseImpl): CreateTruckOwnerUseCase

    @Binds
    abstract fun bindSearchTruckOwnerOptionsUseCase(
        useCaseImpl: SearchTruckOwnerOptionsUseCaseImpl,
    ): SearchTruckOwnerOptionsUseCase

    @Binds
    abstract fun bindValidateTruckOwnerUseCase(
        useCaseImpl: ValidateTruckOwnerUseCaseImpl,
    ): ValidateTruckOwnerUseCase
}
