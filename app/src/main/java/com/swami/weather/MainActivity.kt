package com.swami.weather

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import com.swami.weather.data.WeatherRepository
import com.swami.weather.data.local.WeatherDatabase
import com.swami.weather.data.remote.WeatherApi
import com.swami.weather.navigation.WeatherNavGraph
import com.swami.weather.screen.WeatherViewModel
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val db = Room.databaseBuilder(
            applicationContext,
            WeatherDatabase::class.java,
            "weather_db"
        )
            .fallbackToDestructiveMigration()
            .build()

        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()

        val weatherRetrofit = Retrofit.Builder()
            .baseUrl("https://api.open-meteo.com/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val geocodingRetrofit = Retrofit.Builder()
            .baseUrl("https://geocoding-api.open-meteo.com/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val weatherApi = weatherRetrofit.create(WeatherApi::class.java)
        val geocodingApi =
            geocodingRetrofit.create(com.swami.weather.data.remote.GeocodingApi::class.java)
        val repository = WeatherRepository(weatherApi, geocodingApi, db.weatherDao())
        val viewModel = WeatherViewModel(repository)

        setContent {
            MaterialTheme {
                val navController = rememberNavController()
                WeatherNavGraph(
                    navController = navController,
                    viewModel = viewModel
                )
            }
        }
    }
}
