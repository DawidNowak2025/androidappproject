package com.growgardentracker.android.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.growgardentracker.android.ui.components.AppIcons
import com.growgardentracker.android.ui.components.DashboardStatCard
import com.growgardentracker.android.ui.components.EmptyStateCard
import com.growgardentracker.android.ui.components.GardenHeroHeader
import com.growgardentracker.android.ui.components.QuickActionCard
import com.growgardentracker.android.ui.components.ScreenScaffold
import com.growgardentracker.android.ui.components.SectionTitle
import com.growgardentracker.android.ui.components.StatusBadge
import com.growgardentracker.android.ui.navigation.Routes
import com.growgardentracker.android.ui.theme.CardCream
import com.growgardentracker.android.ui.theme.DueOrange
import com.growgardentracker.android.ui.theme.GardenDark
import com.growgardentracker.android.ui.theme.TextMuted
import com.growgardentracker.android.ui.theme.WarningRed
import com.growgardentracker.android.util.DateUtils
import com.growgardentracker.android.viewmodel.DashboardViewModel
import com.growgardentracker.android.viewmodel.PlantViewModel
import com.growgardentracker.android.viewmodel.ZoneViewModel

@Composable
fun DashboardScreen(
    dashboardViewModel: DashboardViewModel,
    plantViewModel: PlantViewModel,
    zoneViewModel: ZoneViewModel,
    navController: NavHostController
) {
    val dashboardState by dashboardViewModel.state.collectAsState()
    val plantState by plantViewModel.state.collectAsState()
    val zoneState by zoneViewModel.state.collectAsState()
    val plants = plantState.plants
    val dueToday = plants.count { DateUtils.wateringStatus(it.nextWateringDate) == "Water Today" }
    val upcomingPlants = plants.sortedBy { it.nextWateringDate }.take(4)

    ScreenScaffold("Dashboard", navController) { modifier ->
        LazyColumn(modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item {
                GardenHeroHeader()
            }
            item {
                if (plants.isEmpty()) {
                    EmptyStateCard(
                        title = "No plants added yet",
                        message = "Start by adding your first plant and your dashboard will fill with watering and zone insights.",
                        actionText = "Add Plant",
                        onAction = { navController.navigate(Routes.ADD_PLANT) }
                    )
                }
            }
            item { SectionTitle("Garden overview", "A quick look at your local garden records") }
            item {
                BoxWithConstraints {
                    if (maxWidth < 360.dp) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            DashboardStatCard("Total plants", dashboardState.summary.totalPlants.toString(), "View all plants") { navController.navigate("plants/all") }
                            DashboardStatCard("Due today", dueToday.toString(), "Due today", DueOrange) { navController.navigate("plants/dueToday") }
                            DashboardStatCard("Overdue", dashboardState.summary.overduePlants.toString(), "Check overdue", WarningRed) { navController.navigate("plants/overdue") }
                            DashboardStatCard("Active zones", zoneState.zones.size.toString(), "Open zones", MaterialTheme.colorScheme.primary) { navController.navigate(Routes.ZONES) }
                        }
                    } else {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        DashboardStatCard(
                            "Total plants",
                            dashboardState.summary.totalPlants.toString(),
                            "View all plants",
                            onClick = { navController.navigate("plants/all") }
                        )
                        DashboardStatCard(
                            "Due today",
                            dueToday.toString(),
                            "Due today",
                            DueOrange,
                            onClick = { navController.navigate("plants/dueToday") }
                        )
                    }
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        DashboardStatCard(
                            "Overdue",
                            dashboardState.summary.overduePlants.toString(),
                            "Check overdue",
                            WarningRed,
                            onClick = { navController.navigate("plants/overdue") }
                        )
                        DashboardStatCard(
                            "Active zones",
                            zoneState.zones.size.toString(),
                            "Open zones",
                            MaterialTheme.colorScheme.primary,
                            onClick = { navController.navigate(Routes.ZONES) }
                        )
                    }
                        }
                    }
                }
            }
            item { SectionTitle("Quick actions", "Common garden tasks") }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    QuickActionCard("Add Plant", "Create a new plant record", AppIcons.Add) { navController.navigate(Routes.ADD_PLANT) }
                    QuickActionCard("Garden Map", "See plant pins on zone photos", AppIcons.Zones) { navController.navigate(Routes.GARDEN_MAP) }
                    QuickActionCard("Plant Knowledge", "Search Wikipedia plant pages", AppIcons.Knowledge) { navController.navigate(Routes.SEARCH) }
                    QuickActionCard("AI Assistant", "Local rule-based plant care advice", AppIcons.Assistant) { navController.navigate(Routes.ASSISTANT) }
                    QuickActionCard("Zones", "Manage growing areas and beds", AppIcons.Zones) { navController.navigate(Routes.ZONES) }
                    QuickActionCard("Activity Log", "Review recent local garden actions", AppIcons.Activity) { navController.navigate(Routes.ACTIVITY) }
                }
            }
            item { SectionTitle("Upcoming watering", "Plants ordered by next watering date") }
            if (upcomingPlants.isEmpty()) {
                item { EmptyStateCard("No watering schedule yet", "Add plants with watering dates to see the next jobs here.") }
            } else {
                items(upcomingPlants) { plant ->
                    Card(colors = CardDefaults.cardColors(containerColor = CardCream), elevation = CardDefaults.cardElevation(2.dp)) {
                        Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text(plant.name, style = MaterialTheme.typography.titleMedium, color = GardenDark, modifier = Modifier.weight(1f))
                                StatusBadge(DateUtils.wateringStatus(plant.nextWateringDate))
                            }
                            Text("Next watering: ${plant.nextWateringDate}", color = TextMuted)
                            Text("Last watered: ${plant.lastWateredDate}", color = TextMuted)
                        }
                    }
                }
            }
            item { SectionTitle("Zones overview", "Where your plants are growing") }
            if (zoneState.zones.isEmpty()) {
                item { EmptyStateCard("No zones yet", "Default zones will appear after the local database is ready.") }
            } else {
                items(zoneState.zones.take(4)) { zone ->
                    val count = plants.count { it.zoneId == zone.id }
                    Card(colors = CardDefaults.cardColors(containerColor = CardCream), elevation = CardDefaults.cardElevation(2.dp)) {
                        Row(Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text(zone.name, style = MaterialTheme.typography.titleMedium, color = GardenDark)
                                Text("$count plants in this zone", color = TextMuted)
                            }
                        }
                    }
                }
            }
            item { SectionTitle("Recent activity", "Latest changes in your tracker") }
            if (dashboardState.summary.recentActivity.isEmpty()) {
                item { EmptyStateCard("No activity yet", "Actions like adding plants, watering and editing zones will appear here.") }
            } else {
                items(dashboardState.summary.recentActivity) { log ->
                    Card(colors = CardDefaults.cardColors(containerColor = CardCream), elevation = CardDefaults.cardElevation(2.dp)) {
                        Column(Modifier.fillMaxWidth().padding(14.dp)) {
                            Text(log.date, color = TextMuted, style = MaterialTheme.typography.bodyMedium)
                            Text(log.text, color = GardenDark, style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
            }
        }
    }
}
