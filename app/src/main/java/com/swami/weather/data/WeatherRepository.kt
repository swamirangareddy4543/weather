package com.swami.weather.data

import android.util.Log
import com.swami.weather.data.local.WeatherDao
import com.swami.weather.data.local.WeatherEntity
import com.swami.weather.data.remote.GeocodingApi
import com.swami.weather.data.remote.WeatherApi

class WeatherRepository(
    private val weatherApi: WeatherApi,
    private val geocodingApi: GeocodingApi,
    private val dao: WeatherDao
) {

    companion object {
        private const val TAG = "WeatherRepository"
    }

    suspend fun getWeather(city: String): List<WeatherEntity> {
        return try {

            val location = geocodeCity(city.trim())

            val cityName = buildString {
                append(location.name)
                location.admin1?.let { append(", $it") }
                location.country?.let { append(", $it") }
            }

            val response = weatherApi.getForecast(location.latitude, location.longitude)

            val data = response.daily.time.take(3).mapIndexed { index, date ->
                val maxTemp = response.daily.temperature_2m_max[index]
                val minTemp = response.daily.temperature_2m_min[index]
                val avgTemp = (maxTemp + minTemp) / 2
                val weatherCode = response.daily.weathercode[index]

                WeatherEntity(
                    city = cityName,
                    date = date,
                    temp = avgTemp,
                    condition = getWeatherCondition(weatherCode),
                    icon = getWeatherIcon(weatherCode),
                    tempMin = minTemp,
                    tempMax = maxTemp
                )
            }

            dao.clearCity(cityName)
            dao.insertAll(data)
            data
        } catch (e: Exception) {

            val isNetworkError =
                e.message?.contains("Unable to resolve host", ignoreCase = true) == true ||
                        e.message?.contains("UnknownHost", ignoreCase = true) == true ||
                        e.message?.contains("Network is unreachable", ignoreCase = true) == true ||
                        e.message?.contains(
                            "No address associated with hostname",
                            ignoreCase = true
                        ) == true ||
                        e.cause?.javaClass?.simpleName?.contains("UnknownHost") == true

            val cachedData = findCachedData(city.trim())

            if (cachedData.isEmpty()) {
                if (isNetworkError) {
                    throw Exception("Unable to resolve host")
                } else {
                    throw Exception(buildErrorMessage(city, e))
                }
            }

            cachedData
        }
    }

    private suspend fun geocodeCity(cityInput: String): com.swami.weather.data.remote.GeocodingResult {
        var lastException: Exception? = null

        try {
            val response = geocodingApi.searchCity(cityInput, count = 5)

            if (!response.results.isNullOrEmpty()) {
                val firstResult = response.results.first()
                return firstResult
            }
        } catch (e: Exception) {
            e.printStackTrace()
            lastException = e
            if (e.message?.contains("Unable to resolve host", ignoreCase = true) == true ||
                e.message?.contains("UnknownHost", ignoreCase = true) == true ||
                e.message?.contains("Failed to connect", ignoreCase = true) == true
            ) {
                throw e
            }
        }

        if (cityInput.contains(",")) {
            val cityPart = cityInput.split(",")[0].trim()
            try {
                val response = geocodingApi.searchCity(cityPart, count = 5)

                if (!response.results.isNullOrEmpty()) {
                    val firstResult = response.results.first()
                    return firstResult
                }
            } catch (e: Exception) {
                lastException = e
                if (e.message?.contains("Unable to resolve host", ignoreCase = true) == true ||
                    e.message?.contains("UnknownHost", ignoreCase = true) == true ||
                    e.message?.contains("Failed to connect", ignoreCase = true) == true
                ) {
                    throw e
                }
            }
        }

        val capitalizedCity = cityInput.split(" ").joinToString(" ") {
            it.replaceFirstChar { char -> char.uppercase() }
        }
        try {
            val response = geocodingApi.searchCity(capitalizedCity, count = 5)

            if (!response.results.isNullOrEmpty()) {
                val firstResult = response.results.first()
                return firstResult
            }
        } catch (e: Exception) {
            lastException = e
            if (e.message?.contains("Unable to resolve host", ignoreCase = true) == true ||
                e.message?.contains("UnknownHost", ignoreCase = true) == true ||
                e.message?.contains("Failed to connect", ignoreCase = true) == true
            ) {
                throw e
            }
        }

        if (lastException?.message?.contains("Unable to resolve host", ignoreCase = true) == true ||
            lastException?.message?.contains("UnknownHost", ignoreCase = true) == true
        ) {
            throw lastException
        }

        throw Exception("City not found. Try: 'London', 'London,UK', 'New York,US', 'Paris,FR'")
    }

    private suspend fun findCachedData(cityInput: String): List<WeatherEntity> {
        val query = cityInput.trim()

        var cached = dao.getWeatherByCity(query)
        if (cached.isNotEmpty()) {
            return cached
        }

        if (query.contains(",")) {
            val cityOnly = query.split(",")[0].trim()
            cached = dao.getWeatherByCity(cityOnly)
            if (cached.isNotEmpty()) {
                return cached
            }
        }

        val allCachedCities = dao.getAllCachedCities()

        var matchedCity = allCachedCities.find {
            it.startsWith(query, ignoreCase = true)
        }

        if (matchedCity == null) {
            matchedCity = allCachedCities.find {
                it.contains(query, ignoreCase = true)
            }
        }

        if (matchedCity != null) {
            cached = dao.getWeatherByCity(matchedCity)
            if (cached.isNotEmpty()) {
                return cached
            }
        }

        return emptyList()
    }

    private fun buildErrorMessage(city: String, error: Exception): String {
        return when {
            error.message?.contains("not found", ignoreCase = true) == true ->
                "City '$city' not found.\n\nTry:\n• Check spelling\n• Add country: 'London,UK'\n• Use major city nearby"

            error.message?.contains("Unable to resolve host", ignoreCase = true) == true ||
                    error.message?.contains("UnknownHost", ignoreCase = true) == true ->
                "No internet connection.\n\nPlease check your network and try again."

            error.message?.contains("timeout", ignoreCase = true) == true ->
                "Request timeout.\n\nPlease try again."

            else ->
                "Unable to fetch weather for '$city'.\n\n${error.message ?: "Unknown error"}"
        }
    }

    suspend fun getAllCachedCities(): List<String> {
        return dao.getAllCachedCities()
    }

    suspend fun getCachedWeatherByCity(city: String): List<WeatherEntity> {
        return dao.getWeatherByCity(city.trim())
    }

    private fun getWeatherCondition(code: Int): String {
        return when (code) {
            0 -> "Clear"
            1, 2, 3 -> "Partly Cloudy"
            45, 48 -> "Foggy"
            51, 53, 55 -> "Drizzle"
            61, 63, 65 -> "Rain"
            71, 73, 75 -> "Snow"
            77 -> "Snow Grains"
            80, 81, 82 -> "Rain Showers"
            85, 86 -> "Snow Showers"
            95 -> "Thunderstorm"
            96, 99 -> "Thunderstorm with Hail"
            else -> "Unknown"
        }
    }

    // Map weather codes to icon names (compatible with OpenWeatherMap icons)
    private fun getWeatherIcon(code: Int): String {
        return when (code) {
            0 -> "01d"           // Clear sky
            1, 2 -> "02d"        // Partly cloudy
            3 -> "03d"           // Cloudy
            45, 48 -> "50d"      // Fog
            51, 53, 55 -> "09d"  // Drizzle
            61, 63, 65 -> "10d"  // Rain
            71, 73, 75 -> "13d"  // Snow
            77 -> "13d"          // Snow grains
            80, 81, 82 -> "09d"  // Rain showers
            85, 86 -> "13d"      // Snow showers
            95 -> "11d"          // Thunderstorm
            96, 99 -> "11d"      // Thunderstorm with hail
            else -> "01d"
        }
    }
}
