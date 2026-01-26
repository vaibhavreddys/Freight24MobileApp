package com.freight24.connect.feature.indents.di

import com.freight24.connect.feature.indents.domain.CreateIndentUseCase
import com.freight24.connect.feature.indents.domain.CreateIndentUseCaseImpl
import com.freight24.connect.feature.indents.domain.GetClientsUseCase
import com.freight24.connect.feature.indents.domain.GetClientsUseCaseImpl
import com.freight24.connect.feature.indents.domain.GetLocationsUseCase
import com.freight24.connect.feature.indents.domain.GetLocationsUseCaseImpl
import com.freight24.connect.feature.indents.domain.GetTruckTypesUseCase
import com.freight24.connect.feature.indents.domain.GetTruckTypesUseCaseImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
abstract class IndentsDomainModule {

    @Binds
    abstract fun bindGetLocationsUseCase(
        useCaseImpl: GetLocationsUseCaseImpl
    ): GetLocationsUseCase

    @Binds
    abstract fun bindGetClientsUseCase(
        useCaseImpl: GetClientsUseCaseImpl
    ): GetClientsUseCase

    @Binds
    abstract fun bindGetTruckTypesUseCase(
        useCaseImpl: GetTruckTypesUseCaseImpl
    ): GetTruckTypesUseCase

    @Binds
    abstract fun bindCreateIndentUseCase(
        useCaseImpl: CreateIndentUseCaseImpl
    ): CreateIndentUseCase
}
