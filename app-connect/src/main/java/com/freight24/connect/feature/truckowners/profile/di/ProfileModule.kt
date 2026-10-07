package com.freight24.connect.feature.truckowners.profile.di

import com.freight24.connect.feature.truckowners.profile.data.ProfileRepositoryImpl
import com.freight24.connect.feature.truckowners.profile.domain.ProfileRepository
import com.freight24.connect.feature.truckowners.profile.domain.usecase.FetchProfileUseCase
import com.freight24.connect.feature.truckowners.profile.domain.usecase.FetchProfileUseCaseImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class)
internal abstract class ProfileModule {
    @Binds
    @ViewModelScoped
    abstract fun bindProfileRepository(impl: ProfileRepositoryImpl): ProfileRepository

    @Binds
    @ViewModelScoped
    abstract fun bindFetchProfileUseCase(impl: FetchProfileUseCaseImpl): FetchProfileUseCase

    @Binds
    @ViewModelScoped
    abstract fun bindGetProfileSignedUrlUseCase(
        impl: com.freight24.connect.feature.truckowners.profile.domain.usecase.GetProfileSignedUrlUseCaseImpl,
    ): com.freight24.connect.feature.truckowners.profile.domain.usecase.GetProfileSignedUrlUseCase

    @Binds
    @ViewModelScoped
    abstract fun bindUpdateTruckOwnerStatusUseCase(
        impl: com.freight24.connect.feature.truckowners.profile.domain.usecase.UpdateTruckOwnerStatusUseCaseImpl,
    ): com.freight24.connect.feature.truckowners.profile.domain.usecase.UpdateTruckOwnerStatusUseCase

    @Binds
    @ViewModelScoped
    abstract fun bindUpdateTruckOwnerSectionsUseCase(
        impl: com.freight24.connect.feature.truckowners.profile.domain.usecase.UpdateTruckOwnerSectionsUseCaseImpl,
    ): com.freight24.connect.feature.truckowners.profile.domain.usecase.UpdateTruckOwnerSectionsUseCase
}
