package com.swami.weather.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [WeatherEntity::class], version = 3, exportSchema = false)
abstract class WeatherDatabase : RoomDatabase() {
    abstract fun weatherDao(): WeatherDao
}
