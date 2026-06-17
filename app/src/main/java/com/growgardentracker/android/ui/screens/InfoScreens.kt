package com.growgardentracker.android.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.growgardentracker.android.data.local.assistant.PlantCareAssistant
import com.growgardentracker.android.data.local.entity.PlantEntity
import com.growgardentracker.android.data.remote.wiki.WikiPage
import com.growgardentracker.android.ui.components.EmptyStateCard
import com.growgardentracker.android.ui.components.ErrorView
import com.growgardentracker.android.ui.components.GreenPrimaryButton
import com.growgardentracker.android.ui.components.ScreenHeader
import com.growgardentracker.android.ui.components.ScreenScaffold
import com.growgardentracker.android.ui.components.SectionTitle
import com.growgardentracker.android.ui.components.StatusBadge
import com.growgardentracker.android.ui.navigation.Routes
import com.growgardentracker.android.ui.theme.CardCream
import com.growgardentracker.android.ui.theme.GardenDark
import com.growgardentracker.android.ui.theme.TextMuted
import com.growgardentracker.android.util.DateUtils
import com.growgardentracker.android.viewmodel.ActivityViewModel
import com.growgardentracker.android.viewmodel.AuthViewModel
import com.growgardentracker.android.viewmodel.PlantViewModel
import com.growgardentracker.android.viewmodel.SettingsViewModel
import com.growgardentracker.android.viewmodel.WikiSearchViewModel
import com.growgardentracker.android.viewmodel.ZoneViewModel

@Composable
fun WateringScheduleScreen(plantViewModel: PlantViewModel, zoneViewModel: ZoneViewModel, navController: NavHostController) {
    val plantState by plantViewModel.state.collectAsState()
    val zoneState by zoneViewModel.state.collectAsState()
    val overdue = plantState.plants.filter { DateUtils.wateringStatus(it.nextWateringDate) == "Overdue" }
    val today = plantState.plants.filter { DateUtils.wateringStatus(it.nextWateringDate) == "Water Today" }
    val upcoming = plantState.plants.filter { DateUtils.wateringStatus(it.nextWateringDate) == "Upcoming" }.sortedBy { it.nextWateringDate }

    ScreenScaffold("Watering Schedule", navController, canGoBack = true) { modifier ->
        LazyColumn(modifier, verticalArrangement = Arrangement.spacedBy(14.dp)) {
            item { ScreenHeader("Watering Schedule", "Prioritise overdue plants, today's watering and upcoming care.") }
            if (plantState.plants.isEmpty()) {
                item { EmptyStateCard("No watering schedule yet", "Add plants with watering frequency to create a schedule.", "Add Plant") { navController.navigate(Routes.ADD_PLANT) } }
            }
            wateringSection("Overdue", overdue, zoneState.zones.associate { it.id to it.name }, plantViewModel)
            wateringSection("Due today", today, zoneState.zones.associate { it.id to it.name }, plantViewModel)
            wateringSection("Upcoming", upcoming, zoneState.zones.associate { it.id to it.name }, plantViewModel)
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.wateringSection(
    title: String,
    plants: List<PlantEntity>,
    zoneNames: Map<Long, String>,
    plantViewModel: PlantViewModel
) {
    item { SectionTitle(title, "${plants.size} plants") }
    if (plants.isEmpty()) {
        item { EmptyStateCard("No plants in $title", "This section is clear right now.") }
    } else {
        items(plants) { plant ->
            val zoneName = plant.zoneId?.let { zoneNames[it] } ?: "No zone"
            WateringCard(plant, zoneName) { plantViewModel.waterToday(plant) }
        }
    }
}

@Composable
private fun WateringCard(plant: PlantEntity, zoneName: String, onWater: () -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = CardCream), elevation = CardDefaults.cardElevation(3.dp)) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(plant.name, style = MaterialTheme.typography.titleMedium, color = GardenDark, modifier = Modifier.weight(1f))
                StatusBadge(DateUtils.wateringStatus(plant.nextWateringDate))
            }
            Text(zoneName, color = TextMuted)
            Text("Last watered: ${plant.lastWateredDate}", color = TextMuted)
            Text("Next watering: ${plant.nextWateringDate}", color = TextMuted)
            GreenPrimaryButton("Water Today", onWater)
        }
    }
}

