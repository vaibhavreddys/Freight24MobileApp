package com.freight24.trucker.feature.profile.di

import com.freight24.rest.repository.LocationRepository
import com.freight24.trucker.feature.profile.data.LocationRepositoryImpl
import com.freight24.trucker.feature.profile.data.ProfileRepositoryImpl
import com.freight24.trucker.feature.profile.domain.ProfileRepository
import com.freight24.trucker.feature.profile.domain.usecase.FetchProfileUseCase
import com.freight24.trucker.feature.profile.domain.usecase.FetchProfileUseCaseImpl
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
    abstract fun bindLocationRepository(impl: LocationRepositoryImpl): LocationRepository

    @Binds
    @ViewModelScoped
    abstract fun bindFetchProfileUseCase(impl: FetchProfileUseCaseImpl): FetchProfileUseCase

    @Binds
    @ViewModelScoped
    abstract fun bindUpdateProfileUseCase(
        impl: com.freight24.trucker.feature.profile.domain.usecase.UpdateProfileUseCaseImpl,
    ): com.freight24.trucker.feature.profile.domain.usecase.UpdateProfileUseCase

    @Binds
    @ViewModelScoped
    abstract fun bindUploadIdentityDocumentUseCase(
        impl: com.freight24.trucker.feature.profile.domain.usecase.UploadIdentityDocumentUseCaseImpl,
    ): com.freight24.trucker.feature.profile.domain.usecase.UploadIdentityDocumentUseCase

    @Binds
    @ViewModelScoped
    abstract fun bindGetProfileSignedUrlUseCase(
        impl: com.freight24.trucker.feature.profile.domain.usecase.GetProfileSignedUrlUseCaseImpl,
    ): com.freight24.trucker.feature.profile.domain.usecase.GetProfileSignedUrlUseCase

    @Binds
    @ViewModelScoped
    abstract fun bindUpdateIdentityDocumentUseCase(
        impl: com.freight24.trucker.feature.profile.domain.usecase.UpdateIdentityDocumentUseCaseImpl,
    ): com.freight24.trucker.feature.profile.domain.usecase.UpdateIdentityDocumentUseCase

    @Binds
    @ViewModelScoped
    abstract fun bindFinalizeIdentityDocumentsUseCase(
        impl: com.freight24.trucker.feature.profile.domain.usecase.FinalizeIdentityDocumentsUseCaseImpl,
    ): com.freight24.trucker.feature.profile.domain.usecase.FinalizeIdentityDocumentsUseCase
}
