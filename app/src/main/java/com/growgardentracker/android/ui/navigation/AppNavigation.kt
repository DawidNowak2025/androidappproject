package com.growgardentracker.android.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.growgardentracker.android.ui.screens.ActivityLogScreen
import com.growgardentracker.android.ui.screens.AddPlantScreen
import com.growgardentracker.android.ui.screens.DashboardScreen
import com.growgardentracker.android.ui.screens.EditPlantScreen
import com.growgardentracker.android.ui.screens.FullScreenImagePreview
import com.growgardentracker.android.ui.screens.GardenMapScreen
import com.growgardentracker.android.ui.screens.LoginScreen
import com.growgardentracker.android.ui.screens.PlantApiSearchScreen
import com.growgardentracker.android.ui.screens.PlantDetailsScreen
import com.growgardentracker.android.ui.screens.PlantHistoryScreen
import com.growgardentracker.android.ui.screens.PlantListScreen
import com.growgardentracker.android.ui.screens.RegisterScreen
import com.growgardentracker.android.ui.screens.SettingsScreen
import com.growgardentracker.android.ui.screens.WateringScheduleScreen
import com.growgardentracker.android.ui.screens.WeatherAdviceScreen
import com.growgardentracker.android.ui.screens.ZoneManagerScreen
import com.growgardentracker.android.viewmodel.ActivityViewModel
import com.growgardentracker.android.viewmodel.AuthViewModel
import com.growgardentracker.android.viewmodel.DashboardViewModel
import com.growgardentracker.android.viewmodel.PlantViewModel
import com.growgardentracker.android.viewmodel.SettingsViewModel
import com.growgardentracker.android.viewmodel.ZoneViewModel

object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val DASHBOARD = "dashboard"
    const val PLANTS = "plants"
    const val PLANT_DETAILS = "plantDetails/{id}"
    const val ADD_PLANT = "addPlant"
    const val EDIT_PLANT = "editPlant/{id}"
    const val GARDEN_MAP = "gardenMap"
    const val ZONES = "zones"
    const val WATERING = "watering"
    const val HISTORY = "history/{id}"
    const val ACTIVITY = "activity"
    const val SEARCH = "search"
    const val WEATHER = "weather"
    const val SETTINGS = "settings"
    const val IMAGE = "image?path={path}"
}

@Composable
fun AppNavigation(navController: NavHostController = rememberNavController()) {
    val authViewModel: AuthViewModel = viewModel()
    val plantViewModel: PlantViewModel = viewModel()
    val zoneViewModel: ZoneViewModel = viewModel()
    val dashboardViewModel: DashboardViewModel = viewModel()
    val activityViewModel: ActivityViewModel = viewModel()
    val settingsViewModel: SettingsViewModel = viewModel()

    NavHost(navController = navController, startDestination = Routes.LOGIN) {
        composable(Routes.LOGIN) { LoginScreen(authViewModel, navController) }
        composable(Routes.REGISTER) { RegisterScreen(authViewModel, navController) }
            composable(Routes.DASHBOARD) { DashboardScreen(dashboardViewModel, plantViewModel, zoneViewModel, navController) }
        composable(Routes.PLANTS) { PlantListScreen(plantViewModel, zoneViewModel, navController) }
        composable(Routes.PLANT_DETAILS, arguments = listOf(navArgument("id") { type = NavType.LongType })) {
            PlantDetailsScreen(it.arguments?.getLong("id") ?: 0L, plantViewModel, zoneViewModel, navController)
        }
        composable(Routes.ADD_PLANT) { AddPlantScreen(plantViewModel, zoneViewModel, navController) }
        composable(Routes.EDIT_PLANT, arguments = listOf(navArgument("id") { type = NavType.LongType })) {
            EditPlantScreen(it.arguments?.getLong("id") ?: 0L, plantViewModel, zoneViewModel, navController)
        }
        composable(Routes.GARDEN_MAP) { GardenMapScreen(plantViewModel, zoneViewModel, navController) }
            composable(Routes.ZONES) { ZoneManagerScreen(zoneViewModel, plantViewModel, navController) }
            composable(Routes.WATERING) { WateringScheduleScreen(plantViewModel, zoneViewModel, navController) }
        composable(Routes.HISTORY, arguments = listOf(navArgument("id") { type = NavType.LongType })) {
            PlantHistoryScreen(it.arguments?.getLong("id") ?: 0L, plantViewModel, navController)
        }
        composable(Routes.ACTIVITY) { ActivityLogScreen(activityViewModel, navController) }
        composable(Routes.SEARCH) { PlantApiSearchScreen(navController) }
        composable(Routes.WEATHER) { WeatherAdviceScreen(plantViewModel, navController) }
        composable(Routes.SETTINGS) { SettingsScreen(authViewModel, settingsViewModel, navController) }
        composable(
            Routes.IMAGE,
            arguments = listOf(navArgument("path") { type = NavType.StringType; defaultValue = "" })
        ) { FullScreenImagePreview(it.arguments?.getString("path").orEmpty(), navController) }
    }
}
