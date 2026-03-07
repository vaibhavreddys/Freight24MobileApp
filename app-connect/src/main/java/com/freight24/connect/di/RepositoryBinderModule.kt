package com.freight24.connect.di

import com.freight24.connect.repository.ClientRepository
import com.freight24.connect.repository.ClientRepositoryImpl
import com.freight24.connect.repository.LocationRepository
import com.freight24.connect.repository.LocationRepositoryImpl
import com.freight24.connect.repository.trips.TripHaltingRepository
import com.freight24.connect.repository.trips.TripHaltingRepositoryImpl
import com.freight24.connect.repository.trips.TripRepository
import com.freight24.connect.repository.trips.TripRepositoryImpl
import com.freight24.connect.repository.TruckRepository
import com.freight24.connect.repository.TruckRepositoryImpl
import com.freight24.connect.repository.trips.TripLUChargesRepository
import com.freight24.connect.repository.trips.TripLUChargesRepositoryImpl
import com.freight24.connect.repository.TruckOwnerRepository
import com.freight24.connect.repository.TruckOwnerRepositoryImpl
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

    @Binds
    @Singleton
    fun bindsTripLUChargesRepository(
        tripLUChargesRepositoryImpl: TripLUChargesRepositoryImpl
    ): TripLUChargesRepository

    @Binds
    @Singleton
    fun bindsTruckOwnerRepository(
        truckOwnerRepositoryImpl: TruckOwnerRepositoryImpl
    ): TruckOwnerRepository
}
