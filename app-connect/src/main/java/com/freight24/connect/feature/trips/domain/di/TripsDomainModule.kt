package com.freight24.connect.feature.trips.domain.di

import com.freight24.connect.feature.trips.domain.usecase.CreateTripUseCase
import com.freight24.connect.feature.trips.domain.usecase.CreateTripUseCaseImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal abstract class TripsDomainModule {

    @Binds
    abstract fun bindCreateTripUseCase(useCaseImpl: CreateTripUseCaseImpl): CreateTripUseCase
}