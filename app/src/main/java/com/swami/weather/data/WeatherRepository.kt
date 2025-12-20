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
            Log.d(TAG, "=== Starting weather fetch for: $city ===")

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
            Log.e(TAG, "Error fetching weather for '$city': ${e.message}")

            // Check if it's a network error
            val isNetworkError = e.message?.contains("Unable to resolve host", ignoreCase = true) == true ||
                    e.message?.contains("UnknownHost", ignoreCase = true) == true ||
                    e.message?.contains("Network is unreachable", ignoreCase = true) == true ||
                    e.message?.contains("No address associated with hostname", ignoreCase = true) == true ||
                    e.cause?.javaClass?.simpleName?.contains("UnknownHost") == true

            val cachedData = findCachedData(city.trim())

            if (cachedData.isEmpty()) {
                // If no cached data and it's a network error, throw a clear network error
                if (isNetworkError) {
                    throw Exception("Unable to resolve host")
                } else {
                    throw Exception(buildErrorMessage(city, e))
                }
            }

            Log.d(TAG, "Returning cached data for '$city'")
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
            // Check if it's a network error - if so, throw it immediately
            if (e.message?.contains("Unable to resolve host", ignoreCase = true) == true ||
                e.message?.contains("UnknownHost", ignoreCase = true) == true ||
                e.message?.contains("Failed to connect", ignoreCase = true) == true) {
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
                // Check if it's a network error - if so, throw it immediately
                if (e.message?.contains("Unable to resolve host", ignoreCase = true) == true ||
                    e.message?.contains("UnknownHost", ignoreCase = true) == true ||
                    e.message?.contains("Failed to connect", ignoreCase = true) == true) {
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
            // Check if it's a network error - if so, throw it immediately
            if (e.message?.contains("Unable to resolve host", ignoreCase = true) == true ||
                e.message?.contains("UnknownHost", ignoreCase = true) == true ||
                e.message?.contains("Failed to connect", ignoreCase = true) == true) {
                throw e
            }
        }

        // If we had a network error, throw it, otherwise throw city not found
        if (lastException?.message?.contains("Unable to resolve host", ignoreCase = true) == true ||
            lastException?.message?.contains("UnknownHost", ignoreCase = true) == true) {
            throw lastException!!
        }

        throw Exception("City not found. Try: 'London', 'London,UK', 'New York,US', 'Paris,FR'")
    }

    private suspend fun findCachedData(cityInput: String): List<WeatherEntity> {
        val query = cityInput.trim()

        // Try exact match first (case-insensitive via DAO)
        var cached = dao.getWeatherByCity(query)
        if (cached.isNotEmpty()) {
            Log.d(TAG, "Found exact match for: $query")
            return cached
        }

        // If input contains comma, try just the city part
        if (query.contains(",")) {
            val cityOnly = query.split(",")[0].trim()
            cached = dao.getWeatherByCity(cityOnly)
            if (cached.isNotEmpty()) {
                Log.d(TAG, "Found match for city part: $cityOnly")
                return cached
            }
        }

        // Try partial matching against all cached cities
        val allCachedCities = dao.getAllCachedCities()
        Log.d(TAG, "Searching '$query' in cached cities: $allCachedCities")

        // Try startsWith match (e.g., "London" matches "London, England, UK")
        var matchedCity = allCachedCities.find {
            it.startsWith(query, ignoreCase = true)
        }

        // Try contains match (e.g., "York" matches "New York, US")
        if (matchedCity == null) {
            matchedCity = allCachedCities.find {
                it.contains(query, ignoreCase = true)
            }
        }

        // If we found a matching city, return its weather data
        if (matchedCity != null) {
            Log.d(TAG, "Found partial match: $matchedCity")
            cached = dao.getWeatherByCity(matchedCity)
            if (cached.isNotEmpty()) {
                return cached
            }
        }

        // Don't fall back to last cached weather - return empty if not found
        Log.d(TAG, "No cached data found for: $query")
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

    suspend fun getLastCachedWeather(): List<WeatherEntity> {
        return dao.getLastCachedWeather()
    }

    suspend fun getAllCachedWeather(): List<WeatherEntity> {
        return dao.getAllCachedWeather()
    }

    suspend fun getAllCachedCities(): List<String> {
        return dao.getAllCachedCities()
    }

    suspend fun getCachedWeatherByCity(city: String): List<WeatherEntity> {
        return dao.getWeatherByCity(city.trim())
    }

    suspend fun searchCachedCities(searchQuery: String): List<WeatherEntity> {
        val query = searchQuery.trim()
        val allCachedCities = dao.getAllCachedCities()

        Log.d(TAG, "Searching for '$query' in cached cities: $allCachedCities")

        // Try exact match first (case-insensitive)
        var matchedCity = allCachedCities.find {
            it.equals(query, ignoreCase = true)
        }

        // Try partial match at the start (e.g., "London" matches "London, England, UK")
        if (matchedCity == null) {
            matchedCity = allCachedCities.find {
                it.startsWith(query, ignoreCase = true)
            }
        }

        // Try contains match (e.g., "York" matches "New York, US")
        if (matchedCity == null) {
            matchedCity = allCachedCities.find {
                it.contains(query, ignoreCase = true)
            }
        }

        // If we found a matching city, return its weather data
        return if (matchedCity != null) {
            Log.d(TAG, "Found matching city: $matchedCity")
            dao.getWeatherByCity(matchedCity)
        } else {
            Log.d(TAG, "No matching city found for: $query")
            emptyList()
        }
    }

    // Convert WMO Weather codes to readable conditions
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
