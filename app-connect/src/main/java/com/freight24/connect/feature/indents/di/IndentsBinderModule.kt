package com.freight24.connect.feature.indents.di

import com.freight24.connect.repository.IndentRepository
import com.freight24.connect.repository.IndentRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class IndentsBinderModule {

    @Binds
    @Singleton
    abstract fun bindIndentRepository(
        indentRepositoryImpl: IndentRepositoryImpl
    ): IndentRepository
}
