package com.growgardentracker.android.ui.components

import android.net.Uri

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import com.growgardentracker.android.data.local.entity.PlantEntity
import com.growgardentracker.android.ui.navigation.Routes
import com.growgardentracker.android.ui.theme.BorderSoft
import com.growgardentracker.android.ui.theme.CardCream
import com.growgardentracker.android.ui.theme.DueOrange
import com.growgardentracker.android.ui.theme.FreshMint
import com.growgardentracker.android.ui.theme.GardenDark
import com.growgardentracker.android.ui.theme.OkGreen
import com.growgardentracker.android.ui.theme.SoftGreen
import com.growgardentracker.android.ui.theme.TextMuted
import com.growgardentracker.android.ui.theme.WarningRed
import com.growgardentracker.android.util.DateUtils
import java.io.File
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(title: String, canGoBack: Boolean = false, onBack: () -> Unit = {}) {
    TopAppBar(
        title = { Text(title, fontWeight = FontWeight.SemiBold) },
        navigationIcon = {
            if (canGoBack) {
                IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = SoftGreen,
            titleContentColor = GardenDark,
            navigationIconContentColor = GardenDark
        )
    )
}

@Composable
fun ScreenScaffold(
    title: String,
    navController: NavHostController,
    canGoBack: Boolean = false,
    content: @Composable (Modifier) -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { AppTopBar(title, canGoBack) { navController.popBackStack() } },
        bottomBar = { if (!canGoBack) AppBottomNavigation(navController) }
    ) { padding ->
        content(Modifier.padding(padding).padding(start = 16.dp, top = 10.dp, end = 16.dp, bottom = 16.dp))
    }
}

@Composable
fun AppBottomNavigation(navController: NavHostController) {
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    val items = listOf(
        Triple("Home", Routes.DASHBOARD, Icons.Default.Home),
        Triple("Plants", Routes.PLANTS, Icons.Default.LocalFlorist),
        Triple("Map", Routes.GARDEN_MAP, Icons.Default.Map),
        Triple("AI", Routes.ASSISTANT, Icons.Default.Psychology),
        Triple("Settings", Routes.SETTINGS, Icons.Default.Settings)
    )
    NavigationBar(containerColor = CardCream, tonalElevation = 8.dp) {
        items.forEach { (label, route, icon) ->
            NavigationBarItem(
                selected = currentRoute == route || (route == Routes.PLANTS && currentRoute == Routes.PLANTS_FILTERED),
                onClick = { navController.navigate(route) { launchSingleTop = true } },
                icon = { Icon(icon, contentDescription = label) },
                label = {
                    Text(label, maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 11.sp)
                }
            )
        }
    }
}

@Composable
fun ScreenHeader(title: String, subtitle: String, welcome: String = "") {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = GardenDark),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (welcome.isNotBlank()) Text(welcome, color = FreshMint, style = MaterialTheme.typography.bodyMedium)
            Text(title, color = Color.White, style = MaterialTheme.typography.headlineMedium)
            Text(subtitle, color = FreshMint, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
fun GardenHeroHeader(welcome: String = "Welcome back to your garden") {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(GardenDark, MaterialTheme.colorScheme.primary, FreshMint)
                    )
                )
                .padding(22.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(welcome, color = Color.White.copy(alpha = 0.86f), style = MaterialTheme.typography.bodyLarge)
                Text("GROW Garden Tracker", color = Color.White, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Text("Garden Resource Organisation and Watering", color = Color.White.copy(alpha = 0.90f), style = MaterialTheme.typography.titleMedium)
                Text("Track plants, watering, zones and garden activity in one place.", color = Color.White.copy(alpha = 0.82f), style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

@Composable
fun SectionTitle(title: String, subtitle: String = "") {
    Column(Modifier.fillMaxWidth()) {
        Text(title, style = MaterialTheme.typography.titleLarge, color = GardenDark)
        if (subtitle.isNotBlank()) Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = TextMuted)
    }
}

@Composable
fun DashboardStatCard(
    title: String,
    value: String,
    subtitle: String,
    tint: Color = MaterialTheme.colorScheme.primary,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardCream),
        border = BorderStroke(1.dp, BorderSoft),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = GardenDark, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(value, style = MaterialTheme.typography.headlineLarge, color = tint)
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = TextMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
fun DashboardCard(title: String, value: String, subtitle: String) {
    DashboardStatCard(title, value, subtitle)
}

@Composable
fun QuickActionCard(title: String, subtitle: String, icon: ImageVector, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, BorderSoft),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(44.dp).clip(RoundedCornerShape(14.dp)).background(FreshMint),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = GardenDark)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, color = GardenDark, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = TextMuted)
            }
        }
    }
}

