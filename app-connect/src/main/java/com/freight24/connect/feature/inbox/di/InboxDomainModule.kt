package com.freight24.connect.feature.inbox.di

import com.freight24.connect.feature.inbox.domain.usecase.InboxUseCase
import com.freight24.connect.feature.inbox.domain.usecase.InboxUseCaseImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class InboxDomainModule {

    @Binds
    abstract fun bindInboxUseCase(useCaseImpl: InboxUseCaseImpl): InboxUseCase

}