package com.freight24.core.network.di

import com.freight24.core.common.repository.LoadBoardRepository
import com.freight24.core.network.repository.LoadBoardRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindLoadBoardRepository(
        impl: LoadBoardRepositoryImpl
    ): LoadBoardRepository
}
