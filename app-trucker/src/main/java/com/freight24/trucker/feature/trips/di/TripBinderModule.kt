package com.freight24.trucker.feature.trips.di

import com.freight24.trucker.feature.trips.data.repository.TripRepository
import com.freight24.trucker.feature.trips.data.repository.TripRepositoryImpl
import com.freight24.trucker.feature.trips.domain.usecase.FetchTripDetailUseCase
import com.freight24.trucker.feature.trips.domain.usecase.FetchTripDetailUseCaseImpl
import com.freight24.trucker.feature.trips.domain.usecase.FetchTripsUseCase
import com.freight24.trucker.feature.trips.domain.usecase.FetchTripsUseCaseImpl
import com.freight24.trucker.feature.trips.domain.usecase.UploadLrUseCase
import com.freight24.trucker.feature.trips.domain.usecase.UploadLrUseCaseImpl
import com.freight24.trucker.feature.trips.domain.usecase.GetSignedDocumentUseCase
import com.freight24.trucker.feature.trips.domain.usecase.GetSignedDocumentUseCaseImpl
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

    @Binds
    @Singleton
    fun bindUpdateTripStatusUseCase(impl: com.freight24.trucker.feature.trips.domain.usecase.UpdateTripStatusUseCaseImpl): com.freight24.trucker.feature.trips.domain.usecase.UpdateTripStatusUseCase

    @Binds
    @Singleton
    fun bindUploadLrUseCase(impl: UploadLrUseCaseImpl): UploadLrUseCase

    @Binds
    @Singleton
    fun bindGetSignedDocumentUseCase(impl: GetSignedDocumentUseCaseImpl): GetSignedDocumentUseCase



}
