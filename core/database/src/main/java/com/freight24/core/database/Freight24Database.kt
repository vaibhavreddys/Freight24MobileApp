package com.freight24.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.freight24.core.database.dao.LoadBoardDao
import com.freight24.core.database.dao.TripDao
import com.freight24.core.database.entity.CityEntity
import com.freight24.core.database.entity.CityPaginationEntity
import com.freight24.core.database.entity.LoadEntity
import com.freight24.core.database.entity.TripEntity
import com.freight24.core.database.entity.TripsPaginationEntity

@Database(
    entities = [
        CityEntity::class,
        LoadEntity::class,
        CityPaginationEntity::class,
        TripEntity::class,
        TripsPaginationEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class Freight24Database : RoomDatabase() {
    abstract fun loadBoardDao(): LoadBoardDao
    abstract fun tripDao(): TripDao
}
