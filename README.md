# 🌤️ Weather App

A modern, feature-rich Android weather application built with **Kotlin** and **Jetpack Compose**. This project demonstrates best practices in Android development using Clean Architecture, MVVM pattern, and modern Android technologies.

## 📱 Project Overview

The **Weather App** provides real-time weather information with a clean, intuitive user interface. It leverages modern Android APIs and architecture patterns to deliver a scalable, maintainable, and testable application.

**Perfect for developers** looking to understand:
- Clean Architecture implementation
- MVVM (Model-View-ViewModel) pattern
- Jetpack Compose for modern UI development
- API integration with Retrofit
- Local data persistence with Room
- Coroutines for asynchronous operations

---

## ✨ Key Features

### 🌡️ Real-time Weather Information
- Current temperature and weather conditions
- Wind speed and humidity
- Sunrise and sunset times
- Detailed hourly and daily forecasts

### 📍 Location-Based Services
- Location-based weather forecasts
- Multiple location support
- Geolocation integration

### 🎨 Modern UI/UX
- Built with Jetpack Compose
- Material Design 3 components
- Responsive and intuitive design
- Dark/Light theme support

### 💾 Data Persistence
- Local caching with Room Database
- Offline access to previously fetched data
- Efficient data management

### 🔄 Auto-Refresh
- Automatic weather updates
- Background sync capabilities
- Smart refresh intervals

### 📊 Rich Weather Data
- Humidity and pressure information
- UV index
- Visibility metrics
- Weather alerts and warnings

---

## 🛠️ Tech Stack

| Layer | Technology | Version |
|-------|-----------|---------|
| **UI Framework** | Jetpack Compose | Latest |
| **Navigation** | Navigation Compose | Latest |
| **Architecture** | MVVM + Clean Architecture | - |
| **API Integration** | Retrofit | 2.11.0 |
| **JSON Parsing** | Gson | 2.11.0 |
| **Database** | Room | 2.6.1 |
| **ViewModel** | Lifecycle ViewModel | 2.8.4 |
| **Async** | Kotlin Coroutines | 1.8.1 |
| **Image Loading** | Coil | 2.5.0 |
| **Language** | Kotlin | Latest |
| **Minimum SDK** | Android 24 | API Level 24 |
| **Target SDK** | Android 36 | API Level 36 |
| **Compile SDK** | 36 | - |
| **JVM Target** | Java 11 | - |

---

## 🏗️ Architecture Overview

This project follows **Clean Architecture** with clear separation of concerns:

```
┌─────────────────────────────────────────┐
│    Presentation Layer (UI)              │
│  (Compose UI + ViewModels)              │
└────────────────┬────────────────────────┘
                 │
┌────────────────▼────────────────────────┐
│    Domain Layer (Business Logic)        │
│  (Use Cases & Repository Interfaces)    │
└────────────────┬────────────────────────┘
                 │
┌────────────────▼────────────────────────┐
│    Data Layer (Data Sources)            │
│  (API, Database, Repositories)          │
└────────────────┬────────────────────────┘
                 │
┌────────────────▼────────────────────────┐
│  External APIs & Local Database         │
│  (Retrofit, Room, Preferences)          │
└─────────────────────────────────────────┘
```

### Layer Responsibilities

**Presentation Layer**
- Compose UI components and screens
- ViewModels for state management
- Navigation between screens

**Domain Layer**
- Business logic and use cases
- Repository interfaces
- Domain models and entities

**Data Layer**
- API client (Retrofit)
- Database entities (Room)
- Repository implementations
- Data source management

---

## 📂 Project Structure

```
weather/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── kotlin/com/swami/weather/
│   │   │   │   ├── presentation/
│   │   │   │   │   ├── screens/         # Compose screens
│   │   │   │   │   ├── components/      # Reusable UI components
│   │   │   │   │   └── viewmodel/       # MVVM ViewModels
│   │   │   │   ├── domain/
│   │   │   │   │   ├── model/           # Domain models
│   │   │   │   │   ├── repository/      # Repository interfaces
│   │   │   │   │   └── usecase/         # Business logic
│   │   │   │   ├── data/
│   │   │   │   │   ├── api/             # Retrofit API client
│   │   │   │   │   ├── db/              # Room database
│   │   │   │   │   ├── repository/      # Repository implementations
│   │   │   │   │   └── datasource/      # Data sources
│   │   │   │   ├── di/                  # Dependency injection (if using Hilt)
│   │   │   │   ├── utils/               # Utility functions
│   │   │   │   └── MainActivity.kt      # App entry point
│   │   │   └── res/
│   │   │       ├── drawable/            # Images and drawables
│   │   │       ├── values/              # Colors, strings, themes
│   │   │       └── mipmap/              # App icons
│   │   └── test/                        # Unit tests
│   │       └── kotlin/                  # Test code
│   └── build.gradle.kts                 # App-level build config
├── build.gradle.kts                     # Project-level build config
├── settings.gradle.kts                  # Project settings
├── gradle.properties                    # Gradle properties
└── README.md                            # This file
```

