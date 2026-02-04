package com.freight24.connect.feature.indents.di

import com.freight24.connect.feature.indents.domain.usecases.AssignIndentUseCase
import com.freight24.connect.feature.indents.domain.usecases.AssignIndentUseCaseImpl
import com.freight24.connect.feature.indents.domain.usecases.CreateIndentUseCase
import com.freight24.connect.feature.indents.domain.usecases.CreateIndentUseCaseImpl
import com.freight24.connect.feature.indents.domain.usecases.GetClientsUseCase
import com.freight24.connect.feature.indents.domain.usecases.GetClientsUseCaseImpl
import com.freight24.connect.feature.indents.domain.usecases.GetIndentByIdUseCase
import com.freight24.connect.feature.indents.domain.usecases.GetIndentByIdUseCaseImpl
import com.freight24.connect.feature.indents.domain.usecases.GetLocationsUseCase
import com.freight24.connect.feature.indents.domain.usecases.GetLocationsUseCaseImpl
import com.freight24.connect.feature.indents.domain.usecases.GetMyIndentsUseCase
import com.freight24.connect.feature.indents.domain.usecases.GetMyIndentsUseCaseImpl
import com.freight24.connect.feature.indents.domain.usecases.GetTruckTypesUseCase
import com.freight24.connect.feature.indents.domain.usecases.GetTruckTypesUseCaseImpl
import com.freight24.connect.feature.indents.domain.usecases.UpdateIndentUseCase
import com.freight24.connect.feature.indents.domain.usecases.UpdateIndentUseCaseImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
abstract class IndentsDomainModule {
    @Binds
    abstract fun bindGetLocationsUseCase(useCaseImpl: GetLocationsUseCaseImpl): GetLocationsUseCase

    @Binds
    abstract fun bindGetClientsUseCase(useCaseImpl: GetClientsUseCaseImpl): GetClientsUseCase

    @Binds
    abstract fun bindGetTruckTypesUseCase(useCaseImpl: GetTruckTypesUseCaseImpl): GetTruckTypesUseCase

    @Binds
    abstract fun bindCreateIndentUseCase(useCaseImpl: CreateIndentUseCaseImpl): CreateIndentUseCase

    @Binds
    abstract fun bindGetMyIndentsUseCase(useCaseImpl: GetMyIndentsUseCaseImpl): GetMyIndentsUseCase

    @Binds
    abstract fun bindGetIndentByIdUseCase(useCaseImpl: GetIndentByIdUseCaseImpl): GetIndentByIdUseCase

    @Binds
    abstract fun bindAssignIndentUseCase(useCaseImpl: AssignIndentUseCaseImpl): AssignIndentUseCase

    @Binds
    abstract fun bindUpdateIndentUseCase(useCaseImpl: UpdateIndentUseCaseImpl): UpdateIndentUseCase
}
