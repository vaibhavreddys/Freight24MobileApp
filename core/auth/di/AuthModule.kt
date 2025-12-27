// core/auth/src/main/java/com/freight24/core/auth/di/AuthModule.kt

package com.freight24.core.auth.di

import android.content.Context
import com.freight24.core.auth.AuthDataStore
import com.freight24.core.network.api.AuthApi
import com.freight24.core.auth.AuthRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AuthModule {

    @Provides
    @Singleton
    fun provideAuthApi(retrofit: Retrofit): AuthApi =
        retrofit.create(AuthApi::class.java)

    @Provides
    @Singleton
    fun provideAuthRepository(
        authApi: AuthApi,
        authDataStore: AuthDataStore
    ): AuthRepository = AuthRepository(authApi, authDataStore)

    @Provides
    @Singleton
    fun provideAuthDataStore(@ApplicationContext context: Context): AuthDataStore =
        AuthDataStore(context)
}