package com.growgardentracker.android.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.growgardentracker.android.data.local.entity.GardenZoneEntity
import com.growgardentracker.android.data.local.entity.PlantEntity
import com.growgardentracker.android.ui.components.EmptyStateCard
import com.growgardentracker.android.ui.components.ErrorView
import com.growgardentracker.android.ui.components.GreenPrimaryButton
import com.growgardentracker.android.ui.components.LocalImage
import com.growgardentracker.android.ui.components.PlantCard
import com.growgardentracker.android.ui.components.ScreenHeader
import com.growgardentracker.android.ui.components.ScreenScaffold
import com.growgardentracker.android.ui.components.SectionTitle
import com.growgardentracker.android.ui.components.StatusBadge
import com.growgardentracker.android.ui.components.ZonePhotoMapBox
import com.growgardentracker.android.ui.navigation.Routes
import com.growgardentracker.android.ui.theme.CardCream
import com.growgardentracker.android.ui.theme.GardenDark
import com.growgardentracker.android.ui.theme.TextMuted
import com.growgardentracker.android.util.DateUtils
import com.growgardentracker.android.util.ImageStorageHelper
import com.growgardentracker.android.viewmodel.ActivityViewModel
import com.growgardentracker.android.viewmodel.PlantViewModel
import com.growgardentracker.android.viewmodel.ZoneViewModel
import java.io.File

@Composable
fun PlantListScreen(plantViewModel: PlantViewModel, zoneViewModel: ZoneViewModel, navController: NavHostController, initialFilter: String = "all") {
    val plantState by plantViewModel.state.collectAsState()
    val zoneState by zoneViewModel.state.collectAsState()
    var query by remember { mutableStateOf("") }
    var filter by remember(initialFilter) {
        mutableStateOf(
            when (initialFilter) {
                "overdue" -> "Overdue"
                "dueToday" -> "Water Today"
                else -> "All"
            }
        )
    }
    val screenTitle = when (filter) {
        "Overdue" -> "Overdue Plants"
        "Water Today" -> "Due Today Plants"
        else -> "All Plants"
    }
    val filtered = plantState.plants.filter { plant ->
        val matchesSearch = plant.name.contains(query, true) || plant.plantType.contains(query, true)
        val status = DateUtils.wateringStatus(plant.nextWateringDate)
        val matchesFilter = filter == "All" || filter == status
        matchesSearch && matchesFilter
    }

    ScreenScaffold("Plants", navController) { modifier ->
        LazyColumn(modifier, verticalArrangement = Arrangement.spacedBy(14.dp)) {
            item { ScreenHeader(screenTitle, "Browse, search and care for plants in your garden.") }
            item { ErrorView(plantState.error) }
            item {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text("Search plants") },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("All", "Overdue", "Water Today", "Upcoming").forEach { label ->
                        AssistChip(onClick = { filter = label }, label = { Text(label) })
                    }
                }
            }
            item { GreenPrimaryButton("Add new plant", onClick = { navController.navigate(Routes.ADD_PLANT) }) }
            if (filtered.isEmpty()) {
                item {
                    EmptyStateCard(
                        title = when (filter) {
                            "Overdue" -> "No overdue plants"
                            "Water Today" -> "No plants due today"
                            else -> "No plants added yet"
                        },
                        message = when (filter) {
                            "Overdue" -> "Everything is on schedule right now."
                            "Water Today" -> "No plants need watering today."
                            else -> "Start by adding your first plant to build your garden tracker."
                        },
                        actionText = if (filter == "All") "Add Plant" else null,
                        onAction = { navController.navigate(Routes.ADD_PLANT) }
                    )
                }
            }
            items(filtered) { plant ->
                val zoneName = zoneState.zones.find { it.id == plant.zoneId }?.name ?: "No zone"
                PlantCard(plant, zoneName) { navController.navigate("plantDetails/${plant.id}") }
            }
        }
    }
}

