package com.freight24.trucker.di

import com.freight24.auth.AuthInterceptor
import com.freight24.network.adapter.AuthNetworkConfig
import com.freight24.network.adapter.NetworkConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal class NetworkAdapterModule {
    @Provides
    @Singleton
    fun provideNetworkConfig() =
        NetworkConfig(
            logsEnabled = true,
            logsTag = "OkHttpApp",
            headers = mapOf(),
            timeout = 60L,
        )

    @Provides
    @Singleton
    fun providesAuthNetworkConfig(authInterceptor: AuthInterceptor) = AuthNetworkConfig(interceptor = authInterceptor)
}
