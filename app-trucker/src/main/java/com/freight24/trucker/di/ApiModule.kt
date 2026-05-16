package com.freight24.trucker.di

import com.freight24.network.RetrofitFactory
import com.freight24.network.di.AuthenticatedNetwork
import com.freight24.network.di.DefaultNetwork
import com.freight24.rest.api.AuthSessionApi
import com.freight24.rest.api.LocationApi
import com.freight24.rest.api.IndentsApi
import com.freight24.rest.api.ProfileApi
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
    fun providesAuthSessionApi(
        @DefaultNetwork retrofitFactory: RetrofitFactory,
    ): AuthSessionApi {
        return retrofitFactory
            .create(BuildConfig.BASE_URL, "AuthSessionApi")
            .create(AuthSessionApi::class.java)
    }

    @Provides
    fun providesLoadApi(
        @AuthenticatedNetwork retrofitFactory: RetrofitFactory,
    ): IndentsApi {
        return retrofitFactory
            .create(BuildConfig.BASE_URL, "TruckerLoadsService")
            .create(IndentsApi::class.java)
    }

    @Provides
    fun providesTripsApi(
        @AuthenticatedNetwork retrofitFactory: RetrofitFactory,
    ): TripApi {
        return retrofitFactory
            .create(BuildConfig.BASE_URL, "TruckerTripsService")
            .create(TripApi::class.java)
    }

    @Provides
    fun providesTruckApi(
        @AuthenticatedNetwork retrofitFactory: RetrofitFactory,
    ): TruckApi {
        return retrofitFactory
            .create(BuildConfig.BASE_URL, "TruckerTrucksService")
            .create(TruckApi::class.java)
    }

    @Provides
    fun providesProfileApi(
        @AuthenticatedNetwork retrofitFactory: RetrofitFactory,
    ): ProfileApi {
        return retrofitFactory
            .create(BuildConfig.BASE_URL, "TruckerProfileService")
            .create(ProfileApi::class.java)
    }

    @Provides
    fun providesLocationApi(
        @AuthenticatedNetwork retrofitFactory: RetrofitFactory,
    ): LocationApi {
        return retrofitFactory
            .create(BuildConfig.BASE_URL, "TruckerLocationService")
            .create(LocationApi::class.java)
    }

    @Provides
    fun providesDocumentApi(
        @AuthenticatedNetwork retrofitFactory: RetrofitFactory,
    ): com.freight24.rest.api.DocumentApi {
        return retrofitFactory
            .create(BuildConfig.BASE_URL, "TruckerDocumentService")
            .create(com.freight24.rest.api.DocumentApi::class.java)
    }
}
