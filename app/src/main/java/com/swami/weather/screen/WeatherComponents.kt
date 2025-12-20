package com.swami.weather.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.swami.weather.data.local.WeatherEntity

@Suppress("SwallowedException")
private fun formatDate(dateString: String): String {
    return try {
        val parts = dateString.split("-")
        if (parts.size == 3) {
            val year = parts[0]
            val month = parts[1]
            val day = parts[2]

            val monthNames = listOf(
                "Jan", "Feb", "Mar", "Apr", "May", "Jun",
                "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
            )

            val monthIndex = month.toIntOrNull()?.minus(1)
            val monthName = if (monthIndex != null && monthIndex in monthNames.indices) {
                monthNames[monthIndex]
            } else {
                month
            }

            "$day $monthName $year"
        } else {
            dateString
        }
    } catch (_: Exception) {
        dateString
    }
}


@Composable
fun WeatherCard(weather: WeatherEntity) {
    val gradientColors = getWeatherGradient(weather.condition)

    Card(
        modifier = Modifier
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(gradientColors)
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left section: Date and condition
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = formatDate(weather.date),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = weather.condition,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White.copy(alpha = 0.9f),
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Min/Max temperature row
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Min temp
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = "Min temp",
                                tint = Color(0xFF90CAF9),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${weather.tempMin.toInt()}°",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        // Max temp
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowUp,
                                contentDescription = "Max temp",
                                tint = Color(0xFFFFAB91),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${weather.tempMax.toInt()}°",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                // Center: Weather icon
                Surface(
                    modifier = Modifier
                        .size(90.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    color = Color.White.copy(alpha = 0.2f)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.padding(8.dp)
                    ) {
                        AsyncImage(
                            model = "https://openweathermap.org/img/wn/${weather.icon}@2x.png",
                            contentDescription = weather.condition,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                // Right section: Main temperature
                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier.padding(start = 16.dp)
                ) {
                    Text(
                        text = "${weather.temp.toInt()}°",
                        style = MaterialTheme.typography.displayLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 56.sp
                    )
                    Text(
                        text = "Celsius",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

// Helper function to get weather-based gradient colors
fun getWeatherGradient(condition: String): List<Color> {
    return when {
        condition.contains("Clear", ignoreCase = true) ||
                condition.contains("Sunny", ignoreCase = true) ->
            listOf(Color(0xFFFFA726), Color(0xFFFB8C00))

        condition.contains("Cloud", ignoreCase = true) ->
            listOf(Color(0xFF78909C), Color(0xFF546E7A))

        condition.contains("Rain", ignoreCase = true) ||
                condition.contains("Drizzle", ignoreCase = true) ->
            listOf(Color(0xFF5C6BC0), Color(0xFF3949AB))

        condition.contains("Thunder", ignoreCase = true) ||
                condition.contains("Storm", ignoreCase = true) ->
            listOf(Color(0xFF5E35B1), Color(0xFF4527A0))

        condition.contains("Snow", ignoreCase = true) ->
            listOf(Color(0xFF81D4FA), Color(0xFF4FC3F7))

        condition.contains("Fog", ignoreCase = true) ||
                condition.contains("Mist", ignoreCase = true) ->
            listOf(Color(0xFF90A4AE), Color(0xFF78909C))

        condition.contains("Overcast", ignoreCase = true) ->
            listOf(Color(0xFF757575), Color(0xFF616161))

        else -> listOf(Color(0xFF42A5F5), Color(0xFF1E88E5))
    }
}

