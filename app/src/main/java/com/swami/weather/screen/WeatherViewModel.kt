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
    val selectedCity: String? = null,
    val hasNavigated: Boolean = false
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

        uiState = uiState.copy(isLoading = true, error = null)

        viewModelScope.launch {
            try {
                val weatherData = withTimeout(30000L) {
                    repository.getWeather(city)
                }

                val cachedCities = repository.getAllCachedCities()
                val cityName = weatherData.firstOrNull()?.city

                uiState = uiState.copy(
                    weather = weatherData,
                    cachedCities = cachedCities,
                    selectedCity = cityName,
                    isLoading = false,
                    hasNavigated = false,
                    error = if (weatherData.isEmpty()) "No weather data found for $city" else null
                )
            } catch (_: TimeoutCancellationException) {
                uiState = uiState.copy(
                    weather = emptyList(),
                    selectedCity = null,
                    isLoading = false,
                    hasNavigated = false,
                    error = "Request timeout. Please check your internet connection and try again."
                )
            } catch (e: Exception) {
                val isNetworkError = e.message?.contains("UnknownHost") == true ||
                        e.message?.contains("Unable to resolve host") == true

                val errorMessage = when {
                    isNetworkError -> "No internet connection. Please check your network and try again."
                    e.message?.contains("not found", ignoreCase = true) == true -> {
                        e.message ?: "City not found"
                    }

                    else -> "Error: ${e.message ?: "Failed to fetch weather data"}"
                }

                uiState = uiState.copy(
                    weather = emptyList(),
                    selectedCity = null,
                    isLoading = false,
                    hasNavigated = false,
                    error = errorMessage
                )
            }
        }
    }

    fun selectCity(cityName: String) {
        viewModelScope.launch {
            try {
                val cityWeather = repository.getCachedWeatherByCity(cityName)

                uiState = if (cityWeather.isNotEmpty()) {
                    uiState.copy(
                        weather = cityWeather,
                        selectedCity = cityName,
                        hasNavigated = true,
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

    fun refreshWeather(cityName: String) {
        fetchWeather(cityName)
    }

    fun clearNavigationState() {
        uiState = uiState.copy(
            hasNavigated = false,
            error = null
        )
    }

    fun markNavigationCompleted() {
        uiState = uiState.copy(hasNavigated = true)
    }
}
