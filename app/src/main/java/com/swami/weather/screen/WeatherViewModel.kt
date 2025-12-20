package com.swami.weather.screen

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swami.weather.data.WeatherRepository
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import com.swami.weather.data.local.WeatherEntity

data class WeatherUiState(
    val weather: List<WeatherEntity> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val cachedCities: List<String> = emptyList(),
    val selectedCity: String? = null
)

class WeatherViewModel(
    private val repository: WeatherRepository
) : ViewModel() {

    var uiState by mutableStateOf(WeatherUiState())
        private set

    private companion object {
        const val TAG = "WeatherViewModel"
    }

    init {
        viewModelScope.launch {
            try {
                val cachedCities = repository.getAllCachedCities()
                uiState = uiState.copy(cachedCities = cachedCities)
                Log.d(TAG, "Loaded ${cachedCities.size} cached cities: $cachedCities")
            } catch (e: Exception) {
                Log.e(TAG, "Error loading cached cities", e)
            }
        }
    }

    fun fetchWeather(city: String) {
        if (city.isBlank()) {
            uiState = uiState.copy(error = "Please enter a city name")
            return
        }

        Log.d(TAG, "Fetching weather for: $city")
        uiState = uiState.copy(isLoading = true, error = null)

        viewModelScope.launch {
            try {
                val weatherData = withTimeout(30000L) {
                    repository.getWeather(city)
                }
                Log.d(TAG, "Weather data received: ${weatherData.size} items")

                val cachedCities = repository.getAllCachedCities()
                val cityName = weatherData.firstOrNull()?.city

                uiState = uiState.copy(
                    weather = weatherData,
                    cachedCities = cachedCities,
                    selectedCity = cityName,
                    isLoading = false,
                    error = if (weatherData.isEmpty()) "No weather data found for $city" else null
                )
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "Request timeout for: $city", e)
                // Try to find in cached data
                tryOfflineSearch(city)
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching weather", e)
                Log.e(TAG, "Exception type: ${e.javaClass.simpleName}")
                Log.e(TAG, "Exception message: ${e.message}")
                Log.e(TAG, "Stack trace:", e)

                val isNetworkError = e.message?.contains("UnknownHost") == true ||
                                    e.message?.contains("Unable to resolve host") == true

                if (isNetworkError) {
                    // Network error - try offline search
                    Log.d(TAG, "Network error detected, searching in cached data")
                    tryOfflineSearch(city)
                } else {
                    val errorMessage = when {
                        e.message?.contains("not found", ignoreCase = true) == true -> {
                            e.message ?: "City not found"
                        }
                        else -> "Error: ${e.message ?: "Failed to fetch weather data"}"
                    }

                    uiState = uiState.copy(
                        isLoading = false,
                        error = errorMessage
                    )
                }
            }
        }
    }

    private suspend fun tryOfflineSearch(city: String) {
        try {
            // Search for matching city in cached data
            val matchedWeather = repository.searchCachedCities(city)

            if (matchedWeather.isNotEmpty()) {
                val cachedCities = repository.getAllCachedCities()
                val cityName = matchedWeather.firstOrNull()?.city

                Log.d(TAG, "Found cached weather for: $cityName")
                uiState = uiState.copy(
                    weather = matchedWeather,
                    cachedCities = cachedCities,
                    selectedCity = cityName,
                    isLoading = false,
                    error = "Offline mode: Showing cached data for $cityName"
                )
            } else {
                val cachedCities = repository.getAllCachedCities()
                Log.d(TAG, "No cached data found for: $city")

                val errorMessage = if (cachedCities.isEmpty()) {
                    "No internet connection.\n\nCity '$city' not found in cached data.\nNo cities available offline."
                } else {
                    "No internet connection.\n\nCity '$city' not found in cached data.\n\nAvailable cities: ${cachedCities.joinToString(", ")}"
                }

                uiState = uiState.copy(
                    isLoading = false,
                    cachedCities = cachedCities,
                    error = errorMessage
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in offline search", e)
            uiState = uiState.copy(
                isLoading = false,
                error = "No internet connection.\n\nUnable to access cached data."
            )
        }
    }

    fun clearError() {
        uiState = uiState.copy(error = null)
    }

    fun selectCity(cityName: String) {
        viewModelScope.launch {
            try {
                val cityWeather = repository.getCachedWeatherByCity(cityName)

                uiState = if (cityWeather.isNotEmpty()) {
                    uiState.copy(
                        weather = cityWeather,
                        selectedCity = cityName,
                        error = null
                    )
                } else {
                    uiState.copy(
                        error = "No cached data available for $cityName"
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error selecting city: $cityName", e)
            }
        }
    }
}
