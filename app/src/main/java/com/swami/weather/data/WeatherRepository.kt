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

            // Step 1: Geocode the city to get coordinates
            val location = geocodeCity(city.trim())

            val cityName = buildString {
                append(location.name)
                location.admin1?.let { append(", $it") }
                location.country?.let { append(", $it") }
            }

            Log.d(TAG, "✅ Found location: $cityName")
            Log.d(TAG, "   Coordinates: Lat=${location.latitude}, Lon=${location.longitude}")

            // Step 2: Fetch weather data using coordinates
            Log.d(TAG, "🌤️ Fetching weather from Open-Meteo API...")
            val response = weatherApi.getForecast(location.latitude, location.longitude)
            Log.d(TAG, "✅ Weather data received: ${response.daily.time.size} days")

            // Step 3: Process and map the data
            val data = response.daily.time.take(3).mapIndexed { index, date ->
                val maxTemp = response.daily.temperature_2m_max[index]
                val minTemp = response.daily.temperature_2m_min[index]
                val avgTemp = (maxTemp + minTemp) / 2
                val weatherCode = response.daily.weathercode[index]

                Log.d(TAG, "   Day $index: $date - ${avgTemp}°C - ${getWeatherCondition(weatherCode)}")

                WeatherEntity(
                    city = cityName,
                    date = date,
                    temp = avgTemp,
                    condition = getWeatherCondition(weatherCode),
                    icon = getWeatherIcon(weatherCode)
                )
            }

            // Step 4: Save to database
            Log.d(TAG, "💾 Saving ${data.size} items to database")
            dao.clearCity(cityName)
            dao.insertAll(data)
            Log.d(TAG, "=== Weather fetch completed successfully ===")

            data
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error fetching weather for '$city'", e)
            Log.e(TAG, "   Error type: ${e.javaClass.simpleName}")
            Log.e(TAG, "   Error message: ${e.message}")

            // Try to load cached data
            Log.d(TAG, "🔍 Attempting to load cached data...")
            val cachedData = findCachedData(city.trim())

            if (cachedData.isEmpty()) {
                Log.e(TAG, "❌ No cached data available for '$city'")
                throw Exception(buildErrorMessage(city, e))
            }

            Log.d(TAG, "✅ Loaded ${cachedData.size} cached items")
            cachedData
        }
    }

    // Geocode city with retry logic
    private suspend fun geocodeCity(cityInput: String): com.swami.weather.data.remote.GeocodingResult {
        Log.d(TAG, "🔍 Geocoding: '$cityInput'")

        // Strategy 1: Try direct search
        try {
            Log.d(TAG, "   Try 1: Making API call to geocoding service...")
            val response = geocodingApi.searchCity(cityInput, count = 5)
            Log.d(TAG, "   Response received. Results: ${response.results?.size ?: 0}")

            if (!response.results.isNullOrEmpty()) {
                Log.d(TAG, "   ✅ Found ${response.results.size} result(s)")
                val firstResult = response.results.first()
                Log.d(TAG, "   First result: ${firstResult.name}, Lat=${firstResult.latitude}, Lon=${firstResult.longitude}")
                return firstResult
            } else {
                Log.w(TAG, "   ⚠️ Response was empty or null")
            }
        } catch (e: Exception) {
            Log.e(TAG, "   ❌ Direct search failed: ${e.javaClass.simpleName}: ${e.message}")
            e.printStackTrace()
        }

        // Strategy 2: If comma-separated, try just the city name
        if (cityInput.contains(",")) {
            val cityPart = cityInput.split(",")[0].trim()
            Log.d(TAG, "   Try 2: Searching without country code: '$cityPart'")
            try {
                Log.d(TAG, "   Making API call...")
                val response = geocodingApi.searchCity(cityPart, count = 5)
                Log.d(TAG, "   Response received. Results: ${response.results?.size ?: 0}")

                if (!response.results.isNullOrEmpty()) {
                    Log.d(TAG, "   ✅ Found ${response.results.size} result(s)")
                    val firstResult = response.results.first()
                    Log.d(TAG, "   First result: ${firstResult.name}")
                    return firstResult
                } else {
                    Log.w(TAG, "   ⚠️ Response was empty or null")
                }
            } catch (e: Exception) {
                Log.e(TAG, "   ❌ Partial search failed: ${e.javaClass.simpleName}: ${e.message}")
            }
        }

        // Strategy 3: Try with different casing
        Log.d(TAG, "   Try 3: Trying with capitalized name")
        val capitalizedCity = cityInput.split(" ").joinToString(" ") {
            it.replaceFirstChar { char -> char.uppercase() }
        }
        Log.d(TAG, "   Capitalized: '$capitalizedCity'")
        try {
            Log.d(TAG, "   Making API call...")
            val response = geocodingApi.searchCity(capitalizedCity, count = 5)
            Log.d(TAG, "   Response received. Results: ${response.results?.size ?: 0}")

            if (!response.results.isNullOrEmpty()) {
                Log.d(TAG, "   ✅ Found ${response.results.size} result(s)")
                val firstResult = response.results.first()
                Log.d(TAG, "   First result: ${firstResult.name}")
                return firstResult
            } else {
                Log.w(TAG, "   ⚠️ Response was empty or null")
            }
        } catch (e: Exception) {
            Log.e(TAG, "   ❌ Capitalized search failed: ${e.javaClass.simpleName}: ${e.message}")
        }

        Log.e(TAG, "   ❌ All geocoding strategies failed")
        throw Exception("City not found. Try: 'London', 'London,UK', 'New York,US', 'Paris,FR'")
    }

    // Find cached data with flexible matching
    private suspend fun findCachedData(cityInput: String): List<WeatherEntity> {
        // Try exact match
        var cached = dao.getWeatherByCity(cityInput)
        if (cached.isNotEmpty()) {
            Log.d(TAG, "   Found exact match in cache")
            return cached
        }

        // Try case-insensitive
        cached = dao.getWeatherByCity(cityInput.lowercase())
        if (cached.isNotEmpty()) {
            Log.d(TAG, "   Found lowercase match in cache")
            return cached
        }

        // Try without country code
        if (cityInput.contains(",")) {
            val cityOnly = cityInput.split(",")[0].trim()
            cached = dao.getWeatherByCity(cityOnly)
            if (cached.isNotEmpty()) {
                Log.d(TAG, "   Found partial match in cache")
                return cached
            }
        }

        // Last resort: return any cached data
        cached = dao.getLastCachedWeather()
        if (cached.isNotEmpty()) {
            Log.d(TAG, "   Returning last cached weather data")
        }

        return cached
    }

    // Build user-friendly error message
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

    suspend fun getCachedWeatherByCity(city: String): List<WeatherEntity> {
        return dao.getWeatherByCity(city.trim())
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
