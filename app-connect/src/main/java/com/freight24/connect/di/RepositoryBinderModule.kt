package com.freight24.connect.di

import com.freight24.connect.repository.ClientRepository
import com.freight24.connect.repository.ClientRepositoryImpl
import com.freight24.connect.repository.LocationRepository
import com.freight24.connect.repository.LocationRepositoryImpl
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
    fun bindsClientRepository(indentRepositoryImpl: ClientRepositoryImpl): ClientRepository
}
