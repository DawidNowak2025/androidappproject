package com.growgardentracker.android.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.growgardentracker.android.data.AppContainer
import com.growgardentracker.android.data.local.entity.ActivityLogEntity
import com.growgardentracker.android.data.local.entity.GardenZoneEntity
import com.growgardentracker.android.data.local.entity.PlantEntity
import com.growgardentracker.android.data.local.entity.UserEntity
import com.growgardentracker.android.data.local.entity.WateringHistoryEntity
import com.growgardentracker.android.data.repository.DashboardSummary
import com.growgardentracker.android.data.repository.ResultState
import com.growgardentracker.android.util.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(val user: UserEntity? = null, val isLoading: Boolean = false, val error: String = "")

class AuthViewModel : ViewModel() {
    private val repository = AppContainer.authRepository
    private val _state = MutableStateFlow(AuthUiState())
    val state: StateFlow<AuthUiState> = _state

    init {
        viewModelScope.launch {
            repository.loggedInUserId.collect { userId ->
                if (userId == null) {
                    _state.update { it.copy(user = null) }
                } else {
                    _state.update { it.copy(user = repository.getLoggedInUser(userId)) }
                }
            }
        }
    }

    fun register(name: String, email: String, password: String) = viewModelScope.launch {
        _state.update { it.copy(isLoading = true, error = "") }
        when (val result = repository.register(name, email, password)) {
            is ResultState.Success -> _state.update { it.copy(user = result.data, isLoading = false) }
            is ResultState.Error -> _state.update { it.copy(error = result.message, isLoading = false) }
        }
    }

    fun login(email: String, password: String) = viewModelScope.launch {
        _state.update { it.copy(isLoading = true, error = "") }
        when (val result = repository.login(email, password)) {
            is ResultState.Success -> _state.update { it.copy(user = result.data, isLoading = false) }
            is ResultState.Error -> _state.update { it.copy(error = result.message, isLoading = false) }
        }
    }

    fun logout() = viewModelScope.launch {
        repository.logout()
        _state.value = AuthUiState()
    }
}

data class PlantUiState(
    val plants: List<PlantEntity> = emptyList(),
    val selectedPlant: PlantEntity? = null,
    val history: List<WateringHistoryEntity> = emptyList(),
    val error: String = ""
)

class PlantViewModel : ViewModel() {
    private val repository = AppContainer.plantRepository
    private val _state = MutableStateFlow(PlantUiState())
    val state: StateFlow<PlantUiState> = _state

    init {
        viewModelScope.launch {
            repository.plants.collect { plants -> _state.update { it.copy(plants = plants) } }
        }
    }

    fun observePlant(id: Long) = viewModelScope.launch {
        repository.observePlant(id).collect { plant -> _state.update { it.copy(selectedPlant = plant) } }
    }

    fun observeHistory(id: Long) = viewModelScope.launch {
        repository.observeHistory(id).collect { history -> _state.update { it.copy(history = history) } }
    }

    fun savePlant(plant: PlantEntity, onDone: () -> Unit) = viewModelScope.launch {
        val result = if (plant.id == 0L) repository.addPlant(plant) else repository.updatePlant(plant)
        when (result) {
            is ResultState.Success -> onDone()
            is ResultState.Error -> _state.update { it.copy(error = result.message) }
        }
    }

    fun deletePlant(plant: PlantEntity, onDone: () -> Unit = {}) = viewModelScope.launch {
        repository.deletePlant(plant)
        onDone()
    }

    fun waterToday(plant: PlantEntity) = viewModelScope.launch {
        repository.waterToday(plant)
    }

    fun addHistory(plantId: Long, date: String, note: String) = viewModelScope.launch {
        repository.addHistory(plantId, date, note)
    }
}

data class ZoneUiState(
    val zones: List<GardenZoneEntity> = emptyList(),
    val selectedImagePath: String? = null,
    val error: String = "",
    val message: String = ""
)

class ZoneViewModel : ViewModel() {
    private val repository = AppContainer.zoneRepository
    private val _state = MutableStateFlow(ZoneUiState())
    val state: StateFlow<ZoneUiState> = _state

    init {
        viewModelScope.launch {
            repository.zones.collect { zones -> _state.update { it.copy(zones = zones) } }
        }
    }

    fun setSelectedImage(path: String?) {
        _state.update { it.copy(selectedImagePath = path) }
    }

    fun clearMessages() {
        _state.update { it.copy(error = "", message = "") }
    }

