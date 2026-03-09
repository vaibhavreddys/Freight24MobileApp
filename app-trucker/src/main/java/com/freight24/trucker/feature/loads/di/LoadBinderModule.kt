package com.freight24.trucker.feature.loads.di

import com.freight24.trucker.feature.loads.data.repository.LoadBoardRepository
import com.freight24.trucker.feature.loads.data.repository.LoadBoardRepositoryImpl
import com.freight24.trucker.feature.loads.domain.usecase.CityLoadBoardUseCase
import com.freight24.trucker.feature.loads.domain.usecase.CityLoadBoardUseCaseImpl
import com.freight24.trucker.feature.loads.domain.usecase.ExpressInterestUseCase
import com.freight24.trucker.feature.loads.domain.usecase.ExpressInterestUseCaseImpl
import com.freight24.trucker.feature.loads.domain.usecase.LoadBoardUseCase
import com.freight24.trucker.feature.loads.domain.usecase.LoadBoardUseCaseImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface LoadBinderModule {
    @Binds
    @Singleton
    fun bindLoadRepository(impl: LoadBoardRepositoryImpl): LoadBoardRepository

    @Binds
    @Singleton
    fun bindLoadBoardUseCase(impl: LoadBoardUseCaseImpl): LoadBoardUseCase

    @Binds
    @Singleton
    fun bindCityLoadBoardUseCase(impl: CityLoadBoardUseCaseImpl): CityLoadBoardUseCase

    @Binds
    @Singleton
    fun bindExpressInterestUseCase(impl: ExpressInterestUseCaseImpl): ExpressInterestUseCase
}