@Composable
fun PlantDetailsScreen(id: Long, plantViewModel: PlantViewModel, zoneViewModel: ZoneViewModel, activityViewModel: ActivityViewModel, navController: NavHostController) {
    val plantState by plantViewModel.state.collectAsState()
    val zoneState by zoneViewModel.state.collectAsState()
    val activityState by activityViewModel.state.collectAsState()
    LaunchedEffect(id) { plantViewModel.observePlant(id); plantViewModel.observeHistory(id) }
    val plant = plantState.selectedPlant
    ScreenScaffold("Plant Details", navController, canGoBack = true) { modifier ->
        if (plant == null) {
            EmptyStateCard("Plant not found", "This plant record could not be loaded.")
        } else {
            val selectedZone = zoneState.zones.firstOrNull { it.id == plant.zoneId }
            val zoneName = selectedZone?.name ?: "No zone"
            LazyColumn(modifier, verticalArrangement = Arrangement.spacedBy(14.dp)) {
                item { LocalImage(plant.plantImagePath) { navController.navigate("image?path=${Uri.encode(plant.plantImagePath)}") } }
                item {
                    Card(colors = CardDefaults.cardColors(containerColor = CardCream), elevation = CardDefaults.cardElevation(3.dp)) {
                        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(plant.name, style = MaterialTheme.typography.headlineSmall, color = GardenDark)
                            Text(plant.plantType, color = TextMuted)
                            StatusBadge(DateUtils.wateringStatus(plant.nextWateringDate))
                        }
                    }
                }
                item {
                    DetailCard("Watering status") {
                        Text("Last watered: ${plant.lastWateredDate}")
                        Text("Next watering: ${plant.nextWateringDate}")
                        Text("Every ${plant.wateringFrequencyDays} days")
                    }
                }
                item {
                    DetailCard("Location") {
                        Text(zoneName)
                        Text(plant.description.ifBlank { "No location description yet." }, color = TextMuted)
                    }
                }
                item {
                    DetailCard("Map pin") {
                        ZonePhotoMapBox(
                            zoneImagePath = selectedZone?.imagePath,
                            plants = emptyList(),
                            selectedX = plant.mapX,
                            selectedY = plant.mapY,
                            selectedPlantName = plant.name,
                            allowTap = false,
                            onTap = null,
                            onPlantClick = null
                        )
                        OutlinedButton(onClick = { navController.navigate("editPlant/${plant.id}") }, modifier = Modifier.fillMaxWidth()) {
                            Text("Edit Map Pin")
                        }
                    }
                }
                item {
                    DetailCard("Notes") {
                        Text(plant.notes.ifBlank { "No notes yet." })
                    }
                }
                item {
                    DetailCard("AI Care Advice") {
                        val advice = PlantCareAssistant.generateAdvice(plant, selectedZone, plantState.history, activityState.activity)
                        advice.forEach { Text(it, color = GardenDark) }
                    }
                }
                item {
                    DetailCard("Location image") {
                        LocalImage(plant.locationImagePath) { navController.navigate("image?path=${Uri.encode(plant.locationImagePath)}") }
                    }
                }
                item {
                    DetailCard("Recent history") {
                        if (plantState.history.isEmpty()) {
                            Text("No history yet.", color = TextMuted)
                        } else {
                            plantState.history.take(3).forEach { Text("${it.date} - ${it.note}") }
                        }
                    }
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { plantViewModel.waterToday(plant) }, modifier = Modifier.weight(1f)) { Text("Water Today") }
                        OutlinedButton(onClick = { navController.navigate("editPlant/${plant.id}") }, modifier = Modifier.weight(1f)) { Text("Edit") }
                    }
                }
                item { OutlinedButton(onClick = { plantViewModel.deletePlant(plant) { navController.popBackStack() } }, modifier = Modifier.fillMaxWidth()) { Text("Delete") } }
                item { OutlinedButton(onClick = { navController.navigate("history/${plant.id}") }, modifier = Modifier.fillMaxWidth()) { Text("Plant history") } }
            }
        }
    }
}

@Composable
fun AddPlantScreen(plantViewModel: PlantViewModel, zoneViewModel: ZoneViewModel, navController: NavHostController) {
    PlantFormScreen(null, plantViewModel, zoneViewModel, navController)
}

@Composable
fun EditPlantScreen(id: Long, plantViewModel: PlantViewModel, zoneViewModel: ZoneViewModel, navController: NavHostController) {
    val state by plantViewModel.state.collectAsState()
    LaunchedEffect(id) { plantViewModel.observePlant(id) }
    PlantFormScreen(state.selectedPlant, plantViewModel, zoneViewModel, navController)
}

