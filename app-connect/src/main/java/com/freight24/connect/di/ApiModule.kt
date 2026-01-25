package com.freight24.connect.di

import com.freight24.connect.BuildConfig
import com.freight24.network.RetrofitFactory
import com.freight24.network.di.DefaultNetwork
import com.freight24.rest.api.IndentsApi
import com.freight24.rest.api.LoginConnectApi
import com.freight24.rest.api.NotificationServiceApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object ApiModule {

    @Provides
    fun providesLoginConnectApi(
        @DefaultNetwork retrofitFactory: RetrofitFactory,
    ): LoginConnectApi {
      return retrofitFactory
          .create(BuildConfig.BASE_URL, "ConnectLoginService")
          .create(LoginConnectApi::class.java)
    }

    @Provides
    fun providesNotificationServiceApi(retrofitFactory: RetrofitFactory): NotificationServiceApi {
        return retrofitFactory
            .create(BuildConfig.BASE_URL, "NotificationServiceApi")
            .create(NotificationServiceApi::class.java)
    }


}