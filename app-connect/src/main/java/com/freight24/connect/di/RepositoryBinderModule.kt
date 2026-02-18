package com.freight24.connect.di

import com.freight24.connect.repository.ClientRepository
import com.freight24.connect.repository.ClientRepositoryImpl
import com.freight24.connect.repository.LocationRepository
import com.freight24.connect.repository.LocationRepositoryImpl
import com.freight24.connect.repository.TripHaltingRepository
import com.freight24.connect.repository.TripHaltingRepositoryImpl
import com.freight24.connect.repository.TripRepository
import com.freight24.connect.repository.TripRepositoryImpl
import com.freight24.connect.repository.TruckRepository
import com.freight24.connect.repository.TruckRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface RepositoryBinderModule {
    @Binds
    @Singleton
    fun bindsLocationRepository(locationRepositoryImpl: LocationRepositoryImpl): LocationRepository

    @Binds
    @Singleton
    fun bindsClientRepository(clientRepositoryImpl: ClientRepositoryImpl): ClientRepository

    @Binds
    @Singleton
    fun bindsTruckRepository(truckRepositoryImpl: TruckRepositoryImpl): TruckRepository

    @Binds
    @Singleton
    fun bindsTripRepository(tripRepositoryImpl: TripRepositoryImpl): TripRepository

    @Binds
    @Singleton
    fun bindsTripHaltingRepository(
        tripHaltingRepositoryImpl: TripHaltingRepositoryImpl
    ): TripHaltingRepository
}