@Composable
private fun PlantFormScreen(existing: PlantEntity?, plantViewModel: PlantViewModel, zoneViewModel: ZoneViewModel, navController: NavHostController) {
    val context = LocalContext.current
    val zones by zoneViewModel.state.collectAsState()
    val plantState by plantViewModel.state.collectAsState()
    var name by remember(existing) { mutableStateOf(existing?.name ?: "") }
    var type by remember(existing) { mutableStateOf(existing?.plantType ?: "Vegetable") }
    var zoneId by remember(existing?.id) { mutableStateOf(existing?.zoneId) }
    var description by remember(existing) { mutableStateOf(existing?.description ?: "") }
    var plantedDate by remember(existing) { mutableStateOf(existing?.plantedDate ?: DateUtils.today()) }
    var lastWatered by remember(existing) { mutableStateOf(existing?.lastWateredDate ?: DateUtils.today()) }
    var frequency by remember(existing) { mutableStateOf((existing?.wateringFrequencyDays ?: 3).toString()) }
    var notes by remember(existing) { mutableStateOf(existing?.notes ?: "") }
    var plantImage by remember(existing) { mutableStateOf(existing?.plantImagePath ?: "") }
    var locationImage by remember(existing) { mutableStateOf(existing?.locationImagePath ?: "") }
    var mapX by remember(existing) { mutableStateOf(existing?.mapX) }
    var mapY by remember(existing) { mutableStateOf(existing?.mapY) }
    var pickingPlantImage by remember { mutableStateOf(true) }
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            val copied = ImageStorageHelper.copyUriToInternalStorage(context, it)
            if (pickingPlantImage) plantImage = copied else locationImage = copied
        }
    }
    LaunchedEffect(existing?.id, existing?.zoneId, zones.zones) {
        if (zoneId == null) {
            zoneId = existing?.zoneId ?: zones.zones.firstOrNull()?.id
        }
    }
    val selectedZone = zones.zones.firstOrNull { it.id == zoneId }
    val selectedZoneImagePath = selectedZone?.imagePath.orEmpty()

    ScreenScaffold(if (existing == null) "Add Plant" else "Edit Plant", navController, canGoBack = true) { modifier ->
        LazyColumn(modifier, verticalArrangement = Arrangement.spacedBy(14.dp)) {
            item { ErrorView(plantState.error) }
            item { FormSection("Plant information") {
                RoundedField(name, { name = it }, "Plant name")
                RoundedField(type, { type = it }, "Plant type")
            } }
            item { FormSection("Location") {
                ZonePicker(zones.zones, zoneId) { selectedId ->
                    if (zoneId != selectedId) {
                        zoneId = selectedId
                        mapX = null
                        mapY = null
                    }
                }
                RoundedField(description, { description = it }, "Location description")
            } }
            item {
                FormSection("Set Map Pin") {
                    when {
                        selectedZone == null -> {
                            Text("Select a zone first.", color = TextMuted)
                        }
                        selectedZoneImagePath.isBlank() -> {
                            Text("This zone has no photo. Add a zone photo first to place the plant pin.", color = TextMuted)
                            OutlinedButton(
                                onClick = { navController.navigate(Routes.ZONES) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Open Zone Manager")
                            }
                        }
                        else -> {
                            Text(
                                if (mapX == null || mapY == null) "Tap on the zone photo to place this plant." else "Pin saved at x ${"%.2f".format(mapX)} and y ${"%.2f".format(mapY)}.",
                                color = TextMuted
                            )
                            ZonePhotoMapBox(
                                zoneImagePath = selectedZoneImagePath,
                                plants = emptyList(),
                                selectedX = mapX,
                                selectedY = mapY,
                                selectedPlantName = name.ifBlank { "Plant" },
                                allowTap = true,
                                onTap = { x, y ->
                                    mapX = x
                                    mapY = y
                                },
                                onPlantClick = null
                            )
                            OutlinedButton(onClick = { mapX = null; mapY = null }, modifier = Modifier.fillMaxWidth()) {
                                Text("Clear pin")
                            }
                        }
                    }
                }
            }
            item { FormSection("Watering") {
                RoundedField(plantedDate, { plantedDate = it }, "Planted date")
                RoundedField(lastWatered, { lastWatered = it }, "Last watered date")
                RoundedField(frequency, { frequency = it }, "Watering frequency days")
            } }
            item { FormSection("Images") {
                ImagePickerCard("Plant image", plantImage) { pickingPlantImage = true; imagePicker.launch("image/*") }
                Spacer(Modifier.height(10.dp))
                ImagePickerCard("Location image", locationImage) { pickingPlantImage = false; imagePicker.launch("image/*") }
            } }
            item { FormSection("Notes") { RoundedField(notes, { notes = it }, "Notes") } }
            item {
                GreenPrimaryButton("Save plant", onClick = {
                    val freq = frequency.toIntOrNull() ?: 3
                    val next = DateUtils.nextWateringDate(lastWatered, freq)
                    val plant = PlantEntity(
                        id = existing?.id ?: 0,
                        name = name,
                        plantType = type,
                        zoneId = zoneId,
                        description = description,
                        plantedDate = plantedDate,
                        lastWateredDate = lastWatered,
                        nextWateringDate = next,
                        wateringFrequencyDays = freq,
                        notes = notes,
                        plantImagePath = plantImage,
                        locationImagePath = locationImage,
                        mapX = mapX,
                        mapY = mapY,
                        createdAt = existing?.createdAt ?: System.currentTimeMillis()
                    )
                    plantViewModel.savePlant(plant) { navController.popBackStack() }
                })
            }
        }
    }
}