@Composable
fun ActivityLogScreen(viewModel: ActivityViewModel, navController: NavHostController) {
    val state by viewModel.state.collectAsState()
    ScreenScaffold("Activity Log", navController, canGoBack = true) { modifier ->
        LazyColumn(modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item { ScreenHeader("Activity Timeline", "A local record of plant, zone and backup actions.") }
            if (state.activity.isEmpty()) item { EmptyStateCard("No activity yet", "Actions like watering, editing and exporting will appear here.") }
            items(state.activity) { log ->
                Card(colors = CardDefaults.cardColors(containerColor = CardCream), elevation = CardDefaults.cardElevation(2.dp)) {
                    Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(log.date, color = TextMuted, modifier = Modifier.weight(0.34f))
                        Column(Modifier.weight(0.66f)) {
                            Text("Garden action", style = MaterialTheme.typography.titleMedium, color = GardenDark)
                            Text(log.text, color = TextMuted)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PlantApiSearchScreen(viewModel: WikiSearchViewModel, navController: NavHostController) {
    val state by viewModel.state.collectAsState()
    ScreenScaffold("Plant Knowledge", navController, canGoBack = true) { modifier ->
        LazyColumn(modifier, verticalArrangement = Arrangement.spacedBy(14.dp)) {
            item { ScreenHeader("Plant Knowledge", "Search Wikipedia for plant pages, descriptions and images.") }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        state.query,
                        { viewModel.updateQuery(it) },
                        label = { Text("Search Wikipedia plant knowledge") },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    GreenPrimaryButton("Search Wikipedia", onClick = { viewModel.search() })
                }
            }
            if (state.isLoading) {
                item { CircularProgressIndicator() }
            }
            if (state.error.isNotBlank()) {
                item { EmptyStateCard("Plant knowledge unavailable", state.error) }
            }
            if (!state.isLoading && state.error.isBlank() && state.results.isEmpty()) {
                item { EmptyStateCard("Search for a plant", "Try tomato, mint, dahlia, peace lily or strawberry.") }
            }
            items(state.results) { page ->
                WikiResultCard(page)
            }
        }
    }
}

@Composable
private fun WikiResultCard(page: WikiPage) {
    val context = LocalContext.current
    val pageUrl = page.contentUrls?.desktop?.page ?: page.contentUrls?.mobile?.page ?: "https://en.wikipedia.org/wiki/${page.key}"
    Card(colors = CardDefaults.cardColors(containerColor = CardCream), elevation = CardDefaults.cardElevation(2.dp)) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                page.thumbnail?.url?.let { url ->
                    AsyncImage(model = url, contentDescription = page.title, modifier = Modifier.size(82.dp))
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(page.title, style = MaterialTheme.typography.titleLarge, color = GardenDark)
                    Text(page.description ?: "Wikipedia plant result", color = TextMuted, fontWeight = FontWeight.SemiBold)
                }
            }
            Text(page.excerpt?.replace(Regex("<.*?>"), "").orEmpty().ifBlank { "Open Wikipedia to read more." }, color = TextMuted)
            OutlinedButton(
                onClick = { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(pageUrl))) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Open on Wikipedia")
            }
        }
    }
}

