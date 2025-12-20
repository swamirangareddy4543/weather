package com.swami.weather.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.swami.weather.screen.SearchScreen
import com.swami.weather.screen.WeatherResultScreen
import com.swami.weather.screen.WeatherViewModel

sealed class Screen(val route: String) {
    object Search : Screen("search")
    object WeatherResult : Screen("weather_result/{cityName}") {
        fun createRoute(cityName: String) = "weather_result/$cityName"
    }
}

@Composable
fun WeatherNavGraph(
    navController: NavHostController,
    viewModel: WeatherViewModel
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Search.route
    ) {
        composable(Screen.Search.route) {
            SearchScreen(
                viewModel = viewModel,
                onNavigateToWeather = { cityName ->
                    navController.navigate(Screen.WeatherResult.createRoute(cityName)) {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(
            route = Screen.WeatherResult.route,
            arguments = listOf(
                navArgument("cityName") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val cityName = backStackEntry.arguments?.getString("cityName") ?: ""
            WeatherResultScreen(
                viewModel = viewModel,
                cityName = cityName,
                onNavigateBack = {
                    viewModel.clearNavigationState()
                    navController.popBackStack()
                }
            )
        }
    }
}