@Composable
fun StatusBadge(status: String) {
    val color = when (status) {
        "Overdue" -> WarningRed
        "Water Today" -> DueOrange
        else -> OkGreen
    }
    Box(
        modifier = Modifier.clip(RoundedCornerShape(100.dp)).background(color.copy(alpha = 0.12f)).padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Text(status, color = color, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
fun PlantCard(plant: PlantEntity, zoneName: String, onClick: () -> Unit) {
    val status = DateUtils.wateringStatus(plant.nextWateringDate)
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, BorderSoft),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            LocalImage(plant.plantImagePath, Modifier.size(88.dp), onClick)
            Column(Modifier.padding(start = 12.dp).weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(plant.name, style = MaterialTheme.typography.titleMedium, color = GardenDark, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Text("${plant.plantType} - $zoneName", style = MaterialTheme.typography.bodyMedium, color = TextMuted)
                Text("Next watering: ${plant.nextWateringDate}", style = MaterialTheme.typography.bodyMedium)
                StatusBadge(status)
            }
        }
    }
}

@Composable
fun LocalImage(path: String, modifier: Modifier = Modifier.fillMaxWidth().height(180.dp), onClick: () -> Unit = {}) {
    if (path.isBlank() || !File(path).exists()) {
        Box(
            modifier.clip(RoundedCornerShape(16.dp)).background(FreshMint),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Image, contentDescription = "No image", modifier = Modifier.size(42.dp), tint = MaterialTheme.colorScheme.primary)
        }
    } else {
        Image(
            painter = rememberAsyncImagePainter(File(path)),
            contentDescription = "Local image",
            contentScale = ContentScale.Crop,
            modifier = modifier.clip(RoundedCornerShape(16.dp)).clickable(onClick = onClick)
        )
    }
}

@Composable
fun GreenPrimaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier.fillMaxWidth()) {
    Button(
        onClick = onClick,
        modifier = modifier.height(52.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
    ) {
        Text(text, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
fun EmptyStateCard(title: String, message: String, actionText: String? = null, onAction: () -> Unit = {}) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = CardCream),
        border = BorderStroke(1.dp, BorderSoft),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.LocalFlorist, contentDescription = null, modifier = Modifier.size(46.dp), tint = MaterialTheme.colorScheme.primary)
            Text(title, style = MaterialTheme.typography.titleLarge, color = GardenDark)
            Text(message, style = MaterialTheme.typography.bodyMedium, color = TextMuted)
            if (actionText != null) GreenPrimaryButton(actionText, onAction)
        }
    }
}

@Composable
fun LoadingView() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
}

@Composable
fun ErrorView(message: String) {
    if (message.isNotBlank()) {
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer), shape = RoundedCornerShape(16.dp)) {
            Text(message, Modifier.padding(12.dp), color = MaterialTheme.colorScheme.onErrorContainer)
        }
        Spacer(Modifier.height(12.dp))
    }
}

@Composable
fun EmptyView(message: String) {
    EmptyStateCard("Nothing here yet", message)
}

