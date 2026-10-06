package com.freight24.connect.di

import android.util.Log
import com.freight24.connect.BuildConfig
import com.freight24.network.RetrofitFactory
import com.freight24.network.di.AuthenticatedNetwork
import com.freight24.network.di.DefaultNetwork
import com.freight24.rest.api.AdhocTruckApi
import com.freight24.rest.api.AuthSessionApi
import com.freight24.rest.api.ClientApi
import com.freight24.rest.api.ClientV2Api
import com.freight24.rest.api.DocumentApi
import com.freight24.rest.api.EmployeesApi
import com.freight24.rest.api.FindTrucksApi
import com.freight24.rest.api.IndentInterestApi
import com.freight24.rest.api.IndentsApi
import com.freight24.rest.api.LeadsApi
import com.freight24.rest.api.LocationApi
import com.freight24.rest.api.LoginConnectApi
import com.freight24.rest.api.NotificationServiceApi
import com.freight24.rest.api.ProfileApi
import com.freight24.rest.api.TripApi
import com.freight24.rest.api.TripHaltingApi
import com.freight24.rest.api.TripLUChargesApi
import com.freight24.rest.api.TruckOwnerV2Api
import com.freight24.rest.api.TruckV2Api
import com.freight24.rest.api.VerificationApi
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
    fun providesAuthSessionApi(
        @DefaultNetwork retrofitFactory: RetrofitFactory,
    ): AuthSessionApi {
        return retrofitFactory
            .create(BuildConfig.BASE_URL, "AuthSessionApi")
            .create(AuthSessionApi::class.java)
    }

    @Provides
    fun providesIndentsApi(
        @AuthenticatedNetwork retrofitFactory: RetrofitFactory,
    ): IndentsApi {
        return retrofitFactory
            .create(BuildConfig.BASE_URL, "IndentsApi")
            .create(IndentsApi::class.java)
    }

    @Provides
    fun providesLocationsApi(
        @AuthenticatedNetwork retrofitFactory: RetrofitFactory,
    ): LocationApi {
        return retrofitFactory
            .create(BuildConfig.BASE_URL, "LocationApi")
            .create(LocationApi::class.java)
    }

    @Provides
    fun providesClientApi(
        @AuthenticatedNetwork retrofitFactory: RetrofitFactory,
    ): ClientApi {
        return retrofitFactory
            .create(BuildConfig.BASE_URL, "ClientApi")
            .create(ClientApi::class.java)
    }

    /**
     * v2 client reads (branches + contacts) used by the indent flow's client
     * pickers. Separate from [ClientApi], which still serves the legacy
     * `/api/v1/client` screens.
     */
    @Provides
    fun providesClientV2Api(
        @AuthenticatedNetwork retrofitFactory: RetrofitFactory,
    ): ClientV2Api {
        return retrofitFactory
            .create(BuildConfig.BASE_URL, "ClientV2Api")
            .create(ClientV2Api::class.java)
    }

    @Provides
    fun providesTruckV2Api(
        @AuthenticatedNetwork retrofitFactory: RetrofitFactory,
    ): TruckV2Api {
        return retrofitFactory
            .create(BuildConfig.BASE_URL, "TruckV2Api")
            .create(TruckV2Api::class.java)
    }

    @Provides
    fun providesTripApi(
        @AuthenticatedNetwork retrofitFactory: RetrofitFactory,
    ): TripApi {
        return retrofitFactory
            .create(BuildConfig.BASE_URL, "TripApi")
            .create(TripApi::class.java)
    }

    @Provides
    fun providesVerificationApi(
        @AuthenticatedNetwork retrofitFactory: RetrofitFactory,
    ): VerificationApi {
        return retrofitFactory
            .create(BuildConfig.BASE_URL, "VerificationApi")
            .create(VerificationApi::class.java)
    }

    @Provides
    fun providesTripHaltingApi(
        @AuthenticatedNetwork retrofitFactory: RetrofitFactory,
    ): TripHaltingApi {
        return retrofitFactory
            .create(BuildConfig.BASE_URL, "TripHaltingApi")
            .create(TripHaltingApi::class.java)
    }

    @Provides
    fun providesTripChargesApi(
        @AuthenticatedNetwork retrofitFactory: RetrofitFactory,
    ): TripLUChargesApi {
        return retrofitFactory
            .create(BuildConfig.BASE_URL, "TripHaltingApi")
            .create(TripLUChargesApi::class.java)
    }

    @Provides
    fun providesTruckOwnerV2Api(
        @AuthenticatedNetwork retrofitFactory: RetrofitFactory,
    ): TruckOwnerV2Api {
        return retrofitFactory
            .create(BuildConfig.BASE_URL, "TruckOwnerV2Api")
            .create(TruckOwnerV2Api::class.java)
    }

    @Provides
    fun providesProfileApi(
        @AuthenticatedNetwork retrofitFactory: RetrofitFactory,
    ): ProfileApi {
        return retrofitFactory
            .create(BuildConfig.BASE_URL, "TruckOwnerProfileApi")
            .create(ProfileApi::class.java)
    }

    @Provides
    fun providesDocumentApi(
        @AuthenticatedNetwork retrofitFactory: RetrofitFactory,
    ): DocumentApi {
        return retrofitFactory
            .create(BuildConfig.BASE_URL, "TruckOwnerDocumentApi")
            .create(DocumentApi::class.java)
    }

    @Provides
    fun providesFindTrucksApi(
        @AuthenticatedNetwork retrofitFactory: RetrofitFactory,
    ): FindTrucksApi {
        return retrofitFactory
            .create(BuildConfig.BASE_URL, "FindTrucksApi")
            .create(FindTrucksApi::class.java)
    }

    @Provides
    fun providesEmployeesApi(
        @AuthenticatedNetwork retrofitFactory: RetrofitFactory,
    ): EmployeesApi {
        return retrofitFactory
            .create(BuildConfig.BASE_URL, "EmployeesApi")
            .create(EmployeesApi::class.java)
    }

    @Provides
    fun providesIndentInterestApi(
        @AuthenticatedNetwork retrofitFactory: RetrofitFactory,
    ): IndentInterestApi {
        return retrofitFactory
            .create(BuildConfig.BASE_URL, "IndentInterestApi")
            .create(IndentInterestApi::class.java)
    }

    @Provides
    fun providesNotificationServiceApi(
        @AuthenticatedNetwork retrofitFactory: RetrofitFactory,
    ): NotificationServiceApi {
        return retrofitFactory
            .create(BuildConfig.BASE_URL, "NotificationServiceApi")
            .create(NotificationServiceApi::class.java)
    }

    @Provides
    fun providesAdhocTruckApi(
        @AuthenticatedNetwork retrofitFactory: RetrofitFactory,
    ): AdhocTruckApi {
        return retrofitFactory
            .create(BuildConfig.BASE_URL, "AdhocTruckApi")
            .create(AdhocTruckApi::class.java)
    }

    @Provides
    fun providesLeadsApi(
        @AuthenticatedNetwork retrofitFactory: RetrofitFactory,
    ): LeadsApi {
        return retrofitFactory
            .create(BuildConfig.BASE_URL, "LeadsApi")
            .create(LeadsApi::class.java)
    }
}
