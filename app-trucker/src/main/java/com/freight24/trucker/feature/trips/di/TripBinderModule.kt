package com.freight24.trucker.feature.trips.di

import com.freight24.trucker.feature.trips.data.repository.TripRepository
import com.freight24.trucker.feature.trips.data.repository.TripRepositoryImpl
import com.freight24.trucker.feature.trips.domain.usecase.FetchTripDetailUseCase
import com.freight24.trucker.feature.trips.domain.usecase.FetchTripDetailUseCaseImpl
import com.freight24.trucker.feature.trips.domain.usecase.FetchTripsUseCase
import com.freight24.trucker.feature.trips.domain.usecase.FetchTripsUseCaseImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal interface TripBinderModule {

    @Binds
    @Singleton
    fun bindTripRepository(impl: TripRepositoryImpl): TripRepository

    @Binds
    @Singleton
    fun bindFetchTripUseCase(impl: FetchTripsUseCaseImpl): FetchTripsUseCase

    @Binds
    @Singleton
    fun bindFetchTripDetailUseCase(impl: FetchTripDetailUseCaseImpl): FetchTripDetailUseCase

}