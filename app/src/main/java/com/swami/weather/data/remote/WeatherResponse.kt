package com.swami.weather.data.remote

// Open-Meteo Weather API Response
data class WeatherResponse(
    val daily: DailyWeather
)

data class DailyWeather(
    val time: List<String>,                    // Dates
    val temperature_2m_max: List<Double>,      // Max temperatures
    val temperature_2m_min: List<Double>,      // Min temperatures
    val weathercode: List<Int>                 // Weather codes
)

// Geocoding API Response
data class GeocodingResponse(
    val results: List<GeocodingResult>?
)

data class GeocodingResult(
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val country: String? = null,
    val admin1: String? = null  // State/Province
)