@Composable
private fun ZonePicker(zones: List<GardenZoneEntity>, selectedId: Long?, onSelect: (Long) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Garden zone", fontWeight = FontWeight.SemiBold, color = GardenDark)
        zones.forEach { zone ->
            AssistChip(onClick = { onSelect(zone.id) }, label = { Text(if (zone.id == selectedId) "${zone.name} selected" else zone.name) })
        }
    }
}

@Composable
fun PlantHistoryScreen(id: Long, plantViewModel: PlantViewModel, navController: NavHostController) {
    val state by plantViewModel.state.collectAsState()
    var note by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(DateUtils.today()) }
    LaunchedEffect(id) { plantViewModel.observeHistory(id) }
    ScreenScaffold("Plant History", navController, canGoBack = true) { modifier ->
        LazyColumn(modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item { FormSection("Add history note") {
                RoundedField(date, { date = it }, "Date")
                RoundedField(note, { note = it }, "Note")
                GreenPrimaryButton("Save history", onClick = { plantViewModel.addHistory(id, date, note); note = "" })
            } }
            if (state.history.isEmpty()) item { EmptyStateCard("No history yet", "Watering and care notes will appear here.") }
            items(state.history) { history ->
                Card(colors = CardDefaults.cardColors(containerColor = CardCream)) {
                    Column(Modifier.fillMaxWidth().padding(14.dp)) {
                        Text(history.date, color = TextMuted)
                        Text(history.note, style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }
    }
}

@Composable
fun FullScreenImagePreview(path: String, navController: NavHostController) {
    ScreenScaffold("Image Preview", navController, canGoBack = true) { modifier ->
        if (path.isBlank() || !File(path).exists()) {
            EmptyStateCard("Image not found", "The selected local image is not available.")
        } else {
            AsyncImage(model = File(path), contentDescription = "Preview", modifier = modifier.fillMaxSize())
        }
    }
}

@Composable
private fun DetailCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = CardCream), elevation = CardDefaults.cardElevation(2.dp)) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = GardenDark)
            content()
        }
    }
}

@Composable
private fun FormSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = CardCream), elevation = CardDefaults.cardElevation(2.dp)) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = GardenDark)
            content()
        }
    }
}

@Composable
private fun RoundedField(value: String, onValueChange: (String) -> Unit, label: String) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun ImagePickerCard(title: String, path: String, onPick: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, fontWeight = FontWeight.SemiBold, color = GardenDark)
        LocalImage(path, Modifier.fillMaxWidth().height(150.dp))
        OutlinedButton(onClick = onPick, modifier = Modifier.fillMaxWidth()) { Text("Choose image") }
    }
}
