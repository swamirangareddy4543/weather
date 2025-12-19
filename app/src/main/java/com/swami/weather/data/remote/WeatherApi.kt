package com.swami.weather.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

// Open-Meteo API - Completely free, no API key required!
interface WeatherApi {

    @GET("v1/forecast")
    suspend fun getForecast(
        @Query("latitude") lat: Double,
        @Query("longitude") lon: Double,
        @Query("daily") daily: String = "temperature_2m_max,temperature_2m_min,weathercode",
        @Query("timezone") timezone: String = "auto",
        @Query("forecast_days") days: Int = 3
    ): WeatherResponse
}

// Geocoding API to convert city name to coordinates
interface GeocodingApi {

    @GET("v1/search")
    suspend fun searchCity(
        @Query("name") cityName: String,
        @Query("count") count: Int = 10,
        @Query("language") language: String = "en",
        @Query("format") format: String = "json"
    ): GeocodingResponse
}

