package com.freight24.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.freight24.database.dao.LoadBoardDao
import com.freight24.database.dao.TripDao
import com.freight24.database.entity.CityEntity
import com.freight24.database.entity.CityPaginationEntity
import com.freight24.database.entity.LoadEntity
import com.freight24.database.entity.TripEntity
import com.freight24.database.entity.TripsPaginationEntity

@Database(
    entities = [
        CityEntity::class,
        LoadEntity::class,
        CityPaginationEntity::class,
        TripEntity::class,
        TripsPaginationEntity::class,
    ],
    version = 3,
    exportSchema = false,
)
abstract class Freight24Database : RoomDatabase() {
    abstract fun loadBoardDao(): LoadBoardDao

    abstract fun tripDao(): TripDao
}
