package com.freight24.trucker.feature.login.di

import com.freight24.trucker.feature.login.data.repository.LoginRepository
import com.freight24.trucker.feature.login.data.repository.LoginRepositoryImpl
import com.freight24.trucker.feature.login.domain.usecase.LoginUseCase
import com.freight24.trucker.feature.login.domain.usecase.LoginUseCaseImpl
import com.freight24.trucker.feature.login.domain.usecase.VerifyOtpUseCase
import com.freight24.trucker.feature.login.domain.usecase.VerifyOtpUseCaseImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal interface LoginBinderModule {
    @Binds
    @Singleton
    fun bindLoginRepository(impl: LoginRepositoryImpl): LoginRepository

    @Binds
    @Singleton
    fun bindLoginUseCase(impl: LoginUseCaseImpl): LoginUseCase

    @Binds
    @Singleton
    fun bindVerifyOtpUseCase(impl: VerifyOtpUseCaseImpl): VerifyOtpUseCase
}
