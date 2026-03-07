package com.freight24.trucker.feature.trucks.di

import com.freight24.trucker.feature.trucks.data.repository.TruckRepository
import com.freight24.trucker.feature.trucks.data.repository.TruckRepositoryImpl
import com.freight24.trucker.feature.trucks.domain.usecase.AddTruckUseCase
import com.freight24.trucker.feature.trucks.domain.usecase.AddTruckUseCaseImpl
import com.freight24.trucker.feature.trucks.domain.usecase.FetchTruckDetailUseCase
import com.freight24.trucker.feature.trucks.domain.usecase.FetchTruckDetailUseCaseImpl
import com.freight24.trucker.feature.trucks.domain.usecase.FetchTrucksUseCase
import com.freight24.trucker.feature.trucks.domain.usecase.FetchTrucksUseCaseImpl
import com.freight24.trucker.feature.trucks.domain.usecase.GetGenericSignedUrlUseCase
import com.freight24.trucker.feature.trucks.domain.usecase.GetGenericSignedUrlUseCaseImpl
import com.freight24.trucker.feature.trucks.domain.usecase.UpdateTruckDocumentsUseCase
import com.freight24.trucker.feature.trucks.domain.usecase.UpdateTruckDocumentsUseCaseImpl
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

    @Binds
    @Singleton
    fun bindUpdateTruckDocumentsUseCase(impl: UpdateTruckDocumentsUseCaseImpl): UpdateTruckDocumentsUseCase

    @Binds
    @Singleton
    fun bindGetGenericSignedUrlUseCase(impl: GetGenericSignedUrlUseCaseImpl): GetGenericSignedUrlUseCase

    @Binds
    @Singleton
    fun bindUpdateTruckUseCase(impl: com.freight24.trucker.feature.trucks.domain.usecase.UpdateTruckUseCaseImpl): com.freight24.trucker.feature.trucks.domain.usecase.UpdateTruckUseCase
}
