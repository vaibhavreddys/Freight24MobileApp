package com.freight24.network.di

import com.freight24.common.repository.TripRepository
import com.freight24.network.repository.TripRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindTripRepository(tripRepositoryImpl: TripRepositoryImpl): TripRepository
}
