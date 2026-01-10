package com.freight24.network.di

import com.freight24.network.provider.SafeCallProvider
import com.freight24.network.provider.SafeCallProviderImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal interface NetworkBindModule {
    @Binds
    fun bindsSafeCallProvider(safeCallProviderImpl: SafeCallProviderImpl): SafeCallProvider
}