@Composable
fun ZonePhotoMapBox(
    zoneImagePath: String?,
    plants: List<PlantEntity>,
    selectedX: Float?,
    selectedY: Float?,
    selectedPlantName: String?,
    allowTap: Boolean,
    onTap: ((Float, Float) -> Unit)?,
    onPlantClick: ((PlantEntity) -> Unit)?,
    modifier: Modifier = Modifier
) {
    var boxSize by remember { mutableStateOf(IntSize.Zero) }
    val pinSizePx = 24
    val imagePath = zoneImagePath.orEmpty()
    if (imagePath.isBlank()) {
        Card(
            modifier = modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = CardCream),
            border = BorderStroke(1.dp, BorderSoft),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Text(
                "This zone has no photo. Add a zone photo first.",
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                color = TextMuted,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        return
    }
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = CardCream),
        border = BorderStroke(1.dp, BorderSoft),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .padding(10.dp)
                .clip(RoundedCornerShape(18.dp))
                .onSizeChanged { boxSize = it }
                .background(Color.White)
                .then(
                    if (allowTap && onTap != null) {
                        Modifier.pointerInput(zoneImagePath) {
                            detectTapGestures { offset ->
                                val width = boxSize.width.takeIf { it > 0 } ?: size.width
                                val height = boxSize.height.takeIf { it > 0 } ?: size.height
                                val x = (offset.x / width).coerceIn(0f, 1f)
                                val y = (offset.y / height).coerceIn(0f, 1f)
                                onTap(x, y)
                            }
                        }
                    } else {
                        Modifier
                    }
                )
        ) {
            AsyncImage(
                model = localImageModel(imagePath),
                contentDescription = "Zone photo",
                contentScale = ContentScale.FillBounds,
                modifier = Modifier.fillMaxSize()
            )
            Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.08f)))
            plants.forEach { plant ->
                val mapX = plant.mapX
                val mapY = plant.mapY
                if (mapX != null && mapY != null) {
                    PlantPin(
                        label = plant.name,
                        modifier = Modifier.offset {
                            IntOffset(
                                x = ((mapX.coerceIn(0f, 1f) * boxSize.width) - (pinSizePx / 2f)).roundToInt(),
                                y = ((mapY.coerceIn(0f, 1f) * boxSize.height) - (pinSizePx / 2f)).roundToInt()
                            )
                        },
                        onClick = { onPlantClick?.invoke(plant) }
                    )
                }
            }
            if (selectedX != null && selectedY != null) {
                PlantPin(
                    label = selectedPlantName ?: "Plant",
                    modifier = Modifier.offset {
                        IntOffset(
                            x = ((selectedX.coerceIn(0f, 1f) * boxSize.width) - (pinSizePx / 2f)).roundToInt(),
                            y = ((selectedY.coerceIn(0f, 1f) * boxSize.height) - (pinSizePx / 2f)).roundToInt()
                        )
                    },
                    onClick = {}
                )
            }
        }
    }
}

private fun localImageModel(path: String): Any {
    return when {
        path.startsWith("content://") || path.startsWith("file://") -> Uri.parse(path)
        else -> File(path)
    }
}

@Composable
private fun PlantPin(label: String, modifier: Modifier, onClick: () -> Unit) {
    Column(modifier.clickable(onClick = onClick), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier.size(24.dp).clip(RoundedCornerShape(100.dp)).background(WarningRed),
            contentAlignment = Alignment.Center
        ) {
            Box(Modifier.size(8.dp).clip(RoundedCornerShape(100.dp)).background(Color.White))
        }
        Box(
            modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(Color.White).padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text(label, color = GardenDark, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
        }
    }
}

object AppIcons {
    val Add = Icons.Default.Add
    val Water = Icons.Default.WaterDrop
    val Zones = Icons.Default.Map
    val Knowledge = Icons.Default.MenuBook
    val Assistant = Icons.Default.Psychology
    val Activity = Icons.Default.ReceiptLong
}
