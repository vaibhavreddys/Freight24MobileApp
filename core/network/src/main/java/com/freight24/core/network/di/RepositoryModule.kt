package com.freight24.core.network.di

import com.freight24.core.common.repository.LoadBoardRepository
import com.freight24.core.common.repository.TripRepository
import com.freight24.core.network.repository.LoadBoardRepositoryCachedImpl
import com.freight24.core.network.repository.TripRepositoryImpl
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
    abstract fun bindLoadBoardRepository(
        loadBoardRepositoryCachedImpl: LoadBoardRepositoryCachedImpl
    ): LoadBoardRepository

    @Binds
    @Singleton
    abstract fun bindTripRepository(
        tripRepositoryImpl: TripRepositoryImpl
    ): TripRepository
}