    fun addZone(name: String, description: String, imagePath: String?) = viewModelScope.launch {
        when (val result = repository.addZone(name, description, imagePath)) {
            is ResultState.Success -> _state.update { it.copy(error = "", message = "Zone added.", selectedImagePath = null) }
            is ResultState.Error -> _state.update { it.copy(error = result.message) }
        }
    }

    fun updateZone(zone: GardenZoneEntity, name: String, description: String, imagePath: String?) = viewModelScope.launch {
        when (val result = repository.updateZone(zone, name, description, imagePath)) {
            is ResultState.Success -> _state.update { it.copy(error = "", message = "Zone saved.", selectedImagePath = null) }
            is ResultState.Error -> _state.update { it.copy(error = result.message) }
        }
    }

    fun deleteZone(zone: GardenZoneEntity) = viewModelScope.launch {
        when (val result = repository.deleteZone(zone)) {
            is ResultState.Success -> _state.update { it.copy(error = "", message = "Zone deleted.") }
            is ResultState.Error -> _state.update { it.copy(error = result.message, message = "") }
        }
    }
}

data class DashboardUiState(val summary: DashboardSummary = DashboardSummary())

class DashboardViewModel : ViewModel() {
    private val repository = AppContainer.dashboardRepository
    private val _state = MutableStateFlow(DashboardUiState())
    val state: StateFlow<DashboardUiState> = _state

    init {
        viewModelScope.launch {
            repository.summary.collect { summary -> _state.value = DashboardUiState(summary) }
        }
    }
}

data class ActivityUiState(val activity: List<ActivityLogEntity> = emptyList())

class ActivityViewModel : ViewModel() {
    private val repository = AppContainer.activityRepository
    private val _state = MutableStateFlow(ActivityUiState())
    val state: StateFlow<ActivityUiState> = _state

    init {
        viewModelScope.launch {
            repository.activity.collect { activity -> _state.value = ActivityUiState(activity) }
        }
    }
}

data class SettingsUiState(val message: String = "", val error: String = "")

class SettingsViewModel : ViewModel() {
    private val repository = AppContainer.settingsRepository
    private val _state = MutableStateFlow(SettingsUiState())
    val state: StateFlow<SettingsUiState> = _state

    fun exportJson() = viewModelScope.launch {
        when (val result = repository.exportJson()) {
            is ResultState.Success -> _state.value = SettingsUiState(message = "Exported to ${result.data}")
            is ResultState.Error -> _state.value = SettingsUiState(error = result.message)
        }
    }

    fun importJson() = viewModelScope.launch {
        when (val result = repository.importJson()) {
            is ResultState.Success -> _state.value = SettingsUiState(message = "JSON imported.")
            is ResultState.Error -> _state.value = SettingsUiState(error = result.message)
        }
    }
}

data class PlantKnowledge(val name: String, val type: String, val advice: String)

fun localPlantKnowledge(): List<PlantKnowledge> = listOf(
    PlantKnowledge("Tomato", "Vegetable", "Tomatoes like full sun, steady watering, and support as they grow."),
    PlantKnowledge("Mint", "Herb", "Mint grows quickly and is best kept in a pot with regular watering."),
    PlantKnowledge("Dahlia", "Flower", "Dahlias prefer rich soil, sunny places, and watering during dry spells."),
    PlantKnowledge("Peace Lily", "Houseplant", "Peace lilies prefer indirect light and should be watered when the top soil feels dry."),
    PlantKnowledge("Strawberry", "Fruit", "Strawberries need sun, mulch, and consistent moisture during fruiting.")
)

fun localWeatherAdvice(plants: List<PlantEntity>): String {
    val month = DateUtils.currentMonth()
    val season = when (month) {
        12, 1, 2 -> "winter"
        3, 4, 5 -> "spring"
        6, 7, 8 -> "summer"
        else -> "autumn"
    }
    val overdue = plants.count { DateUtils.wateringStatus(it.nextWateringDate) == "Overdue" }
    return when (season) {
        "summer" -> "Summer advice: check soil more often. $overdue plants are overdue."
        "winter" -> "Winter advice: water less often and avoid waterlogged soil. $overdue plants are overdue."
        "spring" -> "Spring advice: growth is active, so keep watering regular. $overdue plants are overdue."
        else -> "Autumn advice: reduce watering slowly as temperatures drop. $overdue plants are overdue."
    }
}
