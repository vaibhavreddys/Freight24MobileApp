// core/auth/src/main/java/com/freight24/core/auth/di/AuthModule.kt

package com.freight24.core.auth.di

import com.freight24.core.auth.AuthDataStore
import com.freight24.core.auth.AuthInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import okhttp3.Interceptor

@Module
@InstallIn(SingletonComponent::class)
object AuthModule {

}