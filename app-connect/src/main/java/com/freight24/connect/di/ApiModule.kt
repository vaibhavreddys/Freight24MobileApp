package com.freight24.connect.di

import android.util.Log
import com.freight24.connect.BuildConfig
import com.freight24.network.RetrofitFactory
import com.freight24.network.di.DefaultNetwork
import com.freight24.rest.api.ClientApi
import com.freight24.rest.api.FindTrucksApi
import com.freight24.rest.api.IndentsApi
import com.freight24.rest.api.LocationApi
import com.freight24.rest.api.LoginConnectApi
import com.freight24.rest.api.NotificationServiceApi
import com.freight24.rest.api.TripApi
import com.freight24.rest.api.TripHaltingApi
import com.freight24.rest.api.TripLUChargesApi
import com.freight24.rest.api.TruckApi
import com.freight24.rest.api.TruckOwnerApi
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
        Log.d("ApiModule", BuildConfig.BASE_URL)
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

    @Provides
    fun providesIndentsApi(retrofitFactory: RetrofitFactory): IndentsApi {
        return retrofitFactory
            .create(BuildConfig.BASE_URL, "IndentsApi")
            .create(IndentsApi::class.java)
    }

    @Provides
    fun providesLocationsApi(retrofitFactory: RetrofitFactory): LocationApi {
        return retrofitFactory
            .create(BuildConfig.BASE_URL, "LocationApi")
            .create(LocationApi::class.java)
    }

    @Provides
    fun providesClientApi(retrofitFactory: RetrofitFactory): ClientApi {
        return retrofitFactory
            .create(BuildConfig.BASE_URL, "ClientApi")
            .create(ClientApi::class.java)
    }

    @Provides
    fun providesTruckApi(retrofitFactory: RetrofitFactory): TruckApi {
        return retrofitFactory
            .create(BuildConfig.BASE_URL, "TruckApi")
            .create(TruckApi::class.java)
    }

    @Provides
    fun providesTripApi(retrofitFactory: RetrofitFactory): TripApi {
        return retrofitFactory
            .create(BuildConfig.BASE_URL, "TripApi")
            .create(TripApi::class.java)
    }

    @Provides
    fun providesTripHaltingApi(retrofitFactory: RetrofitFactory): TripHaltingApi {
        return retrofitFactory
            .create(BuildConfig.BASE_URL, "TripHaltingApi")
            .create(TripHaltingApi::class.java)
    }

    @Provides
    fun providesTripChargesApi(retrofitFactory: RetrofitFactory): TripLUChargesApi {
        return retrofitFactory
            .create(BuildConfig.BASE_URL, "TripHaltingApi")
            .create(TripLUChargesApi::class.java)
    }

    @Provides
    fun providesTruckOwnerApi(retrofitFactory: RetrofitFactory): TruckOwnerApi {
        return retrofitFactory
            .create(BuildConfig.BASE_URL, "TruckOwnerApi")
            .create(TruckOwnerApi::class.java)
    }

    @Provides
    fun providesFindTrucksApi(retrofitFactory: RetrofitFactory): FindTrucksApi {
        return retrofitFactory
            .create(BuildConfig.BASE_URL, "FindTrucksApi")
            .create(FindTrucksApi::class.java)
    }
}
