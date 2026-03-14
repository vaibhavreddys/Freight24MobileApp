package com.freight24.connect.feature.clients.di

import com.freight24.connect.feature.clients.domain.usecases.GetClientsUseCase
import com.freight24.connect.feature.clients.domain.usecases.GetClientsUseCaseImpl
import com.freight24.connect.feature.clients.domain.usecases.GetClientByUserIdUseCase
import com.freight24.connect.feature.clients.domain.usecases.GetClientByUserIdUseCaseImpl
import com.freight24.connect.feature.clients.domain.usecases.CreateClientUseCase
import com.freight24.connect.feature.clients.domain.usecases.CreateClientUseCaseImpl
import com.freight24.connect.feature.clients.domain.usecases.UpdateClientUseCase
import com.freight24.connect.feature.clients.domain.usecases.UpdateClientUseCaseImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class ClientsDomainModule {
    @Binds
    abstract fun bindGetClientsUseCase(useCaseImpl: GetClientsUseCaseImpl): GetClientsUseCase

    @Binds
    abstract fun bindGetClientByUserIdUseCase(
        useCaseImpl: GetClientByUserIdUseCaseImpl,
    ): GetClientByUserIdUseCase

    @Binds
    abstract fun bindCreateClientUseCase(
        useCaseImpl: CreateClientUseCaseImpl,
    ): CreateClientUseCase

    @Binds
    abstract fun bindUpdateClientUseCase(
        useCaseImpl: UpdateClientUseCaseImpl,
    ): UpdateClientUseCase
}