---

## 🚀 Getting Started

### Prerequisites

- **Android Studio** (latest version recommended)
- **JDK 11** or higher
- **Android SDK 24** (minimum) to **SDK 36** (target)
- **Gradle** (included with Android Studio)
- **Weather API Key** (OpenWeatherMap, WeatherAPI, or similar)

### Installation

1. **Clone the Repository**
```bash
git clone https://github.com/swamirangareddy4543/weather.git
cd weather
```

2. **Add API Key**

Create a local configuration file or add your weather API key:

**Option A: Build.gradle.kts**
```gradle
android {
    defaultConfig {
        buildConfigField("String", "WEATHER_API_KEY", "\"YOUR_API_KEY_HERE\"")
    }
}
```

**Option B: local.properties**
```properties
WEATHER_API_KEY=YOUR_API_KEY_HERE
```

3. **Build the Project**
```bash
./gradlew build
```

4. **Run on Emulator or Device**
```bash
./gradlew installDebug
```

Or directly from Android Studio:
- Connect an Android device or start an emulator
- Click **Run** (or press `Shift + F10`)

---

## 🔧 Development Workflow

### Build Commands

```bash
# Clean build
./gradlew clean

# Build the project
./gradlew build

# Build APK (debug)
./gradlew assembleDebug

# Build APK (release)
./gradlew assembleRelease

# Install on connected device
./gradlew installDebug
```

### Testing

```bash
# Run unit tests
./gradlew test

# Run instrumented tests (on device/emulator)
./gradlew connectedAndroidTest

# Run specific test
./gradlew test:testDebugUnitTest
```

### Code Quality

```bash
# Format code
./gradlew spotlessApply

# Lint checks
./gradlew lint
```

---

## 📋 Key Concepts Explained

### MVVM Architecture

**ViewModel** holds UI state and communicates with UseCases:
```kotlin
class WeatherViewModel(
    private val getWeatherUseCase: GetWeatherUseCase
) : ViewModel() {
    
    private val _weatherState = MutableStateFlow<WeatherState>(Loading)
    val weatherState: StateFlow<WeatherState> = _weatherState.asStateFlow()
    
    fun fetchWeather(city: String) {
        viewModelScope.launch {
            _weatherState.value = getWeatherUseCase(city)
        }
    }
}
```

### Clean Architecture Layers

**Data Layer** - Implements repositories and handles data sources:
```kotlin
class WeatherRepositoryImpl(
    private val api: WeatherApi,
    private val db: WeatherDatabase
) : WeatherRepository {
    
    override suspend fun getWeather(city: String): Weather {
        return try {
            api.getWeather(city).also {
                db.saveWeather(it)  // Cache locally
            }
        } catch (e: Exception) {
            db.getWeather(city)  // Fallback to cache
        }
    }
}
```

### Coroutines & Async Operations

All network and database operations use Kotlin Coroutines:
```kotlin
viewModelScope.launch {
    val weather = withContext(Dispatchers.IO) {
        weatherRepository.getWeather(city)
    }
    // Update UI on Main thread
}
```

---

## 📦 Dependencies Overview

### Core Android
- **androidx.core.ktx** - Kotlin extensions for Android
- **androidx.lifecycle** - Lifecycle management
- **androidx.activity.compose** - Activity integration with Compose

### Jetpack Compose
- **androidx.compose.ui** - Core Compose components
- **androidx.compose.material3** - Material Design 3
- **androidx.navigation.compose** - Navigation

### Networking
- **Retrofit** - Type-safe HTTP client
- **Gson** - JSON serialization/deserialization

### Local Storage
- **Room** - Typed database abstraction
- **androidx.room.ktx** - Kotlin coroutines support

