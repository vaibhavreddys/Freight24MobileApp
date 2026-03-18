package com.freight24.connect.feature.home.di

import com.freight24.connect.repository.FcmRepository
import com.freight24.connect.repository.FcmRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal interface HomeBinderModule {

    @Binds
    @Singleton
    fun bindFcmRepository(impl: FcmRepositoryImpl): FcmRepository

}
