package com.freight24.trucker.di

import com.freight24.network.RetrofitFactory
import com.freight24.network.di.DefaultNetwork
import com.freight24.rest.api.IndentsApi
import com.freight24.rest.api.TripApi
import com.freight24.rest.api.TruckApi
import com.freight24.rest.api.TruckerLoginApi
import com.freight24.trucker.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object ApiModule {
    @Provides
    fun providesLoginApi(
        @DefaultNetwork retrofitFactory: RetrofitFactory,
    ): TruckerLoginApi {
        return retrofitFactory
            .create(BuildConfig.BASE_URL, "TruckerLoginService")
            .create(TruckerLoginApi::class.java)
    }

    @Provides
    fun providesLoadApi(retrofitFactory: RetrofitFactory): IndentsApi {
        return retrofitFactory
            .create(BuildConfig.BASE_URL, "TruckerLoadsService")
            .create(IndentsApi::class.java)
    }

    @Provides
    fun providesTripsApi(retrofitFactory: RetrofitFactory): TripApi {
        return retrofitFactory
            .create(BuildConfig.BASE_URL, "TruckerTripsService")
            .create(TripApi::class.java)
    }

    @Provides
    fun providesTruckApi(retrofitFactory: RetrofitFactory): TruckApi {
        return retrofitFactory
            .create(BuildConfig.BASE_URL, "TruckerTrucksService")
            .create(TruckApi::class.java)
    }
}