@Composable
fun PlantAssistantScreen(
    plantViewModel: PlantViewModel,
    zoneViewModel: ZoneViewModel,
    activityViewModel: ActivityViewModel,
    navController: NavHostController
) {
    val plantState by plantViewModel.state.collectAsState()
    val zoneState by zoneViewModel.state.collectAsState()
    val activityState by activityViewModel.state.collectAsState()
    var selectedPlantId by remember { mutableStateOf<Long?>(null) }
    val selectedPlant = plantState.plants.firstOrNull { it.id == selectedPlantId } ?: plantState.plants.firstOrNull()
    LaunchedEffect(selectedPlant?.id) {
        selectedPlant?.let {
            selectedPlantId = it.id
            plantViewModel.observeHistory(it.id)
        }
    }

    ScreenScaffold("AI Plant Care Assistant", navController, canGoBack = true) { modifier ->
        LazyColumn(modifier, verticalArrangement = Arrangement.spacedBy(14.dp)) {
            item { ScreenHeader("AI Plant Care Assistant", "Local rule-based advice from your plants, zones, notes and history.") }
            if (plantState.plants.isEmpty()) {
                item { EmptyStateCard("No plants to analyse", "Add a plant first, then the assistant can generate care advice.", "Add Plant") { navController.navigate(Routes.ADD_PLANT) } }
            }
            if (plantState.plants.isNotEmpty()) {
                item {
                    SectionTitle("Choose a plant", "Advice is generated locally on this device")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        plantState.plants.take(6).forEach { plant ->
                            AssistChip(
                                onClick = { selectedPlantId = plant.id },
                                label = { Text(if (plant.id == selectedPlant?.id) "${plant.name} selected" else plant.name) }
                            )
                        }
                    }
                }
                selectedPlant?.let { plant ->
                    val zone = zoneState.zones.firstOrNull { it.id == plant.zoneId }
                    val advice = PlantCareAssistant.generateAdvice(plant, zone, plantState.history, activityState.activity)
                    item {
                        Card(colors = CardDefaults.cardColors(containerColor = CardCream), elevation = CardDefaults.cardElevation(3.dp)) {
                            Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text(plant.name, style = MaterialTheme.typography.titleLarge, color = GardenDark)
                                Text(zone?.name ?: "No zone assigned", color = TextMuted)
                                StatusBadge(DateUtils.wateringStatus(plant.nextWateringDate))
                            }
                        }
                    }
                    item { SectionTitle("Care advice", "Generated from local Room data") }
                    items(advice) { item ->
                        Card(colors = CardDefaults.cardColors(containerColor = CardCream), elevation = CardDefaults.cardElevation(1.dp)) {
                            Text(item, Modifier.fillMaxWidth().padding(14.dp), color = GardenDark)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsScreen(authViewModel: AuthViewModel, settingsViewModel: SettingsViewModel, navController: NavHostController) {
    val state by settingsViewModel.state.collectAsState()
    ScreenScaffold("Settings", navController) { modifier ->
        LazyColumn(modifier, verticalArrangement = Arrangement.spacedBy(14.dp)) {
            item { ScreenHeader("Settings", "Manage local backup, restore and session options.") }
            item { ErrorView(state.error) }
            if (state.message.isNotBlank()) {
                item {
                    Card(colors = CardDefaults.cardColors(containerColor = CardCream)) {
                        Text(state.message, Modifier.padding(14.dp), color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
            item {
                Card(colors = CardDefaults.cardColors(containerColor = CardCream), elevation = CardDefaults.cardElevation(2.dp)) {
                    Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Local app data", style = MaterialTheme.typography.titleMedium, color = GardenDark)
                        Text("This standalone app stores plants, zones, users and activity on this device.", color = TextMuted)
                        GreenPrimaryButton("Export JSON", onClick = { settingsViewModel.exportJson() })
                        GreenPrimaryButton("Import JSON", onClick = { settingsViewModel.importJson() })
                    }
                }
            }
            item {
                GreenPrimaryButton("Logout", onClick = {
                    authViewModel.logout()
                    navController.navigate(Routes.LOGIN) { popUpTo(Routes.DASHBOARD) { inclusive = true } }
                })
            }
        }
    }
}
