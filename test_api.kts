import kotlinx.coroutines.runBlocking
import java.net.HttpURLConnection
import java.net.URL

fun main() = runBlocking {
    println("=== Testing Open-Meteo APIs ===\n")

    // Test 1: Geocoding API
    println("Test 1: Geocoding API for 'London'")
    val geocodingUrl = "https://geocoding-api.open-meteo.com/v1/search?name=London&count=1&language=en&format=json"
    try {
        val response = URL(geocodingUrl).readText()
        println("✅ Geocoding Response:")
        println(response)
        println()
    } catch (e: Exception) {
        println("❌ Geocoding Failed: ${e.message}")
        println()
    }

    // Test 2: Weather API
    println("Test 2: Weather API for London (51.5074, -0.1278)")
    val weatherUrl = "https://api.open-meteo.com/v1/forecast?latitude=51.5074&longitude=-0.1278&daily=temperature_2m_max,temperature_2m_min,weathercode&timezone=auto&forecast_days=3"
    try {
        val response = URL(weatherUrl).readText()
        println("✅ Weather Response:")
        println(response)
        println()
    } catch (e: Exception) {
        println("❌ Weather API Failed: ${e.message}")
        println()
    }
}

