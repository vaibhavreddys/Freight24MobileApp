package com.freight24.connect.feature.login.di

import com.freight24.connect.feature.login.domain.usecase.LoginConnectUseCase
import com.freight24.connect.feature.login.domain.usecase.LoginConnectUseCaseImpl
import com.freight24.connect.feature.login.domain.usecase.VerifyOtpUseCase
import com.freight24.connect.feature.login.domain.usecase.VerifyOtpUseCaseImpl
import com.freight24.connect.feature.login.repository.LoginConnectRepository
import com.freight24.connect.feature.login.repository.LoginConnectRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal interface LoginBinderModule {

    @Binds
    @Singleton
    fun bindLoginConnectRepository(impl: LoginConnectRepositoryImpl): LoginConnectRepository

    @Binds
    @Singleton
    fun bindLoginConnectUseCase(impl: LoginConnectUseCaseImpl): LoginConnectUseCase

    @Binds
    @Singleton
    fun bindVerifyOtpUseCase(impl: VerifyOtpUseCaseImpl): VerifyOtpUseCase


}