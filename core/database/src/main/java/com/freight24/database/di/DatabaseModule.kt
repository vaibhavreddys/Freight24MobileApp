package com.freight24.database.di

import android.content.Context
import androidx.room.Room
import com.freight24.database.Freight24Database
import com.freight24.database.dao.LeadsCacheDao
import com.freight24.database.dao.LoadBoardDao
import com.freight24.database.dao.TripDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
    ): Freight24Database =
        Room.databaseBuilder(
            context,
            Freight24Database::class.java,
            "freight24.db",
        )
            .fallbackToDestructiveMigration() // Clear DB on schema changes
            .build()

    @Provides
    fun provideLoadBoardDao(db: Freight24Database): LoadBoardDao = db.loadBoardDao()

    @Provides
    fun provideTripDao(db: Freight24Database): TripDao = db.tripDao()

    @Provides
    fun provideLeadsCacheDao(db: Freight24Database): LeadsCacheDao = db.leadsCacheDao()
}
