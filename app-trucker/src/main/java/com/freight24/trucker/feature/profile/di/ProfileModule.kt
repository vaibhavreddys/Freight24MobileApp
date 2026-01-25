package com.freight24.trucker.feature.profile.di

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
    abstract fun bindFetchProfileUseCase(impl: FetchProfileUseCaseImpl): FetchProfileUseCase
}
