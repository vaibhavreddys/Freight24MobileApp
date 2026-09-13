package com.freight24.connect.di

import com.freight24.connect.repository.ClientRepository
import com.freight24.connect.repository.ClientRepositoryImpl
import com.freight24.connect.repository.EmployeesRepository
import com.freight24.connect.repository.EmployeesRepositoryImpl
import com.freight24.connect.repository.FindTrucksRepository
import com.freight24.connect.repository.FindTrucksRepositoryImpl
import com.freight24.connect.repository.LeadsRepository
import com.freight24.connect.repository.LeadsRepositoryImpl
import com.freight24.connect.repository.InterestInboxRepository
import com.freight24.connect.repository.InterestInboxRepositoryImpl
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
import com.freight24.connect.repository.verificationrequests.VerificationRequestsRepository
import com.freight24.connect.repository.verificationrequests.VerificationRequestsRepositoryImpl
import com.freight24.connect.repository.AdhocTruckRepository
import com.freight24.connect.repository.AdhocTruckRepositoryImpl
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

    @Binds
    @Singleton
    fun bindsFindTrucksRepository(
        findTrucksRepositoryImpl: FindTrucksRepositoryImpl
    ): FindTrucksRepository

    @Binds
    @Singleton
    fun bindsEmployeesRepository(
        employeesRepository: EmployeesRepositoryImpl
    ): EmployeesRepository

    @Binds
    @Singleton
    fun bindsInterestInboxRepository(
        interestInboxRepository: InterestInboxRepositoryImpl
    ): InterestInboxRepository

    @Binds
    @Singleton
    fun bindsVerificationRequestsRepository(
        verificationRequestsRepositoryImpl: VerificationRequestsRepositoryImpl,
    ): VerificationRequestsRepository

    @Binds
    @Singleton
    fun bindsLeadsRepository(
        leadsRepositoryImpl: LeadsRepositoryImpl,
    ): LeadsRepository

    @Binds
    @Singleton
    fun bindsAdhocTruckRepository(
        adhocTruckRepositoryImpl: AdhocTruckRepositoryImpl,
    ): AdhocTruckRepository
}
