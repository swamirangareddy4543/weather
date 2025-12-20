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
        // Load all cached cities on startup
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
                // Add 30-second timeout to prevent infinite loading
                val weatherData = withTimeout(30000L) {
                    repository.getWeather(city)
                }
                Log.d(TAG, "Weather data received: ${weatherData.size} items")

                // Refresh cached cities list
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
                uiState = uiState.copy(
                    isLoading = false,
                    error = "Request timeout. Please try again."
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching weather", e)
                Log.e(TAG, "Exception type: ${e.javaClass.simpleName}")
                Log.e(TAG, "Exception message: ${e.message}")
                Log.e(TAG, "Stack trace:", e)

                val errorMessage = when {
                    e.message?.contains("not found", ignoreCase = true) == true -> {
                        e.message ?: "City not found"
                    }
                    e.message?.contains("UnknownHost") == true || e.message?.contains("Unable to resolve host") == true -> {
                        "No internet connection. Showing cached data."
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

    fun clearError() {
        uiState = uiState.copy(error = null)
    }

    fun selectCity(cityName: String) {
        viewModelScope.launch {
            try {
                val cityWeather = repository.getCachedWeatherByCity(cityName)

                if (cityWeather.isNotEmpty()) {
                    uiState = uiState.copy(
                        weather = cityWeather,
                        selectedCity = cityName,
                        error = null
                    )
                    Log.d(TAG, "Selected city: $cityName with ${cityWeather.size} weather records")
                } else {
                    Log.w(TAG, "No cached data found for city: $cityName")
                    uiState = uiState.copy(
                        error = "No cached data available for $cityName"
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error selecting city: $cityName", e)
            }
        }
    }
}
