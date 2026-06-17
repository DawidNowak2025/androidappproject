package com.growgardentracker.android.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.growgardentracker.android.data.local.entity.GardenZoneEntity
import com.growgardentracker.android.ui.components.EmptyStateCard
import com.growgardentracker.android.ui.components.ErrorView
import com.growgardentracker.android.ui.components.GreenPrimaryButton
import com.growgardentracker.android.ui.components.LocalImage
import com.growgardentracker.android.ui.components.ScreenHeader
import com.growgardentracker.android.ui.components.ScreenScaffold
import com.growgardentracker.android.ui.components.SectionTitle
import com.growgardentracker.android.ui.components.ZonePhotoMapBox
import com.growgardentracker.android.ui.theme.CardCream
import com.growgardentracker.android.ui.theme.GardenDark
import com.growgardentracker.android.ui.theme.TextMuted
import com.growgardentracker.android.util.ImageStorageHelper
import com.growgardentracker.android.viewmodel.PlantViewModel
import com.growgardentracker.android.viewmodel.ZoneViewModel

@Composable
fun GardenMapScreen(plantViewModel: PlantViewModel, zoneViewModel: ZoneViewModel, navController: NavHostController) {
    val plants by plantViewModel.state.collectAsState()
    val zones by zoneViewModel.state.collectAsState()
    ScreenScaffold("Garden Map", navController) { modifier ->
        LazyColumn(modifier, verticalArrangement = Arrangement.spacedBy(14.dp)) {
            item { ScreenHeader("Garden Map", "A mobile overview of your growing zones and the plants inside them.") }
            item { SectionTitle("Zone overview", "Photos, map pins and plants grouped by zone") }
            if (zones.zones.isEmpty()) {
                item { EmptyStateCard("No garden zones added yet", "Add a zone to start mapping your garden.") }
            }
            items(zones.zones) { zone ->
                val zonePlants = plants.plants.filter { it.zoneId == zone.id }
                val pinnedPlants = zonePlants.filter { it.mapX != null && it.mapY != null }
                val unpinnedPlants = zonePlants.filter { it.mapX == null || it.mapY == null }
                ZoneSummaryCard(zone = zone, plantCount = zonePlants.size) {
                    ZonePhotoMapBox(
                        zoneImagePath = zone.imagePath,
                        plants = pinnedPlants,
                        selectedX = null,
                        selectedY = null,
                        selectedPlantName = null,
                        allowTap = false,
                        onTap = null,
                        onPlantClick = { plant -> navController.navigate("plantDetails/${plant.id}") }
                    )
                    if (zone.imagePath?.isNotBlank() == true && pinnedPlants.isEmpty()) {
                        Text("No pins in this zone yet.", color = TextMuted)
                    }
                    if (unpinnedPlants.isNotEmpty()) {
                        Text("Plants without pin", style = MaterialTheme.typography.titleMedium, color = GardenDark)
                        unpinnedPlants.forEach { plant ->
                            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(1.dp)) {
                                Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Column(Modifier.weight(1f)) {
                                        Text(plant.name, fontWeight = FontWeight.SemiBold, color = GardenDark)
                                        Text(plant.plantType, color = TextMuted)
                                    }
                                    OutlinedButton(onClick = { navController.navigate("editPlant/${plant.id}") }) {
                                        Text("Set pin")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ZoneManagerScreen(viewModel: ZoneViewModel, plantViewModel: PlantViewModel, navController: NavHostController) {
    val state by viewModel.state.collectAsState()
    val plantState by plantViewModel.state.collectAsState()
    var showForm by remember { mutableStateOf(false) }
    var editingZone by remember { mutableStateOf<GardenZoneEntity?>(null) }

    ScreenScaffold("Garden Zones", navController, canGoBack = true) { modifier ->
        LazyColumn(modifier, verticalArrangement = Arrangement.spacedBy(14.dp)) {
            item { ScreenHeader("Garden Zones", "Add photos, descriptions and names for every growing area.") }
            item { ErrorView(state.error) }
            if (state.message.isNotBlank()) {
                item {
                    Card(colors = CardDefaults.cardColors(containerColor = CardCream)) {
                        Text(state.message, Modifier.padding(14.dp), color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
            item {
                GreenPrimaryButton("Add Zone", onClick = {
                    editingZone = null
                    showForm = true
                    viewModel.clearMessages()
                    viewModel.setSelectedImage(null)
                })
            }
            if (showForm) {
                item {
                    ZoneFormCard(
                        zone = editingZone,
                        viewModel = viewModel,
                        onSave = { name, description, imagePath ->
                            val zone = editingZone
                            if (zone == null) {
                                viewModel.addZone(name, description, imagePath)
                            } else {
                                viewModel.updateZone(zone, name, description, imagePath)
                            }
                            showForm = false
                            editingZone = null
                        },
                        onCancel = {
                            showForm = false
                            editingZone = null
                            viewModel.setSelectedImage(null)
                        }
                    )
                }
            }
            item { SectionTitle("Your zones", "${state.zones.size} growing areas") }
            if (state.zones.isEmpty()) {
                item { EmptyStateCard("No zones yet", "Create your first garden zone and add an optional photo.") }
            }
            items(state.zones) { zone ->
                val count = plantState.plants.count { it.zoneId == zone.id }
                ZoneCard(
                    zone = zone,
                    plantCount = count,
                    onEdit = {
                        editingZone = zone
                        showForm = true
                        viewModel.setSelectedImage(zone.imagePath)
                    },
                    onDelete = { viewModel.deleteZone(zone) }
                )
            }
        }
    }
}

@Composable
private fun ZoneFormCard(
    zone: GardenZoneEntity?,
    viewModel: ZoneViewModel,
    onSave: (String, String, String?) -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()
    var name by remember(zone) { mutableStateOf(zone?.name ?: "") }
    var description by remember(zone) { mutableStateOf(zone?.description.orEmpty()) }
    var imagePath by remember(zone, state.selectedImagePath) { mutableStateOf(state.selectedImagePath ?: zone?.imagePath) }
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            val copied = ImageStorageHelper.copyUriToInternalStorage(context, it)
            imagePath = copied
            viewModel.setSelectedImage(copied)
        }
    }

    Card(colors = CardDefaults.cardColors(containerColor = CardCream), elevation = CardDefaults.cardElevation(3.dp)) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(if (zone == null) "Add Zone" else "Edit Zone", style = MaterialTheme.typography.titleLarge, color = GardenDark)
            OutlinedTextField(name, { name = it }, label = { Text("Zone name") }, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth())
            OutlinedTextField(description, { description = it }, label = { Text("Description") }, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth())
            Text("Zone photo", style = MaterialTheme.typography.titleMedium, color = GardenDark)
            LocalImage(imagePath.orEmpty(), Modifier.fillMaxWidth().height(150.dp))
            OutlinedButton(onClick = { imagePicker.launch("image/*") }, modifier = Modifier.fillMaxWidth()) {
                Text(if (imagePath.isNullOrBlank()) "Choose zone photo" else "Replace zone photo")
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                GreenPrimaryButton("Save", { onSave(name, description, imagePath) }, Modifier.weight(1f))
                OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f).height(52.dp), shape = RoundedCornerShape(16.dp)) { Text("Cancel") }
            }
        }
    }
}

@Composable
private fun ZoneCard(zone: GardenZoneEntity, plantCount: Int, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = CardCream), elevation = CardDefaults.cardElevation(3.dp)) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            LocalImage(zone.imagePath.orEmpty(), Modifier.fillMaxWidth().height(160.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(Modifier.weight(1f)) {
                    Text(zone.name, style = MaterialTheme.typography.titleLarge, color = GardenDark, fontWeight = FontWeight.SemiBold)
                    Text(zone.description ?: "No description yet.", color = TextMuted)
                }
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                    Text("$plantCount plants", Modifier.padding(horizontal = 10.dp, vertical = 6.dp), color = GardenDark)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onEdit, modifier = Modifier.weight(1f)) { Text("Edit") }
                OutlinedButton(onClick = onDelete, modifier = Modifier.weight(1f)) { Text("Delete") }
            }
        }
    }
}

@Composable
private fun ZoneSummaryCard(zone: GardenZoneEntity, plantCount: Int, content: @Composable ColumnScope.() -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = CardCream), elevation = CardDefaults.cardElevation(3.dp)) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(Modifier.weight(1f)) {
                    Text(zone.name, style = MaterialTheme.typography.titleMedium, color = GardenDark, fontWeight = FontWeight.SemiBold)
                    Text(zone.description ?: "$plantCount plants in this zone", color = TextMuted)
                }
            }
            content()
        }
    }
}
