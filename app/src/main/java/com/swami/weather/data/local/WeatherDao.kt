package com.swami.weather.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface WeatherDao {

    @Query("SELECT * FROM weather WHERE city = :city ORDER BY date ASC")
    suspend fun getWeatherByCity(city: String): List<WeatherEntity>

    @Query("SELECT * FROM weather ORDER BY date ASC LIMIT 3")
    suspend fun getLastCachedWeather(): List<WeatherEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(data: List<WeatherEntity>)

    @Query("DELETE FROM weather WHERE city = :city")
    suspend fun clearCity(city: String)

    @Query("DELETE FROM weather")
    suspend fun clear()
}
