package com.swami.weather.data.remote

data class WeatherResponse(
    val daily: DailyWeather
)

data class DailyWeather(
    val time: List<String>,
    val temperature_2m_max: List<Double>,
    val temperature_2m_min: List<Double>,
    val weathercode: List<Int>
)

data class GeocodingResponse(
    val results: List<GeocodingResult>?
)

data class GeocodingResult(
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val country: String? = null,
    val admin1: String? = null
)
