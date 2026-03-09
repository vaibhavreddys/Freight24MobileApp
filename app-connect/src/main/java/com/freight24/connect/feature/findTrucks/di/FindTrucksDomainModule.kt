package com.freight24.connect.feature.findTrucks.di

import com.freight24.connect.feature.findTrucks.domain.usecases.FindTrucksUseCase
import com.freight24.connect.feature.findTrucks.domain.usecases.FindTrucksUseCaseImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal abstract class FindTrucksDomainModule {

    @Binds
    abstract fun bindFindTrucksUseCase(useCaseImpl: FindTrucksUseCaseImpl): FindTrucksUseCase
    

}