### Async Processing
- **Kotlin Coroutines** - Async and non-blocking programming

### Image Loading
- **Coil** - Efficient image loading and caching

---

## 🔌 API Integration

### Weather API Setup

This app integrates with a weather API (e.g., OpenWeatherMap). Example setup:

```kotlin
// WeatherApi.kt
interface WeatherApi {
    @GET("weather")
    suspend fun getCurrentWeather(
        @Query("q") city: String,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric"
    ): WeatherResponse
    
    @GET("forecast")
    suspend fun getForecast(
        @Query("q") city: String,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric"
    ): ForecastResponse
}
```

### Retrofit Configuration

```kotlin
// RetrofitClient.kt
object RetrofitClient {
    private const val BASE_URL = "https://api.openweathermap.org/data/2.5/"
    
    val instance: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}
```

---

## 💾 Database Schema

### Room Database

```kotlin
// WeatherEntity.kt
@Entity(tableName = "weather")
data class WeatherEntity(
    @PrimaryKey
    val id: String,
    val city: String,
    val temperature: Double,
    val description: String,
    val humidity: Int,
    val windSpeed: Double,
    val timestamp: Long
)
```

---

## 🎨 UI Components

### Main Weather Screen
- Current temperature display
- Weather description and icon
- Hourly forecast
- Daily forecast cards
- Location information

### Search Screen
- City search functionality
- Recent searches
- Suggestions

### Detail Screen
- Extended weather information
- Detailed metrics
- Charts and graphs

---

## 🐛 Troubleshooting

### Build Issues

**Gradle build fails**
```bash
./gradlew clean
./gradlew build --stacktrace
```

**Kotlin version mismatch**
```bash
./gradlew --refresh-dependencies
```

### Runtime Issues

**API key not working**
- Verify API key is correctly set
- Check API rate limits
- Ensure API is enabled in your provider account

**Database migration errors**
```kotlin
// Update Room schema version if needed
@Database(version = 2)
class AppDatabase : RoomDatabase()
```

**Coroutine timeouts**
```kotlin
withTimeoutOrNull(5000) {
    weatherRepository.getWeather(city)
}
```

---

## 📚 Learning Resources

- [Jetpack Compose Documentation](https://developer.android.com/jetpack/compose)
- [Clean Architecture Guide](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)
- [Retrofit Documentation](https://square.github.io/retrofit/)
- [Room Database Guide](https://developer.android.com/training/data-storage/room)
- [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html)
- [MVVM Architecture Pattern](https://developer.android.com/topic/architecture/ui-layer/stateholders)

---

## 🤝 Contributing

Contributions are welcome! Please follow these steps:

1. **Fork** the repository
2. **Create** a feature branch (`git checkout -b feature/amazing-feature`)
3. **Commit** your changes (`git commit -m 'Add amazing feature'`)
4. **Push** to the branch (`git push origin feature/amazing-feature`)
5. **Open** a Pull Request

### Development Guidelines

- Follow Kotlin coding conventions
- Write clean, maintainable code
- Add unit tests for new features
- Update documentation as needed
- Use meaningful commit messages

---

## 📝 License

This project is licensed under the MIT License - see the LICENSE file for details.

---

## 👤 Author

**Swami Ranga Reddy**
- GitHub: [@swamirangareddy4543](https://github.com/swamirangareddy4543)
- Repository: [weather](https://github.com/swamirangareddy4543/weather)

---

## 🙏 Acknowledgments

This project demonstrates modern Android development practices and leverages:
- Android Jetpack team
- Kotlin team
- Retrofit and OkHttp teams
- Room database team
- Coil image loading library

---

## 📞 Support

If you encounter any issues or have questions:
1. Check existing GitHub issues
2. Create a new GitHub issue with:
   - Clear description
   - Steps to reproduce
   - Expected vs actual behavior
   - Android version and device info
3. Include relevant logs and code snippets

---

**Last Updated**: May 2026  
**Project Status**: Active Development  
**Version**: 1.0

---

## 🚀 Quick Start Checklist

- [ ] Clone the repository
- [ ] Set up weather API key
- [ ] Build the project with `./gradlew build`
- [ ] Run on emulator/device
- [ ] Explore the code structure
- [ ] Read through the architecture layers
- [ ] Check out existing features
- [ ] Contribute or modify as needed

**Happy Coding!** ☀️🌙⛅
